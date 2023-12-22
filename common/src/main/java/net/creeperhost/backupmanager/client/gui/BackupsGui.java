package net.creeperhost.backupmanager.client.gui;

import com.google.common.collect.Streams;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import net.creeperhost.backupmanager.BackupManager;
import net.creeperhost.backupmanager.providers.Backup;
import net.creeperhost.backupmanager.providers.BackupException;
import net.creeperhost.backupmanager.providers.FTBBackupProvider;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.BackgroundRender;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.GuiProvider;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Axis;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.creeperhost.polylib.client.modulargui.sprite.Material;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 10/12/2023
 */
public class BackupsGui implements GuiProvider {
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    private final Screen superParent;
    private GuiList<Backup> backupList;
    private Backup selected = null;

    public BackupsGui(SelectWorldScreen parent) {
        this.superParent = parent.lastScreen;
        BackupManager.refreshBackups();
    }

    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return BMStyle.Flat.background(gui);
    }

    @Override
    public void buildGui(ModularGui gui) {
        gui.renderScreenBackground(false);
        gui.initFullscreenGui();
        gui.setGuiTitle(new TranslatableComponent("backupmanager:gui.backups.title"));

        GuiElement<?> root = gui.getRoot();

        GuiText title = new GuiText(root, gui.getGuiTitle())
                .constrain(TOP, relative(root.get(TOP), 5))
                .constrain(HEIGHT, Constraint.literal(8))
                .constrain(LEFT, match(root.get(LEFT)))
                .constrain(RIGHT, match(root.get(RIGHT)));

        GuiElement<?> listBackground = new GuiRectangle(root).fill(0x80202020)
                .constrain(LEFT, relative(root.get(LEFT), 10))
                .constrain(RIGHT, relative(root.get(RIGHT), -10))
                .constrain(TOP, relative(root.get(TOP), 20))
                .constrain(BOTTOM, relative(root.get(BOTTOM), -24));

        GuiButton back = BMStyle.Flat.button(root, new TranslatableComponent("backupmanager:button.back_arrow"))
                .onPress(() -> gui.mc().setScreen(new SelectWorldScreen(superParent)))
                .constrain(BOTTOM, relative(listBackground.get(TOP), -4))
                .constrain(LEFT, match(listBackground.get(LEFT)))
                .constrain(WIDTH, literal(50))
                .constrain(HEIGHT, literal(12));

        GuiButton restore = BMStyle.Flat.buttonPrimary(root, new TranslatableComponent("backupmanager:button.restore_backup"))
                .setTooltip(new TranslatableComponent("backupmanager:button.restore_backup.info"))
                .setDisabled(() -> selected == null)
                .onPress(() -> restoreSelected(gui))
                .constrain(TOP, relative(listBackground.get(BOTTOM), 5))
                .constrain(LEFT, match(listBackground.get(LEFT)))
                .constrain(WIDTH, literal(150))
                .constrain(HEIGHT, literal(14));

        GuiButton delete = BMStyle.Flat.buttonCaution(root, new TranslatableComponent("backupmanager:button.delete_backup"))
                .onPress(() -> deleteSelected(gui))
                .setDisabled(() -> selected == null)
                .constrain(TOP, relative(listBackground.get(BOTTOM), 5))
                .constrain(RIGHT, match(listBackground.get(RIGHT)))
                .constrain(WIDTH, literal(150))
                .constrain(HEIGHT, literal(14));


        backupList = new GuiList<>(listBackground);
        backupList.setDisplayBuilder(BackupElement::new);
        Constraints.bind(backupList, listBackground, 2);

        var scrollBar = BMStyle.Flat.scrollBar(root, Axis.Y);
        scrollBar.container
                .setEnabled(() -> backupList.hiddenSize() > 0)
                .constrain(TOP, match(listBackground.get(TOP)))
                .constrain(BOTTOM, match(listBackground.get(BOTTOM)))
                .constrain(LEFT, relative(listBackground.get(RIGHT), 2))
                .constrain(WIDTH, literal(6));
        scrollBar.primary
                .setScrollableElement(backupList)
                .setSliderState(backupList.scrollState());

        updateList(false);
    }

    private void deleteSelected(ModularGui gui) {
        if (selected == null) return;
        try {
            selected.delete();
            selected = null;
            updateList(true);
        } catch (BackupException ex) {
            OptionDialog.simpleInfoDialog(gui, new TranslatableComponent("backupmanager:gui.backups.error_occurred", ex.getComponent().copy().withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.RED));
        }
    }

    private void restoreSelected(ModularGui gui) {
        if (selected == null) return;
        TextInputDialog dialog = new TextInputDialog(gui.getRoot(), new TranslatableComponent("backupmanager:gui.backups.restore_name"));
        dialog.setResultCallback(name -> {
            try {
                selected.restore(name);
                selected = null;
                OptionDialog.simpleInfoDialog(gui, new TranslatableComponent("backupmanager:gui.backups.restored").withStyle(ChatFormatting.GREEN));
            } catch (BackupException ex) {
                OptionDialog.simpleInfoDialog(gui, new TranslatableComponent("backupmanager:gui.backups.error_occurred", ex.getComponent().copy().withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.RED));
            }
        });
    }

    private void updateList(boolean refresh) {
        if (refresh) BackupManager.refreshBackups();
        List<Backup> backups = new ArrayList<>(BackupManager.getBackups().values());
        backups.sort(Comparator.comparingLong(Backup::creationTime).reversed());
        backupList.getList().clear();
        backupList.getList().addAll(backups);
        backupList.rebuildElements();
    }

    public class BackupElement extends GuiElement<BackupElement> implements BackgroundRender {
        private final Backup backup;

        public BackupElement(@NotNull GuiParent<?> parent, Backup backup) {
            super(parent);
            this.backup = backup;
            this.constrain(HEIGHT, Constraint.literal(24 + (backup.infoText().size() * 9)));

            int leftOffset = 3;
            if (backup instanceof FTBBackupProvider.FTBBackup b && b.preview != null && !b.preview.isEmpty()) {
                if (b.getIcon() == null) {
                    b.setIcon(FaviconTexture.forWorld(mc().getTextureManager(), Util.sanitizeName(b.sha1, ResourceLocation::validPathChar)));
                    try {
                        String encoded = b.preview.replace("data:image/png;base64, ", "");
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)));
                        BufferedImage scaled = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                        scaled.createGraphics().drawImage(image, 0, 0, scaled.getWidth(), scaled.getHeight(), 0, 0, image.getWidth(), image.getWidth(), null);

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ImageIO.write(scaled, "png", bos);
                        b.getIcon().upload(NativeImage.read(new ByteArrayInputStream(bos.toByteArray())));
                    } catch (Throwable e) {
                        BackupManager.LOGGER.error("Failed to load backup preview", e);
                    }
                }
            } else {
                File zip = new File(backup.backupLocation());
                if (backup.getIcon() == null && zip.exists() && zip.getName().endsWith(".zip")) {
                    backup.setIcon(FaviconTexture.forWorld(mc().getTextureManager(), Util.sanitizeName(backup.backupLocation(), ResourceLocation::validPathChar)));
                    try (FileSystem fs = FileSystems.newFileSystem(zip.toPath())) {
                        List<Path> paths = Streams.stream(fs.getRootDirectories()).filter(Files::isDirectory).toList();
                        Path worldDir = null;

                        int i = 0;
                        while (paths.size() == 1 && (i++) < 10) {
                            worldDir = paths.get(0);
                            paths = Files.list(worldDir).filter(Files::isDirectory).toList();
                        }

                        if (worldDir != null && i < 10) {
                            Path icon = worldDir.resolve("icon.png");
                            if (Files.exists(icon)) {
                                backup.getIcon().upload(NativeImage.read(Files.newInputStream(icon)));
                            }
                        }
                    } catch (Throwable e) {
                        BackupManager.LOGGER.error("Failed to load backup preview", e);
                    }
                }
            }

            if (backup.getIcon() != null) {
                leftOffset = (int) ySize() - 2;
                GuiTexture icon = new GuiTexture(this, () -> Material.fromRawTexture(backup.getIcon().textureLocation()))
                        .constrain(TOP, relative(this.get(TOP), 1))
                        .constrain(LEFT, relative(this.get(LEFT), 1))
                        .constrain(HEIGHT, literal(leftOffset))
                        .constrain(WIDTH, literal(leftOffset));
                leftOffset += 3;
            }

            GuiText name = new GuiText(this, new TextComponent(backup.name() + (backup.displayName().isEmpty() ? "" : " (" + backup.displayName() + ")")))
                    .setShadow(false)
                    .setAlignment(Align.LEFT)
                    .constrain(TOP, relative(get(TOP), 3))
                    .constrain(LEFT, relative(get(LEFT), leftOffset))
                    .constrain(RIGHT, relative(get(RIGHT), -2))
                    .constrain(HEIGHT, literal(8));

            GuiText created = new GuiText(this, new TranslatableComponent("backupmanager:gui.backups.created", DATE_TIME_FORMAT.format(backup.creationTime())).withStyle(ChatFormatting.GRAY))
                    .setShadow(false)
                    .setAlignment(Align.LEFT)
                    .constrain(TOP, relative(name.get(BOTTOM), 2))
                    .constrain(LEFT, relative(get(LEFT), leftOffset))
                    .constrain(RIGHT, relative(get(RIGHT), -2))
                    .constrain(HEIGHT, literal(8));

            Component provText = new TextComponent(backup.backupProvider());
            GuiText provider = new GuiText(this, provText)
                    .setTooltip(new TranslatableComponent("backupmanager:gui.backups.provider", backup.backupProvider()))
                    .setShadow(false)
                    .setAlignment(Align.RIGHT)
                    .constrain(TOP, relative(get(TOP), 3))
                    .constrain(WIDTH, literal(font().width(provText)))
                    .constrain(RIGHT, relative(get(RIGHT), -2))
                    .constrain(HEIGHT, literal(8));

            if (!backup.infoText().isEmpty()) {
                GuiTextList info = new GuiTextList(this, backup.infoText())
                        .setHorizontalAlign(Align.MIN)
                        .constrain(LEFT, relative(get(LEFT), leftOffset))
                        .constrain(RIGHT, relative(get(RIGHT), -2))
                        .constrain(BOTTOM, relative(get(BOTTOM), -2))
                        .autoHeight();
            }

            setTooltip(backup.hoverText());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver()) {
                selected = backup;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void renderBehind(GuiRender render, double mouseX, double mouseY, float partialTicks) {
            boolean isSelected = selected == backup;
            render.borderRect(getRectangle(), 1, (isMouseOver() || isSelected) ? 0x10FFFFFF : 0, BMStyle.Flat.listEntryBorder(isSelected));
        }
    }

    @Environment (EnvType.CLIENT)
    public static class FaviconTexture implements AutoCloseable {
        private static final ResourceLocation MISSING_LOCATION = new ResourceLocation("textures/misc/unknown_server.png");
        private final TextureManager textureManager;
        private final ResourceLocation textureLocation;
        @Nullable
        private DynamicTexture texture;
        private boolean closed;

        private FaviconTexture(TextureManager textureManager, ResourceLocation resourceLocation) {
            this.textureManager = textureManager;
            this.textureLocation = resourceLocation;
        }

        public static FaviconTexture forWorld(TextureManager textureManager, String string) {
            String var10006 = Util.sanitizeName(string, ResourceLocation::validPathChar);
            return new FaviconTexture(textureManager, new ResourceLocation("minecraft", "worlds/" + var10006 + "/" + Hashing.sha1().hashUnencodedChars(string) + "/icon"));
        }

        public void upload(NativeImage nativeImage) {
            if (nativeImage.getWidth() == 64 && nativeImage.getHeight() == 64) {
                try {
                    this.checkOpen();
                    if (this.texture == null) {
                        this.texture = new DynamicTexture(nativeImage);
                    } else {
                        this.texture.setPixels(nativeImage);
                        this.texture.upload();
                    }

                    this.textureManager.register(this.textureLocation, this.texture);
                } catch (Throwable var3) {
                    nativeImage.close();
                    this.clear();
                    throw var3;
                }
            } else {
                nativeImage.close();
                int var10002 = nativeImage.getWidth();
                throw new IllegalArgumentException("Icon must be 64x64, but was " + var10002 + "x" + nativeImage.getHeight());
            }
        }

        public void clear() {
            this.checkOpen();
            if (this.texture != null) {
                this.textureManager.release(this.textureLocation);
                this.texture.close();
                this.texture = null;
            }
        }

        public ResourceLocation textureLocation() {
            return this.texture != null ? this.textureLocation : MISSING_LOCATION;
        }

        public void close() {
            this.clear();
            this.closed = true;
        }

        private void checkOpen() {
            if (this.closed) {
                throw new IllegalStateException("Icon already closed");
            }
        }
    }
}

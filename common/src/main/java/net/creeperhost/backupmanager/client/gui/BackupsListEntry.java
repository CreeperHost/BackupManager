package net.creeperhost.backupmanager.client.gui;

import net.creeperhost.backupmanager.mixin.SelectWorldScreenAccessor;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class BackupsListEntry extends WorldSelectionList.Entry {
    private final SelectWorldScreen parentScreen;

    public BackupsListEntry(SelectWorldScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public Component getNarration() {
        return Component.translatable("backupmanager:button.backups_entry");
    }

    private void openBackups() {
        Minecraft minecraft = Minecraft.getInstance();
        WorldSelectionList list = ((SelectWorldScreenAccessor) parentScreen).backupmanager$getList();
        list.setSelected(null);
        list.setFocused(null);
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
        minecraft.gui.setScreen(new ModularGuiScreen(new BackupsGui(parentScreen), parentScreen));
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 1) {
            openBackups();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isSelection()) {
            openBackups();
            return true;
        }
        return super.keyPressed(event);
    }

    private Rectangle buttonBounds() {
        return Rectangle.create(getContentX(), getContentY(), getContentWidth(), 18);
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        GuiRender render = new GuiRender(graphics);
        Rectangle bounds = buttonBounds();
        boolean highlighted = bounds.contains(mouseX, mouseY) || isFocused();
        render.borderRect(bounds, 1, 0xFF000000, highlighted ? 0xFFFFFFFF : 0xFF606060);
        render.drawCenteredString(getNarration(), bounds.x() + bounds.width() / 2D, bounds.y() + 5,
                highlighted ? 0xFF66FF00 : 0xFFFFFFFF, false);
    }
}

package net.creeperhost.backupmanager.client.gui;

import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * Created by brandon3055 on 11/12/2023
 */
public class BackupsListEntry extends WorldSelectionList.Entry {
    private boolean mouseOver = false;
    private final Screen parentScreen;

    public BackupsListEntry(Screen parentScreen) {
        this.parentScreen = parentScreen;
        setHeight(18);
    }

    @Override
    public Component getNarration() {
        return Component.empty();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (mouseOver) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            Minecraft.getInstance().setScreenAndShow(new ModularGuiScreen(new BackupsGui(parentScreen), parentScreen));
            return true;
        }

        return super.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean mouseOverEntry, float partialTicks) {
        GuiRender render = new GuiRender(graphics);

        Rectangle bounds = Rectangle.create(getX(), getY(), getWidth(), 18);
        mouseOver = bounds.contains(mouseX, mouseY);

        render.borderRect(bounds, 1, 0xFF000000, mouseOver ? 0xFFFFFFFF : 0xFF606060);
        render.drawCenteredString(Component.translatable("backupmanager:button.backups_entry"), bounds.x() + (getWidth() / 2D), bounds.y() + 5, mouseOver ? 0xFF66FF00 : 0xFFFFFFFF, false);
    }
}

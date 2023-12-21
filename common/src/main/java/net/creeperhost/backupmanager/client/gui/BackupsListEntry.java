package net.creeperhost.backupmanager.client.gui;

import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * Created by brandon3055 on 11/12/2023
 */
public class BackupsListEntry extends WorldSelectionList.Entry {
    private boolean mouseOver = false;
    private final SelectWorldScreen parentScreen;

    public BackupsListEntry(SelectWorldScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public Component getNarration() {
        return Component.empty();
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (mouseOver) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            Minecraft.getInstance().setScreen(new ModularGuiScreen(new BackupsGui(parentScreen), parentScreen));
            return true;
        }

        return super.mouseClicked(d, e, i);
    }

    @Override
    public void render(GuiGraphics graphics, int index, int yPos, int xPos, int width, int height, int mouseX, int mouseY, boolean mouseOverEntry, float partialTicks) {
        GuiRender render = new GuiRender(Minecraft.getInstance(), graphics.pose(), graphics.bufferSource());

        Rectangle bounds = Rectangle.create(xPos, yPos, width, 18);
        mouseOver = bounds.contains(mouseX, mouseY);

        render.borderRect(bounds, 1, 0xFF000000, mouseOver ? 0xFFFFFFFF : 0xFF606060);
        render.drawCenteredString(Component.translatable("backupmanager:button.backups_entry"), bounds.x() + (width / 2D), bounds.y() + 5, mouseOver ? 0x66FF00 : 0xFFFFFF, false);
    }
}

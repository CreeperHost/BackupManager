//package net.creeperhost.backupmanager.client;
//
//import net.creeperhost.backupmanager.BackupManager;
//import net.creeperhost.backupmanager.client.gui.BackupsGui;
//import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
//import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
//import net.creeperhost.polylib.client.modulargui.lib.geometry.Rectangle;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.client.resources.sounds.SimpleSoundInstance;
//import net.minecraft.network.chat.Component;
//import net.minecraft.sounds.SoundEvents;
//import net.minecraft.world.level.storage.LevelSummary;
//
///**
// * Created by brandon3055 on 09/12/2023
// */
//public class WorldEntryHooks {
//    private static LevelSummary hoveredSummary = null;
//
//    public static void renderWorldEntry(LevelSummary summary, GuiGraphics graphics, int index, int yPos, int xPos, int width, int height, int mouseX, int mouseY, boolean mouseOverEntry, float partialTicks) {
//        if (BackupManager.getBackups().isEmpty()) return;
//
//        GuiRender render = new GuiRender(Minecraft.getInstance(), graphics.pose(), graphics.bufferSource());
//        Component text = Component.translatable("backupmanager:button.backups");
//        int btnWidth = render.font().width(text) + 4;
//        Rectangle bounds = Rectangle.create(xPos + width - 4 - btnWidth + 1, yPos - 1, btnWidth, 12);
//        boolean mouseOver = bounds.contains(mouseX, mouseY);
//
//        render.borderRect(bounds, 1, 0xFF000000, mouseOver ? 0xFFFFFFFF : 0xFF606060);
//        render.drawString(text, bounds.x() + 2, bounds.y() + 2, mouseOver ? 0x66FF00 : 0xFFFFFF, false);
//
//        if (mouseOver) {
//            hoveredSummary = summary;
//        } else if (hoveredSummary == summary) {
//            hoveredSummary = null;
//        }
//    }
//
//    public static boolean entryClicked(LevelSummary summary, Screen screen, double mouseX, double mouseY, int button) {
//        if (hoveredSummary == null || summary != hoveredSummary) return false;
//        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
//        Minecraft.getInstance().setScreen(new ModularGuiScreen(new BackupsGui(hoveredSummary), screen));
//        return true;
//        return false;
//    }
//}

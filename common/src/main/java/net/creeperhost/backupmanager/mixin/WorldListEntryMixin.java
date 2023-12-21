//package net.creeperhost.backupmanager.mixin;
//
//import net.creeperhost.backupmanager.client.WorldEntryHooks;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
//import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
//import net.minecraft.world.level.storage.LevelSummary;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
///**
// * Created by brandon3055 on 09/12/2023
// */
//@Mixin (WorldSelectionList.WorldListEntry.class)
//public class WorldListEntryMixin {
//
//    @Shadow @Final private LevelSummary summary;
//    @Shadow @Final private SelectWorldScreen screen;
//
//    @Inject (
//            method = "<init>",
//            at = @At ("TAIL")
//    )
//    public void init(WorldSelectionList worldSelectionList, WorldSelectionList worldSelectionList2, LevelSummary levelSummary, CallbackInfo ci) {
////        BackupHandler.refreshBackups(levelSummary);
//    }
//
//    @Inject (
//            method = "render",
//            at = @At ("TAIL")
//    )
//    public void render(GuiGraphics graphics, int index, int yPos, int xPos, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks, CallbackInfo ci) {
//        WorldEntryHooks.renderWorldEntry(summary, graphics, index, yPos, xPos, width, height, mouseX, mouseY, hovered, partialTicks);
//    }
//
//    @Inject (
//            method = "mouseClicked",
//            at = @At ("HEAD"),
//            cancellable = true
//    )
//    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
//        if (WorldEntryHooks.entryClicked(summary, screen, mouseX, mouseY, button)) {
//            cir.setReturnValue(true);
//        }
//    }
//
//
//
//}

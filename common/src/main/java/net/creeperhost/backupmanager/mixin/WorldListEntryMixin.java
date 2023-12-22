package net.creeperhost.backupmanager.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.backupmanager.client.WorldEntryHooks;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by brandon3055 on 09/12/2023
 */
@Mixin (WorldSelectionList.WorldListEntry.class)
public class WorldListEntryMixin {

    @Shadow @Final private SelectWorldScreen screen;

    @Inject (
            method = "render",
            at = @At ("TAIL")
    )
    public void render(PoseStack poseStack, int index, int yPos, int xPos, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks, CallbackInfo ci) {
        WorldEntryHooks.renderWorldEntry(screen, poseStack, index, yPos, xPos, width, height, mouseX, mouseY, hovered, partialTicks);
    }
}

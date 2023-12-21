package net.creeperhost.backupmanager.mixin;

import net.creeperhost.backupmanager.BackupManager;
import net.creeperhost.backupmanager.client.gui.BackupsListEntry;
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
@Mixin (WorldSelectionList.class)
public class WorldSelectionListMixin {

    @Shadow @Final private SelectWorldScreen screen;

    private WorldSelectionList getThis() {
        return (WorldSelectionList) (Object) this;
    }

    @Inject (
            method = "notifyListUpdated",
            at = @At ("HEAD")
    )
    private void notifyListUpdated(CallbackInfo ci) {
        getThis().children().removeIf(entry -> entry instanceof BackupsListEntry);
        if (BackupManager.hasBackups()) {
            getThis().children().add(new BackupsListEntry(screen));
        }
    }
}

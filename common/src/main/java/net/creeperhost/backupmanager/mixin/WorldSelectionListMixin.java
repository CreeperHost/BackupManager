package net.creeperhost.backupmanager.mixin;

import net.creeperhost.backupmanager.client.gui.BackupsListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionList.class)
public abstract class WorldSelectionListMixin extends ObjectSelectionList<WorldSelectionList.Entry> {
    @Shadow @Final private Screen screen;

    protected WorldSelectionListMixin(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    @Inject(method = "notifyListUpdated", at = @At("HEAD"))
    private void backupmanager$addBackupsEntry(CallbackInfo ci) {
        // WorldSelectionList is also used by Realms upload screens in 26.3.
        if (!(screen instanceof SelectWorldScreen selectWorldScreen)) return;
        removeEntries(children().stream()
                .filter(entry -> entry instanceof BackupsListEntry)
                .toList());
        addEntry(new BackupsListEntry(selectWorldScreen));
    }
}

package net.creeperhost.backupmanager.mixin;

import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin (SelectWorldScreen.class)
public interface SelectWorldScreenMixin {

    @Accessor ("list")
    WorldSelectionList backupmanager$getList();

}

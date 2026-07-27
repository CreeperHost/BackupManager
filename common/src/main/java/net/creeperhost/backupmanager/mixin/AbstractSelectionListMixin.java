package net.creeperhost.backupmanager.mixin;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Created by brandon3055 on 27/07/2026
 */
@Mixin (AbstractSelectionList.class)
public interface AbstractSelectionListMixin {

    @Accessor ("children")
    List backupmanager$getChildren();
}

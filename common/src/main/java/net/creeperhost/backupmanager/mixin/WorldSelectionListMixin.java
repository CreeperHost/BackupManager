package net.creeperhost.backupmanager.mixin;

import net.creeperhost.backupmanager.client.WorldEntryHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created by brandon3055 on 09/12/2023
 */
@Mixin (WorldSelectionList.class)
public abstract class WorldSelectionListMixin extends AbstractSelectionList {

    @Shadow
    @Final
    private SelectWorldScreen screen;

    public WorldSelectionListMixin(Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
    }

    @Override
    protected int getMaxPosition() {
        return super.getMaxPosition() + 20;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (WorldEntryHooks.click(screen, d, e, i)) return true;
        return super.mouseClicked(d, e, i);
    }
}

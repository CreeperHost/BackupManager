package net.creeperhost.backupmanager.neoforge;

import net.creeperhost.backupmanager.BackupManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod(value = BackupManager.MOD_ID, dist = Dist.CLIENT)
public class BackupManagerNeoForge {
    public BackupManagerNeoForge() {
        BackupManager.init(FMLPaths.GAMEDIR.get());
    }
}

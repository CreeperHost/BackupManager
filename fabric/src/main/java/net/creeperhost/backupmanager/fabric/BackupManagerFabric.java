package net.creeperhost.backupmanager.fabric;

import net.creeperhost.backupmanager.BackupManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class BackupManagerFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BackupManager.init(FabricLoader.getInstance().getGameDir());
    }
}

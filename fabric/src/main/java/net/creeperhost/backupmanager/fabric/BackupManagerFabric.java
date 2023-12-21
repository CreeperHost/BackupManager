package net.creeperhost.backupmanager.fabric;

import net.creeperhost.backupmanager.BackupManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class BackupManagerFabric implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        try {
            BackupManager.init();
        } catch (IOException e) {
            LOGGER.error("An error occurred while attempting initialize", e);
        }
    }
}

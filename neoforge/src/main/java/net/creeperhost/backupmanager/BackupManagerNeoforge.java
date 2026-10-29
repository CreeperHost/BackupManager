package net.creeperhost.backupmanager;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(BackupManager.MOD_ID)
public class BackupManagerNeoforge {
    public static final Logger LOGGER = LogManager.getLogger();

    public BackupManagerNeoforge(IEventBus eventBus) {
        try {
            BackupManager.init();
        } catch (IOException e) {
            LOGGER.error("An error occurred while attempting initialize", e);
        }
    }
}
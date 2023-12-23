package net.creeperhost.backupmanager.neoforge;

import net.creeperhost.backupmanager.BackupManager;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod (BackupManager.MOD_ID)
public class BackupManagerNeoForge {
    public static final Logger LOGGER = LogManager.getLogger();

    public BackupManagerNeoForge() {
//        EventBuses.registerModEventBus(BackupManager.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        try {
            BackupManager.init();
        } catch (IOException e) {
            LOGGER.error("An error occurred while attempting initialize", e);
        }
    }
}

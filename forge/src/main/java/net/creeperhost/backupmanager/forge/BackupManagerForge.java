package net.creeperhost.backupmanager.forge;

import dev.architectury.platform.forge.EventBuses;
import net.creeperhost.backupmanager.BackupManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod (BackupManager.MOD_ID)
public class BackupManagerForge {
    public static final Logger LOGGER = LogManager.getLogger();

    public BackupManagerForge() {
        EventBuses.registerModEventBus(BackupManager.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        try {
            BackupManager.init();
        } catch (IOException e) {
            LOGGER.error("An error occurred while attempting initialize", e);
        }
    }
}

package net.creeperhost.backupmanager;

import dev.architectury.platform.Platform;
import net.creeperhost.backupmanager.providers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackupManager {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "backupmanager";

    private static Path mcPath;
    private static Path savesPath;
    private static List<BackupProvider> providerList = new ArrayList<>();
    private static Map<String, Backup> backups = new HashMap<>();

    public static void init() throws IOException {
        mcPath = Platform.getGameFolder();
        savesPath = mcPath.resolve("saves");

        if (Platform.isModLoaded("ftbbackups2")) {
            addProvider(new FTBBackupProvider());
        }
        if (Platform.isModLoaded("simplebackups")) {
            addProvider(new SimpleBackupsProvider());
        }
        addProvider(new VanillaBackupProvider());
    }

    public static void addProvider(BackupProvider provider) {
        providerList.add(provider);
    }

    public static void refreshBackups() {
        backups.clear();
        for (BackupProvider provider : providerList) {
            try {
                List<Backup> results = provider.getBackups();
                for (Backup result : results) {
                    if (!backups.containsKey(result.backupLocation())) {
                        backups.put(result.backupLocation(), result);
                    }
                }
            } catch (IOException ex) {
                BackupManager.LOGGER.error("An error occurred while attempting to get backups for provider: {}", provider.getClass().getName(), ex);
            }
        }
    }

    public static boolean hasBackups() {
        if (backups.isEmpty()) refreshBackups();
        return !backups.isEmpty();
    }

    public static Map<String, Backup> getBackups() {
        return backups;
    }

    public static Path getMcPath() {
        return mcPath;
    }

    public static Path getSavesPath() {
        return savesPath;
    }
}
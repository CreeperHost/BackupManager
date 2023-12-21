package net.creeperhost.backupmanager.providers;

import net.creeperhost.backupmanager.BackupManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 10/12/2023
 */
public class VanillaBackupProvider implements BackupProvider {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    @Override
    public List<Backup> getBackups() throws IOException {
        Path backups = BackupManager.getMcPath().resolve("backups");
        List<Backup> results = new ArrayList<>();
        List<Path> files = Files.list(backups).toList();

        for (Path file : files) {
            if (!file.getFileName().toString().endsWith(".zip")) continue;
            String fileName = file.getFileName().toString();

            if (!fileName.contains("_")) continue;
            int i = fileName.indexOf("_");
            int dateEnd = fileName.indexOf("_", i + 1);
            if (dateEnd == -1 || fileName.length() <= dateEnd + 1) continue;

            long timestamp;
            try {
                timestamp = DATE_FORMAT.parse(fileName.substring(0, dateEnd)).getTime();
            } catch (ParseException e) {
                continue;
            }

            String name = fileName.substring(dateEnd + 1).replace(".zip", "");
            results.add(new VanillaBackup(file.toAbsolutePath().toString(), name, timestamp));
        }

        return results;
    }

    public static class VanillaBackup implements Backup {
        private transient FaviconTexture icon = null;
        private final String location;
        private final String name;
        private final long timestamp;

        public VanillaBackup(String location, String name, long timestamp) {
            this.location = location;
            this.name = name;
            this.timestamp = timestamp;
        }

        @Override
        public FaviconTexture getIcon() {
            return icon;
        }

        @Override
        public void setIcon(FaviconTexture icon) {
            this.icon = icon;
        }

        @Override
        public List<Component> hoverText() {
            return List.of(Component.translatable("backupmanager:gui.backups.file_location"), Component.literal(location).withStyle(ChatFormatting.GRAY));
        }

        @Override
        public String backupLocation() {
            return location;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String displayName() {
            return "";
        }

        @Override
        public long creationTime() {
            return timestamp;
        }

        @Override
        public String backupProvider() {
            return ChatFormatting.GRAY + "Vanilla / Unknown";
        }

        @Override
        public void delete() throws BackupException {
            File backup = new File(location);
            if (!backup.isFile()) {
                throw new BackupException(Component.literal("Backup file not found: " + location));
            }

            if (!backup.delete()) {
                throw new BackupException(Component.literal("Failed do delete backup file! You may need to delete the file manually: " + location));
            }
        }

        @Override
        public void restore(String restoreName) throws BackupException {
            FileSystem fileSystem = FileSystems.getDefault();
            Path backup = fileSystem.getPath(location);

            if (!Files.exists(backup)) {
                throw new BackupException(Component.literal("Backup file not found: " + location));
            }

            Path worldFolder = BackupManager.getSavesPath().resolve(getWorldFolderName(restoreName));
            Path temp = getTempDirectory();
            unzip(backup, temp);

            smartMove(temp, worldFolder);
            try {
                setWorldName(worldFolder, restoreName);
            } catch (BackupException ex) {
                throw new BackupException(Component.literal("World was extracted but name could not be set. \nReason:\n").append(ex.getComponent()));
            } finally {
                try {
                    if (Files.exists(temp)) {
                        FileUtils.deleteDirectory(temp.toFile());
                    }
                } catch (IOException e) {
                    BackupManager.LOGGER.error("Backup was restored but the temporary directory could not be deleted. " + e);
                }
            }
        }
    }
}

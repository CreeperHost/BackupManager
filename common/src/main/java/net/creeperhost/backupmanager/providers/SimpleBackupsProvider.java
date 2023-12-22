package net.creeperhost.backupmanager.providers;

import net.creeperhost.backupmanager.BackupManager;
import net.creeperhost.backupmanager.client.gui.BackupsGui.FaviconTexture;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by brandon3055 on 10/12/2023
 */
public class SimpleBackupsProvider implements BackupProvider {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    @Override
    public List<Backup> getBackups() throws IOException {
        Path config = BackupManager.getMcPath().resolve("config/simplebackups-common.toml");
        if (!Files.exists(config)) {
            return Collections.emptyList();
        }

        String backupDir = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(config)))){
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("outputPath") && line.contains("=")) {
                    backupDir = line.substring(line.indexOf("=") + 1).trim().replace("\"", "");
                    break;
                }
            }
        }

        if (backupDir == null) {
            return Collections.emptyList();
        }

        Path backups = BackupManager.getMcPath().resolve(backupDir);
        if (!Files.isDirectory(backups)) {
            return Collections.emptyList();
        }

        List<Backup> results = new ArrayList<>();
        List<Path> files = Files.list(backups).toList();

        for (Path file : files) {
            if (!file.getFileName().toString().endsWith(".zip")) continue;
            String fileName = file.getFileName().toString();
            if (!fileName.contains("_")) continue;

            int i = fileName.lastIndexOf("_");
            String date = fileName.substring(i);
            fileName = fileName.substring(0, i);
            if (!fileName.contains("_")) continue;
            i = fileName.lastIndexOf("_");
            date = (fileName.substring(i+1) + date).replace(".zip", "");
            fileName = fileName.substring(0, i);

            long timestamp;
            try {
                timestamp = DATE_FORMAT.parse(date).getTime();
            } catch (ParseException e) {
                continue;
            }

            results.add(new SimpleBackup(file.toAbsolutePath().toString(), fileName, timestamp));
        }

        return results;
    }

    public static class SimpleBackup implements Backup {
        private transient FaviconTexture icon = null;
        private final String location;
        private final String name;
        private final long timestamp;

        public SimpleBackup(String location, String name, long timestamp) {
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
            return List.of(new TranslatableComponent("backupmanager:gui.backups.file_location"), new TextComponent(location).withStyle(ChatFormatting.GRAY));
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
            return ChatFormatting.GRAY + "Simple Backups";
        }

        @Override
        public void delete() throws BackupException {
            File backup = new File(location);
            if (!backup.isFile()) {
                throw new BackupException(new TextComponent("Backup file not found: " + location));
            }

            if (!backup.delete()) {
                throw new BackupException(new TextComponent("Failed do delete backup file! You may need to delete the file manually: " + location));
            }
        }

        @Override
        public void restore(String restoreName) throws BackupException {
            FileSystem fileSystem = FileSystems.getDefault();
            Path backup = fileSystem.getPath(location);

            if (!Files.exists(backup)) {
                throw new BackupException(new TextComponent("Backup file not found: " + location));
            }

            Path worldFolder = BackupManager.getSavesPath().resolve(getWorldFolderName(restoreName));
            Path temp = getTempDirectory();
            unzip(backup, temp);

            smartMove(temp, worldFolder);
            try {
                setWorldName(worldFolder, restoreName);
            } catch (BackupException ex) {
                throw new BackupException(new TextComponent("World was extracted but name could not be set. \nReason:\n").append(ex.getComponent()));
            }
            finally {
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

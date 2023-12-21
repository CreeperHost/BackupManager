package net.creeperhost.backupmanager.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.creeperhost.backupmanager.BackupManager;
import net.creeperhost.backupmanager.client.gui.BackupsGui.FaviconTexture;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by brandon3055 on 10/12/2023
 */
public class FTBBackupProvider implements BackupProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Override
    public List<Backup> getBackups() {
        BackupsStore backupsStore = readBackupsJson();
        if (backupsStore == null) {
            return Collections.emptyList();
        }

        FileSystem fileSystem = FileSystems.getDefault();
        List<Backup> results = new ArrayList<>();
        for (FTBBackup backup : backupsStore.backups) {
            Path path = fileSystem.getPath(backup.backupLocation);
            if (!backup.complete || !Files.exists(path)) continue;
            if (backup.snapshot) {
                backup.info.add(Component.literal("Snapshot").withStyle(ChatFormatting.YELLOW));
            }
            results.add(backup);
        }

        return results;
    }

    private static BackupsStore readBackupsJson() {
        Path directory = BackupManager.getMcPath().resolve("backups");
        Path json = directory.resolve("backups.json");
        if (!Files.isDirectory(directory) || !Files.exists(json)) {
            return null;
        }

        try (FileReader reader = new FileReader(json.toFile())) {
            return GSON.fromJson(reader, BackupsStore.class);
        } catch (Exception e) {
            BackupManager.LOGGER.error("An error occurred loading FTB backups json {}", json.toAbsolutePath().toAbsolutePath(), e);
            return null;
        }
    }

    private static boolean writeBackupsJson(BackupsStore store) {
        Path directory = BackupManager.getMcPath().resolve("backups");
        Path json = directory.resolve("backups.json");
        if (!Files.isDirectory(directory)) {
            return false;
        }

        try (FileWriter writer = new FileWriter(json.toFile())) {
            GSON.toJson(store, writer);
        } catch (Exception e) {
            BackupManager.LOGGER.error("An error occurred loading FTB backups json {}", json.toAbsolutePath().toAbsolutePath(), e);
            return false;
        }
        return true;
    }

    private static class BackupsStore {
        private List<FTBBackup> backups = new ArrayList<>();
    }

    public static class FTBBackup implements Backup {
        private String worldName;
        private long createTime;
        private String backupLocation;
        private long size;
        private float ratio;
        public String sha1;
        public String preview;
        private boolean snapshot;
        private String backupName;
        private Format backupFormat;
        private boolean complete;

        private transient List<Component> info = new ArrayList<>();
        private transient FaviconTexture icon = null;

        @Override
        public List<Component> hoverText() {
            return List.of(Component.translatable("backupmanager:gui.backups.file_location"), Component.literal(backupLocation).withStyle(ChatFormatting.GRAY));
        }

        @Override
        public String backupLocation() {
            return backupLocation;
        }

        @Override
        public String name() {
            return worldName;
        }

        @Override
        public String displayName() {
            return backupName;
        }

        @Override
        public long creationTime() {
            return createTime;
        }

        @Override
        public String backupProvider() {
            return ChatFormatting.GREEN + "FTB Backups 2";
        }

        @Override
        public List<Component> infoText() {
            return info;
        }

        @Override
        public void delete() throws BackupException {
            BackupsStore backupsStore = readBackupsJson();
            if (backupsStore == null) {
                throw new BackupException(Component.literal("Failed to read backups json file."));
            }

            if (backupFormat != Format.ZIP) {
                //TODO Support directory format
                throw new BackupException(Component.literal("Backup manager currently only supports backups using the zip format! (Found: " + backupFormat + ")"));
            }

            File backup = new File(backupLocation);
            if (!backup.isFile()) {
                throw new BackupException(Component.literal("Backup file not found: " + backupLocation));
            }

            boolean removed = backupsStore.backups.removeIf(e -> Objects.equals(e.sha1, sha1) && Objects.equals(e.worldName, worldName) && Objects.equals(e.createTime, createTime));
            if (!removed) {
                throw new BackupException(Component.literal("Could not find backup in the FTBBackups 2 'backups.json' file."));
            }

            if (!writeBackupsJson(backupsStore)) {
                throw new BackupException(Component.literal("Failed to update the FTBBackups 2 'backups.json' file."));
            }

            if (!backup.delete()) {
                throw new BackupException(Component.literal("Backup entry was removed from the FTBBackups 2 'backups.json' file, But the actual backup file could not be deleted! You may need to delete the file manually: " + backupLocation));
            }
        }

        @Override
        public void restore(String restoreName) throws BackupException {
            if (backupFormat != Format.ZIP) {
                //TODO Support directory format
                throw new BackupException(Component.literal("Backup manager currently only supports backups using the zip format! (Found: " + backupFormat + ")"));
            }

            FileSystem fileSystem = FileSystems.getDefault();
            Path backup = fileSystem.getPath(backupLocation);
            if (!Files.exists(backup)) {
                throw new BackupException(Component.literal("Backup file not found: " + backupLocation));
            }

            Path worldFolder = BackupManager.getSavesPath().resolve(getWorldFolderName(restoreName));
            Path temp = getTempDirectory();
            unzip(backup, temp);

            Path extracted = temp.resolve("saves/" + worldName);
            Path levelDat = extracted.resolve("level.dat");
            if (!Files.isDirectory(extracted) || !Files.exists(levelDat)) {
                try {
                    FileUtils.deleteDirectory(temp.toFile());
                } catch (IOException ignored) {}
                throw new BackupException(Component.literal("Zip file contents do not match the expected structure of an FTB Backups 2 zip! Can not proceed."));
            }

            setWorldName(extracted, restoreName);
            smartMove(extracted, worldFolder);

            try {
                FileUtils.deleteDirectory(temp.toFile());
            } catch (IOException e) {
                BackupManager.LOGGER.error("Backup was restored but the temporary directory could not be deleted. " + e);
                throw new BackupException(Component.literal("Backup was restored but the temporary directory could not be deleted: " + temp.toAbsolutePath()));
            }
        }

        @Override
        public FaviconTexture getIcon() {
            return icon;
        }

        @Override
        public void setIcon(FaviconTexture icon) {
            this.icon = icon;
        }
    }

    public enum Format {
        ZIP,
        DIRECTORY
    }
}

package net.creeperhost.backupmanager.providers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.creeperhost.backupmanager.BackupManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by brandon3055 on 10/12/2023
 */
public class SimpleBackupsProvider implements BackupProvider {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final Gson GSON = new Gson();

    @Override
    public List<Backup> getBackups() throws IOException {
        Path config = findConfig();
        if (config == null) {
            return Collections.emptyList();
        }

        String backupDir = readConfigValue(config, "outputPath");
        if (backupDir == null || backupDir.isBlank()) {
            return Collections.emptyList();
        }

        Path configuredPath;
        try {
            configuredPath = Path.of(backupDir);
        } catch (InvalidPathException ex) {
            BackupManager.LOGGER.warn("Ignoring invalid Simple Backups output path: {}", backupDir, ex);
            return Collections.emptyList();
        }

        Path backups = configuredPath.isAbsolute()
                ? configuredPath.normalize()
                : BackupManager.getMcPath().resolve(configuredPath).normalize();
        if (!Files.isDirectory(backups)) {
            return Collections.emptyList();
        }

        List<Backup> results = new ArrayList<>();
        try (var stream = Files.walk(backups, 4)) {
            for (Path path : stream.toList()) {
                if (Files.isRegularFile(path) && path.getFileName().toString().equals("metadata.json")) {
                    SimpleBackup backup = readChainBackup(path.getParent(), backups);
                    if (backup != null) {
                        results.add(backup);
                    }
                } else if (Files.isRegularFile(path) && path.getFileName().toString().endsWith(".zip")) {
                    SimpleBackup backup = readLegacyBackup(path);
                    if (backup != null) {
                        results.add(backup);
                    }
                }
            }
        }

        return results;
    }

    private Path findConfig() {
        Path configDirectory = BackupManager.getMcPath().resolve("config");
        Path current = configDirectory.resolve("simplebackups/common.toml");
        if (Files.isRegularFile(current)) {
            return current;
        }

        Path legacy = configDirectory.resolve("simplebackups-common.toml");
        return Files.isRegularFile(legacy) ? legacy : null;
    }

    private String readConfigValue(Path config, String key) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(config)))){
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(key) && line.contains("=")) {
                    return line.substring(line.indexOf("=") + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }

    private SimpleBackup readLegacyBackup(Path file) {
        String fileName = file.getFileName().toString();
        if (fileName.equals("full.zip") || !fileName.contains("_")) {
            return null;
        }

        int i = fileName.lastIndexOf("_");
        String date = fileName.substring(i);
        fileName = fileName.substring(0, i);
        if (!fileName.contains("_")) {
            return null;
        }

        i = fileName.lastIndexOf("_");
        date = (fileName.substring(i + 1) + date).replace(".zip", "");
        String worldName = fileName.substring(0, i);

        try {
            long timestamp = DATE_FORMAT.parse(date).getTime();
            return new SimpleBackup(file.toAbsolutePath().toString(), worldName, timestamp);
        } catch (ParseException ignored) {
            return null;
        }
    }

    private SimpleBackup readChainBackup(Path chainDirectory, Path backupsRoot) {
        try (BufferedReader reader = Files.newBufferedReader(chainDirectory.resolve("metadata.json"))) {
            JsonObject metadata = GSON.fromJson(reader, JsonObject.class);
            if (metadata == null || !isZipChain(metadata)) {
                return null;
            }

            Path fullBackup = resolveChainFile(chainDirectory, metadata.get("fullBackup"));
            if (fullBackup == null || !Files.isRegularFile(fullBackup)) {
                return null;
            }

            SimpleBackupType backupType = readBackupType(metadata);
            if (backupType == null) {
                return null;
            }

            List<Path> children = readChainChildren(chainDirectory, metadata);
            if (children == null) {
                return null;
            }

            List<Path> archives = selectRestoreArchives(fullBackup, children, backupType);

            String worldName = readWorldName(fullBackup);
            if (worldName == null || worldName.isBlank()) {
                Path relative = backupsRoot.relativize(chainDirectory);
                worldName = relative.getNameCount() > 1 ? relative.getName(0).toString() : "World";
            }

            long timestamp = getChainTimestamp(metadata, chainDirectory);
            return new SimpleBackup(fullBackup.toAbsolutePath().toString(), worldName, timestamp,
                    chainDirectory.toAbsolutePath(), archives, backupType);
        } catch (Exception ex) {
            BackupManager.LOGGER.warn("Failed to read Simple Backups chain at {}", chainDirectory, ex);
            return null;
        }
    }

    private boolean isZipChain(JsonObject metadata) {
        JsonElement format = metadata.get("format");
        return format == null || format.getAsString().equalsIgnoreCase("ZIP");
    }

    private SimpleBackupType readBackupType(JsonObject metadata) {
        JsonElement value = metadata.get("backupType");
        if (value == null || value.isJsonNull()) {
            return null;
        }

        try {
            return SimpleBackupType.valueOf(value.getAsString().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private List<Path> readChainChildren(Path chainDirectory, JsonObject metadata) {
        List<Path> children = new ArrayList<>();
        JsonArray values = metadata.getAsJsonArray("children");
        if (values == null) {
            return children;
        }

        for (JsonElement value : values) {
            Path archive = resolveChainFile(chainDirectory, value);
            if (archive == null || !Files.isRegularFile(archive)) {
                return null;
            }
            children.add(archive);
        }
        return children;
    }

    private List<Path> selectRestoreArchives(Path fullBackup, List<Path> children, SimpleBackupType backupType) {
        List<Path> archives = new ArrayList<>();
        archives.add(fullBackup);

        switch (backupType) {
            case FULL_BACKUPS -> {
            }
            case INCREMENTAL -> archives.addAll(children);
            case DIFFERENTIAL -> {
                if (!children.isEmpty()) {
                    archives.add(children.getLast());
                }
            }
        }
        return archives;
    }

    private Path resolveChainFile(Path chainDirectory, JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return null;
        }

        try {
            Path resolved = chainDirectory.resolve(value.getAsString()).normalize();
            return resolved.startsWith(chainDirectory.normalize()) ? resolved : null;
        } catch (InvalidPathException ex) {
            return null;
        }
    }

    private long getChainTimestamp(JsonObject metadata, Path chainDirectory) {
        JsonElement lastUpdated = metadata.get("lastUpdated");
        if (lastUpdated != null) {
            try {
                return lastUpdated.getAsLong();
            } catch (RuntimeException ignored) {
            }
        }

        try {
            return DATE_FORMAT.parse(chainDirectory.getFileName().toString()).getTime();
        } catch (ParseException ignored) {
            try {
                return Files.getLastModifiedTime(chainDirectory).toMillis();
            } catch (IOException ex) {
                return 0;
            }
        }
    }

    private String readWorldName(Path archive) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String entryName = entry.getName().replace('\\', '/');
                int separator = entryName.indexOf('/');
                if (separator > 0) {
                    return entryName.substring(0, separator);
                }
            }
        }
        return null;
    }

    private enum SimpleBackupType {
        FULL_BACKUPS("backupmanager:gui.backups.simplebackups.type.full"),
        INCREMENTAL("backupmanager:gui.backups.simplebackups.type.incremental"),
        DIFFERENTIAL("backupmanager:gui.backups.simplebackups.type.differential");

        private final String translationKey;

        SimpleBackupType(String translationKey) {
            this.translationKey = translationKey;
        }
    }

    public static class SimpleBackup implements Backup {
        private transient FaviconTexture icon = null;
        private final String location;
        private final String name;
        private final long timestamp;
        private final Path chainDirectory;
        private final List<Path> archives;
        private final SimpleBackupType backupType;

        public SimpleBackup(String location, String name, long timestamp) {
            this(location, name, timestamp, null, List.of(Path.of(location)), SimpleBackupType.FULL_BACKUPS);
        }

        private SimpleBackup(String location, String name, long timestamp, Path chainDirectory, List<Path> archives,
                             SimpleBackupType backupType) {
            this.location = location;
            this.name = name;
            this.timestamp = timestamp;
            this.chainDirectory = chainDirectory;
            this.archives = List.copyOf(archives);
            this.backupType = backupType;
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
            return ChatFormatting.GRAY + "Simple Backups";
        }

        @Override
        public List<Component> infoText() {
            return List.of(Component.translatable(
                    "backupmanager:gui.backups.simplebackups.type",
                    Component.translatable(backupType.translationKey)
            ).withStyle(ChatFormatting.GRAY));
        }

        @Override
        public void delete() throws BackupException {
            if (chainDirectory != null) {
                if (!Files.isDirectory(chainDirectory)) {
                    throw new BackupException(Component.literal("Backup directory not found: " + chainDirectory));
                }

                try {
                    FileUtils.deleteDirectory(chainDirectory.toFile());
                    return;
                } catch (IOException ex) {
                    BackupManager.LOGGER.error("Failed to delete Simple Backups chain: {}", chainDirectory, ex);
                    throw new BackupException(Component.literal("Failed to delete backup directory: " + chainDirectory));
                }
            }

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
            for (Path archive : archives) {
                if (!Files.isRegularFile(archive)) {
                    throw new BackupException(Component.literal("Backup file not found: " + archive));
                }
            }

            Path worldFolder = BackupManager.getSavesPath().resolve(getWorldFolderName(restoreName));
            Path temp = getTempDirectory();
            for (Path archive : archives) {
                unzip(archive, temp);
            }

            smartMove(temp, worldFolder);
            try {
                setWorldName(worldFolder, restoreName);
            } catch (BackupException ex) {
                throw new BackupException(Component.literal("World was extracted but name could not be set. \nReason:\n").append(ex.getComponent()));
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

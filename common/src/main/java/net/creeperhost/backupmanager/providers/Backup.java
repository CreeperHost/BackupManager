package net.creeperhost.backupmanager.providers;

import net.creeperhost.backupmanager.BackupManager;
import net.creeperhost.backupmanager.client.gui.BackupsGui;
import net.creeperhost.backupmanager.client.gui.BackupsGui.FaviconTexture;
import net.minecraft.FileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by brandon3055 on 10/12/2023
 */
public interface Backup {

    String backupLocation();

    String name();

    String displayName();

    long creationTime();

    String backupProvider();

    default List<Component> hoverText() {
        return Collections.emptyList();
    }

    /**
     * @return Additional info text to be displayed as part of the backup entry.
     */
    default List<Component> infoText() {
        return Collections.emptyList();
    }

    void delete() throws BackupException;

    void restore(String restoreName) throws BackupException;

    default void clear() {
        if (getIcon() != null) {
            getIcon().close();
            setIcon(null);
        }

    }

    FaviconTexture getIcon();

    void setIcon(FaviconTexture icon);

    default String getWorldFolderName(String worldName) throws BackupException {
        worldName = worldName.trim();
        if (worldName.isEmpty()) throw new BackupException(Component.literal("World name can not be empty!"));
        Path savesFolder = BackupManager.getSavesPath();

        try {
            return FileUtil.findAvailableName(savesFolder, worldName, "");
        } catch (Exception ex) {
            BackupManager.LOGGER.error("Could not create save folder!", ex);
            throw new BackupException(Component.literal("Could not create save folder! " + ex.getMessage()));
        }
    }

    default void unzip(Path zipFile, Path outputFolder) throws BackupException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                Path newFile = outputFolder.resolve(zipEntry.getName());

                if (!newFile.startsWith(outputFolder)) {
                    BackupManager.LOGGER.error("Entry is outside of the target dir: " + zipEntry.getName());
                    throw new BackupException(Component.literal("Invalid zip file"));
                }

                if (zipEntry.isDirectory()) {
                    if (!Files.isDirectory(newFile)) {
                        Files.createDirectories(newFile);
                    }
                } else {
                    Path parent = newFile.getParent();
                    if (!Files.isDirectory(parent)) {
                        Files.createDirectories(parent);
                    }

                    try (OutputStream fos = Files.newOutputStream(newFile)) {
                        IOUtils.copy(zis, fos);
                    }
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        } catch (Throwable e) {
            BackupManager.LOGGER.error("En error occurred while trying to unzip file: {}", zipFile.toAbsolutePath().toString(), e);
            throw new BackupException(Component.literal("Failed to unzip file. See log for details."));
        }
    }

    default Path getTempDirectory() throws BackupException {
        int i = 0;
        Path tempPath;
        do {
            tempPath = BackupManager.getSavesPath().resolve(".backup-extract." + i + ".temp");
            i++;
            if (i > 1000) {
                throw new BackupException(Component.literal("Failed to create temp file after 1000 attempts."));
            }
        } while (Files.exists(tempPath) || Files.isDirectory(tempPath));
        return tempPath;
    }

    default void setWorldName(Path worldFolder, String name) throws BackupException {
        Path levelDat = worldFolder.resolve("level.dat");
        if (!Files.exists(levelDat)) {
            throw new BackupException(Component.literal("Failed to set world name because level.dat file could not be found!"));
        }

        try {
            CompoundTag levelTag = NbtIo.readCompressed(levelDat.toFile());
            CompoundTag data = levelTag.getCompound("Data");
            if (!data.contains("LevelName")) {
                throw new BackupException(Component.literal("Failed to set world name because level.dat file is not valid"));
            }
            data.putString("LevelName", name);
            NbtIo.writeCompressed(levelTag, levelDat.toFile());
        } catch (IOException ex) {
            BackupManager.LOGGER.error("An error occurred while attempting to update level.dat", ex);
            throw new BackupException(Component.literal("An error occurred while attempting to update level.dat"));
        }
    }

    default void smartMove(Path extractedBackup, Path worldFolder) throws BackupException {
        Path worldPath = extractedBackup;

        try {
            while (!Files.exists(worldPath.resolve("level.dat"))) {
                List<Path> subDirs = Files.list(worldPath).filter(Files::isDirectory).toList();
                if (subDirs.size() == 1) {
                    worldPath = subDirs.get(0);
                } else {
                    throw new BackupException(Component.literal("Could not locate level.dat file inside the extracted backup."));
                }
            }

            Files.move(worldPath, worldFolder);
        } catch (IOException e) {
            BackupManager.LOGGER.error("An error occurred while attempting to move extracted world!" + e);
            throw new BackupException(Component.literal("An error occurred while attempting to move extracted world!"));
        }
    }
}

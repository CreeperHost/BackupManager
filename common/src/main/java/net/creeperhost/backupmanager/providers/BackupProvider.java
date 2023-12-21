package net.creeperhost.backupmanager.providers;

import java.io.IOException;
import java.util.List;

/**
 * Created by brandon3055 on 10/12/2023
 */
public interface BackupProvider {

    List<Backup> getBackups() throws IOException;

}

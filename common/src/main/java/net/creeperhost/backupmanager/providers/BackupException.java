package net.creeperhost.backupmanager.providers;

import net.minecraft.network.chat.Component;

/**
 * Created by brandon3055 on 14/12/2023
 */
public class BackupException extends Exception {
    private final Component message;

    public BackupException(Component message) {
        this.message = message;
    }

    public Component getComponent() {
        return message;
    }
}

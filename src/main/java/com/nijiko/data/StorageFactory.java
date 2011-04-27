package com.nijiko.data;

import java.io.File;
import java.io.IOException;

import org.bukkit.util.config.Configuration;

import com.nijikokun.bukkit.Permissions.Permissions;

//TODO: Remove duplicated code
public class StorageFactory {
    private static Configuration config;

    public static final void setConfig(Configuration storageConfig) {
        StorageFactory.config = storageConfig;
    }

    public final static UserStorage getUserStorage(String world)
            throws IOException {
        if (world == null)
            return null;
        if (config == null)
            return null;
        world = world.toLowerCase();
        int delay = 6000;
        boolean autosave = true;
        config.load();
        String parent = config.getString("permissions.storage.worldcopy."
                + world);
        if (parent != null)
            world = parent;
        String userWorld = config
                .getString("permissions.storage.user.worldcopy." + world);
        if (userWorld == null)
            userWorld = world;
        parent = null;
        String typename = config.getString("permissions.storage.type",
                StorageType.YAML.toString());
        String userWorldtype = config
                .getString("permissions.storage.user.worldtype." + userWorld);
        if (userWorldtype == null || userWorldtype.isEmpty())
            userWorldtype = typename;

        delay = config.getInt("permissions.storage.reload", 6000); // Default is
                                                                   // 5 minutes
        autosave = config.getBoolean("permissions.storage.autosave", true);

        StorageType type = StorageType.YAML;
        try {
            type = StorageType.valueOf(userWorldtype);
        } catch (IllegalArgumentException e) {
            System.err
                    .println("Error occurred while selecting permissions user config type. Reverting to YAML.");
            type = StorageType.YAML;
        }

        switch (type) {
        case SQL:
            String dbms = config
                    .getString("permissions.storage.dbms", "SQLITE");
            String uri = config.getString("permissions.storage.uri",
                    "jdbc:sqlite:" + Permissions.instance.getDataFolder()
                            + File.separator + "permissions.db");
            String username = config.getString("permissions.storage.username");
            String password = config.getString("permissions.storage.password");
            try {
                SqlStorage.init(dbms, uri, username, password, delay);
                return new SqlUserStorage(userWorld);
            } catch (Exception e) {
                System.err
                        .println("Error occured while connecting to SQL database. Reverting to YAML.");
            }
            // Will fall through only if an exception occurs
        default:
        case YAML:
            String worldString = Permissions.instance.getDataFolder().getPath()
                    + File.separator + userWorld;
            File worldFolder = new File(worldString);
            if (!worldFolder.exists())
                worldFolder.mkdirs();
            if (!worldFolder.isDirectory())
                throw new IOException("World folder for world " + userWorld
                        + " is not a directory.");
            File userFile = new File(worldString, "users.yml");
            if (!userFile.exists())
                userFile.createNewFile();
            if (!userFile.isFile())
                throw new IOException("User config for world " + userWorld
                        + " is not a file.");
            if (!userFile.canRead())
                throw new IOException("User config for world " + userWorld
                        + " cannot be read.");
            if (!userFile.canWrite())
                throw new IOException("User config for world " + userWorld
                        + " cannot be written to.");
            return new YamlUserStorage(new Configuration(userFile), userWorld,
                    delay, autosave);
        }
    }

    public final static GroupStorage getGroupStorage(String world)
            throws IOException {
        if (world == null)
            return null;
        if (config == null)
            return null;
        world = world.toLowerCase();
        int delay = 6000;
        boolean autosave = true;
        config.load();
        String parent = config.getString("permissions.storage.worldcopy."
                + world);
        if (parent != null)
            world = parent;
        String groupWorld = config
                .getString("permissions.storage.group.worldcopy." + world);
        if (groupWorld == null)
            groupWorld = world;
        parent = null;
        String typename = config.getString("permissions.storage.type",
                StorageType.YAML.toString());
        String groupWorldtype = config
                .getString("permissions.storage.group.worldtype." + groupWorld);
        if (groupWorldtype == null || groupWorldtype.isEmpty())
            groupWorldtype = typename;

        delay = config.getInt("permissions.storage.reload", 6000); // Default is
                                                                   // 5 minutes
        autosave = config.getBoolean("permissions.storage.autosave", true);

        StorageType type = StorageType.YAML;
        try {
            type = StorageType.valueOf(groupWorldtype);
        } catch (IllegalArgumentException e) {
            System.err
                    .println("Error occurred while selecting permissions group config type. Reverting to YAML.");
            type = StorageType.YAML;
        }

        switch (type) {
        case SQL:
            String dbms = config
                    .getString("permissions.storage.dbms", "SQLITE");
            String uri = config.getString("permissions.storage.uri",
                    "jdbc:sqlite:" + Permissions.instance.getDataFolder()
                            + File.separator + "permissions.db");
            String username = config.getString("permissions.storage.username");
            String password = config.getString("permissions.storage.password");
            try {
                SqlStorage.init(dbms, uri, username, password, delay);
                return new SqlGroupStorage(groupWorld);
            } catch (Exception e) {
                System.err
                        .println("Error occured while connecting to SQL database. Reverting to YAML.");
            }
            // Will fall through only if an exception occurs
        default:
        case YAML:
            String worldString = Permissions.instance.getDataFolder().getPath()
                    + File.separator + groupWorld;
            File worldFolder = new File(worldString);
            if (!worldFolder.exists())
                worldFolder.mkdirs();
            if (!worldFolder.isDirectory())
                throw new IOException("World folder for world " + groupWorld
                        + " is not a directory.");
            File userFile = new File(worldString, "groups.yml");
            if (!userFile.exists())
                userFile.createNewFile();
            if (!userFile.isFile())
                throw new IOException("Group config for world " + groupWorld
                        + " is not a file.");
            if (!userFile.canRead())
                throw new IOException("Group config for world " + groupWorld
                        + " cannot be read.");
            if (!userFile.canWrite())
                throw new IOException("Group config for world " + groupWorld
                        + " cannot be written to.");
            return new YamlGroupStorage(new Configuration(userFile),
                    groupWorld, delay, autosave);
        }
    }

    private StorageFactory() {
    }
}

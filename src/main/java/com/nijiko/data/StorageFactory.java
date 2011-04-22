package com.nijiko.data;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.util.config.Configuration;

import com.nijikokun.bukkit.Permissions.Permissions;

public class StorageFactory {
    private static Configuration config;
    public static final IStorage createInstance(String world, Configuration config) throws IOException
    {
        StorageFactory.config = config;
        return createInstance(world);
    }

    public static final IStorage createInstance(String world) throws IOException
    {
        world = world.toLowerCase();
        config.load();
        String typename = config.getString("permissions.storage.type",StorageType.YAML.toString());
        String worldtype = config.getString("permissions.storage.worlds." + world);
        if(worldtype!=null&&!worldtype.isEmpty()) typename = worldtype;
        StorageType type;
        try
        {
            type = StorageType.valueOf(typename);
        }
        catch(IllegalArgumentException e)
        {
            System.err.println("Error occurred while selecting permissions config type. Reverting to YAML.");
            type = StorageType.YAML;
        }

        int delay = config.getInt("permissions.storage.reload", 6000); //Default is 5 minutes
        switch(type)
        {
        case SQL:
            String dbms = config.getString("permissions.storage.dbms","SQLITE");
            String uri = config.getString("permissions.storage.uri","jdbc:sqlite:"+Permissions.instance.getDataFolder()+File.separator+"permissions.db");
            String username = config.getString("permissions.storage.username");
            String password = config.getString("permissions.storage.password");
            try {
                SqlStorage.init(dbms,uri,username,password, delay);
                return new SqlStorage(world);
            } catch (Exception e) {
                System.err.println("Error occured while connecting to SQL database. Reverting to YAML.");
            }
            //Will fall through only if an exception occurs  
        default:     
        case YAML:
            boolean autoSave = config.getBoolean("permissions.storage.autosave", true); //Default is 5 minutes
            String worldString = Permissions.instance.getDataFolder().getPath() + world;
            File userFile = new File(worldString + "users.yml");
            if(!userFile.isFile()) throw new IOException("User config for world "+ world +" is not a file.");
            if(!userFile.canRead()) throw new IOException("User config for world "+ world +" cannot be read.");
            if(!userFile.canWrite()) throw new IOException("User config for world "+ world +" cannot be written to.");
            if(!userFile.exists()) userFile.createNewFile();
            File groupFile = new File(worldString + "groups.yml");
            if(!groupFile.isFile()) throw new IOException("Group config for world "+ world +" is not a file.");
            if(!groupFile.canRead()) throw new IOException("Group config for world "+ world +" cannot be read.");
            if(!groupFile.canWrite()) throw new IOException("Group config for world "+ world +" cannot be written to.");
            if(!groupFile.exists()) groupFile.createNewFile();
            return new YamlStorage(new Configuration(userFile), new Configuration(groupFile), world, delay, autoSave);
        }
    }

}

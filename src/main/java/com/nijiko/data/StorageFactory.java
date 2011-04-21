package com.nijiko.data;

import java.io.File;
import java.io.IOException;

import org.bukkit.util.config.Configuration;

import com.nijikokun.bukkit.Permissions.Permissions;

public class StorageFactory {
    public static final IStorage createInstance(String world, Configuration config) throws IOException
    {
        config.load();
        String typename = config.getString("permissions.storage.type",StorageType.YAML.toString());
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
        
        switch(type)
        {
        default:
            System.err.println("Error occurred while creating instance of config. Reverting to YAML.");
        case YAML:
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
            return new YamlStorage(new Configuration(userFile), new Configuration(groupFile));
        case SQL:
            String driver = config.getString("permissions.storage.driver");
            String uri = config.getString("permissions.storage.uri");
            String username = config.getString("permissions.storage.username");
            String password = config.getString("permissions.storage.password");
            return new SqlStorage(driver,uri,username,password);
            
        }        
    }
    
    
}

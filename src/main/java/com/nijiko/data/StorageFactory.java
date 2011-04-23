package com.nijiko.data;

import java.io.File;
import java.io.IOException;

import org.bukkit.util.config.Configuration;

import com.nijikokun.bukkit.Permissions.Permissions;

public class StorageFactory {
    private static Configuration config;
    
    public static final void setConfig(Configuration config)
    {
        StorageFactory.config = config;
    }
    public static final IStorage createInstance(String world, Configuration config) throws IOException
    {
        StorageFactory.config = config;
        return createInstance(world);
    }

    public static final IStorage createInstance(String world) throws IOException
    {
        if(world==null)return null;
        world = world.toLowerCase();
        StorageType type = StorageType.YAML;
        int delay = 6000;
        boolean autoSave = true;
        if(config!=null)
        {
            config.load();
            String parent = config.getString("permissions.storage.worldcopy." + world);
            if(parent!=null) world = parent;
            String userWorld = config.getString("permissions.storage.worldusercopy." + world);
            if(userWorld==null) userWorld = world;
            String parentWorld = config.getString("permissions.storage.worldparentcopy." + world);
            if(parentWorld==null) userWorld = world;
            parent = null;
            String typename = config.getString("permissions.storage.type",StorageType.YAML.toString());
            String worldtype = config.getString("permissions.storage.worldtype." + world);
            if(worldtype!=null&&!worldtype.isEmpty()) typename = worldtype;
            try
            {
                type = StorageType.valueOf(typename);
            }
            catch(IllegalArgumentException e)
            {
                System.err.println("Error occurred while selecting permissions config type. Reverting to YAML.");
                type = StorageType.YAML;
            }

            delay = config.getInt("permissions.storage.reload", 6000); //Default is 5 minutes
            autoSave = config.getBoolean("permissions.storage.autosave", true); //Default is 5 minutes
        }
       
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
            String worldString = Permissions.instance.getDataFolder().getPath() + File.separator + world;
            File userFile = new File(worldString , "users.yml");
            if(!userFile.isFile()) throw new IOException("User config for world "+ world +" is not a file.");
            if(!userFile.canRead()) throw new IOException("User config for world "+ world +" cannot be read.");
            if(!userFile.canWrite()) throw new IOException("User config for world "+ world +" cannot be written to.");
            if(!userFile.exists()) userFile.createNewFile();
            File groupFile = new File(worldString , "groups.yml");
            if(!groupFile.isFile()) throw new IOException("Group config for world "+ world +" is not a file.");
            if(!groupFile.canRead()) throw new IOException("Group config for world "+ world +" cannot be read.");
            if(!groupFile.canWrite()) throw new IOException("Group config for world "+ world +" cannot be written to.");
            if(!groupFile.exists()) groupFile.createNewFile();
            return new YamlStorage(new Configuration(userFile), new Configuration(groupFile), world, delay, autoSave);
        }
    }

    public final static YamlStorage createInstance(String world, Configuration userConfig, Configuration groupConfig)
    {
        return new YamlStorage(userConfig, groupConfig , world, 6000, true);
    }
}

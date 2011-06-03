package com.nijiko.data;

import org.bukkit.util.config.Configuration;

public interface StorageCreator {
    public UserStorage getUserStorage(String world, int reload, boolean autosave, Configuration config) throws Exception;
    public GroupStorage getGroupStorage(String world, int reload, boolean autosave, Configuration config) throws Exception;
}

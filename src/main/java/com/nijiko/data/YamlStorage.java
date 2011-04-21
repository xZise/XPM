package com.nijiko.data;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.EntryType;

public class YamlStorage implements IStorage {
    
    private final Configuration userConfig;
    private final Configuration groupConfig;
    private final ReentrantReadWriteLock rwl;
    
    YamlStorage(Configuration userConfig, Configuration groupConfig) {
        this.userConfig = userConfig;
        this.groupConfig = groupConfig;
        this.rwl = new ReentrantReadWriteLock(false); //Give writer threads a higher priority than reader threads
        reload(false);
    }
    @Override
    public void save() {
        rwl.writeLock().lock();
        //TODO: Apply changes in cache to file
        userConfig.save();
        groupConfig.save();
        rwl.writeLock().unlock();
    }
    @Override
    public void reload(final boolean applyChanges)
    {
        userConfig.load();
        groupConfig.load();
        if(applyChanges) this.save();
    }

    @Override
    public boolean isDefault(String world, String name) {
        rwl.readLock().lock();
        boolean isDefault = false;
        //TODO: Read
        rwl.readLock().unlock();
        return isDefault;
    }

    @Override
    public boolean canBuild(String world, String name, EntryType type) {
        rwl.readLock().lock();
        boolean canBuild = false;
        //TODO: Read
        rwl.readLock().unlock();
        return canBuild;
    }

    @Override
    public String getPrefix(String world, String name, EntryType type) {
        rwl.readLock().lock();
        String prefix = "";
        //TODO: Read
        rwl.readLock().unlock();
        return prefix;
    }

    @Override
    public String getSuffix(String world, String name, EntryType type) {
        rwl.readLock().lock();
        String suffix = "";
        //TODO: Read
        rwl.readLock().unlock();
        return suffix;
    }

    @Override
    public Set<String> getPermissions(String world, String name, EntryType type) {
        rwl.readLock().lock();
        Set<String> permissions = new HashSet<String>();
        //TODO: Read
        rwl.readLock().unlock();
        return permissions;
    }

    @Override
    public Set<String> getParents(String world, String name, EntryType type) {
        rwl.readLock().lock();
        Set<String> parents = new HashSet<String>();
        //TODO: Read
        rwl.readLock().unlock();
        return parents;
    }

    @Override
    public void setBuild(String world, String name, EntryType type,
            boolean build) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();

    }

    @Override
    public void setPrefix(String world, String name, EntryType type,
            String prefix) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();

    }

    @Override
    public void setSuffix(String world, String name, EntryType type,
            String suffix) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();

    }

    @Override
    public void addPermission(String world, String name, EntryType type,
            String permission) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();

    }

    @Override
    public void removePermission(String world, String name, EntryType type,
            String negated) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();

    }
    @Override
    public void addParent(String world, String name, String groupWorld,
            String groupName) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void removeParent(String world, String name, String groupWorld,
            String groupName) {
        // TODO Auto-generated method stub
        
    }

    

    

}

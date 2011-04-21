package com.nijiko.data;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.EntryType;

public class YamlStorage implements IStorage {
    
    private final Configuration userConfig;
    private final Configuration groupConfig;
    private final String world;
    private final ReentrantReadWriteLock rwl;
    
    YamlStorage(Configuration userConfig, Configuration groupConfig, String world) {
        this.world = world;
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
    public boolean isDefault(String name) {
        rwl.readLock().lock();
        boolean isDefault = false;
        //TODO: Read
        rwl.readLock().unlock();
        return isDefault;
    }

    @Override
    public boolean canBuild(String name) {
        rwl.readLock().lock();
        boolean canBuild = false;
        //TODO: Read
        rwl.readLock().unlock();
        return canBuild;
    }

    @Override
    public String getPrefix(String name) {
        rwl.readLock().lock();
        String prefix = "";
        //TODO: Read
        rwl.readLock().unlock();
        return prefix;
    }

    @Override
    public String getSuffix(String name) {
        rwl.readLock().lock();
        String suffix = "";
        //TODO: Read
        rwl.readLock().unlock();
        return suffix;
    }

    @Override
    public Set<String> getPermissions(String name, EntryType type) {
        rwl.readLock().lock();
        Set<String> permissions = new HashSet<String>();
        //TODO: Read
        rwl.readLock().unlock();
        return permissions;
    }

    @Override
    public Set<GroupWorld> getParents(String name, EntryType type) {
        rwl.readLock().lock();
        Set<GroupWorld> parents = new HashSet<GroupWorld>();
        //TODO: Read
        rwl.readLock().unlock();
        return parents;
    }

    @Override
    public void setBuild(String name, boolean build) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();

    }

    @Override
    public void setPrefix(String name, String prefix) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();

    }

    @Override
    public void setSuffix(String name, String suffix) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();

    }

    @Override
    public void addPermission(String name, EntryType type,
            String permission) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();

    }

    @Override
    public void removePermission(String name, EntryType type,
            String negated) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();

    }
    @Override
    public void addParent(String name, String groupWorld,
            String groupName, EntryType type) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();
        
    }
    @Override
    public void removeParent(String name, String groupWorld,
            String groupName, EntryType type) {
        rwl.writeLock().lock();
        //TODO: Write
        rwl.writeLock().unlock();
        
    }

    

    

}

package com.nijiko.data;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.EntryType;
import com.nijikokun.bukkit.Permissions.Permissions;

public class YamlStorage implements IStorage {
    
    private final Configuration userConfig;
    private final Configuration groupConfig;
    private boolean userModified;
    private boolean groupModified;
    private final String world;
    private final ReentrantReadWriteLock rwl;
//    private final int reloadDelay;
    private boolean saveOff = false;

    
    YamlStorage(Configuration userConfig, Configuration groupConfig, String world, int reloadDelay, boolean autoSave) {
        this.saveOff = autoSave;
        this.world = world;
        this.userConfig = userConfig;
        this.groupConfig = groupConfig;
        this.rwl = new ReentrantReadWriteLock(false); //Give writer threads a higher priority than reader threads
//        this.reloadDelay = reloadDelay;
        reload();
        Permissions.instance.getServer().getScheduler().scheduleAsyncRepeatingTask(Permissions.instance, new Runnable()
        {
            @Override
            public void run() {
                reload();
            }
        }
        , reloadDelay, reloadDelay);
    }
    @Override
    public void save() 
    {
        rwl.readLock().lock();
        if(saveOff) return;
        rwl.readLock().unlock();
        forceSave();
    }
    @Override
    public void forceSave()
    {
        rwl.writeLock().lock();
        if(userModified)userConfig.save();
        if(groupModified)groupConfig.save();
        userConfig.load();
        groupConfig.load();
        userModified = false;
        groupModified = false;
        rwl.writeLock().unlock();
    }
    
    @Override
    public void reload()    
    {
        rwl.writeLock().lock();
        userConfig.load();
        groupConfig.load();
        userModified = false;
        groupModified = false;
        rwl.writeLock().unlock();
    }

    @Override
    public boolean isDefault(String name) {
        rwl.readLock().lock();
        boolean isDefault =  groupConfig.getBoolean("groups."+name+".default", false);
        rwl.readLock().unlock();
        return isDefault;
    }

    @Override
    public boolean canBuild(String name) {
        rwl.readLock().lock();
        boolean canBuild = groupConfig.getBoolean("groups."+name+".info.build", false);
        rwl.readLock().unlock();
        return canBuild;
    }

    @Override
    public String getPrefix(String name) {
        rwl.readLock().lock();
        String prefix = groupConfig.getString("groups."+name+".info.prefix", "");
        rwl.readLock().unlock();
        return prefix;
    }

    @Override
    public String getSuffix(String name) {
        rwl.readLock().lock();
        String suffix = groupConfig.getString("groups."+name+".info.prefix", "");
        rwl.readLock().unlock();
        return suffix;
    }

    @Override
    public Set<String> getPermissions(String name, EntryType type) {
        Configuration config = (type == EntryType.GROUP ? groupConfig : userConfig);
        rwl.readLock().lock();
        Set<String> permissions = new HashSet<String>(config.getStringList(type.toString().toLowerCase()+"s."+name+".permissions", null));
        rwl.readLock().unlock();
        return permissions;
    }

    @Override
    public Set<GroupWorld> getParents(String name, EntryType type) {
        Configuration config = (type == EntryType.GROUP ? groupConfig : userConfig);
        rwl.readLock().lock();
        Set<String> rawParents = new HashSet<String>(config.getStringList(type.toString().toLowerCase()+"s."+name+".inheritance", null));
        rwl.readLock().unlock();
        Set<GroupWorld> parents = new HashSet<GroupWorld>(rawParents.size());
        for(String raw : rawParents)
        {
            String[] split = raw.split(",",2);
            if(split.length == 0) continue;
            if(split.length == 1) parents.add(new GroupWorld(world,split[0]));
            else parents.add(new GroupWorld(split[0],split[1]));
        }
        return parents;
    }

    @Override
    public void setBuild(String name, boolean build) {
        rwl.writeLock().lock();
        groupConfig.setProperty("groups."+name+".info.build", build);
        groupModified = true;
        save();
        rwl.writeLock().unlock();
    }

    @Override
    public void setPrefix(String name, String prefix) {
        rwl.writeLock().lock();
        groupConfig.setProperty("groups."+name+".info.prefix", prefix);
        groupModified = true;
        save();
        rwl.writeLock().unlock();

    }

    @Override
    public void setSuffix(String name, String suffix) {
        rwl.writeLock().lock();
        groupConfig.setProperty("groups."+name+".info.suffix", suffix);
        groupModified = true;
        save();
        rwl.writeLock().unlock();

    }

    @Override
    public void addPermission(String name, EntryType type,
            String permission) {
        Configuration config = (type == EntryType.GROUP ? groupConfig : userConfig);
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(config.getStringList(type.toString().toLowerCase()+"s."+name+".permissions", null));
        permissions.add(permission);
        config.setProperty(type.toString().toLowerCase()+"s."+name+".permissions", new LinkedList<String>(permissions));
        if(type==EntryType.GROUP) groupModified = true;
        else userModified = true;
        save();
        
        rwl.writeLock().unlock();

    }

    @Override
    public void removePermission(String name, EntryType type,
            String permission) {
        Configuration config = (type == EntryType.GROUP ? groupConfig : userConfig);
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(config.getStringList(type.toString().toLowerCase()+"s."+name+".permissions", null));
        permissions.remove(permission);
        config.setProperty(type.toString().toLowerCase()+"s."+name+".permissions", new LinkedList<String>(permissions)); 
        if(type==EntryType.GROUP) groupModified = true;
        else userModified = true;
        save();
        rwl.writeLock().unlock();

    }
    @Override
    public void addParent(String name, String groupWorld,
            String groupName, EntryType type) {
        Configuration config = (type == EntryType.GROUP ? groupConfig : userConfig);
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(config.getStringList(type.toString().toLowerCase()+"s."+name+".inheritance", null));
        permissions.add(groupWorld+","+groupName);
        config.setProperty(type.toString().toLowerCase()+"s."+name+".permissions", new LinkedList<String>(permissions)); 
        if(type==EntryType.GROUP) groupModified = true;
        else userModified = true;
        save();
        rwl.writeLock().unlock();
        
    }
    @Override
    public void removeParent(String name, String groupWorld,
            String groupName, EntryType type) {
        Configuration config = (type == EntryType.GROUP ? groupConfig : userConfig);
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(config.getStringList(type.toString().toLowerCase()+"s."+name+".inheritance", null));
        permissions.add(groupWorld+","+groupName);
        config.setProperty(type.toString().toLowerCase()+"s."+name+".permissions", new LinkedList<String>(permissions));
        if(type==EntryType.GROUP) groupModified = true;
        else userModified = true;
        save();
        rwl.writeLock().unlock();
        
    }
    @Override
    public Set<String> getUsers() {
        rwl.readLock().lock();
        Set<String> users = new HashSet<String>(userConfig.getKeys("users"));
        rwl.readLock().unlock();
        return users;
    }
    @Override
    public Set<String> getGroups() {
        rwl.readLock().lock();
        Set<String> groups = new HashSet<String>(groupConfig.getKeys("groups"));
        rwl.readLock().unlock();
        return groups;
    }
    @Override
    public String getWorld() {
        return this.world;
    }
    @Override
    public boolean isAutoSave() {
        rwl.readLock().lock();
        boolean save = saveOff;
        rwl.readLock().unlock();
        return save;
    }
    @Override
    public void setAutoSave(boolean autoSave) {
        rwl.writeLock().lock();
        saveOff = autoSave;
        rwl.writeLock().unlock();
    }

    

    

}

package com.nijiko.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.util.config.Configuration;

import com.nijikokun.bukkit.Permissions.Permissions;

public class YamlGroupStorage implements GroupStorage {
    private final Configuration groupConfig;
    private final ReentrantReadWriteLock rwl;
    private boolean modified;
    private final String world;
    // private int taskId;
    private boolean saveOff;

    YamlGroupStorage(Configuration groupConfig, String world, int reloadDelay, boolean autoSave) {
        this.groupConfig = groupConfig;
        this.world = world;
        this.rwl = new ReentrantReadWriteLock(false);
        reload();
        // this.taskId =
        Permissions.instance.getServer().getScheduler().scheduleAsyncRepeatingTask(Permissions.instance, new Runnable() {
            @Override
            public void run() {
                reload();
            }
        }, reloadDelay, reloadDelay);
    }

    @Override
    public Set<String> getPermissions(String name) {
        rwl.readLock().lock();
        List<String> rawPerms = groupConfig.getStringList("groups." + name + ".permissions", null);
        rwl.readLock().unlock();
        Set<String> permissions = new HashSet<String>();
        if (rawPerms != null && !rawPerms.isEmpty())
            permissions.addAll(rawPerms);
        return permissions;
    }

    @Override
    public LinkedHashSet<GroupWorld> getParents(String name) {
        rwl.readLock().lock();
        List<String> rawParents = groupConfig.getStringList("groups." + name + ".inheritance", null);
        rwl.readLock().unlock();
        LinkedHashSet<GroupWorld> parents = new LinkedHashSet<GroupWorld>(rawParents.size());
        for (String raw : rawParents) {
            String[] split = raw.split(",", 2); // Split into at most 2 parts
            // ("world,blah" -> "world",
            // "blah")("blah" -> "blah")
            if (split.length == 0)
                continue;
            if (split.length == 1)
                parents.add(new GroupWorld(world, split[0]));
            else
                parents.add(new GroupWorld(split[0], split[1]));
        }
        return parents;
    }

    @Override
    public void addPermission(String name, String permission) {
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(groupConfig.getStringList("groups." + name + ".permissions", null));
        permissions.add(permission);
        groupConfig.setProperty("groups." + name + ".permissions", new LinkedList<String>(permissions));
        modified = true;
        save();
        rwl.writeLock().unlock();
    }

    @Override
    public void removePermission(String name, String permission) {
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(groupConfig.getStringList("groups." + name + ".permissions", null));
        permissions.add(permission);
        groupConfig.setProperty("groups." + name + ".permissions", new LinkedList<String>(permissions));
        modified = true;
        save();
        rwl.writeLock().unlock();
    }

    @Override
    public void addParent(String name, String groupWorld, String groupName) {
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(groupConfig.getStringList("groups." + name + ".inheritance", null));
        if (groupWorld == null || this.world.equalsIgnoreCase(groupWorld))
            permissions.add(groupName);
        else
            permissions.add(groupWorld + "," + groupName);
        groupConfig.setProperty("groups." + name + ".inheritance", new LinkedList<String>(permissions));
        modified = true;
        save();
        rwl.writeLock().unlock();

    }

    @Override
    public void removeParent(String name, String groupWorld, String groupName) {
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(groupConfig.getStringList("groups." + name + ".inheritance", null));
        if (groupWorld == null || this.world.equalsIgnoreCase(groupWorld))
            permissions.remove(groupName);
        else
            permissions.remove(groupWorld + "," + groupName);
        groupConfig.setProperty("groups." + name + ".inheritance", new LinkedList<String>(permissions));
        modified = true;
        save();
        rwl.writeLock().unlock();

    }

    @Override
    public Set<String> getGroups() {
        rwl.readLock().lock();
        List<String> rawGroups = groupConfig.getKeys("groups");
        rwl.readLock().unlock();
        Set<String> users = rawGroups == null ? new LinkedHashSet<String>() : new LinkedHashSet<String>(rawGroups);
        return users;
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public void forceSave() {
        boolean writeLocked = false;
        if (rwl.isWriteLockedByCurrentThread())
            writeLocked = true;
        else
            rwl.writeLock().lock();
        if (modified)
            groupConfig.save();
        groupConfig.load();
        modified = false;
        if (!writeLocked)
            rwl.writeLock().unlock();
    }

    @Override
    public void save() {
        boolean writeLocked = false;
        if (rwl.isWriteLockedByCurrentThread())
            writeLocked = true;
        else
            rwl.readLock().lock();
        if (saveOff)
            return;
        if (!writeLocked)
            rwl.readLock().unlock();
        forceSave();
    }

    @Override
    public void reload() {
        rwl.writeLock().lock();
        // System.out.println("Reloading group config for world \""+world+"\".");
        groupConfig.load();
        modified = false;
        rwl.writeLock().unlock();
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

    @Override
    public boolean isDefault(String name) {
        rwl.readLock().lock();
        boolean isDefault = groupConfig.getBoolean("groups." + name + ".default", false);
        rwl.readLock().unlock();
        return isDefault;
    }

    @Override
    public boolean canBuild(String name) {
        rwl.readLock().lock();
        boolean canBuild = groupConfig.getBoolean("groups." + name + ".info.build", false);
        rwl.readLock().unlock();
        return canBuild;
    }

    @Override
    public String getPrefix(String name) {
        rwl.readLock().lock();
        String prefix = groupConfig.getString("groups." + name + ".info.prefix", "");
        rwl.readLock().unlock();
        return prefix;
    }

    @Override
    public String getSuffix(String name) {
        rwl.readLock().lock();
        String suffix = groupConfig.getString("groups." + name + ".info.suffix", "");
        rwl.readLock().unlock();
        return suffix;
    }

    @Override
    public void setBuild(String name, boolean build) {
        rwl.writeLock().lock();
        groupConfig.setProperty("groups." + name + ".info.build", build);
        modified = true;
        save();
        rwl.writeLock().unlock();
    }

    @Override
    public void setPrefix(String name, String prefix) {
        rwl.writeLock().lock();
        groupConfig.setProperty("groups." + name + ".info.prefix", prefix);
        modified = true;
        save();
        rwl.writeLock().unlock();
    }

    @Override
    public void setSuffix(String name, String suffix) {
        rwl.writeLock().lock();
        groupConfig.setProperty("groups." + name + ".info.suffix", suffix);
        modified = true;
        save();
        rwl.writeLock().unlock();
    }

    @Override
    public boolean createGroup(String name) {
        rwl.writeLock().lock();
        if (groupConfig.getProperty("groups." + name) != null) {
            rwl.writeLock().unlock();
            return false;
        }
        Map<String, Object> template = new HashMap<String, Object>();
        template.put("inheritance", null);
        template.put("permissions", null);
        groupConfig.setProperty("groups." + name, template);
        groupConfig.save();
        rwl.writeLock().unlock();
        return true;
    }

    

    @Override
    public int getWeight(String name) {
        rwl.readLock().lock();
        int weight = groupConfig.getInt("groups."+name+".weight", -1);
        rwl.readLock().unlock();
        return weight;        
    }
    //TODO: Add setWeight()
    
    @Override
    public String getString(String name, String path) {
        rwl.readLock().lock();
        String data = groupConfig.getString("groups."+name+".info."+path, "");
        groupConfig.save();
        rwl.readLock().unlock();
        return data;
    }

    @Override
    public void setData(String name, String path, Object data) {
        rwl.writeLock().lock();
        groupConfig.setProperty("groups."+name+".info."+path, data);
        groupConfig.save();
        rwl.writeLock().unlock();
        return;        
    }

    @Override
    public Set<String> getTracks() {
        rwl.readLock().lock();
        List<String> rawTracks = groupConfig.getKeys("tracks");
        rwl.readLock().unlock();
        if(rawTracks==null)return null;
        return new HashSet<String>(rawTracks);
    }

    @Override
    public LinkedList<GroupWorld> getTrack(String trackName) {
        rwl.readLock().lock();
        List<String> rawGroups = groupConfig.getStringList("tracks."+trackName,null);
        rwl.readLock().unlock();

        LinkedHashSet<GroupWorld> track = new LinkedHashSet<GroupWorld>(rawGroups.size());
        for (String raw : rawGroups) {
            String[] split = raw.split(",", 2); // Split into at most 2 parts
            // ("world,blah" -> "world",
            // "blah")("blah" -> "blah")
            if (split.length == 0)
                continue;
            if (split.length == 1)
                track.add(new GroupWorld(world, split[0]));
            else
                track.add(new GroupWorld(split[0], split[1]));
        }
        return new LinkedList<GroupWorld>(track);
    }

    @Override
    public void removeData(String name, String path) {
        rwl.writeLock().lock();
        groupConfig.removeProperty("groups."+name+".info."+path);
        groupConfig.save();
        rwl.writeLock().unlock();
        return;        
    }
    


    @Override
    public int getInt(String name, String path) {
        rwl.readLock().lock();
        int data = groupConfig.getInt("groups."+name+".info."+path , -1);
        groupConfig.save();
        rwl.readLock().unlock();
        return data;
    }

    @Override
    public double getDouble(String name, String path) {
        rwl.readLock().lock();
        double data = groupConfig.getDouble("groups."+name+".info."+path , -1.0D);
        groupConfig.save();
        rwl.readLock().unlock();
        return data;
    }

    @Override
    public boolean getBool(String name, String path) {
        rwl.readLock().lock();
        boolean data = groupConfig.getBoolean("groups."+name+".info."+path , false);
        groupConfig.save();
        rwl.readLock().unlock();
        return data;
    }
}

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

import com.nijiko.permissions.EntryType;

public class YamlUserStorage implements UserStorage {
    private final Configuration userConfig;
    private final ReentrantReadWriteLock rwl;
    private boolean modified;
    private final String world;
    // private int taskId;
    private boolean saveOff;

    YamlUserStorage(Configuration userConfig, String world, int reloadDelay, boolean autoSave) {
        this.userConfig = userConfig;
        this.world = world;
        this.rwl = new ReentrantReadWriteLock(false);
        this.saveOff = !autoSave;
        reload();
    }

    @Override
    public Set<String> getPermissions(String name) {
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.readLock().lock();
        Set<String> permissions = new HashSet<String>(userConfig.getStringList("users." + name + ".permissions", null));
        rwl.readLock().unlock();
        return permissions;
    }

    @Override
    public LinkedHashSet<GroupWorld> getParents(String name) {
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.readLock().lock();
        List<String> rawParents = userConfig.getStringList("users." + name + ".groups", null);
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
//        System.out.println("Adding permission " + permission + " to " + name + " in world '" + world + "'.");
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.writeLock().lock();
        Set<String> permissions = new LinkedHashSet<String>(userConfig.getStringList("users." + name + ".permissions", null));
        permissions.add(permission);
        userConfig.setProperty("users." + name + ".permissions", new LinkedList<String>(permissions));
//        System.out.println(userConfig.getStringList("users." + name + ".permissions", null));
        modified = true;
        save();
//        System.out.println(userConfig.getStringList("users." + name + ".permissions", null));
        rwl.writeLock().unlock();
    }

    @Override
    public void removePermission(String name, String permission) {
//        System.out.println("Removing permission " + permission + " from " + name + " in world '" + world + "'.");
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.writeLock().lock();
        Set<String> permissions = new LinkedHashSet<String>(userConfig.getStringList("users." + name + ".permissions", null));
        permissions.remove(permission);
        userConfig.setProperty("users." + name + ".permissions", new LinkedList<String>(permissions));
//        System.out.println(userConfig.getStringList("users." + name + ".permissions", null));
        modified = true;
        save();
//        System.out.println(userConfig.getStringList("users." + name + ".permissions", null));
        rwl.writeLock().unlock();
    }

    @Override
    public void addParent(String name, String groupWorld, String groupName) {
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(userConfig.getStringList("users." + name + ".groups", null));
        if (groupWorld == null || this.world.equalsIgnoreCase(groupWorld))
            permissions.add(groupName);
        else
            permissions.add(groupWorld + "," + groupName);
        userConfig.setProperty("users." + name + ".groups", new LinkedList<String>(permissions));
        modified = true;
        save();
        rwl.writeLock().unlock();

    }

    @Override
    public void removeParent(String name, String groupWorld, String groupName) {
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(userConfig.getStringList("users." + name + ".groups", null));
        if (groupWorld == null || this.world.equalsIgnoreCase(groupWorld))
            permissions.remove(groupName);
        else
            permissions.remove(groupWorld + "," + groupName);
        userConfig.setProperty("users." + name + ".groups", new LinkedList<String>(permissions));
        modified = true;
        save();
        rwl.writeLock().unlock();

    }

    @Override
    public Set<String> getEntries() {
        rwl.readLock().lock();
        List<String> rawUsers = userConfig.getKeys("users");
        rwl.readLock().unlock();
        Set<String> users = new HashSet<String>();
        if (rawUsers != null)
            for (String username : rawUsers) {
                if (username == null)
                    continue;
                users.add(username.replace(',', '.'));
            }
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
        if (modified) {
//            System.out.println("Saving world '" + world + "'.");
            userConfig.save();
        }
        userConfig.load();
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
        // System.out.println("Reloading user config for world \""+world+"\".");
        userConfig.load();
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
    public boolean create(String name) {
        rwl.writeLock().lock();
        if (userConfig.getProperty("users." + name) != null) {
            rwl.writeLock().unlock();
            return false;
        }
        Map<String, Object> template = new HashMap<String, Object>();
        template.put("groups", null);
        template.put("permissions", null);
        userConfig.setProperty("users." + name, template);
        modified = true;
        save();
        rwl.writeLock().unlock();
        return true;
    }

    @Override
    public boolean delete(String name) {
        rwl.writeLock().lock();
        boolean exists = userConfig.getProperty("users." + name) != null;
        userConfig.removeProperty("users." + name);
        modified = true;
        save();
        rwl.writeLock().unlock();
        return exists;
    }
    
    @Override
    public void removeData(String name, String path) {
        rwl.writeLock().lock();
        userConfig.removeProperty("users." + name + ".info." + path);
        modified = true;
        save();
        rwl.writeLock().unlock();
        return;
    }

    @Override
    public void setData(String name, String path, Object data) {
        rwl.writeLock().lock();
        userConfig.setProperty("users." + name + ".info." + path, data);
        modified = true;
        save();
        rwl.writeLock().unlock();
        return;
    }


    @Override
    public String getString(String name, String path) {
        Object raw = getObj(name, path);
        if (raw instanceof String)
            return (String) raw;
        if(raw == null)
            return null;
        return raw.toString();
    }

    @Override
    public Integer getInt(String name, String path) {
        Object raw = getObj(name, path);
        if (raw instanceof Integer)
            return (Integer) raw;
        if(raw == null)
            return null;
        int val;
        try {
            val = Integer.valueOf(raw.toString());
        } catch (NumberFormatException e) {
            return null;
        }
        return val;
    }

    @Override
    public Double getDouble(String name, String path) {
        Object raw = getObj(name, path);
        if (raw instanceof Double)
            return (Double) raw;
        if(raw == null)
            return null;
        double val;
        try {
            val = Double.valueOf(raw.toString());
        } catch (NumberFormatException e) {
            return null;
        }
        return val;
    }

    @Override
    public Boolean getBool(String name, String path) {
        Object raw = getObj(name, path);
        if (raw instanceof Boolean)
            return (Boolean) raw;
        if(raw == null)
            return null;
        boolean val = Boolean.valueOf(raw.toString());
        return val;
    }

    private Object getObj(String name, String path) {
        rwl.readLock().lock();
        Object data = userConfig.getProperty("users." + name + ".info." + path);
        rwl.readLock().unlock();
        return data;
    }

    @Override
    public EntryType getType() {
        return EntryType.USER;
    }
}

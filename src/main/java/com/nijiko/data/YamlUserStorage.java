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

public class YamlUserStorage implements UserStorage {
    private final Configuration userConfig;
    private final ReentrantReadWriteLock rwl;
    private boolean modified;
    private final String world;
    // private int taskId;
    private boolean saveOff;

    YamlUserStorage(Configuration userConfig, String world, int reloadDelay,
            boolean autoSave) {
        this.userConfig = userConfig;
        this.world = world;
        this.rwl = new ReentrantReadWriteLock(false);
        reload();
        // this.taskId =
        Permissions.instance
                .getServer()
                .getScheduler()
                .scheduleAsyncRepeatingTask(Permissions.instance,
                        new Runnable() {
                            @Override
                            public void run() {
                                reload();
                            }
                        }, reloadDelay, reloadDelay);
    }

    @Override
    public Set<String> getPermissions(String name) {
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.readLock().lock();
        Set<String> permissions = new HashSet<String>(userConfig.getStringList(
                "users." + name + ".permissions", null));
        rwl.readLock().unlock();
        return permissions;
    }

    @Override
    public LinkedHashSet<GroupWorld> getParents(String name) {
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.readLock().lock();
        List<String> rawParents = userConfig.getStringList(
                "users." + name + ".groups", null);
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
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(userConfig.getStringList(
                "users." + name + ".permissions", null));
        permissions.add(permission);
        userConfig.setProperty("users." + name + ".permissions",
                new LinkedList<String>(permissions));
        modified = true;
        save();
        rwl.writeLock().unlock();
    }

    @Override
    public void removePermission(String name, String permission) {
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(userConfig.getStringList(
                "users." + name + ".permissions", null));
        permissions.add(permission);
        userConfig.setProperty("users." + name + ".permissions",
                new LinkedList<String>(permissions));
        modified = true;
        save();
        rwl.writeLock().unlock();
    }

    @Override
    public void addParent(String name, String groupWorld, String groupName) {
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(userConfig.getStringList(
                "users." + name + ".groups", null));
        if (groupWorld == null || this.world.equalsIgnoreCase(groupWorld))
            permissions.add(groupName);
        else
            permissions.add(groupWorld + "," + groupName);
        userConfig.setProperty("users." + name + ".groups",
                new LinkedList<String>(permissions));
        modified = true;
        save();
        rwl.writeLock().unlock();

    }

    @Override
    public void removeParent(String name, String groupWorld, String groupName) {
        name = name.replace('.', ','); // Fix for legacy usernames with periods
        rwl.writeLock().lock();
        Set<String> permissions = new HashSet<String>(userConfig.getStringList(
                "users." + name + ".groups", null));
        if (groupWorld == null || this.world.equalsIgnoreCase(groupWorld))
            permissions.remove(groupName);
        else
            permissions.remove(groupWorld + "," + groupName);
        userConfig.setProperty("users." + name + ".groups",
                new LinkedList<String>(permissions));
        modified = true;
        save();
        rwl.writeLock().unlock();

    }

    @Override
    public Set<String> getUsers() {
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
        if (modified)
            userConfig.save();
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
//        System.out.println("Reloading user config for world \""+world+"\".");
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
    public boolean createUser(String name) {
        rwl.writeLock().lock();
        if(userConfig.getProperty("users."+name)!=null)
        {
            rwl.writeLock().unlock();
            return false;
        }
        Map<String, Object> template = new HashMap<String,Object>();
        template.put("groups", null);
        template.put("permissions", null);
        userConfig.setProperty("users."+name, template);
        userConfig.save();
        rwl.writeLock().unlock();
        return true;
    }

    @Override
    public String getString(String name, String path) {
        rwl.readLock().lock();
        String data = userConfig.getString("users."+name+".info."+path);
        rwl.readLock().unlock();
        return data;
    }

    @Override
    public void removeData(String name, String path) {
        rwl.writeLock().lock();
        userConfig.removeProperty("users."+name+".info."+path);
        rwl.writeLock().unlock();
        return;        
    }
    
    @Override
    public void setData(String name, String path, Object data) {
        rwl.writeLock().lock();
        userConfig.setProperty("users."+name+".info."+path, data);
        rwl.writeLock().unlock();
        return;        
    }

    @Override
    public int getInt(String name, String path) {
        rwl.readLock().lock();
        int data = userConfig.getInt("users."+name+".info."+path , 0);
        rwl.readLock().unlock();
        return data;
    }

    @Override
    public double getDouble(String name, String path) {
        rwl.readLock().lock();
        double data = userConfig.getDouble("users."+name+".info."+path , 0D);
        rwl.readLock().unlock();
        return data;
    }

    @Override
    public boolean getBool(String name, String path) {
        rwl.readLock().lock();
        boolean data = userConfig.getBoolean("users."+name+".info."+path , false);
        rwl.readLock().unlock();
        return data;
    }
    
}

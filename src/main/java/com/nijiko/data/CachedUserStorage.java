package com.nijiko.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class CachedUserStorage implements UserStorage {

    private Map<String, Set<String>> userPermissions;
    private Map<String, LinkedHashSet<GroupWorld>> userParents;
    private Map<String, Map<String, Object>> userData;
    private final UserStorage wrapped;

    public CachedUserStorage(UserStorage wrapped) {
        if (wrapped instanceof CachedUserStorage)
            throw new RuntimeException("No Cacheception, please. (CachedUserStorage wrapping a CachedUserStorage.)");
        this.wrapped = wrapped;
    }

    @Override
    public Set<String> getPermissions(String name) {
        Set<String> perms = userPermissions.get(name);
        if (perms == null) {
            perms = wrapped.getPermissions(name);
            userPermissions.put(name, perms);
        }
        return perms;
    }

    @Override
    public LinkedHashSet<GroupWorld> getParents(String name) {
        LinkedHashSet<GroupWorld> parents = userParents.get(name);
        if (parents == null) {
            parents = wrapped.getParents(name);
            userParents.put(name, parents);
        }
        return parents;
    }

    @Override
    public void addPermission(String name, String permission) {
        if (userPermissions.get(name) == null) {
            userPermissions.put(name, new HashSet<String>());
        }
        userPermissions.get(name).add(permission);
        wrapped.addPermission(name, permission);
    }

    @Override
    public void removePermission(String name, String permission) {
        if (userPermissions.get(name) == null) {
            userPermissions.put(name, new HashSet<String>());
        }
        userPermissions.get(name).add(permission);
        wrapped.addPermission(name, permission);
    }

    @Override
    public void addParent(String name, String groupWorld, String groupName) {
        GroupWorld gw = new GroupWorld(groupWorld, groupName);
        if (userParents.get(name) == null) {
            userParents.put(name, new LinkedHashSet<GroupWorld>());
        }
        userParents.get(name).add(gw);
        wrapped.addParent(name, groupWorld, groupName);
    }

    @Override
    public void removeParent(String name, String groupWorld, String groupName) {
        GroupWorld gw = new GroupWorld(groupWorld, groupName);
        if (userParents.get(name) == null) {
            userParents.put(name, new LinkedHashSet<GroupWorld>());
        }
        userParents.get(name).remove(gw);
        wrapped.removeParent(name, groupWorld, groupName);
    }

    @Override
    public Set<String> getUsers() {
        return wrapped.getUsers();
    }

    @Override
    public String getWorld() {
        return wrapped.getWorld();
    }

    @Override
    public void forceSave() {
        wrapped.forceSave();
    }

    @Override
    public void save() {
        wrapped.save();
    }

    @Override
    public void reload() {
        userPermissions.clear();
        userParents.clear();
        userData.clear();
        wrapped.reload();
    }

    @Override
    public boolean isAutoSave() {
        return wrapped.isAutoSave();
    }

    @Override
    public void setAutoSave(boolean autoSave) {
        wrapped.setAutoSave(autoSave);
    }

    @Override
    public boolean createUser(String name) {
        return wrapped.createUser(name);
    }

    @Override
    public String getString(String name, String path) {
        if (userData.get(name) == null) {
            userData.put(name, new HashMap<String, Object>());
        } else {
            Object data = userData.get(name).get(path);
            if (userData.get(name).containsKey(path) && data == null)
                return null;
            if (data != null)
                return data.toString();
        }
        String data = wrapped.getString(name, path);
        userData.get(name).put(path, data);
        return data;
    }

    @Override
    public Integer getInt(String name, String path) {
        if (userData.get(name) == null) {
            userData.put(name, new HashMap<String, Object>());
        } else {
            Object data = userData.get(name).get(path);
            if (userData.get(name).containsKey(path) && data == null)
                return null;
            if (data instanceof Integer)
                return (Integer) data;
        }
        Integer data = wrapped.getInt(name, path);
        userData.get(name).put(path, data);
        return data;
    }

    @Override
    public Double getDouble(String name, String path) {
        if (userData.get(name) == null) {
            userData.put(name, new HashMap<String, Object>());
        } else {
            Object data = userData.get(name).get(path);
            if (userData.get(name).containsKey(path) && data == null)
                return null;
            if (data instanceof Double)
                return (Double) data;
        }
        Double data = wrapped.getDouble(name, path);
        userData.get(name).put(path, data);
        return data;
    }

    @Override
    public Boolean getBool(String name, String path) {
        if (userData.get(name) == null) {
            userData.put(name, new HashMap<String, Object>());
        } else {
            Object data = userData.get(name).get(path);
            if (userData.get(name).containsKey(path) && data == null)
                return null;
            if (data instanceof Boolean)
                return (Boolean) data;
        }
        Boolean data = wrapped.getBool(name, path);
        userData.get(name).put(path, data);
        return data;
    }

    @Override
    public void setData(String name, String path, Object data) {
        if (!(data instanceof Integer) || !(data instanceof Boolean) || !(data instanceof Double) || !(data instanceof String)) {
            throw new IllegalArgumentException("Only ints, bools, doubles and Strings are allowed!");
        }
        if (userData.get(name) == null) {
            userData.put(name, new HashMap<String, Object>());
        }
        userData.get(name).put(path, data);
        wrapped.setData(name, path, data);
    }

    @Override
    public void removeData(String name, String path) {
        if (userData.get(name) != null) {
            userData.get(name).remove(path);
            wrapped.removeData(name, path);
        }
    }

}

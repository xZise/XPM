package com.nijiko.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class CachedGroupStorage implements GroupStorage {

    private final GroupStorage wrapped;

    private final Map<String, String> groupPrefixes = new HashMap<String, String>();
    private final Map<String, String> groupSuffixes = new HashMap<String, String>();
    private final Map<String, Set<String>> groupPermissions = new HashMap<String, Set<String>>();
    private final Map<String, LinkedHashSet<GroupWorld>> groupParents = new HashMap<String, LinkedHashSet<GroupWorld>>();
    private final Map<String, Integer> groupWeights = new HashMap<String, Integer>();

    private final Map<String, Map<String, Object>> groupData = new HashMap<String, Map<String, Object>>();

    private String defaultGroup;

    public CachedGroupStorage(GroupStorage wrapped) {
        if (wrapped instanceof CachedGroupStorage)
            throw new RuntimeException("No Cacheception, please. (CachedGroupStorage wrapping a CachedGroupStorage.)");
        this.wrapped = wrapped;
    }

    @Override
    public boolean isDefault(String name) {
        if (defaultGroup != null)
            return defaultGroup.equals(name);
        if (wrapped.isDefault(name)) {
            defaultGroup = name;
            return true;
        }
        return false;
    }

    @Override
    public boolean canBuild(String name) {
        return wrapped.canBuild(name);
    }

    @Override
    public String getPrefix(String name) {
        String prefix = groupPrefixes.get(name);
        if (prefix == null) {
            prefix = wrapped.getPrefix(name);
            groupPrefixes.put(name, prefix);
        }
        return prefix;
    }

    @Override
    public String getSuffix(String name) {
        String suffix = groupSuffixes.get(name);
        if (suffix == null) {
            suffix = wrapped.getSuffix(name);
            groupPrefixes.put(name, suffix);
        }
        return suffix;
    }

    @Override
    public Set<String> getPermissions(String name) {
        Set<String> perms = groupPermissions.get(name);
        if (perms == null) {
            perms = wrapped.getPermissions(name);
            groupPermissions.put(name, perms);
        }
        return perms;
    }

    @Override
    public LinkedHashSet<GroupWorld> getParents(String name) {
        LinkedHashSet<GroupWorld> parents = groupParents.get(name);
        if (parents == null) {
            parents = wrapped.getParents(name);
            groupParents.put(name, parents);
        }
        return parents;
    }

    @Override
    public void setBuild(String name, boolean build) {
        wrapped.setBuild(name, build);
    }

    @Override
    public void setPrefix(String name, String prefix) {
        groupPrefixes.put(name, prefix);
        wrapped.setPrefix(name, prefix);
    }

    @Override
    public void setSuffix(String name, String suffix) {
        groupSuffixes.put(name, suffix);
        wrapped.setSuffix(name, suffix);
    }

    @Override
    public void addPermission(String name, String permission) {
        if (groupPermissions.get(name) == null) {
            groupPermissions.put(name, new HashSet<String>());
        }
        groupPermissions.get(name).add(permission);
        wrapped.addPermission(name, permission);
    }

    @Override
    public void removePermission(String name, String permission) {
        if (groupPermissions.get(name) == null) {
            groupPermissions.put(name, new HashSet<String>());
        }
        groupPermissions.get(name).remove(permission);
        wrapped.removePermission(name, permission);
    }

    @Override
    public void addParent(String name, String groupWorld, String groupName) {
        GroupWorld gw = new GroupWorld(groupWorld, groupName);
        if (groupParents.get(name) == null) {
            groupParents.put(name, new LinkedHashSet<GroupWorld>());
        }
        groupParents.get(name).add(gw);
        wrapped.addParent(name, groupWorld, groupName);
    }

    @Override
    public void removeParent(String name, String groupWorld, String groupName) {
        GroupWorld gw = new GroupWorld(groupWorld, groupName);
        if (groupParents.get(name) == null) {
            groupParents.put(name, new LinkedHashSet<GroupWorld>());
        }
        groupParents.get(name).remove(gw);
        wrapped.removeParent(name, groupWorld, groupName);
    }

    @Override
    public Set<String> getGroups() {
        return wrapped.getGroups();
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
        wrapped.reload();
        groupPrefixes.clear();
        groupSuffixes.clear();
        groupPermissions.clear();
        groupParents.clear();
        groupWeights.clear();
        groupData.clear();
        defaultGroup = null;
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
    public boolean createGroup(String name) {
        return wrapped.createGroup(name);
    }

    @Override
    public Set<String> getTracks() {
        return wrapped.getTracks();
    }

    @Override
    public LinkedList<GroupWorld> getTrack(String track) {
        return wrapped.getTrack(track);
    }

    @Override
    public int getWeight(String name) {
        return wrapped.getWeight(name);
    }

    @Override
    public String getString(String name, String path) {
        if (groupData.get(name) == null) {
            groupData.put(name, new HashMap<String, Object>());
        } else {
            Object data = groupData.get(name).get(path);
            if (groupData.get(name).containsKey(path) && data == null)
                return null;
            if (data != null)
                return data.toString();
        }
        String data = wrapped.getString(name, path);
        groupData.get(name).put(path, data);
        return data;
    }

    @Override
    public Integer getInt(String name, String path) {
        if (groupData.get(name) == null) {
            groupData.put(name, new HashMap<String, Object>());
        } else {
            Object data = groupData.get(name).get(path);
            if (groupData.get(name).containsKey(path) && data == null)
                return null;
            if (data instanceof Integer)
                return (Integer) data;
        }
        Integer data = wrapped.getInt(name, path);
        groupData.get(name).put(path, data);
        return data;
    }

    @Override
    public Double getDouble(String name, String path) {
        if (groupData.get(name) == null) {
            groupData.put(name, new HashMap<String, Object>());
        } else {
            Object data = groupData.get(name).get(path);
            if (groupData.get(name).containsKey(path) && data == null)
                return null;
            if (data instanceof Double)
                return (Double) data;
        }
        Double data = wrapped.getDouble(name, path);
        groupData.get(name).put(path, data);
        return data;
    }

    @Override
    public Boolean getBool(String name, String path) {
        if (groupData.get(name) == null) {
            groupData.put(name, new HashMap<String, Object>());
        } else {
            Object data = groupData.get(name).get(path);
            if (groupData.get(name).containsKey(path) && data == null)
                return null;
            if (data instanceof Boolean)
                return (Boolean) data;
        }
        Boolean data = wrapped.getBool(name, path);
        groupData.get(name).put(path, data);
        return data;
    }

    @Override
    public void setData(String name, String path, Object data) {
        if (!(data instanceof Integer) || !(data instanceof Boolean) || !(data instanceof Double) || !(data instanceof String)) {
            throw new IllegalArgumentException("Only ints, bools, doubles and Strings are allowed!");
        }
        if (groupData.get(name) == null) {
            groupData.put(name, new HashMap<String, Object>());
        }
        groupData.get(name).put(path, data);
        wrapped.setData(name, path, data);
    }

    @Override
    public void removeData(String name, String path) {
        if (groupData.get(name) != null) {
            groupData.get(name).remove(path);
            wrapped.removeData(name, path);
        }
    }
}

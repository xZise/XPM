package com.nijiko.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.nijiko.permissions.EntryType;


public class SqlUserStorage implements UserStorage {

    private String userWorld;
    private Map<String, Set<String>> userPermissions = new HashMap<String, Set<String>>();
    private Map<String, Set<GroupWorld>> userParents = new HashMap<String, Set<GroupWorld>>();
        
    public SqlUserStorage(String userWorld) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Set<String> getPermissions(String name) {
        if(name == null) return new HashSet<String>();
        Set<String> permissions = userPermissions.get(name.toLowerCase());
        if(permissions != null) return permissions;
        //TODO SQL Query
        return null;
    }

    @Override
    public Set<GroupWorld> getParents(String name) {
        if(name == null) return new HashSet<GroupWorld>();
        Set<GroupWorld> parents = userParents.get(name.toLowerCase());
        if(parents != null) return parents;
        //TODO SQL Query
        return null;
    }

    @Override
    public void addPermission(String name, String permission) {
        if(userPermissions.get(name.toLowerCase()) == null) userPermissions.put(name.toLowerCase(), new HashSet<String>());
        Set<String> perms = userPermissions.get(name.toLowerCase());
        if(perms.contains(permission)) return;
        perms.add(permission);
        //TODO SQL Update
    }

    @Override
    public void removePermission(String name, String permission) {
        if(userPermissions.get(name.toLowerCase()) == null) userPermissions.put(name.toLowerCase(), new HashSet<String>());
        Set<String> perms = userPermissions.get(name.toLowerCase());
        if(!perms.contains(permission)) return;
        perms.remove(permission);
        //TODO SQL Update
    }

    @Override
    public void addParent(String name, String groupWorld, String groupName) {
        if(userParents.get(name.toLowerCase()) == null) userParents.put(name.toLowerCase(), new HashSet<GroupWorld>());
        Set<GroupWorld> parents = userParents.get(name.toLowerCase());
        if(parents.contains(groupWorld)) return;
        parents.add(new GroupWorld(groupWorld, groupName));
        //TODO SQL Updates

    }

    @Override
    public void removeParent(String name, String groupWorld, String groupName) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<String> getUsers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getWorld() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void forceSave() {
        // TODO Auto-generated method stub

    }

    @Override
    public void save() {
        // TODO Auto-generated method stub

    }

    @Override
    public void reload() {
        userPermissions.clear();
        userParents.clear();

    }

    @Override
    public boolean isAutoSave() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setAutoSave(boolean autoSave) {
        // TODO Auto-generated method stub

    }

}

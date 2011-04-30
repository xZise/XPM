package com.nijiko.data;

import java.util.LinkedHashSet;
import java.util.Set;

public interface UserStorage {

    public Set<String> getPermissions(String name);

    public LinkedHashSet<GroupWorld> getParents(String name);

    public void addPermission(String name, String permission);

    public void removePermission(String name, String permission);

    public void addParent(String name, String groupWorld, String groupName);

    public void removeParent(String name, String groupWorld, String groupName);

    public Set<String> getUsers();

    public String getWorld();

    public void forceSave();

    public void save();

    public void reload();

    public boolean isAutoSave();

    public void setAutoSave(boolean autoSave);
    
    public boolean createUser(String name);
}

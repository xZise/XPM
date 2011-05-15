package com.nijiko.data;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public interface GroupStorage {
    public boolean isDefault(String name);

    public boolean canBuild(String name);

    public String getPrefix(String name);

    public String getSuffix(String name);

    public Set<String> getPermissions(String name);

    public LinkedHashSet<GroupWorld> getParents(String name);

    public void setBuild(String name, boolean build);

    public void setPrefix(String name, String prefix);

    public void setSuffix(String name, String suffix);

    public void addPermission(String name, String permission);

    public void removePermission(String name, String permission);

    public void addParent(String name, String groupWorld, String groupName);

    public void removeParent(String name, String groupWorld, String groupName);

    public Set<String> getGroups();

    public String getWorld();

    public void forceSave();

    public void save();

    public void reload();

    public boolean isAutoSave();

    public void setAutoSave(boolean autoSave);

    public boolean createGroup(String name);

    public Set<String> getTracks();
    public LinkedList<GroupWorld> getTrack(String track);
    
    public int getWeight(String name);
    public String getString(String name, String path, String def);
    
    public int getInt(String name, String path, int def);
    
    public double getDouble(String name, String path, double def);
    
    public boolean getBool(String name, String path, boolean def);
    
    public void setData(String name, String path, Object data);
    public void removeData(String name, String path);
}

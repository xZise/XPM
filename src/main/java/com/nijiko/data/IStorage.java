package com.nijiko.data;

import java.util.Set;

import com.nijiko.permissions.EntryType;

public interface IStorage {

    public boolean isDefault(String name);
    public boolean canBuild(String name);
    public String getPrefix(String name);
    public String getSuffix(String name);
    public Set<String> getPermissions(String name, EntryType type);
    public Set<GroupWorld> getParents(String name, EntryType type);

    public void setBuild(String name, boolean build);
    public void setPrefix(String name, String prefix);
    public void setSuffix(String name, String suffix);

    public void addPermission(String name, EntryType type, String permission);
    public void removePermission(String name, EntryType type, String permissions);

    public void save();
    public void reload(final boolean applyChanges);
    public void addParent(String name, String groupWorld, String groupName, EntryType type);
    public void removeParent(String name, String groupWorld, String groupName, EntryType type);
}
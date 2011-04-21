package com.nijiko.data;

import java.util.Set;

import com.nijiko.permissions.EntryType;

public interface IStorage {

    public boolean isDefault(String world, String name);
    public boolean canBuild(String world, String name, EntryType type);
    public String getPrefix(String world, String name, EntryType type);
    public String getSuffix(String world, String name, EntryType type);
    public Set<String> getPermissions(String world, String name, EntryType type);
    public Set<String> getParents(String world, String name, EntryType type);

    public void setBuild(String world, String name, EntryType type, boolean build);
    public void setPrefix(String world, String name, EntryType type, String prefix);
    public void setSuffix(String world, String name, EntryType type, String suffix);

    public void addPermission(String world, String name, EntryType type, String permission);
    public void removePermission(String world, String name, EntryType type, String negated);

    public void save();
    public void reload(final boolean applyChanges);
    public void addParent(String world, String name, String groupWorld, String groupName);
    public void removeParent(String world, String name, String groupWorld, String groupName);
}
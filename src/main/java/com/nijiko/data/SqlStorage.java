package com.nijiko.data;

import java.util.Set;

import com.nijiko.permissions.EntryType;

public class SqlStorage implements IStorage {

    SqlStorage(String driver, String uri, String username, String password)
    {
        //TODO: Constructor
    }
    @Override
    public boolean isDefault(String world, String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canBuild(String world, String name, EntryType type) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getPrefix(String world, String name, EntryType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSuffix(String world, String name, EntryType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getPermissions(String world, String name, EntryType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getParents(String world, String name, EntryType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setBuild(String world, String name, EntryType type,
            boolean build) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPrefix(String world, String name, EntryType type,
            String prefix) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSuffix(String world, String name, EntryType type,
            String suffix) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPermission(String world, String name, EntryType type,
            String permission) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePermission(String world, String name, EntryType type,
            String negated) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub

    }

}

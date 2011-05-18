package com.nijiko.data;

import java.util.LinkedHashSet;
import java.util.Set;

public class HarcUserStorage implements UserStorage {

    @Override
    public Set<String> getPermissions(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LinkedHashSet<GroupWorld> getParents(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addPermission(String name, String permission) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removePermission(String name, String permission) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addParent(String name, String groupWorld, String groupName) {
        // TODO Auto-generated method stub
        
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
        // TODO Auto-generated method stub
        
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

    @Override
    public boolean createUser(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setData(String name, String path, Object data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeData(String name, String path) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getString(String name, String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getInt(String name, String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getDouble(String name, String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean getBool(String name, String path) {
        // TODO Auto-generated method stub
        return null;
    }

}

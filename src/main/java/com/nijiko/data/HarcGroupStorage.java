package com.nijiko.data;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class HarcGroupStorage implements GroupStorage {

    @Override
    public boolean isDefault(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canBuild(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getPrefix(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSuffix(String name) {
        // TODO Auto-generated method stub
        return null;
    }

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
    public void setBuild(String name, boolean build) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPrefix(String name, String prefix) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSuffix(String name, String suffix) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPermission(String name, String permission) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePermission(String name, String permissions) {
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
    public Set<String> getGroups() {
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
    public boolean createGroup(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getWeight(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Set<String> getTracks() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LinkedList<GroupWorld> getTrack(String track) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getString(String name, String path, String def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getInt(String name, String path, int def) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getDouble(String name, String path, double def) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getBool(String name, String path, boolean def) {
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

}

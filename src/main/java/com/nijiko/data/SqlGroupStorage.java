package com.nijiko.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SqlGroupStorage implements GroupStorage {

    private String groupWorld;
    private String baseGroup = null;
    private Set<String> buildGroups = new HashSet<String>();
    private Map<String, String> groupPrefixes = new HashMap<String, String>();
    private Map<String, String> groupSuffixes = new HashMap<String, String>();
    private Map<String, Set<String>> groupPermissions = new HashMap<String, Set<String>>();
    private Map<String, Set<GroupWorld>> groupParents = new HashMap<String, Set<GroupWorld>>();

    public SqlGroupStorage(String groupWorld) {
        // TODO Auto-generated constructor stub
    }

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
    public Set<GroupWorld> getParents(String name) {
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
        baseGroup = null;
        buildGroups.clear();
        groupPrefixes.clear();
        groupSuffixes.clear();
        groupPermissions.clear();
        groupParents.clear();
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

package com.nijiko.permissions;

import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class ModularControl extends PermissionHandler {

    @Override
    public void setDefaultWorld(String world) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean loadWorld(String world) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void forceLoadWorld(String world) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean checkWorld(String world) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void load() {
        // TODO Auto-generated method stub

    }

    @Override
    public void load(String world, Configuration config) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean reload(String world) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCache(String world, Map<String, Boolean> Cache) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCacheItem(String world, String player, String permission,
            boolean data) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Boolean> getCache(String world) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getCacheItem(String world, String player, String permission) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeCachedItem(String world, String player, String permission) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearCache(String world) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearAllCache() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean has(Player player, String permission) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean permission(Player player, String permission) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getGroup(String world, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getGroups(String world, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean inGroup(String world, String name, String group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean inSingleGroup(String world, String name, String group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getGroupPrefix(String world, String group) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGroupSuffix(String world, String group) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canGroupBuild(String world, String group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getGroupPermissionString(String world, String group,
            String permission) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getGroupPermissionInteger(String world, String group,
            String permission) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getGroupPermissionBoolean(String world, String group,
            String permission) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getGroupPermissionDouble(String world, String group,
            String permission) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getUserPermissionString(String world, String name,
            String permission) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getUserPermissionInteger(String world, String name,
            String permission) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getUserPermissionBoolean(String world, String name,
            String permission) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getUserPermissionDouble(String world, String name,
            String permission) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getPermissionString(String world, String name,
            String permission) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPermissionInteger(String world, String name, String permission) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getPermissionBoolean(String world, String name,
            String permission) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getPermissionDouble(String world, String name,
            String permission) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void addGroupInfo(String world, String group, String node,
            Object data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeGroupInfo(String world, String group, String node) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addUserPermission(String world, String user, String node) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeUserPermission(String world, String user, String node) {
        // TODO Auto-generated method stub

    }

    @Override
    public void save(String world) {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }
    
    Set<Group> getGroups(String world, Set<String> groupNames)
    {
        return null;
    }

}

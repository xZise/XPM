package com.nijiko.permissions;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

/**
 * Permissions 3.0
 * Copyright (C) 2011  Matt 'The Yeti' Burnett <admin@theyeticave.net>
 * Original Credit & Copyright (C) 2010 Nijikokun <nijikokun@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Permissions Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Permissions Public License for more details.
 *
 * You should have received a copy of the GNU Permissions Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public abstract class PermissionHandler { //Name will be changed
    //World config manipulation methods
    public abstract void setDefaultWorld(String world);
    public abstract boolean checkWorld(String world);
    public abstract boolean loadWorld(String world) throws Exception;
    public abstract void forceLoadWorld(String world) throws Exception;
    public abstract Set<String> getWorlds();
    public abstract void load() throws Exception;
    public abstract void reload();
    public abstract boolean reload(String world);
    public abstract void saveAll();
    public abstract void save(String world);
    
    public abstract void closeAll();
    
    //Permission-checking methods
    public abstract boolean has(Player player, String node);
    public abstract boolean has(String worldName, String playerName, String node);
    public abstract boolean permission(Player player, String node);
    public abstract boolean permission(String worldName, String playerName, String node);
    
    //Permission-manipulation methods
    public abstract void addUserPermission(String world, String user, String node);
    public abstract void removeUserPermission(String world, String user, String node);
    public abstract void addGroupPermission(String world, String user, String node);
    public abstract void removeGroupPermission(String world, String user, String node);
    
    //Chat, prefix, suffix, build methods
    public abstract String getGroupProperName(String world, String group);
    
    public abstract String getUserPrefix(String world, String user);
    public abstract String getUserSuffix(String world, String user);
    public abstract boolean canUserBuild(String world, String user);
    
    public abstract String getGroupRawPrefix(String world, String group);
    public abstract String getGroupRawSuffix(String world, String group);
    public abstract boolean canGroupRawBuild(String world, String group);
    
    //Entry methods
    public abstract User safeGetUser(String world, String name) throws Exception;
    public abstract Group safeGetGroup(String world, String name) throws Exception;
    public abstract User getUserObject(String world, String name);
    public abstract Group getGroupObject(String world, String name);
    
    public abstract Group getDefaultGroup(String world);
    
    public abstract Collection<User> getUsers(String world);
    public abstract Collection<Group> getGroups(String world);
    
    //Parent-related methods
    public abstract boolean inGroup(String world, String user, String group);
    public abstract boolean inGroup(String world, String user, String groupWorld, String group);
    public abstract boolean inSingleGroup(String world, String user, String group);
    public abstract boolean inSingleGroup(String world, String user, String groupWorld, String group);

    public abstract String[] getGroups(String world, String name);
    public abstract Map<String, Set<String>> getAllGroups(String world, String name);
    //Weight-related methods
    public abstract int compareWeights(String firstWorld, String first, String secondWorld, String second);
    public abstract int compareWeights(String world, String first, String second);
    
    //Data-related methods
    public abstract String getRawInfoString(String world, String entryName, String path,boolean isGroup);
    
    public abstract Integer getRawInfoInteger(String world, String entryName, String path, boolean isGroup);
    
    public abstract Double getRawInfoDouble(String world, String entryName, String path, boolean isGroup);
    
    public abstract Boolean getRawInfoBoolean(String world, String entryName, String path, boolean isGroup);


    public abstract String getInfoString(String world, String entryName, String path,boolean isGroup);
    public abstract String getInfoString(String world, String entryName, String path, boolean isGroup, Comparator<String> comparator);
    
    public abstract Integer getInfoInteger(String world, String entryName, String path, boolean isGroup);
    public abstract Integer getInfoInteger(String world, String entryName, String path, boolean isGroup, Comparator<Integer> comparator);
    
    public abstract Double getInfoDouble(String world, String entryName, String path, boolean isGroup);
    public abstract Double getInfoDouble(String world, String entryName, String path, boolean isGroup, Comparator<Double> comparator);
    
    public abstract Boolean getInfoBoolean(String world, String entryName, String path, boolean isGroup);
    public abstract Boolean getInfoBoolean(String world, String entryName, String path, boolean isGroup, Comparator<Boolean> comparator);
    
    
    public abstract void addUserInfo(String world, String name, String path, Object data);
    public abstract void removeUserInfo(String world, String name, String path);
    public abstract void addGroupInfo(String world, String name, String path, Object data);
    public abstract void removeGroupInfo(String world, String name, String path);
    
    //Legacy methods
    @Deprecated
    public abstract String getGroupPermissionString(String world, String group, String path);
    @Deprecated
    public abstract int getGroupPermissionInteger(String world, String group, String path);
    @Deprecated
    public abstract boolean getGroupPermissionBoolean(String world, String group, String path);
    @Deprecated
    public abstract double getGroupPermissionDouble(String world, String group, String path);
    
    @Deprecated
    public abstract String getUserPermissionString(String world, String group, String path);
    @Deprecated
    public abstract int getUserPermissionInteger(String world, String group, String path);
    @Deprecated
    public abstract boolean getUserPermissionBoolean(String world, String group, String path);
    @Deprecated
    public abstract double getUserPermissionDouble(String world, String group, String path);
    
    @Deprecated
    public abstract String getPermissionString(String world, String group, String path);
    @Deprecated
    public abstract int getPermissionInteger(String world, String group, String path);
    @Deprecated
    public abstract boolean getPermissionBoolean(String world, String group, String path);
    @Deprecated
    public abstract double getPermissionDouble(String world, String group, String path);
    

    @Deprecated
    public abstract String getGroup(String world, String group);

}
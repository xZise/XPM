package com.nijiko.permissions;

import java.util.Collection;
import java.util.Set;

import org.bukkit.entity.Player;

/**
 * Permissions 2.x
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

/**
 * Interface for multiple permission handlers
 * 
 * @author Nijiko
 */
public abstract class PermissionHandler {

    public abstract void setDefaultWorld(String world);

    /**
     * This function attempts to load a world, creating the files if necessary.
     * @param world Name of world
     * @return True if the world is not already loaded, false otherwise
     * @throws Exception Exception thrown by the world loader
     */
    public abstract boolean loadWorld(String world) throws Exception;
    
    /**
     * This function forces the load of a world, even if the world is already loaded, creating the files if necessary.
     * @param world Name of world
     * @throws Exception Exception thrown by the world loader
     */
    public abstract void forceLoadWorld(String world) throws Exception;

    /**
     * Check if world is loaded.
     * @param world Name of world
     * @return True if the world is loaded, false otherwise.
     */
    public abstract boolean checkWorld(String world);

    /**
     * Get a set of the names of loaded worlds.
     * @return Set of the names of loaded worlds
     */
    public abstract Set<String> getWorlds();
    
    /**
     * Loads the default world
     * @throws Exception Exception thrown by the world loader
     */
    public abstract void load() throws Exception;

    /**
     * Reloads every world
     */
    public abstract void reload();

    /**
     * Reloads the specified world
     * @param world Name of world
     * @return True if the world is loaded, false otherwise.
     */
    public abstract boolean reload(String world);
    
    /**
     * Forces a save of the specified world
     * @param world Name of world
     */
    public abstract void save(String world);

    /**
     * Forces a save of all worlds
     */
    public abstract void saveAll();

    
    //Permission-checking methods
    /**
     * Simple alias for permission method. Easier to understand / recognize what
     * it does and is checking for.
     * 
     * @param player
     * @param permission
     * @return boolean
     */
//    @Deprecated
    public abstract boolean has(Player player, String permission);
    /**
     * Redirects to permission(String,String,String)
     * @param player
     * @param permission
     * @return
     */
//    @Deprecated
    public abstract boolean permission(Player player, String permission);

    /**
     * Checks to see if a player has permission to a specific tree node. <br />
     * <br />
     * Example usage: <blockquote>
     * 
     * <pre>
     * boolean canReload = Plugin.Permissions.Security.permission(playerWorld, playerName,
     *         &quot;permission.reload&quot;);
     * if (canReload) {
     *     System.out.println(&quot;The user can reload!&quot;);
     * } else {
     *     System.out.println(&quot;The user has no such permission!&quot;);
     * }
     * </pre>
     * 
     * </blockquote>
     * 
     * @param world
     * @param name
     * @param permission
     * @return boolean
     */
    public abstract boolean permission(String world, String name, String permission);
    /**
     * Simple alias for permission method. Easier to understand / recognize what
     * it does and is checking for.
     * 
     * @param player
     * @param permission
     * @return boolean
     */
    public abstract boolean has(String world, String name, String permission);
  
    
    //Group-related methods
    /**
     * Grabs group prefix, line that comes before the group. <br />
     * <br />
     * Namespace: groups.name.info.prefix
     * 
     * @param world
     * @param group
     * @return String
     */
    public abstract String getGroupPrefix(String world, String group);

    /**
     * Grabs group suffix, line that comes after the group. <br />
     * <br />
     * Namespace: groups.name.info.suffix
     * 
     * @param world
     * @param group
     * @return String
     */
    public abstract String getGroupSuffix(String world, String group);

    /**
     * Checks to see if the group has build permission. <br />
     * <br />
     * Namespace: groups.name.info.build
     * 
     * @param world
     * @param group
     * @return String
     */
    public abstract boolean canGroupBuild(String world, String group);
    

    //User&Group object related methods
    /**
     * This method returns the specified user, creating the user if necessary. Never returns null.
     * @param world
     * @param name
     * @return
     * @throws Exception
     */
    public abstract User safeGetUser(String world, String name)
            throws Exception;
    /**
     * This method returns the specified group, creating it if necessary. Never returns null.
     * @param world
     * @param name
     * @return
     * @throws Exception
     */
    public abstract Group safeGetGroup(String world, String name)
            throws Exception;

    public abstract Collection<User> getUsers(String world);

    public abstract Collection<Group> getGroups(String world);

    /**
     * This method returns the specified user, and returns null if no such user exists.
     * @param world
     * @param name
     * @return
     * @throws Exception
     */
    public abstract User getUserObject(String world, String name);

    /**
     * This method returns the specified group, and returns null if no such group exists.
     * @param world
     * @param name
     * @return
     * @throws Exception
     */
    public abstract Group getGroupObject(String world, String name);

    /**
     * This method returns the default group for the specified world.
     * @param world
     * @param name
     * @return
     * @throws Exception
     */
    public abstract Group getDefaultGroup(String world);

    /**
     * This retrieves the parent/ancestor groups for the specified user.
     * @param world
     * @param name
     * @param ancestors If true, ancestors(parents of parents) will also be returned.
     * @return
     */
    public abstract Set<Entry> getUserParentGroups(String world, String name, boolean ancestors);
    


    /**
     * This retrieves the parent/ancestor groups for the specified group.
     * @param world
     * @param name
     * @param ancestors If true, ancestors(parents of parents) will also be returned.
     * @return
     */
    public abstract Set<Entry> getGroupParentGroups(String world, String name, boolean ancestors);
    
    /**
     * Check if specified user exists.
     * @param world
     * @param name
     * @return
     */
    public abstract boolean userExists(String world, String name);

    /**
     * Check if specified group exists.
     * @param world
     * @param name
     * @return
     */
    public abstract boolean groupExists(String world, String name);
    
    /**
     * Depreciated alias for getGroupName()
     * @param world
     * @param name
     * @return
     */
    @Deprecated
    public abstract String getGroup(String world, String name);
    
    /**
     * Grabs group's name (i.e the name in the config file) with proper capitali[zs]ation. <br />
     * <br />
     * Namespace: groups.name
     * 
     * @param group
     * @return String
     */
    public abstract String getGroupName(String world, String name);
    
    /**
     * Grabs users groups in the same world. <br />
     * <br />
     * 
     * @param group
     * @return Array
     */
    public abstract String[] getGroups(String world, String name);


    /**
     * Checks to see if the player is in the requested group.
     * 
     * @param world
     * @param name
     *            - Player
     * @param group
     *            - Group to be checked upon.
     * @return boolean
     */
    public abstract boolean inGroup(String world, String name, String group);
    /**
     * Checks to see if the player is in the requested group.
     * @param world User's world
     * @param name User's name
     * @param groupWorld Group's world
     * @param group Group's name
     * @return
     */
    public abstract boolean inGroup(String world, String name, String groupWorld,
            String group);
    /**
     * Checks to see if a player is in a single group. This does not check
     * inheritance.
     * 
     * @param world
     * @param name
     *            - Player
     * @param group
     *            - Group to be checked
     * @return boolean
     */
    public abstract boolean inSingleGroup(String world, String name,
            String group);

    /** 
     * Checks to see if a player is in a single group. This does not check
     * inheritance.
     * @param world User's world
     * @param name User's name
     * @param groupWorld Group's world
     * @param group Group's name
     * @return
     */
    public abstract boolean inSingleGroup(String world, String name, String groupWorld,
            String group);


    /**
     * Get info nodes from a group that contain values. <br />
     * <br />
     * Grab Group Permission String values.
     * 
     * @param world
     * @param group
     * @param permission
     * @return String. If no string found return "".
     */
    public abstract String getGroupPermissionString(String world, String group,
            String permission);

    /**
     * Get permission nodes from a group that contain values. <br />
     * <br />
     * Grab Group Permission Integer values.
     * 
     * @param world
     * @param group
     * @param permission
     * @return Integer. No integer found return -1.
     */
    public abstract int getGroupPermissionInteger(String world, String group,
            String permission);

    /**
     * Get permission nodes from a group that contain values. <br />
     * <br />
     * Grab Group Permission String values.
     * 
     * @param group
     * @param permission
     * @return Boolean. No boolean found return false.
     */
    public abstract boolean getGroupPermissionBoolean(String world,
            String group, String permission);

    /**
     * Get permission nodes from a group that contain values. <br />
     * <br />
     * Grab Group Permission Double values.
     * 
     * @param world
     * @param group
     * @param permission
     * @return Double. No value found return -1.0
     */
    public abstract double getGroupPermissionDouble(String world, String group,
            String permission);

    /**
     * Get permission nodes from a specific user that contain values. <br />
     * <br />
     * Grab User Permission String values.
     * 
     * @param world
     * @param group
     * @param permission
     * @return String. If no string found return "".
     */
    public abstract String getUserPermissionString(String world, String name,
            String permission);

    /**
     * Get permission nodes from a specific user that contain values. <br />
     * <br />
     * Grab User Permission Integer values.
     * 
     * @param world
     * @param group
     * @param permission
     * @return Integer. No integer found return -1.
     */
    public abstract int getUserPermissionInteger(String world, String name,
            String permission);

    /**
     * Get permission nodes from a specific user that contain values. <br />
     * <br />
     * Grab User Permission Boolean values.
     * 
     * @param world
     * @param group
     * @param permission
     * @return Boolean. No boolean found return false.
     */
    public abstract boolean getUserPermissionBoolean(String world, String name,
            String permission);

    /**
     * Get permission nodes from a specific user that contain values. <br />
     * <br />
     * Grab User Permission Double values.
     * 
     * @param world
     * @param group
     * @param permission
     * @return Double. No value found return -1.0
     */
    public abstract double getUserPermissionDouble(String world, String name,
            String permission);

    /**
     * Get permission nodes from a user / group that contain values. <br />
     * <br />
     * Grab User Permission String values.
     * 
     * @param world
     * @param group
     * @param permission
     * @return String. If no string found return "".
     */
    public abstract String getPermissionString(String world, String name,
            String permission);

    /**
     * Get permission nodes from a user / group that contain values. <br />
     * <br />
     * Grab User Permission Integer values.
     * 
     * @param world
     * @param group
     * @param permission
     * @return Integer. No integer found return -1.
     */
    public abstract int getPermissionInteger(String world, String name,
            String permission);

    /**
     * Get permission nodes from a user / group that contain values. <br />
     * <br />
     * Grab User Permission Boolean values.
     * 
     * @param world
     * @param group
     * @param permission
     * @return Boolean. No boolean found return false.
     */
    public abstract boolean getPermissionBoolean(String world, String name,
            String permission);

    /**
     * Get permission nodes from a user / group that contain values. <br />
     * <br />
     * Grab User Permission Double values.
     * 
     * @param world
     * @param group
     * @param permission
     * @return Double. No value found return -1.0
     */
    public abstract double getPermissionDouble(String world, String name,
            String permission);

    public abstract void addGroupInfo(String world, String group, String node,
            Object data);

    public abstract void removeGroupInfo(String world, String group, String node);

    public abstract void addUserPermission(String world, String user,
            String node);

    public abstract void removeUserPermission(String world, String user,
            String node);
    /**
     * Compares the weights of two different users
     * @param firstWorld World of first user
     * @param first Name of first user
     * @param secondWorld World of second user
     * @param second Name of second user
     * @return -1 if the second user's weight is higher than the first, 1 if vice versa, 0 if equal.
     */
    public abstract int compareWeights(String firstWorld, String first, String secondWorld, String second);

    public abstract int compareWeights(String world, String first, String second);



}

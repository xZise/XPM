package com.nijiko.permissions;

import java.util.Collection;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

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
 * Abstract method for multiple permission handlers
 *
 * @author Nijiko
 */
public abstract class PermissionHandler {

    public abstract void setDefaultWorld(String world);


    public abstract boolean loadWorld(String world) throws Exception;
    public abstract void forceLoadWorld(String world) throws Exception;
    public abstract boolean checkWorld(String world);

    public abstract void load() throws Exception;
    public abstract void load(String world, Configuration userConfig, Configuration groupConfig);
    public abstract void reload();
    public abstract boolean reload(String world);

    // Cache
    public abstract void setCache(String world, Map<String, Boolean> Cache);
    public abstract void setCacheItem(String world, String player, String permission, boolean data);
    public abstract Map<String, Boolean> getCache(String world);
    public abstract boolean getCacheItem(String world, String player, String permission);
    public abstract void removeCachedItem(String world, String player, String permission);
    public abstract void clearCache(String world);
    public abstract void clearAllCache();


    /**
     * Simple alias for permission method.
     * Easier to understand / recognize what it does and is checking for.
     *
     * @param player
     * @param permission
     * @return boolean
     */
    public abstract boolean has(Player player, String permission);

    /**
     * Checks to see if a player has permission to a specific tree node.
     * <br /><br />
     * Example usage:
     * <blockquote><pre>
     * boolean canReload = Plugin.Permissions.Security.permission(player, "permission.reload");
     * if(canReload) {
     *	System.out.println("The user can reload!");
     * } else {
     *	System.out.println("The user has no such permission!");
     * }
     * </pre></blockquote>
     *
     * @param player
     * @param permission
     * @return boolean
     */
    public abstract boolean permission(Player player, String permission);

    
    /**
     * Grabs group name.
     * <br /><br />
     * Namespace: groups.name
     *
     * @param group
     * @return String
     */
    public abstract String getGroup(String world, String name);

    
    /**
     * Grabs users groups.
     * <br /><br />
     * 
     * @param group
     * @return Array
     */
    public abstract String[] getGroups(String world, String name);

    /**
     * Checks to see if the player is in the requested group. 
     *
     * @param world
     * @param name - Player
     * @param group - Group to be checked upon.
     * @return boolean
     */
    public abstract boolean inGroup(String world, String name, String group);

    /**
     * Checks to see if a player is in a single group.
     * This does not check inheritance.
     * 
     * @param world
     * @param name - Player
     * @param group - Group to be checked
     * @return boolean
     */
    public abstract boolean inSingleGroup(String world, String name, String group);
    
    /**
     * Grabs group prefix, line that comes before the group.
     * <br /><br />
     * Namespace: groups.name.info.prefix
     *
     * @param world
     * @param group
     * @return String
     */
    public abstract String getGroupPrefix(String world, String group);

    /**
     * Grabs group suffix, line that comes after the group.
     * <br /><br />
     * Namespace: groups.name.info.suffix
     *
     * @param world
     * @param group
     * @return String
     */
    public abstract String getGroupSuffix(String world, String group);

    /**
     * Checks to see if the group has build permission.
     * <br /><br />
     * Namespace: groups.name.info.build
     *
     * @param world
     * @param group
     * @return String
     */
    public abstract boolean canGroupBuild(String world, String group);
    
    public abstract void addUserPermission(String world, String user, String node);
    public abstract void removeUserPermission(String world, String user, String node);

	//Addition by rcjrrjcr
    public abstract void save(String world);
    public abstract void saveAll();
	//End of addition by rcjrrjcr
    
    public abstract User safeGetUser(String world, String name) throws Exception;
    public abstract Group safeGetGroup(String world, String name) throws Exception;
    public abstract Collection<User> getUsers(String world);
    public abstract Collection<Group> getGroups(String world);
    public abstract User getUserObject(String world, String name);
    public abstract Group getGroupObject(String world, String name);
}

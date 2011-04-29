package com.nijiko.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nijiko.data.GroupStorage;
import com.nijiko.data.GroupWorld;
import com.nijiko.data.StorageFactory;
import com.nijiko.data.UserStorage;

public class ModularControl extends PermissionHandler {
    private Map<String, UserStorage> WorldUserStorage = new HashMap<String, UserStorage>();
    private Map<String, GroupStorage> WorldGroupStorage = new HashMap<String, GroupStorage>();
    private Map<String, String> WorldUserStorageCopy = new HashMap<String, String>();
    private Map<String, String> WorldGroupStorageCopy = new HashMap<String, String>();

    private Map<String, Map<String, Group>> WorldGroups = new HashMap<String, Map<String, Group>>();
    private Map<String, Map<String, User>> WorldUsers = new HashMap<String, Map<String, User>>();
    private Map<String, Group> WorldBase = new HashMap<String, Group>();
    // private Configuration storageConfig;
    private String defaultWorld = "";

    public ModularControl(Configuration storageConfig) {
        // this.storageConfig = storageConfig;
        StorageFactory.setConfig(storageConfig);
    }

    @Override
    public void setDefaultWorld(String world) {
        this.defaultWorld = world;
    }

    @Override
    public boolean loadWorld(String world) throws Exception {
        if (checkWorld(world)) {
            forceLoadWorld(world);
            return true;
        }
        return false;
    }



    @Override
    public void forceLoadWorld(String world) throws Exception {
        UserStorage userStore = StorageFactory.getUserStorage(world);
        GroupStorage groupStore = StorageFactory.getGroupStorage(world);
        load(world, userStore, groupStore);
    }

    @Override
    public boolean checkWorld(String world) {
        return ((WorldUserStorage.get(world) == null) && (WorldUserStorageCopy
                .get(world) == null))
                || (((WorldGroupStorage.get(world) == null) && (WorldGroupStorageCopy
                        .get(world) == null)));
    }

    @Override
    public void load() throws Exception {
        this.loadWorld(defaultWorld);
    }

    @Override
    public void reload() {
        for (UserStorage store : WorldUserStorage.values()) {
            store.reload();
        }
        for (GroupStorage store : WorldGroupStorage.values()) {
            store.reload();
        }
        WorldBase.clear();
        WorldUsers.clear();
        WorldGroups.clear();
        Set<String> worlds = this.getWorlds();
        for(String world : worlds)
        {
            load(world, getUserStorage(world), getGroupStorage(world));
        }
        
    }

    private UserStorage getUserStorage(String world) {
        if (world == null)
            return null;
        return this.WorldUserStorage.get(getParentWorldUser(world));
    }

    private GroupStorage getGroupStorage(String world) {
        if (world == null)
            return null;
        return this.WorldGroupStorage.get(getParentWorldGroup(world));
    }

    @Override
    public boolean reload(String world) {
        UserStorage userStore = getUserStorage(world);
        GroupStorage groupStore = getGroupStorage(world);
        if (userStore == null && groupStore == null)
            return false;
        if (userStore != null)
            userStore.reload();
        if (groupStore != null)
            groupStore.reload();
        WorldBase.remove(world);
        WorldUsers.remove(world);
        WorldGroups.remove(world);
        load(world,userStore,groupStore);
        return true;
    }

    @Override
    public boolean has(String world, String name, String permission) {
        return permission(world, name, permission);
    }

    @Override
    public boolean has(Player player, String permission) {
        return permission(player, permission);
    }

    @Override
    public boolean permission(Player player, String permission) {
        String name = player.getName();
        String worldName = player.getWorld().getName();
        return permission(worldName, name, permission);
    }

    @Override
    public boolean permission(String world, String name, String permission) {
        if (name == null || name.isEmpty() || world == null || world.isEmpty())
            return true;
        world = getParentWorldUser(world);
        User user = this.getUserObject(world, name);
        if(user==null) return false;
        return user.hasPermission(permission);
    }

    @Override
    public String getGroupName(String world, String name) {
        world = getParentWorldGroup(world);
        Map<String, Group> groups = this.WorldGroups.get(world);
        if (groups == null)
            return null;
        Group g = groups.get(name.toLowerCase());
        if (g == null)
            return null;
        return g.getName();
    }

    @Override
    public Set<Group> getUserParentGroups(String world, String name, boolean ancestors) {
        world = getParentWorldUser(world);
        User u = this.getUserObject(world, name);
        if(u==null)
        {
            Set<Group> groups = new HashSet<Group>();
            Group defaultGroup = this.getDefaultGroup(world);
            if(defaultGroup == null) return groups;
            groups.add(defaultGroup);
            groups.addAll(defaultGroup.getAncestors());
            return groups;
        }
        return ancestors ? u.getAncestors() : this.stringToGroups(u.getParents());
    }
    
    @Override
    public Set<Group> getGroupParentGroups(String world, String name, boolean ancestors) {
        world = getParentWorldUser(world);
        Group g = this.getGroupObject(world, name);
        if(g==null)
        {
            return new HashSet<Group>();            
        }
        return ancestors ? g.getAncestors() : this.stringToGroups(g.getParents());
    }

    @Override
    public boolean inGroup(String world, String name, String groupWorld,
            String group) {
        world = getParentWorldUser(world);
        groupWorld = getParentWorldGroup(groupWorld);
        try {
            return safeGetUser(world, name).inGroup(groupWorld, group);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean inSingleGroup(String world, String name, String groupWorld,
            String group) {
        world = getParentWorldUser(world);
        groupWorld = getParentWorldGroup(groupWorld);
        User u = this.getUserObject(world, name);
        if(u==null)
        {
            if(!world.equalsIgnoreCase(groupWorld)) return false;
            Group g = this.getDefaultGroup(world);
            if(g!=null&&g.getWorld().equalsIgnoreCase(groupWorld)&&g.getName().equalsIgnoreCase(group)) return true;
            return false;
        }
        return u.getParents().contains(new GroupWorld(groupWorld, group));
    }

    @Override
    public String getGroupPrefix(String world, String group) {
        world = getParentWorldGroup(world);
        Group g = this.getGroupObject(world, group);
        if(g==null) return "";
        return g.getPrefix();
    }

    @Override
    public String getGroupSuffix(String world, String group) {
        world = getParentWorldGroup(world);
        Group g = this.getGroupObject(world, group);
        if(g==null) return "";
        return g.getSuffix();
    }

    @Override
    public boolean canGroupBuild(String world, String group) {
        world = getParentWorldGroup(world);
        Group g = this.getGroupObject(world, group);
        if(g==null) return false;
        return g.canBuild();
    }

    @Override
    public void save(String world) {
        UserStorage userStore = getUserStorage(world);
        GroupStorage groupStore = getGroupStorage(world);
        if (userStore != null)
            userStore.save();
        if (groupStore != null)
            groupStore.save();
    }

    @Override
    public void saveAll() {
        Collection<UserStorage> userStores = this.WorldUserStorage.values();
        for (UserStorage userStore : userStores) {
            userStore.save();
        }
        Collection<GroupStorage> groupStores = this.WorldGroupStorage.values();
        for (GroupStorage groupStore : groupStores) {
            groupStore.save();
        }
    }

    Set<Group> stringToGroups(Set<GroupWorld> raws) {
        Set<Group> groupSet = new HashSet<Group>();
        for (GroupWorld raw : raws) {
            String world = getParentWorldGroup(raw.getWorld());
            Map<String, Group> gMap = this.WorldGroups.get(world);
            if (gMap != null) {
                Group g = gMap.get(raw.getName().toLowerCase());
                if (g != null)
                    groupSet.add(g);
            }
        }
        return groupSet;
    }

    @Override
    public User safeGetUser(String world, String name) throws Exception {
        try {
            loadWorld(world);
        } catch (Exception e) {
            throw new Exception("Error creating user " + name + " in world "
                    + world + " due to storage problems!", e);
        }
        world = getParentWorldUser(world);
        if (this.WorldUsers.get(world) == null)
            this.WorldUsers.put(world,
                    new HashMap<String, User>());
        if (this.WorldUsers.get(world).get(name.toLowerCase()) == null)
            this.WorldUsers.get(world).put(name.toLowerCase(),
                    new User(this, getUserStorage(world), name, world, true));
        return this.WorldUsers.get(world).get(name.toLowerCase());
    }

    @Override
    public Group safeGetGroup(String world, String name) throws Exception {
        try {
            loadWorld(world);
        } catch (Exception e) {
            throw new Exception("Error creating group " + name + " in world "
                    + world + " due to storage problems!", e);
        }
        world = getParentWorldGroup(world);
        if(WorldGroupStorageCopy.get(world)!=null)world = WorldGroupStorageCopy.get(world);
        if (this.WorldGroups.get(world) == null)
            this.WorldGroups.put(world, new HashMap<String, Group>());
        if (this.WorldGroups.get(world).get(name.toLowerCase()) == null)
            this.WorldGroups.get(world).put(name.toLowerCase(),
                    new Group(this, getGroupStorage(world), name, world, true));
        return this.WorldGroups.get(world)
                .get(name.toLowerCase());
    }
    
    @Override
    public Group getDefaultGroup(String world) {
        world = getParentWorldGroup(world);
        return this.WorldBase.get(world);
    }

    @Override
    public Collection<User> getUsers(String world) {
        world = getParentWorldUser(world);
        if (WorldUsers.get(world) == null)
            return new HashSet<User>();
        return WorldUsers.get(world).values();
    }

    @Override
    public Collection<Group> getGroups(String world) {
        world = getParentWorldGroup(world);
        if (WorldGroups.get(world.toLowerCase()) == null)
            return new HashSet<Group>();
        return WorldGroups.get(world).values();
    }

    @Override
    public User getUserObject(String world, String name) {
        world = getParentWorldUser(world);
        if (WorldUsers.get(world) == null)
            return null;
        return WorldUsers.get(world).get(name.toLowerCase());
    }

    @Override
    public Group getGroupObject(String world, String name) {
        world = getParentWorldGroup(world);
        if (WorldGroups.get(world) == null)
            return null;
        return WorldGroups.get(world).get(name.toLowerCase());
    }

    @Override
    public String getGroup(String world, String name) {
        return this.getGroupName(world, name);
    }

    @Override
    public String[] getGroups(String world, String name) {
        world = getParentWorldGroup(world);
        Set<Group> groups;
        User u = this.getUserObject(world, name);
        if(u==null) return new String[0];
        groups = u.getAncestors();
        List<String> groupList = new ArrayList<String>(groups.size());
        for (Group g : groups) {
            if (g == null)
                continue;
            if (g.getWorld().equalsIgnoreCase(world))
                groupList.add(g.getName());
        }
        return groupList.toArray(new String[0]);
    }

    @Override
    public boolean inGroup(String world, String name, String group) {
        return inGroup(world, name, world, group);
    }

    @Override
    public boolean inSingleGroup(String world, String name, String group) {
        return inSingleGroup(world, name, world, group);
    }

    @Override
    // @Deprecated
    public void addUserPermission(String world, String user, String node) {
        try {
            safeGetUser(world, user).addPermission(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    // @Deprecated
    public void removeUserPermission(String world, String user, String node) {
        try {
            safeGetUser(world, user).removePermission(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load(String world, UserStorage userStore,
            GroupStorage groupStore) {
        if (userStore == null || groupStore == null)
            return;
        String userWorld = userStore.getWorld();
        if (!world.equalsIgnoreCase(userWorld))
            this.WorldUserStorageCopy.put(world, userWorld);
        String groupWorld = groupStore.getWorld();
        if (!world.equalsIgnoreCase(groupWorld))
            this.WorldGroupStorageCopy.put(world, groupWorld);
        this.WorldUserStorage
                .put(userStore.getWorld(), userStore);
        this.WorldGroupStorage.put(groupStore.getWorld(),
                groupStore);

        Map<String, User> users = new HashMap<String, User>();
        Set<String> userNames = userStore.getUsers();
        for (String userName : userNames) {
            User user = new User(this, userStore, userName, userWorld, false);
            users.put(userName.toLowerCase(), user);
        }
        WorldUsers.put(world, users);

        HashMap<String, Group> groups = new HashMap<String, Group>();
        Set<String> groupNames = groupStore.getGroups();
        for (String groupName : groupNames) {
            Group group = new Group(this, groupStore, groupName, groupWorld, false);
            groups.put(groupName.toLowerCase(), group);
            if(group.isDefault()&&WorldBase.get(world)==null) WorldBase.put(groupWorld, group);
        }
        WorldGroups.put(world, groups);
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
    
    public String getParentWorldGroup(String world)
    {
        if(WorldGroupStorageCopy.get(world)!=null)return WorldGroupStorageCopy.get(world);
        return world;
    }
    
    public String getParentWorldUser(String world)
    {

        if(WorldUserStorageCopy.get(world)!=null)world = WorldUserStorageCopy.get(world);
        return world;
    }

    @Override
    public Set<String> getWorlds() {
        Set<String> worlds = new HashSet<String>();
        worlds.addAll(this.WorldUserStorageCopy.keySet());
        worlds.addAll(this.WorldUserStorage.keySet());
        return worlds;
    }

    @Override
    public boolean userExists(String world, String name) {
        world = getParentWorldUser(world);
        if (WorldUsers.get(world) == null)
            return false;
        return WorldUsers.get(world).get(name.toLowerCase()) != null;
    }

    @Override
    public boolean groupExists(String world, String name) {
        world = getParentWorldUser(world);
        if (WorldGroups.get(world) == null)
            return false;
        return WorldGroups.get(world).get(name.toLowerCase()) != null;
    }

}
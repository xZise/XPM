package com.nijiko.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nijiko.data.GroupStorage;
import com.nijiko.data.GroupWorld;
import com.nijiko.data.SqlStorage;
import com.nijiko.data.StorageFactory;
import com.nijiko.data.UserStorage;

public class ModularControl extends PermissionHandler {
    private Map<String, UserStorage> userStores = new HashMap<String, UserStorage>();
    private Map<String, GroupStorage> groupStores = new HashMap<String, GroupStorage>();
    private Map<String, String> userStorageMirrorings = new HashMap<String, String>();
    private Map<String, String> groupStorageMirrorings = new HashMap<String, String>();

    private Map<String, String> userStorageInheritance = new HashMap<String, String>();
    private Map<String, String> groupStorageInheritance = new HashMap<String, String>();

    private Map<String, Map<String, Group>> worldGroups = new HashMap<String, Map<String, Group>>();
    private Map<String, Map<String, User>> worldUsers = new HashMap<String, Map<String, User>>();
    private Map<String, Group> defaultGroups = new HashMap<String, Group>();
    private Configuration storageConfig;
    private String defaultWorld = "";

    @Override
    public void closeAll() {
        this.saveAll();
        SqlStorage.closeAll();
    }

    public ModularControl(Configuration storageConfig) {
        this.storageConfig = storageConfig;
        StorageFactory.setConfig(storageConfig);
        loadWorldInheritance();
    }

    private void loadWorldInheritance() {
        userStorageInheritance.clear();
        groupStorageInheritance.clear();
        storageConfig.load();
        List<String> worlds = storageConfig.getKeys("permissions.storage.world-inheritance");
        Map<String, String> worldInheritance = new HashMap<String, String>();
        if(worlds != null) {
            for (String world : worlds) {
                String parentWorld = storageConfig.getString("permissions.storage.world-inheritance." + world);
                if (parentWorld != null && !world.equals("*"))
                    worldInheritance.put(world, parentWorld);
            }
        }
        List<String> userWorlds = storageConfig.getKeys("permissions.storage.user.world-inheritance");
        if(userWorlds != null) {
            for (String userWorld : userWorlds) {
                String parentWorld = storageConfig.getString("permissions.storage.user.world-inheritance.users" + userWorld);
                if (parentWorld != null && !userWorld.equals("*"))
                    userStorageInheritance.put(userWorld, parentWorld);
            }
        }
        for (Map.Entry<String, String> inherit : worldInheritance.entrySet()) {
            if (!userStorageInheritance.containsKey(inherit.getKey())) {
                userStorageInheritance.put(inherit.getKey(), inherit.getValue());
            }
        }

        List<String> groupWorlds = storageConfig.getKeys("permissions.storage.group.world-inheritance");
        if(groupWorlds != null) {
            for (String groupWorld : groupWorlds) {
                String parentWorld = storageConfig.getString("permissions.storage.group.world-inheritance." + groupWorld);
                if (parentWorld != null && !groupWorld.equals("*"))
                    userStorageInheritance.put(groupWorld, parentWorld);
            }
        }
        for (Map.Entry<String, String> inherit : worldInheritance.entrySet()) {
            if (!groupStorageInheritance.containsKey(inherit.getKey())) {
                groupStorageInheritance.put(inherit.getKey(), inherit.getValue());
            }
        }
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
        return ((userStores.get(world) == null) && (userStorageMirrorings.get(world) == null)) || (((groupStores.get(world) == null) && (groupStorageMirrorings.get(world) == null)));
    }

    @Override
    public void load() throws Exception {
        this.loadWorld("*"); // Global permissions
        this.loadWorld(defaultWorld);
    }

    @Override
    public void reload() {
        for (UserStorage store : userStores.values()) {
            store.reload();
        }
        for (GroupStorage store : groupStores.values()) {
            store.reload();
        }
        defaultGroups.clear();
        worldUsers.clear();
        worldGroups.clear();
        Set<String> worlds = this.getWorlds();
        for (String world : worlds) {
            load(world, getUserStorage(world), getGroupStorage(world));
        }

    }

    private UserStorage getUserStorage(String world) {
        if (world == null)
            return null;
        return this.userStores.get(getParentWorldUser(world));
    }

    private GroupStorage getGroupStorage(String world) {
        if (world == null)
            return null;
        return this.groupStores.get(getParentWorldGroup(world));
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
        defaultGroups.remove(world);
        worldUsers.remove(world);
        worldGroups.remove(world);
        load(world, userStore, groupStore);
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
        if (user == null)
            return false;
        return user.hasPermission(permission);
    }

    @Override
    public String getGroupName(String world, String name) {
        world = getParentWorldGroup(world);
        Map<String, Group> groups = this.worldGroups.get(world);
        if (groups == null)
            return null;
        Group g = groups.get(name.toLowerCase());
        if (g == null)
            return null;
        return g.getName();
    }

    @Override
    public Set<Entry> getUserParentGroups(String world, String name, boolean ancestors) {
        world = getParentWorldUser(world);
        User u = this.getUserObject(world, name);
        if (u == null) {
            Set<Entry> groups = new HashSet<Entry>();
            Group defaultGroup = this.getDefaultGroup(world);
            if (defaultGroup == null)
                return groups;
            groups.add(defaultGroup);
            groups.addAll(defaultGroup.getAncestors());
            return groups;
        }
        return ancestors ? u.getAncestors() : u.getParents();
    }

    @Override
    public Set<Entry> getGroupParentGroups(String world, String name, boolean ancestors) {
        world = getParentWorldUser(world);
        Group g = this.getGroupObject(world, name);
        if (g == null) {
            return new HashSet<Entry>();
        }
        return ancestors ? g.getAncestors() : g.getParents();
    }

    @Override
    public boolean inGroup(String world, String name, String groupWorld, String group) {
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
    public boolean inSingleGroup(String world, String name, String groupWorld, String group) {
        world = getParentWorldUser(world);
        groupWorld = getParentWorldGroup(groupWorld);
        User u = this.getUserObject(world, name);
        if (u == null) {
            if (!world.equalsIgnoreCase(groupWorld))
                return false;
            Group g = this.getDefaultGroup(world);
            if (g != null && g.getWorld().equalsIgnoreCase(groupWorld) && g.getName().equalsIgnoreCase(group))
                return true;
            return false;
        }
        return u.getParents().contains(new GroupWorld(groupWorld, group));
    }

    @Override
    public String getGroupPrefix(String world, String group) {
        world = getParentWorldGroup(world);
        Group g = this.getGroupObject(world, group);
        if (g == null)
            return "";
        String prefix = g.getPrefix();
        return prefix == null ? "" : prefix;
    }

    @Override
    public String getGroupSuffix(String world, String group) {
        world = getParentWorldGroup(world);
        Group g = this.getGroupObject(world, group);
        if (g == null)
            return "";
        String suffix = g.getSuffix();
        return suffix == null ? "" : suffix;
    }

    @Override
    public boolean canGroupBuild(String world, String group) {
        world = getParentWorldGroup(world);
        Group g = this.getGroupObject(world, group);
        if (g == null)
            return false;
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
        Collection<UserStorage> userStores = this.userStores.values();
        for (UserStorage userStore : userStores) {
            userStore.save();
        }
        Collection<GroupStorage> groupStores = this.groupStores.values();
        for (GroupStorage groupStore : groupStores) {
            groupStore.save();
        }
    }

    LinkedHashSet<Group> stringToGroups(LinkedHashSet<GroupWorld> raws) {
        LinkedHashSet<Group> groupSet = new LinkedHashSet<Group>();
        for (GroupWorld raw : raws) {
            String world = getParentWorldGroup(raw.getWorld());
            Map<String, Group> gMap = this.worldGroups.get(world);
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
            throw new Exception("Error creating user " + name + " in world " + world + " due to storage problems!", e);
        }
        world = getParentWorldUser(world);
        if (this.worldUsers.get(world) == null)
            this.worldUsers.put(world, new HashMap<String, User>());
        if (this.worldUsers.get(world).get(name.toLowerCase()) == null)
            this.worldUsers.get(world).put(name.toLowerCase(), new User(this, getUserStorage(world), name, world, true));
        return this.worldUsers.get(world).get(name.toLowerCase());
    }

    @Override
    public Group safeGetGroup(String world, String name) throws Exception {
        try {
            loadWorld(world);
        } catch (Exception e) {
            throw new Exception("Error creating group " + name + " in world " + world + " due to storage problems!", e);
        }
        world = getParentWorldGroup(world);
        if (groupStorageMirrorings.get(world) != null)
            world = groupStorageMirrorings.get(world);
        if (this.worldGroups.get(world) == null)
            this.worldGroups.put(world, new HashMap<String, Group>());
        if (this.worldGroups.get(world).get(name.toLowerCase()) == null)
            this.worldGroups.get(world).put(name.toLowerCase(), new Group(this, getGroupStorage(world), name, world, true));
        return this.worldGroups.get(world).get(name.toLowerCase());
    }

    @Override
    public Group getDefaultGroup(String world) {
        world = getParentWorldGroup(world);
        return this.defaultGroups.get(world);
    }

    @Override
    public Collection<User> getUsers(String world) {
        world = getParentWorldUser(world);
        if (worldUsers.get(world) == null)
            return null;
        return worldUsers.get(world).values();
    }

    @Override
    public Collection<Group> getGroups(String world) {
        world = getParentWorldGroup(world);
        if (worldGroups.get(world.toLowerCase()) == null)
            return null;
        return worldGroups.get(world).values();
    }

    @Override
    public User getUserObject(String world, String name) {
        world = getParentWorldUser(world);
        if (worldUsers.get(world) == null)
            return world.equals("*") ? null : getUserObject("*", name);
        return worldUsers.get(world).get(name.toLowerCase());
    }

    @Override
    public Group getGroupObject(String world, String name) {
        world = getParentWorldGroup(world);
        if (worldGroups.get(world) == null)
            return world.equals("*") ? null : getGroupObject("*", name);
        return worldGroups.get(world).get(name.toLowerCase());
    }

    @Override
    public String getGroup(String world, String name) {
        return this.getGroupName(world, name);
    }

    @Override
    public String[] getGroups(String world, String name) {
        world = getParentWorldGroup(world);
        Set<Entry> parents;
        User u = this.getUserObject(world, name);
        if (u == null)
            return new String[0];
        parents = u.getAncestors();
        List<String> groupList = new ArrayList<String>(parents.size());
        for (Entry e : parents) {
            if (e instanceof Group) {
                Group g = (Group) e;
                if (g.getWorld().equalsIgnoreCase(world))
                    groupList.add(g.getName());
            }
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

    private void load(String world, UserStorage userStore, GroupStorage groupStore) {
        if (userStore == null || groupStore == null)
            return;
        String userWorld = userStore.getWorld();
        if (!world.equalsIgnoreCase(userWorld))
            this.userStorageMirrorings.put(world, userWorld);
        String groupWorld = groupStore.getWorld();
        if (!world.equalsIgnoreCase(groupWorld))
            this.groupStorageMirrorings.put(world, groupWorld);
        this.userStores.put(userStore.getWorld(), userStore);
        this.groupStores.put(groupStore.getWorld(), groupStore);

        Map<String, User> users = new HashMap<String, User>();
        Set<String> userNames = userStore.getUsers();
        for (String userName : userNames) {
            User user = new User(this, userStore, userName, userWorld, false);
            users.put(userName.toLowerCase(), user);
        }
        worldUsers.put(world, users);

        HashMap<String, Group> groups = new HashMap<String, Group>();
        Set<String> groupNames = groupStore.getGroups();
        for (String groupName : groupNames) {
            Group group = new Group(this, groupStore, groupName, groupWorld, false);
            groups.put(groupName.toLowerCase(), group);
            if (group.isDefault() && defaultGroups.get(world) == null)
                defaultGroups.put(groupWorld, group);
        }
        worldGroups.put(world, groups);
    }

    @Override
    public String getGroupPermissionString(String world, String group, String path) {
        Group g = this.getGroupObject(world, group);
        if (g == null)
            return null;
        return g.getRawString(path, "");
    }

    @Override
    public int getGroupPermissionInteger(String world, String group, String path) {
        Group g = this.getGroupObject(world, group);
        if (g == null)
            return 0;
        return g.getRawInt(path, -1);
    }

    @Override
    public boolean getGroupPermissionBoolean(String world, String group, String path) {
        Group g = this.getGroupObject(world, group);
        if (g == null)
            return false;
        return g.getRawBool(path, false);
    }

    @Override
    public double getGroupPermissionDouble(String world, String group, String path) {
        Group g = this.getGroupObject(world, group);
        if (g == null)
            return 0D;
        return g.getRawDouble(path, -1.0d);
    }

    @Override
    public String getUserPermissionString(String world, String name, String path) {
        User u = this.getUserObject(world, name);
        if (u == null)
            return null;
        return u.getRawString(path, "");
    }

    @Override
    public int getUserPermissionInteger(String world, String name, String path) {
        User u = this.getUserObject(world, name);
        if (u == null)
            return 0;
        return u.getRawInt(path, -1);
    }

    @Override
    public boolean getUserPermissionBoolean(String world, String name, String path) {
        User u = this.getUserObject(world, name);
        if (u == null)
            return false;
        return u.getRawBool(path, false);
    }

    @Override
    public double getUserPermissionDouble(String world, String name, String path) {
        User u = this.getUserObject(world, name);
        if (u == null)
            return 0D;
        return u.getRawDouble(path, -1.0d);
    }

    @Override
    public String getPermissionString(String world, String name, String path) {
        User u = this.getUserObject(world, name);
        if (u == null)
            return null;
        return u.getString(path);
    }

    @Override
    public int getPermissionInteger(String world, String name, String path) {
        User u = this.getUserObject(world, name);
        if (u == null)
            return 0;
        return u.getInt(path);
    }

    @Override
    public boolean getPermissionBoolean(String world, String name, String path) {
        User u = this.getUserObject(world, name);
        if (u == null)
            return false;
        return u.getBool(path);
    }

    @Override
    public double getPermissionDouble(String world, String name, String path) {
        User u = this.getUserObject(world, name);
        if (u == null)
            return -1.0d;
        return u.getDouble(path);
    }

    @Override
    public void addGroupInfo(String world, String group, String path, Object data) {
        Group g = this.getGroupObject(world, group);
        if (g == null)
            return;
        g.setData(path, data);
    }

    @Override
    public void removeGroupInfo(String world, String group, String path) {
        Group g = this.getGroupObject(world, group);
        if (g == null)
            return;
        g.removeData(path);
    }

    public String getParentWorldGroup(String world) {
        if (!world.equals("*") && groupStorageMirrorings.get(world) != null)
            return groupStorageMirrorings.get(world);
        return world;
    }

    public String getParentWorldUser(String world) {
        if (!world.equals("*") && userStorageMirrorings.get(world) != null)
            world = userStorageMirrorings.get(world);
        return world;
    }

    @Override
    public Set<String> getWorlds() {
        Set<String> worlds = new HashSet<String>();
        worlds.addAll(this.userStorageMirrorings.keySet());
        worlds.addAll(this.userStores.keySet());
        return worlds;
    }

    @Override
    public boolean userExists(String world, String name) {
        world = getParentWorldUser(world);
        if (worldUsers.get(world) == null)
            return false;
        return worldUsers.get(world).get(name.toLowerCase()) != null;
    }

    @Override
    public boolean groupExists(String world, String name) {
        world = getParentWorldUser(world);
        if (worldGroups.get(world) == null)
            return false;
        return worldGroups.get(world).get(name.toLowerCase()) != null;
    }

    @Override
    public int compareWeights(String world, String first, String second) {
        return compareWeights(world, first, world, second);
    }

    @Override
    public int compareWeights(String firstWorld, String first, String secondWorld, String second) {
        User firstUser = this.getUserObject(firstWorld, first);
        User secondUser = this.getUserObject(secondWorld, second);
        if (firstUser == null) {
            if (secondUser == null)
                return 0;
            else
                return -1;
        }
        if (secondUser == null)
            return 1;
        return Integer.signum(((Integer) firstUser.getWeight()).compareTo(secondUser.getWeight()));
    }
    
    String getWorldParent(String world, boolean user) {
        return user ? userStorageInheritance.containsKey(world) ? userStorageInheritance.get(world) : null : groupStorageInheritance.containsKey(world) ? groupStorageInheritance.get(world) : null;
    }

    @Override
    public String getGroupPermissionString(String world, String group, String path, String def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGroupPermissionString(String world, String group, String path, String def, Comparator<String> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGroupPermissionInteger(String world, String group, String path, int def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGroupPermissionInteger(String world, String group, String path, int def, Comparator<Integer> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGroupPermissionBoolean(String world, String group, String path, boolean def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGroupPermissionBoolean(String world, String group, String path, boolean def, Comparator<Boolean> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGroupPermissionDouble(String world, String group, String path, double def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGroupPermissionDouble(String world, String group, String path, double def, Comparator<Double> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUserPermissionString(String world, String group, String path, String def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUserPermissionString(String world, String group, String path, String def, Comparator<String> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUserPermissionInteger(String world, String group, String path, int def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUserPermissionInteger(String world, String group, String path, int def, Comparator<Integer> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUserPermissionBoolean(String world, String group, String path, boolean def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUserPermissionBoolean(String world, String group, String path, boolean def, Comparator<Boolean> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUserPermissionDouble(String world, String group, String path, double def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUserPermissionDouble(String world, String group, String path, double def, Comparator<Double> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPermissionString(String world, String group, String path, String def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPermissionString(String world, String group, String path, String def, Comparator<String> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPermissionInteger(String world, String group, String path, int def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPermissionInteger(String world, String group, String path, int def, Comparator<Integer> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPermissionBoolean(String world, String group, String path, boolean def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPermissionBoolean(String world, String group, String path, boolean def, Comparator<Boolean> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPermissionDouble(String world, String group, String path, double def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPermissionDouble(String world, String group, String path, double def, Comparator<Double> comparator) {
        // TODO Auto-generated method stub
        return null;
    }
}

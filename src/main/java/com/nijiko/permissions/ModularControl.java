package com.nijiko.permissions;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nijiko.data.GroupWorld;
import com.nijiko.data.IStorage;
import com.nijiko.data.StorageFactory;
public class ModularControl //extends PermissionHandler 
{

    private Map<String, IStorage> WorldStorage = new HashMap<String, IStorage>();
    private Map<String, Map<String, Group>> WorldGroups;
    private Map<String, Map<String, User>> WorldUsers;
    private Map<String, Group> WorldBase = new HashMap<String, Group>();
    private Map<String, String> WorldInheritance = new HashMap<String, String>();
    private final Configuration storageConfig;
    private String defaultWorld = "";
    
    public ModularControl(Configuration storageConfig)
    {
        this.storageConfig = storageConfig;
    }
    //@Override
    public void setDefaultWorld(String world) {
        this.defaultWorld = world;
    }
    //@Override
    public boolean loadWorld(String world) throws Exception {
        if(WorldStorage.get(world.toLowerCase())==null)
        {
            forceLoadWorld(world);
            return true;
        }
        return false;
    }
    //@Override
    public void forceLoadWorld(String world) throws Exception {
        IStorage store = StorageFactory.createInstance(world, storageConfig);
        this.WorldStorage.put(world.toLowerCase(), store);

        // TODO WorldInheritance check
        Map<String, User> users = new HashMap<String, User>();        
        Set<String> userNames = store.getUsers();
        for(String userName : userNames)
        {
            User user = new User(this,store,world,userName);
            users.put(userName.toLowerCase(), user);
        }
        WorldUsers.put(world.toLowerCase(), users);

        HashMap<String, Group> groups = new HashMap<String, Group>();        
        Set<String> groupNames = store.getGroups();
        for(String groupName : groupNames)
        {
            Group group = new Group(this,store,world,groupName);
            groups.put(groupName.toLowerCase(), group);
        }
        WorldGroups.put(world.toLowerCase(), groups);
    }
    //@Override
    public boolean checkWorld(String world) {
        return WorldStorage.containsKey(world.toLowerCase());
    }
    //@Override
    public void load() throws Exception {
        this.loadWorld(defaultWorld);
    }
    //@Override
    public void reload() {
        for(IStorage store : WorldStorage.values())
        {
            store.reload();
        }
    }
    //@Override
    public boolean reload(String world) {
        IStorage store = this.WorldStorage.get(world.toLowerCase());
        if(store==null) return false;
        store.reload();
        return true;
    }
    
    //@Override
    public boolean has(String world, String name, String permission) {
        return permission(world,name,permission);
    }
    public boolean has(Player player, String permission) {
        return permission(player,permission);
    }
    //@Override
    public boolean permission(Player player, String permission) {
        String name = player.getName();
        String worldName = player.getWorld().getName();
        return permission(worldName,name,permission);
    }

    public boolean permission(String world, String name, String permission)
    {
        if(name==null||name.isEmpty()||world==null||world.isEmpty()) return true;
        User user;
        try {
            user = this.safeGetUser(world, name);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }        
        return user.hasPermission(permission);
    }
    
    
    //@Override
    public String getGroupName(String world, String name) {
        // TODO WorldInheritance check
        Map<String,Group> groups = this.WorldGroups.get(world.toLowerCase());
        if(groups == null) return null;
        Group g = groups.get(name.toLowerCase());
        if(g == null) return null;
        return g.getName();
    }
    //@Override
    public Set<Group> getParentGroups(String world, String name) {
        // TODO WorldInheritance check
        try {
            return this.stringToGroups(safeGetUser(world, name).getParents());
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<Group>();
        }
    }
    //@Override
    public boolean inGroup(String world, String name, String groupWorld, String group) {
        // TODO WorldInheritance check
        try {
            return safeGetUser(world, name).inGroup(groupWorld, group);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //@Override
    public boolean inSingleGroup(String world, String name,String groupWorld, String group) {
        // TODO WorldInheritance check
        try {
            return safeGetUser(world, name).getParents().contains(new GroupWorld(groupWorld,group));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //@Override
    public String getGroupPrefix(String world, String group) {
        // TODO WorldInheritance check
        try {
            return safeGetGroup(world, group).getPrefix();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    //@Override
    public String getGroupSuffix(String world, String group) {
        // TODO WorldInheritance check
        try {
            return safeGetGroup(world, group).getSuffix();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    //@Override
    public boolean canGroupBuild(String world, String group) {
        // TODO WorldInheritance check
        try {
            return safeGetGroup(world, group).canBuild();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    //@Override
    public void save(String world) {
        IStorage store = this.WorldStorage.get(world.toLowerCase());
        if(store != null)
        {
            store.save();
        }
    }
    //@Override
    public void saveAll() {
        // TODO Auto-generated method stub
    }
    Set<Group> stringToGroups(Set<GroupWorld> raws)
    {        
        Set<Group> groupSet = new HashSet<Group>();
        for(GroupWorld raw : raws)
        {
            Map<String, Group> gMap = this.WorldGroups.get(raw.getWorld().toLowerCase());
            if(gMap != null)
            {
                Group g = gMap.get(raw.getName().toLowerCase());
                if(g != null) groupSet.add(g);
            }
        }
        return groupSet;
    }
    //@Override
    public void load(String world, Configuration userConfig,
            Configuration groupConfig) {
        // TODO Auto-generated method stub
    }
    public User safeGetUser(String world, String name) throws Exception
    {
        try
        {
            loadWorld(world);
        }
        catch(Exception e)
        {
            throw new Exception("Error creating user " + name + " in world " + world + " due to storage problems!", e);
        }
        if(this.WorldUsers.get(world.toLowerCase()) == null) this.WorldUsers.put(world.toLowerCase(), new HashMap<String, User>());
        if(this.WorldUsers.get(world.toLowerCase()).get(name.toLowerCase()) == null) this.WorldUsers.get(world.toLowerCase()).put(name.toLowerCase(), new User(this, WorldStorage.get(world), name, world));
        return this.WorldUsers.get(world.toLowerCase()).get(name.toLowerCase());
    }
    public Group safeGetGroup(String world, String name) throws Exception
    {
        try
        {
            loadWorld(world);
        }
        catch(Exception e)
        {
            throw new Exception("Error creating group " + name + " in world " + world + " due to storage problems!", e);
        }
        if(this.WorldGroups.get(world.toLowerCase()) == null) this.WorldGroups.put(world, new HashMap<String, Group>());
        if(this.WorldGroups.get(world.toLowerCase()).get(name.toLowerCase()) == null) this.WorldGroups.get(world.toLowerCase()).put(name.toLowerCase(), new Group(this, WorldStorage.get(world), name, world));
        return this.WorldGroups.get(world.toLowerCase()).get(name.toLowerCase());
    }

    Group getDefaultGroup(String world)
    {
        return this.WorldBase.get(world.toLowerCase());
    }

}
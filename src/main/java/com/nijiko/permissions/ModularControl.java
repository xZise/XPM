package com.nijiko.permissions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import com.nijiko.data.IStorage;
import com.nijiko.data.YamlStorage;
public class ModularControl extends PermissionHandler {
    
    private List<String> Worlds = new LinkedList<String>();
    private Map<String, IStorage> WorldStorage = new HashMap<String, IStorage>();
    private Map<String, Map<String, Group>> WorldGroups;
    private Map<String, Map<String, User>> WorldUsers;
    private Map<String, Group> WorldBase;
    private Map<String, String> WorldInheritance = new HashMap<String, String>();
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
        throw new UnsupportedOperationException("No cache is implemented.");
    }
    @Override
    public void setCacheItem(String world, String player, String permission,
            boolean data) {
        throw new UnsupportedOperationException("No cache is implemented.");
    }
    @Override
    public Map<String, Boolean> getCache(String world) {
        throw new UnsupportedOperationException("No cache is implemented.");
    }
    @Override
    public boolean getCacheItem(String world, String player, String permission) {
        throw new UnsupportedOperationException("No cache is implemented.");
    }
    @Override
    public void removeCachedItem(String world, String player, String permission) {
        throw new UnsupportedOperationException("No cache is implemented.");
    }
    @Override
    public void clearCache(String world) {
        throw new UnsupportedOperationException("No cache is implemented.");
    }
    @Override
    public void clearAllCache() {
        throw new UnsupportedOperationException("No cache is implemented.");
    }
    @Override
    public boolean has(Player player, String permission) {
        return permission(player,permission);
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
        // TODO WorldInheritance check
        return safeGetUser(world, name).getParents().toArray(new String[0]);
    }
    @Override
    public boolean inGroup(String world, String name, String group) {
        // TODO WorldInheritance check
        return safeGetUser(world, name).inGroup(group);
    }
    @Override
    public boolean inSingleGroup(String world, String name, String group) {
        // TODO WorldInheritance check
        return safeGetUser(world, name).getParents().contains(group);
    }
    @Override
    public String getGroupPrefix(String world, String group) {
        // TODO WorldInheritance check
        return safeGetGroup(world, group).getPrefix();
    }
    @Override
    public String getGroupSuffix(String world, String group) {
        // TODO WorldInheritance check
        return safeGetGroup(world, group).getSuffix();
    }
    @Override
    public boolean canGroupBuild(String world, String group) {
        // TODO WorldInheritance check
        return safeGetGroup(world, group).canBuild();
    }
    @Override
    public String getGroupPermissionString(String world, String group,
            String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public int getGroupPermissionInteger(String world, String group,
            String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public boolean getGroupPermissionBoolean(String world, String group,
            String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public double getGroupPermissionDouble(String world, String group,
            String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public String getUserPermissionString(String world, String name,
            String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public int getUserPermissionInteger(String world, String name,
            String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public boolean getUserPermissionBoolean(String world, String name,
            String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public double getUserPermissionDouble(String world, String name,
            String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public String getPermissionString(String world, String name,
            String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public int getPermissionInteger(String world, String name, String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public boolean getPermissionBoolean(String world, String name,
            String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public double getPermissionDouble(String world, String name,
            String permission) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public void addGroupInfo(String world, String group, String node,
            Object data) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public void removeGroupInfo(String world, String group, String node) {
        throw new UnsupportedOperationException("No data storage support is implemented.");
    }
    @Override
    public void addUserPermission(String world, String user, String node) {
        safeGetUser(world, user).addPermission(node);
    }
    @Override
    public void removeUserPermission(String world, String user, String node) {
        safeGetUser(world, user).removePermission(node);
    }
    @Override
    public void save(String world) {
        // TODO Auto-generated method stub
    }
    @Override
    public void saveAll() {
        // TODO Auto-generated method stub
    }
    Set<Group> stringToGroups(String world, Set<String> groupNames)
    {
        Map<String, Group> groups = this.WorldGroups.get(world);
        if(groups == null||groups.isEmpty()) return null;
        Set<Group> groupSet = new HashSet<Group>();
        for(String name : groupNames)
        {
            Group g = groups.get(name);
            if(name != null) groupSet.add(g);
        }
        return groupSet;
    }
    @Override
    public void load(String world, Configuration userConfig,
            Configuration groupConfig) {
        // TODO Auto-generated method stub
    }
    private User safeGetUser(String world, String name)
    {
        if(this.WorldUsers.get(world) == null) this.WorldUsers.put(world, new HashMap<String, User>());
        if(this.WorldStorage.get(world) == null) this.WorldStorage.put(world, new YamlStorage()); //TODO: Temp fix
        if(this.WorldUsers.get(world).get(name) == null) this.WorldUsers.get(world).put(name, new User(this, WorldStorage.get(world), name, world));
        return this.WorldUsers.get(world).get(name);
    }
    private Group safeGetGroup(String world, String name)
    {
        if(this.WorldGroups.get(world) == null) this.WorldGroups.put(world, new HashMap<String, Group>());
        if(this.WorldStorage.get(world) == null) this.WorldStorage.put(world, new YamlStorage()); //TODO: Temp fix
        if(this.WorldGroups.get(world).get(name) == null) this.WorldGroups.get(world).put(name, new Group(this, WorldStorage.get(world), name, world));
        return this.WorldGroups.get(world).get(name);
    }
}
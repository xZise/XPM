package com.nijiko.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.nijiko.permissions.EntryType;


public class SqlStorage implements IStorage {

    private static Connection dbConn;
    private static int reloadDelay;
    private static Map<String, SqlStorage> instances;
    private static boolean init = false;
    
    public static void init(String dbmsName, String uri, String username, String password, int reloadDelay) throws Exception
    {
        if(init) return;
        SqlStorage.reloadDelay = reloadDelay;
        Dbms dbms = null;
        try
        {
            dbms = Dbms.valueOf(dbmsName);
        }
        catch(IllegalArgumentException e)
        {
            System.err.println("Error occurred while selecting permissions config DBMS. Reverting to SQLite.");
            dbms = Dbms.SQLITE;
        }
        try {
            Class.forName(dbms.getDriver());
        } catch (ClassNotFoundException e) {
            throw new Exception("Unable to load SQL driver!", e);
        }
        dbConn = DriverManager.getConnection(uri, username, password);
        //TODO: Prepare tables
//        reload(false);
        //TODO: Reload thread
        init = true;
    }
    
    private String world;
    private String baseGroup = null;
    private Set<String> buildGroups = new HashSet<String>();
    private Map<String, String> groupPrefixes = new HashMap<String, String>();
    private Map<String, String> groupSuffixes = new HashMap<String, String>();
    private Map<String, Set<String>> groupPermissions = new HashMap<String, Set<String>>();
    private Map<String, Set<String>> userPermissions = new HashMap<String, Set<String>>();
    private Map<String, Set<GroupWorld>> groupParents = new HashMap<String, Set<GroupWorld>>();
    private Map<String, Set<GroupWorld>> userParents = new HashMap<String, Set<GroupWorld>>();
    

    SqlStorage(String world)
    {
        this.world = world;
        SqlStorage.instances.put(world, this);
    }
    
    @Override
    public boolean isDefault(String name) {
        if(name == null) return false;
        if(name.equalsIgnoreCase(baseGroup)) return true;
        //TODO: SQL Query
        return false;
    }

    @Override
    public boolean canBuild(String name) {
        if(name == null) return false;
        if(buildGroups.contains(name.toLowerCase())) return true;
        //TODO: SQL Query
        return false;
    }

    @Override
    public String getPrefix(String name) {
        if(name == null) return "";
        String prefix = groupPrefixes.get(name.toLowerCase());
        if(prefix != null) return prefix;
        //TODO: SQL Query
        return prefix;
    }

    @Override
    public String getSuffix(String name) {
        if(name == null) return "";
        String suffix = groupSuffixes.get(name.toLowerCase());
        if(suffix != null) return suffix;
        //TODO: SQL Query
        return suffix;
    }

    @Override
    public Set<String> getPermissions(String name, EntryType type) {
        if(name == null) return new HashSet<String>();
        Map<String, Set<String>> permissionsets = type == EntryType.GROUP ? groupPermissions : userPermissions;
        if(permissionsets != null)
        {
            Set<String> permissions = permissionsets.get(name.toLowerCase());
            if(permissions != null) return permissions;
        }
        //TODO SQL Query
        return null;
    }

    @Override
    public Set<GroupWorld> getParents(String name, EntryType type) {
        if(name == null) return new HashSet<GroupWorld>();
        Map<String, Set<GroupWorld>> parentsets = type == EntryType.GROUP ? groupParents : userParents;
        if(parentsets != null)
        {
            Set<GroupWorld> parents = parentsets.get(name.toLowerCase());
            if(parents != null) return parents;
        }
        //TODO SQL Query
        return null;
    }

    @Override
    public void setBuild(String name, boolean build) {
        if(name == null) return;
        if(build) buildGroups.add(name.toLowerCase());
        else buildGroups.remove(name.toLowerCase());
        //TODO SQL Updates
    }

    @Override
    public void setPrefix(String name, String prefix) {
        if(name == null) return;
        groupPrefixes.put(name.toLowerCase(), prefix);
        //TODO SQL Updates
    }

    @Override
    public void setSuffix(String name, String suffix) {
        if(name == null) return;
        groupSuffixes.put(name.toLowerCase(), suffix);
        //TODO SQL Updates
    }

    @Override
    public void addPermission(String name, EntryType type,
            String permission) {
        Map<String, Set<String>> permMap = (type == EntryType.GROUP ? this.groupPermissions : this.userPermissions);
        if(permMap.get(name.toLowerCase()) == null) permMap.put(name.toLowerCase(), new HashSet<String>());
        Set<String> perms = permMap.get(name.toLowerCase());
        perms.add(permission);
        //TODO SQL Updates
    }

    @Override
    public void removePermission(String name, EntryType type,
            String permission) {
        Map<String, Set<String>> permMap = (type == EntryType.GROUP ? this.groupPermissions : this.userPermissions);
        if(permMap.get(name.toLowerCase()) == null) permMap.put(name.toLowerCase(), new HashSet<String>());
        Set<String> perms = permMap.get(name.toLowerCase());
        perms.remove(permission);
        //TODO SQL Updates

    }

    @Override
    public void reload(final boolean applyChanges) {
        baseGroup = null;
        buildGroups.clear();
        groupPrefixes.clear();
        groupSuffixes.clear();
        groupPermissions.clear();
        userPermissions.clear();
        groupParents.clear();
        userParents.clear();
    }
    @Override
    public void save() {
        return;
    }
    @Override
    public void addParent(String name, String groupWorld,
            String groupName, EntryType type) {
        Map<String, Set<GroupWorld>> permMap = (type == EntryType.GROUP ? this.groupParents : this.userParents);
        if(permMap.get(name.toLowerCase()) == null) permMap.put(name.toLowerCase(), new HashSet<GroupWorld>());
        Set<GroupWorld> perms = permMap.get(name.toLowerCase());
        perms.add(new GroupWorld(groupWorld, groupName));
        //TODO SQL Updates

    }
    @Override
    public void removeParent(String name, String groupWorld,
            String groupName, EntryType type) {
        Map<String, Set<GroupWorld>> permMap = (type == EntryType.GROUP ? this.groupParents : this.userParents);
        if(permMap.get(name.toLowerCase()) == null) permMap.put(name.toLowerCase(), new HashSet<GroupWorld>());
        Set<GroupWorld> perms = permMap.get(name.toLowerCase());
        perms.remove(new GroupWorld(groupWorld, groupName));
        //TODO SQL Updates
    }

    @Override
    public void finalize()
    {
        try {
            dbConn.close();
        } catch (SQLException e) {
            System.err.println("Disconnecting from database failed.");
            e.printStackTrace();
        }

    }
    
    @SuppressWarnings("unused")
    private static void refresh() //Used for periodic cache flush
    {
        for(SqlStorage instance : instances.values())
        {
            instance.reload(false);
        }
    }
}

enum Dbms
{
    SQLITE("org.sqlite.JDBC"),
    MYSQL("com.mysql.jdbc.driver");

    private final String driver;

    Dbms(String driverClass)
    {
        this.driver = driverClass;
    }

    public String getDriver()
    {
        return driver;
    }
}


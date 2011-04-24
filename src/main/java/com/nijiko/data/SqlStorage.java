package com.nijiko.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.sqlite.SQLiteDataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.nijiko.permissions.EntryType;
import com.nijikokun.bukkit.Permissions.Permissions;


public class SqlStorage {

    private static DataSource dbSource;
    private static int reloadDelay;
//    private static Map<String, SqlStorage> instances;
    private static boolean init = false;
    private static List<String> create = new ArrayList<String>(8);
    static
    {
        create.add(
                "CREATE TABLE IF NOT EXISTS Worlds (" +
                " worldid INT NOT NULL PRIMARY KEY," +
                " worldname VARCHAR(32) NOT NULL," +
                " CONSTRAINT WorldNoSelfInherit CHECK (worldid IS NOT = parentid)," +
                ")");        
        create.add("CREATE TABLE IF NOT EXISTS Users (" +
                " uid INT NOT NULL PRIMARY KEY," +
                " username VARCHAR(32) NOT NULL," +
                " worldid INT NOT NULL FORIEGN KEY REFERENCES Worlds(worldid)," +
                " CONSTRAINT UserNameWorld UNIQUE (username, World)," +
                " INDEX(username)"+
                ")");
        create.add("CREATE TABLE IF NOT EXISTS Groups (" +
                " gid INT NOT NULL PRIMARY KEY," +
                " groupname VARCHAR(32) NOT NULL," +
                " worldid  INT NOT NULL FORIEGN KEY REFERENCES Worlds(worldid)," +
                " prefix VARCHAR(32) NOT NULL," +
                " suffix VARCHAR(32) NOT NULL, " +
                " build TINYINT NOT NULL DEFAULT 0" +
                " CONSTRAINT GroupNameWorld UNIQUE (groupname, World)," +
                ")");        
        create.add("CREATE TABLE IF NOT EXISTS UserPermission (" +
                " upermid INT NOT NULL PRIMARY KEY," +
                " permstring VARCHAR(64) NOT NULL," +
                " uid int NOT NULL FOREIGN KEY REFERENCES Users(uid)" +
                ")");
        create.add("CREATE TABLE IF NOT EXISTS GroupPermission (" +
                " gpermid INT NOT NULL PRIMARY KEY," +
                " permstring VARCHAR(64) NOT NULL," +
                " gid int NOT NULL FOREIGN KEY REFERENCES Groups(gid)" +
                ")");
        create.add("CREATE TABLE IF NOT EXISTS UserInheritance (" +
                " uinheritid INT NOT NULL PRIMARY KEY," +
                " childid INT NOT NULL FOREIGN KEY REFERENCES Users(uid)," +
                " parentid int NOT NULL FOREIGN KEY REFERENCES Groups(gid)" +
                ");");
        create.add("CREATE TABLE IF NOT EXISTS GroupInheritance (" +
                " ginheritid INT NOT NULL PRIMARY KEY," +
                " childid INT NOT NULL FOREIGN KEY REFERENCES Groups(gid)," +
                " parentid int NOT NULL FOREIGN KEY REFERENCES Groups(gid)," +
                " CONSTRAINT GroupNoSelfInherit CHECK (childid IS NOT = parentid)" +
                ")");
        create.add("CREATE TABLE IF NOT EXISTS WorldBase (" +
                " worldid INT NOT NULL FOREIGN KEY REFERENCES Worlds(worldid)," +
                " defaultid INT NOT NULL FOREIGN KEY REFERENCES Groups(gid)," +
                ")");
    }
    
    public static void init(String dbmsName, String uri, String username, String password, int reloadDelay) throws Exception
    {
        if(init) return;
//        SqlStorage.reloadDelay = reloadDelay;
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
        dbSource = dbms.getSource(username, password, uri);
        Connection dbConn = dbSource.getConnection();
        verifyAndCreateTables(dbms);
        Permissions.instance.getServer().getScheduler().scheduleAsyncRepeatingTask(Permissions.instance, new Runnable(){
            @Override
            public void run() {
                refresh();                
            }
            
        } , reloadDelay, reloadDelay);
        
        init = true;    
    }
    
    private String userWorld;
    private String groupWorld;
    private String baseGroup = null;
    private Set<String> buildGroups = new HashSet<String>();
    private Map<String, String> groupPrefixes = new HashMap<String, String>();
    private Map<String, String> groupSuffixes = new HashMap<String, String>();
    private Map<String, Set<String>> groupPermissions = new HashMap<String, Set<String>>();
    private Map<String, Set<String>> userPermissions = new HashMap<String, Set<String>>();
    private Map<String, Set<GroupWorld>> groupParents = new HashMap<String, Set<GroupWorld>>();
    private Map<String, Set<GroupWorld>> userParents = new HashMap<String, Set<GroupWorld>>();
    

    SqlStorage(String userWorld, String groupWorld)
    {
        this.userWorld = userWorld;
        this.groupWorld = groupWorld;
//        SqlStorage.instances.put(world.toLowerCase(), this);
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
    public void reload() {
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
    
    private static void refresh() //Used for periodic cache flush
    {
        for(SqlStorage instance : instances.values())
        {
            instance.reload();
        }
    }
    
    private static void verifyAndCreateTables(Dbms dbms) throws SQLException
    {        
        Connection dbConn = SqlStorage.dbSource.getConnection();
        Statement s = dbConn.createStatement();
        //Verify stuff

        String engine = dbms.equals(Dbms.MYSQL) ? " ENGINE = InnoDB;" : ";";
        for(String state : create)
        {
            s.executeUpdate(state + engine);
        }
    }

    @Override
    public Set<String> getUsers() {
        // TODO Auto-generated method stub
        return null;
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
    public boolean isAutoSave() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setAutoSave(boolean autoSave) {
        // TODO Auto-generated method stub
        
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
    
    public DataSource getSource(String username, String password, String url)
    {
        switch(this)
        {
        case MYSQL:
            MysqlDataSource mds = new MysqlDataSource();
            mds.setUser(username);
            mds.setPassword(password);
            mds.setUrl(url);
            return mds;
        default:
        case SQLITE:
            SQLiteDataSource sds = new SQLiteDataSource();
            sds.setUrl(url);
            return sds;
        }
    }
}


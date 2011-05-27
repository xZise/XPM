package com.nijiko.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import org.sqlite.SQLiteDataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.nijiko.data.PreparedStatementPool.PreparedStatementWrapper;

public abstract class SqlStorage {

    private static final int max = 10;
    private static Dbms dbms;
    private static DataSource dbSource;
    private static boolean init = false;
    private static Map<String, SqlUserStorage> userStores = new HashMap<String, SqlUserStorage>();
    private static Map<String, SqlGroupStorage> groupStores = new HashMap<String, SqlGroupStorage>();
    private static Map<String, Integer> worldMap = new HashMap<String, Integer>();
    private static List<String> create = new ArrayList<String>(12);
    static final String getWorld = "SELECT PrWorlds.worldid FROM PrWorlds WHERE PrWorlds.worldname = ?;";
    private static PreparedStatementPool getWorldPool;
    static final String getUser = "SELECT uid FROM PrUsers WHERE PrUsers.worldid = ? AND PrUsers.username = ?;";
    private static PreparedStatementPool getUserPool;
    static final String getGroup = "SELECT gid FROM PrGroups WHERE PrGroups.worldid = ? AND PrGroups.groupname = ?;";
    private static PreparedStatementPool getGroupPool;
    static final String createWorld = "INSERT IGNORE INTO PrWorlds (worldname) VALUES (?);";
    private static PreparedStatementPool createWorldPool;
    static final String createUser = "INSERT IGNORE INTO PrUsers (worldid,username) VALUES (?,?);";
    private static PreparedStatementPool createUserPool;
    static final String createGroup = "INSERT IGNORE INTO PrGroups (worldid, groupname, build, weight) VALUES (?,?,0,0);";
    private static PreparedStatementPool createGroupPool;
    static final String getWorldName = "SELECT worldname FROM PrWorlds WHERE worldid = ?;";
    private static PreparedStatementPool getWorldNamePool;
    static final String getUserName = "SELECT username, worldid FROM PrUsers WHERE uid = ?;";
    private static PreparedStatementPool getUserNamePool;
    static final String getGroupName = "SELECT groupname, worldid FROM PrGroups WHERE gid = ?;";
    private static PreparedStatementPool getGroupNamePool;
    private static Connection dbConn;

    static {
        create.add("CREATE TABLE IF NOT EXISTS PrWorlds (" + " worldid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + " worldname VARCHAR(32) NOT NULL UNIQUE" + ")");
        create.add("CREATE TABLE IF NOT EXISTS PrUsers (" + " uid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + " username VARCHAR(32) NOT NULL," + " worldid INTEGER NOT NULL," + " CONSTRAINT UserNameWorld UNIQUE (username, worldid)," + " USERINDEX" + " FOREIGN KEY(worldid) REFERENCES PrWorlds(worldid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS PrGroups (" + " gid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + " groupname VARCHAR(32) NOT NULL," + " worldid  INTEGER NOT NULL," + " prefix VARCHAR(32)," + " suffix VARCHAR(32), " + " build TINYINT NOT NULL DEFAULT 0," + " weight INTEGER NOT NULL DEFAULT 0," + " CONSTRAINT GroupNameWorld UNIQUE (groupname, worldid)," + " FOREIGN KEY(worldid) REFERENCES PrWorlds(worldid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS PrUserPermissions (" + " upermid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + " permstring VARCHAR(64) NOT NULL," + " uid INTEGER NOT NULL," + " CONSTRAINT UserPerm UNIQUE (uid, permstring)," + " FOREIGN KEY(uid) REFERENCES PrUsers(uid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS PrGroupPermissions (" + " gpermid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + " permstring VARCHAR(64) NOT NULL," + " gid INTEGER NOT NULL," + " CONSTRAINT GroupPerm UNIQUE (gid, permstring)," + " FOREIGN KEY(gid) REFERENCES PrGroups(gid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS PrUserInheritance (" + " uinheritid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + " childid INTEGER NOT NULL," + " parentid INTEGER NOT NULL," + " CONSTRAINT UserParent UNIQUE (childid, parentid)," + " FOREIGN KEY(childid) REFERENCES PrUsers(uid)," + " FOREIGN KEY(parentid) REFERENCES PrGroups(gid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS PrGroupInheritance (" + " ginheritid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + " childid INTEGER NOT NULL," + " parentid INTEGER NOT NULL," + " CONSTRAINT UserParent UNIQUE (childid, parentid)," + " CONSTRAINT GroupNoSelfInherit CHECK (childid <> parentid)," + " FOREIGN KEY(childid) REFERENCES PrGroups(gid)," + " FOREIGN KEY(parentid) REFERENCES PrGroups(gid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS PrWorldBase (" + " worldid INTEGER NOT NULL," + " defaultid INTEGER," + "FOREIGN KEY(worldid) REFERENCES PrWorlds(worldid)," + "FOREIGN KEY(defaultid) REFERENCES PrGroups(gid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS PrUserData (" + " dataid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + " uid INTEGER NOT NULL ," + " path VARCHAR(64) NOT NULL," + " data VARCHAR(64) NOT NULL," + " CONSTRAINT UserDataUnique UNIQUE (uid, path)," + "FOREIGN KEY(uid) REFERENCES PrUsers(uid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS PrGroupData (" + " dataid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + " gid INTEGER NOT NULL," + " path VARCHAR(64) NOT NULL," + " data VARCHAR(64) NOT NULL," + " CONSTRAINT GroupDataUnique UNIQUE (gid, path)," + "FOREIGN KEY(gid) REFERENCES PrGroups(gid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS PrTracks (" + " trackid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + " trackname VARCHAR(64) NOT NULL UNIQUE," + "worldid INTEGER NOT NULL," + "FOREIGN KEY(worldid) REFERENCES PrWorlds(worldid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS PrTrackGroups (" + " trackgroupid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + " trackid INTEGER NOT NULL," + " gid INTEGER NOT NULL," + " groupOrder INTEGER NOT NULL," + " CONSTRAINT TrackGroupsUnique UNIQUE (trackid, gid)," + "FOREIGN KEY(trackid) REFERENCES PrTracks(trackid)," + "FOREIGN KEY(gid) REFERENCES PrGroups(gid)" + ")");
    }

    static Dbms getDbms() {
        return dbms;
    }
    public static void init(String dbmsName, String uri, String username, String password, int reloadDelay) throws Exception {
        if (init) {
            return;
        }
        
        System.out.println("[Permissions] Initializing Permissions 3 SQL interface.");
        // SqlStorage.reloadDelay = reloadDelay;
        try {
            dbms = Dbms.valueOf(dbmsName);
        } catch (IllegalArgumentException e) {
            System.err.println("Error occurred while selecting permissions config DBMS. Reverting to SQLite.");
            dbms = Dbms.SQLITE;
        }
        try {
            Class.forName(dbms.getDriver());
        } catch (ClassNotFoundException e) {
            throw new Exception("Unable to load SQL driver!", e);
        }
        dbSource = dbms.getSource(username, password, uri);
        verifyAndCreateTables();
        dbConn = dbSource.getConnection();
        getWorldPool = new PreparedStatementPool(dbConn, getWorld, max);
        getUserPool = new PreparedStatementPool(dbConn, getUser, max);
        getGroupPool = new PreparedStatementPool(dbConn, getGroup, max);
        createWorldPool = new PreparedStatementPool(dbConn, (dbms==Dbms.SQLITE ? createWorld.replace("IGNORE", "OR IGNORE") : createWorld), max);
        createUserPool = new PreparedStatementPool(dbConn, (dbms==Dbms.SQLITE ? createUser.replace("IGNORE", "OR IGNORE") : createUser), max);
        createGroupPool = new PreparedStatementPool(dbConn, (dbms==Dbms.SQLITE ? createGroup.replace("IGNORE", "OR IGNORE") : createGroup), max);
        getWorldNamePool = new PreparedStatementPool(dbConn, getWorldName, max);
        getUserNamePool = new PreparedStatementPool(dbConn, getUserName, max);
        getGroupNamePool = new PreparedStatementPool(dbConn, getGroupName, max);
        SqlGroupStorage.reloadPools(dbConn);
        SqlUserStorage.reloadPools(dbConn);
        init = true;
        clearWorldCache();
    }

    public synchronized static void clearWorldCache() // Used for periodic cache flush
    {
        if(init)
            worldMap.clear();
    }

    private static void verifyAndCreateTables() throws SQLException {
        Connection dbConn = SqlStorage.dbSource.getConnection();
        Statement s = dbConn.createStatement();
        // Verify stuff
        String engine = dbms.equals(Dbms.MYSQL) ? " ENGINE = InnoDB;" : ";";
        for (String state : create) {
            if (dbms == Dbms.MYSQL) {
                state = state.replace("AUTOINCREMENT", "AUTO_INCREMENT");
                state = state.replace(" USERINDEX", " INDEX pr_username_index(username),");
            } else {
                state = state.replace(" USERINDEX", "");
            }
            s.executeUpdate(state + engine);
        }
        if (dbms != Dbms.MYSQL) {
            s.executeUpdate("CREATE INDEX IF NOT EXISTS pr_username_index ON PrUsers(username);");
        }
    }

    static DataSource getSource() {
        return dbSource;
    }

    static int getWorld(String name) throws SQLException {
        if (worldMap.containsKey(name)) {
            return worldMap.get(name);
        }
        PreparedStatementWrapper getWorldWrap = getWorldPool.getStatement();
        PreparedStatement getWorldStmt = getWorldWrap.getStatement();
        getWorldStmt.clearParameters();
        getWorldStmt.setString(1, name);
        ResultSet rs = getWorldStmt.executeQuery();
        if (!rs.next()) {
            System.out.println("[Permissions] Creating world '" + name + "'.");
            PreparedStatementWrapper createWorldWrap = createWorldPool.getStatement();
            PreparedStatement createWorldStmt = createWorldWrap.getStatement();
            createWorldStmt.setString(1, name);
            createWorldStmt.executeUpdate();
            createWorldWrap.close();
            rs = getWorldStmt.executeQuery();
            rs.next();
        }
        int id = rs.getInt(1);
        worldMap.put(name, id);
        rs.close();
        getWorldWrap.close();
        return id;
    }

    static int getUser(String world, String name) throws SQLException {
        SqlUserStorage sus = userStores.get(world);
        if (sus != null) {
            Integer id = sus.getUserId(name);
            if (id != null) {
                return id;
            }
        }
        int worldid = getWorld(world);
        PreparedStatementWrapper getUserWrap = getUserPool.getStatement();
        PreparedStatement getUserStmt = getUserWrap.getStatement();
        getUserStmt.clearParameters();
        getUserStmt.setInt(1, worldid);
        getUserStmt.setString(2, name);
        ResultSet rs = getUserStmt.executeQuery();
        if (!rs.next()) {
            System.out.println("[Permissions] Creating user '" + name + "' in world '" + world + "'.");
            PreparedStatementWrapper createUserWrap = createUserPool.getStatement();
            PreparedStatement createUserStmt = createUserWrap.getStatement();
            createUserStmt.setInt(1, worldid);
            createUserStmt.setString(2, name);
            createUserStmt.executeUpdate();
            createUserWrap.close();
            rs = getUserStmt.executeQuery();
            rs.next();
        }
        int id = rs.getInt(1);
        rs.close();
        getUserWrap.close();
        return id;

    }

    static int getGroup(String world, String name) throws SQLException {
        SqlGroupStorage sgs = groupStores.get(world);
        if (sgs != null) {
            Integer id = sgs.getGroupId(name);
            if (id != null) {
                return id;
            }
        }
        int worldid = getWorld(world);
        PreparedStatementWrapper getGroupWrap = getGroupPool.getStatement();
        PreparedStatement getGroupStmt = getGroupWrap.getStatement();
        getGroupStmt.clearParameters();
        getGroupStmt.setInt(1, worldid);
        getGroupStmt.setString(2, name);
        ResultSet rs = getGroupStmt.executeQuery();
        if (!rs.next()) {
            System.out.println("[Permissions] Creating group '" + name + "' in world '" + world + "'.");
            PreparedStatementWrapper createGroupWrap = createGroupPool.getStatement();
            PreparedStatement createGroupStmt = createGroupWrap.getStatement();
            createGroupStmt.setInt(1, worldid);
            createGroupStmt.setString(2, name);
            createGroupStmt.executeUpdate();
            createGroupWrap.close();
            rs = getGroupStmt.executeQuery();
            rs.next();
        }
        int id = rs.getInt(1);
        rs.close();
        getGroupWrap.close();
        return id;
    }

    static String getWorldName(int id) throws SQLException {
        PreparedStatementWrapper getWorldNameWrap = getWorldNamePool.getStatement();
        PreparedStatement getWorldNameStmt = getWorldNameWrap.getStatement();
        getWorldNameStmt.clearParameters();
        getWorldNameStmt.setInt(1, id);
        ResultSet rs = getWorldNameStmt.executeQuery();
        if(!rs.next()) {
            return "Error";
        }
        String name = rs.getString(1);
        worldMap.put(name, id);
        rs.close();
        getWorldNameWrap.close();
        return name;
    }

    static NameWorldId getUserName(int uid) throws SQLException {
        PreparedStatementWrapper getUserNameWrap = getUserNamePool.getStatement();
        PreparedStatement getUserNameStmt = getUserNameWrap.getStatement();
        getUserNameStmt.clearParameters();
        getUserNameStmt.setInt(1, uid);
        ResultSet rs = getUserNameStmt.executeQuery();
        NameWorldId nw = new NameWorldId();
        if(!rs.next()) {
            nw.name = "Error";
            nw.worldid = -1;
            return nw;
        }
        String name = rs.getString(1);
        int worldid = rs.getInt(2);
        nw.name = name;
        nw.worldid = worldid;
        rs.close();
        getUserNameWrap.close();
        return nw;
    }
    static NameWorldId getGroupName(int gid) throws SQLException {
        PreparedStatementWrapper getGroupNameWrap = getGroupNamePool.getStatement();
        PreparedStatement getGroupNameStmt = getGroupNameWrap.getStatement();
        getGroupNameStmt.clearParameters();
        getGroupNameStmt.setInt(1, gid);
        ResultSet rs = getGroupNameStmt.executeQuery();
        NameWorldId nw = new NameWorldId();
        if(!rs.next()) {
            nw.name = "Error";
            nw.worldid = -1;
            return nw;
        }
        String name = rs.getString(1);
        int worldid = rs.getInt(2);
        nw.name = name;
        nw.worldid = worldid;
        rs.close();
        getGroupNameWrap.close();
        return nw;
    }
    static SqlUserStorage getUserStorage(String world) throws SQLException {
        if (userStores.containsKey(world)) {
            return userStores.get(world);
        }
        SqlUserStorage sus = new SqlUserStorage(world, getWorld(world));
        userStores.put(sus.getWorld(), sus);
        return sus;
    }

    static SqlGroupStorage getGroupStorage(String world) throws SQLException {
        if (groupStores.containsKey(world)) {
            return groupStores.get(world);
        }
        SqlGroupStorage sgs = new SqlGroupStorage(world, getWorld(world));
        groupStores.put(sgs.getWorld(), sgs);
        return sgs;
    }

    public synchronized static void closeAll() {
        try {
            if(init){
                SqlUserStorage.close();
                SqlGroupStorage.close();
                dbConn.close();
                dbSource = null;
                init = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static Connection getConnection() throws SQLException {
        return dbSource.getConnection();
    }
    public static class NameWorldId {
        public int worldid;
        public String name;
    }
}

enum Dbms {

    SQLITE("org.sqlite.JDBC"), MYSQL("com.mysql.jdbc.Driver");
    private final String driver;

    Dbms(String driverClass) {
        this.driver = driverClass;
    }

    public String getDriver() {
        return driver;
    }

    public DataSource getSource(String username, String password, String url) {
        switch (this) {
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

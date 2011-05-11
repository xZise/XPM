package com.nijiko.data;

import java.sql.Connection;
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
import com.nijikokun.bukkit.Permissions.Permissions;

public abstract class SqlStorage {
    private static DataSource dbSource;
    private static int reloadId;
    private static boolean init = false;
    private static Map<String, SqlUserStorage> userStores = new HashMap<String, SqlUserStorage>();
    private static Map<String, SqlGroupStorage> groupStores = new HashMap<String, SqlGroupStorage>();
    private static Map<String, Integer> worldMap = new HashMap<String, Integer>();
    private static List<String> create = new ArrayList<String>(8);

    static final String getWorld = "SELECT worldid FROM Worlds WHERE worldname = ?;";
    static final String getUser = "SELECT * FROM Users WHERE Users.worldid = ? AND Users.username = ?;";
    static final String getGroup = "SELECT * FROM Groups WHERE Groups.worldid = ? AND Groups.groupname = ?;";

    static final String createWorld = "INSERT INTO Worlds (worldname) VALUES (?);";
    static final String createUser = "INSERT INTO Users (worldid,username) VALUES (?,?);";
    static final String createGroup = "INSERT INTO Groups (worldid, groupname, prefix, suffix, previd, nextid) VALUES (?,?, '','', NULL, NULL);";

    static {
        create.add("CREATE TABLE IF NOT EXISTS Worlds (" + " worldid INT NOT NULL PRIMARY KEY AUTO_INCREMENT," + " worldname VARCHAR(32) NOT NULL UNIQUE" + ");");
        create.add("CREATE TABLE IF NOT EXISTS Users (" + " uid INT NOT NULL PRIMARY KEY AUTO_INCREMENT," + " username VARCHAR(32) NOT NULL," + " worldid INT NOT NULL FORIEGN KEY REFERENCES Worlds(worldid)," + " CONSTRAINT UserNameWorld UNIQUE (username, World)," + " INDEX(username)" + ");");
        create.add("CREATE TABLE IF NOT EXISTS Groups (" + " gid INT NOT NULL PRIMARY KEY AUTO_INCREMENT," + " groupname VARCHAR(32) NOT NULL," + " worldid  INT NOT NULL FORIEGN KEY REFERENCES Worlds(worldid)," + " prefix VARCHAR(32) NOT NULL," + " suffix VARCHAR(32) NOT NULL, " + " build TINYINT NOT NULL DEFAULT 0" + " weight INT NOT NULL DEFAULT 0" + " previd INT FOREIGN KEY REFERENCES Groups(gid)" + " nextid INT FOREIGN KEY REFERENCES Groups(gid)" + " CONSTRAINT GroupNameWorld UNIQUE (groupname, World)" + ");");
        create.add("CREATE TABLE IF NOT EXISTS UserPermissions (" + " upermid INT NOT NULL PRIMARY KEY AUTO_INCREMENT," + " permstring VARCHAR(64) NOT NULL," + " uid int NOT NULL FOREIGN KEY REFERENCES Users(uid)" + " CONSTRAINT UserPerm UNIQUE (uid, permstring)" + ");");
        create.add("CREATE TABLE IF NOT EXISTS GroupPermissions (" + " gpermid INT NOT NULL PRIMARY KEY AUTO_INCREMENT," + " permstring VARCHAR(64) NOT NULL," + " gid int NOT NULL FOREIGN KEY REFERENCES Groups(gid)" + " CONSTRAINT UserPerm UNIQUE (gid, permstring)" + ");");
        create.add("CREATE TABLE IF NOT EXISTS UserInheritance (" + " uinheritid INT NOT NULL PRIMARY KEY AUTO_INCREMENT," + " childid INT NOT NULL FOREIGN KEY REFERENCES Users(uid)," + " parentid int NOT NULL FOREIGN KEY REFERENCES Groups(gid)" + " CONSTRAINT UserParent UNIQUE (childid, parentid)" + ");");
        create.add("CREATE TABLE IF NOT EXISTS GroupInheritance (" + " ginheritid INT NOT NULL PRIMARY KEY," + " childid INT NOT NULL FOREIGN KEY REFERENCES Groups(gid)," + " parentid int NOT NULL FOREIGN KEY REFERENCES Groups(gid)," + " CONSTRAINT UserParent UNIQUE (childid, parentid)," + " CONSTRAINT GroupNoSelfInherit CHECK (childid IS NOT = parentid)" + ");");
        create.add("CREATE TABLE IF NOT EXISTS WorldBase (" + " worldid INT NOT NULL FOREIGN KEY REFERENCES Worlds(worldid)," + " defaultid INT NOT NULL FOREIGN KEY REFERENCES Groups(gid)" + ");");
        create.add("CREATE TABLE IF NOT EXISTS UserData (" + " dataid INT NOT NULL PRIMARY KEY AUTO_INCREMENT," + " uid INT NOT NULL FOREIGN KEY REFERENCES Users(uid)," + " path VARCHAR(64) NOT NULL," + " data VARCHAR(64) NOT NULL" + " CONSTRAINT UserDataUnique UNIQUE (uid, path)" + ");");
        create.add("CREATE TABLE IF NOT EXISTS GroupData (" + " dataid INT NOT NULL PRIMARY KEY AUTO_INCREMENT," + " gid INT NOT NULL FOREIGN KEY REFERENCES Groups(gid)," + " path VARCHAR(64) NOT NULL," + " data VARCHAR(64) NOT NULL" + " CONSTRAINT GroupDataUnique UNIQUE (gid, path)" + ");");
        create.add("CREATE TABLE IF NOT EXISTS Tracks (" + " trackid INT NOT NULL PRIMARY KEY AUTO_INCREMENT," + " trackname VARCHAR(64) NOT NULL UNIQUE" + ");");
        create.add("CREATE TABLE IF NOT EXISTS TrackGroups (" + " trackgroupid INT NOT NULL PRIMARY KEY AUTO_INCREMENT," + " trackid INT NOT NULL FORIEGN KEY REFERENCES Tracks(trackid)," + " gid INT NOT NULL FOREIGN KEY REFERENCES Groups(gid)" + " CONSTRAINT TrackGroupsUnique UNIQUE (trackid, group)" + ");");
    }

    public static void init(String dbmsName, String uri, String username, String password, int reloadDelay) throws Exception {
        if (init)
            return;
        // SqlStorage.reloadDelay = reloadDelay;
        Dbms dbms = null;
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
        verifyAndCreateTables(dbms);
        reloadId = Permissions.instance.getServer().getScheduler().scheduleAsyncRepeatingTask(Permissions.instance, new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, reloadDelay, reloadDelay);
        init = true;
        refresh();
    }

    private synchronized static void refresh() // Used for periodic cache flush
    {
        worldMap.clear();
        for (SqlUserStorage instance : userStores.values()) {
            instance.reload();
        }
        for (SqlGroupStorage instance : groupStores.values()) {
            instance.reload();
        }
    }

    private static void verifyAndCreateTables(Dbms dbms) throws SQLException {
        Connection dbConn = SqlStorage.dbSource.getConnection();
        Statement s = dbConn.createStatement();
        // Verify stuff
        String engine = dbms.equals(Dbms.MYSQL) ? " ENGINE = InnoDB;" : ";";
        for (String state : create) {
            s.executeUpdate(state + engine);
        }
    }

    static DataSource getSource() {
        return dbSource;
    }

    static int getWorld(String name) throws SQLException {
        if (worldMap.containsKey(name))
            return worldMap.get(name);
        Connection dbConn = getConnection();
        Statement stmt = dbConn.createStatement();
        String query = getWorld.replace("?", name);
        if (!stmt.execute(query)) {
            String addQuery = createWorld.replace("?", name);
            stmt.executeUpdate(addQuery);
            stmt.execute(query);
        }
        ResultSet rs = stmt.getResultSet();
        rs.next();
        int id = rs.getInt(1);
        worldMap.put(name, id);
        rs.close();
        stmt.close();
        dbConn.close();
        return id;

    }

    static int getUser(String world, String name) throws SQLException {
        SqlUserStorage sus = userStores.get(world);
        if (sus != null) {
            Integer id = sus.getUserId(name);
            if (id != null)
                return id;
        }
        Connection dbConn = getConnection();
        Statement stmt = dbConn.createStatement();
        int worldId = getWorld(world);
        String query = getUser.replaceFirst("\\?", String.valueOf(worldId)).replaceFirst("\\?", name);
        if (!stmt.execute(query))
        {
            String addQuery = createUser.replaceFirst("\\?", String.valueOf(worldId)).replaceFirst("\\?", name);
            stmt.executeUpdate(addQuery);
            stmt.execute(query);
        }
        ResultSet rs = stmt.getResultSet();
        rs.next();
        int id = rs.getInt(1);
        rs.close();
        stmt.close();
        dbConn.close();
        return id;

    }

    static int getGroup(String world, String name) throws SQLException {
        SqlGroupStorage sgs = groupStores.get(world);
        if (sgs != null) {
            Integer id = sgs.getGroupId(name);
            if (id != null)
                return id;
        }
        Connection dbConn = getConnection();
        Statement stmt = dbConn.createStatement();
        int worldId = getWorld(world);
        String query = getGroup.replaceFirst("\\?", String.valueOf(worldId)).replaceFirst("\\?", name);
        if (!stmt.execute(query))
        {
            String addQuery = createGroup.replaceFirst("\\?", String.valueOf(worldId)).replaceFirst("\\?", name);
            stmt.executeUpdate(addQuery);
            stmt.execute(query);
        }
        ResultSet rs = stmt.getResultSet();
        rs.next();
        int id = rs.getInt(1);
        rs.close();
        stmt.close();
        dbConn.close();
        return id;
    }

    static SqlUserStorage getUserStorage(String world) throws SQLException {
        if (userStores.containsKey(world))
            return userStores.get(world);
        SqlUserStorage sus = new SqlUserStorage(world, getWorld(world));
        userStores.put(sus.getWorld(), sus);
        return sus;
    }

    static SqlGroupStorage getGroupStorage(String world) throws SQLException {
        if (groupStores.containsKey(world))
            return groupStores.get(world);
        SqlGroupStorage sgs = new SqlGroupStorage(world, getWorld(world));
        groupStores.put(sgs.getWorld(), sgs);
        return sgs;
    }

    synchronized static void closeAll() {
        try {
            for (SqlUserStorage sus : userStores.values()) {
                sus.close();
            }
            Permissions.instance.getServer().getScheduler().cancelTask(reloadId);
            init = false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static Connection getConnection() throws SQLException {
        return dbSource.getConnection();
    }
}

enum Dbms {
    SQLITE("org.sqlite.JDBC"), MYSQL("com.mysql.jdbc.driver");
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
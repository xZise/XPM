package com.nijiko.data;

import java.sql.Connection;
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

public class SqlStorage {

    private static DataSource dbSource;
    // private static int reloadDelay;
    private static boolean init = false;
    private static Map<String, SqlUserStorage> userStores = new HashMap<String, SqlUserStorage>();
    private static HashMap<String, SqlGroupStorage> groupStores = new HashMap<String, SqlGroupStorage>();
    private static List<String> create = new ArrayList<String>(8);
    static {
        create.add("CREATE TABLE IF NOT EXISTS Worlds ("
                + " worldid INT NOT NULL PRIMARY KEY,"
                + " worldname VARCHAR(32) NOT NULL,"
                + " CONSTRAINT WorldNoSelfInherit CHECK (worldid IS NOT = parentid),"
                + ")");
        create.add("CREATE TABLE IF NOT EXISTS Users ("
                + " uid INT NOT NULL PRIMARY KEY,"
                + " username VARCHAR(32) NOT NULL,"
                + " worldid INT NOT NULL FORIEGN KEY REFERENCES Worlds(worldid),"
                + " CONSTRAINT UserNameWorld UNIQUE (username, World),"
                + " INDEX(username)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS Groups ("
                + " gid INT NOT NULL PRIMARY KEY,"
                + " groupname VARCHAR(32) NOT NULL,"
                + " worldid  INT NOT NULL FORIEGN KEY REFERENCES Worlds(worldid),"
                + " prefix VARCHAR(32) NOT NULL,"
                + " suffix VARCHAR(32) NOT NULL, "
                + " build TINYINT NOT NULL DEFAULT 0"
                + " CONSTRAINT GroupNameWorld UNIQUE (groupname, World)," + ")");
        create.add("CREATE TABLE IF NOT EXISTS UserPermission ("
                + " upermid INT NOT NULL PRIMARY KEY,"
                + " permstring VARCHAR(64) NOT NULL,"
                + " uid int NOT NULL FOREIGN KEY REFERENCES Users(uid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS GroupPermission ("
                + " gpermid INT NOT NULL PRIMARY KEY,"
                + " permstring VARCHAR(64) NOT NULL,"
                + " gid int NOT NULL FOREIGN KEY REFERENCES Groups(gid)" + ")");
        create.add("CREATE TABLE IF NOT EXISTS UserInheritance ("
                + " uinheritid INT NOT NULL PRIMARY KEY,"
                + " childid INT NOT NULL FOREIGN KEY REFERENCES Users(uid),"
                + " parentid int NOT NULL FOREIGN KEY REFERENCES Groups(gid)"
                + ");");
        create.add("CREATE TABLE IF NOT EXISTS GroupInheritance ("
                + " ginheritid INT NOT NULL PRIMARY KEY,"
                + " childid INT NOT NULL FOREIGN KEY REFERENCES Groups(gid),"
                + " parentid int NOT NULL FOREIGN KEY REFERENCES Groups(gid),"
                + " CONSTRAINT GroupNoSelfInherit CHECK (childid IS NOT = parentid)"
                + ")");
        create.add("CREATE TABLE IF NOT EXISTS WorldBase ("
                + " worldid INT NOT NULL FOREIGN KEY REFERENCES Worlds(worldid),"
                + " defaultid INT NOT NULL FOREIGN KEY REFERENCES Groups(gid),"
                + ")");
    }

    public static void init(String dbmsName, String uri, String username,
            String password, int reloadDelay) throws Exception {
        if (init)
            return;
        // SqlStorage.reloadDelay = reloadDelay;
        Dbms dbms = null;
        try {
            dbms = Dbms.valueOf(dbmsName);
        } catch (IllegalArgumentException e) {
            System.err
                    .println("Error occurred while selecting permissions config DBMS. Reverting to SQLite.");
            dbms = Dbms.SQLITE;
        }
        try {
            Class.forName(dbms.getDriver());
        } catch (ClassNotFoundException e) {
            throw new Exception("Unable to load SQL driver!", e);
        }
        dbSource = dbms.getSource(username, password, uri);
        verifyAndCreateTables(dbms);
        Permissions.instance
                .getServer()
                .getScheduler()
                .scheduleAsyncRepeatingTask(Permissions.instance,
                        new Runnable() {
                            @Override
                            public void run() {
                                refresh();
                            }

                        }, reloadDelay, reloadDelay);

        init = true;
    }

    private SqlStorage() {
    }

    private static void refresh() // Used for periodic cache flush
    {
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
    
    static DataSource getSource()
    {
        return dbSource;
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

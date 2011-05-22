package com.nijiko.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.nijiko.data.PreparedStatementPool.PreparedStatementWrapper;
import com.nijiko.data.SqlStorage.NameWorldId;


public class SqlUserStorage implements UserStorage {

    private static int max = 5;
    private final String userWorld;
    private int worldId;
    private Map<String, Integer> userIds = new HashMap<String, Integer>();

    private static final String permGetText = "SELECT PrUserPermissions.permstring FROM PrUserPermissions WHERE PrUserPermissions.uid = ?;";
    private static PreparedStatementPool permGetPool;
    private static final String parentGetText = "SELECT parentid FROM PrUserInheritance WHERE PrUserInheritance.childid = ?;";
    private static PreparedStatementPool parentGetPool;


    private static final String permAddText = "INSERT IGNORE INTO PrUserPermissions (uid, permstring) VALUES (?,?);";
    private static PreparedStatementPool permAddPool;
    private static final String permRemText = "DELETE FROM PrUserPermissions WHERE uid = ? AND permstring = ?;";
    private static PreparedStatementPool permRemPool;
    private static final String parentAddText = "INSERT IGNORE INTO PrUserInheritance (childid, parentid) VALUES (?,?);";
    private static PreparedStatementPool parentAddPool;
    private static final String parentRemText = "DELETE FROM PrUserInheritance WHERE childid = ? AND parentid = ?;";
    private static PreparedStatementPool parentRemPool;

    private static final String userListText = "SELECT username, uid FROM PrUsers WHERE worldid = ?;";
    private static PreparedStatementPool userListPool;
    
    private static final String dataGetText = "SELECT * FROM PrUserData WHERE uid = ? AND path = ?;";
    private static PreparedStatementPool dataGetPool;
    private static final String dataModText = "REPLACE INTO PrUserData (data, uid, path) VALUES (?,?,?);";
    private static PreparedStatementPool dataModPool;
    private static final String dataDelText = "DELETE FROM PrUserData WHERE uid = ? AND path = ?;";
    private static PreparedStatementPool dataDelPool;
    

    static void reloadPools(Connection dbConn) {
        Dbms dbms = SqlStorage.getDbms();
        permGetPool = new PreparedStatementPool(dbConn, permGetText, max);
        parentGetPool = new PreparedStatementPool(dbConn, parentGetText, max);
        permAddPool = new PreparedStatementPool(dbConn, (dbms==Dbms.SQLITE ? permAddText.replace("IGNORE", "OR IGNORE") : permAddText), max);
        permRemPool = new PreparedStatementPool(dbConn, permRemText, max);
        parentAddPool = new PreparedStatementPool(dbConn, (dbms==Dbms.SQLITE ? parentAddText.replace("IGNORE", "OR IGNORE") : parentAddText), max);
        parentRemPool = new PreparedStatementPool(dbConn, parentRemText, max);
        userListPool = new PreparedStatementPool(dbConn, userListText, max);
        dataModPool = new PreparedStatementPool(dbConn, dataModText, max);
        dataDelPool = new PreparedStatementPool(dbConn, dataDelText, max);
        dataGetPool = new PreparedStatementPool(dbConn, dataGetText, max);
    }
    public SqlUserStorage(String userWorld, int id) {
        worldId = id;
        this.userWorld = userWorld;

        reload();
    }

    @Override
    public Set<String> getPermissions(String name) {
        if (name == null)
            return new HashSet<String>();
        Set<String> permissions = new HashSet<String>();

        PreparedStatementWrapper wrap = null;

        try {
            wrap = permGetPool.getStatement();
            PreparedStatement permGetStmt = wrap.getStatement();
            permGetStmt.clearParameters();
            int uid = 0;
            if(userIds.containsKey(name)) uid = userIds.get(name);
            else  {
                uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            }
            permGetStmt.setInt(1, uid);
            ResultSet rs = permGetStmt.executeQuery();
            while (rs.next()) {
                permissions.add(rs.getString(1));
                if(SqlStorage.getDbms()==Dbms.MYSQL && rs.isClosed()) break; //Temp fix for MySQL's stupid implementation of next()
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashSet<String>();
        } finally {
            if(wrap != null) wrap.close();
        }

        return permissions;
    }

    @Override
    public LinkedHashSet<GroupWorld> getParents(String name) {
        if (name == null)
            return new LinkedHashSet<GroupWorld>();
        LinkedHashSet<GroupWorld> parents = new LinkedHashSet<GroupWorld>();

        PreparedStatementWrapper wrap = null;
        try {
            wrap = parentGetPool.getStatement();
            PreparedStatement parentGetStmt = wrap.getStatement();
            parentGetStmt.clearParameters();
            int uid = 0;
            if(userIds.containsKey(name)) uid = userIds.get(name);
            else  {
                uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            }
            parentGetStmt.setInt(1, uid);
            ResultSet rs = parentGetStmt.executeQuery();
            while (rs.next()) {
                int groupid = rs.getInt(1);
                NameWorldId nw = SqlStorage.getGroupName(groupid);
                String worldName = SqlStorage.getWorldName(nw.worldid);
                GroupWorld gw = new GroupWorld(worldName, nw.name);
                parents.add(gw);
                if(SqlStorage.getDbms()==Dbms.MYSQL && rs.isClosed()) break; //Temp fix for MySQL's stupid implementation of next()
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new LinkedHashSet<GroupWorld>();
        } finally {
            if(wrap != null) wrap.close();
        }
        return parents;
    }

    @Override
    public void addPermission(String name, String permission) {
        PreparedStatementWrapper wrap = null;
        try {
            wrap = permAddPool.getStatement();
            PreparedStatement permAddStmt = wrap.getStatement();
            permAddStmt.clearParameters();
            int uid = 0;
            if(userIds.containsKey(name)) uid = userIds.get(name);
            else  {
                uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            }
            permAddStmt.setInt(1, uid);
            permAddStmt.setString(2, permission);
            permAddStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }
    }

    @Override
    public void removePermission(String name, String permission) {
        PreparedStatementWrapper wrap = null;
        try {
            wrap = permRemPool.getStatement();
            PreparedStatement permRemStmt = wrap.getStatement();
            permRemStmt.clearParameters();
            int uid = 0;
            if(userIds.containsKey(name)) uid = userIds.get(name);
            else  {
                uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            }
            permRemStmt.setInt(1, uid);
            permRemStmt.setString(2, permission);
            permRemStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }
    }

    @Override
    public void addParent(String name, String groupWorld, String groupName) {
//        System.out.println("Adding parent " + groupName + " in "+ groupWorld);

        PreparedStatementWrapper wrap = null;
        try {
            wrap = parentAddPool.getStatement();
            PreparedStatement parentAddStmt = wrap.getStatement();
            parentAddStmt.clearParameters();
            int uid = 0;
            if(userIds.containsKey(name)) uid = userIds.get(name);
            else  {
                uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            }
            int gid = SqlStorage.getGroup(groupWorld, groupName);
            parentAddStmt.setInt(1, uid);
            parentAddStmt.setInt(2, gid);
            parentAddStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }

    }

    @Override
    public void removeParent(String name, String groupWorld, String groupName) {
//        System.out.println("Removing parent " + groupName + " in "+ groupWorld);
        PreparedStatementWrapper wrap = null;
        try {
            wrap = parentRemPool.getStatement();
            PreparedStatement parentRemStmt = wrap.getStatement();
            parentRemStmt.clearParameters();
            int uid = 0;
            if(userIds.containsKey(name)) uid = userIds.get(name);
            else  {
                uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            }
            int gid = SqlStorage.getGroup(groupWorld, groupName);
            parentRemStmt.setInt(1, uid);
            parentRemStmt.setInt(2, gid);
            parentRemStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }

    }

    @Override
    public Set<String> getUsers() {
        if(userIds.isEmpty()) {
            PreparedStatementWrapper wrap = null;
            try {
                wrap = userListPool.getStatement();
                PreparedStatement userListStmt = wrap.getStatement();
                userListStmt.clearParameters();
                userListStmt.setInt(1, worldId);
                ResultSet rs = userListStmt.executeQuery();
                while(rs.next()) {
                    userIds.put(rs.getString(1), rs.getInt(2));
                    if(SqlStorage.getDbms()==Dbms.MYSQL && rs.isClosed()) break; //Temp fix for MySQL's stupid implementation of next()
                }
            } catch(SQLException e) {
                e.printStackTrace();
            } finally {
                if(wrap != null) wrap.close();
            }
        }
        return userIds.keySet();
    }

    @Override
    public String getWorld() {
        return userWorld;
    }

    @Override
    public void forceSave() {
        return;
    }

    @Override
    public void save() {
        return;
    }

    @Override
    public void reload() {
        userIds.clear();
    }

    @Override
    public boolean isAutoSave() {
        return true;
    }

    @Override
    public void setAutoSave(boolean autoSave) {
        return;
    }

    @Override
    public boolean createUser(String name) {
        if(userIds.containsKey(name)) {
            try {
                int uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String getString(String name, String path) {
        String data = null;
        PreparedStatementWrapper wrap = null;
        try {
            wrap = dataGetPool.getStatement();
            PreparedStatement dataGetStmt = wrap.getStatement();
            int uid = 0;
            if(userIds.containsKey(name)) uid = userIds.get(name);
            else  {
                uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            }
            dataGetStmt.clearParameters();
            dataGetStmt.setInt(1, uid);
            dataGetStmt.setString(2, path);
            ResultSet rs = dataGetStmt.executeQuery();
            if(rs.next()) {
                data = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }
        return data;
    }

    @Override
    public Integer getInt(String name, String path) {
        String raw = getString(name, path);
        Integer value;
        try {
            value = Integer.valueOf(raw);
        } catch (NumberFormatException e) {
            value = null;
        }
        return value;
    }

    @Override
    public Double getDouble(String name, String path) {
        String raw = getString(name, path);
        Double value;
        try {
            value = Double.valueOf(raw);
        } catch (NumberFormatException e) {
            value = null;
        }
        return value;
    }

    @Override
    public Boolean getBool(String name, String path) {
        String raw = getString(name, path);
        Boolean value;
        try {
            value = Boolean.valueOf(raw);
        } catch (NumberFormatException e) {
            value = null;
        }
        return value;
    }

    @Override
    public void setData(String name, String path, Object data) {
        String szForm = "";
        if (data instanceof Integer) {
            szForm = ((Integer) data).toString();
        } else if (data instanceof Boolean) {
            szForm = ((Boolean) data).toString();
        } else if (data instanceof Double) {
            szForm = ((Double) data).toString();
        } else if (data instanceof String) {
            szForm = (String) data;
        } else {
            throw new IllegalArgumentException("Only ints, bools, doubles and Strings are allowed!");
        }

        PreparedStatementWrapper wrap = null;
        try {
            int uid = 0;
            if(userIds.containsKey(name)) uid = userIds.get(name);
            else  {
                uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            }
            wrap = dataModPool.getStatement();
            PreparedStatement dataModStmt = wrap.getStatement();
            dataModStmt.clearParameters();
            dataModStmt.setString(1, szForm);
            dataModStmt.setInt(2, uid);
            dataModStmt.setString(3, path);
            dataModStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            data = "";
        } finally {
            if(wrap != null) wrap.close();
        }
        
    }

    @Override
    public void removeData(String name, String path) {        
        PreparedStatementWrapper wrap = null;
        try {
            int uid = 0;
            if(userIds.containsKey(name)) uid = userIds.get(name);
            else  {
                uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            }
            wrap = dataDelPool.getStatement();
            PreparedStatement dataDelStmt = wrap.getStatement();
            dataDelStmt.clearParameters();
            dataDelStmt.setInt(1, uid);
            dataDelStmt.setString(2, path);
            dataDelStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public static void close() {
        parentGetPool.close();
        parentAddPool.close();
        parentRemPool.close();
        permGetPool.close();
        permAddPool.close();
        permRemPool.close();
        dataGetPool.close();
        dataModPool.close();
        dataDelPool.close();
        userListPool.close();
    }

    public Integer getUserId(String name) {
        return userIds.get(name);
    }

}

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

import com.nijiko.data.SqlStorage.NameWorldId;


public class SqlUserStorage implements UserStorage {

    private final String userWorld;
    private int worldId;
    private Map<String, Integer> userIds = new HashMap<String, Integer>();
    private Connection dbConn;

    private static final String permGetText = "SELECT PrUserPermissions.permstring FROM PrUserPermissions WHERE PrUserPermissions.uid = ?;";
    PreparedStatement permGetStmt;
    private static final String parentGetText = "SELECT parentid FROM PrUserInheritance WHERE PrUserInheritance.childid = ?;";
    PreparedStatement parentGetStmt;


    private static final String permAddText = "INSERT IGNORE INTO PrUserPermissions (uid, permstring) VALUES (?,?);";
    PreparedStatement permAddStmt;
    private static final String permRemText = "DELETE FROM PrUserPermissions WHERE uid = ? AND permstring = ?;";
    PreparedStatement permRemStmt;
    private static final String parentAddText = "INSERT IGNORE INTO PrUserInheritance (childid, parentid) VALUES (?,?);";
    PreparedStatement parentAddStmt;
    private static final String parentRemText = "DELETE FROM PrUserInheritance WHERE childid = ? AND parentid = ?;";
    PreparedStatement parentRemStmt;

    private static final String userListText = "SELECT username, uid FROM PrUsers WHERE worldid = ?;";
    PreparedStatement userListStmt;
    
    private static final String dataGetText = "SELECT * FROM PrUserData WHERE uid = ? AND path = ?;";
    PreparedStatement dataGetStmt;
    private static final String dataModText = "REPLACE INTO PrUserData (data, uid, path) VALUES (?,?,?);";
    PreparedStatement dataModStmt;
    private static final String dataDelText = "DELETE FROM PrUserData WHERE uid = ? AND path = ?;";
    PreparedStatement dataDelStmt;
    
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


        try {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashSet<String>();
        }

        return permissions;
    }

    @Override
    public LinkedHashSet<GroupWorld> getParents(String name) {
        if (name == null)
            return new LinkedHashSet<GroupWorld>();
        LinkedHashSet<GroupWorld> parents = new LinkedHashSet<GroupWorld>();

        try {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new LinkedHashSet<GroupWorld>();
        }
        return parents;
    }

    @Override
    public void addPermission(String name, String permission) {
        try {
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
        }
    }

    @Override
    public void removePermission(String name, String permission) {
        try {
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
        }
    }

    @Override
    public void addParent(String name, String groupWorld, String groupName) {
        System.out.println("Adding parent " + groupName + " in "+ groupWorld);
        try {
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
        }

    }

    @Override
    public void removeParent(String name, String groupWorld, String groupName) {
        System.out.println("Removing parent " + groupName + " in "+ groupWorld);
        try {
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
        }

    }

    @Override
    public Set<String> getUsers() {
        if(userIds.isEmpty()) {
            try {
                userListStmt.clearParameters();
                userListStmt.setInt(1, worldId);
                ResultSet rs = userListStmt.executeQuery();
                while(rs.next()) {
                    userIds.put(rs.getString(1), rs.getInt(2));
                }
            } catch(SQLException e) {
                e.printStackTrace();
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
        try {
//            close();
            Dbms dbms = SqlStorage.getDbms();
            worldId = SqlStorage.getWorld(userWorld);
            dbConn = SqlStorage.getConnection();
            permGetStmt = dbConn.prepareStatement(permGetText);
            parentGetStmt = dbConn.prepareStatement(parentGetText);
            permAddStmt = dbConn.prepareStatement((dbms==Dbms.SQLITE ? permAddText.replace("IGNORE", "OR IGNORE") : permAddText));
            permRemStmt = dbConn.prepareStatement(permRemText);
            parentAddStmt = dbConn.prepareStatement((dbms==Dbms.SQLITE ? parentAddText.replace("IGNORE", "OR IGNORE") : parentAddText));
            parentRemStmt = dbConn.prepareStatement(parentRemText);
            userListStmt = dbConn.prepareStatement(userListText);
            dataModStmt = dbConn.prepareStatement(dataModText);
            dataDelStmt = dbConn.prepareStatement(dataDelText);
            dataGetStmt = dbConn.prepareStatement(dataGetText);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        try {
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

        try {
            int uid = 0;
            if(userIds.containsKey(name)) uid = userIds.get(name);
            else  {
                uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            }
            dataModStmt.clearParameters();
            dataModStmt.setString(1, szForm);
            dataModStmt.setInt(2, uid);
            dataModStmt.setString(3, path);
            dataModStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            data = "";
        }
        
    }

    @Override
    public void removeData(String name, String path) {        
        try {
            int uid = 0;
            if(userIds.containsKey(name)) uid = userIds.get(name);
            else  {
                uid = SqlStorage.getUser(userWorld, name);
                userIds.put(name, uid);
            }
            dataDelStmt.clearParameters();
            dataDelStmt.setInt(1, uid);
            dataDelStmt.setString(2, path);
            dataDelStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public void close() throws SQLException {
        if(parentGetStmt!=null)parentGetStmt.close();
        if(parentAddStmt!=null)parentAddStmt.close();
        if(parentRemStmt!=null)parentRemStmt.close();
        if(permGetStmt!=null)permGetStmt.close();
        if(permAddStmt!=null)permAddStmt.close();
        if(permRemStmt!=null)permRemStmt.close();
        if(dataGetStmt!=null)dataGetStmt.close();
        if(dataModStmt!=null)dataModStmt.close();
        if(dataDelStmt!=null)dataDelStmt.close();
        if(userListStmt!=null)userListStmt.close();
        if(dbConn!=null)dbConn.close();
    }

    public Integer getUserId(String name) {
        return userIds.get(name);
    }

}

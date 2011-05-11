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


public class SqlUserStorage implements UserStorage {

    private final String userWorld;
    private int worldId;
    private Map<String, Integer> userIds = new HashMap<String, Integer>();
    private Map<String, Set<String>> userPermissions = new HashMap<String, Set<String>>();
    private Map<String, LinkedHashSet<GroupWorld>> userParents = new HashMap<String, LinkedHashSet<GroupWorld>>();
    private Map<String, Map<String, String>> userData = new HashMap<String, Map<String, String>>();
    private Connection dbConn;

    private static final String permGetText = "SELECT UserPermissions.permstring FROM UserPermissions WHERE UserPermissions.uid = ?;";
    PreparedStatement permGetStmt;
    private static final String parentGetText = "SELECT * FROM UserInheritance WHERE UserInheritance.childid = ?;";
    PreparedStatement parentGetStmt;


    private static final String permAddText = "INSERT INTO UserPermissions (uid, permstring) VALUES (?,?);";
    PreparedStatement permAddStmt;
    private static final String permRemText = "DELETE FROM UserPermissions WHERE uid = ? AND permstring = ?;";
    PreparedStatement permRemStmt;
    private static final String parentAddText = "INSERT INTO UserInheritance (childid, parentid) VALUES (?,?);";
    PreparedStatement parentAddStmt;
    private static final String parentRemText = "DELETE FROM UserInheritance WHERE childid = ? AND parentid = ?;";
    PreparedStatement parentRemStmt;

    private static final String userListText = "SELECT username, uid FROM Users WHERE worldid = ?;";
    PreparedStatement userListStmt;
    private static final String userAddText = "INSERT INTO Users (worldid,username) VALUES (?,?);";
    PreparedStatement userAddStmt;
    
    private static final String dataGetText = "SELECT * FROM UserData WHERE uid = ? AND path = ?;";
    PreparedStatement dataGetStmt;
    private static final String dataAddText = "INSERT INTO UserData (data, uid, path) VALUES (?,?,?);";
    PreparedStatement dataAddStmt;
    private static final String dataEditText = "UPDATE UserData SET data = ? WHERE uid = ? AND path = ?;";
    PreparedStatement dataEditStmt;
    private static final String dataDelText = "DELETE FROM UserData WHERE uid = ? AND path = ?;";
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
        Set<String> permissions = userPermissions.get(name);
        if (permissions != null)
            return permissions;
        permissions = new HashSet<String>();


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

        userPermissions.put(name, permissions);
        return permissions;
    }

    @Override
    public LinkedHashSet<GroupWorld> getParents(String name) {
        if (name == null)
            return new LinkedHashSet<GroupWorld>();
        LinkedHashSet<GroupWorld> parents = userParents.get(name);
        if (parents != null)
            return parents;
        parents = new LinkedHashSet<GroupWorld>();

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
                GroupWorld gw = new GroupWorld(rs.getString(1), rs.getString(2));
                parents.add(gw);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new LinkedHashSet<GroupWorld>();
        }
        userParents.put(name, parents);
        return null;
    }

    @Override
    public void addPermission(String name, String permission) {
        if (userPermissions.get(name) == null)
            userPermissions.put(name, new HashSet<String>());
        Set<String> perms = userPermissions.get(name);
        if (perms.contains(permission))
            return;
        perms.add(permission);

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
        if (userPermissions.get(name) == null)
            userPermissions.put(name, new LinkedHashSet<String>());
        Set<String> perms = userPermissions.get(name);
        if (!perms.contains(permission))
            return;
        perms.remove(permission);
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
        if (userParents.get(name) == null)
            userParents.put(name, new LinkedHashSet<GroupWorld>());
        Set<GroupWorld> parents = userParents.get(name);
        if (parents.contains(groupWorld))
            return;
        parents.add(new GroupWorld(groupWorld, groupName));
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
        if (userParents.get(name) == null)
            userParents.put(name, new LinkedHashSet<GroupWorld>());
        Set<GroupWorld> parents = userParents.get(name);
        if (parents.contains(groupWorld))
            return;
        parents.remove(new GroupWorld(groupWorld, groupName));
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
        userPermissions.clear();
        userParents.clear();
        userIds.clear();
        userData.clear();
        try {
            close();
            worldId = SqlStorage.getWorld(userWorld);
            dbConn = SqlStorage.getConnection();
            permGetStmt = dbConn.prepareStatement(permGetText);
            parentGetStmt = dbConn.prepareStatement(parentGetText);
            permAddStmt = dbConn.prepareStatement(permAddText);
            permRemStmt = dbConn.prepareStatement(permRemText);
            parentAddStmt = dbConn.prepareStatement(parentAddText);
            parentAddStmt = dbConn.prepareStatement(parentRemText);
            userListStmt = dbConn.prepareStatement(userListText);
            userAddStmt = dbConn.prepareStatement(userAddText);
            dataAddStmt = dbConn.prepareStatement(dataAddText);
            dataDelStmt = dbConn.prepareStatement(dataDelText);
            dataEditStmt = dbConn.prepareStatement(dataEditText);
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
        if(userData.get(name)==null)userData.put(name, new HashMap<String, String>());
        String data = userData.get(name).get(path);
        if(data != null) return data;
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
            if(!rs.next()) data = "";
            else {
                data = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            data = "";
        }
        if(data == null) data = "";
        userData.get(name).put(path, data);
        return data;
    }

    @Override
    public int getInt(String name, String path) {
        String raw = getString(name, path);
        int value;
        try {
            value = Integer.valueOf(raw);
        } catch (NumberFormatException e) {
            value = -1;
        }
        return value;
    }

    @Override
    public double getDouble(String name, String path) {
        String raw = getString(name, path);
        double value;
        try {
            value = Double.valueOf(raw);
        } catch (NumberFormatException e) {
            value = -1.0D;
        }
        return value;
    }

    @Override
    public boolean getBool(String name, String path) {
        String raw = getString(name, path);
        boolean value;
        try {
            value = Boolean.valueOf(raw);
        } catch (NumberFormatException e) {
            value = false;
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
        if(userData.get(name)==null)userData.put(name, new HashMap<String, String>());
        userData.get(name).put(path, szForm);

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
            PreparedStatement addEdit;
            if(dataGetStmt.executeQuery().next()) addEdit = dataEditStmt;
            else addEdit = dataAddStmt;
            addEdit.clearParameters();
            addEdit.setString(1, szForm);
            addEdit.setInt(2, uid);
            addEdit.setString(3, path);
            addEdit.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            data = "";
        }
        
    }

    @Override
    public void removeData(String name, String path) {
        if(userData.get(name)==null)userData.put(name, new HashMap<String, String>());
        userData.get(name).remove(path);
        
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
        if(dataAddStmt!=null)dataAddStmt.close();
        if(dataDelStmt!=null)dataDelStmt.close();
        if(dataEditStmt!=null)dataEditStmt.close();
        if(userListStmt!=null)userListStmt.close();
        if(userAddStmt!=null)userAddStmt.close();
        if(dbConn!=null)dbConn.close();
    }

    public Integer getUserId(String name) {
        return userIds.get(name);
    }

}

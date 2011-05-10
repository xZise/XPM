package com.nijiko.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.nijiko.permissions.EntryType;
import com.nijiko.permissions.Group;

public class SqlUserStorage implements UserStorage {

    private Connection dbConn;
    
    private String userWorld;
    private int worldId;
    private Map<String, Set<String>> userPermissions = new HashMap<String, Set<String>>();
    private Map<String, LinkedHashSet<GroupWorld>> userParents = new HashMap<String, LinkedHashSet<GroupWorld>>();

    private static final String permGetText = "SELECT * FROM Worlds, Users, UserPermissions WHERE Worlds.worldname = ?, Users.username = ?, Users.worldid = World.worldid, UserPermissions.uid = Users.uid;";
    private static final String parentGetText = "SELECT * FROM Worlds, Users, UserInheritance WHERE Worlds.worldname = ?, Users.username = ?, Users.worldid = World.worldid, UserInheritance.uid = Users.uid;";

    private static final String permAddText = "INSERT INTO UserPermissions (uid, permstring) VALUES (<?>,<?>);";
    private static final String parentAddText = "INSERT INTO UserPermissions (childid, parentid) VALUES (<?>,<?>);";

    private static PreparedStatement permGetStmt;
    private static PreparedStatement parentGetStmt;
    public SqlUserStorage(String userWorld) {
        reload();
    }

    @Override
    public Set<String> getPermissions(String name) {
        if (name == null)
            return new HashSet<String>();
        Set<String> permissions = userPermissions.get(name.toLowerCase());
        if (permissions != null)
            return permissions;
        permissions = new HashSet<String>();

        DataSource ds = SqlStorage.getSource();
        Connection dbConn;
        try {
            dbConn = ds.getConnection();
        } catch (SQLException e1) {
            e1.printStackTrace();
            return new HashSet<String>();
        }

        try {
            permGetStmt.clearParameters();
            ResultSet rs = permGetStmt.executeQuery();
            while (rs.next()) {
                permissions.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashSet<String>();
        } finally {
            try {
                dbConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        userPermissions.put(name.toLowerCase(), permissions);
        return permissions;
    }

    @Override
    public LinkedHashSet<GroupWorld> getParents(String name) {
        if (name == null)
            return new LinkedHashSet<GroupWorld>();
        LinkedHashSet<GroupWorld> parents = userParents.get(name.toLowerCase());
        if (parents != null)
            return parents;
        parents = new LinkedHashSet<GroupWorld>();
        DataSource ds = SqlStorage.getSource();
        Connection dbConn;
        try {
            dbConn = ds.getConnection();
        } catch (SQLException e1) {
            e1.printStackTrace();
            return new LinkedHashSet<GroupWorld>();
        }

        try {
            parentGetStmt.clearParameters();
            ResultSet rs = parentGetStmt.executeQuery();
            while (rs.next()) {
                GroupWorld gw = new GroupWorld(rs.getString(1), rs.getString(2));
                parents.add(gw);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new LinkedHashSet<GroupWorld>();
        } finally {
            try {
                dbConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        userParents.put(name.toLowerCase(), parents);
        return null;
    }

    @Override
    public void addPermission(String name, String permission) {
        if (userPermissions.get(name.toLowerCase()) == null)
            userPermissions.put(name.toLowerCase(), new HashSet<String>());
        Set<String> perms = userPermissions.get(name.toLowerCase());
        if (perms.contains(permission))
            return;
        perms.add(permission);
        // TODO SQL Update
    }

    @Override
    public void removePermission(String name, String permission) {
        if (userPermissions.get(name.toLowerCase()) == null)
            userPermissions.put(name.toLowerCase(), new LinkedHashSet<String>());
        Set<String> perms = userPermissions.get(name.toLowerCase());
        if (!perms.contains(permission))
            return;
        perms.remove(permission);
        // TODO SQL Update
    }

    @Override
    public void addParent(String name, String groupWorld, String groupName) {
        if (userParents.get(name.toLowerCase()) == null)
            userParents.put(name.toLowerCase(), new LinkedHashSet<GroupWorld>());
        Set<GroupWorld> parents = userParents.get(name.toLowerCase());
        if (parents.contains(groupWorld))
            return;
        parents.add(new GroupWorld(groupWorld, groupName));
        // TODO SQL Updates

    }

    @Override
    public void removeParent(String name, String groupWorld, String groupName) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<String> getUsers() {
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
    public void save() {
        // TODO Auto-generated method stub

    }

    @Override
    public void reload() {
        userPermissions.clear();
        userParents.clear();
        try {
//            if(dbConn != null) dbConn.close();
//            dbConn = SqlStorage.getSource().getConnection();
            ResultSet rs = SqlStorage.getWorldStmt.executeQuery();
            worldId = rs.getInt(1);
            permGetStmt = dbConn.prepareStatement(permGetText);
            parentGetStmt = dbConn.prepareStatement(parentGetText);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    @Override
    public boolean createUser(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getString(String name, String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getInt(String name, String path) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getDouble(String name, String path) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getBool(String name, String path) {
        // TODO Auto-generated method stub
        return false;
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

    }

    @Override
    public void removeData(String name, String path) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void finalize() {
        
    }
}

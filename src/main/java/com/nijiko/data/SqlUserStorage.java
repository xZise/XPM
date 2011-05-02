package com.nijiko.data;

import java.sql.Connection;
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

public class SqlUserStorage implements UserStorage {

    private String userWorld;
    private Map<String, Set<String>> userPermissions = new HashMap<String, Set<String>>();
    private Map<String, LinkedHashSet<GroupWorld>> userParents = new HashMap<String, LinkedHashSet<GroupWorld>>();

    private static final String permGetStmt = "";
    private static final String parentGetStmt = "";

    public SqlUserStorage(String userWorld) {
        // TODO Auto-generated constructor stub
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
            Statement s = dbConn.createStatement();
            s.execute(permGetStmt);
            ResultSet rs = s.getResultSet();
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
            Statement s = dbConn.createStatement();
            s.execute(parentGetStmt);
            ResultSet rs = s.getResultSet();
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
    public String getData(String name, String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setData(String name, String path, String data) {
        // TODO Auto-generated method stub
        
    }

}

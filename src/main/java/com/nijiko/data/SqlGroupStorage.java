package com.nijiko.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.nijiko.data.PreparedStatementPool.PreparedStatementWrapper;
import com.nijiko.data.SqlStorage.NameWorldId;

public class SqlGroupStorage implements GroupStorage {

    private static final int max = 5;

    private int worldId;
    private String groupWorld;
    private String baseGroup = null;
    private Map<String, Integer> groupIds = new HashMap<String, Integer>();
    private Set<String> buildGroups = new HashSet<String>();
    private static Connection dbConn;

    private static final String permGetText = "SELECT PrGroupPermissions.permstring FROM PrGroupPermissions WHERE PrGroupPermissions.gid = ?;";
    private static PreparedStatementPool permGetPool;
    private static final String parentGetText = "SELECT * FROM PrGroupInheritance WHERE PrGroupInheritance.childid = ?;";
    private static PreparedStatementPool parentGetPool;

    private static final String getGroupText = "SELECT * FROM PrGroups WHERE PrGroups.gid = ?;";
    private static PreparedStatementPool getGroupPool;
    private static final String getGroupsText = "SELECT * FROM PrGroups WHERE PrGroups.worldid = ?;";
    //No preparedstatement pool needed
    private static final String getBaseText = "SELECT PrGroups.groupname FROM PrWorldBase, PrGroups WHERE PrWorldBase.worldid = ? AND PrGroups.worldid = ? AND PrWorldBase.defaultid = PrGroups.gid;";
    //No preparedstatement pool needed

    private static final String permAddText = "INSERT IGNORE INTO PrGroupPermissions (gid, permstring) VALUES (?,?);";
    private static PreparedStatementPool permAddPool;
    private static final String permRemText = "DELETE FROM PrGroupPermissions WHERE gid = ? AND permstring = ?;";
    private static PreparedStatementPool permRemPool;
    private static final String parentAddText = "INSERT IGNORE INTO PrGroupInheritance (childid, parentid) VALUES (?,?);";
    private static PreparedStatementPool parentAddPool;
    private static final String parentRemText = "DELETE FROM PrGroupInheritance WHERE childid = ? AND parentid = ?;";
    private static PreparedStatementPool parentRemPool;

    private static final String groupListText = "SELECT groupname, gid FROM PrGroups WHERE worldid = ?;";
    private static PreparedStatementPool groupListPool;

    private static final String dataGetText = "SELECT * FROM PrGroupData WHERE gid = ? AND path = ?;";
    private static PreparedStatementPool dataGetPool;
    private static final String dataModText = "REPLACE INTO PrGroupData (data, gid, path) VALUES (?,?,?);";
    private static PreparedStatementPool dataModPool;
    private static final String dataDelText = "DELETE FROM PrGroupData WHERE gid = ? AND path = ?;";
    private static PreparedStatementPool dataDelPool;

    private static final String buildSetText = "UPDATE PrGroups SET build = ? WHERE gid = ?;";
    private static PreparedStatementPool buildSetPool;
    private static final String prefixSetText = "UPDATE PrGroups SET prefix = ? WHERE gid = ?;";
    private static PreparedStatementPool prefixSetPool;
    private static final String suffixSetText = "UPDATE PrGroups SET suffix = ? WHERE gid = ?;";
    private static PreparedStatementPool suffixSetPool;

    private static final String trackListText = "SELECT * FROM PrTracks WHERE worldid = ?;";
    private static PreparedStatementPool trackListPool;
    private static final String trackGetText = "SELECT PrWorlds.worldname, PrGroups.groupname FROM PrWorlds, PrGroups, PrTracks, PrTrackGroups WHERE PrTrackGroups.trackid = PrTracks.trackid AND PrTracks.worldid = ? AND PrTracks.trackname = ? AND PrGroups.gid = PrTrackGroups.gid AND PrWorlds.worldid = PrGroups.worldid ORDER BY PrTrackGroups.groupOrder;";
    private static PreparedStatementPool trackGetPool;
    // private static final String weightSetText =
    // "UPDATE Groups SET suffix = ? WHERE gid = ?;";
    // PreparedStatement weightSetStmt;

    
    static void reloadPools(Connection dbConn) {
        Dbms dbms = SqlStorage.getDbms();
        permGetPool = new PreparedStatementPool(dbConn, permGetText, max);
        parentGetPool = new PreparedStatementPool(dbConn, parentGetText, max);
        getGroupPool = new PreparedStatementPool(dbConn, getGroupText, max);
        permAddPool = new PreparedStatementPool(dbConn,(dbms==Dbms.SQLITE ? permAddText.replace("IGNORE", "OR IGNORE") : permAddText), max);
        permRemPool = new PreparedStatementPool(dbConn, permRemText, max);
        parentAddPool = new PreparedStatementPool(dbConn, (dbms==Dbms.SQLITE ? parentAddText.replace("IGNORE", "OR IGNORE") : parentAddText), max);
        parentRemPool = new PreparedStatementPool(dbConn, parentRemText, max);
        groupListPool = new PreparedStatementPool(dbConn, groupListText, max);
        dataGetPool = new PreparedStatementPool(dbConn, dataGetText, max);
        dataModPool = new PreparedStatementPool(dbConn, dataModText, max);
        dataDelPool = new PreparedStatementPool(dbConn, dataDelText, max);
        buildSetPool = new PreparedStatementPool(dbConn, buildSetText, max);
        prefixSetPool = new PreparedStatementPool(dbConn, prefixSetText, max);
        suffixSetPool = new PreparedStatementPool(dbConn, suffixSetText, max);
        trackListPool = new PreparedStatementPool(dbConn, trackListText, max);
        trackGetPool = new PreparedStatementPool(dbConn, trackGetText, max);
    }
    public SqlGroupStorage(String groupWorld, int id) throws SQLException {
        worldId = id;
        this.groupWorld = groupWorld;
        if(dbConn == null) dbConn = SqlStorage.getConnection();

        worldId = SqlStorage.getWorld(groupWorld);
        try {   
            ResultSet rs = dbConn.createStatement().executeQuery(getBaseText.replace("?", String.valueOf(worldId)));
            if (rs.next()) {
                baseGroup = rs.getString(1);
            }

            rs = dbConn.createStatement().executeQuery(getGroupsText.replace("?", String.valueOf(worldId)));
            while (rs.next()) {
                int gid = rs.getInt(1);
                String groupName = rs.getString(2);
                // Skip worldId
                boolean build = (rs.getByte(6) != 0);
                groupIds.put(groupName, gid);
                if (build)
                    buildGroups.add(groupName);
                if(SqlStorage.getDbms()==Dbms.MYSQL && rs.isClosed()) break; //Temp fix for MySQL's stupid implementation of next()
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        reload();
    }

    @Override
    public boolean isDefault(String name) {
        return baseGroup != null && baseGroup.equals(name);
    }

    @Override
    public boolean canBuild(String name) {
        if (buildGroups.contains(name))
            return true;
        PreparedStatementWrapper wrap = null;
        boolean build = false;
        try {
            wrap = getGroupPool.getStatement();
            PreparedStatement getGroupStmt = wrap.getStatement();
            getGroupStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            getGroupStmt.setInt(1, gid);
            ResultSet rs = getGroupStmt.executeQuery();
            if(!rs.next()) return build;
            build = (rs.getByte(6) != 0);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }
        if (build)
            buildGroups.add(name);
        else
            buildGroups.remove(name);

        return build;
    }

    @Override
    public String getPrefix(String name) {
        String prefix = null;
        PreparedStatementWrapper wrap = null;
        try {
            wrap = getGroupPool.getStatement();
            PreparedStatement getGroupStmt = wrap.getStatement();
            getGroupStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            getGroupStmt.setInt(1, gid);
            ResultSet rs = getGroupStmt.executeQuery();
            if(!rs.next()) return prefix;
            prefix = rs.getString(4);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }
        return prefix;
    }

    @Override
    public String getSuffix(String name) {
        String suffix = null;
        PreparedStatementWrapper wrap = null;
        try {
            wrap = getGroupPool.getStatement();
            PreparedStatement getGroupStmt = wrap.getStatement();
            getGroupStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            getGroupStmt.setInt(1, gid);
            ResultSet rs = getGroupStmt.executeQuery();
            if(!rs.next()) return suffix;
            suffix = rs.getString(5);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }
        return suffix;
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
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            permGetStmt.setInt(1, gid);
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
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            parentGetStmt.setInt(1, gid);
            ResultSet rs = parentGetStmt.executeQuery();
            while (rs.next()) {
                int groupid = rs.getInt(3);
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
    public void setBuild(String name, boolean build) {
        if (build)
            buildGroups.add(name);
        else
            buildGroups.remove(name);

        PreparedStatementWrapper wrap = null;
        try {
            wrap = buildSetPool.getStatement();
            PreparedStatement buildSetStmt = wrap.getStatement();
            buildSetStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            buildSetStmt.setByte(1, (byte) (build ? 1 : 0));
            buildSetStmt.setInt(2, gid);
            buildSetStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap!=null)wrap.close();
        }

    }

    @Override
    public void setPrefix(String name, String prefix) {
        PreparedStatementWrapper wrap = null;
        try {
            wrap = prefixSetPool.getStatement();
            PreparedStatement prefixSetStmt = wrap.getStatement();
            prefixSetStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            prefixSetStmt.setString(1, prefix);
            prefixSetStmt.setInt(2, gid);
            prefixSetStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap!=null)wrap.close();
        }

    }

    @Override
    public void setSuffix(String name, String suffix) {
        PreparedStatementWrapper wrap = null;
        try {
            wrap = suffixSetPool.getStatement();
            PreparedStatement suffixSetStmt = wrap.getStatement();
            suffixSetStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            suffixSetStmt.setString(1, suffix);
            suffixSetStmt.setInt(2, gid);
            suffixSetStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap!=null)wrap.close();
        }


    }

    @Override
    public void addPermission(String name, String permission) {
        PreparedStatementWrapper wrap = null;
        try {
            wrap = permAddPool.getStatement();
            PreparedStatement permAddStmt = wrap.getStatement();
            permAddStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            permAddStmt.setInt(1, gid);
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
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            permRemStmt.setInt(1, gid);
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
        PreparedStatementWrapper wrap = null;
        try {
            wrap = parentAddPool.getStatement();
            PreparedStatement parentAddStmt = wrap.getStatement();
            parentAddStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            int pid = SqlStorage.getGroup(groupWorld, groupName);
            parentAddStmt.setInt(1, gid);
            parentAddStmt.setInt(2, pid);
            parentAddStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }
    }

    @Override
    public void removeParent(String name, String groupWorld, String groupName) {
        PreparedStatementWrapper wrap = null;
        try {
            wrap = parentRemPool.getStatement();
            PreparedStatement parentRemStmt = wrap.getStatement();
            parentRemStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            int pid = SqlStorage.getGroup(groupWorld, groupName);
            parentRemStmt.setInt(1, gid);
            parentRemStmt.setInt(2, pid);
            parentRemStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }
    }

    @Override
    public Set<String> getGroups() {
        if (groupIds.isEmpty()) {
            PreparedStatementWrapper wrap = null;
            try {
                wrap = groupListPool.getStatement();
                PreparedStatement groupListStmt = wrap.getStatement();
                groupListStmt.clearParameters();
                groupListStmt.setInt(1, worldId);
                ResultSet rs = groupListStmt.executeQuery();
                while (rs.next()) {
                    groupIds.put(rs.getString(1), rs.getInt(2));
                    if(SqlStorage.getDbms()==Dbms.MYSQL && rs.isClosed()) break; //Temp fix for MySQL's stupid implementation of next()
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if(wrap != null) wrap.close();
            }
        }
        return groupIds.keySet();
    }

    @Override
    public String getWorld() {
        return this.groupWorld;
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
        baseGroup = null;
        buildGroups.clear();
    }

    public static void close(){
        parentGetPool.close();
        parentAddPool.close();
        parentRemPool.close();
        permGetPool.close();
        permAddPool.close();
        permRemPool.close();
        dataGetPool.close();
        dataModPool.close();
        dataDelPool.close();
        getGroupPool.close();
        buildSetPool.close();
        prefixSetPool.close();
        suffixSetPool.close();
        trackListPool.close();
        trackGetPool.close();
        if(dbConn!=null)
            try {
                dbConn.close();
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
    public boolean createGroup(String name) {
        if (groupIds.containsKey(name)) {
            try {
                int uid = SqlStorage.getGroup(groupWorld, name);
                groupIds.put(name, uid);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int getWeight(String name) {
        int weight = 0;
        PreparedStatementWrapper wrap = null;
        try {
            wrap = getGroupPool.getStatement();
            PreparedStatement getGroupStmt = wrap.getStatement();
            getGroupStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            getGroupStmt.setInt(1, gid);
            ResultSet rs = getGroupStmt.executeQuery();
            if(rs.next()) return weight;
            weight = rs.getInt(7);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }

        return weight;
    }

    @Override
    public Set<String> getTracks() {
        Set<String> trackSet = new LinkedHashSet<String>();
        PreparedStatementWrapper wrap = null;
        try {
            wrap = trackListPool.getStatement();
            PreparedStatement trackListStmt = wrap.getStatement();
            trackListStmt.clearParameters();
            trackListStmt.setInt(1, worldId);
            ResultSet rs = trackListStmt.executeQuery();
            while(rs.next()) {
                trackSet.add(rs.getString(2));
                if(SqlStorage.getDbms()==Dbms.MYSQL && rs.isClosed()) break; //Temp fix for MySQL's stupid implementation of next()
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }
        return trackSet;
    }

    @Override
    public LinkedList<GroupWorld> getTrack(String track) {
        LinkedList<GroupWorld> trackGroups = new LinkedList<GroupWorld>();
        PreparedStatementWrapper wrap = null;
        try {
            wrap = trackGetPool.getStatement();
            PreparedStatement trackGetStmt = wrap.getStatement();
            trackGetStmt.clearParameters();
            trackGetStmt.setInt(1, worldId);
            trackGetStmt.setString(2, track);
            ResultSet rs = trackGetStmt.executeQuery();
            while(rs.next()) {
                trackGroups.add(new GroupWorld(rs.getString(1),rs.getString(2)));
                if(SqlStorage.getDbms()==Dbms.MYSQL && rs.isClosed()) break; //Temp fix for MySQL's stupid implementation of next()
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }
        return trackGroups;
    }

    @Override
    public String getString(String name, String path) {
        String data = null;
        PreparedStatementWrapper wrap = null;
        try {
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            wrap = dataGetPool.getStatement();
            PreparedStatement dataGetStmt = wrap.getStatement();
            dataGetStmt.clearParameters();
            dataGetStmt.setInt(1, gid);
            dataGetStmt.setString(2, path);
            ResultSet rs = dataGetStmt.executeQuery();
            if (rs.next()) {
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
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            wrap = dataModPool.getStatement();
            PreparedStatement dataModStmt = wrap.getStatement();
            dataModStmt.clearParameters();
            dataModStmt.setString(1, szForm);
            dataModStmt.setInt(2, gid);
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
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            wrap = dataDelPool.getStatement();
            PreparedStatement dataDelStmt = wrap.getStatement();
            dataDelStmt.clearParameters();
            dataDelStmt.setInt(1, gid);
            dataDelStmt.setString(2, path);
            dataDelStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(wrap != null) wrap.close();
        }
    }

    Integer getGroupId(String name) {
        return this.groupIds.get(name);
    }
}

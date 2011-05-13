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

public class SqlGroupStorage implements GroupStorage {

    private int worldId;
    private String groupWorld;
    private String baseGroup = null;
    private Map<String, Integer> groupIds = new HashMap<String, Integer>();
    private Set<String> buildGroups = new HashSet<String>();
    private Map<String, Integer> groupWeights = new HashMap<String, Integer>();
    private Map<String, String> groupPrefixes = new HashMap<String, String>();
    private Map<String, String> groupSuffixes = new HashMap<String, String>();
    private Map<String, Set<String>> groupPermissions = new HashMap<String, Set<String>>();
    private Map<String, LinkedHashSet<GroupWorld>> groupParents = new HashMap<String, LinkedHashSet<GroupWorld>>();
    private Set<String> trackSet = new HashSet<String>();
    private HashMap<String, LinkedList<GroupWorld>> tracks = new HashMap<String, LinkedList<GroupWorld>>();
    private Map<String, Map<String, String>> groupData = new HashMap<String, Map<String, String>>();
    private Connection dbConn;

    private static final String permGetText = "SELECT PrGroupPermissions.permstring FROM PrGroupPermissions WHERE PrGroupPermissions.gid = ?;";
    PreparedStatement permGetStmt;
    private static final String parentGetText = "SELECT * FROM PrGroupInheritance WHERE PrGroupInheritance.childid = ?;";
    PreparedStatement parentGetStmt;

    private static final String getGroupText = "SELECT * FROM PrGroups WHERE PrGroups.worldid = ? AND PrGroups.gid = ?;";
    PreparedStatement getGroupStmt;
    private static final String getGroupsText = "SELECT * FROM PrGroups WHERE PrGroups.worldid = ?;";
    PreparedStatement getGroupsStmt;
    private static final String getBaseText = "SELECT PrGroups.groupname FROM PrWorldBase, PrGroups WHERE PrWorldBase.worldid = ? AND PrGroups.worldid = ? AND PrWorldBase.defaultid = PrGroups.gid;";
    PreparedStatement getBaseStmt;

    private static final String permAddText = "INSERT INTO PrGroupPermissions (gid, permstring) VALUES (?,?);";
    PreparedStatement permAddStmt;
    private static final String permRemText = "DELETE FROM PrGroupPermissions WHERE gid = ? AND permstring = ?;";
    PreparedStatement permRemStmt;
    private static final String parentAddText = "INSERT INTO PrGroupInheritance (childid, parentid) VALUES (?,?);";
    PreparedStatement parentAddStmt;
    private static final String parentRemText = "DELETE FROM PrGroupInheritance WHERE childid = ? AND parentid = ?;";
    PreparedStatement parentRemStmt;

    private static final String groupListText = "SELECT groupname, gid FROM PrGroups WHERE worldid = ?;";
    PreparedStatement groupListStmt;
    private static final String groupAddText = "INSERT INTO PrGroups (worldid,groupname) VALUES (?,?);";
    PreparedStatement groupAddStmt;

    private static final String dataGetText = "SELECT * FROM PrGroupData WHERE gid = ? AND path = ?;";
    PreparedStatement dataGetStmt;
    private static final String dataAddText = "INSERT INTO PrGroupData (data, gid, path) VALUES (?,?,?);";
    PreparedStatement dataAddStmt;
    private static final String dataEditText = "UPDATE PrGroupData SET data = ? WHERE gid = ? AND path = ?;";
    PreparedStatement dataEditStmt;
    private static final String dataDelText = "DELETE FROM PrGroupData WHERE gid = ? AND path = ?;";
    PreparedStatement dataDelStmt;

    private static final String buildSetText = "UPDATE PrGroups SET build = ? WHERE gid = ?;";
    PreparedStatement buildSetStmt;
    private static final String prefixSetText = "UPDATE PrGroups SET prefix = ? WHERE gid = ?;";
    PreparedStatement prefixSetStmt;
    private static final String suffixSetText = "UPDATE PrGroups SET suffix = ? WHERE gid = ?;";
    PreparedStatement suffixSetStmt;

    private static final String trackListText = "SELECT * FROM PrTracks WHERE worldid = ?;";
    PreparedStatement trackListStmt;
    private static final String trackGetText = "SELECT PrWorlds.worldname, PrGroups.groupname FROM PrWorlds, PrGroups, PrTracks, PrTrackGroups WHERE PrTrackGroups.trackid = PrTracks.trackid AND PrTracks.worldid = ? AND PrTracks.trackname = ? AND PrGroups.gid = PrTrackGroups.gid AND PrWorlds.worldid = PrGroups.worldid ORDER BY PrTrackGroups.groupOrder;";
    PreparedStatement trackGetStmt;
    // private static final String weightSetText =
    // "UPDATE Groups SET suffix = ? WHERE gid = ?;";
    // PreparedStatement weightSetStmt;

    public SqlGroupStorage(String groupWorld, int id) {
        worldId = id;
        this.groupWorld = groupWorld;
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

        boolean build = false;
        try {
            getGroupStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            getGroupStmt.setInt(1, worldId);
            getGroupStmt.setInt(2, gid);
            ResultSet rs = permGetStmt.executeQuery();
            rs.next();
            build = (rs.getByte(6) != 0);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (build)
            buildGroups.add(name);
        else
            buildGroups.remove(name);

        return build;
    }

    @Override
    public String getPrefix(String name) {
        if (groupPrefixes.containsKey(name))
            return groupPrefixes.get(name);

        String prefix = "";
        try {
            getGroupStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            getGroupStmt.setInt(1, worldId);
            getGroupStmt.setInt(2, gid);
            ResultSet rs = permGetStmt.executeQuery();
            rs.next();
            prefix = rs.getString(4);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        groupPrefixes.put(name, prefix);
        return prefix;
    }

    @Override
    public String getSuffix(String name) {
        if (groupSuffixes.containsKey(name))
            return groupSuffixes.get(name);

        String suffix = "";
        try {
            getGroupStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            getGroupStmt.setInt(1, worldId);
            getGroupStmt.setInt(2, gid);
            ResultSet rs = permGetStmt.executeQuery();
            rs.next();
            suffix = rs.getString(5);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        groupSuffixes.put(name, suffix);
        return suffix;
    }

    @Override
    public Set<String> getPermissions(String name) {
        if (name == null)
            return new HashSet<String>();
        Set<String> permissions = groupPermissions.get(name);
        if (permissions != null)
            return permissions;
        permissions = new HashSet<String>();

        try {
            permGetStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            permGetStmt.setInt(1, gid);
            ResultSet rs = permGetStmt.executeQuery();
            while (rs.next()) {
                permissions.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashSet<String>();
        }

        groupPermissions.put(name, permissions);
        return permissions;
    }

    @Override
    public LinkedHashSet<GroupWorld> getParents(String name) {
        if (name == null)
            return new LinkedHashSet<GroupWorld>();
        LinkedHashSet<GroupWorld> parents = groupParents.get(name);
        if (parents != null)
            return parents;
        parents = new LinkedHashSet<GroupWorld>();

        try {
            parentGetStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            parentGetStmt.setInt(1, gid);
            ResultSet rs = parentGetStmt.executeQuery();
            while (rs.next()) {
                GroupWorld gw = new GroupWorld(rs.getString(1), rs.getString(2));
                parents.add(gw);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new LinkedHashSet<GroupWorld>();
        }
        groupParents.put(name, parents);
        return null;
    }

    @Override
    public void setBuild(String name, boolean build) {
        if (build)
            buildGroups.add(name);
        else
            buildGroups.remove(name);

        try {
            buildSetStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            buildSetStmt.setByte(1, (byte) (build ? 1 : 0));
            buildSetStmt.setInt(2, gid);
            buildSetStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setPrefix(String name, String prefix) {
        groupPrefixes.put(name, prefix);

        try {
            prefixSetStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            prefixSetStmt.setString(1, prefix);
            prefixSetStmt.setInt(2, gid);
            prefixSetStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setSuffix(String name, String suffix) {
        groupSuffixes.put(name, suffix);

        try {
            suffixSetStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            suffixSetStmt.setString(1, suffix);
            suffixSetStmt.setInt(2, gid);
            suffixSetStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addPermission(String name, String permission) {
        if (groupPermissions.get(name) == null)
            groupPermissions.put(name, new HashSet<String>());
        Set<String> perms = groupPermissions.get(name);
        if (perms.contains(permission))
            return;
        perms.add(permission);

        try {
            permAddStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            permAddStmt.setInt(1, gid);
            permAddStmt.setString(2, permission);
            permAddStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removePermission(String name, String permission) {
        if (groupPermissions.get(name) == null)
            groupPermissions.put(name, new LinkedHashSet<String>());
        Set<String> perms = groupPermissions.get(name);
        if (!perms.contains(permission))
            return;
        perms.remove(permission);
        try {
            permRemStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            permRemStmt.setInt(1, gid);
            permRemStmt.setString(2, permission);
            permRemStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addParent(String name, String groupWorld, String groupName) {
        if (groupParents.get(name) == null)
            groupParents.put(name, new LinkedHashSet<GroupWorld>());
        Set<GroupWorld> parents = groupParents.get(name);
        if (parents.contains(groupWorld))
            return;
        parents.add(new GroupWorld(groupWorld, groupName));
        try {
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
        }
    }

    @Override
    public void removeParent(String name, String groupWorld, String groupName) {
        if (groupParents.get(name) == null)
            groupParents.put(name, new LinkedHashSet<GroupWorld>());
        Set<GroupWorld> parents = groupParents.get(name);
        if (parents.contains(groupWorld))
            return;
        parents.remove(new GroupWorld(groupWorld, groupName));
        try {
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
        }
    }

    @Override
    public Set<String> getGroups() {
        if (groupIds.isEmpty()) {
            try {
                groupListStmt.clearParameters();
                groupListStmt.setInt(1, worldId);
                ResultSet rs = groupListStmt.executeQuery();
                while (rs.next()) {
                    groupIds.put(rs.getString(1), rs.getInt(2));
                }
            } catch (SQLException e) {
                e.printStackTrace();
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
        groupWeights.clear();
        baseGroup = null;
        buildGroups.clear();
        groupPrefixes.clear();
        groupSuffixes.clear();
        groupPermissions.clear();
        tracks.clear();
        groupParents.clear();
        try {
            close();
            worldId = SqlStorage.getWorld(groupWorld);
            dbConn = SqlStorage.getConnection();
            permGetStmt = dbConn.prepareStatement(permGetText);
            parentGetStmt = dbConn.prepareStatement(parentGetText);
            permAddStmt = dbConn.prepareStatement(permAddText);
            permRemStmt = dbConn.prepareStatement(permRemText);
            parentAddStmt = dbConn.prepareStatement(parentAddText);
            parentAddStmt = dbConn.prepareStatement(parentRemText);
            groupListStmt = dbConn.prepareStatement(groupListText);
            groupAddStmt = dbConn.prepareStatement(groupAddText);
            dataAddStmt = dbConn.prepareStatement(dataAddText);
            dataDelStmt = dbConn.prepareStatement(dataDelText);
            dataEditStmt = dbConn.prepareStatement(dataEditText);
            dataGetStmt = dbConn.prepareStatement(dataGetText);
            getGroupStmt = dbConn.prepareStatement(getGroupText);
            getGroupsStmt = dbConn.prepareStatement(getGroupsText);
            getBaseStmt = dbConn.prepareStatement(getBaseText);
            buildSetStmt = dbConn.prepareStatement(buildSetText);
            prefixSetStmt = dbConn.prepareStatement(prefixSetText);
            suffixSetStmt = dbConn.prepareStatement(suffixSetText);
            trackGetStmt = dbConn.prepareStatement(trackGetText);
            trackListStmt = dbConn.prepareStatement(trackListText);
            getBaseStmt.setInt(1, worldId);
            getBaseStmt.setInt(2, worldId);
            ResultSet rs = getBaseStmt.executeQuery();
            if (rs.next()) {
                baseGroup = rs.getString(1);
            }

            getGroupsStmt.setInt(1, worldId);
            rs = getGroupsStmt.executeQuery();
            while (rs.next()) {
                int gid = rs.getInt(1);
                String groupName = rs.getString(2);
                // Skip worldId
                String prefix = rs.getString(4);
                String suffix = rs.getString(5);
                boolean build = (rs.getByte(6) != 0);
                int weight = rs.getInt(7);
                groupIds.put(groupName, gid);
                groupPrefixes.put(groupName, prefix);
                groupSuffixes.put(groupName, suffix);
                if (build)
                    buildGroups.add(prefix);
                groupWeights.put(groupName, weight);
            }
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
        if(getGroupStmt!=null)getGroupStmt.close();
        if(getGroupsStmt!=null)getGroupsStmt.close();
        if(getBaseStmt!=null)getBaseStmt.close();
        if(buildSetStmt!=null)buildSetStmt.close();
        if(prefixSetStmt!=null)prefixSetStmt.close();
        if(suffixSetStmt!=null)suffixSetStmt.close();
        if(trackListStmt!=null)trackListStmt.close();
        if(trackGetStmt!=null)trackGetStmt.close();
        if(dbConn!=null)dbConn.close();
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
        if (groupWeights.containsKey(name)) {
            Integer weight = groupWeights.get(name);;
            return weight == null ? 0 : weight;
        }

        int weight = 0;
        try {
            permGetStmt.clearParameters();
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            getGroupStmt.setInt(1, worldId);
            getGroupStmt.setInt(2, gid);
            ResultSet rs = permGetStmt.executeQuery();
            rs.next();
            weight = rs.getInt(7);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        groupWeights.put(name, weight);
        return weight;
    }

    @Override
    public Set<String> getTracks() {
        if(this.trackSet.isEmpty()) {
            try {
                trackListStmt.clearParameters();
                trackListStmt.setInt(1, worldId);
                ResultSet rs = trackListStmt.executeQuery();
                while(rs.next()) {
                    trackSet.add(rs.getString(2));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return trackSet;
    }

    @Override
    public LinkedList<GroupWorld> getTrack(String track) {
        if(this.tracks.containsKey(track)) return tracks.get(track);
        LinkedList<GroupWorld> trackGroups = new LinkedList<GroupWorld>();
        try {
            trackGetStmt.clearParameters();
            trackGetStmt.setInt(1, worldId);
            trackGetStmt.setString(2, track);
            ResultSet rs = trackGetStmt.executeQuery();
            while(rs.next()) {
                trackGroups.add(new GroupWorld(rs.getString(1),rs.getString(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tracks.put(track, trackGroups);
        return trackGroups;
    }

    @Override
    public String getString(String name, String path) {
        if (groupData.get(name) == null)
            groupData.put(name, new HashMap<String, String>());
        String data = groupData.get(name).get(path);
        if (data != null)
            return data;
        try {
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            dataGetStmt.clearParameters();
            dataGetStmt.setInt(1, gid);
            dataGetStmt.setString(2, path);
            ResultSet rs = dataGetStmt.executeQuery();
            if (!rs.next())
                data = "";
            else {
                data = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            data = "";
        }
        if (data == null)
            data = "";
        groupData.get(name).put(path, data);
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
        if (groupData.get(name) == null)
            groupData.put(name, new HashMap<String, String>());
        groupData.get(name).put(path, szForm);

        try {
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            dataGetStmt.clearParameters();
            dataGetStmt.setInt(1, gid);
            dataGetStmt.setString(2, path);
            PreparedStatement addEdit;
            if (dataGetStmt.executeQuery().next())
                addEdit = dataEditStmt;
            else
                addEdit = dataAddStmt;
            addEdit.clearParameters();
            addEdit.setString(1, szForm);
            addEdit.setInt(2, gid);
            addEdit.setString(3, path);
            addEdit.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            data = "";
        }
    }

    @Override
    public void removeData(String name, String path) {
        if (groupData.get(name) == null)
            groupData.put(name, new HashMap<String, String>());
        groupData.get(name).remove(path);

        try {
            int gid = SqlStorage.getGroup(groupWorld, name);
            if (!groupIds.containsKey(name))
                groupIds.put(name, gid);
            dataDelStmt.clearParameters();
            dataDelStmt.setInt(1, gid);
            dataDelStmt.setString(2, path);
            dataDelStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    Integer getGroupId(String name) {
        return this.groupIds.get(name);
    }
}

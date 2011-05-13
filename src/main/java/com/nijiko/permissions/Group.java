package com.nijiko.permissions;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import com.nijiko.data.GroupStorage;
import com.nijiko.data.GroupWorld;

public class Group extends Entry {

    private GroupStorage data;

    Group(ModularControl controller, GroupStorage data, String name,
            String world, boolean create) {
        super(controller, name, world);
        this.data = data;
        if(create)data.createGroup(name);
    }

    public boolean isDefault() {
        return data.isDefault(name);
    }

    @Override
    public EntryType getType() {
        return EntryType.GROUP;
    }

    public String getPrefix() {
        return data.getPrefix(name);
    }

    public String getSuffix() {
        return data.getSuffix(name);
    }

    public void setBuild(final boolean build) {
        data.setBuild(name, build);
    }

    public void setPrefix(final String prefix) {
        data.setPrefix(name, prefix);
    }

    public void setSuffix(final String suffix) {
        data.setSuffix(name, suffix);
    }

    protected boolean canSelfBuild() {
        return data.canBuild(name);
    }

    @Override
    public String toString() {
        return "Group " + name + " in " + world;
    }

    @Override
    public Set<String> getPermissions() {
        return data.getPermissions(name);
    }

    @Override
    public LinkedHashSet<GroupWorld> getRawParents() {
        return data.getParents(name);
    }

    @Override
    public void setPermission(final String permission, final boolean add) {
//        Set<String> permissions = this.getPermissions();
//        String negated = permission.startsWith("-") ? permission.substring(1)
//                : "-" + permission;
//        if (add) {
//            if (permissions.contains(negated)) {
//                data.removePermission(name, negated);
//            }
//            data.addPermission(name, permission);
//        } else {
//            data.removePermission(name, permission);
//            data.addPermission(name, negated);
//        }
        if(add) data.addPermission(name, permission);
        else data.removePermission(name, permission);
    }

    @Override
    public void addParent(Group group) {
        data.addParent(name, group.world, group.name);
    }

    @Override
    public void removeParent(Group group) {
        if (this.inGroup(group.world, group.name))
            data.removeParent(name, group.world, group.name);
    }
    
    public Set<String> getTracks()
    {
        return data.getTracks();
    }
    public GroupWorld getPrevRank(String trackName)
    {
        LinkedList<GroupWorld> track = data.getTrack(trackName);
        if(track != null)
            for(ListIterator<GroupWorld> iter = track.listIterator(); iter.hasNext();)
            {
                GroupWorld gw = iter.next();
                if(gw.getWorld().equals(world)&&gw.getName().equalsIgnoreCase(name))
                {
                    return iter.previous();
                }
            }
        return null;
    }
    
    public GroupWorld getNextRank(String trackName)
    {
        LinkedList<GroupWorld> track = data.getTrack(trackName);
        if(track != null)
            for(ListIterator<GroupWorld> iter = track.listIterator(); iter.hasNext();)
            {
                GroupWorld gw = iter.next();
                if(gw.getWorld().equals(world)&&gw.getName().equalsIgnoreCase(name))
                {
                    if(iter.hasNext()) return iter.next();                    
                }
            }
        return null;
    }
    
    public int getRawWeight()
    {
        return data.getWeight(name);
    }

    @Override
    public void setData(String path, Object newdata) {
        data.setData(name,path,newdata);
    }

    @Override
    public String getRawString(String path) {
        return data.getString(name,path);
    }

    @Override
    public int getRawInt(String path) {
        return data.getInt(name, path);
    }

    @Override
    public boolean getRawBool(String path) {
        return data.getBool(name, path);
    }

    @Override
    public double getRawDouble(String path) {
        return data.getDouble(name, path);
    }

    @Override
    public void removeData(String path) {
        data.removeData(name, path);
    }
}

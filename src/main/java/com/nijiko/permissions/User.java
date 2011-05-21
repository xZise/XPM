package com.nijiko.permissions;

import java.util.LinkedHashSet;
import java.util.Set;

import com.nijiko.data.GroupWorld;
import com.nijiko.data.UserStorage;

public class User extends Entry {
    private UserStorage data;

    User(ModularControl controller, UserStorage data, String name, String world, boolean create) {
        super(controller, name, world);
        this.data = data;
        if(create)data.createUser(name);
    }

    @Override
    public EntryType getType() {
        return EntryType.USER;
    }

    @Override
    public String toString() {
        return "User " + name + " in " + world;
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
    
    
    public void demote(Group group, String track) {
        if(group==null) return;
        if(!this.getParents().contains(group)) return;
        GroupWorld prevRank = group.getPrevRank(track);
        if(prevRank == null) return;
        
        this.removeParent(group);
        if(this.getRawParents().contains(prevRank)) return;
        Group prev = controller.getGroupObject(prevRank.getWorld(), prevRank.getName());
        if(prev==null) return;
        this.addParent(prev);
    }
    
    public void promote(Group group, String track) {
        if(group==null) return;
        if(!this.getParents().contains(group)) return;
        GroupWorld nextRank = group.getNextRank(track);
        if(nextRank==null)return;
        this.removeParent(group);
        if(this.getRawParents().contains(nextRank)) return;
        Group prev = controller.getGroupObject(nextRank.getWorld(), nextRank.getName());
        if (prev == null)
            return;
        this.addParent(prev);
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
    public Integer getRawInt(String path) {
        return data.getInt(name, path);
    }

    @Override
    public Boolean getRawBool(String path) {
        return data.getBool(name, path);
    }

    @Override
    public Double getRawDouble(String path) {
        return data.getDouble(name, path);
    }

    @Override
    public void removeData(String path) {
        data.removeData(name, path);
    }
    
}
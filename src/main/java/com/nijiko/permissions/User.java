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
        Group defaultGroup = controller.getDefaultGroup(world);
        if (defaultGroup != null)
            this.addParent(defaultGroup);
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
    public LinkedHashSet<GroupWorld> getParents() {
        return data.getParents(name);
    }

    @Override
    public void setPermission(final String permission, final boolean add) {
        Set<String> permissions = this.getPermissions();
        String negated = permission.startsWith("-") ? permission.substring(1)
                : "-" + permission;
        if (add) {
            if (permissions.contains(negated)) {
                data.removePermission(name, negated);
            }
            data.addPermission(name, permission);
        } else {
            data.removePermission(name, permission);
            data.addPermission(name, negated);
        }
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
    
    
    public void demote(Group group) {
        if(group==null) return;
        if(!this.getParentGroups().contains(group)) return;
        GroupWorld prevRank = group.getPrevRank();
        if(prevRank == null) return;
        
        this.removeParent(group);
        if(this.getParents().contains(prevRank)) return;
        Group prev = controller.getGroupObject(prevRank.getWorld(), prevRank.getName());
        if(prev==null) return;
        this.addParent(prev);
    }
    
    public void promote(Group group) {
        if(group==null) return;
        if(!this.getParentGroups().contains(group)) return;
        GroupWorld nextRank = group.getNextRank();
        if(nextRank==null)return;
        this.removeParent(group);
        if(this.getParents().contains(nextRank)) return;
        Group prev = controller.getGroupObject(nextRank.getWorld(), nextRank.getName());
        if (prev == null)
            return;
        this.addParent(prev);
    }
    @Override
    public void setData(String path, String newdata) {
        data.setData(name,path,newdata);
    }

    @Override
    public String getData(String path) {
        return data.getData(name,path);
    }
}
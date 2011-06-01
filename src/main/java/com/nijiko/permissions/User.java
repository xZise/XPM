package com.nijiko.permissions;

import java.util.LinkedHashSet;

import com.nijiko.data.GroupWorld;
import com.nijiko.data.Storage;
import com.nijiko.data.UserStorage;

public class User extends Entry {
    private UserStorage data;

    User(ModularControl controller, UserStorage data, String name, String world, boolean create) {
        super(controller, name, world);
        this.data = data;
        if(create) {
            System.out.println("Creating user " + name);
            data.create(name);
        }
    }

    @Override
    public EntryType getType() {
        return EntryType.USER;
    }

    @Override
    public String toString() {
        return "User " + name + " in " + world;
    }
    
    public void demote(Group group, String track) {
        if(group==null) return;
        if(!this.getParents().contains(group)) return;
        GroupWorld prevRank = group.getPrevRank(track);
        if(prevRank == null) return;
        
        if(this.getRawParents().contains(prevRank)) return;
        Group prev = controller.getGroupObject(prevRank.getWorld(), prevRank.getName());
        if(prev==null) return;
        this.removeParent(group);
        this.addParent(prev);
    }
    
    public void promote(Group group, String track) {
        if(group==null) return;
        if(!this.getParents().contains(group)) return;
        GroupWorld nextRank = group.getNextRank(track);
        if(nextRank==null)return;
        if(this.getRawParents().contains(nextRank)) return;
        Group prev = controller.getGroupObject(nextRank.getWorld(), nextRank.getName());
        if (prev == null)
            return;
        this.removeParent(group);
        this.addParent(prev);
    }
    
    @Override
    public LinkedHashSet<Entry> getParents(String world) {
        LinkedHashSet<Entry> parents = super.getParents(world);
        Group def = controller.getDefaultGroup(this.world);
        if(parents.isEmpty() && def != null) parents.add(def);
        return parents;
    }

    @Override
    protected Storage getStorage() {
        return data;
    }
    
    @Override
    public boolean delete() {
        controller.delUsr(world, name);
        return super.delete();
    }
}
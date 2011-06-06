package com.nijiko.permissions;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;

import com.nijiko.data.GroupStorage;
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
    
    public void demote(GroupWorld groupW, String track) {
        if(groupW==null) return;
        if(!this.getRawParents().contains(groupW)) return;
        
        GroupStorage gStore = controller.getGroupStorage(world);
        if(gStore == null)
            return;
        LinkedList<GroupWorld> trackGroups = gStore.getTrack(track);
        if(trackGroups == null)
            return;
        for (ListIterator<GroupWorld> iter = trackGroups.listIterator(); iter.hasNext();) {
            GroupWorld gw = iter.next();
            if (gw.equals(groupW)) {
                iter.previous();
                if(iter.hasPrevious()) {
                    GroupWorld prev = iter.previous();
                    data.removeParent(name, gw.getWorld(), gw.getName());
                    data.addParent(name, prev.getWorld(), prev.getName());
                }
                else
                    iter.next();
            }
        }
    }
    
    public void promote(GroupWorld groupW, String track) {
        if(groupW==null) return;
        if(!this.getRawParents().contains(groupW)) return;
        
        GroupStorage gStore = controller.getGroupStorage(world);
        if(gStore == null)
            return;
        LinkedList<GroupWorld> trackGroups = gStore.getTrack(track);
        if(trackGroups == null)
            return;
        for (ListIterator<GroupWorld> iter = trackGroups.listIterator(); iter.hasNext();) {
            GroupWorld gw = iter.next();
            if (gw.equals(groupW)) {
                if(iter.hasNext()) {
                    GroupWorld next = iter.next();
                    data.removeParent(name, gw.getWorld(), gw.getName());
                    data.addParent(name, next.getWorld(), next.getName());
                }
            }
        }
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
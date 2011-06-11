package com.nijiko.permissions;

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
        if(this.getRawParents().isEmpty()) {
            Group g = controller.getDefaultGroup(world);
            if(g != null) this.addParent(g);
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
        for (ListIterator<GroupWorld> iter = trackGroups.listIterator(trackGroups.size() - 1); iter.hasPrevious();) {
            GroupWorld gw = iter.previous();
            if (gw.equals(groupW)) {
                if(iter.hasPrevious()) {
                    GroupWorld prev = iter.previous();
                    data.removeParent(name, gw.getWorld(), gw.getName());
                    data.addParent(name, prev.getWorld(), prev.getName());
                }
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
    protected Storage getStorage() {
        return data;
    }
    
    @Override
    public boolean delete() {
        controller.delUsr(world, name);
        return super.delete();
    }
}
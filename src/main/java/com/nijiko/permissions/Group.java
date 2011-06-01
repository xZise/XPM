package com.nijiko.permissions;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import com.nijiko.data.GroupStorage;
import com.nijiko.data.GroupWorld;
import com.nijiko.data.Storage;

public class Group extends Entry {

    private GroupStorage data;

    Group(ModularControl controller, GroupStorage data, String name, String world, boolean create) {
        super(controller, name, world);
        this.data = data;
        if (create) {
            System.out.println("Creating group " + name);
            data.create(name);
        }
    }

    public boolean isDefault() {
        return data.isDefault(name);
    }

    @Override
    public EntryType getType() {
        return EntryType.GROUP;
    }

    @Override
    public String toString() {
        return "Group " + name + " in " + world;
    }

    public Set<String> getTracks() {
        return data.getTracks();
    }

    public GroupWorld getPrevRank(String trackName) {
        LinkedList<GroupWorld> track = data.getTrack(trackName);
        if (track != null)
            for (ListIterator<GroupWorld> iter = track.listIterator(); iter.hasNext();) {
                GroupWorld gw = iter.next();
                if (gw.getWorld().equals(world) && gw.getName().equalsIgnoreCase(name)) {
                    iter.previous();
                    if(iter.hasPrevious())
                        return iter.previous();
                    else
                        iter.next();
                }
            }
        return null;
    }

    public GroupWorld getNextRank(String trackName) {
        LinkedList<GroupWorld> track = data.getTrack(trackName);
        if (track != null)
            for (ListIterator<GroupWorld> iter = track.listIterator(); iter.hasNext();) {
                GroupWorld gw = iter.next();
                if (gw.getWorld().equals(world) && gw.getName().equalsIgnoreCase(name)) {
                    if (iter.hasNext())
                        return iter.next();
                }
            }
        return null;
    }

    @Override
    protected Storage getStorage() {
        return data;
    }
    
    @Override
    public boolean delete() {
        controller.delGrp(world, name);
        return super.delete();
    }
}

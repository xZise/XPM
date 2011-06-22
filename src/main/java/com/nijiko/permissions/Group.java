package com.nijiko.permissions;

import java.util.Set;

import com.nijiko.data.GroupStorage;
import com.nijiko.data.GroupWorld;
import com.nijiko.data.Storage;

public class Group extends Entry {

    private GroupStorage data;

    Group(ModularControl controller, GroupStorage data, String name, String world, boolean create) {
        super(controller, name, world);
        this.data = data;
        if (create && !world.equals("?")) {
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

    @Override
    protected Storage getStorage() {
        return data;
    }
    
    @Override
    public boolean delete() {
        controller.delGrp(world, name);
        return super.delete();
    }
    
    public GroupWorld toGroupWorld() {
        return new GroupWorld(world, name);
    }
}

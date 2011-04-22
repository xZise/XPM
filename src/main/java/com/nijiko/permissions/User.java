package com.nijiko.permissions;

import com.nijiko.data.IStorage;

public class User extends Entry
{
    User(ModularControl controller, IStorage data, String name, String world) {
        super(controller, data, name, world);
        Group defaultGroup = controller.getDefaultGroup(world);
        if(defaultGroup != null) this.addParent(defaultGroup);
    }
    @Override
    public EntryType getType() {
        return EntryType.USER;
    }
    @Override
    public String toString()
    {
        return "User " + name + " in " + world;
    }
}
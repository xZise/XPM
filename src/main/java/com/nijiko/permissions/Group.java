package com.nijiko.permissions;


import com.nijiko.data.IStorage;


public class Group extends Entry {
    
    static
    {
        Group.type = EntryType.GROUP;
    }
    
    Group(ModularControl controller, IStorage data, String name, String world) {
        super(controller, data, name, world);
    }
    
    public boolean isDefault()
    {
        return data.isDefault(world, name);
    }

    @Override
    public EntryType getType() {
        return Group.type;
    }

}

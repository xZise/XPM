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

    public String getPrefix() {
        return data.getPrefix(world, name);
    }
    public String getSuffix() {
        return data.getSuffix(world, name);
    }

    public void setBuild(final boolean build) {
        data.setBuild(world, name, type, build);
    }
    public void setPrefix(final String prefix) {
        data.setPrefix(world, name, type,prefix);
    }
    public void setSuffix(final String suffix) {
        data.setSuffix(world, name, type, suffix);
    }

    protected boolean canSelfBuild() {
        return data.canBuild(world, name);
    }
}

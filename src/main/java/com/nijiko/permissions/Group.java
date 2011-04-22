package com.nijiko.permissions;


import com.nijiko.data.IStorage;


public class Group extends Entry {
    

    
    Group(ModularControl controller, IStorage data, String name, String world) {
        super(controller, data, name, world);
    }
    
    public boolean isDefault()
    {
        return data.isDefault(name);
    }

    @Override
    public EntryType getType() {
        return EntryType.GROUP;
    }

    public String getPrefix() {
        return data.getPrefix(name);
    }
    public String getSuffix() {
        return data.getSuffix(name);
    }

    public void setBuild(final boolean build) {
        data.setBuild(name, build);
    }
    public void setPrefix(final String prefix) {
        data.setPrefix(name, prefix);
    }
    public void setSuffix(final String suffix) {
        data.setSuffix(name, suffix);
    }

    protected boolean canSelfBuild() {
        return data.canBuild(name);
    }
}

package com.nijiko.permissions;

import java.util.LinkedHashSet;
import java.util.Set;

import com.nijiko.data.GroupStorage;
import com.nijiko.data.GroupWorld;

public class Group extends Entry {

    private GroupStorage data;

    Group(ModularControl controller, GroupStorage data, String name,
            String world, boolean create) {
        super(controller, name, world);
        this.data = data;
        if(create)data.createGroup(name);
    }

    public boolean isDefault() {
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

    @Override
    public String toString() {
        return "Group " + name + " in " + world;
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
    
    public GroupWorld getPrevRank()
    {
        return data.getPrevRank(name);
    }
    
    public GroupWorld getNextRank()
    {
        return data.getNextRank(name);
    }
    
    public int getRawWeight()
    {
        return data.getWeight(name);
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

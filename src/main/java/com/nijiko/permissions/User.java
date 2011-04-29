package com.nijiko.permissions;

import java.util.Set;

import com.nijiko.data.GroupWorld;
import com.nijiko.data.UserStorage;

public class User extends Entry {
    private UserStorage data;

    User(ModularControl controller, UserStorage data, String name, String world, boolean create) {
        super(controller, name, world);
        this.data = data;
        Group defaultGroup = controller.getDefaultGroup(world);
        if (defaultGroup != null)
            this.addParent(defaultGroup);
        if(create)data.createUser(name);
    }

    @Override
    public EntryType getType() {
        return EntryType.USER;
    }

    @Override
    public String toString() {
        return "User " + name + " in " + world;
    }

    @Override
    public Set<String> getPermissions() {
        return data.getPermissions(name);
    }

    @Override
    public Set<GroupWorld> getParents() {
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
}
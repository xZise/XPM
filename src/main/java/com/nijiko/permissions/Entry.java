package com.nijiko.permissions;

//import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.nijiko.data.IStorage;

public abstract class Entry {

    protected ModularControl controller;
    protected IStorage data;
    protected String name;
    protected String world;
    protected static EntryType type;

   Entry(ModularControl controller, IStorage data, String name, String world) {
        this.controller = controller;
        this.data = data;
        this.name = name;
        this.world = world;
    }
   
    public boolean canBuild() {
        return data.canBuild(world, name, type);
    }
    public String getPrefix() {
        return data.getPrefix(world, name, type);
    }
    public String getSuffix() {
        return data.getSuffix(world, name, type);
    }
    public Set<String> getPermissions() {
        return data.getPermissions(world, name, type);
    }
    public Set<String> getParents() {
        return data.getParents(world, name, type);
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

    
    public void setPermission(final String permission, final boolean add) {
        Set<String> permissions = this.getPermissions();
        String negated = permission.startsWith("-") ? permission.substring(1) : "-" + permission;
        if(add)
        {
            if(permissions.contains(negated))
            {
                data.removePermission(world, name, type, negated);
            }
            data.addPermission(world, name, type, permission);
        }
        else
        {
            data.removePermission(world, name, type, permission);
            data.addPermission(world, name, type, negated);
        }
    }

    public void addPermission(final String permission)
    {
        this.setPermission(permission, true);
    }

    public void removePermission(final String permission)
    {
        this.setPermission(permission, false);
    }

    public boolean hasPermission(String permission)
    {
        Set<String> permissions = this.getPermissions();
        Set<String> groupPermissions = this.getInheritancePermissions();
        if( (permissions == null || permissions.isEmpty()) && (groupPermissions == null || groupPermissions.isEmpty()) ) return false;

        //Do it in +user -> -user -> +group -> -group order
        if(permissions.contains(permission)) return true;
        if(permissions.contains("-" + permission)) return false;
        if(groupPermissions.contains(permission)) return true;
        if(groupPermissions.contains("-" + permission)) return true;



        String[] nodeHierachy = permission.split("\\.");
        //  nodeHierachy = Arrays.copyOfRange(nodeHierachy, 0, nodeHierachy.length);
        String nextNode = "";
        String wild = "";
        String negated = "";
        String relevantNode = permissions.contains("-*") ? (permissions.contains("*") ? "*" : "-*") : "";
        for(String nextLevel : nodeHierachy)
        {
            nextNode += nextLevel + ".";
            wild = nextNode + "*";
            negated = "-" + wild;
            if (permissions.contains(wild)) {
                relevantNode = wild;
                continue;
            }

            if (permissions.contains(negated)) {
                relevantNode = negated;
                continue;
            }

            if (groupPermissions.contains(wild)) {
                relevantNode = wild;
                continue;
            }

            if (groupPermissions.contains(negated)) {
                relevantNode = negated;
                continue;
            } 
        }

        return !relevantNode.startsWith("-");        
    }

    public Set<String> getInheritancePermissions()
    {
        Set<String> permSet = new HashSet<String>();
        Set<Group> groupSet = this.getAncestors();
        for(Group grp: groupSet)
        {
            permSet.addAll(grp.getPermissions());
        }
        return permSet;
    }

    protected Set<Group> getAncestors() {
        Set<Group> groupSet = new HashSet<Group>();
        Queue<Group> queue = new LinkedList<Group>();

        //Start with the direct ancestors
        queue.addAll(controller.getGroups(world, this.getParents()));

        //Poll the queue
        while(queue.peek() != null) {
            Group grp = queue.poll();
            if(grp == null || groupSet.contains(grp)) continue;
            Set<String> parents = grp.getParents();
            if(parents!=null && parents.size() > 0) queue.addAll(controller.getGroups(grp.world, parents));
            groupSet.add(grp);
        }

        return groupSet;
    }

    protected boolean inGroup(String group, Set<Group> checked)
    {
        Set<Group> parents = controller.getGroups(world, getParents());
        for(Group grp : parents)
        {
            if(checked.contains(grp)) continue;
            checked.add(grp);
            if(grp.name.equalsIgnoreCase(group)) return true;
            if(grp.inGroup(group, checked)) return true;
        }
        return false;
    }
    
    public boolean inGroup(String group)
    {
        Set<Group> checked = new HashSet<Group>();
        return this.inGroup(group, checked);
    }
    
    
}


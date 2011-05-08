package com.nijiko.permissions;

//import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.nijiko.data.GroupWorld;

public abstract class Entry {

    protected ModularControl controller;
    protected String name;
    protected String world;

    Entry(ModularControl controller, String name, String world) {
        this.controller = controller;
        this.name = name;
        this.world = world;
    }

    public abstract Set<String> getPermissions();

    public abstract LinkedHashSet<GroupWorld> getParents();

    public abstract void setPermission(final String permission, final boolean add);

    public void addPermission(final String permission) {
        this.setPermission(permission, true);
    }

    public void removePermission(final String permission) {
        this.setPermission(permission, false);
    }

    public abstract void addParent(Group group);

    public abstract void removeParent(Group group);

    public boolean hasPermission(String permission) {
        Set<String> permissions = this.getAllPermissions();
        if (permissions == null || permissions.isEmpty()) {
            // System.out.println("Entry \""+name+"\"'s permissions are empty.");
            return false;
        }

        // Do it in +user -> -user -> +group -> -group order
        if (!permissions.isEmpty()) {
            if (permissions.contains(permission)) {
                // System.out.println("Found direct match for \""+permission+"\" in \""+name+"\".");/
                return true;
            }
            if (permissions.contains("-" + permission)) {
                // System.out.println("Found direct negated match for \""+permission+"\" in \""+name+"\".");
                return false;
            }
        }

        String[] nodeHierachy = permission.split("\\.");
        // nodeHierachy = Arrays.copyOfRange(nodeHierachy, 0,
        // nodeHierachy.length);
        String nextNode = "";
        String wild = "";
        String negated = "";
        String relevantNode = "";
        if (!permissions.isEmpty()) {
            if (permissions.contains("-*")) {
                relevantNode = "-*";
            }
            if (permissions.contains("*")) {
                relevantNode = "*";
            }
        }

        // System.out.println("Relevant node: " +relevantNode);

        for (String nextLevel : nodeHierachy) {
            nextNode += nextLevel + ".";
            wild = nextNode + "*";
            negated = "-" + wild;

            if (!permissions.isEmpty()) {
                if (permissions.contains(wild)) {
                    relevantNode = wild;
                    // System.out.println("Relevant node: " +relevantNode);
                    continue;
                }

                if (permissions.contains(negated)) {
                    relevantNode = negated;
                    // System.out.println("Relevant node: " +relevantNode);
                    continue;
                }
            }
        }

        return !relevantNode.isEmpty() && !relevantNode.startsWith("-");
    }

    public Set<String> getAllPermissions() {
        return getAllPermissions(new LinkedHashSet<Group>());
    }

    protected Set<String> getAllPermissions(LinkedHashSet<Group> chain) {
        Set<String> perms = new HashSet<String>();
        if (chain == null)
            return perms;
        if (this instanceof Group)
            if (chain.contains(this))
                return perms;
            else
                chain.add((Group) this);
        LinkedHashSet<Group> rawParents = getParentGroups();
        LinkedHashSet<Group> parents = new LinkedHashSet<Group>();
        for (Group g : rawParents)
            if (!chain.contains(g))
                parents.add(g);
        rawParents = null;

        for (Group g : parents) {
            perms = resolvePerms(perms, g.getAllPermissions(chain));
        }
        if (this instanceof Group && chain.contains(this))
            chain.remove(this);
        resolvePerms(perms, this.getPermissions());
        return perms;
    }

    protected static Set<String> resolvePerms(Set<String> perms, Set<String> rawPerms) {
        Set<String> newPerms = new HashSet<String>();
        for (String perm : rawPerms) {
            if (perm.isEmpty())
                continue;
            if (perm.endsWith("*")) // Wildcards
            {
                String wild = perm.substring(0, perm.length() - 1);
                String oppWild = perm.startsWith("-") ? wild.substring(1) : "-" + wild;
                wild = null;
                for (Iterator<String> itr = perms.iterator(); itr.hasNext();) {
                    String candidate = itr.next();
                    if (candidate.startsWith(oppWild))
                        itr.remove();
                }
            }

            newPerms.add(perm);
        }
        perms.addAll(newPerms);
        return perms;
    }

    protected LinkedHashSet<Group> getParentGroups() {
        return controller.stringToGroups(getParents());
    }

    public int getWeight()
    {
        Set<Group> groups = this.getAncestors();
        if(this instanceof Group) groups.add((Group) this);
        int maxWeight = 0;
        for(Group group : groups)
        {
            maxWeight = maxWeight < group.getRawWeight() ? group.getRawWeight() : maxWeight;
        }
        return maxWeight;
    }
    
    public Set<Group> getAncestors() {
        Set<Group> groupSet = new HashSet<Group>();
        Queue<Group> queue = new LinkedList<Group>();

        // Start with the direct ancestors or the default group
        Set<Group> parents = getParentGroups();
        if (parents != null && parents.size() > 0)
            queue.addAll(parents);
        else
            queue.add(controller.getDefaultGroup(world));

        // Poll the queue
        while (queue.peek() != null) {
            Group grp = queue.poll();
            if (grp == null || groupSet.contains(grp))
                continue;
            parents = grp.getParentGroups();
            if (parents != null && parents.size() > 0)
                queue.addAll(parents);
            groupSet.add(grp);
        }

        return groupSet;
    }

    protected boolean inGroup(String world, String group, Set<Group> checked) {
        Set<Group> parents = getParentGroups();
        if (parents == null || parents.isEmpty())
            return false;
        for (Group grp : parents) {
            if (checked.contains(grp))
                continue;
            checked.add(grp);
            if (grp.world.equalsIgnoreCase(world) && grp.name.equalsIgnoreCase(group))
                return true;
            if (grp.inGroup(world, group, checked))
                return true;
        }
        return false;
    }

    public boolean inGroup(String world, String group) {
        if (this.getType() == EntryType.GROUP && this.world.equalsIgnoreCase(world) && this.name.equalsIgnoreCase(group))
            return true;
        Set<Group> checked = new HashSet<Group>();
        return this.inGroup(world, group, checked);
    }

    public boolean canBuild() {
        Set<Group> checked = new HashSet<Group>();
        return this.canBuild(checked);
    }

    protected boolean canBuild(Set<Group> checked) {
        Set<Group> parents = getParentGroups();
        if (this instanceof Group) {
            Group g = (Group) this;
            if (g.canSelfBuild())
                return true;
        }
        if (parents == null || parents.isEmpty())
            return false;
        for (Group grp : parents) {
            if (checked.contains(grp))
                continue;
            checked.add(grp);
            if (grp.canBuild(checked))
                return true;
        }
        return false;
    }

//    public Set<String> getGroups() {
//        Set<Group> groupSet = this.getAncestors();
//        Set<String> nameSet = new HashSet<String>();
//        for (Group grp : groupSet) {
//            if (grp != null)
//                nameSet.add(grp.name);
//        }
//        return nameSet;
//    }

    public abstract EntryType getType();

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return "Entry " + name + " in " + world;
    }
    
    public abstract void setData(String path, Object data);
    
    public abstract String getString(String path);
    public abstract int getInt(String path);
    public abstract boolean getBool(String path);
    public abstract double getDouble(String path);
    
    public abstract void removeData(String path);
}

package com.nijiko.permissions;

//import java.util.Arrays;
import java.util.HashSet;
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

    public abstract Set<GroupWorld> getParents();

    public abstract void setPermission(final String permission,
            final boolean add);

    public void addPermission(final String permission) {
        this.setPermission(permission, true);
    }

    public void removePermission(final String permission) {
        this.setPermission(permission, false);
    }

    public abstract void addParent(Group group);

    public abstract void removeParent(Group group);

    public boolean hasPermission(String permission) {
        Set<String> permissions = this.getPermissions();
        Set<String> groupPermissions = this.getInheritancePermissions();
        if ((permissions == null || permissions.isEmpty())
                && (groupPermissions == null || groupPermissions.isEmpty()))
        {
//            System.out.println("Entry \""+name+"\"'s permissions are empty.");
            return false;
        }

        // Do it in +user -> -user -> +group -> -group order
        if (permissions != null && !permissions.isEmpty()) {
            if (permissions.contains(permission))
            {
//                System.out.println("Found direct match for \""+permission+"\" in \""+name+"\".");/
                return true;
            }
            if (permissions.contains("-" + permission))
            {
//                System.out.println("Found direct negated match for \""+permission+"\" in \""+name+"\".");
                return false;
            }
        }
        if (groupPermissions != null && !groupPermissions.isEmpty()) {
            if (groupPermissions.contains(permission))
            {

//                System.out.println("Found direct match for \""+permission+"\" in \""+name+"\" from inherited permissions.");
                return true;
            }
            if (groupPermissions.contains("-" + permission))
            {
//                System.out.println("Found direct match for \""+permission+"\" in \""+name+"\" from inherited permissions.");
                return true;
            }
        }

        String[] nodeHierachy = permission.split("\\.");
        // nodeHierachy = Arrays.copyOfRange(nodeHierachy, 0,
        // nodeHierachy.length);
        String nextNode = "";
        String wild = "";
        String negated = "";
        String relevantNode = "";
        if (groupPermissions != null && !groupPermissions.isEmpty()) {
            if(groupPermissions.contains("-*"))
            {
                relevantNode = "-*";
            }
            if(groupPermissions.contains("*"))
            {
                relevantNode = "*";
            }
        }
//        System.out.println("Relevant node: " +relevantNode);
        if (permissions != null && !permissions.isEmpty()) {
            if(permissions.contains("-*"))
            {
                relevantNode = "-*";
            }
            if(permissions.contains("*"))
            {
                relevantNode = "*";
            }
        }
        
//        System.out.println("Relevant node: " +relevantNode);
        
        for (String nextLevel : nodeHierachy) {
            nextNode += nextLevel + ".";
            wild = nextNode + "*";
            negated = "-" + wild;

            if (permissions != null && !permissions.isEmpty()) {
                if (permissions.contains(wild)) {
                    relevantNode = wild;
//                    System.out.println("Relevant node: " +relevantNode);
                    continue;
                }

                if (permissions.contains(negated)) {
                    relevantNode = negated;
//                    System.out.println("Relevant node: " +relevantNode);
                    continue;
                }
            }

            if (groupPermissions != null && !groupPermissions.isEmpty()) {
                if (groupPermissions.contains(wild)) {
                    relevantNode = wild;
//                    System.out.println("Relevant node: " +relevantNode);
                    continue;
                }

                if (groupPermissions.contains(negated)) {
                    relevantNode = negated;
//                    System.out.println("Relevant node: " +relevantNode);
                    continue;
                }
            }
        }

        return !relevantNode.isEmpty()&&!relevantNode.startsWith("-");
    }

    public Set<String> getInheritancePermissions() {
        Set<String> permSet = new HashSet<String>();
        Set<Group> groupSet = this.getAncestors();
        for (Group grp : groupSet) {
            permSet.addAll(grp.getPermissions());
        }
        return permSet;
    }

    protected Set<Group> getAncestors() {
        Set<Group> groupSet = new HashSet<Group>();
        Queue<Group> queue = new LinkedList<Group>();

        // Start with the direct ancestors or the default group
        Set<Group> parents = controller.stringToGroups(this.getParents());
        if (parents != null && parents.size() > 0)
            queue.addAll(parents);
        else
            queue.add(controller.getDefaultGroup(world));

        // Poll the queue
        while (queue.peek() != null) {
            Group grp = queue.poll();
            if (grp == null || groupSet.contains(grp))
                continue;
            parents = controller.stringToGroups(grp.getParents());
            if (parents != null && parents.size() > 0)
                queue.addAll(parents);
            groupSet.add(grp);
        }

        return groupSet;
    }

    protected boolean inGroup(String world, String group, Set<Group> checked) {
        Set<Group> parents = controller.stringToGroups(getParents());
        if (parents == null || parents.isEmpty())
            return false;
        for (Group grp : parents) {
            if (checked.contains(grp))
                continue;
            checked.add(grp);
            if (grp.world.equalsIgnoreCase(world)
                    && grp.name.equalsIgnoreCase(group))
                return true;
            if (grp.inGroup(world, group, checked))
                return true;
        }
        return false;
    }

    public boolean inGroup(String world, String group) {
        if (this.getType() == EntryType.GROUP
                && this.world.equalsIgnoreCase(world)
                && this.name.equalsIgnoreCase(group))
            return true;
        Set<Group> checked = new HashSet<Group>();
        return this.inGroup(world, group, checked);
    }

    public boolean canBuild() {
        Set<Group> checked = new HashSet<Group>();
        return this.canBuild(checked);
    }

    protected boolean canBuild(Set<Group> checked) {
        Set<Group> parents = controller.stringToGroups(getParents());
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

    public Set<String> getGroups() {
        Set<Group> groupSet = this.getAncestors();
        Set<String> nameSet = new HashSet<String>();
        for (Group grp : groupSet) {
            if (grp != null)
                nameSet.add(grp.name);
        }
        return nameSet;
    }

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
}

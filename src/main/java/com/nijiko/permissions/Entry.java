package com.nijiko.permissions;

//import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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

    public abstract LinkedHashSet<GroupWorld> getRawParents();

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
        StringBuilder nextNode = new StringBuilder(permission.length() + 1);
        StringBuilder wild = new StringBuilder(permission.length() + 2);
        StringBuilder negated = new StringBuilder(permission.length() + 3).append("-");
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
            nextNode.append(nextLevel).append(".");
            wild.append(nextLevel).append(".*");
            negated.append(nextLevel).append(".*");

            if (!permissions.isEmpty()) {
                String wildString = wild.toString();
                String negString = negated.toString();
                if (permissions.contains(wildString)) {
                    relevantNode = wildString;
                    // System.out.println("Relevant node: " +relevantNode);
                    continue;
                }

                if (permissions.contains(negString)) {
                    relevantNode = negString;
                    // System.out.println("Relevant node: " +relevantNode);
                    continue;
                }
            }
        }

        return !relevantNode.isEmpty() && !relevantNode.startsWith("-");
    }

    public Set<String> getAllPermissions() {
        return getAllPermissions(new LinkedHashSet<Entry>());
    }

    protected Set<String> getAllPermissions(LinkedHashSet<Entry> chain) {
        Set<String> perms = new HashSet<String>();
        if (chain == null)
            return perms;
        if (chain.contains(this))
            return perms;
        else
            chain.add(this);
        LinkedHashSet<Entry> rawParents = getParents();
        LinkedHashSet<Entry> parents = new LinkedHashSet<Entry>();
        for (Entry e : rawParents)
            if (!chain.contains(e))
                parents.add(e);
        rawParents = null;

        for (Entry e : parents) {
            perms = resolvePerms(perms, e.getAllPermissions(chain));
        }
        if (chain.contains(this))
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

    public LinkedHashSet<Entry> getParents() {
        LinkedHashSet<Group> groupParents = controller.stringToGroups(getRawParents());
        LinkedHashSet<Entry> parents = new LinkedHashSet<Entry>();
        Entry global = this.getType() == EntryType.USER ? controller.getUserObject("*", name) : controller.getGroupObject("*", name);
        if(global != null) parents.add(global);
        String parentWorld = controller.getWorldParent(world, this.getType() == EntryType.USER);
        if(parentWorld != null) {
            Entry inherited = this.getType() == EntryType.USER ? controller.getUserObject(parentWorld, name) : controller.getGroupObject(parentWorld, name);
            if(inherited != null) parents.add(inherited);
        }
        parents.addAll(groupParents);
        return parents;
    }

    public int getWeight() {
        Integer value = this.recursiveCheck(new EntryVisitor<Integer>() {
            @Override
            public Integer value(Entry e) {
                if(e instanceof Group) {
                    Group g = (Group) e;
                    int val = g.getRawWeight();
                    return val == -1 ? null : val;
                }
                return null;
            }
        });
        return value == null ? -1 : value;
    }

    public Set<Entry> getAncestors() {
        Set<Entry> parentSet = new HashSet<Entry>();
        Queue<Entry> queue = new ArrayDeque<Entry>();

        // Start with the direct ancestors or the default group
        LinkedHashSet<Entry> parents = getParents();
        if (parents != null && parents.size() > 0)
            queue.addAll(parents);
        else
            queue.add(controller.getDefaultGroup(world));

        // Poll the queue
        while (queue.peek() != null) {
            Entry entry = queue.poll();
            if (parentSet.contains(entry))
                continue;
            parents = entry.getParents();
            if (parents != null && parents.size() > 0)
                queue.addAll(parents);
            parentSet.add(entry);
        }

        return parentSet;
    }

    public boolean inGroup(String world, String group) {
        if (this.getType() == EntryType.GROUP && this.world.equalsIgnoreCase(world) && this.name.equalsIgnoreCase(group))
            return true;
        class GroupChecker implements EntryVisitor<Boolean> {
            protected final String world;
            protected final String group;

            GroupChecker(String world, String group) {
                this.world = world;
                this.group = group;
            }

            @Override
            public Boolean value(Entry e) {
                if(e instanceof Group) {
                    Group g = (Group) e;
                    if (g.world != null && g.name != null && g.world.equals(world) && g.name.equalsIgnoreCase(group))
                        return true;
                }
                return null;
            }
        }
        Boolean val = this.recursiveCheck(new GroupChecker(world, group));
        return val == null ? false : val;
    }

    public boolean canBuild() {
        Boolean value = this.recursiveCheck(new EntryVisitor<Boolean>() {
            @Override
            public Boolean value(Entry e) {
                if(e instanceof Group) {
                    Group g = (Group) e;
                    if (g.canSelfBuild())
                        return true;
                }
                return null;
            }
        });
        return value == null ? false : value;
    }

    public <T> T recursiveCheck(EntryVisitor<T> visitor) {
        return recursiveCheck(new HashSet<Entry>(), visitor);
    }
    protected <T> T recursiveCheck(Set<Entry> checked, EntryVisitor<T> visitor) {
        if(checked.contains(this)) return null;
        
        T result = visitor.value(this);
        if (result != null)
            return result;
        
        LinkedHashSet<Entry> parents = getParents();
        if (parents == null || parents.isEmpty())
            return null;
        
        checked.add(this);
        
        for (Entry entry : parents) {
            if (checked.contains(entry))
                continue;
            checked.add(entry);
            result = entry.recursiveCheck(checked, visitor);
            checked.remove(entry);
            if (result != null)
                return result;
        }
        
        checked.remove(this);
        return null;
    }

    public <T> T recursiveCompare(EntryVisitor<T> visitor, Comparator<T> comparator) {
        return recursiveCompare(new HashSet<Entry>(), visitor, comparator);
    }
    protected <T> T recursiveCompare(Set<Entry> checked, EntryVisitor<T> visitor, Comparator<T> comparator) {
        if(checked.contains(this)) return null;
        
        T result = visitor.value(this);
        if (result != null)
            return result;
        
        Set<Entry> parents = getParents();
        if (parents == null || parents.isEmpty())
            return null;
        
        checked.add(this);
        T currentValue = null;
        
        for (Entry e : parents) {
            if (checked.contains(e))
                continue;
            checked.add(e);
            result = e.recursiveCompare(checked, visitor, comparator);
            checked.remove(e);
            if (result != null) {
                if(comparator.compare(result ,currentValue)==1)  currentValue = result;
            }
        }
        
        checked.remove(this);
        return currentValue;
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

    public abstract void setData(String path, Object data);

    public abstract String getRawString(String path, String def);

    public abstract int getRawInt(String path, int def);

    public abstract boolean getRawBool(String path, boolean def);

    public abstract double getRawDouble(String path, double def);

    public abstract void removeData(String path);

    public interface EntryVisitor<T> {
        /**
         * This is the method called by the recursive checker when searching for a value.
         * If the recursion is to be stopped, return a non-null value.
         * 
         * @param g Group to test
         * @return Null if recursion should continue, any applicable value otherwise
         */
        T value(Entry e);
    }

    public int getInt(String path) {
        return getInt(path, new SimpleComparator<Integer>());
    }

    public int getInt(final String path, Comparator<Integer> comparator) {
        return getInt(path,comparator,-1);
    }
    public int getInt(final String path, Comparator<Integer> comparator, final int def) {
        Integer value = this.recursiveCompare(new EntryVisitor<Integer>(){
            @Override
            public Integer value(Entry e) {
                int value = e.getRawInt(path, def);
                if(value != -1) return value;
                return null;
            }}, comparator);
        return value == null ? def : value;
    }
    


    public double getDouble(String path) {
        return getDouble(path, new SimpleComparator<Double>());
    }

    public double getDouble(final String path, Comparator<Double> comparator) {
        return getDouble(path,comparator,-1.0d);
    }
    public double getDouble(final String path, Comparator<Double> comparator, final double def) {
        Double value = this.recursiveCompare(new EntryVisitor<Double>(){
            @Override
            public Double value(Entry e) {
                double value = e.getRawDouble(path, def);
                if(value != -1.0D) return value;
                return null;
            }}, comparator);
        return value == null ? def : value;
    }
    
    public boolean getBool(String path) {
        return getBool(path, new SimpleComparator<Boolean>());
    }

    public boolean getBool(final String path, Comparator<Boolean> comparator) {
        return getBool(path,comparator,false);
    }

    public boolean getBool(final String path, Comparator<Boolean> comparator, final boolean def) {
        Boolean value = this.recursiveCompare(new EntryVisitor<Boolean>(){
            @Override
            public Boolean value(Entry e) {
                boolean value = e.getRawBool(path, def);
                if(value) return value;
                return null;
            }}, comparator);
        return value == null ? def : value;
    }

    public String getString(String path) {
        return getString(path, new SimpleComparator<String>());
    }

    public String getString(final String path, Comparator<String> comparator) {
        return getString(path,comparator,"");
    }

    public String getString(final String path, Comparator<String> comparator, final String def) {
        String value = this.recursiveCompare(new EntryVisitor<String>(){
            @Override
            public String value(Entry e) {
                String value = e.getRawString(path, def);
                if(!value.isEmpty()) return value;
                return null;
            }}, comparator);
        return value == null ? def : value;
    }
    
    //And now to showcase how insane Java generics can get
    /**
     * Simple comparator to order objects by natural ordering
     */
    public static class SimpleComparator<T extends Comparable<T>> implements Comparator<T> {
        @Override
        public int compare(T o1, T o2) {
            if(o1 == null) {
                if(o2 == null) return 0;
                return -1;
            }
            if(o2 == null) return 1;
            return o1.compareTo(o2);
        }
    }
}

package com.nijiko.data;

public class GroupWorld {
    private String world;
    private String group;

    public GroupWorld(String world, String group) {
        super();
        this.world = world;
        this.group = group;
    }
    
    @Override
    public int hashCode() {
        int hashWorld = world != null ? world.hashCode() : 0;
        int hashName = group != null ? group.hashCode() : 0;

        return (hashWorld + hashName) * hashName + hashWorld;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof GroupWorld) {
                GroupWorld otherPair = (GroupWorld) other;
                return 
                ((  this.world == otherPair.world ||
                        ( this.world != null && otherPair.world != null &&
                          this.world.equalsIgnoreCase(otherPair.world))) &&
                 (      this.group == otherPair.group ||
                        ( this.group != null && otherPair.group != null &&
                          this.group.equalsIgnoreCase(otherPair.group))) );
        }

        return false;
    }

    @Override
    public String toString()
    { 
           return "(" + world + ", " + group + ")"; 
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String getName() {
        return group;
    }

    public void setName(String group) {
        this.group = group;
    }
}
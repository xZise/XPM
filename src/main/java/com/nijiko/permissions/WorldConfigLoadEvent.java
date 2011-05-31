package com.nijiko.permissions;

import org.bukkit.event.Event;

public class WorldConfigLoadEvent extends Event {

    private static final long serialVersionUID = -6311691060236595479L;
    private final String world;
    protected WorldConfigLoadEvent(String world) {
        super("WorldConfigLoadEvent");
        this.world = world;
    }
    
    public String getWorld() {
        return world;
    }

}

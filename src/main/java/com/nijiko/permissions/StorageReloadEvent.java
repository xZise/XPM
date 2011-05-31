package com.nijiko.permissions;

import org.bukkit.event.Event;

public class StorageReloadEvent extends Event {

    private static final long serialVersionUID = 3003170497246820469L;

    protected StorageReloadEvent() {
        super("StorageReloadEvent");
    }

}

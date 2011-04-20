package com.nijiko.permissions;

import com.nijiko.data.IStorage;

public class User extends Entry
{
    static
    {
        User.type = EntryType.USER;
    }
    User(ModularControl controller, IStorage data, String name, String world) {
        super(controller, data, name, world);
    }    
}
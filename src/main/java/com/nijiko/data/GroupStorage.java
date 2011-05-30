package com.nijiko.data;

import java.util.LinkedList;
import java.util.Set;

public interface GroupStorage extends Storage {
    public boolean isDefault(String name);

    public Set<String> getTracks();
    public LinkedList<GroupWorld> getTrack(String track);
}

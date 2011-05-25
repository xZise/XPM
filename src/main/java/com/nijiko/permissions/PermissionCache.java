package com.nijiko.permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PermissionCache {
    private Map<Entry, Set<CheckResult>> permCache = new HashMap<Entry, Set<CheckResult>>();
    
    public void cacheResult(CheckResult result) {
        if(result == null) return;
        if(permCache.get(result.getSource()) == null) permCache.put(result.getSource(), new HashSet<CheckResult>());
        permCache.get(result.getSource()).add(result);
    }
    
    public void flushAll() {
        permCache.clear();
    }
    
    public void updatePerms(Entry entry, String node) {
        if(entry == null) return;
        if(node == null) return;
        Set<String> relevant = Entry.relevantPerms(node);
        String wild = node.endsWith(".*") ? node.substring(0, node.length() - 2) : null;
        for(Map.Entry<Entry, Set<CheckResult>> mapEntry : permCache.entrySet()) {
            if(!mapEntry.getKey().isChildOf(entry)) continue;
            for(Iterator<CheckResult> iter = mapEntry.getValue().iterator(); iter.hasNext();) {
                //Check if node is relevant
                CheckResult cached = iter.next();
                String testNode = cached.getMostRelevantNode();
                if(relevant.contains(testNode) || (wild != null && testNode.startsWith(wild))) {
                    cached.invalidate();
                    iter.remove();
                }
            }
        }
    }
    
    public void updateParent(Entry child, Entry parent) {
        if(child == null || parent == null) return;
        Set<String> perms = parent.getAllPermissions();
        for(String perm : perms) {
            updatePerms(child, perm);
        }
    }
}

class CheckResult {
    private final Entry source;
    private final String mostRelevantNode;
    private final Entry checked;
    private final String node;
    private final boolean result;
    private boolean valid;
    
    public CheckResult(Entry source, String mrn, Entry checked, String node, boolean result) {
        this.source = source;
        this.mostRelevantNode = mrn;
        this.checked = checked;
        this.node = node;
        this.result = result;
        this.valid = true;
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        this.valid = false;
    }

    public Entry getSource() {
        return source;
    }

    public String getMostRelevantNode() {
        return mostRelevantNode;
    }

    public Entry getChecked() {
        return checked;
    }

    public String getNode() {
        return node;
    }

    public boolean getResult() {
        return result;
    }
    
    public CheckResult getNegated() {
        if(!valid) return null;
        return new CheckResult(source, mostRelevantNode, checked, node,!result);
    }
}
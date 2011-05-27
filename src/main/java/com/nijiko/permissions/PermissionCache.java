package com.nijiko.permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PermissionCache {
    private Map<Entry, Set<CheckResult>> permCache = new HashMap<Entry, Set<CheckResult>>();
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    
    public void cacheResult(CheckResult result) {
        if(result == null) return;
        rwl.readLock().lock();
        if(permCache.get(result.getSource()) == null) permCache.put(result.getSource(), new HashSet<CheckResult>());
        permCache.get(result.getSource()).add(result);
        rwl.readLock().unlock();
    }
    
    public void flushAll() {
        rwl.writeLock().lock();
        for(Set<CheckResult> val : permCache.values())
            for(CheckResult cr : val)
                cr.invalidate();
        permCache.clear();
        rwl.writeLock().unlock();
    }
    
    public void updatePerms(Entry entry, String node) {
        if(entry == null) return;
        if(node == null) return;
        boolean alreadyLocked = rwl.isWriteLockedByCurrentThread();
        Set<String> relevant = Entry.relevantPerms(node);
        String wild = node.endsWith(".*") ? node.substring(0, node.length() - 2) : null;
        if(!alreadyLocked) rwl.writeLock().lock();
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
        if(!alreadyLocked) rwl.writeLock().unlock();
    }
    
    public void updateParent(Entry child, Entry parent) {
        if(child == null || parent == null) return;
        Set<String> perms = parent.getAllPermissions();
        rwl.writeLock().lock();
        for(String perm : perms) {
            updatePerms(child, perm);
        }
        rwl.writeLock().unlock();
    }
    
    public void reloadWorld(String world) {
        if(world == null) {
            flushAll();
            return;
        }
        rwl.writeLock().lock();
        for(Map.Entry<Entry, Set<CheckResult>> mapEntry : permCache.entrySet())
            if(mapEntry.getKey().getWorld().endsWith(world))
                for(CheckResult cr : mapEntry.getValue())
                    cr.invalidate();
        rwl.writeLock().unlock();
    }
}
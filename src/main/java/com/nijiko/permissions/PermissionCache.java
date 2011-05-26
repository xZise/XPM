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
        for(Set<CheckResult> crSet : permCache.values())
            for(CheckResult cr : crSet)
                cr.invalidate();
        permCache.clear();
        rwl.writeLock().unlock();
    }
    
    public void updatePerms(Entry entry, String node) {
        if(entry == null) return;
        if(node == null) return;
        Set<String> relevant = Entry.relevantPerms(node);
        String wild = node.endsWith(".*") ? node.substring(0, node.length() - 2) : null;
        String negWild = Entry.negationOf(wild);
        boolean alreadyLocked = rwl.writeLock().isHeldByCurrentThread();
        if(!alreadyLocked) rwl.writeLock().lock();
        for(Map.Entry<Entry, Set<CheckResult>> mapEntry : permCache.entrySet()) {
            if(!mapEntry.getKey().isChildOf(entry)) continue;
            for(Iterator<CheckResult> iter = mapEntry.getValue().iterator(); iter.hasNext();) {
                //Check if node is relevant
                CheckResult cached = iter.next();
                String testNode = cached.getMostRelevantNode();
                String negated = Entry.negationOf(testNode);
                if(relevant.contains(testNode) || (wild != null && testNode.startsWith(wild)) || relevant.contains(negated) || (negWild != null && testNode.startsWith(negWild))) {
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
        if(world == null)
            return;
        rwl.writeLock().lock();
        for(Entry e : permCache.keySet()) {
            if(e.getWorld().equals(world)) {
                for(CheckResult cr : permCache.get(e)) {
                    cr.invalidate();
                }
                permCache.remove(e);
            }
        }
        rwl.writeLock().unlock();
    }
}
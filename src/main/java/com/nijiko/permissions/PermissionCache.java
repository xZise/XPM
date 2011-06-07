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
        if (result == null)
            return;
        // System.out.println(result);
        rwl.readLock().lock();
        if (permCache.get(result.getChecked()) == null)
            permCache.put(result.getChecked(), new HashSet<CheckResult>());
        permCache.get(result.getChecked()).add(result);
        rwl.readLock().unlock();
    }

    public void flushAll() {
        rwl.writeLock().lock();
        try {
            for (Set<CheckResult> val : permCache.values())
                for (CheckResult cr : val)
                    cr.invalidate();
            permCache.clear();
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public void updatePerms(Entry entry, String node) {
        if (entry == null)
            return;
        if (node == null)
            return;
        rwl.writeLock().lock();
        try {
            if (node.equals("*") || node.equals("-*")) {
                for (Map.Entry<Entry, Set<CheckResult>> mapEntry : permCache.entrySet()) {
                    if (!mapEntry.getKey().isChildOf(entry))
                        continue;
                    for (Iterator<CheckResult> iter = mapEntry.getValue().iterator(); iter.hasNext();) {
                        iter.next().invalidate();
                        iter.remove();
                    }
                }
            } else {
                Set<String> relevant = Entry.relevantPerms(node);
                String wild = node.endsWith(".*") ? node.substring(0, node.length() - 2) : null;
                for (Map.Entry<Entry, Set<CheckResult>> mapEntry : permCache.entrySet()) {
                    if (!mapEntry.getKey().isChildOf(entry))
                        continue;
                    for (Iterator<CheckResult> iter = mapEntry.getValue().iterator(); iter.hasNext();) {
                        // Check if node is relevant
                        CheckResult cached = iter.next();
                        String testNode = cached.getNode();
                        if (relevant.contains(testNode) || (wild != null && (testNode.startsWith(wild) || Entry.negationOf(testNode).startsWith(wild)))) {
                            cached.invalidate();
                            iter.remove();
                        }
                    }
                }
            }
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public void updateParent(Entry child, Entry parent) {
        if (child == null || parent == null)
            return;
        Set<String> perms = parent.getAllPermissions();
        rwl.writeLock().lock();
        try {
            for (String perm : perms) {
                updatePerms(child, perm);
            }
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public void reloadWorld(String world) {
        if (world == null) {
            flushAll();
            return;
        }
        rwl.writeLock().lock();
        try {
            for (Map.Entry<Entry, Set<CheckResult>> mapEntry : permCache.entrySet())
                if (mapEntry.getKey().getWorld().equals(world))
                    for (CheckResult cr : mapEntry.getValue())
                        cr.invalidate();
        } finally {
            rwl.writeLock().unlock();
        }
    }
}

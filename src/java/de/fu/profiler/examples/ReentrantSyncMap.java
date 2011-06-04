package de.fu.profiler.examples;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantSyncMap implements IMap {
    private final Map map;
    private final ReentrantLock lock = new ReentrantLock(false);

    public ReentrantSyncMap(Map map) {
        this.map = map;
    }

    public Object put(Object key, Object value) {
        lock.lock();
        try {
            return map.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public Object get(Object key) {
        lock.lock();
        try {
            return map.get(key);
        } finally {
            lock.unlock();
        }
    }
    
    public boolean containsKey(Object key) {
        lock.lock();
        try {
            return map.containsKey(key);
        } finally {
            lock.unlock();
        }
    }
    
    public int size() {
        lock.lock();
        try {
            return map.size();
        } finally {
            lock.unlock();
        }
    }
}


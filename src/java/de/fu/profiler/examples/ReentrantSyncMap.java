package de.fu.profiler.examples;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantSyncMap<Key, Value> implements IMap<Key, Value> {
    private final Map<Key, Value> map;
    private final ReentrantLock lock = new ReentrantLock(false);

    public ReentrantSyncMap(Map<Key, Value> map) {
        this.map = map;
    }

    public Value put(Key key, Value value) {
        lock.lock();
        try {
            return map.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public Value get(Object key) {
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


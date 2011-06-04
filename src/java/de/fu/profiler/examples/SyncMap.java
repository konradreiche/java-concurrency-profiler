package de.fu.profiler.examples;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SyncMap implements IMap {
    private final Map map;

    public SyncMap(Map map) {
        this.map = map;
    }

    public synchronized Object put(Object key, Object value) {
         return map.put(key, value);
    }
     
    public synchronized Object get(Object key) {
       return map.get(key);
    }
    
    public synchronized boolean containsKey(Object key) {
       return map.containsKey(key); 
    }
   
    public synchronized int size() {
        return map.size();
    }
}


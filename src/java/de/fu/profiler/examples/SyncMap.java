package de.fu.profiler.examples;

import java.util.Map;

public class SyncMap<Key, Value> implements IMap<Key, Value> {
    private final Map<Key, Value> map;

    public SyncMap(Map<Key, Value> map) {
        this.map = map;
    }

    public synchronized Value put(Key key, Value value) {
         return map.put(key, value);
    }
     
    public synchronized Value get(Key key) {
       return map.get(key);
    }
    
    public synchronized boolean containsKey(Object key) {
       return map.containsKey(key); 
    }
   
    public synchronized int size() {
        return map.size();
    }
}


package de.fu.profiler.examples;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteMap implements IMap {
    private final Map map;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock r = lock.readLock();
    private final Lock w = lock.writeLock();

    public ReadWriteMap(Map map) {
        this.map = map;
    }

    /* (non-Javadoc)
	 * @see p.IMap#put(java.lang.Object, java.lang.Object)
	 */
    public Object put(Object key, Object value) {
        w.lock();
        try {
            return map.put(key, value);
        } finally {
            w.unlock();
        }
    }
    // Do the same for remove(), putAll(), clear()

    /* (non-Javadoc)
	 * @see p.IMap#get(java.lang.Object)
	 */
    public Object get(Object key) {
        r.lock();
        try {
            return map.get(key);
        } finally {
            r.unlock();
        }
    }
    
    /* (non-Javadoc)
	 * @see p.IMap#containsKey(java.lang.Object)
	 */
    public boolean containsKey(Object key) {
        r.lock();
        try {
            return map.containsKey(key);
        } finally {
            r.unlock();
        }
    }
    
    public int size() {
        r.lock();
        try {
            return map.size();
        } finally {
            r.unlock();
        }
    }
}


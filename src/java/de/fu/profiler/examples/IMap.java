package de.fu.profiler.examples;

public interface IMap {

	public abstract Object put(Object key, Object value);

	public abstract Object get(Object key);

	public abstract int size();
	
	public abstract boolean containsKey(Object key);

}
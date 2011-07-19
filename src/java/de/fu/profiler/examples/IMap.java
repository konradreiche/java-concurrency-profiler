package de.fu.profiler.examples;

public interface IMap<Key,Value> {

	public abstract Value put(Key key, Value value);

	public abstract Value get(Key key);

	public abstract int size();
	
	public abstract boolean containsKey(Key key);

}
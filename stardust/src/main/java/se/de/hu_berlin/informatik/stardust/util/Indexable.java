package se.de.hu_berlin.informatik.stardust.util;

import java.util.Map;

public interface Indexable<T> {

	public T getOriginalFromIdentifier(String identifier);
	
	public T getOriginalFromIndexedIdentifier(String identifier, Map<Integer,String> map);
	
	public String getIndexedIdentifier(T original, Map<String,Integer> map);
	
}

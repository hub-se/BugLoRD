package se.de.hu_berlin.informatik.stardust.util;

import java.util.Map;

import se.de.hu_berlin.informatik.utils.miscellaneous.FromString;

public interface Indexable<T> extends FromString<T> {

	public T getOriginalFromIndexedIdentifier(String identifier, Map<Integer,String> map) throws IllegalArgumentException;
	
	public String getIndexedIdentifier(T original, Map<String,Integer> map);
	
}

package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MappingWrapper<T> {
	
	final private List<T> mappings;

	@SafeVarargs
	public MappingWrapper(T... mappings) {
		this.mappings = new ArrayList<>(mappings.length);
		for (T mapping : mappings) {
			this.mappings.add(mapping);
		}
	}
	
	public MappingWrapper(List<T> mappings) {
		this.mappings = mappings;
	}
	
	public MappingWrapper(int capacity) {
		this.mappings = new ArrayList<>(capacity);
	}
	
	public MappingWrapper() {
		this.mappings = new ArrayList<>();
	}
	
	public int getNumberOfMappings() {
		return mappings.size();
	}
	
	public List<T> getMappings() {
		return mappings;
	}

	public void addMapping(T mapping) {
		mappings.add(mapping);
	}
	
	public void addMappings(Collection<? extends T> mapping) {
		mappings.addAll(mapping);
	}
	
	@Override
	public String toString() {
		String result = "";
		for (T token : mappings) {
			result += token.toString();
		}
		return result;
	}
}

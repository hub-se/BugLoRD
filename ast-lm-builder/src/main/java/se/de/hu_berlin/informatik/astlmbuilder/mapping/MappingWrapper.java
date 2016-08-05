package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.ArrayList;
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
	
	public int getNumberOfMappings() {
		return mappings.size();
	}
	
	public List<T> getMappings() {
		return mappings;
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

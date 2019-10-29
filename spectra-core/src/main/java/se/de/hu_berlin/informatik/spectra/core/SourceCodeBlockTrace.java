package se.de.hu_berlin.informatik.spectra.core;

import java.util.Map;

import se.de.hu_berlin.informatik.spectra.util.Indexable;
import se.de.hu_berlin.informatik.spectra.util.Shortened;

public class SourceCodeBlockTrace implements Shortened, Comparable<SourceCodeBlockTrace>, Indexable<SourceCodeBlockTrace>, Cloneable {

	public final static String IDENTIFIER_SEPARATOR_CHAR = "|";
	public final static String UNKNOWN_ELEMENT = "_";
	
	public static final SourceCodeBlockTrace DUMMY = new SourceCodeBlockTrace(SourceCodeBlock.DUMMY);
	
	private final SourceCodeBlock[] trace;
	
//	private final String immutableIdentifier;
	private final int immutableHashCode;
	
	public SourceCodeBlockTrace(SourceCodeBlock... blocks) {
		this.trace = blocks;
		//we can also store the hashCode now
		int immutableHashCode =  31 * (527 + blocks.length);
		for (SourceCodeBlock block : blocks) {
			immutableHashCode = immutableHashCode * 31 + block.hashCode();
		}
		this.immutableHashCode = immutableHashCode;
	}
	
	public static SourceCodeBlockTrace getNewBlockFromString(String identifier) throws IllegalArgumentException {
		String[] elements = identifier.split(IDENTIFIER_SEPARATOR_CHAR);
		SourceCodeBlock[] trace = new SourceCodeBlock[elements.length];
		for (int i = 0; i < elements.length; i++) {
			trace[i] = SourceCodeBlock.getNewBlockFromString(elements[i]);
		}
		return new SourceCodeBlockTrace(trace);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (SourceCodeBlock block : trace) {
			if (first) {
				first = false;
			} else {
				builder.append(IDENTIFIER_SEPARATOR_CHAR);
			}
			builder.append(block.toString());
		}
		return builder.toString();
	}
	
	public String toCompressedString() {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (SourceCodeBlock block : trace) {
			if (first) {
				first = false;
			} else {
				builder.append(IDENTIFIER_SEPARATOR_CHAR);
			}
			builder.append(block.toCompressedString());
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return immutableHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SourceCodeBlockTrace) {
			SourceCodeBlockTrace o = (SourceCodeBlockTrace) obj;
			if (this.trace.length != o.trace.length) {
				return false;
			}
			for (int i = 0; i < this.trace.length; ++i) {
				if (!this.trace[i].equals(o.trace[i])) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(SourceCodeBlockTrace o) {
		if (this.trace.length == 0 && o.trace.length == 0) {
			return 0;
		}
		if (this.trace.length == 0 && o.trace.length != 0) {
			return -1;
		}
		if (this.trace.length != 0 && o.trace.length == 0) {
			return 1;
		}
		// first element in the trace decides the order...
		return this.trace[0].compareTo(o.trace[0]);
	}

	@Override
	public SourceCodeBlockTrace getOriginalFromIndexedIdentifier(String identifier, Map<Integer, String> map) throws IllegalArgumentException {
		String[] elements = identifier.split(SourceCodeBlockTrace.IDENTIFIER_SEPARATOR_CHAR);
		SourceCodeBlock[] trace = new SourceCodeBlock[elements.length];
		for (int i = 0; i < elements.length; i++) {
			trace[i] = SourceCodeBlock.DUMMY.getOriginalFromIndexedIdentifier(elements[i], map);
		}
		return new SourceCodeBlockTrace(trace);
	}

	@Override
	public String getIndexedIdentifier(SourceCodeBlockTrace original, Map<String, Integer> map) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (SourceCodeBlock block : original.trace) {
			if (first) {
				first = false;
			} else {
				builder.append(IDENTIFIER_SEPARATOR_CHAR);
			}
			builder.append(SourceCodeBlock.DUMMY.getIndexedIdentifier(block, map));
		}
		return builder.toString();
	}

	@Override
	public SourceCodeBlockTrace getFromString(String identifier) throws IllegalArgumentException {
		return getNewBlockFromString(identifier);
	}

	@Override
	public String getShortIdentifier() throws IllegalArgumentException {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (SourceCodeBlock block : trace) {
			if (first) {
				first = false;
			} else {
				builder.append(IDENTIFIER_SEPARATOR_CHAR);
			}
			builder.append(block.getShortIdentifier());
		}
		return builder.toString();
	}
	
	@Override
	public SourceCodeBlockTrace clone() {
		SourceCodeBlock[] clonedTrace = new SourceCodeBlock[trace.length];
		for (int i = 0; i < trace.length; i++) {
			clonedTrace[i] = trace[i].clone();
		}
		return new SourceCodeBlockTrace(clonedTrace);
	}
}

package se.de.hu_berlin.informatik.spectra.core;

import java.util.Map;

import se.de.hu_berlin.informatik.spectra.core.Node.NodeType;
import se.de.hu_berlin.informatik.spectra.util.Indexable;
import se.de.hu_berlin.informatik.spectra.util.Shortened;

public class SourceCodeBlock implements Shortened, Comparable<SourceCodeBlock>, Indexable<SourceCodeBlock>, Cloneable {

	public final static String IDENTIFIER_SEPARATOR_CHAR = ":";
	public final static String UNKNOWN_ELEMENT = "_";
	
	public static final SourceCodeBlock DUMMY = new SourceCodeBlock(UNKNOWN_ELEMENT, 
			UNKNOWN_ELEMENT, UNKNOWN_ELEMENT, -1, -1, NodeType.NORMAL);
	
	private final String packageName;
	private final String filePath;
	private final String methodName;
	private final NodeType nodeType;
	private int lineNumberStart;
	
	private int lineNumberEnd;
	
	private final String immutableIdentifier;
	private final int immutableHashCode;
	
	public SourceCodeBlock(String packageName, String className, String methodName, int lineNumber, NodeType nodeType) {
		this(packageName, className, methodName, lineNumber, lineNumber, nodeType);
	}
	
	public SourceCodeBlock(String packageName, String filePath, String methodName, 
			int lineNumberStart, int lineNumberEnd, NodeType nodeType) {
		this.packageName = packageName;
		this.filePath = filePath;
		this.methodName = methodName;
		this.lineNumberStart = lineNumberStart;
		this.lineNumberEnd = lineNumberEnd;
		this.nodeType = nodeType;
		//that's the immutable part (without the end line number)
		this.immutableIdentifier = this.packageName + IDENTIFIER_SEPARATOR_CHAR + 
				this.filePath + IDENTIFIER_SEPARATOR_CHAR + 
				this.methodName + IDENTIFIER_SEPARATOR_CHAR + 
				this.lineNumberStart;
		//we can also store the hashCode now
		this.immutableHashCode =  31 * (527 + this.filePath.hashCode()) + this.lineNumberStart + this.nodeType.ordinal();
	}
	
	public static SourceCodeBlock getNewBlockFromString(String identifier) throws IllegalArgumentException {
		String[] elements = identifier.split(IDENTIFIER_SEPARATOR_CHAR);
		if (elements.length == 5) { // package:path/to/Class.java:method:startLine#:endLine#
			return new SourceCodeBlock(elements[0], elements[1], elements[2], 
					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), 
					NodeType.NORMAL);
		} else if (elements.length == 6) { // package:path/to/Class.java:method:startLine#:endLine#:#type
			return new SourceCodeBlock(elements[0], elements[1], elements[2], 
					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), 
					NodeType.values()[Integer.valueOf(elements[5])]);
		} else if (elements.length == 7) { // package:path/to/Class.java:method:startLine#:endLine#:coveredStatements#:#type
			return new SourceCodeBlock(elements[0], elements[1], elements[2], 
					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), 
					NodeType.values()[Integer.valueOf(elements[6])]);
		} else if (elements.length == 3) { // path/to/Class.java:line#:#type
			return new SourceCodeBlock(UNKNOWN_ELEMENT, elements[0], UNKNOWN_ELEMENT, 
					Integer.valueOf(elements[1]), Integer.valueOf(elements[1]), 
					NodeType.values()[Integer.valueOf(elements[2])]);
		} else {
			throw new IllegalArgumentException("Wrong input format: '" + identifier + "'.");
		}
	}
	
	public NodeType getNodeType() {
		return nodeType;
	}

	public int getStartLineNumber() {
		return lineNumberStart;
	}

	public int getEndLineNumber() {
		return lineNumberEnd;
	}

	public void setLineNumberStart(int lineNumberStart) {
		this.lineNumberStart = lineNumberStart;
	}

	public void setLineNumberEnd(int lineNumberEnd) {
		this.lineNumberEnd = lineNumberEnd;
	}
	
	public int getNumberOfCoveredLines() {
		return this.lineNumberEnd - this.lineNumberStart + 1;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public String getPackageName() {
		return packageName;
	}

	@Override
	public String toString() {
		return immutableIdentifier + IDENTIFIER_SEPARATOR_CHAR + lineNumberEnd + IDENTIFIER_SEPARATOR_CHAR + nodeType.ordinal();// + IDENTIFIER_SEPARATOR_CHAR + getNumberOfCoveredLines();
	}
	
	public String toCompressedString() {
		if (lineNumberStart == lineNumberEnd) {
			return this.filePath + IDENTIFIER_SEPARATOR_CHAR + lineNumberStart + IDENTIFIER_SEPARATOR_CHAR + nodeType.ordinal();
		} else {
			return this.filePath + IDENTIFIER_SEPARATOR_CHAR + lineNumberStart + '-' + lineNumberEnd + IDENTIFIER_SEPARATOR_CHAR + nodeType.ordinal();
		}
	}

	@Override
	public int hashCode() {
		return immutableHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		//two objects may not technically be equal with this implementation
		//but it is necessary to make sure that the hash code stays the same
		//even if the end line number changes;
		//this way, objects can still be found in hash maps, even after changing
		//the referred fields;
		//in a spectra, there should not be two different nodes with equal starting
		//line numbers that differ in other fields, anyway
		//edit: only use class file path and start line number and ignore the rest
		if (obj instanceof SourceCodeBlock) {
			SourceCodeBlock o = (SourceCodeBlock) obj;
			return this.getStartLineNumber() == o.getStartLineNumber()
					&& this.getFilePath().equals(o.getFilePath())
					&& this.getNodeType().equals(o.getNodeType());
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(SourceCodeBlock o) {
		//if class file paths are equal, the start line number decides the order
		if (this.getFilePath().equals(o.getFilePath())) {
			// if start lines are equal, the node type decides the order
			if (this.getStartLineNumber() == o.getStartLineNumber()) {
				return Integer.compare(this.getNodeType().ordinal(), o.getNodeType().ordinal());
			} else {
				return Integer.compare(this.getStartLineNumber(), o.getStartLineNumber());
			}
		} else {
			return this.getFilePath().compareTo(o.getFilePath());
		}
	}

	@Override
	public SourceCodeBlock getOriginalFromIndexedIdentifier(String identifier, Map<Integer, String> map) throws IllegalArgumentException {
		String[] elements = identifier.split(SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
		if (elements.length == 5) {
			return new SourceCodeBlock(map.get(Integer.valueOf(elements[0])), 
					map.get(Integer.valueOf(elements[1])), map.get(Integer.valueOf(elements[2])), 
					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), 
					NodeType.NORMAL);
		} else if (elements.length == 6) {
			return new SourceCodeBlock(map.get(Integer.valueOf(elements[0])), 
					map.get(Integer.valueOf(elements[1])), map.get(Integer.valueOf(elements[2])), 
					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), 
					NodeType.values()[Integer.valueOf(elements[5])]);
		} else if (elements.length == 7) {
			return new SourceCodeBlock(map.get(Integer.valueOf(elements[0])), 
					map.get(Integer.valueOf(elements[1])), map.get(Integer.valueOf(elements[2])), 
					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), 
					NodeType.values()[Integer.valueOf(elements[6])]);
		} else if (elements.length == 3) {
			return new SourceCodeBlock(UNKNOWN_ELEMENT, map.get(Integer.valueOf(elements[0])), UNKNOWN_ELEMENT, 
					Integer.valueOf(elements[1]), Integer.valueOf(elements[1]), 
					NodeType.values()[Integer.valueOf(elements[2])]);
		} else {
			throw new IllegalArgumentException("Wrong input format: '" + identifier + "'.");
		}
	}

	@Override
	public String getIndexedIdentifier(SourceCodeBlock original, Map<String, Integer> map) {

		//		Log.out(SpectraUtils.class, builder.toString());
		return map.computeIfAbsent(original.getPackageName(), k -> map.size()) + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR +
				map.computeIfAbsent(original.getFilePath(), k -> map.size()) + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR +
				map.computeIfAbsent(original.getMethodName(), k -> map.size()) + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR +
				original.getStartLineNumber() + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR +
				original.getEndLineNumber() + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR + original.nodeType.ordinal();
	}

	@Override
	public SourceCodeBlock getFromString(String identifier) throws IllegalArgumentException {
		return getNewBlockFromString(identifier);
	}

	@Override
	public String getShortIdentifier() throws IllegalArgumentException {
		if (getNumberOfCoveredLines() == 1) {
			return this.filePath + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR + this.lineNumberStart + IDENTIFIER_SEPARATOR_CHAR + nodeType.ordinal();
		} else {
			return this.filePath + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR + this.lineNumberStart + "-" + this.lineNumberEnd + IDENTIFIER_SEPARATOR_CHAR + nodeType.ordinal();
		}
	}
	
	@Override
	public SourceCodeBlock clone() {
		return new SourceCodeBlock(this.packageName, this.filePath, this.methodName, 
				this.lineNumberStart, this.lineNumberEnd, this.nodeType);
	}
}

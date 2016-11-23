package se.de.hu_berlin.informatik.stardust.localizer;

import java.util.Map;

import se.de.hu_berlin.informatik.stardust.util.Indexable;

public class SourceCodeBlock implements Comparable<SourceCodeBlock>, Indexable<SourceCodeBlock> {

	public final static String IDENTIFIER_SEPARATOR_CHAR = ":";
	public final static String UNKNOWN_ELEMENT = "_";
	
	public static final SourceCodeBlock DUMMY = new SourceCodeBlock(UNKNOWN_ELEMENT, UNKNOWN_ELEMENT, UNKNOWN_ELEMENT, -1, -1, -1);
	
	private final String packageName;
	private final String className;
	private final String methodName;
	private int lineNumberStart;
	
	private int lineNumberEnd;
	private int numberOfCoveredStatements;
	
	private final String immutableIdentifier;
	private final int immutableHashCode;
	
	public SourceCodeBlock(String packageName, String className, String methodName, int lineNumber) {
		this(packageName, className, methodName, lineNumber, lineNumber, 1);
	}
	
	public SourceCodeBlock(String packageName, String className, String methodName, 
			int lineNumberStart, int lineNumberEnd, int numberOfCoveredStatements) {
		this.packageName = packageName;
		this.className = className;
		this.methodName = methodName;
		this.lineNumberStart = lineNumberStart;
		this.lineNumberEnd = lineNumberEnd;
		this.numberOfCoveredStatements = numberOfCoveredStatements;
		//that's the immutable part (without the end line number)
		this.immutableIdentifier = packageName + IDENTIFIER_SEPARATOR_CHAR + 
				className + IDENTIFIER_SEPARATOR_CHAR + 
				methodName + IDENTIFIER_SEPARATOR_CHAR + 
				lineNumberStart + IDENTIFIER_SEPARATOR_CHAR;
		//we can also store the hashCode now
		this.immutableHashCode = immutableIdentifier.hashCode();
		
	}
	
	public static SourceCodeBlock getNewBlockFromString(String identifier) throws IllegalArgumentException {
		String[] elements = identifier.split(IDENTIFIER_SEPARATOR_CHAR);
		if (elements.length == 5) { // package:path/to/Class.java:method:startLine#:endLine#
			return new SourceCodeBlock(elements[0], elements[1], elements[2], 
					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), 1);
		} else if (elements.length == 6) { // package:path/to/Class.java:method:startLine#:endLine#:coveredStatements#
			return new SourceCodeBlock(elements[0], elements[1], elements[2], 
					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), Integer.valueOf(elements[5]));
		} else if (elements.length == 2) { // path/to/Class.java:line#
			return new SourceCodeBlock(UNKNOWN_ELEMENT, elements[0], UNKNOWN_ELEMENT, 
					Integer.valueOf(elements[1]), Integer.valueOf(elements[1]), 1);
		} else {
			throw new IllegalArgumentException("Wrong input format: '" + identifier + "'.");
		}
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
	
	public int getCoveredStatements() {
		return numberOfCoveredStatements;
	}
	
	public void addCoveredStatement() {
		++numberOfCoveredStatements;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getPackageName() {
		return packageName;
	}

	@Override
	public String toString() {
		return immutableIdentifier + lineNumberEnd + IDENTIFIER_SEPARATOR_CHAR + numberOfCoveredStatements;
	}

	@Override
	public int hashCode() {
		return immutableHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		//two objects may not technically be equal with this implementation
		//but it is necessary to make sure that the hash code stays the same
		//even if the end line number changes (or/and the number of covered statements);
		//this way, objects can still be found in hash maps, even after changing
		//the referred fields;
		//in a spectra, there should not be two different nodes with equal starting
		//line numbers that differ in other fields, anyway
		if (obj instanceof SourceCodeBlock) {
			SourceCodeBlock o = (SourceCodeBlock) obj;
			return this.getStartLineNumber() == o.getStartLineNumber()
					&& this.getMethodName().equals(o.getMethodName())
					&& this.getClassName().equals(o.getClassName())
					&& this.getPackageName().equals(o.getPackageName()) 
					//the end line number must not be taken into account here
//					&& this.getEndLineNumber() == o.getEndLineNumber()
					;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(SourceCodeBlock o) {
		//if package names are equal, the class names decide the order
		if (this.getPackageName().equals(o.getPackageName())) {
			//if class names are equal, the method names decide the order
			if (this.getClassName().equals(o.getClassName())) {
				//if method names are equal, too, the line numbers decide the order
				if (this.getMethodName().equals(o.getMethodName())) {
					return Integer.compare(this.getStartLineNumber(), o.getStartLineNumber());
				} else {
					return this.getMethodName().compareTo(o.getMethodName());
				}
			} else {
				return this.getClassName().compareTo(o.getClassName());
			}
		}else {
			return this.getPackageName().compareTo(o.getPackageName());
		}
	}

	@Override
	public SourceCodeBlock getOriginalFromIndexedIdentifier(String identifier, Map<Integer, String> map) throws IllegalArgumentException {
		String[] elements = identifier.split(SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
		if (elements.length == 5) {
			return new SourceCodeBlock(map.get(Integer.valueOf(elements[0])), 
					map.get(Integer.valueOf(elements[1])), map.get(Integer.valueOf(elements[2])), 
					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), 1);
		} else if (elements.length == 6) {
			return new SourceCodeBlock(map.get(Integer.valueOf(elements[0])), 
					map.get(Integer.valueOf(elements[1])), map.get(Integer.valueOf(elements[2])), 
					Integer.valueOf(elements[3]), Integer.valueOf(elements[4]), Integer.valueOf(elements[5]));
		} else if (elements.length == 2) {
			return new SourceCodeBlock(UNKNOWN_ELEMENT, map.get(Integer.valueOf(elements[0])), UNKNOWN_ELEMENT, 
					Integer.valueOf(elements[1]), Integer.valueOf(elements[1]), 1);
		} else {
			throw new IllegalArgumentException("Wrong input format: '" + identifier + "'.");
		}
	}

	@Override
	public String getIndexedIdentifier(SourceCodeBlock original, Map<String, Integer> map) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(map.computeIfAbsent(original.getPackageName(), k -> map.size()) + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
		builder.append(map.computeIfAbsent(original.getClassName(), k -> map.size()) + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
		builder.append(map.computeIfAbsent(original.getMethodName(), k -> map.size()) + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
		builder.append(original.getStartLineNumber() + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
		builder.append(original.getEndLineNumber() + SourceCodeBlock.IDENTIFIER_SEPARATOR_CHAR);
		builder.append(original.getCoveredStatements());
		
//		Log.out(SpectraUtils.class, builder.toString());
		return builder.toString();
	}

	@Override
	public SourceCodeBlock getOriginalFromIdentifier(String identifier) throws IllegalArgumentException {
		return getNewBlockFromString(identifier);
	}
	
	
}

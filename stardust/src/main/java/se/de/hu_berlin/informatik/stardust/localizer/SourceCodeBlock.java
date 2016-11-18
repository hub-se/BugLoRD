package se.de.hu_berlin.informatik.stardust.localizer;

public class SourceCodeBlock implements Comparable<SourceCodeBlock> {

	public final static String IDENTIFIER_SEPARATOR_CHAR = ":";
	
	private final String packageName;
	private final String className;
	private final String methodName;
	private int lineNumberStart;
	private int lineNumberEnd;
	
	private String identifier = null;
	
	public SourceCodeBlock(String packageName, String className, String methodName, int lineNumber) {
		this(packageName, className, methodName, lineNumber, lineNumber);
	}
	
	public SourceCodeBlock(String packageName, String className, String methodName, int lineNumberStart, int lineNumberEnd) {
		this.packageName = packageName;
		this.className = className;
		this.methodName = methodName;
		this.lineNumberStart = lineNumberStart;
		this.lineNumberEnd = lineNumberEnd;
	}
	
	public static SourceCodeBlock getNewBlockFromString(String identifier) {
		String[] elements = identifier.split(IDENTIFIER_SEPARATOR_CHAR);
		assert elements.length == 5;
		return new SourceCodeBlock(elements[0], elements[1], elements[2], 
				Integer.valueOf(elements[3]), Integer.valueOf(elements[4]));
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
		if (identifier == null) {
			identifier = packageName + IDENTIFIER_SEPARATOR_CHAR + 
					className + IDENTIFIER_SEPARATOR_CHAR + 
					methodName + IDENTIFIER_SEPARATOR_CHAR + 
					lineNumberStart + IDENTIFIER_SEPARATOR_CHAR + 
					lineNumberStart;
		}
		return identifier;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SourceCodeBlock) {
			SourceCodeBlock o = (SourceCodeBlock) obj;
			return this.getPackageName().equals(o.getPackageName())
					&& this.getClassName().equals(o.getClassName())
					&& this.getMethodName().equals(o.getMethodName())
					&& this.getStartLineNumber() == o.getStartLineNumber()
					&& this.getEndLineNumber() == o.getEndLineNumber();
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
	
	
}

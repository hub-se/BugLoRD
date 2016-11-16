package se.de.hu_berlin.informatik.stardust.localizer;

public class SourceCodeLine {

	private final String className;
	private final int lineNumber;
	
	private String identifier = null;
	
	public SourceCodeLine(String className, int lineNumber) {
		this.className = className;
		this.lineNumber = lineNumber;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public String toString() {
		if (identifier == null) {
			identifier = className + ":" + lineNumber;
		}
		return identifier;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SourceCodeLine) {
			return this.toString().equals(obj.toString());
		} else {
			return false;
		}
	}
	
	
}

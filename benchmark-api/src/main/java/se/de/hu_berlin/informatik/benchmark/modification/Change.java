package se.de.hu_berlin.informatik.benchmark.modification;


public class Change extends Modification {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6859519610954084374L;

	public Change(int line, String classPath) {
		super(line, classPath);
	}

	public Change(int[] possibleLines, String classPath) {
		super(possibleLines, classPath);
	}

	@Override
	public String toString() {
		return super.toString() + ":CHANGE";
	}
	
}

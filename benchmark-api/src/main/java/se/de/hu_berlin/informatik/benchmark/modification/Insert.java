package se.de.hu_berlin.informatik.benchmark.modification;


public class Insert extends Modification {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8262046119735639352L;

	public Insert(int[] possibleLines, String classPath) {
		super(possibleLines, classPath);
	}
	
	public Insert(int line, String classPath) {
		super(line, classPath);
	}

	@Override
	public String toString() {
		return super.toString() + ":INSERT";
	}
	
}

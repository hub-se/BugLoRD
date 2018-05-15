package se.de.hu_berlin.informatik.benchmark.modification;


public class Delete extends Modification {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5151084012103140605L;

	public Delete(int line, String classPath) {
		super(line, classPath);
	}

	public Delete(int[] possibleLines, String classPath) {
		super(possibleLines, classPath);
	}

	@Override
	public String toString() {
		return super.toString() + ":DELETE";
	}
	
}

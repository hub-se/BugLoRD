package se.de.hu_berlin.informatik.spectra.core.traces;


public class Triplet {

	private int rangeStart;
	private int rangeLength;
	private int count;
	
	public Triplet(int rangeStart, int rangeLength, int count) {
		super();
		this.rangeStart = rangeStart;
		this.rangeLength = rangeLength;
		this.count = count;
	}

	
	public int getRangeStart() {
		return rangeStart;
	}

	
	public int getRangeLength() {
		return rangeLength;
	}

	
	public int getCount() {
		return count;
	}
	
}

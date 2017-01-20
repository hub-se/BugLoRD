package se.de.hu_berlin.informatik.stardust.provider.cobertura;

import net.sourceforge.cobertura.coveragedata.CoverageData;

public class MyLineData implements Comparable<Object>, CoverageData {

	final private int lineNumber;
	private long hits;
	
	public MyLineData(int lineNumber, int hits) {
		this.lineNumber = lineNumber;
		this.hits = hits;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public long getHits() {
		return hits;
	}
	
	synchronized public boolean isCovered() {
		return hits > 0;
	}
	
	@Override
	public int compareTo(Object o) {
		if (!o.getClass().equals(MyLineData.class))
			return Integer.MAX_VALUE;
		return this.lineNumber - ((MyLineData) o).lineNumber;
	}

	@Override
	public double getBranchCoverageRate() {
		return 1d;
	}

	@Override
	public double getLineCoverageRate() {
		return (hits > 0) ? 1 : 0;
	}

	@Override
	public int getNumberOfCoveredBranches() {
		return 0;
	}

	@Override
	public int getNumberOfCoveredLines() {
		return (hits > 0) ? 1 : 0;
	}

	@Override
	public int getNumberOfValidBranches() {
		return 0;
	}

	@Override
	public int getNumberOfValidLines() {
		return 1;
	}

	@Override
	synchronized public void merge(CoverageData coverageData) {
		MyLineData lineData = (MyLineData) coverageData;
		this.hits += lineData.hits;
	}

	public void setHits(long hits) {
		this.hits = hits;
	}
	
}

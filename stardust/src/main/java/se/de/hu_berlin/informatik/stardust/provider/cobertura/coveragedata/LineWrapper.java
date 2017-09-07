package se.de.hu_berlin.informatik.stardust.provider.cobertura.coveragedata;

import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.LineData;

public class LineWrapper {
	
	private LineData lineData = null;
	private MyLineData myLineData = null;

	public LineWrapper(CoverageData coverageData) {
		if (coverageData instanceof MyLineData) {
			myLineData = (MyLineData) coverageData;
		} else if (coverageData instanceof LineData) {
			lineData = (LineData) coverageData;
		}
	}
	
	public int getLineNumber() {
		if (myLineData != null) {
			return myLineData.getLineNumber();
		} else if (lineData != null) {
			return lineData.getLineNumber();
		} else {
			return 0;
		}
	}
	
	public long getHits() {
		if (myLineData != null) {
			return myLineData.getHits();
		} else if (lineData != null) {
			return lineData.getHits();
		} else {
			return 0;
		}
	}
	
	public boolean isCovered() {
		if (myLineData != null) {
			return myLineData.isCovered();
		} else if (lineData != null) {
			return lineData.isCovered();
		} else {
			return false;
		}
	}

	public boolean setHits(long hits) {
		if (myLineData != null) {
			myLineData.setHits(hits);
			return true;
		} else {
			return false;
		}
	}
	
}

package se.de.hu_berlin.informatik.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import se.de.hu_berlin.informatik.benchmark.LineWithFaultInformation;
import se.de.hu_berlin.informatik.benchmark.FaultInformation;
import se.de.hu_berlin.informatik.benchmark.FaultLocations;

/**
 * Create a file that contains faults.
 */
public class SimpleFaultLocations implements FaultLocations {

    /** all fault locations in this file */
    private final Map<Integer, LineWithFaultInformation> lines = new TreeMap<>();

	@Override
	public void addFaultyLine(int lineNo) throws IllegalStateException {
		if (lines.containsKey(lineNo)) {
			throw new IllegalStateException("Entry for line " + lineNo + " already exists.");
		}
		lines.put(lineNo, new SimpleLineWithFaultInformation(lineNo));
	}

	@Override
	public void addFaultyLine(int lineNo, FaultInformation information) throws NullPointerException, IllegalStateException {
		if (lines.containsKey(lineNo)) {
			throw new IllegalStateException("Entry for line " + lineNo + " already exists.");
		}
		if (information == null) {
			throw new NullPointerException("No information given.");
		}
		lines.put(lineNo, new SimpleLineWithFaultInformation(lineNo, information));
	}

	@Override
	public void addFaultyLine(LineWithFaultInformation line) throws NullPointerException, IllegalStateException {
		if (line == null) {
			throw new NullPointerException("No information given.");
		}
		if (lines.containsKey(line.getLineNo())) {
			throw new IllegalStateException("Entry for line " + line.getLineNo() + " already exists.");
		}
		lines.put(line.getLineNo(), line);
	}
	
	@Override
	public List<Integer> getFaultyLineNumbers() {
		 return new ArrayList<>(lines.keySet());
	}

	@Override
	public LineWithFaultInformation getFaultyLine(int lineNo) {
		return lines.get(lineNo);
	}

	@Override
	public List<LineWithFaultInformation> getFaultyLines() {
		return new ArrayList<>(lines.values());
	}

}

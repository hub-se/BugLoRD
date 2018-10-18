package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.cobertura.coveragedata.CoverageData;

public class MyClassData extends ClassData {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1448963625768666802L;

	private Map<String, Set<CoverageData>> coverageMap = new HashMap<>();
	
	private Set<String> methodNamesAndDescriptors = new HashSet<String>();
	
	/**
	 * Each key is a counter Id (array index), stored as an Integer object.
	 * Each value is information about the line, stored as a LineData object.
	 */
	private Map<Integer, MyLineData> counterIdToLineMap = new HashMap<Integer, MyLineData>();

	public Map<Integer, MyLineData> getCounterIdToMyLineDataMap() {
		return counterIdToLineMap;
	}
	
	/**
	 * Each key is a line number, stored as an Integer object.
	 * Each value is information about the line, stored as a LineData object.
	 */
	private Map<Integer, MyLineData> lineNumberToLineMap = new HashMap<Integer, MyLineData>();

	public Map<Integer, MyLineData> getLineNumberToMyLineDataMap() {
		return lineNumberToLineMap;
	}
	
	public MyClassData() {
		super();
	}

	public MyClassData(String name) {
		super(name);
	}

	@Override
	public LineData addLine(int lineNumber, String methodName, String methodDescriptor) {
		//do nothing
		return null;
	}

	public MyLineData addLine(int classLine, String methodName, String methodDescription, int hitCounter) {
		lock.lock();
		try {
			String methodNameAndDescriptor = methodName + methodDescription;
			MyLineData lineData = new MyLineData(classLine, hitCounter);
			coverageMap.computeIfAbsent(methodNameAndDescriptor, k -> new HashSet<>()).add(lineData);
			methodNamesAndDescriptors.add(methodNameAndDescriptor);
			return lineData;
		} finally {
			lock.unlock();
		}
	}
	
	public MyLineData getMyLineData(int lineNumber) {
		lock.lock();
		try {
			return lineNumberToLineMap.get(lineNumber);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public Collection<CoverageData> getLines(String methodNameAndDescriptor) {
		lock.lock();
		try {
			Set<CoverageData> lines = coverageMap.get(methodNameAndDescriptor);
			return lines;
		} finally {
			lock.unlock();
		}
	}
	
	public Set<String> getMethodNamesAndDescriptors() {
		lock.lock();
		try {
			return methodNamesAndDescriptors;
		} finally {
			lock.unlock();
		}
	}

}

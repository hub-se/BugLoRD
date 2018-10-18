package se.de.hu_berlin.informatik.spectra.provider.cobertura.coveragedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.LineData;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.coveragedata.MyLineData;

public class MyClassData extends ClassData {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1448963625768666802L;

	private Map<String, Set<CoverageData>> coverageMap = new HashMap<>();
	
	private Set<String> methodNamesAndDescriptors = new HashSet<String>();

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

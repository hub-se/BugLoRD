package se.de.hu_berlin.informatik.aspectj.frontend;

import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.IBugsUtils;
import se.de.hu_berlin.informatik.benchmark.SimpleFileWithFaultLocations;
import se.de.hu_berlin.informatik.benchmark.FaultLocations;

/**
 * Create a file that contains faults.
 */
public class IBugsFileWithFaultLocations extends SimpleFileWithFaultLocations {
  
    public IBugsFileWithFaultLocations(String name) {
		super(name);
	}
    
	public IBugsFileWithFaultLocations(String name, FaultLocations locations) {
		super(name, locations);
	}

	@Override
    public String getClassName() {
        return IBugsUtils.resolveFileName(getFileName());
    }

}

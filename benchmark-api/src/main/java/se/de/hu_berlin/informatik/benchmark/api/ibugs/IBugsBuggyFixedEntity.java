package se.de.hu_berlin.informatik.benchmark.api.ibugs;

import se.de.hu_berlin.informatik.benchmark.api.AbstractBuggyFixedEntity;

public class IBugsBuggyFixedEntity extends AbstractBuggyFixedEntity {

	public IBugsBuggyFixedEntity(String aProject, String aProjectRoot, String aFixId) {
		super(IBugsEntity.getBuggyIBugsEntity(aProject, aProjectRoot, aFixId), 
				IBugsEntity.getFixedIBugsEntity(aProject, aProjectRoot, aFixId));
	}

}

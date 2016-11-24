package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import se.de.hu_berlin.informatik.benchmark.api.AbstractBuggyFixedEntity;

public class Defects4JBuggyFixedEntity extends AbstractBuggyFixedEntity {
		
	public Defects4JBuggyFixedEntity(String project, String bugId) {
		super(Defects4JEntity.getBuggyDefects4JEntity(project, bugId), 
				Defects4JEntity.getFixedDefects4JEntity(project, bugId));
	}

}

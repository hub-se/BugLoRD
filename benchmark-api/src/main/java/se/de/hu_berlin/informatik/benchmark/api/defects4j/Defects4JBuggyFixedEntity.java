package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import se.de.hu_berlin.informatik.benchmark.api.AbstractBuggyFixedEntity;

public class Defects4JBuggyFixedEntity extends AbstractBuggyFixedEntity {
		
	final private String project;
	final private String bugID;
	
	public static final String SEPARATOR_CHAR = ":";
	
	public Defects4JBuggyFixedEntity(String project, String bugId) {
		super(Defects4JEntity.getBuggyDefects4JEntity(project, bugId), 
				Defects4JEntity.getFixedDefects4JEntity(project, bugId));
		this.project = project;
		this.bugID = bugId;
	}
	
	@Override
	public String getUniqueIdentifier() {
		return project + SEPARATOR_CHAR + bugID;
	}

}

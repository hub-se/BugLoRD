package se.de.hu_berlin.informatik.defects4j.experiments;

public class ExperimentToken {

	final private String bugId;
	final private String project;
	
	public ExperimentToken(String project, String bugId) {
		super();
		this.bugId = bugId;
		this.project = project;
	}
	
	public String getBugId() {
		return bugId;
	}
	
	public String getProject() {
		return project;
	}
	
}

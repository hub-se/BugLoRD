package se.de.hu_berlin.informatik.defects4j.frontend;

import se.de.hu_berlin.informatik.changechecker.ChangeChecker;

public class ChangeWrapper {

	private int start;
	private int end;
	
	private String entityType;
	private String changeType;
	private String significance;
	
	
	
	public ChangeWrapper(int start, int end, String entityType, String changeType, String significance) {
		super();
		this.start = start;
		this.end = end;
		this.entityType = entityType;
		this.changeType = changeType;
		this.significance = significance;
	}



	public int getStart() {
		return start;
	}



	public int getEnd() {
		return end;
	}



	public String getEntityType() {
		return entityType;
	}



	public String getChangeType() {
		return changeType;
	}



	public String getSignificance() {
		return significance;
	}



	@Override
	public String toString() {
		return start + ChangeChecker.SEPARATION_CHAR
				+ end + ChangeChecker.SEPARATION_CHAR
				+ entityType + ChangeChecker.SEPARATION_CHAR
				+ changeType + ChangeChecker.SEPARATION_CHAR
				+ significance;
	}
	
	
	
}

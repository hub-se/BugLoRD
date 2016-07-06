package se.de.hu_berlin.informatik.changechecker;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.SignificanceLevel;
import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

public class ChangeWrapper {

	private int start;
	private int end;
	
	private String entityType;
	private String changeType;
	private SignificanceLevel significance;
	
	public static final String SIGNIFICANCE_NONE = "NONE";
	public static final String SIGNIFICANCE_LOW = "LOW";
	public static final String SIGNIFICANCE_MEDIUM = "MEDIUM";
	public static final String SIGNIFICANCE_HIGH = "HIGH";
	public static final String SIGNIFICANCE_CRUCIAL = "CRUCIAL";
	
	public static final String SIGNIFICANCE_ALL = "ALL";
	
	public ChangeWrapper(int start, int end, String entityType, String changeType, String significance) {
		super();
		this.start = start;
		this.end = end;
		this.entityType = entityType;
		this.changeType = changeType;
		switch(significance) {
		case SIGNIFICANCE_NONE:
			this.significance = SignificanceLevel.NONE;
			break;
		case SIGNIFICANCE_LOW:
			this.significance = SignificanceLevel.LOW;
			break;
		case SIGNIFICANCE_MEDIUM:
			this.significance = SignificanceLevel.MEDIUM;
			break;
		case SIGNIFICANCE_HIGH:
			this.significance = SignificanceLevel.HIGH;
			break;
		case SIGNIFICANCE_CRUCIAL:
			this.significance = SignificanceLevel.CRUCIAL;
			break;
		default:
			this.significance = SignificanceLevel.NONE;
			Misc.err(this, "Could not parse significance level '%s'.", significance);
		}
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



	public SignificanceLevel getSignificance() {
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

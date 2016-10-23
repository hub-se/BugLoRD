package se.de.hu_berlin.informatik.experiments.results;

import se.de.hu_berlin.informatik.stardust.localizer.SBFLRanking;

public class Rankings<T> {

	private SBFLRanking<T> sbflRanking;

	public SBFLRanking<T> getSbflRanking() {
		return sbflRanking;
	}

	public void setSbflRanking(SBFLRanking<T> sbflRanking) {
		this.sbflRanking = sbflRanking;
	}
	
}

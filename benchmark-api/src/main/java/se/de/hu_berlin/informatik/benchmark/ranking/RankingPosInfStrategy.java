package se.de.hu_berlin.informatik.benchmark.ranking;

@FunctionalInterface
public interface RankingPosInfStrategy {
	
	public enum Strategy {
		ZERO,
		INFINITY,
		NEGATIVE_INFINITY,
		BEST,
		WORST,
		NAN
	}

	public Strategy assignValueToPosInfValue();
	
}

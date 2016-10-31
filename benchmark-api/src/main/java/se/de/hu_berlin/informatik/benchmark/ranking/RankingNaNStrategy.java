package se.de.hu_berlin.informatik.benchmark.ranking;

@FunctionalInterface
public interface RankingNaNStrategy {
	
	public enum Strategy {
		ZERO,
		INFINITY,
		NEGATIVE_INFINITY,
		BEST,
		WORST,
		NAN
	}

	public Strategy assignValueToNaNValue();
	
}

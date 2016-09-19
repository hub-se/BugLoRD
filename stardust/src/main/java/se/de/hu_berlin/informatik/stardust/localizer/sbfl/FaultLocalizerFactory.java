package se.de.hu_berlin.informatik.stardust.localizer.sbfl;

import se.de.hu_berlin.informatik.stardust.localizer.IFaultLocalizer;

/**
 * Generates new instances of available spectrum based fault localizers.
 * 
 * @author Simon Heiden
 */
public class FaultLocalizerFactory {

	/**
	 * @param localizer
	 * the identifier of a fault localizer
	 * @return
	 * a new instance of the desired fault localizer
	 * @param <T>
	 * the type of element identifiers
	 */
	public static <T> IFaultLocalizer<T> newInstance(String localizer) {
		localizer = localizer.toLowerCase();
		switch(localizer) {
		case "op2":
			return new Op2<>();
		case "gp13":
			return new GP13<>();
		case "tarantula":
			return new Tarantula<>();
		case "ochiai":
			return new Ochiai<>();
		case "jaccard":
			return new Jaccard<>();
		case "ample":
			return new Ample<>();
		case "anderberg":
			return new Anderberg<>();
		case "arithmeticmean":
			return new ArithmeticMean<>();
		case "cohen":
			return new Cohen<>();
		case "dice":
			return new Dice<>();
		case "euclid":
			return new Euclid<>();
		case "fleiss":
			return new Fleiss<>();
		case "geometricmean":
			return new GeometricMean<>();
		case "goodman":
			return new Goodman<>();
		case "hamann":
			return new Hamann<>();
		case "hamming":
			return new Hamming<>();
		case "harmonicmean":
			return new HarmonicMean<>();
		case "kulczynski1":
			return new Kulczynski1<>();
		case "kulczynski2":
			return new Kulczynski2<>();
		case "m1":
			return new M1<>();
		case "m2":
			return new M2<>();
		case "ochiai2":
			return new Ochiai2<>();
		case "overlap":
			return new Overlap<>();
		case "rogerstanimoto":
			return new RogersTanimoto<>();
		case "rogot1":
			return new Rogot1<>();
		case "rogot2":
			return new Rogot2<>();
		case "russellrao":
			return new RussellRao<>();
		case "scott":
			return new Scott<>();
		case "simplematching":
			return new SimpleMatching<>();
		case "sokal":
			return new Sokal<>();
		case "sorensendice":
			return new SorensenDice<>();
		case "wong1":
			return new Wong1<>();
		case "wong2":
			return new Wong2<>();
		case "wong3":
			return new Wong3<>();
		case "zoltar":
			return new Zoltar<>();
		default:
			throw new IllegalArgumentException(localizer + " is not a valid localizer.");
		}
	}
	
}

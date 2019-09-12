package se.de.hu_berlin.informatik.faultlocalizer.sbfl;

import java.util.Locale;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Ample;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Anderberg;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.ArithmeticMean;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Barinel;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Cohen;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.DStar;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Dice;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Euclid;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Fleiss;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.GP13;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.GeometricMean;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Goodman;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Hamann;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Hamming;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.HarmonicMean;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Hyperbolic;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Jaccard;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Kulczynski1;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Kulczynski2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.M1;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.M2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Ochiai;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Ochiai2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Op2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Overlap;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.RogersTanimoto;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Rogot1;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Rogot2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.RussellRao;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Scott;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.SimpleMatching;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Sokal;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.SorensenDice;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Tarantula;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Wong1;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Wong2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Wong3;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Zoltar;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.ExtendedSimilarityFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.ExtendedSimilarityFL2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.ExtendedSimilarityFL3;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.MethodFocusFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.MethodLevelFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.NeighborhoodFocusFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.PwrExtSimilarityFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.ScoreCombinationFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.SimilarityBoostFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.SimilarityFL3;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.SimpleSimilarityFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.NeighborhoodFocusFL.Direction;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.depricated.AsymmetricSimilarityFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.depricated.ReverseExtendedSimilarityFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.depricated.ReverseExtendedSimilarityFL2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.depricated.ReverseSimilarityFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.depricated.ReverseSimilarityFL2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.depricated.SimilarityFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.depricated.SimilarityFL2;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;

/**
 * Generates new instances of available (spectrum based) fault localizers.
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
		localizer = localizer.toLowerCase(Locale.getDefault());
		switch(localizer) {
		case "dstar":
			return new DStar<>();
		case "barinel":
			return new Barinel<>();
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
		case "hyperbolic":
			return new Hyperbolic<>();
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
		case "similarityfl":
			return new SimilarityFL<>();
		case "similarityfl2":
			return new SimilarityFL2<>();
		case "similarityfl3":
			return new SimilarityFL3<>();
		case "simplesimilarityfl":
			return new SimpleSimilarityFL<>();
		case "extendedsimilarityfl":
			return new ExtendedSimilarityFL<>();
		case "extendedsimilarityfl2":
			return new ExtendedSimilarityFL2<>();
		case "reversesimilarityfl":
			return new ReverseSimilarityFL<>();
		case "reversesimilarityfl2":
			return new ReverseSimilarityFL2<>();
		case "reverseextendedsimilarityfl":
			return new ReverseExtendedSimilarityFL<>();
		case "reverseextendedsimilarityfl2":
			return new ReverseExtendedSimilarityFL2<>();
		case "extendedsimilarityfl3":
			return new ExtendedSimilarityFL3<>();
		case "asymmetricsimilarityfl":
			return new AsymmetricSimilarityFL<>();
		case "pwrextsimilarityfl5-0":
			return new PwrExtSimilarityFL<>(5);
		case "pwrextsimilarityfl4-0":
			return new PwrExtSimilarityFL<>(4);
		case "pwrextsimilarityfl3-0":
			return new PwrExtSimilarityFL<>(3);
		case "pwrextsimilarityfl2-0":
			return new PwrExtSimilarityFL<>(2);
		case "pwrextsimilarityfl0-5":
			return new PwrExtSimilarityFL<>(0.5);
		case "pwrextsimilarityfl0-7":
			return new PwrExtSimilarityFL<>(0.7);
		case "pwrextsimilarityfl0-2":
			return new PwrExtSimilarityFL<>(0.2);
		case "pwrextsimilarityfl0-1":
			return new PwrExtSimilarityFL<>(0.1);
		case "methodfocusfl+dstar":
			return new MethodFocusFL<>(new DStar<>());
		case "methodfocusfl+pwrextsimilarityfl0-5":
			return new MethodFocusFL<>(new PwrExtSimilarityFL<>(0.5));
		case "methodfocusfl+pwrextsimilarityfl0-1":
			return new MethodFocusFL<>(new PwrExtSimilarityFL<>(0.1));
		case "methodfocusfl+extendedsimilarityfl2":
			return new MethodFocusFL<>(new ExtendedSimilarityFL2<>());
		case "neighborhoodfocusfl-both3+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 1);
		case "neighborhoodfocusfl-both3-2+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 2);
		case "neighborhoodfocusfl-both3-5+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 5);
		case "neighborhoodfocusfl-both3-10+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10);
		case "neighborhoodfocusfl-both2+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 2, Direction.BOTH, 1);
		case "neighborhoodfocusfl-both2-2+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 2, Direction.BOTH, 2);
		case "neighborhoodfocusfl-both2-5+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 2, Direction.BOTH, 5);
		case "neighborhoodfocusfl-both2-10+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 2, Direction.BOTH, 10);
		case "neighborhoodfocusfl-both1+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 1, Direction.BOTH, 1);
		case "neighborhoodfocusfl-both1-2+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 1, Direction.BOTH, 2);
		case "neighborhoodfocusfl-both1-5+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 1, Direction.BOTH, 5);
		case "neighborhoodfocusfl-both1-10+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 1, Direction.BOTH, 10);
		case "neighborhoodfocusfl-both2+pwrextsimilarityfl0-5":
			return new NeighborhoodFocusFL<>(new PwrExtSimilarityFL<>(0.5), 2, Direction.BOTH, 1);
		case "neighborhoodfocusfl-both2+pwrextsimilarityfl0-1":
			return new NeighborhoodFocusFL<>(new PwrExtSimilarityFL<>(0.1), 2, Direction.BOTH, 1);
		case "neighborhoodfocusfl-both2+extendedsimilarityfl2":
			return new NeighborhoodFocusFL<>(new ExtendedSimilarityFL2<>(), 2, Direction.BOTH, 1);
		case "neighborhoodfocusfl-forward2+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 2, Direction.FORWARD, 1);
		case "neighborhoodfocusfl-forward2+pwrextsimilarityfl0-5":
			return new NeighborhoodFocusFL<>(new PwrExtSimilarityFL<>(0.5), 2, Direction.FORWARD, 1);
		case "neighborhoodfocusfl-forward2+pwrextsimilarityfl0-1":
			return new NeighborhoodFocusFL<>(new PwrExtSimilarityFL<>(0.1), 2, Direction.FORWARD, 1);
		case "neighborhoodfocusfl-forward2+extendedsimilarityfl2":
			return new NeighborhoodFocusFL<>(new ExtendedSimilarityFL2<>(), 2, Direction.FORWARD, 1);
		case "neighborhoodfocusfl-backward2+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 2, Direction.BACKWARD, 1);
		case "neighborhoodfocusfl-backward2+pwrextsimilarityfl0-5":
			return new NeighborhoodFocusFL<>(new PwrExtSimilarityFL<>(0.5), 2, Direction.BACKWARD, 1);
		case "neighborhoodfocusfl-backward2+pwrextsimilarityfl0-1":
			return new NeighborhoodFocusFL<>(new PwrExtSimilarityFL<>(0.1), 2, Direction.BACKWARD, 1);
		case "neighborhoodfocusfl-backward2+extendedsimilarityfl2":
			return new NeighborhoodFocusFL<>(new ExtendedSimilarityFL2<>(), 2, Direction.BACKWARD, 1);
		case "methodlevelfl+dstar":
			return new MethodLevelFL<>(new DStar<>());
		case "scorecombinationfl(neighborhoodfocusfl-both3-10+dstar_methodlevelfl+dstar)":
			return new ScoreCombinationFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, new MethodLevelFL<>(new DStar<>()), 1);
		case "scorecombinationfl(neighborhoodfocusfl-both3-10+dstar_methodlevelfl+dstar-2)":
			return new ScoreCombinationFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, new MethodLevelFL<>(new DStar<>()), 2);
		case "scorecombinationfl(neighborhoodfocusfl-both3-10+dstar_methodlevelfl+dstar-5)":
			return new ScoreCombinationFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, new MethodLevelFL<>(new DStar<>()), 5);
		case "scorecombinationfl(neighborhoodfocusfl-both3-10+dstar_methodlevelfl+dstar-10)":
			return new ScoreCombinationFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, new MethodLevelFL<>(new DStar<>()), 10);
		case "neighborhoodfocusfl-both4-10+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 4, Direction.BOTH, 10);
		case "neighborhoodfocusfl-both5-10+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 5, Direction.BOTH, 10);
		case "neighborhoodfocusfl-both3-20+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 20);
		case "neighborhoodfocusfl-both3-50+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 50);
		case "neighborhoodfocusfl-both3-100+dstar":
			return new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 100);
		case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr<2-0>)":
			return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 2.0);
		case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr<1-0>)":
			return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 1.0);
		case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr<0-5>)":
			return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 0.5);
		case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr<0-1>)":
			return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 0.1);
		default:
			throw new IllegalArgumentException(localizer + " is not a valid localizer.");
		}
	}
	
}

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
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.SimilarityFL;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.SimpleMatching;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Sokal;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.SorensenDice;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Tarantula;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Wong1;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Wong2;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Wong3;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.Zoltar;

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
		default:
			throw new IllegalArgumentException(localizer + " is not a valid localizer.");
		}
	}
	
}

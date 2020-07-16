package se.de.hu_berlin.informatik.faultlocalizer.sbfl;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.cfg.CfgASCOSFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.cfg.CfgASCOSFaultLocalizer.ASCOSRankingAlgorithm;
import se.de.hu_berlin.informatik.faultlocalizer.cfg.CfgRankFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.cfg.CfgRankFaultLocalizer.RankingAlgorithm;
import se.de.hu_berlin.informatik.faultlocalizer.cfg.CfgSimRankFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.cfg.CfgSimRankFaultLocalizer.SimRankingAlgorithm;
import se.de.hu_berlin.informatik.faultlocalizer.cfg.CfgSimRankStarFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.cfg.CfgSimRankStarFaultLocalizer.SimRankStarAlgorithm;
import se.de.hu_berlin.informatik.faultlocalizer.cfg.SimilarityUtils;
import se.de.hu_berlin.informatik.faultlocalizer.ngram.Nessa;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.*;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.*;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.NeighborhoodFocusFL.Direction;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.depricated.*;

import java.util.Locale;

/**
 * Generates new instances of available (spectrum based) fault localizers.
 *
 * @author Simon Heiden
 */
public class FaultLocalizerFactory {

    /**
     * @param localizer the identifier of a fault localizer
     * @param <T>       the type of element identifiers
     * @return a new instance of the desired fault localizer
     */
    public static <T> IFaultLocalizer<T> newInstance(String localizer) {
        localizer = localizer.toLowerCase(Locale.getDefault());
        if (localizer.startsWith("pr_") || localizer.startsWith("cr_") || localizer.startsWith("pcr_") ||
        		localizer.startsWith("hpr_") || localizer.startsWith("hcr_") || localizer.startsWith("hpcr_")) {
        	// assume PageRank localizer (format: <id>_<dampingFactor>_<iterations>_<localizer>
        	int underscoreIndex = localizer.indexOf('_');
        	String temp = localizer.substring(underscoreIndex + 1);
        	underscoreIndex = temp.indexOf('_');
        	if (underscoreIndex < 0) {
        		throw new IllegalArgumentException(localizer + " is not a valid localizer.");
			}
        	
        	double dampingFactor;
        	try {
        		dampingFactor = Double.valueOf(temp.substring(0, underscoreIndex));
        	} catch (NumberFormatException e) {
        		throw new IllegalArgumentException(temp.substring(0, underscoreIndex) + " is not a valid damping factor.");
			}
        	
        	temp = temp.substring(underscoreIndex + 1);
        	underscoreIndex = temp.indexOf('_');
        	if (underscoreIndex < 0) {
        		throw new IllegalArgumentException(localizer + " is not a valid localizer.");
			}
        	
        	int iterations;
        	try {
        		iterations = Integer.valueOf(temp.substring(0, underscoreIndex));
        	} catch (NumberFormatException e) {
        		throw new IllegalArgumentException(temp.substring(0, underscoreIndex) + " is not a valid max iterations count.");
			}
        	
        	temp = temp.substring(underscoreIndex + 1);
        	
        	RankingAlgorithm algorithm = null;
        	switch (localizer.substring(0, 3)) {
			case "pr_":
				algorithm = RankingAlgorithm.PAGE_RANK;
				break;
			case "cr_":
				algorithm = RankingAlgorithm.CHEI_RANK;
				break;
			case "pcr":
				algorithm = RankingAlgorithm.PAGE_CHEI_RANK;
				break;
			case "hpr":
				algorithm = RankingAlgorithm.HA_PAGE_RANK;
				break;
			case "hcr":
				algorithm = RankingAlgorithm.HA_CHEI_RANK;
				break;
			case "hpc":
				algorithm = RankingAlgorithm.HA_PAGE_CHEI_RANK;
				break;
			default:
				throw new IllegalStateException();
			}
        	
        	return new CfgRankFaultLocalizer<>(parseRawLocalizer(temp), dampingFactor, iterations, algorithm);
        }
        
        if (localizer.startsWith("sr_") || localizer.startsWith("isr_") || localizer.startsWith("vsr_") ||
        		localizer.startsWith("hsr_") || localizer.startsWith("hisr_") || localizer.startsWith("hvsr_")) {
        	// assume PageRank localizer (format: <id>_<calcId>_<dampingFactor>_<iterations>_<localizer>
        	int underscoreIndex = localizer.indexOf('_');
        	String temp = localizer.substring(underscoreIndex + 1);
        	underscoreIndex = temp.indexOf('_');
        	if (underscoreIndex < 0) {
        		throw new IllegalArgumentException(localizer + " is not a valid localizer.");
			}
        	
        	SimilarityUtils.CalculationStrategy strategy = null;
        	switch (temp.substring(0, underscoreIndex)) {
			case "max":
				strategy = SimilarityUtils.CalculationStrategy.MAX_SIMILARITY;
				break;
			case "avg":
				strategy = SimilarityUtils.CalculationStrategy.AVERAGE_SIMILARITY;
				break;
			default:
				throw new IllegalStateException(temp.substring(0, underscoreIndex));
			}
        	
        	temp = temp.substring(underscoreIndex + 1);
        	underscoreIndex = temp.indexOf('_');
        	if (underscoreIndex < 0) {
        		throw new IllegalArgumentException(localizer + " is not a valid localizer.");
			}
        	
        	double dampingFactor;
        	try {
        		dampingFactor = Double.valueOf(temp.substring(0, underscoreIndex));
        	} catch (NumberFormatException e) {
        		throw new IllegalArgumentException(temp.substring(0, underscoreIndex) + " is not a valid damping factor.");
			}
        	
        	temp = temp.substring(underscoreIndex + 1);
        	underscoreIndex = temp.indexOf('_');
        	if (underscoreIndex < 0) {
        		throw new IllegalArgumentException(localizer + " is not a valid localizer.");
			}
        	
        	int iterations;
        	try {
        		iterations = Integer.valueOf(temp.substring(0, underscoreIndex));
        	} catch (NumberFormatException e) {
        		throw new IllegalArgumentException(temp.substring(0, underscoreIndex) + " is not a valid max iterations count.");
			}
        	
        	temp = temp.substring(underscoreIndex + 1);
        	
        	SimRankingAlgorithm algorithm = null;
        	switch (localizer.substring(0, 3)) {
			case "sr_":
				algorithm = SimRankingAlgorithm.SIM_RANK;
				break;
			case "isr":
				algorithm = SimRankingAlgorithm.INV_SIM_RANK;
				break;
			case "vsr":
				algorithm = SimRankingAlgorithm.VECTOR_SIM_RANK;
				break;
			case "hsr":
				algorithm = SimRankingAlgorithm.HA_SIM_RANK;
				break;
			case "his":
				algorithm = SimRankingAlgorithm.HA_INV_SIM_RANK;
				break;
			case "hvs":
				algorithm = SimRankingAlgorithm.HA_VECTOR_SIM_RANK;
				break;
			default:
				throw new IllegalStateException();
			}
        	
        	return new CfgSimRankFaultLocalizer<>(parseRawLocalizer(temp), dampingFactor, iterations, algorithm, strategy);
        }
        
        if (localizer.startsWith("ssr_") || localizer.startsWith("issr_") || localizer.startsWith("vssr_") ||
        		localizer.startsWith("hssr_") || localizer.startsWith("hissr_") || localizer.startsWith("hvssr_")) {
        	// assume PageRank localizer (format: <id>_<calcId>_<dampingFactor>_<iterations>_<localizer>
        	int underscoreIndex = localizer.indexOf('_');
        	String temp = localizer.substring(underscoreIndex + 1);
        	underscoreIndex = temp.indexOf('_');
        	if (underscoreIndex < 0) {
        		throw new IllegalArgumentException(localizer + " is not a valid localizer.");
			}
        	
        	SimilarityUtils.CalculationStrategy strategy = null;
        	switch (temp.substring(0, underscoreIndex)) {
			case "max":
				strategy = SimilarityUtils.CalculationStrategy.MAX_SIMILARITY;
				break;
			case "avg":
				strategy = SimilarityUtils.CalculationStrategy.AVERAGE_SIMILARITY;
				break;
			default:
				throw new IllegalStateException(temp.substring(0, underscoreIndex));
			}
        	
        	temp = temp.substring(underscoreIndex + 1);
        	underscoreIndex = temp.indexOf('_');
        	if (underscoreIndex < 0) {
        		throw new IllegalArgumentException(localizer + " is not a valid localizer.");
			}
        	
        	double dampingFactor;
        	try {
        		dampingFactor = Double.valueOf(temp.substring(0, underscoreIndex));
        	} catch (NumberFormatException e) {
        		throw new IllegalArgumentException(temp.substring(0, underscoreIndex) + " is not a valid damping factor.");
			}
        	
        	temp = temp.substring(underscoreIndex + 1);
        	underscoreIndex = temp.indexOf('_');
        	if (underscoreIndex < 0) {
        		throw new IllegalArgumentException(localizer + " is not a valid localizer.");
			}
        	
        	int iterations;
        	try {
        		iterations = Integer.valueOf(temp.substring(0, underscoreIndex));
        	} catch (NumberFormatException e) {
        		throw new IllegalArgumentException(temp.substring(0, underscoreIndex) + " is not a valid max iterations count.");
			}
        	
        	temp = temp.substring(underscoreIndex + 1);
        	
        	SimRankStarAlgorithm algorithm = null;
        	switch (localizer.substring(0, 3)) {
			case "ssr":
				algorithm = SimRankStarAlgorithm.SIM_RANK_STAR;
				break;
			case "iss":
				algorithm = SimRankStarAlgorithm.INV_SIM_RANK_STAR;
				break;
			case "vss":
				algorithm = SimRankStarAlgorithm.VECTOR_SIM_RANK_STAR;
				break;
			case "hss":
				algorithm = SimRankStarAlgorithm.HA_SIM_RANK_STAR;
				break;
			case "his":
				algorithm = SimRankStarAlgorithm.HA_INV_SIM_RANK_STAR;
				break;
			case "hvs":
				algorithm = SimRankStarAlgorithm.HA_VECTOR_SIM_RANK_STAR;
				break;
			default:
				throw new IllegalStateException();
			}
        	
        	return new CfgSimRankStarFaultLocalizer<>(parseRawLocalizer(temp), dampingFactor, iterations, algorithm, strategy);
        }
        
        if (localizer.startsWith("ar_") || localizer.startsWith("iar_") || localizer.startsWith("var_") ||
        		localizer.startsWith("har_") || localizer.startsWith("hiar_") || localizer.startsWith("hvar_") ||
        		localizer.startsWith("war_") || localizer.startsWith("wiar_") || localizer.startsWith("wvar_") ||
        		localizer.startsWith("whar_") || localizer.startsWith("whiar_") || localizer.startsWith("whvar_")) {
        	// assume PageRank localizer (format: <id>_<dampingFactor>_<iterations>_<localizer>
        	int underscoreIndex = localizer.indexOf('_');
        	String temp = localizer.substring(underscoreIndex + 1);
        	underscoreIndex = temp.indexOf('_');
        	if (underscoreIndex < 0) {
        		throw new IllegalArgumentException(localizer + " is not a valid localizer.");
			}
        	
        	double dampingFactor;
        	try {
        		dampingFactor = Double.valueOf(temp.substring(0, underscoreIndex));
        	} catch (NumberFormatException e) {
        		throw new IllegalArgumentException(temp.substring(0, underscoreIndex) + " is not a valid damping factor.");
			}
        	
        	temp = temp.substring(underscoreIndex + 1);
        	underscoreIndex = temp.indexOf('_');
        	if (underscoreIndex < 0) {
        		throw new IllegalArgumentException(localizer + " is not a valid localizer.");
			}
        	
        	int iterations;
        	try {
        		iterations = Integer.valueOf(temp.substring(0, underscoreIndex));
        	} catch (NumberFormatException e) {
        		throw new IllegalArgumentException(temp.substring(0, underscoreIndex) + " is not a valid max iterations count.");
			}
        	
        	temp = temp.substring(underscoreIndex + 1);
        	
        	ASCOSRankingAlgorithm algorithm = null;
        	switch (localizer.substring(0, 3)) {
			case "ar_":
				algorithm = ASCOSRankingAlgorithm.ASCOS_RANK;
				break;
			case "iar":
				algorithm = ASCOSRankingAlgorithm.INV_ASCOS_RANK;
				break;
			case "var":
				algorithm = ASCOSRankingAlgorithm.VECTOR_ASCOS_RANK;
				break;
			case "har":
				algorithm = ASCOSRankingAlgorithm.HA_ASCOS_RANK;
				break;
			case "hia":
				algorithm = ASCOSRankingAlgorithm.HA_INV_ASCOS_RANK;
				break;
			case "hva":
				algorithm = ASCOSRankingAlgorithm.HA_VECTOR_ASCOS_RANK;
				break;
			case "war":
				algorithm = ASCOSRankingAlgorithm.WEIGHTED_ASCOS_RANK;
				break;
			case "wia":
				algorithm = ASCOSRankingAlgorithm.WEIGHTED_INV_ASCOS_RANK;
				break;
			case "wva":
				algorithm = ASCOSRankingAlgorithm.WEIGHTED_VECTOR_ASCOS_RANK;
				break;
			case "wha":
				algorithm = ASCOSRankingAlgorithm.WEIGHTED_HA_ASCOS_RANK;
				break;
			case "whi":
				algorithm = ASCOSRankingAlgorithm.WEIGHTED_HA_INV_ASCOS_RANK;
				break;
			case "whv":
				algorithm = ASCOSRankingAlgorithm.WEIGHTED_HA_VECTOR_ASCOS_RANK;
				break;
			default:
				throw new IllegalStateException();
			}
        	
        	return new CfgASCOSFaultLocalizer<>(parseRawLocalizer(temp), dampingFactor, iterations, algorithm);
        }

        return parseRawLocalizer(localizer);
    }

	public static <T> IFaultLocalizer<T> parseRawLocalizer(String localizer) {
		switch (localizer) {
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
            case "nessa-2-04":
                return new Nessa<>(2, 0.4);
            case "nessa-2-09":
                return new Nessa<>(2, 0.9);
            case "nessa-3-04":
                return new Nessa<>(3, 0.4);
            case "nessa-3-09":
                return new Nessa<>(3, 0.9);
            case "nessa-2-04-true":
                return new Nessa<>(2, 0.4, true);
            case "nessa-3-04-true":
                return new Nessa<>(3, 0.4, true);
            case "nessa-2-09-true":
                return new Nessa<>(2, 0.9, true);
            case "nessa-3-09-true":
                return new Nessa<>(3, 0.9, true);
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
            case "scorecombinationfl(dstar_methodlevelfl+dstar)":
                return new ScoreCombinationFL<>(new DStar<>(), 1, new MethodLevelFL<>(new DStar<>()), 1);
            case "scorecombinationfl(dstar_methodlevelfl+dstar-2)":
                return new ScoreCombinationFL<>(new DStar<>(), 1, new MethodLevelFL<>(new DStar<>()), 2);
            case "scorecombinationfl(dstar_methodlevelfl+dstar-5)":
                return new ScoreCombinationFL<>(new DStar<>(), 1, new MethodLevelFL<>(new DStar<>()), 5);
            case "scorecombinationfl(dstar_methodlevelfl+dstar-10)":
                return new ScoreCombinationFL<>(new DStar<>(), 1, new MethodLevelFL<>(new DStar<>()), 10);
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
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(2-0)-10)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 2.0, 10);
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(1-0)-10)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 1.0, 10);
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(0-5)-10)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 0.5, 10);
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(0-1)-10)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 0.1, 10);
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(2-0)-100)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 2.0, 100);
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(1-0)-100)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 1.0, 100);
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(0-5)-100)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 0.5, 100);
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(0-1)-100)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 0.1, 100);
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(2-0)-1)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 2.0, 1);
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(1-0)-1)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 1.0, 1);
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(0-5)-1)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 0.5, 1);
            case "similarityboostfl(neighborhoodfocusfl-both3-10+dstar_pwr0-1(0-1)-1)":
                return new SimilarityBoostFL<>(new NeighborhoodFocusFL<>(new DStar<>(), 3, Direction.BOTH, 10), 1, 0.1, 1);
            default:
                throw new IllegalArgumentException(localizer + " is not a valid localizer.");
        }
	}

}

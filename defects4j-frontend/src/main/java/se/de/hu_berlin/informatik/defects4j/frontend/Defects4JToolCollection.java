/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking;
import se.de.hu_berlin.informatik.combranking.CombineSBFLandNLFLRanking;
import se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize;
import se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines;
import se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

/**
 * Provides central access to all tools concerning the Defects4J Framework.
 * 
 * @author SimHigh
 * @deprecated
 */
public class Defects4JToolCollection {

	/**
	 * @param args
	 * ( Tokenize | TokenizeLines | Cobertura2Ranking | CombineSBFLandNLFLRanking | UnitTestLister | Plotter )
	 */
	public static void main(String[] args) {
		
		final String possibleArgs = "Tokenize,%n" +
				"TokenizeLines,%n" +
				"Cobertura2Ranking,%n" +
				"CombineSBFLandNLFLRanking,%n" +
				"UnitTestLister,%n" +
				"Plotter.%n";
		
		if (args.length < 1) {
			Misc.abort("No arguments given. List of arguments:%n%n" + possibleArgs);
		}
		
		String firstArg = args[0];
		String[] restArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
		
		switch(firstArg) {
		case "Tokenize":
			Tokenize.main(restArgs);
			break;
		case "TokenizeLines":
			TokenizeLines.main(restArgs);
			break;
		case "Cobertura2Ranking":
			Cob2Instr2Coverage2Ranking.main(restArgs);
			break;
		case "CombineSBFLandNLFLRanking":
			CombineSBFLandNLFLRanking.main(restArgs);
			break;
		case "UnitTestLister":
			UnitTestLister.main(restArgs);
			break;
		case "Plotter":
			Plotter.main(restArgs);
			break;
		default:
			Misc.abort("Argument '" + firstArg + "' is not valid. Possible arguments are: %n%n" + possibleArgs);	
		}
		
	}

}

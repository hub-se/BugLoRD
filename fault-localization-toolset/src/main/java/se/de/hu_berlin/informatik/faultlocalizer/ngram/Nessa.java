package se.de.hu_berlin.informatik.faultlocalizer.ngram;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.NodeRanking;
import se.de.hu_berlin.informatik.spectra.core.*;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList; //PT
import java.lang.Math; // PT

public class Nessa<T> extends AbstractFaultLocalizer<T> {
    private HashMap<Integer, Double> confidence;
    private double minSup = 0.9;
    private int maxN = 3;
    private boolean dynSup;
    
    boolean found; //PT
    
    public Nessa() {
        super();
        confidence = new LinkedHashMap<>();
    }

    public Nessa(int maxN, double minSup) {
        super();
        confidence = new LinkedHashMap<>();
        this.maxN = maxN;
        this.minSup = minSup;
    }

    public Nessa(int maxN, double minSup, boolean dynSup) {
        super();
        confidence = new LinkedHashMap<>();
        this.maxN = maxN;
        this.minSup = minSup;
        this.dynSup = dynSup;
    }

    @Override
    public Ranking<INode<T>> localize(final ISpectra<T, ? extends ITrace<T>> spectra, ComputationStrategies strategy) {
        final Ranking<INode<T>> ranking = new NodeRanking<>();
        LinearExecutionHitTrace hitTrace = new LinearExecutionHitTrace((ISpectra<SourceCodeBlock, ?>) spectra);
        //System.out.println(hitTrace.toString()); //PT
        NGramSet nGrams = dynSup ?
                new NGramSet(hitTrace, maxN, minSup, true) : new NGramSet(hitTrace, maxN, minSup);
       //PT ->
        //System.out.println(nGrams.getResultAsText()); //PT
        	//System.out.println("_________________"); //PT
        	//System.out.println(hitTrace.successfulToString()); //PT
        	//System.out.println("_________________"); //PT
        	//System.out.println("Successful tests (PT method): {" + hitTrace.getSuccessfulTestTraces() + "}");
        	//System.out.println("_________________"); //PT
        	//System.out.println(hitTrace.failedToString()); //PT
        	//System.out.println("_________________"); //PT
        	//System.out.println("Failed tests: {" + hitTrace.getFailedTest() + "}");
        //System.out.println("_________________"); //PT
        	//System.out.println("Failed tests (PT method): {" + hitTrace.getFailedTestTraces() + "}");
        	//System.out.println("_________________"); //PT
        	//System.out.println("All tests: {" + hitTrace.getTestTrace() + "}");
        //System.out.println("Confidence: " + nGrams.getConfidence()); //PT
        //System.out.println("_________________");
        nGrams.getnGrams().forEach(nGram -> {
        	//Idee: für jedes nGram mit EF > 0 den kleinsten Abstand (Cross-Entropy) zu nGrams mit EF = 0 berechnen
        	//und den Cross-Entropy-Wert als Suspiciousness zuordnen.
    	  //System.out.println(nGram.toString());
    		//System.out.println("_________________");
    		//System.out.println("BlockIDs: " + nGram.getBlockIDs());
    		//System.out.println("_________________");
    		//ArrayList<Integer> BlockIDs = new ArrayList<Integer>();
    		//BlockIDs = nGram.getBlockIDs();
    		//setNewConfidence(nGram, calculateConfidence(nGram, hitTrace)); //ohne Cross-Entropy
    		setNewConfidence(nGram, calculateCrossEntropy(nGram, hitTrace));
    //		System.out.println(nGram.toString());
    		//BlockIDs.forEach(ID -> {
    			//setNewConfidence(nGram, 1.0); //funktioniert auch nicht -> scheinbar keine Zuweisungen auf nGrams
    			//hitTrace.getFailedTestTraces().forEach(failedTestTrace -> { //in inneren foreach Schleifen moeglich
    				//failedTestTrace.getInvolvedBlocks().forEach(blockID -> {
    					//if (ID == blockID) {
    						//setNewConfidence(nGram, 1.0);
    						//nGram.setConfidence(1.0);
    						//System.out.println("------->");
    						//System.out.println(nGram.toString());
    						//System.out.println(nGram.getConfidence());
    						//System.out.println("<-------");
    					//}
    				//});
    			//});
    			/*hitTrace.getSuccessfulTestTraces().forEach(successfulTestTrace -> {
    				successfulTestTrace.getInvolvedBlocks().forEach(blockID -> {
    					if (ID == blockID) {
    						nGram.setConfidence(nGram.getConfidence() - 0.0000000001);
    					}
    				});
    			});*/
    		//});
    		/*if (nGram.getEF() > 0.0) {
    			System.out.println("EF > 0:" + nGram.toString());
    		}
    		System.out.println("_________________");
    		if (nGram.getEF()  <= 0.0) { //kein Ergebnis, es existieren nur nGrams mit EF > 0.0
    			System.out.println("EF <= 0:" + nGram.toString());
    		}*/
        });
        nGrams.updateConfidence();
        System.out.println("New Confidence: " + nGrams.getConfidence());
        // <- PT
        confidence = nGrams.getConfidence();
        confidence.forEach((key, value) -> {
                    ranking.add(spectra.getNode(key), value);
                }
        );
        for (final INode<T> node : spectra.getNodes()) {
            if (confidence.get(node) != null) continue;
            ranking.add(node, 0.0);
        }

        return Ranking.getRankingWithStrategies(
                ranking, Ranking.RankingValueReplacementStrategy.ZERO, Ranking.RankingValueReplacementStrategy.ZERO,
                Ranking.RankingValueReplacementStrategy.ZERO);
    }

    @Override
    public Ranking<INode<T>> localize(final ILocalizerCache<T> localizer, ComputationStrategies strategy) {
        return localize((ISpectra<T, ?>) localizer, strategy);

    }

    @Override
    public double suspiciousness(INode<T> node, ComputationStrategies strategy) {
        return 0;
    }
    
    //PT ->
    public void setNewConfidence(NGram nGram, double conf) {
    	nGram.setConfidence(conf);
    	return;
    }
    
    public double calculateConfidence(NGram nGram, LinearExecutionHitTrace hitTrace) {
    	if (nGram.getLength() >= 3) {   		
	    	ArrayList<Integer> BlockIDs = new ArrayList<Integer>();
	    	//ArrayList<LinearExecutionTestTrace> FailedTestTraces = new ArrayList<LinearExecutionTestTrace>();
	    	//List<Integer> InvolvedBlocks;
	    	found = false;
			BlockIDs = nGram.getBlockIDs();
			//FailedTestTraces = hitTrace.getFailedTestTraces();
			BlockIDs.forEach(ID -> {
				ArrayList<LinearExecutionTestTrace> FailedTestTraces = new ArrayList<LinearExecutionTestTrace>();
				FailedTestTraces = hitTrace.getFailedTestTraces();
				idInFailedTestTraces(ID, FailedTestTraces);
			    //found = true;			
				/*FailedTestTraces.forEach(failedTestTrace -> {
					List<Integer> InvolvedBlocks;
					InvolvedBlocks = failedTestTrace.getInvolvedBlocks();
					
					InvolvedBlocks.forEach(blockID -> {
						if (ID == blockID) {
							found = true;
							System.out.println("found = true");
						}
					});
				});*/
				/*if (ID > 10000) {
					found = true;
				}*/
			});
			if (found) {
				return 1.0;
			}
			else {
				return 0.5;
			}
    	}
    	else if (nGram.getLength() == 2) {
    		return 0.25;
    	}
    	return 0.0;
    }
    
    public void idInFailedTestTraces(int ID, ArrayList<LinearExecutionTestTrace> FailedTestTraces) {
    	//boolean IDinFailedTestTrace = false;
    	FailedTestTraces.forEach(failedTestTrace -> {
			List<Integer> InvolvedBlocks;
			InvolvedBlocks = failedTestTrace.getInvolvedBlocks();
			compareIDs(ID, InvolvedBlocks);
		    //IDinFailedTestTrace = true;
    	});	
    	//return IDinFailedTestTrace;
    }
    
    public void compareIDs(int ID, List<Integer> InvolvedBlocks) {
    	//boolean sameID = false;
    	InvolvedBlocks.forEach(blockID -> {
			if (ID == blockID) {
				found = true;
			}
		});
    }
    
    //Berechnet die Cross-Entropy fuer ein nGram
    public double calculateCrossEntropy(NGram nGram, LinearExecutionHitTrace hitTrace) {
    	System.out.println("calculate cross-entropy");
    	double crossEntropy = 0.0;
    	int length = nGram.getLength();
    	//int m = length -1;
    	//int N = length - m;
    	int N = 3;
    	//double nGramProbability = 0.0;
    	//double logProbability = 0.0;
    	double nGramProbability1 = calculateNGramProbability(nGram, hitTrace, 1);
    	double nGramProbability2 = calculateNGramProbability(nGram, hitTrace, 2);
    	double nGramProbability3 = calculateNGramProbability(nGram, hitTrace, 3);
    	double logProbability1 = 0.0;
    	double logProbability2 = 0.0;
    	double logProbability3 = 0.0;
    //	System.out.println("nGramProbability: " + nGramProbability);
    	if (nGramProbability1 > 0) {
    		logProbability1 = Math.log(nGramProbability1)/Math.log(2);
    	}
    	if (nGramProbability2 > 0) {
    		logProbability2 = Math.log(nGramProbability2)/Math.log(2);
    	}
    	if (nGramProbability3 > 0) {
    		logProbability3 = Math.log(nGramProbability3)/Math.log(2);
    	}
    //	System.out.println("logProbability: " + logProbability);
    	double sumProbability = logProbability1 + logProbability2 + logProbability3;
    	/*if (logProbability != 0.0 ) {
    		crossEntropy = -(1/N)*logProbability; //eigentlich *sum(...), da N = 1 wird nur ein Element berechnet
    	}
    	else {
    		crossEntropy = 0.0;
    	}*/
    	crossEntropy = -(1/N)*sumProbability;
    //	System.out.println("crossEntropy: " + crossEntropy);
    	//if (crossEntropy < 0.00000000001) crossEntropy = 0.0; //Um negative Werte zu vermeiden
    	//crossEntropy = crossEntropy * 2; //--
    	return crossEntropy;
    }
    
    //Berechnet q(nGram) (die Wahrscheinlichkeit des Auftretens des letzten Tokens nach diesem Kontext
    public double calculateNGramProbability(NGram nGram, LinearExecutionHitTrace hitTrace, int contextFlag) {
    	System.out.println("calculate nGram probability");
    	double q = 0.0;
    	if (nGram.getLength() == 3) { //Es werden nur nGrams der Laenge 3 berechnet, sonst confidence = 0
    		//double ET = nGram.getET(); //Anzahl der Ausfuehrungen dieses nGrams
    		double ET = calculateET(nGram, hitTrace, contextFlag);
    		double EC = calculateEC(nGram, hitTrace, contextFlag); //Ausfuehrungen von Kontext mit anderem letzten Wert im nGram
    //		System.out.println("ET: " + ET);
    //		System.out.println("EC: " + EC);
    		if (EC > 0.0) { //Teilen durch 0 verhindern
    			q = (ET/EC);
    		}
    	}
    	return q;
    }
    
    public double calculateEC(NGram nGram, LinearExecutionHitTrace hitTrace, int contextFlag) {
    	double EC = 0.0;
    	System.out.println("calculate EC");
    	ArrayList<Integer> nGramBlockIDs = nGram.getBlockIDs();
    	ArrayList<Integer> context = nGram.getContext();
    	int context1;
		int context2;
    	//contextFlag -> 1: context of first element; 2: context of second element; 3: context of third element
    	if (contextFlag == 1) {
    		context1 = context.get(0);
    		context2 = context.get(1);
    	}
    	else if (contextFlag == 2) {
    		context1 = context.get(1);
    		context2 = nGramBlockIDs.get(0);
    	}
    	else {
    	    context1 = nGramBlockIDs.get(0);
    	    context2 = nGramBlockIDs.get(1);
    	}
    	int seqContext1;
    	int seqContext2;
    	System.out.println("context1: " + context1);
    	System.out.println("context2: " + context2);
    	//EC = nGram.getET() * 2.0; //Test um richtiges Uebergeben von nGram zu pruefen und unterschiedliche Werte 
    							//fuer EC zu erhalten
    	for(int i = 0; i < hitTrace.getTestTracesCount(); i++) { //Iterieren ueber alle Sequenzen
    		LinearExecutionTestTrace testTrace = hitTrace.getTrace(i); //Finden jeder Ausfuehrung des gleichen
    		for(int j = 0; j < testTrace.getTraces().size(); j++) { //Kontexts in den Traces
    			LinearBlockSequence blockSequence = testTrace.getTrace(j);
    			//nGram.getBlockIDs() == blockSequence.getBlockSeq() -> immer zwei Elemente fuer den Kontext
    			//Iterieren ueber blockSequence
    			//einzelne Elemente zum Vergleich in eigene Variablen speichern
    //			System.out.println(testTrace.toString());
    			if (blockSequence.getBlockSeqSize() < 3) {
    //				System.out.println(testTrace.toString());
    //				System.out.println(blockSequence.toString());
    				continue;
    			}
    			for(int k = 0; k < blockSequence.getBlockSeqSize() - 3; k++) { //-3 because the last element has no
    				seqContext1 = blockSequence.getElement(k); //following element
    				seqContext2 = blockSequence.getElement(k+1);
    				/*System.out.println(nGram.toString());
    				System.out.println("context1: " + context1);
    		    	System.out.println("context2: " + context2);
    				System.out.println("seqContext1: " + seqContext1);
    		    	System.out.println("seqContext2: " + seqContext2);*/
    				if ((context1 == seqContext1) && (context2 == seqContext2)) {
    					EC = EC + 1.0;
    				}
    			}
    		}
    	}		
    	return EC;
    }
    
    public double calculateET(NGram nGram, LinearExecutionHitTrace hitTrace, int contextFlag) {
    	double ET = 0.0;
    	System.out.println("calculate ET");
    	ArrayList<Integer> nGramBlockIDs = nGram.getBlockIDs();
    	ArrayList<Integer> context = nGram.getContext();
    	int context1;
		int context2;
		int context3;
		System.out.println("context: " + context);
		System.out.println("context.get(1): " + context.get(1));
		System.out.println("context.get(0): " + context.get(0));
    	//contextFlag -> 1: context of first element; 2: context of second element; 3: context of third element
    	if (contextFlag == 1) {
    		context1 = context.get(0);
    		context2 = context.get(1);
    		context3 = nGramBlockIDs.get(0);
    	}
    	else if (contextFlag == 2) {
    		context1 = context.get(1);
    		context2 = nGramBlockIDs.get(0);
    		context3 = nGramBlockIDs.get(1);
    	}
    	else {
    	    context1 = nGramBlockIDs.get(0);
    	    context2 = nGramBlockIDs.get(1);
    	    context3 = nGramBlockIDs.get(2);
    	}
    	int seqContext1;
    	int seqContext2;
    	int seqContext3;
    	System.out.println("context1: " + context1);
    	System.out.println("context2: " + context2);
    	System.out.println("context3: " + context3);
    	//EC = nGram.getET() * 2.0; //Test um richtiges Uebergeben von nGram zu pruefen und unterschiedliche Werte 
    							//fuer EC zu erhalten
    	for(int i = 0; i < hitTrace.getTestTracesCount(); i++) { //Iterieren ueber alle Sequenzen
    		LinearExecutionTestTrace testTrace = hitTrace.getTrace(i); //Finden jeder Ausfuehrung des gleichen
    		for(int j = 0; j < testTrace.getTraces().size(); j++) { //Kontexts in den Traces
    			LinearBlockSequence blockSequence = testTrace.getTrace(j);
    			//nGram.getBlockIDs() == blockSequence.getBlockSeq() -> immer zwei Elemente fuer den Kontext
    			//Iterieren ueber blockSequence
    			//einzelne Elemente zum Vergleich in eigene Variablen speichern
    			if (blockSequence.getBlockSeqSize() < 3) continue;
    			for(int k = 0; k < blockSequence.getBlockSeqSize() - 3; k++) {
    				seqContext1 = blockSequence.getElement(k);
    				seqContext2 = blockSequence.getElement(k+1);
    				seqContext3 = blockSequence.getElement(k+2);
    				/*System.out.println(nGram.toString());
    				System.out.println("context1: " + context1);
    		    	System.out.println("context2: " + context2);
    				System.out.println("seqContext1: " + seqContext1);
    		    	System.out.println("seqContext2: " + seqContext2);*/
    				if ((context1 == seqContext1) && (context2 == seqContext2) && (context3 == seqContext3)) {
    					ET = ET + 1.0;
    				}
    			}
    		}
    	}		
    	return ET;
    }
    
   
    //<- PT
}

package se.de.hu_berlin.informatik.faultlocalizer.ngram;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.NodeRanking;
import se.de.hu_berlin.informatik.spectra.core.*;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList; //PT

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
        System.out.println(nGrams.getResultAsText()); //PT
        	//System.out.println("_________________"); //PT
        	//System.out.println(hitTrace.successfulToString()); //PT
        	//System.out.println("_________________"); //PT
        	//System.out.println("Successful tests (PT method): {" + hitTrace.getSuccessfulTestTraces() + "}");
        	//System.out.println("_________________"); //PT
        	//System.out.println(hitTrace.failedToString()); //PT
        	//System.out.println("_________________"); //PT
        	//System.out.println("Failed tests: {" + hitTrace.getFailedTest() + "}");
        System.out.println("_________________"); //PT
        	//System.out.println("Failed tests (PT method): {" + hitTrace.getFailedTestTraces() + "}");
        	//System.out.println("_________________"); //PT
        	//System.out.println("All tests: {" + hitTrace.getTestTrace() + "}");
        System.out.println("Confidence: " + nGrams.getConfidence()); //PT
        System.out.println("_________________");
        nGrams.getnGrams().forEach(nGram -> {
        	//Idee: für jedes nGram mit EF > 0 den kleinsten Abstand (Cross-Entropy) zu nGrams mit EF = 0 berechnen
        	//und den Cross-Entropy-Wert als Suspiciousness zuordnen.
    		System.out.println(nGram.toString());
    		//System.out.println("_________________");
    		//System.out.println("BlockIDs: " + nGram.getBlockIDs());
    		//System.out.println("_________________");
    		//ArrayList<Integer> BlockIDs = new ArrayList<Integer>();
    		//BlockIDs = nGram.getBlockIDs();
    		setNewConfidence(nGram, calculateConfidence(nGram, hitTrace));
    		System.out.println(nGram.toString());
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
    //<- PT
}

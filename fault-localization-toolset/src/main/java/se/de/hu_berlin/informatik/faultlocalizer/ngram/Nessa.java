package se.de.hu_berlin.informatik.faultlocalizer.ngram;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.NodeRanking;
import se.de.hu_berlin.informatik.spectra.core.*;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;

import java.util.HashMap;
import java.util.LinkedHashMap;

import java.util.ArrayList; //PT

public class Nessa<T> extends AbstractFaultLocalizer<T> {
    private HashMap<Integer, Double> confidence;
    private double minSup = 0.9;
    private int maxN = 3;
    private boolean dynSup;

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
    		ArrayList<Integer> BlockIDs = new ArrayList<Integer>();
    		BlockIDs = nGram.getBlockIDs();
    		nGram.setConfidence(0.0);
    		System.out.println(nGram.toString());
    		BlockIDs.forEach(ID -> {
    			hitTrace.getFailedTestTraces().forEach(failedTestTrace -> {
    				failedTestTrace.getInvolvedBlocks().forEach(blockID -> {
    					if (ID == blockID) {
    						nGram.setConfidence(1.0);
    					}
    				});
    			});
    			/*hitTrace.getSuccessfulTestTraces().forEach(successfulTestTrace -> {
    				successfulTestTrace.getInvolvedBlocks().forEach(blockID -> {
    					if (ID == blockID) {
    						nGram.setConfidence(nGram.getConfidence() - 0.0000000001);
    					}
    				});
    			});*/
    		});
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
}

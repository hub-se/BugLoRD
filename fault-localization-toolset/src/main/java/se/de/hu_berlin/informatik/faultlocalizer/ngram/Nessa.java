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
    
    boolean found; //PT, unused in implementation that uses cross-entropy
    
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
        NGramSet nGrams = dynSup ?
                new NGramSet(hitTrace, maxN, minSup, true) : new NGramSet(hitTrace, maxN, minSup);
       //PT ->
        nGrams.getnGrams().forEach(nGram -> {
    		//ArrayList<Integer> BlockIDs = new ArrayList<Integer>();
    		//BlockIDs = nGram.getBlockIDs();
    		//setNewConfidence(nGram, calculateConfidence(nGram, hitTrace)); //Test ohne Cross-Entropy
    		setNewConfidence(nGram, calculateCrossEntropy(nGram, hitTrace));
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
    
    //Test, unused when using cross-entropy metric
    public double calculateConfidence(NGram nGram, LinearExecutionHitTrace hitTrace) {
    	if (nGram.getLength() >= 3) {   		
	    	ArrayList<Integer> BlockIDs = new ArrayList<Integer>();
	    	found = false;
			BlockIDs = nGram.getBlockIDs();
			BlockIDs.forEach(ID -> {
				ArrayList<LinearExecutionTestTrace> FailedTestTraces = new ArrayList<LinearExecutionTestTrace>();
				FailedTestTraces = hitTrace.getFailedTestTraces();
				idInFailedTestTraces(ID, FailedTestTraces);
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
    
  //Test, unused when using cross-entropy metric
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
    
  //Test, unused when using cross-entropy metric
    public void compareIDs(int ID, List<Integer> InvolvedBlocks) {
    	//boolean sameID = false;
    	InvolvedBlocks.forEach(blockID -> {
			if (ID == blockID) {
				found = true;
			}
		});
    }
    
    //Calculates the cross-entropy for one n-Gram
    public double calculateCrossEntropy(NGram nGram, LinearExecutionHitTrace hitTrace) {
    	double crossEntropy = 0.0;
    	double N = 3.0;
    	double nGramProbability1 = calculateNGramProbability(nGram, hitTrace, 1);
    	double nGramProbability2 = calculateNGramProbability(nGram, hitTrace, 2);
    	double nGramProbability3 = calculateNGramProbability(nGram, hitTrace, 3);
    	double logProbability1 = 0.0;
    	double logProbability2 = 0.0;
    	double logProbability3 = 0.0;
    	if (nGramProbability1 > 0) {
    		logProbability1 = Math.log(nGramProbability1)/Math.log(2);
    	}
    	if (nGramProbability2 > 0) {
    		logProbability2 = Math.log(nGramProbability2)/Math.log(2);
    	}
    	if (nGramProbability3 > 0) {
    		logProbability3 = Math.log(nGramProbability3)/Math.log(2);
    	}
    	double sumProbability = logProbability1 + logProbability2 + logProbability3;
    	crossEntropy = -(1/N)*sumProbability;
    	return Math.abs(crossEntropy); //return Math.abs() to avoid -0.0
    }
    
    //Calculates q(nGram)=(ET/EC)
    public double calculateNGramProbability(NGram nGram, LinearExecutionHitTrace hitTrace, int contextFlag) {
    	double q = 0.0;
    	if (nGram.getLength() == 3) { //only use n-Grams of length 3, else confidence = 0.0
    		double ET = calculateET(nGram, hitTrace, contextFlag); //Executions of this trace of length 3
    		double EC = calculateEC(nGram, hitTrace, contextFlag); //Executions of this context of length 2
    		if (EC > 0.0) { //Avoid division by 0
    			q = (ET/EC);
    		}
    	}
    	return q;
    }
    
    public double calculateEC(NGram nGram, LinearExecutionHitTrace hitTrace, int contextFlag) {
    	double EC = 0.0;
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
    	for(int i = 0; i < hitTrace.getTestTracesCount(); i++) { //Iterate over all sequences
    		LinearExecutionTestTrace testTrace = hitTrace.getTrace(i);
    		for(int j = 0; j < testTrace.getTraces().size(); j++) {
    			LinearBlockSequence blockSequence = testTrace.getTrace(j);
    			if (blockSequence.getBlockSeqSize() < 3) {
    				continue;
    			}
    			//Iterate over blockSequence
    			for(int k = 0; k < blockSequence.getBlockSeqSize() - 3; k++) { //-3 because the last element has no
    				seqContext1 = blockSequence.getElement(k); //following element
    				seqContext2 = blockSequence.getElement(k+1);
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
    	ArrayList<Integer> nGramBlockIDs = nGram.getBlockIDs();
    	ArrayList<Integer> context = nGram.getContext();
    	int context1;
		int context2;
		int context3;
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
    	for(int i = 0; i < hitTrace.getTestTracesCount(); i++) { //Iterate over all sequences
    		LinearExecutionTestTrace testTrace = hitTrace.getTrace(i);
    		for(int j = 0; j < testTrace.getTraces().size(); j++) {
    			LinearBlockSequence blockSequence = testTrace.getTrace(j);
    			if (blockSequence.getBlockSeqSize() < 3) continue;
    			//Iterate over blockSequence
    			for(int k = 0; k < blockSequence.getBlockSeqSize() - 3; k++) {
    				seqContext1 = blockSequence.getElement(k);
    				seqContext2 = blockSequence.getElement(k+1);
    				seqContext3 = blockSequence.getElement(k+2);
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

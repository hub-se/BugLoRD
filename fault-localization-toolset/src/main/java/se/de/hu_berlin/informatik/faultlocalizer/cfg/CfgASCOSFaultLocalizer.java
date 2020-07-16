package se.de.hu_berlin.informatik.faultlocalizer.cfg;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.NodeRanking;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.cfg.Node;
import se.de.hu_berlin.informatik.spectra.core.cfg.ScoredDynamicCFG;
import se.de.hu_berlin.informatik.spectra.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;

public class CfgASCOSFaultLocalizer<T> extends AbstractFaultLocalizer<T> {
	
	public static enum ASCOSRankingAlgorithm {
		ASCOS_RANK("ar"),
		INV_ASCOS_RANK("iar"),
		VECTOR_ASCOS_RANK("var"),
		HA_ASCOS_RANK("har"),
		HA_INV_ASCOS_RANK("hiar"),
		HA_VECTOR_ASCOS_RANK("hvar"),
		WEIGHTED_ASCOS_RANK("war"),
		WEIGHTED_INV_ASCOS_RANK("wiar"),
		WEIGHTED_VECTOR_ASCOS_RANK("wvar"),
		WEIGHTED_HA_ASCOS_RANK("whar"),
		WEIGHTED_HA_INV_ASCOS_RANK("whiar"),
		WEIGHTED_HA_VECTOR_ASCOS_RANK("whvar");
		
		private String id;

		private ASCOSRankingAlgorithm(String id) {
			this.id = id;
		}
	}
	
	private IFaultLocalizer<T> localizer;
	private double dampingFactor;
	private int iterations;
	private ASCOSRankingAlgorithm algorithm;

	public CfgASCOSFaultLocalizer(IFaultLocalizer<T> localizer, double dampingFactor, int iterations, ASCOSRankingAlgorithm algorithm) {
		this.localizer = localizer;
		this.dampingFactor = dampingFactor;
		this.iterations = iterations;
		this.algorithm = algorithm;
	}
	
	public CfgASCOSFaultLocalizer(IFaultLocalizer<T> localizer) {
		this(localizer, 0.7, 0, ASCOSRankingAlgorithm.ASCOS_RANK);
	}

	@Override
    public Ranking<INode<T>> localize(final ISpectra<T, ? extends ITrace<T>> spectra, ComputationStrategies strategy) {		
		File cfgOutput = null;
		if (spectra.getPathToSpectraZipFile() != null) {
			cfgOutput = new File(spectra.getPathToSpectraZipFile().toAbsolutePath().toString() + ".cfg");
		}
		// generate CFG
		final ScoredDynamicCFG<T> cfg = new ScoredDynamicCFG<>(spectra.getCFG(cfgOutput));
        
        // compute a base ranking (usually some sort of SBFL ranking)
        final Ranking<INode<T>> baseRanking = Ranking.getRankingWithStrategies(localizer.localize(spectra), 
        		Ranking.RankingValueReplacementStrategy.NAN, 
        		Ranking.RankingValueReplacementStrategy.BEST,
                Ranking.RankingValueReplacementStrategy.WORST);
     		
        // assign base ranking scores to cfg
        for (INode<T> node : spectra.getNodes()) {
        	cfg.assignScore(node.getIndex(), baseRanking.getRankingValue(node));
		}
        
        // calculate scores with PageRank algorithm
        Map<Integer, double[]> simRank = null;
        switch (algorithm) {
		case ASCOS_RANK:
			simRank = ASCOS.calculateASCOS(cfg, dampingFactor, iterations);
			break;
		case INV_ASCOS_RANK:
			simRank = ASCOS.calculateInvertedASCOS(cfg, dampingFactor, iterations);
			break;
		case VECTOR_ASCOS_RANK:
			simRank = ASCOS.calculateSimVectorRank(cfg, dampingFactor, iterations);
			break;
		case HA_ASCOS_RANK:
			simRank = ASCOS.calculateASCOS(cfg, dampingFactor, iterations);
			break;
		case HA_INV_ASCOS_RANK:
			simRank = ASCOS.calculateInvertedASCOS(cfg, dampingFactor, iterations);
			break;
		case HA_VECTOR_ASCOS_RANK:
			simRank = ASCOS.calculateSimVectorRank(cfg, dampingFactor, iterations);
			break;
		case WEIGHTED_ASCOS_RANK:
			simRank = ASCOS.calculateASCOSPlusPlus(cfg, dampingFactor, iterations);
			break;
		case WEIGHTED_INV_ASCOS_RANK:
			simRank = ASCOS.calculateInvertedASCOSPlusPlus(cfg, dampingFactor, iterations);
			break;
		case WEIGHTED_VECTOR_ASCOS_RANK:
			simRank = ASCOS.calculateSimVectorRankPlusPlus(cfg, dampingFactor, iterations);
			break;
		case WEIGHTED_HA_ASCOS_RANK:
			simRank = ASCOS.calculateASCOSPlusPlus(cfg, dampingFactor, iterations);
			break;
		case WEIGHTED_HA_INV_ASCOS_RANK:
			simRank = ASCOS.calculateInvertedASCOSPlusPlus(cfg, dampingFactor, iterations);
			break;
		case WEIGHTED_HA_VECTOR_ASCOS_RANK:
			simRank = ASCOS.calculateSimVectorRankPlusPlus(cfg, dampingFactor, iterations);
			break;
		}
        
        
        // ignore nodes from spectra that were only executed by successful test cases;
        // this will lead to the scores for the removed nodes not being added to the ranking;
        // filtering is necessary, due to how the algorithm works... :/
        Collection<? extends ITrace<T>> failingTraces = spectra.getFailingTraces();

        // generate ranking based on base ranking and PageRank algorithm
        final Ranking<INode<T>> ranking = new NodeRanking<>();
        for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
        	Node node = entry.getValue();
			int index = node.getIndex();

			double score = 0;
			if (SpectraUtils.isNodeInvolvedInATrace(failingTraces, index)) {
				
				score = calculateScore(cfg, simRank, index);
				ranking.add(spectra.getNode(index), score);
			}
			
			if (node.isMerged()) {
				for (int i : node.getMergedIndices()) {
					if (SpectraUtils.isNodeInvolvedInATrace(failingTraces, i)) {
						ranking.add(spectra.getNode(i), score);
					}
				}
			}
		}
        
        return ranking;
    }
	
	private double calculateScore(ScoredDynamicCFG<T> cfg, Map<Integer, double[]> simRank, int index) {
		double baseScore = cfg.getScore(index);
		double[] simRankScores = simRank.get(index);
		double score = baseScore;
		for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
			int oNodeIndex = entry.getValue().getIndex();
			// if there exists a very similar node with a high suspiciousness score, then increase this node's score...
			score = Math.max(score, simRankScores[oNodeIndex] * cfg.getScore(oNodeIndex));
		}
		return score;
	}

	@Override
	public double suspiciousness(INode<T> node, ComputationStrategies strategy) {
		throw new IllegalStateException();
	}
	
	@Override
	public String getName() {
		return algorithm.id + "_" + dampingFactor + "_" + iterations + "_" + localizer.getName();
	}

}

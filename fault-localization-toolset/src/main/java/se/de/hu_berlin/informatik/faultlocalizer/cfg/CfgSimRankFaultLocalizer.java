package se.de.hu_berlin.informatik.faultlocalizer.cfg;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.cfg.SimilarityUtils.CalculationStrategy;
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

public class CfgSimRankFaultLocalizer<T> extends AbstractFaultLocalizer<T> {
	
	public static enum SimRankingAlgorithm {
		SIM_RANK("sr"),
		INV_SIM_RANK("isr"),
		VECTOR_SIM_RANK("vsr"),
		HA_SIM_RANK("hsr"),
		HA_INV_SIM_RANK("hisr"),
		HA_VECTOR_SIM_RANK("hvsr");
		
		private String id;

		private SimRankingAlgorithm(String id) {
			this.id = id;
		}
	}
	
	private IFaultLocalizer<T> localizer;
	private double dampingFactor;
	private int iterations;
	private SimRankingAlgorithm algorithm;
	private CalculationStrategy calcStrategy;

	public CfgSimRankFaultLocalizer(IFaultLocalizer<T> localizer, double dampingFactor, int iterations, 
			SimRankingAlgorithm algorithm, CalculationStrategy calcStrategy) {
		this.localizer = localizer;
		this.dampingFactor = dampingFactor;
		this.iterations = iterations;
		this.algorithm = algorithm;
		this.calcStrategy = calcStrategy;
	}
	
	public CfgSimRankFaultLocalizer(IFaultLocalizer<T> localizer) {
		this(localizer, 0.7, 0, SimRankingAlgorithm.SIM_RANK, CalculationStrategy.AVERAGE_SIMILARITY);
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
		case SIM_RANK:
			simRank = SimRank.calculateSimRank(cfg, dampingFactor, iterations);
			break;
		case INV_SIM_RANK:
			simRank = SimRank.calculateInvertedSimRank(cfg, dampingFactor, iterations);
			break;
		case VECTOR_SIM_RANK:
			simRank = SimRank.calculateSimVectorRank(cfg, dampingFactor, iterations);
			break;
		case HA_SIM_RANK:
			simRank = SimRank.calculateHitAwareSimRank(cfg, dampingFactor, iterations);
			break;
		case HA_INV_SIM_RANK:
			simRank = SimRank.calculateHitAwareInvertedSimRank(cfg, dampingFactor, iterations);
			break;
		case HA_VECTOR_SIM_RANK:
			simRank = SimRank.calculateHitAwareSimVectorRank(cfg, dampingFactor, iterations);
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
				
				score = SimilarityUtils.calculateScore(cfg, simRank, index, calcStrategy);
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

	@Override
	public double suspiciousness(INode<T> node, ComputationStrategies strategy) {
		throw new IllegalStateException();
	}
	
	@Override
	public String getName() {
		return algorithm.id + "_" + dampingFactor + "_" + iterations + "_" + localizer.getName();
	}

}

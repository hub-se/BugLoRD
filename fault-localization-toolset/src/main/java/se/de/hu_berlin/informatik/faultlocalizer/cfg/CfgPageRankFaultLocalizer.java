package se.de.hu_berlin.informatik.faultlocalizer.cfg;

import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.NodeRanking;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.INode.CoverageType;
import se.de.hu_berlin.informatik.spectra.core.cfg.Node;
import se.de.hu_berlin.informatik.spectra.core.cfg.ScoredDynamicCFG;
import se.de.hu_berlin.informatik.spectra.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;

public class CfgPageRankFaultLocalizer<T> extends AbstractFaultLocalizer<T> {
	
	private IFaultLocalizer<T> localizer;
	private double dampingFactor;

	public CfgPageRankFaultLocalizer(IFaultLocalizer<T> localizer, double dampingFactor) {
		this.localizer = localizer;
		this.dampingFactor = dampingFactor;
	}
	
	public CfgPageRankFaultLocalizer(IFaultLocalizer<T> localizer) {
		this(localizer, 0.5);
	}

	@Override
    public Ranking<INode<T>> localize(final ISpectra<T, ?> spectra, ComputationStrategies strategy) {		
		// generate CFG
		final ScoredDynamicCFG<T> cfg = new ScoredDynamicCFG<>(SpectraUtils.generateCFGFromTraces(spectra));
		// merge linear node sequences
        cfg.mergeLinearSequeces();
        
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
        Map<Integer, Double> pageRank = new PageRank<>(cfg, dampingFactor).calculate();
        
        // remove nodes from spectra that were only executed by successful test cases;
        // this will lead to the scores for the removed nodes not being added to the ranking;
        // filtering is necessary, due to how the algorithm works... :/
        SpectraUtils.removeNodesWithCoverageType(spectra, CoverageType.EF_EQUALS_ZERO);
        
        // generate ranking based on base ranking and PageRank algorithm
        final Ranking<INode<T>> ranking = new NodeRanking<>();
        for (Entry<Integer, Node> entry : cfg.getNodes().entrySet()) {
        	Node node = entry.getValue();
			Double score = pageRank.get(node.getIndex());

			ranking.add(spectra.getNode(node.getIndex()), score);
			
			if (node.isMerged()) {
				for (int i : node.getMergedIndices()) {
					ranking.add(spectra.getNode(i), score);
				}
			}
		}
        
        return ranking;
    }
	
	@Override
	public double suspiciousness(INode<T> node, ComputationStrategies strategy) {
		throw new IllegalStateException();
	}

}

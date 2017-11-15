/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer.sbfl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.stardust.localizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.localizer.SBFLRanking;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.stardust.spectra.DummyNode;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking.RankingStrategy;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class LocalizerFromFile implements ILocalizer<String> {

	// TODO: arrays instead of maps
	/** cache EF */
	private Map<String, Integer> __cacheEF;
	/** cache EP */
	private Map<String, Integer> __cacheEP;
	/** cache NF */
	private Map<String, Integer> __cacheNF;
	/** cache NP */
	private Map<String, Integer> __cacheNP;
	
	private List<INode<String>> nodes;

	public LocalizerFromFile(final String traceFile, final String metricsCsvFile) {
		readFiles(Paths.get(traceFile), Paths.get(metricsCsvFile));
	}
	
	private void readFiles(Path traceFile, Path metricsCsvFile) {
		resetCache();
		try (BufferedReader traceFileReader = Files.newBufferedReader(traceFile, StandardCharsets.UTF_8);
				BufferedReader metricsCsvFileReader = Files.newBufferedReader(metricsCsvFile, StandardCharsets.UTF_8)) {
			String identifier;
			String metricsLine;
			while ((identifier = traceFileReader.readLine()) != null
					&& (metricsLine = metricsCsvFileReader.readLine()) != null) {
				
				nodes.add(new DummyNode<>(identifier, this));
				
				String[] entry = CSVUtils.fromCsvLine(metricsLine);
				if (entry.length != 4) {
					Log.abort(this, "metrics file entry is not correct: '%s'.", metricsLine);
				}
				
				// EF, EP, NF, NP
				__cacheEF.put(identifier, Integer.valueOf(entry[0]));
				__cacheEP.put(identifier, Integer.valueOf(entry[1]));
				__cacheNF.put(identifier, Integer.valueOf(entry[2]));
				__cacheNP.put(identifier, Integer.valueOf(entry[3]));
				
			}
		} catch (IOException e) {
			Log.abort(this, e, "Could not read trace file or metrics file.");
		}
	}

	@Override
	public double getNP(INode<String> node, ComputationStrategies strategy) {
		Integer np = this.__cacheNP.get(node.getIdentifier());
		if (np == null) {
			Log.abort(this, "No value stored for node '%s'.", node.getIdentifier());
		}
		return np;
	}

	@Override
	public double getNF(INode<String> node, ComputationStrategies strategy) {
		Integer nf = this.__cacheNF.get(node.getIdentifier());
		if (nf == null) {
			Log.abort(this, "No value stored for node '%s'.", node.getIdentifier());
		}
		return nf;
	}

	@Override
	public double getEP(INode<String> node, ComputationStrategies strategy) {
		Integer ep = this.__cacheEP.get(node.getIdentifier());
		if (ep == null) {
			Log.abort(this, "No value stored for node '%s'.", node.getIdentifier());
		}
		return ep;
	}

	@Override
	public double getEF(INode<String> node, ComputationStrategies strategy) {
		Integer ef = this.__cacheEF.get(node.getIdentifier());
		if (ef == null) {
			Log.abort(this, "No value stored for node '%s'.", node.getIdentifier());
		}
		return ef;
	}

	@Override
	public void invalidateCachedValues() {
		resetCache();
	}

	private void resetCache() {
		this.nodes = new ArrayList<>();
		this.__cacheEF = new HashMap<>();
		this.__cacheEP = new HashMap<>();
		this.__cacheNF = new HashMap<>();
		this.__cacheNP = new HashMap<>();
	}

	@Override
	public Ranking<INode<String>> localize(IFaultLocalizer<String> localizer, ComputationStrategies strategy) {
		final Ranking<INode<String>> ranking = new SBFLRanking<>();
		for (final INode<String> node : nodes) {
			final double suspiciousness = localizer.suspiciousness(node, strategy);
			ranking.add(node, suspiciousness);
		}

		// treats NaN values as being negative infinity
		return Ranking.getRankingWithStrategies(
				ranking, RankingStrategy.NEGATIVE_INFINITY, RankingStrategy.INFINITY,
				RankingStrategy.NEGATIVE_INFINITY);
	}

}

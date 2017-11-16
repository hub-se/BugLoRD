/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
import se.de.hu_berlin.informatik.experiments.defects4j.plot.ComputeSBFLRankingsProcessor.ResultCollection;
import se.de.hu_berlin.informatik.experiments.defects4j.plot.HyperbolicBucketsEH.ChangeId;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.localizers.Hyperbolic;
import se.de.hu_berlin.informatik.utils.experiments.evo.AbstractEvoLocationProvider;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoAlgorithm;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoFitnessChecker;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoID;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoItem;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoLocationProvider;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoMutation;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoRecombination;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoStatistics;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoAlgorithm.KillStrategy;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoAlgorithm.ParentSelectionStrategy;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoAlgorithm.PopulationSelectionStrategy;
import se.de.hu_berlin.informatik.utils.experiments.evo.EvoAlgorithm.RecombinationStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.basics.CollectionSequencer;
import se.de.hu_berlin.informatik.utils.processors.basics.ItemCollector;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class HyperbolicEvoProcessor extends AbstractProcessor<List<BuggyFixedEntity<?>>, EvoItem<Double[], Double, ChangeId>> {
	
	private String outputDir;
	private String suffix;
	private ComputationStrategies strategy;
	private int threadCount;
	
	private AtomicInteger bucketID = new AtomicInteger(0);

	public HyperbolicEvoProcessor(int threadCount, String outputDir, String suffix, ComputationStrategies strategy) {
		super();
		this.threadCount = threadCount;
		this.outputDir = outputDir;
		this.suffix = suffix;
		this.strategy = strategy;
	}
	
	public double getNumberInRangeForK1K2(Random random) {
		return random.nextGaussian() * random.nextDouble() * 100;
	}
	
	public double getNumberInRangeForK3(Random random) {
		return random.nextGaussian() * random.nextDouble() * 2;
	}

	@Override
	public EvoItem<Double[], Double, ChangeId> processItem(List<BuggyFixedEntity<?>> bucket) {
		
		//Integer[] goal = { 1, 2, 3 };
		Random random = new Random();
		
		// locations: 0 <-> k1, 1 <-> k2, 2 <-> k3
		// ID type for Evoitems: (double, double, double)
		
		EvoLocationProvider<Double[],Integer> locationProvider = new AbstractEvoLocationProvider<Double[],Integer>() {
			@Override
			public Integer getNextLocation(Double[] item, LocationSelectionStrategy strategy) {
				if (item.length > 0) {
					return random.nextInt(item.length);
				} else {
					return 0;
				}
			}
		};
		
		EvoMutation<Double[],Integer,ChangeId> mutation = new EvoMutation<Double[],Integer,ChangeId>() {
			ChangeId nextChange = null;
			
			@Override
			public Double[] applyTo(Double[] target, Integer location) {
				Double[] array = new Double[target.length];
				for (int i = 0; i < array.length; ++i) {
					if (i == location) {
						array[i] = target[i] + nextChange.getChange();
					} else {
						array[i] = target[i];
					}
				}
				return array;
			}

			@Override
			public EvoID<ChangeId> getIDofNextMutation(Double[] target, Integer location) {
				double changeAmount = 0.0;
				if (location == 2) {
					// 0 <= k3 <= 2
					while (changeAmount == 0.0) {
						double temp = getNumberInRangeForK3(random);
						double result = target[location] + temp;
						if (result >= 0 && result <= 2) {
							changeAmount = temp;
						}
					}
				} else {
					// 0 <= k1, k2 <= 100
					while (changeAmount == 0.0) {
						double temp = getNumberInRangeForK1K2(random);
						double result = target[location] + temp;
						if (result >= 0 && result <= 100) {
							changeAmount = temp;
						}
					}
				}
				nextChange = new ChangeId(changeAmount, location);
				return new EvoID<>(0, nextChange);
			}

		};
		
		EvoFitnessChecker<Double[],Double,ChangeId> fitnessChecker = new EvoFitnessChecker<Double[],Double,ChangeId>() {
			
			@Override
			public Double computeFitness(Double[] item) {
				// use coefficients to generate hyperbolic function localizer
				Hyperbolic<String> hyperbolic = new Hyperbolic<>(item[0], item[1], item[2]);
				
				// generate rankings in separate directory
				int uniqueBucketID = bucketID.getAndIncrement();
				String uniqueOutputDir = outputDir + File.separator + uniqueBucketID;
				
				ItemCollector<ResultCollection> collector = new ItemCollector<ResultCollection>();
				// compute the rankings and get the mean rankings and so on as a result
				new PipeLinker().append(
						new CollectionSequencer<>(),
						new HyperbolicComputeSBFLRankingsEH(Collections.singletonList(hyperbolic), 
								ToolSpecific.MERGED, uniqueOutputDir, suffix, strategy),
						new ComputeSBFLRankingsProcessor(Paths.get(uniqueOutputDir), suffix, "hyperbolic"),
						collector)
				.submitAndShutdown(bucket);

				List<ResultCollection> collectedItems = collector.getCollectedItems();
				
				if (collectedItems.size() != 1) {
					Log.abort(this, "Fitness computation for '%s' was not successful -> %d.", 
							uniqueOutputDir, collectedItems.size());
				}
				
				double fitness = collectedItems.get(0).getMeanAvgRanking();

				// fitness is the mean ranking and should be as low as possible (close to 1, optimally)
				return fitness;
			}
		};
		
		//recombiner is optional
		EvoRecombination<Double[],ChangeId> recombination = new EvoRecombination<Double[],ChangeId>() {
			
			ChangeId nextChange = null;

			@Override
			public Double[] recombine(Double[] parent1, Double[] parent2) {
				Double[] child;
				
				if (nextChange.getChange() >= 0) {
					child = new Double[parent2.length];
					for (int i = 0; i < nextChange.getLocation(); ++i) {
						child[i] = parent1[i];
					}
					for (int i = 0; i < parent2.length; ++i) {
						child[i] = parent2[i];
					}
				} else {
					child = new Double[parent1.length];
					for (int i = 0; i < nextChange.getLocation(); ++i) {
						child[i] = parent2[i];
					}
					for (int i = 0; i < parent1.length; ++i) {
						child[i] = parent1[i];
					}
				}
				return child;
			}
			
			@Override
			public EvoID<ChangeId> getIDofNextRecombination(Double[] parent1, Double[] parent2) {
				int smallerLength = parent1.length < parent2.length ? parent1.length : parent2.length;
				nextChange = new ChangeId(random.nextGaussian(),  random.nextInt(smallerLength-1) + 1);
				return new EvoID<>(1, nextChange);
			}
		};
		
		StatisticsCollector<EvoStatistics> collector = new StatisticsCollector<>(EvoStatistics.class);
		
		EvoAlgorithm.Builder<Double[], Integer, Double, ChangeId> builder = 
				new EvoAlgorithm.Builder<Double[], Integer, Double, ChangeId>(50, 20, 
						KillStrategy.KILL_50_PERCENT, 
						PopulationSelectionStrategy.HALF_BEST_HALF_RANDOM, 
						ParentSelectionStrategy.BEST_75_PERCENT,
						RecombinationStrategy.POLYGAMY_BEST_20_PERCENT_WITH_OTHERS)
//						RecombinationStrategy.MONOGAMY_BEST_TO_WORST)
				.addRecombinationTemplate(recombination)
				.addMutationTemplate(mutation)
//				.addMutationTemplate(mutationLength)
				.setLocationProvider(locationProvider)
				.setFitnessChecker(fitnessChecker, threadCount, 1.0)
				.setStatisticsCollector(collector);
		
		for (int i = 0; i < 50; ++i) {
			builder.addToInitialPopulation(new Double[] {
					getNumberInRangeForK1K2(random),
					getNumberInRangeForK1K2(random),
					getNumberInRangeForK3(random),
			});
		}
		
		EvoItem<Double[],Double,ChangeId> result = builder.build().start();
		
		Log.out(this, collector.printStatistics());
		
		Log.out(this, "result: %s", Misc.arrayToString(result.getItem()));
		Log.out(this, "fitness: %d", result.getFitness().intValue());
		
		return result;
		
	}

	

}


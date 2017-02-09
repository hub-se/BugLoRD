/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.evolution;

import java.util.Collection;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.Builder;
import se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.LocationSelectionStrategy;
import se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.MutationSelectionStrategy;
import se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.PopulationSelectionStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class EvolutionaryAlgorithmTest extends TestSettings {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		deleteTestOutputs();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		deleteTestOutputs();
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm#EvolutionaryAlgorithm(int, int, int, java.lang.Object, se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.PopulationSelectionStrategy, se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.RecombinationSelectionStrategy, se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.RecombinationStrategy, se.de.hu_berlin.informatik.experiments.evolution.EvoLocationProvider, se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.LocationSelectionStrategy, se.de.hu_berlin.informatik.experiments.evolution.EvoMutationProvider, se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.MutationSelectionStrategy, se.de.hu_berlin.informatik.experiments.evolution.EvoRecombiner, se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory)}.
	 */
	@Test
	public void testEvolutionaryAlgorithm() throws Exception {
		Integer[] goal = { 1, 2, 3 };
		Random random = new Random(123456789);
		
		EvoLocationProvider<Integer[],Integer> locationProvider = new EvoLocationProvider<Integer[],Integer>() {
			@Override
			public Integer getNextLocation(Integer[] item, LocationSelectionStrategy strategy) {
				return random.nextInt(item.length);
			}
		};
		EvoMutationProvider<Integer[],Integer> mutationProvider = new EvoMutationProvider<Integer[],Integer>() {
			@Override
			public EvoMutation<Integer[], Integer> getNextMutation(MutationSelectionStrategy strategy) {
				return new EvoMutation<Integer[],Integer>() {
					@Override
					public Integer[] applyTo(Integer[] target, Integer location) {
						Integer[] array = new Integer[target.length];
						for (int i = 0; i < array.length; ++i) {
							if (i == location) {
								if (random.nextGaussian() >= 0) {
									array[i] = target[i] + 1;
								} else {
									array[i] = target[i] - 1;
								}
							} else {
								array[i] = target[i];
							}
						}
						return array;
					}
				};
			}

			@Override
			public boolean addMutation(EvoMutation<Integer[], Integer> mutationFunction) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Collection<EvoMutation<Integer[], Integer>> getMutations() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		EvoHandlerProvider<Integer[],Integer> evaluationHandlerFactory = new EvoHandlerProvider<Integer[],Integer>() {
			
			@Override
			public EvoHandler<Integer[], Integer> newFreshInstance() {
				return new EvoHandler<Integer[], Integer>() {

					@Override
					public EvoResult<Integer[], Integer> processInput(Integer[] input) {
						int fitness = 0;
						if (input.length != goal.length) {
							fitness = Integer.MAX_VALUE;
						} else {
							for (int i = 0; i < input.length; ++i) {
								fitness += Math.pow(Math.abs(goal[i] - input[i]), 2);
							}
						}

						return new TestEvoResult(input, fitness);
					}

					@Override
					public void resetAndInit() {
						// do nothing
					}
				};
			}
		};
		
		EvolutionaryAlgorithm.Builder<Integer[], Integer, Integer> builder = 
				new Builder<Integer[], Integer, Integer>(4, 50, 40, PopulationSelectionStrategy.BEST_ONLY)
				.setMutationProvider(mutationProvider, MutationSelectionStrategy.RANDOM)
				.setLocationProvider(locationProvider, LocationSelectionStrategy.RANDOM)
				.setFitnessChecker(evaluationHandlerFactory, 0)
				.addToPopulation(new Integer[] {2,4,1});
		
		EvoResult<Integer[],Integer> result = builder.build().start();
		
		Log.out(this, "result: %s", Misc.arrayToString(result.getItem()));
		Log.out(this, "fitness: %d", result.getFitness().intValue());
	}
	
	private static class TestEvoResult implements EvoResult<Integer[], Integer> {

		private int fitness;
		private Integer[] item;
		
		public TestEvoResult(Integer[] item, int fitness) {
			this.item = item;
			this.fitness = fitness;
		}
		
		@Override
		public int compareTo(Integer o) {
			return o.compareTo(fitness);
		}

		@Override
		public Integer getFitness() {
			return fitness;
		}

		@Override
		public Integer[] getItem() {
			return item;
		}

		@Override
		public boolean cleanUp() {
			item = null;
			return true;
		}
		
	}

}

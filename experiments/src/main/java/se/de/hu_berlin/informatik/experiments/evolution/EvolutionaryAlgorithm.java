package se.de.hu_berlin.informatik.experiments.evolution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import se.de.hu_berlin.informatik.utils.tm.pipeframework.AbstractPipe;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.CollectionSequencerPipe;
import se.de.hu_berlin.informatik.utils.tm.pipes.ThreadedProcessorPipe;
import se.de.hu_berlin.informatik.utils.tracking.ProgressTracker;
import se.de.hu_berlin.informatik.utils.tracking.TrackingStrategy;

public class EvolutionaryAlgorithm<T,L,F> {
	
	public static enum PopulationSelectionStrategy {
		BEST_ONLY,
		HALF_BEST_HALF_RANDOM,
		RANDOM,
		REUSE_ALL
	}
	
	public static enum RecombinationSelectionStrategy {
		BEST_10_PERCENT,
		BEST_20_PERCENT,
		BEST_50_PERCENT,
		WORST_10_PERCENT,
		WORST_20_PERCENT,
		WORST_50_PERCENT,
		RANDOM
	}
	
	public static enum RecombinationStrategy {
		MONOGAMY_BEST_TO_WORST,
		MONOGAMY_RANDOM,
		MULTIGAMY_SINGLE_BEST_WITH_OTHERS,
		MULTIGAMY_BEST_5_WITH_OTHERS,
		RANDOM
	}
	
	public static enum MutationSelectionStrategy {
		RANDOM
	}
	
	public static enum LocationSelectionStrategy {
		RANDOM
	}
	
	public static class Builder<T,L,F> {
		
		private int threadCount;
		private int populationCount;
		private int maxGenerationBound;
		private F fitnessGoal;
		
		private PopulationSelectionStrategy populationSelectionStrategy; 
		private RecombinationSelectionStrategy recombinationSelectionStrategy;
		private RecombinationStrategy recombinationStrategy;
		private EvoLocationProvider<T,L> locationProvider;
		private EvoMutationProvider<T,L> mutationProvider;
		private LocationSelectionStrategy locationSelectionStrategy;
		private MutationSelectionStrategy mutationSelectionStrategy;
		private EvoRecombiner<T> recombiner;
		private EvoHandlerProvider<T, F> evaluationHandlerProvider;
		
		private Set<T> startingPopulation = new HashSet<>();;
		
		public Builder(int threadCount, int populationCount, int maxGenerationBound, 
				PopulationSelectionStrategy populationSelectionStrategy) {
			super();
			this.threadCount = threadCount;
			this.populationCount = populationCount;
			this.maxGenerationBound = maxGenerationBound;
			this.populationSelectionStrategy = populationSelectionStrategy;
		}
		
		public Builder<T, L, F> setFitnessChecker(EvoHandlerProvider<T, F> evaluationHandlerFactory, F fitnessGoal) {
			this.fitnessGoal = fitnessGoal;
			this.evaluationHandlerProvider = evaluationHandlerFactory;
			return this;
		}
		
		public Builder<T, L, F> setLocationProvider(EvoLocationProvider<T,L> locationProvider,
				LocationSelectionStrategy locationSelectionStrategy) {
			this.locationProvider = locationProvider;
			this.locationSelectionStrategy = locationSelectionStrategy;
			return this;
		}
		
		public Builder<T, L, F> setMutationProvider(EvoMutationProvider<T,L> mutationProvider, 
				MutationSelectionStrategy mutationSelectionStrategy) {
			this.mutationProvider = mutationProvider;
			this.mutationSelectionStrategy = mutationSelectionStrategy;
			return this;
		}
		
		public Builder<T, L, F> setRecombiner(EvoRecombiner<T> recombiner,
				RecombinationSelectionStrategy recombinationSelectionStrategy,
				RecombinationStrategy recombinationStrategy) {
			this.recombiner = recombiner;
			this.recombinationSelectionStrategy = recombinationSelectionStrategy;
			this.recombinationStrategy = recombinationStrategy;
			return this;
		}
		
		public EvolutionaryAlgorithm<T, L, F> build() {
			return new EvolutionaryAlgorithm<T,L,F>(this);
		}
		
		public Builder<T, L, F> addToPopulation(T item) {
			startingPopulation.add(item);
			return this;
		}
		
	}
	
	private final int threadCount;
	private final int populationCount;
	private final int maxGenerationBound;
	private final F fitnessGoal;
	
	private final PopulationSelectionStrategy populationSelectionStrategy; 
	private final RecombinationSelectionStrategy recombinationSelectionStrategy;
	private final RecombinationStrategy recombinationStrategy;
	private final EvoLocationProvider<T,L> locationProvider;
	private final EvoMutationProvider<T,L> mutationProvider;
	private final LocationSelectionStrategy locationSelectionStrategy;
	private final MutationSelectionStrategy mutationSelectionStrategy;
	private final EvoRecombiner<T> recombiner;
	private final EvoHandlerProvider<T, F> evaluationHandlerProvider;
	
	private Set<T> startingPopulation;
	
	TrackingStrategy tracker = new ProgressTracker(false);
	
	private EvolutionaryAlgorithm(Builder<T, L, F> builder) {
		this.threadCount = builder.threadCount;
		if (this.threadCount < 1) {
			throw new IllegalStateException("Thread count has to be positive.");
		}
		this.populationCount = builder.populationCount;
		if (this.populationCount < 1) {
			throw new IllegalStateException("Population count has to be positive.");
		}
		this.maxGenerationBound = builder.maxGenerationBound;
		if (this.maxGenerationBound < 1) {
			throw new IllegalStateException("Generation bound has to be positive.");
		}
		this.fitnessGoal = builder.fitnessGoal;
		if (this.fitnessGoal == null) {
			throw new IllegalStateException("Fitness goal not given.");
		}
		this.populationSelectionStrategy = builder.populationSelectionStrategy;
		if (this.populationSelectionStrategy == null) {
			throw new IllegalStateException("Population selection strategy not given.");
		}
		
		this.recombiner = builder.recombiner;
		this.recombinationSelectionStrategy = builder.recombinationSelectionStrategy;
		this.recombinationStrategy = builder.recombinationStrategy;
		if (this.recombiner != null) {
			if (this.recombinationSelectionStrategy == null) {
				throw new IllegalStateException("Recombination selection strategy not given.");
			}
			if (this.recombinationStrategy == null) {
				throw new IllegalStateException("Recombination strategy not given.");
			}
		}
		
		this.locationProvider = builder.locationProvider;
		if (this.locationProvider == null) {
			throw new IllegalStateException("Location provider not given.");
		}
		this.locationSelectionStrategy = builder.locationSelectionStrategy;
		if (this.locationSelectionStrategy == null) {
			throw new IllegalStateException("Location selection strategy not given.");
		}
		this.mutationProvider = builder.mutationProvider;
		if (this.mutationProvider == null) {
			throw new IllegalStateException("Mutation provider not given.");
		}
		this.mutationSelectionStrategy = builder.mutationSelectionStrategy;
		if (this.mutationProvider == null) {
			throw new IllegalStateException("Mutation selection strategy not given.");
		}
		
		this.evaluationHandlerProvider = builder.evaluationHandlerProvider;
		if (this.evaluationHandlerProvider == null) {
			throw new IllegalStateException("Evaluation handler (fitness checker) provider not given.");
		}
		
		this.startingPopulation = builder.startingPopulation;
		if (this.startingPopulation == null || this.startingPopulation.isEmpty()) {
			throw new IllegalStateException("No starting population given.");
		}
	}
	
	public EvoResult<T,F> start() {
		//provide initial item/initial population
		if (startingPopulation.isEmpty()) {
			return null;
		}
		
		int generationCounter= 0;
		tracker.track("...running starting generation");
		//fill up with mutants if below desired population size
		Collection<T> currentPopulation = 
				fillUpPopulationWithMutants(startingPopulation, populationCount,
						mutationProvider, mutationSelectionStrategy, 
						locationProvider, locationSelectionStrategy);
		
		//test/validate (evaluation)
		Collection<EvoResult<T, F>> currentEvaluatedPop = 
				calculateFitness(startingPopulation, threadCount, evaluationHandlerProvider);

		//loop while the generation bound isn't reached and the fitness goal isn't met by any item
		while (generationCounter < maxGenerationBound && !checkIfGoalIsMet(currentEvaluatedPop, fitnessGoal)) {
			++generationCounter;
			tracker.track("...running generation " + generationCounter);
			//use n best items for new population (selection)
			currentEvaluatedPop = selectNewPopulation(currentEvaluatedPop, 
					populationSelectionStrategy, populationCount);
			currentPopulation = new ArrayList<>(currentEvaluatedPop.size());
			for (EvoResult<T,F> item : currentEvaluatedPop) {
				currentPopulation.add(item.getItem());
			}
			if (recombiner != null) {
				//select for recombination
				Collection<EvoResult<T, F>> parentPopulation = selectForRecombination(currentEvaluatedPop, 
						recombinationSelectionStrategy, populationCount);
				//cross-over (recombination)
				Collection<T> children = recombineParents(parentPopulation, recombinationStrategy, recombiner);
				currentPopulation.addAll(children);
			} else {
				//fill up with mutants if below desired population size
				currentPopulation = 
						fillUpPopulationWithMutants(currentPopulation, populationCount,
								mutationProvider, mutationSelectionStrategy, 
								locationProvider, locationSelectionStrategy);
			}
			//mutate
			currentPopulation = mutatePopulation(currentPopulation, 
					mutationProvider, mutationSelectionStrategy, 
					locationProvider, locationSelectionStrategy);
			//test/validate (evaluation)
			currentEvaluatedPop = calculateFitness(currentPopulation, threadCount, evaluationHandlerProvider);
		}
		//loop end
		//return best item, discard the rest
		return selectBestItemAndCleanUpRest(currentEvaluatedPop);
	}

	public static <T,F> boolean checkIfGoalIsMet(Collection<EvoResult<T, F>> currentEvaluatedPop, F fitnessGoal) {
		for (EvoResult<T,F> evaluatedItem : currentEvaluatedPop) {
			if (evaluatedItem.compareTo(fitnessGoal) >= 0) {
				return true;
			}
		}
		return false;
	}

	private static <L,T> Collection<T> fillUpPopulationWithMutants(Collection<T> population, int populationCount,
			EvoMutationProvider<T,L> mutationProvider, MutationSelectionStrategy mutationSelectionStrategy,
			EvoLocationProvider<T,L> locationProvider, LocationSelectionStrategy locationSelectionStrategy) {
		Collection<T> filledPopulation = new ArrayList<>(population);
		filledPopulation.addAll(population);
		
		while (true) {
			//iterate through original population and generate mutants 
			//until the population is filled to the desired count
			for (T item : population) {
				filledPopulation.add(mutationProvider
						.getNextMutation(mutationSelectionStrategy)
						.applyTo(item, locationProvider.getNextLocation(item, locationSelectionStrategy)));
				if (filledPopulation.size() >= populationCount) {
					return filledPopulation;
				}
			}
		}
	}

	private EvoResult<T, F> selectBestItemAndCleanUpRest(Collection<EvoResult<T, F>> currentEvaluatedPop) {
		if (currentEvaluatedPop.isEmpty()) {
			return null;
		}
		EvoResult<T,F> bestItem = currentEvaluatedPop.iterator().next();
		for (EvoResult<T,F> evaluatedItem : currentEvaluatedPop) {
			if (evaluatedItem.compareTo(bestItem.getFitness()) > 0) {
				bestItem = evaluatedItem;
			}
		}
		cleanUpOtherItems(currentEvaluatedPop, bestItem);
		return bestItem;
	}

	private static <L,T> Collection<T> mutatePopulation(Collection<T> currentPopulation, 
			EvoMutationProvider<T,L> mutationProvider, MutationSelectionStrategy mutationSelectionStrategy,
			EvoLocationProvider<T,L> locationProvider, LocationSelectionStrategy locationSelectionStrategy) {
		List<T> mutatedPopulation = new ArrayList<>();
		for (T item : currentPopulation) {
			mutatedPopulation.add(mutationProvider
					.getNextMutation(mutationSelectionStrategy)
					.applyTo(item, locationProvider.getNextLocation(item, locationSelectionStrategy)));
		}
		return mutatedPopulation;
	}

	private static <T,F> Collection<T> recombineParents(Collection<EvoResult<T, F>> parentPopulation, 
			RecombinationStrategy recombinationStrategy, EvoRecombiner<T> recombiner) {
		// TODO implement recombination procedure
		return null;
	}

	private static <T,F> Collection<EvoResult<T, F>> selectForRecombination(Collection<EvoResult<T, F>> evaluatedPop,
			RecombinationSelectionStrategy recombinationSelectionStrategy, int populationCount) {
		// TODO implement recombination selection procedure
		return null;
	}

	private static <T,F> Collection<EvoResult<T, F>> selectNewPopulation(Collection<EvoResult<T, F>> evaluatedPop,
			PopulationSelectionStrategy populationSelectionStrategy, int populationCount) {
		List<EvoResult<T, F>> population = new ArrayList<>(evaluatedPop);
		//kill off items to only keep half of the maximal population //TODO: other strategies...
		int selectionCount = populationCount > 1 ? populationCount/2 : 1;
		//sort from biggest to smallest (best to worst)
		population.sort((o1,o2) -> o2.compareTo(o1.getFitness()));
		List<EvoResult<T, F>> resultPopulation = new ArrayList<>(selectionCount);

		switch(populationSelectionStrategy) {
		case BEST_ONLY:
			int i = 0;
			Iterator<EvoResult<T,F>> iterator = population.iterator();
			while (iterator.hasNext() && i < selectionCount) {
				++i;
				resultPopulation.add(iterator.next());
			}
			//discard the rest
			while (iterator.hasNext()) {
				iterator.next().cleanUp();
			}
			break;
		case HALF_BEST_HALF_RANDOM:
			throw new UnsupportedOperationException();
			//break;
		case RANDOM:
			throw new UnsupportedOperationException();
			//break;
		default:
			throw new UnsupportedOperationException();
			//break;

		}

		return resultPopulation;
	}

	private static <T,F> void cleanUpOtherItems(Collection<EvoResult<T, F>> evaluatedPop, EvoResult<T, F> evaluatedItem) {
		for (EvoResult<T,F> item : evaluatedPop) {
			if (evaluatedItem != item) {
				item.cleanUp();
			}
		}
	}

	private static <T,F> Collection<EvoResult<T, F>> calculateFitness(Collection<T> population, int parallelthreads,
			EvoHandlerProvider<T, F> evaluationHandlerFactory) {
		final List<EvoResult<T, F>> evaluatedPopulation = new ArrayList<>(population.size());
		
		PipeLinker evaluationPipe = new PipeLinker(); 
		evaluationPipe.append(
				new CollectionSequencerPipe<T>(),
				new ThreadedProcessorPipe<>(parallelthreads, evaluationHandlerFactory),
				new AbstractPipe<EvoResult<T, F>, Collection<EvoResult<T, F>>>(true) {
					@Override
					public Collection<EvoResult<T, F>> processItem(EvoResult<T, F> item) {
						evaluatedPopulation.add(item);
						return null;
					}
				}
				).submitAndShutdown(population);
		
		return evaluatedPopulation;
	}

}

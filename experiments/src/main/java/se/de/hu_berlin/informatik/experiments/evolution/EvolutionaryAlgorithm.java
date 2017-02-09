package se.de.hu_berlin.informatik.experiments.evolution;

import java.util.HashSet;
import java.util.Set;

import java_cup.internal_error;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;
import se.de.hu_berlin.informatik.utils.tm.pipes.ThreadedProcessorPipe;

public class EvolutionaryAlgorithm<L,T,F> {
	
	public static enum PopulationSelectionStrategy {
		BEST_10_PERCENT,
		BEST_20_PERCENT,
		RANDOM
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
	
	private final int populationCount;
	private final int maxGenerationBound;
	private final F fitnessGoal;
	
	private final PopulationSelectionStrategy populationSelectionStrategy; 
	private final RecombinationSelectionStrategy recombinationSelectionStrategy;
	private final EvoLocationProvider<L> locationProvider;
	private final EvoMutationProvider<L, T> mutationProvider;
	private final EvoRecombiner<T> recombiner;
	private final EHWithInputAndReturnFactory<T,EvoResult<T, F>> evaluationHandlerFactory;
	private final ThreadedProcessorPipe<T, EvoResult<T, F>> evaluationPipe;
	
	private Set<T> currentPopulation;
	
	public EvolutionaryAlgorithm(int threadCount, int populationCount, int maxGenerationBound, F fitnessGoal,
			PopulationSelectionStrategy populationSelectionStrategy,
			RecombinationSelectionStrategy recombinationSelectionStrategy, 
			EvoLocationProvider<L> locationProvider,
			EvoMutationProvider<L, T> mutationProvider, 
			EvoRecombiner<T> recombiner,
			EHWithInputAndReturnFactory<T, EvoResult<T, F>> evaluationHandlerFactory) {
		super();
		this.populationCount = populationCount;
		this.maxGenerationBound = maxGenerationBound;
		this.fitnessGoal = fitnessGoal;
		this.populationSelectionStrategy = populationSelectionStrategy;
		this.recombinationSelectionStrategy = recombinationSelectionStrategy;
		this.locationProvider = locationProvider;
		this.mutationProvider = mutationProvider;
		this.recombiner = recombiner;
		this.evaluationHandlerFactory = evaluationHandlerFactory;
		
		currentPopulation = new HashSet<>();
		this.evaluationPipe = new ThreadedProcessorPipe<>(threadCount, evaluationHandlerFactory);
	}
	
	public boolean addToPopulation(T item) {
		return currentPopulation.add(item);
	}
	
	public T start() {
		//provide initial item/initial population
		if (currentPopulation.isEmpty()) {
			return null;
		}
		
		//test/validate (evaluation)
		for (T item : currentPopulation) {
			if (checkFitness(item) >= fitnessGoal) {
				return item;
			}
		}
		//if not done, use n best items for new population (selection)
		//loop start
			//mutate
			//cross-over (recombination)
			//test/validate (evaluation)
			//if not done, use n best items for new population (selection)
		//loop end
		//return best item
		return null;
	}

	private F checkFitness(T item) {
		// TODO Auto-generated method stub
		return null;
	}

}

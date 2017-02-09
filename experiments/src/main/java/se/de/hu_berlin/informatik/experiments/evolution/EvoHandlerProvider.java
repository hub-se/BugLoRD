package se.de.hu_berlin.informatik.experiments.evolution;

import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

public abstract class EvoHandlerProvider<T,F> extends EHWithInputAndReturnFactory<T,EvoResult<T, F>> {

	@Override
	public abstract EvoHandler<T,F> newFreshInstance();

}

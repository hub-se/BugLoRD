package se.de.hu_berlin.informatik.experiments.evolution;

import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;

public abstract class EvoHandler<T,F> extends EHWithInputAndReturn<T,EvoResult<T, F>> {

}

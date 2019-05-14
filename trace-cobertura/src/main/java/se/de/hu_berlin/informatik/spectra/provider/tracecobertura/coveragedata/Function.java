package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


public interface Function<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t);

}

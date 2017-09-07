package se.de.hu_berlin.informatik.stardust.provider.loader;

import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;

public interface ICoverageDataLoader<T, K extends ITrace<T>, D> {

	/**
	 * Loads a single coverage data object to the given spectra or only adds the
	 * nodes extracted from the data object if the respective parameter is set.
	 * @param lineSpectra
	 * the line spectra to ehich to add the coverage data
	 * @param coverageData
	 * the coverage data object
	 * @param fullSpectra
	 * whether to add all nodes from the coverage data or only the nodes that
	 * were actually covered
	 * @return true if successful; false otherwise
	 */
	public boolean loadSingleCoverageData(ISpectra<T, K> lineSpectra, final D coverageData, final boolean fullSpectra);

	/**
	 * Provides an identifier of type T, generated from the given parameters.
	 * @param packageName
	 * a package name
	 * @param sourceFilePath
	 * a source file path
	 * @param methodNameAndSig
	 * a method name and signature
	 * @param lineNumber
	 * a line number
	 * @return an identifier (object) of type T
	 */
	public T getIdentifier(String packageName, String sourceFilePath, String methodNameAndSig, int lineNumber);

}

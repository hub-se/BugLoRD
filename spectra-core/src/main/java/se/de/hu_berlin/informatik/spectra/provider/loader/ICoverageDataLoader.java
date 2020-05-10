package se.de.hu_berlin.informatik.spectra.provider.loader;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.Node.NodeType;

public interface ICoverageDataLoader<T, K extends ITrace<T>, D> {

    /**
     * Loads a single coverage data object to the given spectra or only adds the
     * nodes extracted from the data object if the respective parameter is set.
     *
     * @param lineSpectra  the line spectra to ehich to add the coverage data
     * @param coverageData the coverage data object
     * @param fullSpectra  whether to add all nodes from the coverage data or only the nodes that
     *                     were actually covered
     * @return true if successful; false otherwise
     */
    public boolean loadSingleCoverageData(ISpectra<T, K> lineSpectra, final D coverageData, final boolean fullSpectra);

    /**
     * Provides an identifier of type T, generated from the given parameters.
     *
     * @param packageName      a package name
     * @param sourceFilePath   a source file path
     * @param methodNameAndSig a method name and signature
     * @param lineNumber       a line number
     * @param nodeType         the type of the node
     * @return an identifier (object) of type T
     */
    public T getIdentifier(String packageName, String sourceFilePath, String methodNameAndSig, int lineNumber, NodeType nodeType);

    /**
     * Returns the node index (or -1, if not existing) for the given class and line number.
     *
     * @param sourceFilePath a source file path
     * @param lineNumber     the line number in the class
     * @param nodeType       the type of the node
     * @return the node index corresponding to the given class and line number, if existing; -1 otherwise
     */
    public int getNodeIndex(String sourceFilePath, int lineNumber, NodeType nodeType);

}

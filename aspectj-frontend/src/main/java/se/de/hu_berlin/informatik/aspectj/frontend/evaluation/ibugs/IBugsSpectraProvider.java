/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs;

import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ExperimentRuntimeException;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.provider.ISpectraProvider;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.CoberturaSpectraProviderFactory;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.xml.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.utils.files.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provides spectra using iBugs coverage traces for a specific BugID
 */
public class IBugsSpectraProvider implements ISpectraProvider<SourceCodeBlock, HitTrace<SourceCodeBlock>> {

    /**
     * contains the path to the trace folder of the specific bugId
     */
    private final File bugFolder;
    /**
     * contains the bug id this experiment shall run with
     */
    private final int bugId;
    /**
     * Number of failing traces to load
     */
    private final Integer failingTraces;
    /**
     * Number of successful traces to load
     */
    private final Integer successfulTraces;

    /**
     * Once loaded, we cache the spectra
     */
    private ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> __cacheSpectra; // NOCS

    /**
     * Creates a new spectra provider. Take all traces available for the specified bug id
     *
     * @param root  path to the trace files
     * @param bugId bug id to run the experiment with
     */
    public IBugsSpectraProvider(final String root, final int bugId) {
        this(root, bugId, null, null);
    }

    /**
     * Creates a new spectra provider. Takes the specified number of traces for the given bug id to create the trace
     *
     * @param root             path to the trace files
     * @param bugId            bug id to run the experiment with
     * @param failingTraces    the number of required failing traces
     * @param successfulTraces the number of required successful traces
     */
    public IBugsSpectraProvider(final String root, final int bugId, final Integer failingTraces,
                                final Integer successfulTraces) {
        /** contains the path to the iBugs trace folder */
        File root1 = new File(root);
        this.bugId = bugId;
        this.bugFolder = new File(root1.getAbsolutePath() + "/" + bugId + "/pre-fix");
        this.failingTraces = failingTraces;
        this.successfulTraces = successfulTraces;

        // assert folders exist
        if (!root1.isDirectory()) {
            throw new RuntimeException(String.format("Specified iBugs trace root folder '%s' is not a valid directory",
                    root));
        }
        if (!this.bugFolder.isDirectory()) {
            throw new RuntimeException(String.format(
                    "Specified iBugs trace folder '%s' for bugId '%d' is not a valid directory", root, bugId));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> loadSpectra() throws IllegalStateException {
        if (this.__cacheSpectra == null) {
            final CoberturaXMLProvider<HitTrace<SourceCodeBlock>> c = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(true);
            int loadedSuccess = 0;
            int loadedFailure = 0;

            // inject files into cobertura provider
            for (final Map.Entry<String, Boolean> trace : this.traces().entrySet()) {
                if (trace.getValue()) {
                    loadedSuccess++;
                } else {
                    loadedFailure++;
                }
                if (!c.addData(trace.getKey(), null, trace.getValue())) {
                    throw new IllegalStateException("Adding coverage trace failed.");
                }
            }

            // assert we have enough files loaded
            if (this.failingTraces != null && loadedFailure < this.failingTraces) {
                throw new ExperimentRuntimeException(String.format(
                        "Bug ID '%d' has only %d failing traces, but experiment requires at least %d.", this.bugId,
                        loadedFailure, this.failingTraces));
            }
            if (this.successfulTraces != null && loadedSuccess < this.successfulTraces) {
                throw new ExperimentRuntimeException(String.format(
                        "Bug ID '%d' has only %d successful traces, but experiment requires at least %d.", this.bugId,
                        loadedSuccess, this.successfulTraces));
            }

            // load spectra
            this.__cacheSpectra = c.loadSpectra();
        }
        return this.__cacheSpectra;
    }

    /**
     * Lists all traces of the given version and their corresponding success
     * state.
     *
     * @return Map of absolute trace file names to their corresponding success
     * (true) or failure (false) state.
     */
    private Map<String, Boolean> traces() {
        final Map<String, Boolean> traces = new HashMap<>();
        for (final File trace : Objects.requireNonNull(this.bugFolder.listFiles(pathname -> {
            if (!pathname.isFile()) {
                return false;
            }
            final String fileExtension = FileUtils.getFileExtension(pathname);
            if (0 != "xml".compareTo(fileExtension)) {
                return false;
            }
            return pathname.getName().matches("^[pf]_.+");
        }))) {
            final boolean success = trace.getName().matches("^p_.+");
            traces.put(trace.getAbsolutePath(), success);
        }
        return traces;
    }


}

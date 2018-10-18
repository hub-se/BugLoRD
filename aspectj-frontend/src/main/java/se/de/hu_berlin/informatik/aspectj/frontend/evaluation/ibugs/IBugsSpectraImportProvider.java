/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.provider.ISpectraProvider;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

/**
 * Provides spectra using iBugs coverage traces for a specific BugID
 */
public class IBugsSpectraImportProvider implements ISpectraProvider<SourceCodeBlock, HitTrace<SourceCodeBlock>> {

    /** contains the path to the iBugs trace folder */
    private final File root;
    /** contains the path to the trace folder of the specific bugId */
    private final Path bugFile;

    /**
     * Creates a new spectra provider. Take all traces available for the specified bug id
     * 
     * @param root
     *            path to the trace files
     * @param bugId
     *            bug id to run the experiment with
     */
    public IBugsSpectraImportProvider(final String root, final int bugId) {
    	this.root = new File(root);
        this.bugFile = Paths.get(this.root.getAbsolutePath(), bugId + "-traces-compressed.zip");

        // assert folders exist
        if (!this.root.isDirectory()) {
            throw new RuntimeException(String.format("Specified iBugs trace root folder '%s' is not a valid directory.",
                    root));
        }
        if (!this.bugFile.toFile().exists()) {
            throw new RuntimeException(String.format(
                    "Specified iBugs spectra file '%s' for bugId '%d' does not exist.", root, bugId));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISpectra<SourceCodeBlock,HitTrace<SourceCodeBlock>> loadSpectra() throws IllegalStateException {
    	try {
			return SpectraFileUtils.loadSpectraFromBugMinerZipFile2(this.bugFile);
		} catch (IOException e) {
			throw new IllegalStateException("Could not load spectra from " + this.bugFile + ".");
		}
    }

}

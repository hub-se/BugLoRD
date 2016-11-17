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
import java.nio.file.Path;
import java.nio.file.Paths;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.ISpectraProvider;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;

/**
 * Provides spectra using iBugs coverage traces for a specific BugID
 */
public class IBugsSpectraImportProvider implements ISpectraProvider<SourceCodeBlock> {

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
    public ISpectra<SourceCodeBlock> loadSpectra() throws Exception {
    	return SpectraUtils.loadSpectraFromBugMinerZipFile2(this.bugFile);
    }

}

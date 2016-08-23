/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.aspectj.frontend.evaluation.sbfl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.JDOMException;

import se.de.hu_berlin.informatik.aspectj.frontend.Prop;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ISpectraProviderFactory;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.Experiment;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.IBugsFaultLocations;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.IBugsSpectraImportProvider;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.sbfl.CreateRankingsFromSpectra;
import se.de.hu_berlin.informatik.stardust.localizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.GP13;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.Jaccard;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.Ochiai;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.Op2;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.Tarantula;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.threaded.ExecutorServiceProvider;

/**
 * Experiment setup to compute and store the ranking of several bugs of the iBugs AspectJ bug repository with multiple
 * fault localization algorithms.
 */
public class CreateRankingsFromSpectra {

    private final int threads;

    /** Bug IDs to create rankings for */
    private final int[] bugIds;
    /** fault localizers to use in order to create ranking */
    final List<IFaultLocalizer<String>> faultLocalizers = new ArrayList<>();
    /** Path to results */
    private final String resultPath;
    /** Contains the real fault locations for all iBugs bugs */
    final IBugsFaultLocations realFaults;

    /** Holds the logger for the experiment executor */
    final Logger logger = Logger.getLogger(CreateRankingsFromSpectra.class.getName());

    final ISpectraProviderFactory<String> spectraProviderFactory;

    /**
     * Setup experiment
     *
     * @param threads
     * number of concurrent experiments to run
     * @param useDefaultBugIDs
     * whether to use default bug IDs or to use all IDs found in the directory
     *
     * @throws IOException
     * in case of an error concerning reading or writing from/to disk
     * @throws JDOMException
     * in case of JDOM error
     */
    public CreateRankingsFromSpectra(int threads, boolean useDefaultBugIDs) throws JDOMException, IOException {
    	Prop prop = new Prop().loadProperties();
    	
        // settings
        final String tracePath = prop.tracesDir;
        this.resultPath = prop.resultsFilePrefix;
        this.threads = threads;
        
        Paths.get(this.resultPath).getParent().toFile().mkdirs();

        // bug ids to run experiments for
        if (useDefaultBugIDs) {
        	this.bugIds = new int[] { 28919, 28974, 29186, 29959, 30168, 32463, 33635, 34925, 36430, 36803, 37576, 37739,
                38131, 39626, 39974, 40192, 40257, 40380, 40824, 42539, 42993, 43033, 43194, 43709, 44117, 46298,
                47318, 49657, 50776, 51320, 51929, 52394, 54421, 54965, 55341, 57436, 57666, 58520, 59596, 59895,
                61411, 62227, 64069, 64331, 67592, 69011, 70008, 71377, 71878, 72150, 72528, 72531, 72671, 73433,
                74238, 77799, 81846, 81863, 82134, 82218, 83563, 86789, 87376, 88652, 96371, 99168, 100227, 104218,
                107299, 109016, 109614, 113257, 114875, 115251, 115275, 116626, 116949, 118192, 118715, 118781, 119353,
                119451, 119539, 119543, 120351, 120474, 122370, 122728, 123695, 124654, 124808, 125480, 125699, 125810,
                128128, 128237, 128655, 128744, 129566, 130837, 130869, 131505, 131932, 131933, 132130, 135001, 136665,
                138143, 138219, 138223, 138286, 141956, 142165, 145086, 145693, 145950, 146546, 147701, 148409, 150671,
                151673, 151845, 152257, 152388, 152589, 152631, 153490, 153535, 153845, 154332, 155148, 155972, 156904,
                156962, 158412, 161217 };
        } else {
        	List<Path> spectraFiles = new SearchForFilesOrDirsModule(false, true, "**-traces-compressed.zip", false, true).
        			submit(Paths.get(tracePath)).getResult();
        	this.bugIds = new int[spectraFiles.size()];
        	Iterator<Path> iterator = spectraFiles.iterator();
        	for (int i = 0; i < bugIds.length; ++i) {
        		String filename = iterator.next().getFileName().toString();
        		bugIds[i] = Integer.parseInt(filename.substring(0, filename.indexOf('-')));
        	}
        }

        // add file logger
        this.logger.addHandler(new FileHandler(this.resultPath + "-log.txt"));

        // fault localizers to use
        // this.faultLocalizers.add(new
        // FusingFaultLocalizer<String>(NormalizationStrategy.ZeroOne,
        // SelectionTechnique.OVERLAP_RATE, DataFusionTechnique.COMB_ANZ));
        // this.faultLocalizers.add(new
        // FusingFaultLocalizer<String>(NormalizationStrategy.ZeroOne,
        // SelectionTechnique.BIAS_RATE, DataFusionTechnique.COMB_ANZ));
        this.addDefaultFaultLocalizers();

        this.realFaults = new IBugsFaultLocations(tracePath + "/realfaultlocations.xml");

        this.spectraProviderFactory = bugId -> new IBugsSpectraImportProvider(tracePath, bugId);
    }

    /**
     * Initialize CreateRankings experiment
     *
     * @param threads
     * number of concurrent experiments to run
     * @param spectraProviderFactory
     * a factory to provide a spectra object
     * @param resultsFolder
     * the path to the results folder
     * @param bugIds
     * the bug IDs to consider
     * @param logFile
     * the path to the log file
     * @param faultLocalizers
     * a list of fault localizers
     * @param realFaultsFile
     * the path to the xml file that contains the real fault locations
     * @throws SecurityException
     * in case of a security problem
     * @throws IOException
     * in case of an error while reading/writing from/to disk
     * @throws JDOMException
     * in case of a JDOM error
     */
    public CreateRankingsFromSpectra(int threads, 
    		final ISpectraProviderFactory<String> spectraProviderFactory, final String resultsFolder,
            final int[] bugIds, final String logFile, final List<IFaultLocalizer<String>> faultLocalizers,
            final String realFaultsFile) throws SecurityException, IOException, JDOMException {
    	this.threads = threads;
        this.spectraProviderFactory = spectraProviderFactory;
        this.resultPath = resultsFolder;
        this.bugIds = bugIds;
        this.logger.addHandler(new FileHandler(logFile));
        faultLocalizers.addAll(faultLocalizers);
        this.realFaults = new IBugsFaultLocations(realFaultsFile);
    }

    /**
     * Adds some default fault localizers.
     */
    private void addDefaultFaultLocalizers() {
//        this.faultLocalizers.add(new Ample<String>());
//        this.faultLocalizers.add(new Anderberg<String>());
//        this.faultLocalizers.add(new ArithmeticMean<String>());
//        this.faultLocalizers.add(new Cohen<String>());
//        this.faultLocalizers.add(new Dice<String>());
//        this.faultLocalizers.add(new Euclid<String>());
//        this.faultLocalizers.add(new Fleiss<String>());
//        this.faultLocalizers.add(new GeometricMean<String>());
//        this.faultLocalizers.add(new Goodman<String>());
//        this.faultLocalizers.add(new Hamann<String>());
//        this.faultLocalizers.add(new Hamming<String>());
//        this.faultLocalizers.add(new HarmonicMean<String>());
        this.faultLocalizers.add(new Jaccard<String>());
//        this.faultLocalizers.add(new Kulczynski1<String>());
//        this.faultLocalizers.add(new Kulczynski2<String>());
//        this.faultLocalizers.add(new M1<String>());
//        this.faultLocalizers.add(new M2<String>());
        this.faultLocalizers.add(new Ochiai<String>());
//        this.faultLocalizers.add(new Ochiai2<String>());
//        this.faultLocalizers.add(new Overlap<String>());
//        this.faultLocalizers.add(new RogersTanimoto<String>());
//        this.faultLocalizers.add(new Rogot1<String>());
//        this.faultLocalizers.add(new Rogot2<String>());
//        this.faultLocalizers.add(new RussellRao<String>());
//        this.faultLocalizers.add(new Scott<String>());
//        this.faultLocalizers.add(new SimpleMatching<String>());
//        this.faultLocalizers.add(new Sokal<String>());
//        this.faultLocalizers.add(new SorensenDice<String>());
        this.faultLocalizers.add(new Tarantula<String>());
//        this.faultLocalizers.add(new Wong1<String>());
//        this.faultLocalizers.add(new Wong2<String>());
//        this.faultLocalizers.add(new Wong3<String>());
//        this.faultLocalizers.add(new Zoltar<String>());
        this.faultLocalizers.add(new Op2<String>());
        this.faultLocalizers.add(new GP13<String>());
    }

    /**
     * Initialize and run experiment
     *
     * @param args
     *            CLI arguments
     * @throws InterruptedException
     *             in case the experiment was interrupted
     * @throws IOException
     *             in case the experiment failed
     * @throws JDOMException
     *             in case the experiment failed
     */
    public static void main(final String[] args) throws InterruptedException, JDOMException, IOException {
        new CreateRankingsFromSpectra(2, false).run();
    }

    /**
     * Runs all experiments.
     *
     * @throws InterruptedException
     *             in case the experiment was interrupted
     */
    public void run() throws InterruptedException {
    	ExecutorServiceProvider provider = new ExecutorServiceProvider(threads);
        ExecutorService executor = provider.getExecutorService();
        
        this.section("Beginning experiments");
        this.text("About to execute " + this.bugIds.length + " experiments.");

        // submit all experiments
        int submitted = 0;
        for (final int bugId : this.bugIds) {
            boolean dontExecute = true;
            for (final IFaultLocalizer<String> fl : this.faultLocalizers) {
                dontExecute &= this.resultExists(bugId, fl.getName());
            }

            if (dontExecute) {
                this.text(String.format("Skipping bug %d, as all results already exist.", bugId));
            } else {
                // create and submit experiment
                executor.submit(new ExperimentCall(this).setInput(bugId));
                submitted++;
            }
        }
        this.text("Submitted " + submitted + " of " + this.bugIds.length + " experiments.");

        // await experiment completion
        provider.shutdownAndWaitForTermination(2, TimeUnit.DAYS, true);
    }

    /**
     * Determines whether the result exists for a certain bug and FL combination
     *
     * @param bugId
     *            the bug id of the experiment
     * @param faultLocalizer
     *            the fault localizer name used by the experiment
     * @return true if the result already exists for the experiment, false otherwise
     */
    public boolean resultExists(final int bugId, final String faultLocalizer) {
        return this.resultsFile(bugId, faultLocalizer, "ranking.csv").exists();
    }

    /**
     * Returns a result file for an experiment and ensures any necessary folders where this file will reside, exist.
     *
     * @param bugId
     *            the bug id of the experiment
     * @param faultLocalizer
     *            the fault localizer name used by the experiment
     * @param filename
     *            the name of the result file
     * @return resultFile
     */
    public File resultsFile(final int bugId, final String faultLocalizer, final String filename) {
        // filepath: <resultFolder>/<FLName>/<bugId>/<filename>
        final File file = new File(String.format("%s/%d/%s/%s", this.resultPath, bugId, faultLocalizer, filename));
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    /**
     * Returns a result file for an experiment and ensures any necessary folders where this file will reside, exist.
     *
     * @param experiment
     *            the experiment to create the result file for
     * @param filename
     *            the name of the result file
     * @return resultFile
     */
    public File resultsFile(final Experiment experiment, final String filename) {
        return this.resultsFile(experiment.getBugId(), experiment.getLocalizer().getName(), filename);
    }

    /**
     * Log a new logical section
     *
     * @param section
     *            section header
     */
    private void section(final String section) {
        this.logger.log(Level.INFO, "=== " + section + " ===");
    }

    /**
     * Log text
     *
     * @param text
     *            to print
     */
    private void text(final String text) {
        this.logger.log(Level.INFO, ">  " + text);
    }

}

/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.IExperiment;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.IBugsFaultLocationCollection;
import se.de.hu_berlin.informatik.benchmark.ranking.SimpleRanking;
import se.de.hu_berlin.informatik.stardust.localizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeLine;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;

/**
 * Class is used to conduct a failure localization experiment using iBugs.
 */
public class Experiment implements IExperiment {

    /** Holds the logger for this class */
    private final Logger log = Logger.getLogger(Experiment.class.getName());
    /** Holds the real fault locations */
    private final IBugsFaultLocationCollection realFaults;

    // // EXPERIMENT SETUP // //

    /** iBugs bug id that was used to create the spectra */
    private final int bugId;
    /** Holds the fault localizer used by this experiment */
    private final IFaultLocalizer<SourceCodeLine> localizer;
    /** Holds the spectra to localize faults on */
    private final ISpectra<SourceCodeLine> spectra;
    /** True if experiment ran already, false if not */
    private boolean hasRun;


    // // EXPERIMENT RESULTS // //

    /** Holds the produced ranking */
    private SimpleRanking<INode<SourceCodeLine>> ranking;
    /** Holds the real fault locations */
    private Set<INode<SourceCodeLine>> realFaultLocations;



    /**
     * Creates a new experiment.
     *
     * @param bugId
     *            iBugs bug id that was used to create the spectra
     * @param spectra
     *            the spectra to base the experiment on
     * @param localizer
     *            the fault localizer to use
     * @param realFaults
     *            to determine the real fault locations
     */
    public Experiment(final int bugId, final ISpectra<SourceCodeLine> spectra, final IFaultLocalizer<SourceCodeLine> localizer,
            final IBugsFaultLocationCollection realFaults) {
        this.bugId = bugId;
        this.spectra = spectra;
        this.localizer = localizer;
        this.realFaults = realFaults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void conduct() throws Exception {
        if (this.hasRun) {
            throw new RuntimeException(
                    "Cannot run this experiment multiple times. Please initialize a new experiment instance.");
        }
        this.hasRun = true;

        // localize
        this.log.log(Level.INFO, "Begin: fault localization");
        final long begin = System.currentTimeMillis();
        this.ranking = (SimpleRanking<INode<SourceCodeLine>>) this.localizer.localize(this.spectra);
        this.log.log(Level.INFO,
                String.format("End: fault localization. Duration: %d ms", System.currentTimeMillis() - begin));
        this.realFaultLocations = this.realFaults.getFaultyNodesFor(this.bugId, this.spectra);
    }

    /**
     * Returns the ranking produced by this experiment.
     *
     * @return the ranking
     */
    public SimpleRanking<INode<SourceCodeLine>> getRanking() {
        assert this.hasRun;
        return this.ranking;
    }

    /**
     * Returns the real fault location nodes of this experiment.
     *
     * @return the realFaultLocations
     */
    public Set<INode<SourceCodeLine>> getRealFaultLocations() {
        assert this.hasRun;
        return this.realFaultLocations;
    }

    /**
     * Returns the fault localizer used by this experiment
     *
     * @return the localizer
     */
    public IFaultLocalizer<SourceCodeLine> getLocalizer() {
        return this.localizer;
    }

    /**
     * Returns the bug id used by this experiment
     *
     * @return the bugId
     */
    public int getBugId() {
        return this.bugId;
    }

}

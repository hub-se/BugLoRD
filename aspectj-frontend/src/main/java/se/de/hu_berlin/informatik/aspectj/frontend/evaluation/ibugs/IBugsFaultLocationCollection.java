/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import se.de.hu_berlin.informatik.aspectj.frontend.IBugsFileWithFaultLocations;
import se.de.hu_berlin.informatik.aspectj.frontend.IBugsBug;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.IBugsFaultLocationCollection;
import se.de.hu_berlin.informatik.benchmark.Bug;
import se.de.hu_berlin.informatik.benchmark.FileWithFaultLocations;
import se.de.hu_berlin.informatik.benchmark.SimpleLineWithFaultInformation;
import se.de.hu_berlin.informatik.benchmark.FaultInformation.Suspiciousness;
import se.de.hu_berlin.informatik.stardust.provider.CoberturaProvider;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;

/**
 * Used to store the real fault locations of iBugs.
 */
public class IBugsFaultLocationCollection {

    /** Holds the logger for this class */
    private final Logger logger = Logger.getLogger(IBugsFaultLocationCollection.class.getName());

    /** Holds all bugs with their locations */
    private final Map<Integer, Bug> bugs = new TreeMap<>();

    /**
     * Create new real fault locations object
     *
     * @param file
     *            xml file storing the real faults
     * @throws JDOMException
     *             in case we cannot parse the fault file
     * @throws IOException
     *             in case we cannot parse the fault file
     */
    public IBugsFaultLocationCollection(final String file) throws JDOMException, IOException {
        this(new java.io.File(file));
    }

    /**
     * Create new real fault locations object
     *
     * @param file
     *            xml file storing the real faults
     * @throws JDOMException
     *             in case we cannot parse the fault file
     * @throws IOException
     *             in case we cannot parse the fault file
     */
    public IBugsFaultLocationCollection(final java.io.File file) throws JDOMException, IOException {
        this.parse(file);
    }

    /**
     * Parses a real fault locations file.
     *
     * @param faultLocationFile
     *            the file to parse
     * @throws IOException
     * @throws JDOMException
     */
    private void parse(final java.io.File faultLocationFile) throws JDOMException, IOException {
        final Document doc = new SAXBuilder().build(faultLocationFile);

        // loop over all bugs of the real fault locations file
        for (final Object bugObject : doc.getRootElement().getChildren()) {
            final Element bug = (Element) bugObject;

            // create bug object
            IBugsBug curBug;
            try {
                curBug = new IBugsBug(Integer.parseInt(bug.getAttributeValue("id")));
            } catch (final NumberFormatException e1) {
                this.logger.log(Level.INFO,
                        String.format("Could not parse ID while parsing %s", faultLocationFile.getAbsolutePath()), e1);
                continue;
            }
            this.bugs.put(curBug.getId(), curBug);


            // get files
            for (final Object fileObject : bug.getChildren()) {
                final Element file = (Element) fileObject;
                final String filename = file.getAttributeValue("name");

                // ensure we have java extension and no test sources
                if (filename == null 
                		|| !FileUtils.getFileExtension(filename).equals("java")
                        || filename.toLowerCase(Locale.getDefault()).indexOf("test") != -1) {
                    continue;
                }

                // create fault file object
                final FileWithFaultLocations faultFile = new IBugsFileWithFaultLocations(filename);
                curBug.addFile(faultFile);


                // get involved lines
                for (final Object lineObj : file.getChildren()) {
                    final Element line = (Element) lineObj;

                    // parse line info
                    int lineNumber;
                    try {
                        lineNumber = Integer.parseInt(line.getText().trim());
                    } catch (final NumberFormatException | NullPointerException e1) {
                        this.logger.log(Level.INFO, String.format("Could not parse line number '%s' while parsing %s",
                                line.getText(), faultLocationFile.getAbsolutePath()), e1);
                        continue;
                    }

                    Suspiciousness suspiciousness = null;
                    try {
                        suspiciousness = Suspiciousness.valueOf(
                        		line.getAttributeValue("suspiciousness").trim().toUpperCase());
                    } catch (final Exception e) { // NOCS
                        this.logger.log(
                                Level.INFO,
                                String.format("Could not parse suspiciousness '%s' while parsing %s",
                                        line.getAttributeValue("suspiciousness"), faultLocationFile.getAbsolutePath()),
                                        e);
                    } finally {
                        if (suspiciousness == null) {
                            suspiciousness = Suspiciousness.UNKNOWN;
                        }
                    }
                    final String comment = line.getAttributeValue("comment");

                    // add it to file
                    faultFile.addFaultyLine(new SimpleLineWithFaultInformation(lineNumber, suspiciousness, comment));
                }
            }
        }
    }

    /**
     * Check whether a bug id is contained in this set.
     *
     * @param bugId
     *            the bug id to check
     * @return true if fault info is contained, false otherwise
     */
    public boolean hasBug(final int bugId) {
        return this.bugs.containsKey(bugId);
    }

    /**
     * Returns a certain bug
     *
     * @param bugId
     *            the bug to get
     * @return bug fault info object
     */
    public Bug getBug(final int bugId) {
        return this.bugs.get(bugId);
    }

    /**
     * Returns all faulty nodes of a spectra for a given bug id.
     *
     * @param bugId
     *            the bug id to get the faulty lines of
     * @param spectra
     *            to fetch the nodes from
     * @return list of faulty nodes
     */
    public Set<INode<String>> getFaultyNodesFor(final int bugId, final ISpectra<String> spectra) {
        final Set<INode<String>> locations = new HashSet<>();
        if (!this.hasBug(bugId)) {
            return locations;
        }
        Bug bug = this.getBug(bugId);
        // get node for each churned line
        for (final FileWithFaultLocations file : bug.getFaultyFiles()) {
            for (final int lineNo : file.getFaultyLineNumbers()) {
                final String nodeId = CoberturaProvider.createNodeIdentifier(file.getFileName(), lineNo);
                if (spectra.hasNode(nodeId)) {
                    locations.add(spectra.getNode(nodeId));
                } else {
                    this.logger.log(Level.WARNING, String.format("Node %s could not be found in spectra.", nodeId));
                }
            }
        }
        return locations;
    }

}

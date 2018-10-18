/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.aspectj.frontend.evaluation.lines;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.IBugsHierarchical;
import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.machinelearn.WekaFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.AbstractSpectra;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.CoberturaSpectraProviderFactory;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.xml.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.RankingMetric;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Evaluates the ranking of given traces by modifying single line involvements.
 */
public final class LineEvaluator {

    /** Result writer */
    private static FileWriter writer;
    /** Performance benchmarking */
    private static Map<String, Long> benchmarks = new HashMap<>();

    /**
     * Don't call this constructor
     */
    private LineEvaluator() {
    }

    /**
     * Performance benchmark method.
     *
     * Call once with an ID to track begin and call it again with the same ID to stop the benchmark. Method will print
     * both invocations to the console.
     *
     * @param id
     *            benchmark identifier
     */
    public static void perf(final String id) {
        if (!benchmarks.containsKey(id)) {
            final long begin = System.currentTimeMillis();
            benchmarks.put(id, begin);
            Log.out(LineEvaluator.class, String.format("-- Begin: %s", id));
        } else {
            final long duration = System.currentTimeMillis() - benchmarks.get(id);
            Log.out(LineEvaluator.class, String.format("-- End: %s, Duration: %fs", id, new Double(duration) / 1000d));
            benchmarks.remove(id);
        }
    }

    /**
     * Runs experiment
     *
     * @param args
     *            CLI args - dont use
     * @throws Exception
     *             in case the experiment fails
     */
    public static void main(final String[] args) throws Exception {

        // experiment parameters
        final int bugId = 36430;
        final String pathToTraceFolder = "traces";
        final String pathToResultFolder = "experiments";
        final int[] lineIFs = { 1, 3, 5, 10, 25 };
        final int[] lineISs = { 1, 3, 5, 10, 25 };
        final int maxSuccessfulTraces = 25;
        final int maxFailingTraces = 25;
        final IFaultLocalizer<SourceCodeBlock> localizer = new WekaFaultLocalizer<SourceCodeBlock>(IBugsHierarchical.NaiveBayes);

        // initialization
        writer = new FileWriter(pathToResultFolder + "/result-" + bugId + ".csv");
        writer.write(CSVUtils.toCsvLine(new String[] { "BugID", "line", "IF", "IS", "NF", "NS", "BestRanking",
                "WorstRanking", "MinWastedEffort", "MaxWastedEffort", "Suspiciousness", })
                + "\n");

        final CoberturaXMLProvider<HitTrace<SourceCodeBlock>> provider = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(true);
        int added = 0;
        boolean success = false;
        for (final String path : traces(pathToTraceFolder + "/" + bugId + "/pre-fix", maxSuccessfulTraces
                + maxFailingTraces)) {
            if (added == maxFailingTraces) {
                success = true;
            }
            if (!provider.addData(path, null, success)) {
            	throw new IllegalStateException("Adding coverage trace failed.");
            }
            added++;
        }

        final ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> original = provider.loadSpectra();
        if (!(original instanceof HitSpectra)) {
        	Log.abort(LineEvaluator.class, "Loaded spectra is not of correct type...");
        }
        Log.out(LineEvaluator.class, "Spectra loaded");
        int line = 0;
        for (final INode<SourceCodeBlock> node : original.getNodes()) {
            if (line % 100 == 0) {
            	Log.out(LineEvaluator.class, String.format("Progress: line %d of %d", line, original.getNodes().size()));
            }
            line++;

            final SourceCodeBlock identifier = node.getIdentifier();
            // create a clone
            perf("clone");
            @SuppressWarnings("unchecked")
			final AbstractSpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> spectra = ((HitSpectra<SourceCodeBlock>) original).clone();
            perf("clone");

            // set node involvement to none
            for (final ITrace<SourceCodeBlock> trace : spectra.getTraces()) {
                trace.setInvolvement(node, false);
            }

            for (final int lineIF : lineIFs) {
                for (final int lineIS : lineISs) {
                    int curIF = 0;
                    int curIS = 0;

                    // set enough traces to either failing or successful
                    for (final ITrace<SourceCodeBlock> trace : spectra.getTraces()) { // NOCS: sorry nested depth
                        if (trace.isSuccessful() && curIS < lineIS) {
                            trace.setInvolvement(identifier, true);
                            curIS++;
                        } else if (!trace.isSuccessful() && curIF < lineIF) {
                            trace.setInvolvement(identifier, true);
                            curIF++;
                        }
                    }

                    // assert we have enough traces of each kind
                    if (curIS != lineIS) {
                        throw new Exception(String.format("Could only involve %d of %d successful traces!", curIS,
                                lineIS));
                    }
                    if (curIF != lineIF) {
                        throw new Exception(String.format("Could only involve %d of %d failing traces!", curIF, lineIF));
                    }

                    // gather ranking position
                    perf("rank");
                    final Ranking<INode<SourceCodeBlock>> ranking = localizer.localize(spectra);
                    perf("rank");
                    final RankingMetric<INode<SourceCodeBlock>> metric = ranking.getRankingMetrics(spectra.getOrCreateNode(identifier));
                    writeLine(bugId, line, curIF, curIS, maxFailingTraces - curIF, maxSuccessfulTraces - curIS,
                            metric.getBestRanking(), metric.getWorstRanking(), metric.getMinWastedEffort(),
                            metric.getMaxWastedEffort(), metric.getRankingValue());
                }
            }
        }

        writer.flush();
        writer.close();
    }


    private static void writeLine(final int bugId, final int line, final int lineIF, final int lineIS,
            final int lineNF, final int lineNS, final int bestRanking, final int worstRanking,
            final double minWastedEffort, final double maxWastedEffort, final double suspiciousness) throws IOException {
        assert writer != null;
        final String csv = CSVUtils.toCsvLine(new String[] { Integer.toString(bugId), Integer.toString(line),
                Integer.toString(lineIF), Integer.toString(lineIS), Integer.toString(lineNF), Integer.toString(lineNS),
                Integer.toString(bestRanking), Integer.toString(worstRanking), Double.toString(minWastedEffort),
                Double.toString(maxWastedEffort), Double.toString(suspiciousness), });
        writer.write(csv + "\n");
        writer.flush();
    }

    private static List<String> traces(final String path, final int max) throws Exception {
        final List<String> traceFiles = new ArrayList<>();
        for (final File trace : new File(path).listFiles((FileFilter) pathname -> {
            if (!pathname.isFile()) {
                return false;
            }
            final String fileExtension = getFileExtension(pathname);
            if (0 != "xml".compareTo(fileExtension)) {
                return false;
            }
            if (!pathname.getName().matches("^[pf]_.+")) {
                return false;
            }
            return true;
        })) {
            traceFiles.add(trace.getAbsolutePath());
        }
        if (traceFiles.size() < max) {
            throw new Exception(String.format("Found only %d of %d trace files in %s.", traceFiles.size(), max, path));
        }
        return traceFiles.subList(0, max);
    }

    private static String getFileExtension(final File file) {
        final String name = file.getName();
        final int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }
}

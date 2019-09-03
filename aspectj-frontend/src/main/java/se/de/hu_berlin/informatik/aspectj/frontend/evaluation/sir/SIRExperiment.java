/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.aspectj.frontend.evaluation.sir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.sir.SIRExperiment;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.NodeRanking;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.utils.experiments.ranking.RankingMetric;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class SIRExperiment {

    private static final String INPUT_DIR = "experiments/SIR/input";
    private static final String OUTPUT_DIR = "experiments/SIR/output";
    private final File faults;

    public static void main(final String[] args) throws IOException {
        new SIRExperiment();
    }

    public SIRExperiment() throws IOException {
        Log.out(this, "Starting experiment");
        // init real fault locations file
        this.faults = new File(OUTPUT_DIR + "/sir-faults.csv");
        final String[] line = { "Program", "OriginalFile", "NodeID", "BestRanking", "WorstRanking", "MinWastedEffort",
                "MaxWastedEffort", "Suspiciousness", };
        Files.write(this.faults.toPath(),
                (CSVUtils.toCsvLine(line) + System.lineSeparator()).getBytes(Charset.forName("UTF-8")));

        // process each file
        final File input = new File(INPUT_DIR);
        Arrays.stream(Objects.requireNonNull(input.listFiles((file, name) -> !name.startsWith(".") && name.endsWith(".txt")))).forEach(
                unchecked(file -> {
                    this.localize(file);
                }));
    }

    /**
     * This utility simply wraps a functional interface that throws a checked exception into a Java 8 Consumer
     */
    private static <T> Consumer<T> unchecked(final CheckedConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (final Exception e) { // NOCS
                throw new RuntimeException(e);
            }
        };
    }

    @FunctionalInterface
    private interface CheckedConsumer<T> {
        void accept(T t) throws Exception;
    }

    /**
     * Executes one input file
     *
     * @param input
     *            file
     * @throws IOException
     */
    private void localize(final File input) throws IOException {
        // start processing
        final String name = input.getName().substring(0, input.getName().length() - 4);
        Log.out(this, "Processing " + name);
        final String program = name.substring(0, name.indexOf("_"));

        // create ranking
        final SIRRankingProvider provider = new SIRRankingProvider(input);
        final NodeRanking<Integer> ranking = provider.getRanking();
        ranking.save(OUTPUT_DIR + "/" + name + "-ranking.txt");

        // append to faults file
        final RankingMetric<INode<Integer>> m = ranking.getRankingMetrics(provider.getFault());
        final String[] line = { program, name, provider.getFault().toString(), Integer.toString(m.getBestRanking()),
                Integer.toString(m.getWorstRanking()), Double.toString(m.getMinWastedEffort()),
                Double.toString(m.getMaxWastedEffort()), Double.toString(m.getRankingValue()), };
        Files.write(this.faults.toPath(),
                (CSVUtils.toCsvLine(line) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.APPEND);
    }

    class SIRRankingProvider {

        /** Holds the path to the block IF/IP/NF/NP file */
        private final File file;
        /** Contains the actual ranking */
        private NodeRanking<Integer> ranking;
        /** Contains the node of the real fault location */
        private INode<Integer> fault;

        public SIRRankingProvider(final File file) throws IOException {
            this.file = file;
            this.createRanking();
        }

        private void createRanking() throws IOException {
            // create variables
            final Stream<String> lines = Files.lines(this.file.toPath());
            final ISpectra<Integer,?> spectra = new HitSpectra<>(null);
            final Integer[] failedNode = { null };
            final int[] curNode = { 0 };
            final NodeRanking<Integer> rank = new NodeRanking<>();

            // parse lines
            lines.forEachOrdered(line -> {
                if (failedNode[0] == null) {
                    failedNode[0] = Integer.parseInt(line.trim());
                } else {
                    final INode<Integer> node = spectra.getOrCreateNode(curNode[0]);
                    rank.add(node, this.tarantula(line));

                    // hack for java not allowing access to non-final variables
                    curNode[0] += 1;
                }
            });

            // close & return
            lines.close();
            this.ranking = rank;
            this.fault = spectra.getOrCreateNode(failedNode[0]);
        }

        private double tarantula(final String line) {
            // parse line
            final String[] parts = line.split(",");
            assert parts.length == 4;

            final int cIP = Integer.parseInt(parts[0].trim());
            final int cIF = Integer.parseInt(parts[1].trim());
            final int cNP = Integer.parseInt(parts[2].trim());
            final int cNF = Integer.parseInt(parts[3].trim());

            // tarantula
            final double part = (double) cIF / (double) (cIF + cNF);
            return part / (part + cIP / (double) (cIP + cNP));
        }

        public NodeRanking<Integer> getRanking() {
            return this.ranking;
        }

        public INode<Integer> getFault() {
            return this.fault;
        }
    }

}

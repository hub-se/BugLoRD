package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import org.apache.oro.text.regex.Pattern;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageThreshold;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.FileFinder;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

/**
 * Encapsulates arguments;
 */
public class Arguments {

    private final String baseDirectory;
    private final File dataFile;
    private final File destinationDirectory;
    private final File commandsFile;
    private final FileFinder sources;

    private final Collection<Pattern> ignoreRegexes;
    private final Collection<Pattern> ignoreBranchesRegexes;
    private final Collection<Pattern> classPatternIncludeClassesRegexes;
    private final Collection<Pattern> classPatternExcludeClassesRegexes;
    private final boolean calculateMethodComplexity;
    private final boolean failOnError;
    private final boolean ignoreTrivial;
    private final boolean collectExecutionTraces;
    private final boolean threadsafeRigorous;

    private final String encoding;

    private final Set<CoverageThreshold> minimumCoverageThresholds;
    private final double classLineThreshold;
    private final double classBranchThreshold;
    private final double packageLineThreshold;
    private final double packageBranchThreshold;
    private final double totalLineThreshold;
    private final double totalBranchThreshold;

    private final List<CoberturaFile> filesToInstrument;
    private final Set<File> filesToMerge;
    private final Set<String> ignoreMethodAnnotations;
    private final Set<String> ignoreClassAnnotations;

    Arguments(String baseDirectory, File dataFile, File destinationDirectory,
              File commandsFile, Collection<Pattern> ignoreRegexes,
              Collection<Pattern> ignoreBranchesRegexes,
              Collection<Pattern> classPatternIncludeClassesRegexes,
              Collection<Pattern> classPatternExcludeClassesRegexes,
              boolean calculateMethodComplexity,
              boolean failOnError, boolean ignoreTrivial, boolean collectExecutionTraces,
              boolean threadsafeRigorous, String encoding,
              Set<CoverageThreshold> minimumCoverageThresholds,
              double classLineThreshold, double classBranchThreshold,
              double packageLineThreshold, double packageBranchThreshold,
              double totalLineThreshold, double totalBranchThreshold,
              List<CoberturaFile> filesToInstrument, Set<File> filesToMerge,
              Set<String> ignoreMethodAnnotations,
              Set<String> ignoreClassAnnotations, FileFinder sources) {
        this.baseDirectory = baseDirectory;
        this.dataFile = dataFile;
        this.destinationDirectory = destinationDirectory;
        this.commandsFile = commandsFile;
        this.ignoreRegexes = ignoreRegexes;
        this.sources = sources;
        this.ignoreBranchesRegexes = Collections
                .unmodifiableCollection(ignoreBranchesRegexes);
        this.classPatternIncludeClassesRegexes = Collections
                .unmodifiableCollection(classPatternIncludeClassesRegexes);
        this.classPatternExcludeClassesRegexes = Collections
                .unmodifiableCollection(classPatternExcludeClassesRegexes);
        this.calculateMethodComplexity = calculateMethodComplexity;
        this.failOnError = failOnError;
        this.ignoreTrivial = ignoreTrivial;
        this.collectExecutionTraces = collectExecutionTraces;
        this.threadsafeRigorous = threadsafeRigorous;
        this.encoding = encoding;
        this.minimumCoverageThresholds = Collections
                .unmodifiableSet(minimumCoverageThresholds);
        this.classLineThreshold = classLineThreshold;
        this.classBranchThreshold = classBranchThreshold;
        this.packageLineThreshold = packageLineThreshold;
        this.packageBranchThreshold = packageBranchThreshold;
        this.totalLineThreshold = totalLineThreshold;
        this.totalBranchThreshold = totalBranchThreshold;
        this.filesToInstrument = Collections.unmodifiableList(filesToInstrument);
        this.filesToMerge = Collections.unmodifiableSet(filesToMerge);
        this.ignoreMethodAnnotations = Collections
                .unmodifiableSet(ignoreMethodAnnotations);
        this.ignoreClassAnnotations = Collections
                .unmodifiableSet(ignoreClassAnnotations);
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public File getDataFile() {
        return dataFile;
    }

    public File getDestinationDirectory() {
        return destinationDirectory;
    }

    public File getCommandsFile() {
        return commandsFile;
    }

    public Collection<Pattern> getIgnoreRegexes() {
        return ignoreRegexes;
    }

    public Collection<Pattern> getIgnoreBranchesRegexes() {
        return ignoreBranchesRegexes;
    }

    public Collection<Pattern> getClassPatternIncludeClassesRegexes() {
        return classPatternIncludeClassesRegexes;
    }

    public Collection<Pattern> getClassPatternExcludeClassesRegexes() {
        return classPatternExcludeClassesRegexes;
    }

    public boolean isCalculateMethodComplexity() {
        return calculateMethodComplexity;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public boolean isIgnoreTrivial() {
        return ignoreTrivial;
    }

    public boolean collectExecutionTraces() {
        return collectExecutionTraces;
    }

    public boolean isThreadsafeRigorous() {
        return threadsafeRigorous;
    }

    public String getEncoding() {
        return encoding;
    }

    public Set<CoverageThreshold> getMinimumCoverageThresholds() {
        return minimumCoverageThresholds;
    }

    public double getClassLineThreshold() {
        return classLineThreshold;
    }

    public double getClassBranchThreshold() {
        return classBranchThreshold;
    }

    public double getPackageLineThreshold() {
        return packageLineThreshold;
    }

    public double getPackageBranchThreshold() {
        return packageBranchThreshold;
    }

    public double getTotalLineThreshold() {
        return totalLineThreshold;
    }

    public double getTotalBranchThreshold() {
        return totalBranchThreshold;
    }

    public List<CoberturaFile> getFilesToInstrument() {
        return filesToInstrument;
    }

    public Set<File> getFilesToMerge() {
        return filesToMerge;
    }

    public Set<String> getIgnoreMethodAnnotations() {
        return ignoreMethodAnnotations;
    }

    public FileFinder getSources() {
        return sources;
    }

    public Set<String> getIgnoreClassAnnotations() {
        return ignoreClassAnnotations;
    }
}

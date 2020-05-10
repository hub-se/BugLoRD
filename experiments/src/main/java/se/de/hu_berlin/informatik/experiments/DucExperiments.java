package se.de.hu_berlin.informatik.experiments;

import org.junit.Test;
import se.de.hu_berlin.informatik.gen.ranking.modules.RankingModule;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranchSpectra;
import se.de.hu_berlin.informatik.spectra.core.branch.StatementSpectraToBranchSpectra;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class DucExperiments {

    @Test
    public void bla() {

        //assert(false);

        final String traceLocations = "../../resources/spectraTraces";
        Path path = Paths.get(traceLocations, "Lang-10b.zip");
        ISpectra<SourceCodeBlock, ? extends ITrace<SourceCodeBlock>>
                statementSpectra = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, path);

        ProgramBranchSpectra programBranchSpectra = StatementSpectraToBranchSpectra.generateBranchingSpectraFromStatementSpectra(statementSpectra, "");


        Path output = Paths.get(traceLocations, "branchingSpectra.zip");
        SpectraFileUtils.saveSpectraToZipFile(programBranchSpectra, output, true, false);

        final String[] localizers = {"jaccard"};
        final String outputDir = "../../resources/rankings";
        final ComputationStrategies strategy = ComputationStrategies.STANDARD_SBFL;

        new RankingModule<Collection<SourceCodeBlock>>(strategy, outputDir, localizers).submit(programBranchSpectra);


    }

}

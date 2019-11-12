package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings.getStdResourcesDir;

class NGramSetTest {
    @org.junit.jupiter.api.Test
    void generateNGSet() {
        Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");

        ISpectra<SourceCodeBlock, ?> input = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
        System.out.println("number of test: " + input.getTraces().size());
        LinearExecutionHitTrace hitTrace = new LinearExecutionHitTrace(input);
        NGramSet nGrams = new NGramSet(hitTrace, 3, 0.9);
        System.out.println("ngram set size : " + nGrams.getResult().size());
//        nGrams.getResult().forEach(e->{
//            double EF = e.getEF();
//            double ET = e.getET();
//            double conf = e.getConfidence();
//
//            System.out.print("["+ Arrays.toString(e.getBlockIDs()) +"] ");
//            System.out.println("EF: "+EF +", ET: " + ET +", CONFIDENCE: "+conf);
//        });
        nGrams.getResultAsText().forEach(e -> System.out.println(e));
    }

}
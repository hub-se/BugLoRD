package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;


/**
 * Holds data about coverage thresholds for given regexes
 */
public class CoverageThreshold {
    private final String regex;
    private final double minBranchPercentage;
    private final double minLinePercentage;

    /**
     * Costructor
     *
     * @param regex               - a regex expression
     * @param minBranchPercentage -minimum expected branch coverage percentage
     * @param minLinePercentage   -minimum expected line coverage percentage
     */
    public CoverageThreshold(String regex, double minBranchPercentage,
                             double minLinePercentage) {
        this.regex = regex;
        this.minBranchPercentage = minBranchPercentage;
        this.minLinePercentage = minLinePercentage;
    }

    public String getRegex() {
        return regex;
    }

    public double getMinBranchPercentage() {
        return minBranchPercentage;
    }

    public double getMinLinePercentage() {
        return minLinePercentage;
    }
}

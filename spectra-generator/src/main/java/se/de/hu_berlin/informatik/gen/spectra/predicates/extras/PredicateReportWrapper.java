package se.de.hu_berlin.informatik.gen.spectra.predicates.extras;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.NativeReport;

public class PredicateReportWrapper {

    final private NativeReport report;
    final private boolean successful;
    final String testIdentifier;
    final Profile profile;

    public PredicateReportWrapper(final NativeReport report, final String testIdentifier, final boolean successful, Profile testPredicateProfile) {
        this.report = report;
        this.successful = successful;
        this.testIdentifier = testIdentifier;
        this.profile = testPredicateProfile;
    }

    public NativeReport getReport() {

        return report;
    }

    public String getIdentifier() {

        return testIdentifier;
    }

    public boolean isSuccessful() {

        return successful;
    }

    public Profile getProfile() {
        return this.profile;
    }
}

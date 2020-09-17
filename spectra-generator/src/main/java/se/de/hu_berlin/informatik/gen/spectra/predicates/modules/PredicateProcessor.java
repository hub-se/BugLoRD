package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;

import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.PredicateReportWrapper;
import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.Profile;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

public class PredicateProcessor extends AbstractProcessor<PredicateReportWrapper, Profile> {

    public PredicateProcessor(String OutputDir) {
        super();
    }



    @Override
    public Profile processItem(final PredicateReportWrapper reportWrapper) {
        if (reportWrapper.getProfile().predicates.isEmpty()) {
            Log.err(this,reportWrapper.getIdentifier() + " had zero Triggers?!");
        }
     return reportWrapper.getProfile();
    }

}

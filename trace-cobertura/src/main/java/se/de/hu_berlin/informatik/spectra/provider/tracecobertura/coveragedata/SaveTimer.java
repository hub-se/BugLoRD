package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

import java.util.TimerTask;

@CoverageIgnore
public class SaveTimer extends TimerTask {

    public SaveTimer() {
    }

    public void run() {
        ProjectData.saveGlobalProjectData();
    }
}
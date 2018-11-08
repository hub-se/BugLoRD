package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


import java.util.TimerTask;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

@CoverageIgnore
public class SaveTimer extends TimerTask {

	public SaveTimer() {
	}

	public void run() {
		ProjectData.saveGlobalProjectData();
	}
}
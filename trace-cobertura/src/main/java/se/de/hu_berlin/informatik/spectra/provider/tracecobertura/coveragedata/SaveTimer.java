package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


import java.util.TimerTask;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

@CoverageIgnore
public class SaveTimer extends TimerTask {

	private boolean collectExecutionTraces;

	public SaveTimer(boolean collectExecutionTraces) {
		this.collectExecutionTraces = collectExecutionTraces;
	}

	public void run() {
		ProjectData.saveGlobalProjectData(collectExecutionTraces);
	}
}
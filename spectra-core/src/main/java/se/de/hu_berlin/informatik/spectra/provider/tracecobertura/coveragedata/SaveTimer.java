package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import net.sourceforge.cobertura.CoverageIgnore;

import java.util.TimerTask;

@CoverageIgnore
public class SaveTimer extends TimerTask {

	public void run() {
		TraceProjectData.saveGlobalProjectData();
	}
}
package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Arguments;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CodeInstrumentationTask;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CoverageDataFileHandler;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;

/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
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

public class Cobertura {

	private final Arguments args;
	private ProjectData projectData;
	private final CodeInstrumentationTask instrumentationTask;

	public Cobertura(Arguments arguments) {
		args = arguments;
		instrumentationTask = new CodeInstrumentationTask(args.collectExecutionTraces());
	}

	/**
	 * Instruments the code. Should be invoked after compiling.
	 * Classes to be instrumented are taken from constructor args
	 * @return this Cobertura instance
	 * @throws Throwable
	 * if one is thrown
	 */
	public Cobertura instrumentCode() throws Throwable {
		instrumentationTask.instrument(args, getProjectDataInstance());
		return this;
	}

	/**
	 * Serializes project data to file specified in constructor args
	 * @return this Cobertura instance
	 */
	public Cobertura saveProjectData() {
		CoverageDataFileHandler.saveCoverageData(getProjectDataInstance(), args
				.getDataFile());
		return this;
	}

	/*  Aux methods  */
	private ProjectData getProjectDataInstance() {
		// Load project data; see notes at the beginning of CodeInstrumentationTask class
		if (projectData != null) {
			return projectData;
		}
		if (args.getDataFile().isFile())
			projectData = CoverageDataFileHandler.loadCoverageData(args
					.getDataFile());
		if (projectData == null)
			projectData = new ProjectData();

		return projectData;
	}
}

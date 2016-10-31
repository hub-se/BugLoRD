/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.nio.file.Paths;

import se.de.hu_berlin.informatik.rankingplotter.plotter.Plot;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DataTableCollection;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * A module that takes a {@link DataTableCollection} object and produces a
 * {@link Plot} object. Also shows the plot in a frame and saves it as a
 * png and/or a pdf file.
 * 
 * @author Simon Heiden
 */
public class PlotModule extends AbstractModule<DataTableCollection, Plot> {

	private String outputPrefix;
	private boolean plotAll;
	private boolean plotMFR;
	private boolean plotHitAtX;

	/**
	 * Creates a new {@link PlotModule} object with the given parameters.
	 * @param outputPrefix
	 * the output filename prefix 
	 * @param plotAll
	 * whether the data of all tables should be plotted without differentiation
	 * @param plotMFR
	 * whether the mean first rank should be plotted
	 * @param plotHitAtX
	 * whether the HitAtX rankings should be plotted
	 */
	public PlotModule(
			String outputPrefix,
			boolean plotAll, boolean plotMFR, boolean plotHitAtX) {
		super(true);
		this.outputPrefix = outputPrefix;
		this.plotAll = plotAll;
		this.plotMFR = plotMFR;
		this.plotHitAtX = plotHitAtX;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public Plot processItem(DataTableCollection tables) {

		if (plotAll) {
			//create plot
			Plot test = new Plot(tables, 5);

			test.saveData(0, Paths.get(outputPrefix.toString() + ".all"));

		}

		if (plotMFR) {
			//create plot
			Plot test = new Plot(tables, 6);

			test.saveData(0, Paths.get(outputPrefix.toString() + ".MFR"));

		}

		if (plotHitAtX) {
			int[] xArray = { 1, 5, 10, 20, 30, 50, 100 };
			for (int i = 0; i < xArray.length; ++i) {
				//create plot
				Plot test = new Plot(tables, i + 7);

				test.saveData(0, Paths.get(outputPrefix.toString() + ".hitAt" + xArray[i]));

			}

			//create plot
			Plot test = new Plot(tables, 18);

			test.saveData(0, Paths.get(outputPrefix.toString() + ".hitAtInf"));

		}

		//create plot
		Plot test = new Plot(tables, 14, 15, 16, 17);

		test.saveData(0, Paths.get(outputPrefix.toString() + ".changes"));
		test.saveData(1, Paths.get(outputPrefix.toString() + ".deletes"));
		test.saveData(2, Paths.get(outputPrefix.toString() + ".inserts"));
		test.saveData(3, Paths.get(outputPrefix.toString() + ".unknown"));

		//create plot
		test = new Plot(tables, 0, 1, 2, 3, 4);

		test.saveData(0, Paths.get(outputPrefix.toString() + ".unsignificant"));
		test.saveData(1, Paths.get(outputPrefix.toString() + ".low_significance"));
		test.saveData(2, Paths.get(outputPrefix.toString() + ".medium_significance"));
		test.saveData(3, Paths.get(outputPrefix.toString() + ".high_significance"));
		test.saveData(4, Paths.get(outputPrefix.toString() + ".crucial_significance"));

		return test;

	}

	/**
	 * Sets a new output filename prefix.
	 * @param outputPrefix
	 * the output filename prefix
	 */
	public void setOutputPrefix(String outputPrefix) {
		this.outputPrefix = outputPrefix;
	}
}

/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.awt.GraphicsEnvironment;
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

	private boolean useLabels;
	private boolean connectPoints;
	private String title;
	private Integer[] range;
	private boolean pdf;
	private boolean png;
	private boolean eps;
	private boolean svg;
	private String outputPrefix;
	private boolean showPanel;
	private boolean saveData;
	private boolean autoSizeY;
	private boolean singlePlots;
	private boolean plotAll;
	private Integer[] autoSizeYcolumns;
	private Integer plotHeight;
	private boolean plotMFR;
	private boolean plotHitAtX;
	
	/**
	 * Creates a new {@link PlotModule} object with the given parameters.
	 * @param useLabels
	 * if letters should be used as labels instead of dots
	 * @param connectPoints
	 * sets whether data points should be connected with lines
	 * @param title
	 * the title of the plot
	 * @param range
	 * the maximum value of the y-axis
	 * @param pdf
	 * sets if the output shall be saved as a PDF file. (Larger file size, but good quality.)
	 * @param png
	 * sets if the output shall be saved as a PNG file. (Smaller file size, but bad quality.)
	 * @param eps
	 * sets if the output shall be saved as an EPS file. (Larger file size, but good quality.)
	 * @param svg
	 * sets if the output shall be saved as an SVG file. (Medium-large file size, good quality.)
	 * @param outputPrefix
	 * the output filename prefix 
	 * @param showPanel
	 * whether the plot should be shown in a panel (if possible)
	 * @param saveData
	 * save the plot data in .csv files
	 *  @param autoSizeY
	 * whether the plot should be automatically sized, regarding the y-axis
	 * @param autoSizeYcolumns
	 * an array of columns that should be considered for auto sizing
	 * @param plotHeight
	 * a fixed height for the plot, or null
	 * @param singlePlots
	 * whether each data table shall be plotted separately
	 * @param plotAll
	 * whether the data of all tables should be plotted without differentiation
	 * @param plotMFR
	 * whether the mean first rank should be plotted
	 * @param plotHitAtX
	 * whether the HitAtX rankings should be plotted
	 */
	public PlotModule(
			boolean useLabels, boolean connectPoints, String title, Integer[] range, 
			boolean pdf, boolean png, boolean eps, boolean svg, String outputPrefix,
			boolean showPanel, boolean saveData, boolean autoSizeY, 
			Integer[] autoSizeYcolumns, Integer plotHeight, boolean singlePlots, 
			boolean plotAll, boolean plotMFR, boolean plotHitAtX) {
		super(true);
		this.useLabels = useLabels;
		this.connectPoints = connectPoints;
		this.title = title;
		this.range = range;
		this.pdf = pdf;
		this.png = png;
		this.eps = eps;
		this.svg = svg;
		this.outputPrefix = outputPrefix;
		this.showPanel = showPanel;
		this.saveData = saveData;
		this.autoSizeY = autoSizeY;
		this.autoSizeYcolumns = autoSizeYcolumns;
		this.singlePlots = singlePlots;
		this.plotAll = plotAll;
		this.plotMFR = plotMFR;
		this.plotHitAtX = plotHitAtX;
		this.plotHeight = plotHeight;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public Plot processItem(DataTableCollection tables) {
		Integer temp = null;
		if (range == null && !autoSizeY) {
			temp = Plot.DEFAULT_RANGE;
//			temp = range[1] - range[0];
		} else if (range != null && range.length > 0) {
			temp = range[0];
		}
		
		if (singlePlots) {
			for (int i = 0; i < 5; ++i) {
				int minY = 0;
				Integer maxY = temp;
				if (range != null && range.length > 1) {
					minY = range[0];
					maxY = range[1];
				} else if (autoSizeY) {
					if (autoSizeYcolumns == null) {
						minY = tables.getMinY(i)-1;
						if (range != null && range.length > 0) {
							maxY += minY;
						}
					} else {
//						minY = tables.getMinYFromColumns(i, autoSizeYcolumns)-1;
					}
				}

				//create plot
				Plot test = new Plot(tables, tables.getMaxX(), minY, maxY, useLabels, connectPoints, 
						autoSizeY, autoSizeYcolumns, plotHeight, title, i);

				String id = "";
				switch(i) {
				case 0:
					id = "unsignificant";
					break;
				case 1:
					id = "low_significance";
					break;
				case 2:
					id = "medium_significance";
					break;
				case 3:
					id = "high_significance";
					break;
				case 4:
					id = "crucial_significance";
					break;
				}
				
//				if (saveData) {
//					test.saveData(0, Paths.get(outputPrefix.toString() + "." + id));
//				}

				if (showPanel && !GraphicsEnvironment.isHeadless())
					test.showInFrame();

				test.savePlot(Paths.get(outputPrefix.toString() + "_" + id), pdf, png, eps, svg, 
						tables.getPlotWidth(), plotHeight != null ? tables.getPlotHeightFromAbsoluteValue(plotHeight) :
							(autoSizeY && temp == null ? tables.getPlotHeightFromTables(i) : 
								tables.getPlotHeightFromRange(temp)));
			}
			
			for (int i = 14; i < 18; ++i) {
				int minY = 0;
				Integer maxY = temp;
				if (range != null && range.length > 1) {
					minY = range[0];
					maxY = range[1];
				} else if (autoSizeY) {
					if (autoSizeYcolumns == null) {
						minY = tables.getMinY(i)-1;
						if (range != null && range.length > 0) {
							maxY += minY;
						}
					} else {
//						minY = tables.getMinYFromColumns(i, autoSizeYcolumns)-1;
					}
				}

				//create plot
				Plot test = new Plot(tables, tables.getMaxX(), minY, maxY, useLabels, connectPoints, 
						autoSizeY, autoSizeYcolumns, plotHeight, title, i);

				String id = "";
				switch(i) {
				case 14:
					id = "changes";
					break;
				case 15:
					id = "deletes";
					break;
				case 16:
					id = "inserts";
					break;
				case 17:
					id = "unknown";
					break;
				}
				
//				if (saveData) {
//					test.saveData(0, Paths.get(outputPrefix.toString() + "." + id));
//				}

				if (showPanel && !GraphicsEnvironment.isHeadless())
					test.showInFrame();

				test.savePlot(Paths.get(outputPrefix.toString() + "_" + id), pdf, png, eps, svg, 
						tables.getPlotWidth(), plotHeight != null ? tables.getPlotHeightFromAbsoluteValue(plotHeight) :
							(autoSizeY && temp == null ? tables.getPlotHeightFromTables(i) : 
								tables.getPlotHeightFromRange(temp)));
			}
		} 

		if (plotAll) {
			int minY = 0;
			Integer maxY = temp;
			if (range != null && range.length > 1) {
				minY = range[0];
				maxY = range[1];
			} else if (autoSizeY) {
				if (autoSizeYcolumns == null) {
					minY = tables.getMinY(5)-1;
					if (range != null && range.length > 0) {
						maxY += minY;
					}
				} else {
//					minY = tables.getMinYFromColumns(4, autoSizeYcolumns)-1;
				}
			}

			//create plot
			Plot test = new Plot(tables, tables.getMaxX(), minY, maxY, useLabels, connectPoints, 
					autoSizeY, autoSizeYcolumns, plotHeight, title, 5);

			if (saveData) {
				test.saveData(0, Paths.get(outputPrefix.toString() + ".all"));
			}

			if (showPanel && !GraphicsEnvironment.isHeadless())
				test.showInFrame();

			test.savePlot(Paths.get(outputPrefix.toString() + "_all"), pdf, png, eps, svg, 
					tables.getPlotWidth(), plotHeight != null ? tables.getPlotHeightFromAbsoluteValue(plotHeight) :
						(autoSizeY && temp == null ? tables.getPlotHeightFromTables(5) : 
							tables.getPlotHeightFromRange(temp)));

		}
		
		if (plotMFR) {
			int minY = 0;
			Integer maxY = temp;
			if (range != null && range.length > 1) {
				minY = range[0];
				maxY = range[1];
			} else if (autoSizeY) {
				if (autoSizeYcolumns == null) {
					minY = tables.getMinY(6)-1;
					if (range != null && range.length > 0) {
						maxY += minY;
					}
				} else {
//					minY = tables.getMinYFromColumns(4, autoSizeYcolumns)-1;
				}
			}

			//create plot
			Plot test = new Plot(tables, tables.getMaxX(), minY, maxY, useLabels, connectPoints, 
					autoSizeY, autoSizeYcolumns, plotHeight, title, 6);

			if (saveData) {
				test.saveData(0, Paths.get(outputPrefix.toString() + ".MFR"));
			}

			if (showPanel && !GraphicsEnvironment.isHeadless())
				test.showInFrame();

			test.savePlot(Paths.get(outputPrefix.toString() + "_MFR"), pdf, png, eps, svg, 
					tables.getPlotWidth(), plotHeight != null ? tables.getPlotHeightFromAbsoluteValue(plotHeight) :
						(autoSizeY && temp == null ? tables.getPlotHeightFromTables(6) : 
							tables.getPlotHeightFromRange(temp)));

		}
		
		if (plotHitAtX) {
			int[] xArray = { 1, 5, 10, 20, 30, 50, 100 };
			for (int i = 0; i < xArray.length; ++i) {
				int minY = 0;
				Integer maxY = temp;
				if (range != null && range.length > 1) {
					minY = range[0];
					maxY = range[1];
				} else if (autoSizeY) {
					if (autoSizeYcolumns == null) {
						minY = tables.getMinY(i + 7)-1;
						if (range != null && range.length > 0) {
							maxY += minY;
						}
					} else {
//						minY = tables.getMinYFromColumns(4, autoSizeYcolumns)-1;
					}
				}

				//create plot
				Plot test = new Plot(tables, tables.getMaxX(), minY, maxY, useLabels, connectPoints, 
						autoSizeY, autoSizeYcolumns, plotHeight, title, i + 7);

				if (saveData) {
					test.saveData(0, Paths.get(outputPrefix.toString() + ".hitAt" + xArray[i]));
				}

				if (showPanel && !GraphicsEnvironment.isHeadless())
					test.showInFrame();

				test.savePlot(Paths.get(outputPrefix.toString() + "_hitAt" + xArray[i]), pdf, png, eps, svg, 
						tables.getPlotWidth(), plotHeight != null ? tables.getPlotHeightFromAbsoluteValue(plotHeight) :
							(autoSizeY && temp == null ? tables.getPlotHeightFromTables(i + 7) : 
								tables.getPlotHeightFromRange(temp)));
			}
			
			int minY = 0;
			Integer maxY = temp;
			if (range != null && range.length > 1) {
				minY = range[0];
				maxY = range[1];
			} else if (autoSizeY) {
				if (autoSizeYcolumns == null) {
					minY = tables.getMinY(18)-1;
					if (range != null && range.length > 0) {
						maxY += minY;
					}
				} else {
//					minY = tables.getMinYFromColumns(4, autoSizeYcolumns)-1;
				}
			}

			//create plot
			Plot test = new Plot(tables, tables.getMaxX(), minY, maxY, useLabels, connectPoints, 
					autoSizeY, autoSizeYcolumns, plotHeight, title, 18);

			if (saveData) {
				test.saveData(0, Paths.get(outputPrefix.toString() + ".hitAtInf"));
			}

			if (showPanel && !GraphicsEnvironment.isHeadless())
				test.showInFrame();

			test.savePlot(Paths.get(outputPrefix.toString() + "_hitAtInf"), pdf, png, eps, svg, 
					tables.getPlotWidth(), plotHeight != null ? tables.getPlotHeightFromAbsoluteValue(plotHeight) :
						(autoSizeY && temp == null ? tables.getPlotHeightFromTables(18) : 
							tables.getPlotHeightFromRange(temp)));

		}
		
		int minY = 0;
		Integer maxY = temp;
		if (range != null && range.length > 1) {
			minY = range[0];
			maxY = range[1];
		}
		if (autoSizeY) {
			if (autoSizeYcolumns == null) {
				minY = tables.getMinY()-1;
				if (range != null && range.length > 0) {
					maxY += minY;
				}
			} else {
				//				minY = tables.getMinYFromTablesFromColumns(autoSizeYcolumns)-1;
			}
		}

		//create plot
		Plot test = new Plot(tables, tables.getMaxX(), minY, maxY, useLabels, connectPoints, 
				autoSizeY, autoSizeYcolumns, plotHeight, title, 14, 15, 16, 17);

		if (saveData) {
			test.saveData(0, Paths.get(outputPrefix.toString() + ".changes"));
			test.saveData(1, Paths.get(outputPrefix.toString() + ".deletes"));
			test.saveData(2, Paths.get(outputPrefix.toString() + ".inserts"));
			test.saveData(3, Paths.get(outputPrefix.toString() + ".unknown"));
		}

		if (showPanel && !GraphicsEnvironment.isHeadless())
			test.showInFrame();

		test.savePlot(Paths.get(outputPrefix.toString() + "_cdi"), pdf, png, eps, svg, 
				tables.getPlotWidth(), plotHeight != null ? tables.getPlotHeightFromAbsoluteValue(plotHeight) :
					(autoSizeY && temp == null ? tables.getPlotHeightFromTables(14,15,16,17) : 
						tables.getPlotHeightFromRange(temp)));

		
		//create plot
		test = new Plot(tables, tables.getMaxX(), minY, maxY, useLabels, connectPoints, 
				autoSizeY, autoSizeYcolumns, plotHeight, title, 0, 1, 2, 3, 4);

		if (saveData) {
			test.saveData(0, Paths.get(outputPrefix.toString() + ".unsignificant"));
			test.saveData(1, Paths.get(outputPrefix.toString() + ".low_significance"));
			test.saveData(2, Paths.get(outputPrefix.toString() + ".medium_significance"));
			test.saveData(3, Paths.get(outputPrefix.toString() + ".high_significance"));
			test.saveData(4, Paths.get(outputPrefix.toString() + ".crucial_significance"));
		}

		if (showPanel && !GraphicsEnvironment.isHeadless())
			test.showInFrame();

		test.savePlot(Paths.get(outputPrefix.toString()), pdf, png, eps, svg, 
				tables.getPlotWidth(), plotHeight != null ? tables.getPlotHeightFromAbsoluteValue(plotHeight) :
					(autoSizeY && temp == null ? tables.getPlotHeightFromTables(0,1,2,3,4) : 
						tables.getPlotHeightFromRange(temp)));

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

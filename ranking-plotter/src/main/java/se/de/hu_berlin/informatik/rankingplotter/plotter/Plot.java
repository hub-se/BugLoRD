/*
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.io.data.DataWriter;
import de.erichseifert.gral.io.data.DataWriterFactory;
import de.erichseifert.gral.io.plots.DrawableWriter;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LinearRenderer2D;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.LabelPointRenderer;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.DrawablePanel;
import de.erichseifert.gral.util.Insets2D;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DataTableCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.NormalDataTable;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;


/**
 * {@link Panel} extension to plot SBFL/NLFL ranking data points.
 * 
 * @author Simon Heiden
 */
public class Plot extends Panel {
	
	/**
	 * Version id for serialization. 
	 */
	private static final long serialVersionUID = -5263057758564264676L;

	public static final int DEFAULT_RANGE = 50;
	
	/**
	 * The underlying {@link XYPlot} object.
	 */
	private final XYPlot plot;
	
	private List<DataSeries> dataSourceList;

	/**
	 * Creates a new {@link Plot} object with the given parameters.
	 * @param tables
	 * contains all relevant data tables and a label map
	 * @param max_x
	 * maximum x value on the x-axis
	 * @param min_y
	 * minimum value on the y-axis
	 * @param max_y
	 * maximum y value on the y-axis
	 * @param paint_labels
	 * sets if the data points should be plotted as colored letters
	 * @param connectPoints
	 * sets whether data points should be connected with lines
	 * @param autoSizeY
	 * whether the plot should be automatically sized, regarding the y-axis
	 * @param autoSizeYcolumns
	 * an array of columns that should be considered for auto sizing
	 * @param plotHeight
	 * a fixed height for the plot, or null
	 * @param title
	 * sets the title of panel
	 * @param plotTables
	 * sets tables that shall be plotted. Other tables are ignored.
	 */
	public Plot(DataTableCollection tables, int max_x, int min_y, Integer max_y,
			final boolean paint_labels, boolean connectPoints,
			boolean autoSizeY, Integer[] autoSizeYcolumns, Integer plotHeight, final String title, int... plotTables) {
		super(tables.getPlotWidth(), 
				plotHeight != null ? tables.getPlotHeightFromAbsoluteValue(plotHeight) :
					(autoSizeY && max_y == null ? tables.getPlotHeightFromTables(plotTables) : 
							tables.getPlotHeightFromRange(max_y)));
		// Create data sources
		java.util.Arrays.sort(plotTables);
		dataSourceList = new ArrayList<>();
		List<NormalDataTable> tableList = new ArrayList<>();
		for (int i : plotTables) {
			dataSourceList.add(new DataSeries(tables.getTables()[i].getTable(), 0, 1, 2, 3, 4));
			tableList.add(tables.getTables()[i]);
		}		
		//add outlier data table if available
		if ((!autoSizeY || max_y != null) && tables.getOutlierTable() != null) {
			dataSourceList.add(new DataSeries(tables.getOutlierTable().getTable(), 0, 1, 2));
		}
		
		DataSource[] dataSourcesArray = dataSourceList.toArray(new DataSource[0]);
	    
		// Create new xy-plot
		plot = new XYPlot(dataSourcesArray);
		
		// Create x axis and y axis by default
		final Axis axisX = new Axis(0, max_x + 1);
		int real_max_y = autoSizeY && max_y == null ? tables.getMaxYFromTables(plotTables) + 1 : max_y + 1;
		final Axis axisY = new Axis(min_y, real_max_y);
		plot.setAxis(XYPlot.AXIS_X, axisX);
		plot.setAxis(XYPlot.AXIS_Y, axisY);
		
		// Create renderers for x and y axes by default
		final AxisRenderer axisXRenderer = new LinearRenderer2D();
		final AxisRenderer axisYRenderer = new LinearRenderer2D();
		plot.setAxisRenderer(XYPlot.AXIS_X, axisXRenderer);
		plot.setAxisRenderer(XYPlot.AXIS_Y, axisYRenderer);

		// Format axes
		axisXRenderer.setTickSpacing(1);
		axisXRenderer.setMinorTicksVisible(false);
		
		if (real_max_y - min_y > 8000) {
			axisYRenderer.setTickSpacing(500);
			axisYRenderer.setMinorTicksCount(4);
		} else if (real_max_y - min_y > 3000) {
			axisYRenderer.setTickSpacing(200);
			axisYRenderer.setMinorTicksCount(3);
		} else if (real_max_y - min_y > 2000) {
			axisYRenderer.setTickSpacing(100);
			axisYRenderer.setMinorTicksCount(1);
		} else if (real_max_y - min_y > 1000) {
			axisYRenderer.setTickSpacing(50);
			axisYRenderer.setMinorTicksCount(1);
		} else {
			axisYRenderer.setTickSpacing(20);
			axisYRenderer.setMinorTicksCount(1);
		}

		axisYRenderer.setMinorTicksVisible(true);
		
		//axisXRenderer.setLabel("x axis");
		//axisYRenderer.setLabel("y axis");

		// Custom tick labels
		if (tables.getLabelMap().size() > 0) {
			axisXRenderer.setCustomTicks(tables.getLabelMap());
			axisXRenderer.setTickLabelRotation(90);
			axisXRenderer.setTickLabelDistance(0.2);
		}

		// Custom stroke for the x-axis
		//BasicStroke stroke = new BasicStroke(2f);
		//axisRendererX.setShapeStroke(stroke);

		// Format plot
		plot.setInsets(new Insets2D.Double(20,60,95,20));
		plot.setBackground(Color.WHITE);
		if (title != null && !title.equals("")) {
			plot.getTitle().setFont(new Font("Arial", Font.PLAIN, 12));
			plot.getTitle().setText(title);
		}
		
		// Format plot area
//		plot.getPlotArea().setBackground(new RadialGradientPaint(
//			new Point2D.Double(0.5, 0.5),
//			0.75f,
//			new float[] { 0.6f, 0.8f, 1.0f },
//			new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 0) }
//		));
		plot.getPlotArea().setBorderStroke(null);
		

		// Format rendering of outlier data
		if ((!autoSizeY/* || max_y != null*/) && tables.getOutlierTable() != null) {
			final LabelPointRenderer outlierLabelPointRenderer = new LabelPointRenderer();
			outlierLabelPointRenderer.setColor(tables.getOutlierTable().getPlotColor());
			outlierLabelPointRenderer.setColumn(2);
			outlierLabelPointRenderer.setAlignmentY(-0.2);
			outlierLabelPointRenderer.setAlignmentX(0.7);
			plot.setPointRenderer(dataSourceList.get(dataSourceList.size()-1), outlierLabelPointRenderer);
		}
		
		// Format rendering of "normal" data points
		if (paint_labels) {
			for (int i = 0; i < tableList.size(); ++i) {
				LabelPointRenderer renderer = new LabelPointRenderer();
				renderer.setColor(tableList.get(i).getPlotColor());
				renderer.setColumn(4);
				renderer.setAlignmentY(-1.4);
				renderer.setErrorVisible(true);
				renderer.setErrorColumnTop(2);
				renderer.setErrorColumnBottom(3);
				renderer.setErrorColor(Color.RED);
				plot.setPointRenderer(dataSourceList.get(i), renderer);
			}
		} else {
			for (int i = 0; i < tableList.size(); ++i) {
				PointRenderer renderer = new DefaultPointRenderer2D();
				renderer.setColor(tableList.get(i).getPlotColor());
//				renderer.setShape(new Rectangle(-2,-2,5,5));
				renderer.setErrorVisible(true);
				renderer.setErrorColumnTop(2);
				renderer.setErrorColumnBottom(3);				
				renderer.setErrorColor(Color.RED);
				plot.setPointRenderer(dataSourceList.get(i), renderer);
			}
		}
		
		if (connectPoints) {
			for (int i = 0; i < tableList.size(); ++i) {
				LineRenderer lines = new DefaultLineRenderer2D();
				lines.setColor(tableList.get(i).getPlotColor());
				plot.setLineRenderer(dataSourceList.get(i), lines);
			}
		}
		
		
		// Add plot to Swing component
		//InteractivePanel panel = new InteractivePanel(plot);
		final DrawablePanel panel = new DrawablePanel(plot);
		//panel.setPannable(false);
		//panel.setZoomable(false);
		this.add(panel, BorderLayout.CENTER);
	}
	
	
	public List<DataSeries> getDataSourceList() {
		return dataSourceList;
	}


	/**
	 * Saves the plot to either PDF or PNG format (or both).
	 * @param path
	 * contains the path to the output file (as a prefix without extension) 
	 * @param pdf
	 * sets if the output shall be saved as a PDF file. (Larger file size, but good quality.)
	 * @param png
	 * sets if the output shall be saved as a PNG file. (Smaller file size, but bad quality.)
	 * @param eps
	 * sets if the output shall be saved as an EPS file. (Larger file size, but good quality.)
	 * @param svg
	 * sets if the output shall be saved as an SVG file. (Medium-large file size, good quality.)
	 * @param width
	 * sets the width of the output
	 * @param height
	 * sets the height of the output
	 */
	public void savePlot(Path path, boolean pdf, boolean png, boolean eps, boolean svg, int width, int height) {
		path.getParent().toFile().mkdirs();
		if (pdf) {
			try (FileOutputStream stream = new FileOutputStream(Paths.get(path.toString() + ".pdf").toFile())) {
				final DrawableWriter writer = DrawableWriterFactory.getInstance().get("application/pdf");
				writer.write(plot, stream, width, height);
			} catch (IOException e) {
				Log.abort(this, e, "Could not write file '%s'.", path.toString() + ".pdf");;
			}
		}
		if (eps) {
			try (FileOutputStream stream = new FileOutputStream(Paths.get(path.toString() + ".eps").toFile())) {
				final DrawableWriter writer = DrawableWriterFactory.getInstance().get("application/postscript");
				writer.write(plot, stream, width, height);
			} catch (IOException e) {
				Log.abort(this, e, "Could not write file '%s'.", path.toString() + ".eps");;
			}
		}
		if (svg) {
			try (FileOutputStream stream = new FileOutputStream(Paths.get(path.toString() + ".svg").toFile())) {
				final DrawableWriter writer = DrawableWriterFactory.getInstance().get("image/svg+xml");
				writer.write(plot, stream, width, height);
			} catch (IOException e) {
				Log.abort(this, e, "Could not write file '%s'.", path.toString() + ".svg");;
			}
		}
		if (png) {
			try (FileOutputStream stream = new FileOutputStream(Paths.get(path.toString() + ".png").toFile())) {
				final DrawableWriter writer = DrawableWriterFactory.getInstance().get("image/png");
				writer.write(plot, stream, width, height);
			} catch (IOException e) {
				Log.abort(this, e, "Could not write file '%s'.", path.toString() + ".png");;
			}
		} 
	}
	
	/**
	 * Writes the given data source to a file with tab separated values.
	 * @param index
	 * index of the data source
	 * @param path
	 * the output path (will be extended by ".csv")
	 */
	public void saveData(int index, Path path) {
		if (index >= dataSourceList.size()) {
			Log.err(this, "No data source for index %d.", index);
			return;
		}
		path.getParent().toFile().mkdirs();
        try(FileOutputStream dataStream = new FileOutputStream(path.toString() + ".csv")) {
        DataWriterFactory factory = DataWriterFactory.getInstance();
        DataWriter writer = factory.get("text/tab-separated-values");
        writer.write(dataSourceList.get(index), dataStream);
        } catch (IOException e) {
			Log.abort(this, e, "Could not write file '%s'.", path.toString() + ".csv");;
		}
    }

	/* (non-Javadoc)
	 * @see rankingPlotter.Panel#getTitle()
	 */
	@Override
	public String getTitle() {
		return "x-y plot";
	}

	/* (non-Javadoc)
	 * @see rankingPlotter.Panel#getDescription()
	 */
	@Override
	public String getDescription() {
		return "test";
	}
}

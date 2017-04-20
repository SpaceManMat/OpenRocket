package net.sf.openrocket.cdoverride;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.gui.dialogs.motor.thrustcurve.ThrustCurveMotorSelectionPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class CDCurvePlotDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();
	
	public CDCurvePlotDialog(List<CDrec> CDrec, Window parent) {
		super(parent, "CD Override Curve", ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// Thrust curve plot
		JFreeChart chart = ChartFactory.createXYLineChart(
				"CD Override Curve", // title
				"Mach", // xAxisLabel
				"CD", // yAxisLabel
				null, // dataset
				PlotOrientation.VERTICAL,
				true, // legend
				true, // tooltips
				false // urls
				);
		

		// Add the data and formatting to the plot
		XYPlot plot = chart.getXYPlot();
		
		chart.setBackgroundPaint(panel.getBackground());
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		
		ChartPanel chartPanel = new ChartPanel(chart,
				false, // properties
				true, // save
				false, // print
				true, // zoom
				true); // tooltips
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setEnforceFileExtensions(true);
		chartPanel.setInitialDelay(500);
		
		StandardXYItemRenderer renderer = new StandardXYItemRenderer();
		renderer.setBaseShapesVisible(false);
		renderer.setBaseShapesFilled(false);
		plot.setRenderer(renderer);
		

		// Create the plot data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		dataset.addSeries(generateSeries(CDrec, true, "CD Accelerating"));
		renderer.setSeriesStroke(1, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		renderer.setSeriesPaint(1, ThrustCurveMotorSelectionPanel.getColor(1));
		renderer.setSeriesShape(1, new Rectangle());
		
		dataset.addSeries(generateSeries(CDrec, false, "CD Decelerating"));
		renderer.setSeriesStroke(2, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		renderer.setSeriesPaint(2, ThrustCurveMotorSelectionPanel.getColor(2));
		renderer.setSeriesShape(2, new Rectangle());
		
		plot.setDataset(dataset);
		
		panel.add(chartPanel, "width 600:600:, height 400:400:, grow, wrap");
		

		// Close button
		JButton close = new JButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CDCurvePlotDialog.this.setVisible(false);
			}
		});
		panel.add(close, "bottom, right, tag close");
		

		this.add(panel);
		
		this.pack();
		GUIUtil.setDisposableDialogOptions(this, null);
	}
	
	
	private XYSeries generateSeries(List<CDrec> CDrec, boolean accelerating, String name) {
		XYSeries series = new XYSeries(name);
		
		for (int j = 0; j < CDrec.size(); j++) {
			if (CDrec.get(j).ACCELERATING == accelerating) {
				series.add(CDrec.get(j).MACH, CDrec.get(j).CD);
			}
		}
		return series;
	}
}

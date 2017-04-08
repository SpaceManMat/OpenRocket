package net.sf.openrocket.cdoverride;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSwingSimulationExtensionConfigurator;
import net.sf.openrocket.unit.UnitGroup;

/**
 * The Swing configuration dialog for the extension.
 * 
 * The abstract implementation provides a ready JPanel using MigLayout
 * to which you can build the dialog. 
 */
@Plugin
public class CDoverrideConfigurator extends AbstractSwingSimulationExtensionConfigurator<CDoverride> {
	
	public CDoverrideConfigurator() {
		super(CDoverride.class);
	}
	
	@Override
	protected JComponent getConfigurationComponent(CDoverride extension, Simulation simulation, JPanel panel) {
		panel.add(new JLabel("CD Override:"));
		
		DoubleModel m = new DoubleModel(extension, "Multiplier", UnitGroup.UNITS_RELATIVE, 0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "w 65lp!");
		
		UnitSelector unit = new UnitSelector(m);
		panel.add(unit, "w 25");
		
		BasicSlider slider = new BasicSlider(m.getSliderModel(0, 3));
		panel.add(slider, "w 75lp, wrap");
		
		return panel;
	}
	
}

package net.sf.openrocket.cdoverride;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSwingSimulationExtensionConfigurator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.cdoverride.CDLoaderHelper;

/**
 * The Swing configuration dialog for the extension.
 * 
 * The abstract implementation provides a ready JPanel using MigLayout
 * to which you can build the dialog. 
 */
@Plugin
public class CDoverrideConfigurator extends AbstractSwingSimulationExtensionConfigurator<CDoverride> {
	
	private static final Logger log = LoggerFactory.getLogger(BasicFrame.class);
	
	public CDoverrideConfigurator() {
		super(CDoverride.class);
	}
	
	private JLabel labelCDtotal, labelCDfriction, labelCDpressure, labelCDbase;
	private JSpinner spinCDtotal, spinCDfriction, spinCDpressure, spinCDbase;
	private UnitSelector unitCDtotal, unitCDfriction, unitCDpressure, unitCDbase;
	private BasicSlider sliderCDtotal, sliderCDfriction, sliderCDpressure, sliderCDbase; 
	private StringModel smFileName;
	private JButton fileBtn, loadBtn;
	
	@Override
	protected JComponent getConfigurationComponent(final CDoverride extension, Simulation simulation, final JPanel panel) {
		
		JLabel overrideMethod = new JLabel("CD Override Method:");
		panel.add(overrideMethod);
		
		final StringModel selected = new StringModel(extension, "SelectedOption");
		String[] optionList = { "Multiplier", "Separate Multipliers", "Data File" };
		
		final JComboBox OverrideOption = new JComboBox(optionList);
		OverrideOption.setEditable(false);
		OverrideOption.setSelectedItem(selected);
		OverrideOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = (String) OverrideOption.getSelectedItem();
				selected.setValue(s);
				setEnabledSub(extension, s);
			}
		});
		panel.add(OverrideOption, "span, wrap rel");
		
		
		//// CD Override by Multiplier 
		JPanel sub1 = new JPanel(new MigLayout("fill, gap rel unrel", "[grow][65lp!][30lp!][75lp!]", ""));
		sub1.setBorder(BorderFactory.createTitledBorder("CD Override by Multiplier"));
		//useFileCheck.addEnableComponent(sub2, false);
		panel.add(sub1, "growx, split 2, aligny 0, flowy, gapright para");
			
		//BooleanModel useTotalOverrideCheck = new BooleanModel(extension, "UseTotalOverride");
		//J/CheckBox check2 = new JCheckBox(useTotalOverrideCheck);
		//check2.setText("Use Total CD Override");
		//useFileCheck.addEnableComponent(check2, false);
		//sub1.add(check2, "spanx, wrap unrel");
			
		
		labelCDtotal = new JLabel("CD Total Override:");
		//useTotalOverrideCheck.addEnableComponent(labelCDtotal, true);
		//useFileCheck.addEnableComponent(labelCDtotal, false);
		sub1.add(labelCDtotal);
			
		DoubleModel m1 = new DoubleModel(extension, "MultiplierTotal", UnitGroup.UNITS_RELATIVE, 0);
			
		spinCDtotal = new JSpinner(m1.getSpinnerModel());
		spinCDtotal.setEditor(new SpinnerEditor(spinCDtotal));
		//useTotalOverrideCheck.addEnableComponent(spinCDtotal, true);
		//useFileCheck.addEnableComponent(spinCDtotal, false);
		sub1.add(spinCDtotal, "w 65lp!");
			
		unitCDtotal = new UnitSelector(m1);
		//useTotalOverrideCheck.addEnableComponent(unitCDtotal, true);
		//useFileCheck.addEnableComponent(unitCDtotal, false);
		sub1.add(unitCDtotal, "w 25");
		
		sliderCDtotal = new BasicSlider(m1.getSliderModel(0, 3));
		//useTotalOverrideCheck.addEnableComponent(sliderCDtotal, true);
		//useFileCheck.addEnableComponent(sliderCDtotal, false);
		sub1.add(sliderCDtotal, "w 75lp, wrap");
			
			
		//// CD Override by Separate Multipliers
		JPanel sub2 = new JPanel(new MigLayout("fill, gap rel unrel", "[grow][65lp!][30lp!][75lp!]", ""));
		sub2.setBorder(BorderFactory.createTitledBorder("CD Override by Separate Multipliers"));
		//useFileCheck.addEnableComponent(sub2, false);
		panel.add(sub2, "growx, split 2, aligny 0, flowy, gapright para");
			
		labelCDfriction = new JLabel("CD Friction Override:");
		//useTotalOverrideCheck.addEnableComponent(labelCDfriction, false);
		//useFileCheck.addEnableComponent(labelCDfriction, false);
		sub2.add(labelCDfriction);
			
		DoubleModel m2 = new DoubleModel(extension, "MultiplierFriction", UnitGroup.UNITS_RELATIVE, 0);
			
		spinCDfriction = new JSpinner(m2.getSpinnerModel());
		spinCDfriction.setEditor(new SpinnerEditor(spinCDfriction));
		//useTotalOverrideCheck.addEnableComponent(spinCDfriction, false);
		//useFileCheck.addEnableComponent(spinCDfriction, false);
		sub2.add(spinCDfriction, "w 65lp!");
		
		unitCDfriction = new UnitSelector(m2);
		//useTotalOverrideCheck.addEnableComponent(unitCDfriction, false);
		//useFileCheck.addEnableComponent(unitCDfriction, false);
		sub2.add(unitCDfriction, "w 25");
			
		sliderCDfriction = new BasicSlider(m2.getSliderModel(0, 3));
		//useTotalOverrideCheck.addEnableComponent(sliderCDfriction, false);
		//useFileCheck.addEnableComponent(sliderCDfriction, false);
		sub2.add(sliderCDfriction, "w 75lp, wrap");

		
		labelCDpressure = new JLabel("CD Pressure Override:");
		//useTotalOverrideCheck.addEnableComponent(labelCDpressure, false);
		//useFileCheck.addEnableComponent(labelCDpressure, false);
		sub2.add(labelCDpressure);
			
		DoubleModel m3 = new DoubleModel(extension, "MultiplierPressure", UnitGroup.UNITS_RELATIVE, 0);
			
		spinCDpressure = new JSpinner(m3.getSpinnerModel());
		spinCDpressure.setEditor(new SpinnerEditor(spinCDpressure));
		//useTotalOverrideCheck.addEnableComponent(spinCDpressure, false);
		//useFileCheck.addEnableComponent(spinCDpressure, false);
		sub2.add(spinCDpressure, "w 65lp!");
			
		unitCDpressure = new UnitSelector(m3);
		//useTotalOverrideCheck.addEnableComponent(unitCDpressure, false);
		//useFileCheck.addEnableComponent(unitCDpressure, false);
		sub2.add(unitCDpressure, "w 25");
			
		sliderCDpressure = new BasicSlider(m3.getSliderModel(0, 3));
		//useTotalOverrideCheck.addEnableComponent(sliderCDpressure, false);
		//useFileCheck.addEnableComponent(sliderCDpressure, false);
		sub2.add(sliderCDpressure, "w 75lp, wrap");

			
		labelCDbase = new JLabel("CD Base Override:");
		//useTotalOverrideCheck.addEnableComponent(labelCDbase, false);
		//useFileCheck.addEnableComponent(labelCDbase, false);
		sub2.add(labelCDbase);
			
		DoubleModel m4 = new DoubleModel(extension, "MultiplierBase", UnitGroup.UNITS_RELATIVE, 0);
			
		spinCDbase = new JSpinner(m4.getSpinnerModel());
		spinCDbase.setEditor(new SpinnerEditor(spinCDbase));
		//useTotalOverrideCheck.addEnableComponent(spinCDbase, false);
		//useFileCheck.addEnableComponent(spinCDbase, false);
		sub2.add(spinCDbase, "w 65lp!");
			
		unitCDbase = new UnitSelector(m4);
		//useTotalOverrideCheck.addEnableComponent(unitCDbase, false);
		//useFileCheck.addEnableComponent(unitCDbase, false);
		sub2.add(unitCDbase, "w 25");
			
		sliderCDbase = new BasicSlider(m4.getSliderModel(0, 3));
		//useTotalOverrideCheck.addEnableComponent(sliderCDbase, false);
		//useFileCheck.addEnableComponent(sliderCDbase, false);
		sub2.add(sliderCDbase, "w 75lp, wrap");	
		
			
		// CD Override by File Settings sub3 Panel
		JPanel sub3 = new JPanel(new MigLayout("fill, gap rel unrel","[grow][65lp!][30lp!][75lp!]", ""));
		sub3.setBorder(BorderFactory.createTitledBorder("CD Override by File"));
		panel.add(sub3, "growx, split 2, aligny 0, flowy, gapright para");
			
		//BooleanModel useFileCheck = new BooleanModel(extension, "UseFile");
		//JCheckBox check1 = new JCheckBox(useFileCheck);
		//check1.setText("Use File to Override CD");
		//sub3.add(check1, "spanx, wrap unrel");
		
		smFileName = new StringModel(extension, "FileName");
		
		final JTextField fileName = new  JTextField(smFileName.getValue(), 60);
		fileName.setEditable(true);
		//useFileCheck.addEnableComponent(fileName, true);
		fileName.setCaretPosition(0);
		GUIUtil.changeFontSize(fileName, -2);
		fileName.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			
			public void insertUpdate(DocumentEvent e) {
				update();
			}
			
			public void update() {
				smFileName.setValue(fileName.getText());
			}
		});
		sub3.add(new JScrollPane(fileName), "grow, wrap");
		
		fileBtn = new JButton("Browse");
		//useFileCheck.addEnableComponent(fileBtn, true);
		fileBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							File current = new File(fileName.getText());
							fileName.setText(current.getPath());
						
							JFileChooser fc = new JFileChooser(current);
							fc.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
							int action = fc.showOpenDialog(SwingUtilities.getWindowAncestor(fileName));
							if (action == JFileChooser.APPROVE_OPTION) {
								((SwingPreferences) Application.getPreferences()).setDefaultDirectory(fc.getCurrentDirectory());
								File file = fc.getSelectedFile();
								fileName.setText(file.getPath());
							}
						}
					});
			}

		});
		sub3.add(fileBtn, "w 75lp, wrap");
	
		
		loadBtn = new JButton("Load File");
		//useFileCheck.addEnableComponent(fileBtn, true);
		loadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							File current = new File(fileName.getText());
							log.info(Markers.USER_MARKER, "Load selected from CDoverride plugin");
							List<CDrec> cd = CDLoaderHelper.load(current, SwingUtilities.getWindowAncestor(panel));
							if (!cd.isEmpty()) {
								CDCurvePlotDialog CDcurve = new CDCurvePlotDialog(cd, SwingUtilities.getWindowAncestor(panel));
								CDcurve.setVisible(true);
								extension.setCDoverride(cd);
							}
						}
					});
			}

		});
		sub3.add(loadBtn);
		
		//Initalise Enabled/Disabled Items
		setEnabledSub(extension, selected.getValue());
		
		return panel;
	}
	
	private void setEnabledSub(final CDoverride extension, String Option) {
		boolean useCDtotal = false, useCDmultiplier3 = false, useCDfile = false; 
		
		if (Option == "Multiplier") {
			useCDtotal = true;
		} else {
			if (Option == "Separate Multipliers") {
				useCDmultiplier3 = true;
			} else {
				useCDfile = true;
			}
		}
		labelCDtotal.setEnabled(useCDtotal);
		spinCDtotal.setEnabled(useCDtotal);
		unitCDtotal.setEnabled(useCDtotal);
		sliderCDtotal.setEnabled(useCDtotal); 
		
		labelCDfriction.setEnabled(useCDmultiplier3);
		spinCDfriction.setEnabled(useCDmultiplier3);
		unitCDfriction.setEnabled(useCDmultiplier3);
		sliderCDfriction.setEnabled(useCDmultiplier3); 
		labelCDpressure.setEnabled(useCDmultiplier3);
		spinCDpressure.setEnabled(useCDmultiplier3);
		unitCDpressure.setEnabled(useCDmultiplier3);
		sliderCDpressure.setEnabled(useCDmultiplier3); 
		labelCDbase.setEnabled(useCDmultiplier3);
		spinCDbase.setEnabled(useCDmultiplier3);
		unitCDbase.setEnabled(useCDmultiplier3);
		sliderCDbase.setEnabled(useCDmultiplier3); 
		
		smFileName.setEnabled(useCDfile);
		fileBtn.setEnabled(useCDfile);
		loadBtn.setEnabled(useCDfile);
		
		BooleanModel useTotalOverrideCheck = new BooleanModel(extension, "UseTotalOverride");
		useTotalOverrideCheck.setValue(useCDtotal);
		
		BooleanModel useFileCheck = new BooleanModel(extension, "UseFile");
		useFileCheck.setValue(useCDfile);
	}
	
}

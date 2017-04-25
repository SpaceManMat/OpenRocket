package net.sf.openrocket.cdoverride;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import net.sf.openrocket.gui.util.Icons;
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
	private JCheckBox checkSimulationFile;
	private UnitSelector unitCDtotal, unitCDfriction, unitCDpressure, unitCDbase;
	private BasicSlider sliderCDtotal, sliderCDfriction, sliderCDpressure, sliderCDbase; 
	private JTextField fileName;
	private JButton fileBtn, loadBtn;
	
	@Override
	protected JComponent getConfigurationComponent(final CDoverride extension, Simulation simulation, final JPanel panel) {
		
		//// CD Override Method Subsection	
		JPanel sub1 = new JPanel(new MigLayout("fill, gap rel unrel", "[grow][65lp!][30lp!][75lp!]", ""));
		sub1.setBorder(BorderFactory.createTitledBorder("CD Override Method"));
		panel.add(sub1,  "growx, split 2, aligny 0, flowy, gapright para, wrap");
		
		final StringModel selected = new StringModel(extension, "SelectedOption");
		String[] optionList = { "Multiplier", "Separate Multipliers", "Data File" };
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final JComboBox OverrideOption = new JComboBox(optionList);
		OverrideOption.setEditable(false);
		OverrideOption.getModel().setSelectedItem(selected.getValue());
		OverrideOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = (String) OverrideOption.getSelectedItem();
				selected.setValue(s);
				setEnabledSub(extension, s);
			}
		});
		sub1.add(OverrideOption, "w 120lp!");
		
		final JButton helpBtn = new JButton("Help");
		helpBtn.setIcon(Icons.HELP_ABOUT);
		helpBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "About selected");
				new HelpDialog(SwingUtilities.getWindowAncestor(helpBtn)).setVisible(true);
			}
		});
		sub1.add(helpBtn, "w 75lp, skip 2, wrap");
		
		
		//// CD Override by Total Multiplier Subsection	
		JPanel sub2 = new JPanel(new MigLayout("fill, gap rel unrel", "[grow][65lp!][30lp!][75lp!]", ""));
		sub2.setBorder(BorderFactory.createTitledBorder("CD Override by Multiplier"));
		panel.add(sub2,  "growx, split 2, aligny 0, flowy, gapright para, wrap");
		
		labelCDtotal = new JLabel("CD Total Override:");
		sub2.add(labelCDtotal);
			
		DoubleModel m1 = new DoubleModel(extension, "MultiplierTotal", UnitGroup.UNITS_RELATIVE, 0);
			
		spinCDtotal = new JSpinner(m1.getSpinnerModel());
		spinCDtotal.setEditor(new SpinnerEditor(spinCDtotal));
		sub2.add(spinCDtotal, "w 65lp!");
			
		unitCDtotal = new UnitSelector(m1);
		sub2.add(unitCDtotal, "w 25");
		
		sliderCDtotal = new BasicSlider(m1.getSliderModel(0, 3));
		sub2.add(sliderCDtotal, "w 75lp, wrap");
			
			
		//// CD Override by Separate Multipliers Subsection
		JPanel sub3 = new JPanel(new MigLayout("fill, gap rel unrel", "[grow][65lp!][30lp!][75lp!]", ""));
		sub3.setBorder(BorderFactory.createTitledBorder("CD Override by Separate Multipliers"));
		panel.add(sub3, "growx, split 2, aligny 0, flowy, gapright para, wrap");
			
		labelCDfriction = new JLabel("CD Friction Override:");
		sub3.add(labelCDfriction);
			
		DoubleModel m2 = new DoubleModel(extension, "MultiplierFriction", UnitGroup.UNITS_RELATIVE, 0);
			
		spinCDfriction = new JSpinner(m2.getSpinnerModel());
		spinCDfriction.setEditor(new SpinnerEditor(spinCDfriction));
		sub3.add(spinCDfriction, "w 65lp!");
		
		unitCDfriction = new UnitSelector(m2);
		sub3.add(unitCDfriction, "w 25");
			
		sliderCDfriction = new BasicSlider(m2.getSliderModel(0, 3));
		sub3.add(sliderCDfriction, "w 75lp, wrap");

		
		labelCDpressure = new JLabel("CD Pressure Override:");
		sub3.add(labelCDpressure);
			
		DoubleModel m3 = new DoubleModel(extension, "MultiplierPressure", UnitGroup.UNITS_RELATIVE, 0);
			
		spinCDpressure = new JSpinner(m3.getSpinnerModel());
		spinCDpressure.setEditor(new SpinnerEditor(spinCDpressure));
		sub3.add(spinCDpressure, "w 65lp!");
			
		unitCDpressure = new UnitSelector(m3);
		sub3.add(unitCDpressure, "w 25");
			
		sliderCDpressure = new BasicSlider(m3.getSliderModel(0, 3));
		sub3.add(sliderCDpressure, "w 75lp, wrap");

			
		labelCDbase = new JLabel("CD Base Override:");
		sub3.add(labelCDbase);
			
		DoubleModel m4 = new DoubleModel(extension, "MultiplierBase", UnitGroup.UNITS_RELATIVE, 0);
			
		spinCDbase = new JSpinner(m4.getSpinnerModel());
		spinCDbase.setEditor(new SpinnerEditor(spinCDbase));
		sub3.add(spinCDbase, "w 65lp!");
			
		unitCDbase = new UnitSelector(m4);
		sub3.add(unitCDbase, "w 25");
			
		sliderCDbase = new BasicSlider(m4.getSliderModel(0, 3));
		sub3.add(sliderCDbase, "w 75lp, wrap");	
		
			
		// CD Override by File Settings Subsection
		JPanel sub4 = new JPanel(new MigLayout("fill, gap rel unrel","[grow][65lp!][30lp!][75lp!]", ""));
		sub4.setBorder(BorderFactory.createTitledBorder("CD Override by File"));
		panel.add(sub4, "growx, split 2, aligny 0, flowy, gapright para, wrap");
		
		final StringModel smFileName = new StringModel(extension, "FileName");
		
		fileName = new  JTextField(smFileName.getValue(), 60);
		fileName.setEditable(true);
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
		sub4.add(new JScrollPane(fileName), "grow, wrap");
		
		fileBtn = new JButton("Browse");
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
		sub4.add(fileBtn, "w 75lp, wrap");
	
		final BooleanModel isSimulationFile = new BooleanModel(extension, "IsSimulationFile");
		checkSimulationFile = new JCheckBox(isSimulationFile);
		checkSimulationFile.setText("File is in Simulation Extract order");
		sub4.add(checkSimulationFile, "spanx, wrap unrel");
		
		loadBtn = new JButton("Load File");
		loadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							File current = new File(fileName.getText());
							log.info(Markers.USER_MARKER, "Load selected file from CDoverride plugin");
							List<CDrec> cd = CDLoaderHelper.load(current, SwingUtilities.getWindowAncestor(panel), isSimulationFile.getValue());
							if (!cd.isEmpty()) {
								CDCurvePlotDialog CDcurve = new CDCurvePlotDialog(cd, SwingUtilities.getWindowAncestor(panel), isSimulationFile.getValue());
								CDcurve.setVisible(true);
								storeCDcurve(extension, cd, isSimulationFile.getValue());
							}
						}
					});
			}

		});
		sub4.add(loadBtn);
		
		//Initalise Enabled/Disabled Items based on selected option
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
		
		fileName.setEnabled(useCDfile);
		fileBtn.setEnabled(useCDfile);
		checkSimulationFile.setEnabled(useCDfile);
		loadBtn.setEnabled(useCDfile);
		
		BooleanModel useTotalOverrideCheck = new BooleanModel(extension, "UseTotalOverride");
		useTotalOverrideCheck.setValue(useCDtotal);
		
		BooleanModel useFileCheck = new BooleanModel(extension, "UseFile");
		useFileCheck.setValue(useCDfile);
	}
	
	private void storeCDcurve(CDoverride extension, List<CDrec> cd, boolean isSimulationFile) {
		log.info(Markers.USER_MARKER, "Store loaded CDoverride Curves");
		List<CDrec> cdThrust = new ArrayList<CDrec>();
		List<CDrec> cdCoast = new ArrayList<CDrec>();
		
		if (!isSimulationFile) { 
			// Normal file may contain Coast Curve or Thrust Curve or Both (in either order)
			// If only one curve then duplicate from the other 
				
			for (int j = 0; j < cd.size(); j++) {
				if (cd.get(j).THRUSTING) {
					cdThrust.add(new CDrec(cd.get(j).MACH, cd.get(j).CD));	
				} else {
					cdCoast.add(new CDrec(cd.get(j).MACH, cd.get(j).CD));	
				}
			}
			
			if (cdThrust.size() > 0) {
				extension.setCDthrust(cdThrust);
			} else {
				extension.setCDthrust(cdCoast); //No Thrust Curve in file so copy from Coast
			}
			if (cdCoast.size() > 0) {
				extension.setCDcoast(cdCoast);
			} else {
				extension.setCDcoast(cdThrust); //No Coast Curve in file so copy from Thrust
			}			
			
		} else { 
			// For simulation file separate out the 2 curves - Thrust followed by Coast (in decelerating order)
			int maxVelIndex = 0;			
		
			for (int j = 0; j < cd.size(); j++) {
				if (!cd.get(j).THRUSTING) { //End of Thrust Curve
					maxVelIndex = j - 1;
					break;
				}
				cdThrust.add(new CDrec(cd.get(j).MACH, cd.get(j).CD));	
			}
			
			// Start at end of Coast curve and go back to Max Velocity
			for (int j = cd.size()-1; j >= maxVelIndex; j--) {
				cdCoast.add(new CDrec(cd.get(j).MACH, cd.get(j).CD));	
			}
		
			extension.setCDthrust(cdThrust);
			extension.setCDcoast(cdCoast);
		}
	}
}

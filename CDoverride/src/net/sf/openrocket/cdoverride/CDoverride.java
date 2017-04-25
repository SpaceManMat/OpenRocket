package net.sf.openrocket.cdoverride;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension;

/**
 * The actual simulation extension.  A new instance is created for
 * each simulation it is attached to.
 * 
 * This class contains the configuration and is called before the
 * simulation is run.  It can do changes to the simulation, such
 * as add simulation listeners.
 * 
 * All configuration should be stored in the config variable, so that
 * file storage will work automatically.
 */
public class CDoverride extends AbstractSimulationExtension {
	
	@Override
	public String getName() {
		return "CD Override v" + getVersionNumber();
	}
	
	static public String getVersionNumber() {
		return "2.02";
	}
	
	@Override
	public String getDescription() {
		// This description is shown when the user clicks the info-button on the extension	
		return "Overrides the calculated CD based on user input.";
	}
	
	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		conditions.getSimulationListenerList().add(new CDoverrideSimulationListener(isUseFile(),
				isUseTotalOverride(),
				getMultiplierTotal(),
				getMultiplierFriction(),
				getMultiplierPressure(),
				getMultiplierBase(),
				getCDthrust(),
				getCDcoast()));	
	}

	
	public String getSelectedOption() {
		return config.getString("SelectedOption", "Multiplier");
	}
	
	public void setSelectedOption(String selectedOption) {
		config.put("SelectedOption", selectedOption);
		fireChangeEvent();
	}
	
	public boolean isUseFile() {
		return config.getBoolean("UseFile", false);
	}
	
	public void setUseFile(boolean useFile) {
		config.put("UseFile", useFile);
		fireChangeEvent();
	}
	
	public String getFileName() {
		return config.getString("FileName", "");
	}
	
	public void setFileName(String fileName) {
		config.put("FileName", fileName);
		fireChangeEvent();
	}

	public boolean isUseTotalOverride() {
		return config.getBoolean("UseTotalOverride", true);
	}
	
	public void setUseTotalOverride(boolean useTotal) {
		config.put("UseTotalOverride", useTotal);
		fireChangeEvent();
	}
	
	public double getMultiplierTotal() {
		return config.getDouble("MultiplierTotal", 1.0);
	}
	
	public void setMultiplierTotal(double multiplier) {
		config.put("MultiplierTotal", multiplier);
		fireChangeEvent();
	}
	
	public boolean isMultiplierTotalEnabled() {
		//mutex.verify();
		return isUseFile() && isUseTotalOverride();
	}
	
	public double getMultiplierFriction() {
		return config.getDouble("multiplierFriction", 1.0);
	}
	
	public void setMultiplierFriction(double multiplier) {
		config.put("multiplierFriction", multiplier);
		fireChangeEvent();
	}
	
	public boolean isMultiplierFrictionEnabled() {
		//mutex.verify();
		return isUseFile() && !isUseTotalOverride();
	}

	public double getMultiplierPressure() {
		return config.getDouble("multiplierPressure", 1.0);
	}
	
	public void setMultiplierPressure(double multiplier) {
		config.put("multiplierPressure", multiplier);
		fireChangeEvent();
	}
	
	public double getMultiplierBase() {
		return config.getDouble("multiplierBase", 1.0);
	}
	
	public void setMultiplierBase(double multiplier) {
		config.put("multiplierBase", multiplier);
		fireChangeEvent();
	}
	
	public List<CDrec> getCDthrust() {
		List<CDrec> cdOverride = new ArrayList<CDrec>();
		@SuppressWarnings("unchecked")
		List<Double> mach = (List<Double>) config.getList("machThrust", new ArrayList<Double>());
		@SuppressWarnings("unchecked")
		List<Double> cd = (List<Double>) config.getList("cdThrust", new ArrayList<Double>());
		
		for (int j = 0; j < mach.size(); j++) {
			cdOverride.add(new CDrec(mach.get(j), cd.get(j)));
		}
		
		return cdOverride;
	}

	public void setCDthrust(List<CDrec> cdOverride) {
		List<Double> mach = new ArrayList<Double>();
		List<Double> cd = new ArrayList<Double>();
		
		for (int j = 0; j < cdOverride.size(); j++) {
			mach.add(cdOverride.get(j).MACH);
			cd.add(cdOverride.get(j).CD);
		}
				
		config.put("machThrust", mach);
		config.put("cdThrust", cd);
		fireChangeEvent();
	}
	
	public List<CDrec> getCDcoast() {
		List<CDrec> cdOverride = new ArrayList<CDrec>();
		@SuppressWarnings("unchecked")
		List<Double> mach = (List<Double>) config.getList("machCoast", new ArrayList<Double>());
		@SuppressWarnings("unchecked")
		List<Double> cd = (List<Double>) config.getList("cdCoast", new ArrayList<Double>());
		
		for (int j = 0; j < mach.size(); j++) {
			cdOverride.add(new CDrec(mach.get(j), cd.get(j))); //, accelerating.get(j)));
		}
		
		return cdOverride;
	}
	
	public void setCDcoast(List<CDrec> cdOverride) {
		List<Double> mach = new ArrayList<Double>();
		List<Double> cd = new ArrayList<Double>();
		
		for (int j = 0; j < cdOverride.size(); j++) {
			mach.add(cdOverride.get(j).MACH);
			cd.add(cdOverride.get(j).CD);
		}
				
		config.put("machCoast", mach);
		config.put("cdCoast", cd);
		fireChangeEvent();
	}
	
	public boolean getIsSimulationFile() {
		return config.getBoolean("IsSimulationFile", false);
	}
	
	public void setIsSimulationFile(boolean isSimulationFile) {
		config.put("IsSimulationFile", isSimulationFile);
		fireChangeEvent();
	}
	
}

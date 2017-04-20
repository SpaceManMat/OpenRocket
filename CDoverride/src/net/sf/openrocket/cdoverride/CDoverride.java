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
		return "CD Override v2.0";
	}
	
	@Override
	public String getDescription() {
		// This description is shown when the user clicks the info-button on the extension
		return "This extension overrides the calculated CD based on user input";
	}
	
	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		conditions.getSimulationListenerList().add(new CDoverrideSimulationListener(isUseFile(),
				//getFileName(),
				isUseTotalOverride(),
				getMultiplierTotal(),
				getMultiplierFriction(),
				getMultiplierPressure(),
				getMultiplierBase(),
				getCDoverride(),
				getMaxVelIndex()));	
	}

	
	public String getSelectedOption() {
		return config.getString("SelectedOption", "Multiplie");
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

	public List<CDrec> getCDoverride() {
		List<CDrec> cdOverride = new ArrayList<CDrec>();
		@SuppressWarnings("unchecked")
		List<Double> mach = (List<Double>) config.getList("machOverride", new ArrayList<Double>());
		@SuppressWarnings("unchecked")
		List<Double> cd = (List<Double>) config.getList("cdOverride", new ArrayList<Double>());
		@SuppressWarnings("unchecked")
		List<Boolean> accelerating = (List<Boolean>) config.getList("acceleratingOverride", new ArrayList<Boolean>());
		
		for (int j = 0; j < mach.size(); j++) {
			cdOverride.add(new CDrec(mach.get(j), cd.get(j), accelerating.get(j)));
		}
		
		return cdOverride;
	}
	
	public int getMaxVelIndex() {
		return config.getInt("maxVelIndex", 0);
	}
	
	public void setCDoverride(List<CDrec> cdOverride) {
		List<Double> mach = new ArrayList<Double>();
		List<Double> cd = new ArrayList<Double>();
		List<Boolean> accelerating = new ArrayList<Boolean>();
		Boolean acceleratingNext = true;
		Boolean acceleratingPrev = false;
		int maxVelIndex = 0;
		
		for (int j = 0; j < cdOverride.size(); j++) {
			mach.add(cdOverride.get(j).MACH);
			cd.add(cdOverride.get(j).CD);
			acceleratingNext = cdOverride.get(j).ACCELERATING;
			if (acceleratingPrev && !acceleratingNext) {
				maxVelIndex = j - 1;
			}
			accelerating.add(acceleratingNext);
			acceleratingPrev = acceleratingNext;
		}
				
		config.put("machOverride", mach);
		config.put("cdOverride", cd);
		config.put("acceleratingOverride", accelerating);
		config.put("maxVelIndex", maxVelIndex);
		fireChangeEvent();
	}
	
}

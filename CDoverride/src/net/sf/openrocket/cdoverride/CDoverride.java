package net.sf.openrocket.cdoverride;

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
		return "CD Override \u00D7" + getMultiplier();
	}
	
	@Override
	public String getDescription() {
		// This description is shown when the user clicks the info-button on the extension
		return "This extension overrides the calculated CD using a user-defined multiplierr.";
	}
	
	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		conditions.getSimulationListenerList().add(new CDoverrideSimulationListener(getMultiplier()));
	}
	
	public double getMultiplier() {
		return config.getDouble("multiplier", 1.0);
	}
	
	public void setMultiplier(double multiplier) {
		config.put("multiplier", multiplier);
		fireChangeEvent();
	}
	
}

package net.sf.openrocket.cdoverride;

import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtensionProvider;

/**
 * The simulation extension provider.  This is the OpenRocket
 * plugin, which defines the simulation extension class and where
 * it is displayed in the menu.
 */
@Plugin
public class CDoverrideProvider extends AbstractSimulationExtensionProvider {
	
	public CDoverrideProvider() {
		super(CDoverride.class, "Flight", "CD Override");
	}
	
}

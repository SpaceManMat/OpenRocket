package net.sf.openrocket.cdoverride;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.PolyInterpolator;

/**
 * The simulation listener that is attached to the simulation.
 * It is instantiated when the simulation run is started and the
 * methods are called at each step of the simulation.
 */
public class CDoverrideSimulationListener extends AbstractSimulationListener {
	
	private double multiplier;
	private FlightConditions conditions;
	
	public CDoverrideSimulationListener(double multiplier) {
		super();
		this.multiplier = multiplier;
		// Note conditions is initialized by postFlightConditions which must be called before postAerodynamicCalculation
	}
	

	@Override
	public AerodynamicForces postAerodynamicCalculation(SimulationStatus status, AerodynamicForces forces) throws SimulationException {

		forces.setFrictionCD(forces.getFrictionCD() * multiplier);
		forces.setPressureCD(forces.getPressureCD() * multiplier);
		forces.setBaseCD(forces.getBaseCD() * multiplier);
		forces.setCD(forces.getFrictionCD() + forces.getPressureCD() + forces.getBaseCD());
				
		forces.setCaxial(calculateAxialDrag(conditions, forces.getCD()));
		
		return forces;
	}
	
	
	@Override
	public FlightConditions postFlightConditions(SimulationStatus status, FlightConditions flightConditions) throws SimulationException {
		conditions = flightConditions;
		return flightConditions;
	}
	
	
	
	/**
	 * Below is Copied from net.sf.openrocket.aerodynamics.BarrowmanCalculator;
	 * Due to it being private in OpenRocket
	 */
	
	private static final double[] axialDragPoly1, axialDragPoly2;
	static {
		PolyInterpolator interpolator;
		interpolator = new PolyInterpolator(
				new double[] { 0, 17 * Math.PI / 180 },
				new double[] { 0, 17 * Math.PI / 180 }
				);
		axialDragPoly1 = interpolator.interpolator(1, 1.3, 0, 0);
		
		interpolator = new PolyInterpolator(
				new double[] { 17 * Math.PI / 180, Math.PI / 2 },
				new double[] { 17 * Math.PI / 180, Math.PI / 2 },
				new double[] { Math.PI / 2 }
				);
		axialDragPoly2 = interpolator.interpolator(1.3, 0, 0, 0, 0);
	}
	
	/**
	 * Calculate the axial drag from the total drag coefficient.
	 * 
	 * @param conditions
	 * @param cd
	 * @return
	 */
	private double calculateAxialDrag(FlightConditions conditions, double cd) {
		double aoa = MathUtil.clamp(conditions.getAOA(), 0, Math.PI);
		double mul;
		
		//		double sinaoa = conditions.getSinAOA();
		//		return cd * (1 + Math.min(sinaoa, 0.25));
		

		if (aoa > Math.PI / 2)
			aoa = Math.PI - aoa;
		if (aoa < 17 * Math.PI / 180)
			mul = PolyInterpolator.eval(aoa, axialDragPoly1);
		else
			mul = PolyInterpolator.eval(aoa, axialDragPoly2);
		
		if (conditions.getAOA() < Math.PI / 2)
			return mul * cd;
		else
			return -mul * cd;
	}
}

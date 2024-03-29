package net.sf.openrocket.cdoverride;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
//import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
//import net.sf.openrocket.motor.MotorConfiguration;
//import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.PolyInterpolator;
import net.sf.openrocket.util.BugException;

/**
 * The simulation listener that is attached to the simulation.
 * It is instantiated when the simulation run is started and the
 * methods are called at each step of the simulation.
 */
public class CDoverrideSimulationListener extends AbstractSimulationListener {
	
	private static final Logger log = LoggerFactory.getLogger(CDLoaderHelper.class);
	
	private boolean useTotalOverride, useFile; 
	private double multiplierTotal, multiplierFriction, multiplierPressure, multiplierBase;	
	private FlightConditions conditions;
	private List<CDrec> cdThrust, cdCoast;
	
	public CDoverrideSimulationListener(boolean useFile,
			boolean useTotalOverride,
			double multiplierTotal, 
			double multiplierFriction, 
			double multiplierPressure, 
			double multiplierBase,
			List<CDrec> cdThrust,
			List<CDrec> cdCoast) {
		super();
		this.useFile = useFile;
		this.useTotalOverride = useTotalOverride;
		this.multiplierTotal = multiplierTotal;
		this.multiplierFriction = multiplierFriction;
		this.multiplierPressure = multiplierPressure;
		this.multiplierBase = multiplierBase;	
		this.cdThrust = cdThrust;
		this.cdCoast = cdCoast;
		// Note conditions is initialized by postFlightConditions which must be called before postAerodynamicCalculation
	}
	

	@Override
	public AerodynamicForces postAerodynamicCalculation(SimulationStatus status, AerodynamicForces forces) throws SimulationException {

		double mach = conditions.getMach();
		if (mach == 0.0) {
			return forces;
		}
		
		if (useFile) {

			if (cdThrust.isEmpty() || cdCoast.isEmpty()) {
				log.error("CD Override list is not loaded");
				throw new BugException("CD Override list is not loaded");
			}
			
			// Note code is not stage aware so will not work well in multistage commented code may be helpful
//			// Check whether any motor in the active stages is active anymore
//			for (MotorId motorId : status.getMotorConfiguration().getMotorIDs()) {
//				int stage = ((RocketComponent) status.getMotorConfiguration().
//						getMotorMount(motorId)).getStageNumber();
//				if (!status.getConfiguration().isStageActive(stage))
//					continue;
//				if (!status.getMotorConfiguration().getMotorInstance(motorId).isActive())
//					continue;
//				
//			}
			

			// If all motors not burnt out then still thrusting
			if (!status.getConfiguration().getActiveMotors().isEmpty()) {
				forces = updateCD (cdThrust, mach, forces);
			} else {
				forces = updateCD (cdCoast, mach, forces);				
			}
			
		} else {
			if (useTotalOverride) {
				forces.setFrictionCD(forces.getFrictionCD() * multiplierTotal);
				forces.setPressureCD(forces.getPressureCD() * multiplierTotal);
				forces.setBaseCD(forces.getBaseCD() * multiplierTotal);			
			} else {
				forces.setFrictionCD(forces.getFrictionCD() * multiplierFriction);
				forces.setPressureCD(forces.getPressureCD() * multiplierPressure);
				forces.setBaseCD(forces.getBaseCD() * multiplierBase);
			}
			forces.setCD(forces.getFrictionCD() + forces.getPressureCD() + forces.getBaseCD());
		}
				
		//forces.setCDaxial(BarrowmanCalculator.calculateAxialCD(conditions, forces.getCD()));
		forces.setCDaxial(calculateAxialCD(conditions, forces.getCD()));
		
		return forces;
	}
	
	private AerodynamicForces updateCD (List<CDrec> cdOverride, double mach, AerodynamicForces forces) {
		int j;
		int minIndex = 0;
		int maxIndex = cdOverride.size() - 1;
		
		for (j = minIndex; j <= maxIndex; j++) {
			if (mach <= cdOverride.get(j).MACH) {
				break;
			}
		}
		
		if (j>maxIndex) {
			//Velocity larger than override dataset so derive CD from last 2 entries
			if (maxIndex > 0) { //Need minimum 2 entries to derive the CD
				double newCD = deriveCD(cdOverride.get(maxIndex-1).CD, cdOverride.get(maxIndex).CD, cdOverride.get(maxIndex-1).MACH, cdOverride.get(maxIndex).MACH, mach);
				log.warn("Mach " + mach + " is larger than supplied CD Curve. Extrapolated CD value " + newCD);
				forces = updateForces(forces, newCD);
			}
		} else {
			if (j==minIndex && mach < cdOverride.get(j).MACH) {
				//Velocity smaller than override dataset so derive CD from first 2 entries
				if (maxIndex > 0) { //Need minimum 2 entries to derive the CD
					double newCD = deriveCD(cdOverride.get(0).CD, cdOverride.get(1).CD, cdOverride.get(0).MACH, cdOverride.get(1).MACH, mach);
					log.warn("Mach " + mach + " is smaller than supplied CD Curve. Extrapolated CD value " + newCD);
					forces = updateForces(forces, newCD);
				}
			} else {
				if (mach == cdOverride.get(j).MACH) {
					forces = updateForces(forces, cdOverride.get(j).CD);
				} else {
					if (maxIndex > 0) { //Need minimum 2 entries to derive the CD
						double newCD = deriveCD(cdOverride.get(j-1).CD, cdOverride.get(j).CD, cdOverride.get(j-1).MACH, cdOverride.get(j).MACH, mach);
						forces = updateForces(forces, newCD);
					}
				}
			}					
		}
		
		return forces;
	}
	
	private double deriveCD(double cd1, double cd2, double mach1, double mach2, double newMach) {
		double cdDelta = cd2 - cd1;
		double machDelta = mach2 - mach1;
		double machInc = newMach - mach1;
		double cdMachRatio = cdDelta / machDelta;
		double cdInc = cdMachRatio * machInc;
		double newCD = cdInc + cd1;		
		return newCD;
	}
	
	private AerodynamicForces updateForces(AerodynamicForces forces, double newCD) {
		double updateRatio = newCD / forces.getCD();
		
		forces.setFrictionCD(forces.getFrictionCD() * updateRatio);
		forces.setPressureCD(forces.getPressureCD() * updateRatio);
		forces.setBaseCD(forces.getBaseCD() * updateRatio);			
		forces.setCD(newCD);
		
		return forces;
	}
	
	
	@Override
	public FlightConditions postFlightConditions(SimulationStatus status, FlightConditions flightConditions) throws SimulationException {
		conditions = flightConditions;
		return flightConditions;
	}
	
	
	
//	/**
//	 * Below is Copied from net.sf.openrocket.aerodynamics.BarrowmanCalculator;
//	 * Due to it being private in OpenRocket
//	 */

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
	 * Calculate the axial drag coefficient from the total drag coefficient.
	 * 
	 * @param conditions
	 * @param cd
	 * @return
	 */
	private double calculateAxialCD(FlightConditions conditions, double cd) {
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

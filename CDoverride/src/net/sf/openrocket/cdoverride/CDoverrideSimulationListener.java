package net.sf.openrocket.cdoverride;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.motor.MotorId;
import net.sf.openrocket.motor.MotorInstance;
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
	//private String fileName;
	private FlightConditions conditions;
	private List<CDrec> cdOverride;
	private Integer maxVelIndex;
	
	public CDoverrideSimulationListener(boolean useFile,
			//String fileName,
			boolean useTotalOverride,
			double multiplierTotal, 
			double multiplierFriction, 
			double multiplierPressure, 
			double multiplierBase,
			List<CDrec> cdOverride,
			int maxVelIndex) {
		super();
		this.useFile = useFile;
		//this.fileName = fileName;
		this.useTotalOverride = useTotalOverride;
		this.multiplierTotal = multiplierTotal;
		this.multiplierFriction = multiplierFriction;
		this.multiplierPressure = multiplierPressure;
		this.multiplierBase = multiplierBase;
		this.cdOverride = cdOverride;
		this.maxVelIndex = maxVelIndex;
		// Note conditions is initialized by postFlightConditions which must be called before postAerodynamicCalculation
	}
	

	@Override
	public AerodynamicForces postAerodynamicCalculation(SimulationStatus status, AerodynamicForces forces) throws SimulationException {

		double mach = conditions.getMach();
		if (mach == 0.0) {
			return forces;
		}
		
		if (useFile) {

			if (cdOverride.isEmpty()) {
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
			

			// Check if all motors burnt out
			boolean isMotorActive = false;
			for (MotorId motorId : status.getMotorConfiguration().getMotorIDs()) {
				MotorInstance motor = status.getMotorConfiguration().getMotorInstance(motorId);
				if (motor.isActive()){
					isMotorActive = true;
					break; //exit for loop as no need to continue checking
				}
			}
			
			Integer minIndex;
			Integer maxIndex;
			int j = 0;
			
			if (isMotorActive) {
				minIndex = 0;
				maxIndex = maxVelIndex;
				for (j = minIndex; j <= maxIndex; j++) {
					if (mach <= cdOverride.get(j).MACH) {
						break;
					}
				}
				if (j>maxIndex) {
					//Velocity larger than override dataset so do not change CD
				} else {
					if (j==minIndex && mach < cdOverride.get(j).MACH) {
						//Velocity smaller than override dataset so do not change CD
					} else {
						if (mach == cdOverride.get(j).MACH) {
							forces = updateCD(forces, cdOverride.get(j).CD);
						} else {
							double newCD = deriveCD(cdOverride.get(j-1).CD, cdOverride.get(j).CD, cdOverride.get(j-1).MACH, cdOverride.get(j).MACH, mach);
							forces = updateCD(forces, newCD);
						}
					}					
				}	
			} else {
				minIndex = maxVelIndex;
				maxIndex = cdOverride.size() - 1;
				for (j = minIndex; j <= maxIndex; j++) {
					if (mach >= cdOverride.get(j).MACH) {
						break;
					}
				}
				if (j>maxIndex) {
					//Velocity smaller than override dataset so do not change CD
				} else {
					if (j==minIndex && mach > cdOverride.get(j).MACH) {
						//Velocity larger than override dataset so do not change CD
					} else {
						if (mach == cdOverride.get(j).MACH) {
							forces = updateCD(forces, cdOverride.get(j).CD);
						} else {
							double newCD = deriveCD(cdOverride.get(j-1).CD, cdOverride.get(j).CD, cdOverride.get(j-1).MACH, cdOverride.get(j).MACH, mach);
							forces = updateCD(forces, newCD);
						}
					}					
				}				
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
				
		forces.setCaxial(calculateAxialDrag(conditions, forces.getCD()));
		
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
	
	private AerodynamicForces updateCD(AerodynamicForces forces, double newCD) {
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

package net.sf.openrocket.cdoverride;

public class CDrec {
	
	public double MACH = 0;
	public double CD = 0;
	public boolean THRUSTING = true;
	
	/**
	 * Return a CD record from mach, cd and accelerating data.
	 */
	public CDrec(double mach, double cd, boolean isThrusting) {
		super();
		this.MACH = mach;
		this.CD = cd;
		this.THRUSTING = isThrusting;
	}
	
	public CDrec(double mach, double cd) {
		super();
		this.MACH = mach;
		this.CD = cd;
		this.THRUSTING = true;
	}
}

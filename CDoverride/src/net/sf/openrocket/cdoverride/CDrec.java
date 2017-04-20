package net.sf.openrocket.cdoverride;

public class CDrec {
	
	public double MACH = 0;
	public double CD = 0;
	public boolean ACCELERATING = true;
	
	/**
	 * Return a CD record from mach cd and accelerating parms.
	 */
	public CDrec(double mach, double cd, boolean accelerating) {
		super();
		this.MACH = mach;
		this.CD = cd;
		this.ACCELERATING = accelerating;
	}
}

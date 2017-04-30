package net.sf.openrocket.cdoverride;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import net.sf.openrocket.util.ArrayUtils;
import net.sf.openrocket.file.motor.AbstractMotorLoader;

public class CDFileLoader implements CDLoader {
	
	private static final Logger log = LoggerFactory.getLogger(CDLoaderHelper.class);
	
	public static final String CHARSET_NAME = "ISO-8859-1";
	
	public static final Charset CHARSET = Charset.forName(CHARSET_NAME);
	
	public CDrec CDrec;
	
	
	//@Override
	protected Charset getDefaultCharset() {
		return CHARSET;
	}
	
	@Override
	public List<CDrec> load(InputStream stream, String filename, boolean isSimulationFile) throws IOException {
		return load(new InputStreamReader(stream, getDefaultCharset()), filename, isSimulationFile);
	}
	
	/**
	 * Load a list of CD overrides from file 
	 * 
	 * @param reader  the source of the file.
	 * @return		  a list of the {@link CDrec} objects defined in the file.
	 * @throws IOException  if an I/O error occurs or if the file format is illegal.
	 */
	public List<CDrec> load(Reader reader, String filename, boolean isSimulationFile) throws IOException {
		
		BufferedReader in = new BufferedReader(reader);
				
		List<CDrec> cd = new ArrayList<CDrec>();

		
		double machNext = 0;
		double cdNext = 0;
		boolean acceleratingNext = true;
		double machPrev = 0;
		boolean acceleratingPrev = true;
		boolean isThrusting = true;
		int cntThrustHdr = 0;
		double cntCoastHdr = 0;
		String header1 = "";
		String header2 = "";
		
		try {
			String line;
			String[] buf;
							
			// Read the data from file
			for (line = in.readLine(); (line != null); line = in.readLine()) {
				
				buf = AbstractMotorLoader.split(line);
				if (buf.length == 0) {
					continue;
				} else if (buf.length == 2) {
					
					try {
						machNext = Double.parseDouble(buf[0]);
						cdNext = Double.parseDouble(buf[1]);
						// Only keep 1st entry at a given speed, ignore entries with 0 & negative numbers
						if (machNext != machPrev && machNext > 0 && cdNext > 0) { 
							
							if (machNext >= machPrev) { 
								acceleratingNext = true;
							} else {
								acceleratingNext = false;
							}
							if (isSimulationFile) { // For Simulator file make sure it does not accelerate after decelerating
								if (acceleratingNext && !acceleratingPrev) {
									throw new IOException("Simulation data out of order.\n File Entry Mach: " + machNext + " CD: " + cdNext);
								}
								isThrusting = acceleratingNext;
							} else { // Expect up to one of each curve and always in accelerating order
								if ( cntThrustHdr == 0 && cntCoastHdr == 0) { //If no header yet assume to be Thrust Curve
									isThrusting = true;
									cntThrustHdr++;
								}
								if (!acceleratingNext) {
									log.debug("Not Accellerating Mach " + machNext + " Prev: " + machPrev);
									if (isThrusting) {
										throw new IOException("File data out of order.\n Thust Curve file entry Mach: " + machNext + " CD: " + cdNext);
									} else {
										throw new IOException("File data out of order.\n Coast Curve file entry Mach: " + machNext + " CD: " + cdNext);
									}
								}
							}
							
							cd.add(new CDrec(machNext, cdNext, isThrusting));
							machPrev = machNext;
							acceleratingPrev = acceleratingNext;							
						}
					} catch (NumberFormatException e) {
						log.debug("Not a Number line: " + buf[0] + " " + buf[1]);
						header1 = buf[0].toLowerCase(Locale.ROOT);
						header2 = buf[1].toLowerCase(Locale.ROOT);
						if (!isSimulationFile) { //For normal file check for start of new Curve, reset if found
							if (header1.equals("curve") || header1.equals("power")) {
								if (header2.equals("thrust") || header2.equals("on")) {
									log.debug("Start of CD Curve Thrust data");
									if ( cntThrustHdr > 0) {
										throw new IOException("Multiple Thrust CD Curves in File.");
									}
									isThrusting = true;
									machPrev = 0;
									cntThrustHdr++;
								} else {
									if (header2.equals("coast") || header2.equals("off")) {
										log.debug("Start of  CD Curve Coast data");
										if ( cntCoastHdr > 0) {
											throw new IOException("Multiple Coast CD Curves in File.");
										}							
										isThrusting = false;
										machPrev = 0;
										cntCoastHdr++;
									}
								}	
							}
						}
						// All other cases assume column headers or comments
					}				
				} else {
					throw new IOException("Illegal file format.");
				}
			}
			
			
		} catch (NumberFormatException e) {
			
			throw new IOException("Illegal file format.");
			
		}
		return cd;
	}
		
//	// Copied from net.sf.openrocket.file.motor;
//	/**
//	 * Helper method to tokenize a string using whitespace as the delimiter.
//	 */
//	protected static String[] split(String str) {
//		return split(str, "\\s+");
//	}
//	
//	
//	/**
//	 * Helper method to tokenize a string using the given delimiter.
//	 */
//	protected static String[] split(String str, String delim) {
//		String[] pieces = str.split(delim);
//		if (pieces.length == 0 || !pieces[0].equals(""))
//			return pieces;
//		return ArrayUtils.copyOfRange(pieces, 1, pieces.length);
//	}

	@Override
	public List<net.sf.openrocket.cdoverride.CDrec> load(InputStream stream, String filename) throws IOException {
		return null;
	}
}

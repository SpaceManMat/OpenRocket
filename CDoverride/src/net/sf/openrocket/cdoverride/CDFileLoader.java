package net.sf.openrocket.cdoverride;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.util.ArrayUtils;

public class CDFileLoader implements CDLoader {
	
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
		
		try {
			String line;
			String[] buf;
			
			line = in.readLine();
			while (line != null) { // Until EOF	
								
				// Read the data
				for (line = in.readLine(); (line != null) && (line.length() == 0 || line.charAt(0) != ';'); line = in.readLine()) {
					
					buf = split(line);
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
										throw new IOException("File data out of order.");
									}
								} else { // Expect on one curve so must always accelerate
									if (!acceleratingNext) {
										throw new IOException("File data out of order.");
									}									
								}
								
								cd.add(new CDrec(machNext,cdNext,acceleratingNext));
							}
							machPrev = machNext;
							acceleratingPrev = acceleratingNext;
						} catch (NumberFormatException e) {
							continue; //Not valid set of numbers then assume header / comment line
						}				
					} else {
						throw new IOException("Illegal file format.");
					}
				}
			}
			
		} catch (NumberFormatException e) {
			
			throw new IOException("Illegal file format.");
			
		}
		return cd;
	}
		
	// Copied from net.sf.openrocket.file.motor;
	/**
	 * Helper method to tokenize a string using whitespace as the delimiter.
	 */
	protected static String[] split(String str) {
		return split(str, "\\s+");
	}
	
	
	/**
	 * Helper method to tokenize a string using the given delimiter.
	 */
	protected static String[] split(String str, String delim) {
		String[] pieces = str.split(delim);
		if (pieces.length == 0 || !pieces[0].equals(""))
			return pieces;
		return ArrayUtils.copyOfRange(pieces, 1, pieces.length);
	}

	@Override
	public List<net.sf.openrocket.cdoverride.CDrec> load(InputStream stream,
			String filename) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}

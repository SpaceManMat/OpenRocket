package net.sf.openrocket.cdoverride;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.openrocket.file.Loader;


public interface CDLoader extends Loader<CDrec> {
	
	/**
	 * Load CD File from the specified <code>InputStream</code>.
	 * 
	 * @param stream		the source of the motor definitions.
	 * @param filename		the file name of the file, may be <code>null</code> if not 
	 * 						applicable.
	 * @return				a list of CDs contained in the file.
	 * @throws IOException	if an I/O exception occurs of the file format is invalid.
	 */
	@Override
	public List<CDrec> load(InputStream stream, String filename) throws IOException;

	public List<CDrec> load(InputStream is, String fileName, boolean isSimulationFile) throws IOException;
	
}

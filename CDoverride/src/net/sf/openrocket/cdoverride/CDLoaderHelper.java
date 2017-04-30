package net.sf.openrocket.cdoverride;

import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.cdoverride.CDLoader;

public final class CDLoaderHelper {

	private static final Logger log = LoggerFactory.getLogger(CDLoaderHelper.class);

	private CDLoaderHelper() {
		// Prevent construction
	}

	/**
	 * Load a CD data file
	 * 
	 * @param target	the file to load.
	 * @return			a list of all motors in the file/directory.
	 */
	public static List<CDrec> load(File target, Window parent, boolean isSimulationFile) {

		if (target.isDirectory()) {
			log.warn("Could not load file " + target, "Error: Directory was specified");
			JOptionPane.showMessageDialog(parent
					, "Error: Directory was specified " + target
					, "Error opening file"
					, JOptionPane.ERROR_MESSAGE);
			return Collections.emptyList();

		} else {

			InputStream is = null;
			try {
				is = new FileInputStream(target);
				return load(new BufferedInputStream(is), target.getName(), parent, isSimulationFile);
			} catch (IOException e) {
				log.warn("Could not load file " + target, e);
				JOptionPane.showMessageDialog(parent
						, "Could not load file " + target + ' ' + e
						, "Error opening file"
						, JOptionPane.ERROR_MESSAGE);
				return Collections.emptyList();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						log.error("Could not close file " + target, e);
					}
				}
			}

		}
	}

	public static List<CDrec> load( InputStream is, String fileName, Window parent, boolean isSimulationFile) {
		CDLoader loader = new CDFileLoader();
		try {
			List<CDrec> cd = loader.load(is, fileName, isSimulationFile);
			if (cd.size() == 0) {
				log.warn("No entries found in file " + fileName);
				JOptionPane.showMessageDialog(parent
						, "No entries found in file " + fileName
						, "Error opening file"
						, JOptionPane.ERROR_MESSAGE);
			}
			return cd;
		} catch (IOException e) {
			log.warn("Could not load file " + fileName, e);
			JOptionPane.showMessageDialog(parent
					, "Could not load file " + fileName + ' ' + e
					, "Error opening file"
					, JOptionPane.ERROR_MESSAGE);
		}
		return Collections.<CDrec>emptyList();
	}
}

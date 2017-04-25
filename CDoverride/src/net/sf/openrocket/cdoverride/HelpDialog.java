package net.sf.openrocket.cdoverride;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.URLLabel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.cdoverride.CDoverride;

@SuppressWarnings("serial")
public class HelpDialog extends JDialog {
	
	public static final String OPENROCKET_URL = "http://openrocket.sourceforge.net/";
	private static final Translator trans = Application.getTranslator();
	
	private static final String USAGE = "<html>"
			+ "<b>Description</b><br>"
			+ "This plugin overrides the Coefficient of Drag (CD) values calculated by OpenRocket.</b><br><br>"
			+ "<b>Usage</b><br>"
			+ "There are 3 methods used to override the CD:<br>"
			+ "1. Using a multiplier.<br>"
			+ "2. Using separate multiplies for Friction CD, Pressure CD and Base CD.<br>"
			+ "3. Using a file to provide completely new Mach and CD data for the simulation.<br><br>"
			+ "<b>File Details</b><br>"			
			+ "There are 2 types of file that the CD Override plugin can read. "
			+ "A normal file contains either a single CD curve or separate CD curves for thrust and coast phases. "
			+ "The other file type is a simulation extract which follows flight order. "
			+ "Data for simulation extract files can be exported from most rocket simulators and easily converted for upload.<br><br>"
			+ "The files must be formated as follows:<br>"
			+ "1. Must contain only 2 columns. The first is Mach, the second is CD.<br>" 
			+ "2. Must be plain text with white space or tab delimiting.<br>"
			+ "3. For a normal file, the data must be in accelerating order.<br>"
			+ "4. For a normal file, if separate thrust and coast curves are specified then "
			+ "the line 'Curve Thrust' or 'Curve Coast' must be present before each curve.<br>"
			+ "5. For a simulation extract file, the data must accelerate to max velocity then decelerate. "
			+ "As is produced by exporting simulation data from most rocket simulators.<br>"
			+ "6. Text lines (other than mentioned above) are ignored, but must not violate the 2 column rule.<br>"
			+ "7. Blank lines, zero Mach or duplicate Mach entries are ignored.<br><br>"
			+ "<b>Notes</b><br>"
			+ "1. If the file does not provide a low enough or high enough Mach entry then the plugin will extrapolate these "
			+ "based on the first 2 or last 2 entries of the curve.<br>"
			+ "2. There is no provision for separate CD curves of multiple stage rockets in the file."
			+ "</html>";
	
	
	public HelpDialog(Window parent) {
		super(parent);
		
		final String version = CDoverride.getVersionNumber();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		JPanel sub;
		
		sub = new JPanel(new MigLayout("fill"));
		
		sub.add(new StyledLabel("  CD Override pluggin for OpenRocket  ", 20), "ax 50%, growy, wrap para");
		sub.add(new StyledLabel("Version " + version), "ax 50%, growy, wrap para");
		sub.add(new StyledLabel("Developed by Matthew Niejalke"), "ax 50%, growy, wrap para");
		
		sub.add(new StyledLabel("OpenRocket is Copyright " + Chars.COPY + " of Sampo Niskanen and others"), "ax 50%, growy, wrap para");
		sub.add(new URLLabel(OPENROCKET_URL), "ax 50%, growy, wrap para");
		panel.add(sub, "grow");
		
		
		DescriptionArea info = new DescriptionArea(5);
		info.setText(USAGE);
		panel.add(info, "newline, width 10px, height 250lp, grow, spanx, wrap para");
		
		
		//Close button
		JButton close = new JButton(trans.get("button.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HelpDialog.this.dispose();
			}
		});
		panel.add(close, "spanx, right");
		
		this.add(panel);
		this.setTitle("CD Override v" + version);
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		
		GUIUtil.setDisposableDialogOptions(this, close);
	}
	
}

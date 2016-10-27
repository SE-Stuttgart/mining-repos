package atsr.controller.transformer.impls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import javax.swing.JOptionPane;

import org.eclipse.core.runtime.IProgressMonitor;

import atsr.controller.transformer.Transformer;
import atsr.model.Docu;
import atsr.model.Settings;

/**
 * Implementation of the transformer to transform a docu file into the database
 * 
 * @author Simon Lehmann
 *
 */
public class DocuTransfromer extends Transformer {

	/**
	 * Constructor
	 * 
	 * @param path
	 *            - Path of the docu file
	 * @param settings
	 *            - Settings from settingsView
	 * @param obs
	 *            - Observer to notify progress
	 */
	public DocuTransfromer(String path, Settings settings, Observer obs, IProgressMonitor pmonitor) throws SQLException {
		super(path, settings, obs, pmonitor);
	}

	/**
	 * Executive function
	 */
	//@Override
	public void run() {
		// Read the docu file
		File file = new File(getPath());
		List<Docu> docus = new ArrayList<Docu>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));

			// First line of the file is the header and not values
			String line = in.readLine();
			int countSeperator = 0;

			int index = 0;
			String docuValue = "";
			String docuValues[] = new String[3];

			while ((line = in.readLine()) != null) {
				index = 0;
				countSeperator = 0;
				while (countSeperator < 2) {
					if (line.charAt(index) == ';') {
						switch (countSeperator) {
						case 0:
							docuValues[0] = docuValue;
							break;
						case 1:
							docuValues[1] = docuValue;
						}
						countSeperator++;
						docuValue = "";
					} else {
						docuValue += line.charAt(index);
					}
					index++;
				}
				int length = line.length();
				docuValues[3] = line.substring(index, length);

				docus.add(new Docu(Integer.parseInt(docuValues[0]), docuValues[1], docuValues[3]));

			}

			// 3% of the work is done now
			this.notifyProgress(3);
			this.getWriter().writeDocus(docus);
			// The transformation is finished
			this.notifyProgress(100);
			this.getWriter().close();

			in.close();
		} catch (IOException e) {
			// Error handling IO
			JOptionPane.showMessageDialog(null,
					"Error with the selected File! \nPlease check if the file still exists!", "IO Error",
					JOptionPane.ERROR_MESSAGE);
			this.notifyProgress(0);
		} catch (SQLException e) {
			// Error handling SQL
			JOptionPane.showMessageDialog(null,
					"Error with Database! \nPlease check the access and the connection to the Database!", "SQL Error",
					JOptionPane.ERROR_MESSAGE);
			this.notifyProgress(0);
		}
	}

}

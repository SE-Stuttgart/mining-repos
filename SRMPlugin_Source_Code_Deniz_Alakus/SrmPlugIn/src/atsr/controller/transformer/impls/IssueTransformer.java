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
import atsr.model.Issue;
import atsr.model.Settings;

/**
 * Implementation of the transformer to transform a issue file into the database
 * 
 * @author Simon Lehmann
 *
 */
public class IssueTransformer extends Transformer {

	/**
	 * Constructor
	 * 
	 * @param path
	 *            - Path of the issue file
	 * @param settings
	 *            - Settings from settingsView
	 * @param obs
	 *            - Observer to notify progress
	 */
	public IssueTransformer(String path, Settings settings, Observer obs, IProgressMonitor pmonitor) throws SQLException {
		super(path, settings, obs, pmonitor);
	}

	/**
	 * Executive function
	 */
	//@Override
	public void run() {
		// Read the issue file
		File file = new File(getPath());
		List<Issue> issues = new ArrayList<Issue>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));

			// First line of the file is the header and not values
			String line = in.readLine();
			int countSeperator = 0;

			int index = 0;
			String issueValue = "";
			String issueValues[] = new String[4];

			while ((line = in.readLine()) != null) {
				index = 0;
				countSeperator = 0;
				while (countSeperator < 21) {
					if (line.charAt(index) == ';') {
						switch (countSeperator) {
						case 0:
							issueValues[0] = issueValue;
							break;
						case 4:
							issueValues[1] = issueValue;
							break;
						case 2:
							issueValues[2] = issueValue;
							break;
						}
						countSeperator++;
						issueValue = "";
					} else {
						issueValue += line.charAt(index);
					}
					index++;
				}
				int length = line.length();
				issueValues[3] = line.substring(index, length);

				issues.add(new Issue(Integer.parseInt(issueValues[0]), issueValues[1], issueValues[2], issueValues[3]));

			}

			// 3% of the work is done now
			this.notifyProgress(3);
			this.getWriter().writeIssues(issues);
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

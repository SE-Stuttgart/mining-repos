package srmplugin.wordcloud;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observer;

import javax.swing.JOptionPane;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;

import atsr.controller.transformer.Transformer;
import atsr.model.Commit;
import atsr.model.Settings;
import srmplugin.Preferences;
import srmprocess.DBConnection;

public class DateTableBuilder extends Transformer {

	public DateTableBuilder(String path, Settings settings, Observer obs, IProgressMonitor pmonitor)
			throws SQLException {
		super(path, settings, obs, pmonitor);

	}

	Map<String, Date> DateOfLastEdit = new HashMap<>();
	long newestPerFile = 0;
	long millis;

	Date newestDatePerFile;
	Date fileDate;

	/**
	 * query git for log of current file
	 */
	public void createDateTable() {
		
		Process process;

		try {
			ProcessBuilder pb = new ProcessBuilder(this.getSettings().getGitPath(), "log", "--pretty=format:%ad#",
					"--date=format:'%Y-%m-%d %H:%M:%S'", "--name-only");

			pb.directory(new File(this.getPath()));
			process = pb.start();
			InputStream cmdin = process.getInputStream();
			BufferedReader input = new BufferedReader(new InputStreamReader(cmdin));

			String line;
			List<String> linesList = new ArrayList<String>();
			// store the output of the git process in a list of lines
			while ((line = input.readLine()) != null) {
				linesList.add(line);
			}

			input.close();
			cmdin.close();

			if (linesList.size() > 0) {
				HashMap<String, Date> dateTable = new HashMap<String, Date>();
				List<String> files = new ArrayList<String>();
				String dateString = null;
				boolean block = false;
				for (String entry : linesList) {
					if (!block && entry.contains("#")) {
						dateString = entry;
						block = true;
					} else if (block && entry.contains("#")) {
						continue;
					} else {
						if (block) {
							if (entry.length() == 0) {

								for (String f : files) {

									if (dateTable.containsKey(f)) {
										Date dateInTable = dateTable.get(f);

										DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
										dateString = dateString.replace("#", "");
										dateString = dateString.replace("'", "");

										Date currentDate = formatter.parse(dateString);
										System.out.println("File: "+f);
										System.out.println("Current: "+currentDate.toString());
										System.out.println("In Tabl: "+dateInTable.toString());
										// If same file got committed on a newer point in time,
										// take the newer date, to get the most current commit date.
										if(currentDate.after(dateInTable)){
											dateTable.put(f, currentDate);
										}
										
										

									} else if (!dateTable.containsKey(f)) {
										DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
										dateString = dateString.replace("#", "");
										dateString = dateString.replace("'", "");
										Date currentDate = formatter.parse(dateString);
										dateTable.put(f, currentDate);
									}

								}
								files.clear();
								block = false;
							} else {
								files.add(entry);

							}
						} else {
							continue;
						}
					}

				}
				DateTable.lastUsedTable = dateTable;
				Preferences.now = new Date();
			}			
		} catch (IOException e) {
			// Error handling IO
			JOptionPane.showMessageDialog(null,
					"Error with the selected File! \nPlease check if the file still exists!", "IO Error",
					JOptionPane.ERROR_MESSAGE);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Color check(String filepath) {
		Color color = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		return color;
	}

}

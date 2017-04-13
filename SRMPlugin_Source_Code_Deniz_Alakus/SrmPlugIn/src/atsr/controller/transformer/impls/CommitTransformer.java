package atsr.controller.transformer.impls;

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
import java.util.List;
import java.util.Locale;
import java.util.Observer;

import javax.swing.JOptionPane;

import org.eclipse.core.runtime.IProgressMonitor;

import atsr.controller.transformer.Transformer;
import atsr.model.Commit;
import atsr.model.Settings;
import srmplugin.wizard.ATSRPage;

/**
 * Implementation of the transformer to transform commit (repository log data)
 * into the database
 * 
 * @author Simon Lehmann
 *
 */
public class CommitTransformer extends Transformer {

	/**
	 * Constructor
	 * 
	 * @param path
	 *            - Path of the git repository
	 * @param settings
	 *            - Settings from settingsView
	 * @param obs
	 *            - Observer to notify progress
	 */
	public CommitTransformer(String path, Settings settings, Observer obs, IProgressMonitor pmonitor)
			throws SQLException {
		super(path, settings, obs, pmonitor);
	}

	/**
	 * Executive function
	 */
	// @Override
	public void run() {
		Process process;
		try {
			ProcessBuilder pb = new ProcessBuilder(this.getSettings().getGitPath(), "log",
					"--pretty=format:%h#%an#%ad#%s", "--name-only");
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

			List<File> currentExistingFiles = new ArrayList<File>();

			if (linesList.size() > 0) {

				// Create commits and put them into a list
				List<File> files = new ArrayList<File>();
				List<Commit> commits = new ArrayList<Commit>();
				String header = null;
				Commit commit = null;
				boolean block = false;

				// If we ignore old deleted files, compute a list of all current
				// files still existing in the git repository
				if (ATSRPage.getIgnoreOldFiles()) {
					currentExistingFiles = getAllCurrentFiles(this.getPath(), currentExistingFiles);
					//remove system specific basepath
					currentExistingFiles = getProjectSpecificFiles(this.getPath(), currentExistingFiles);
				}

				// find blocks of commits and their files and compile the
				// information
				for (String row : linesList) {
					if (!block && row.contains("#")) {
						header = row;
						block = true;
					} else if (block && row.contains("#")) {
						continue;
					} else {
						if (block) {
							if (row.length() == 0) {

								// TODO remove files that are not part of the
								// current project.
								if (ATSRPage.getIgnoreOldFiles()) {
									removeOldDeletedFiles(files);
								}

								// System.out.println(files);
								// System.out.println(currentExistingFiles);
								commit = createCommit(header, files);
								if (commit != null) {
									commits.add(commit);
									// TODO delete me
									System.out.println(commit.getFiles());
								}
								files.clear();
								block = false;
							} else {
								files.add(new File(row));

							}
						} else {
							continue;
						}
					}
				}

				// 3% of the work is done now
				this.notifyProgress(3);
				this.getWriter().writeCommits(commits);
				// The transformation is finished
				this.notifyProgress(100);
				this.getWriter().close();

			} else {
				// Error handling wrong folder
				JOptionPane.showMessageDialog(null,
						"The selected folder is not a Git-Repository or\n " + "the git folder is not correct!",
						"Repository Error", JOptionPane.ERROR_MESSAGE);
				this.notifyProgress(0);
			}

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
			e.printStackTrace();
		}

	}

	/**
	 * Generates a list of files that are inside the specified folder
	 * 
	 * @param path
	 *            - Path to the root folder. Search recursively from there on.
	 * @return - List of files currently existing in the folder and all
	 *         subfolders.
	 */
	private List<File> getAllCurrentFiles(String path, List<File> currentFiles) {
		File directory = new File(path);

		// get all files from the directory
		File[] filesArray = directory.listFiles();
		for (File file : filesArray) {
			if (file.isFile()) {
				currentFiles.add(file);
			} else if (file.isDirectory()) {
				getAllCurrentFiles(file.getAbsolutePath(), currentFiles);
			}

		}
		
		return currentFiles;

	}
	
	private List<File> getProjectSpecificFiles(String basePath, List<File> systemFiles) {
		
		List<File> projectFiles = new ArrayList<File>();
		
		for(File file : systemFiles){
			String systemPath = file.getPath();
			String projectPath = systemPath.substring(basePath.length()+1);
			projectFiles.add(new File(projectPath));
		}
		
		return projectFiles;
	}
	

	/**
	 * Compares the files of the current commit against the files that are
	 * currently in the project folder.
	 * 
	 * @param files
	 *            - list of files in the current commit
	 */
	private void removeOldDeletedFiles(List<File> files) {
		// TODO Auto-generated method stub
		System.out.println("CheckBox True");
		System.out.println(this.getPath());

	}

	/**
	 * Creates a new object commit and needs only the header and the files as
	 * list
	 * 
	 * @param header
	 *            - header of one commit e.g.(5486558#Lukas Balzer#Mon Dec 1
	 *            12:13:00 2014 +0100#created astpa.extension)
	 * @param files
	 *            - all files of the commit as list
	 * @return a new commit object
	 */
	private Commit createCommit(String header, List<File> files) {
		Commit newCommit;
		String[] commitValues = new String[4];

		int index = 0;
		int countSeperator = 0;
		String headerValue = "";

		// The first three '#' split the string into id, author and date.
		while (countSeperator < 3) {
			if (header.charAt(index) == '#') {
				commitValues[countSeperator] = headerValue;
				headerValue = "";
				countSeperator++;
			} else {
				headerValue += header.charAt(index);
			}
			index++;
		}
		// After the third '#' the header consists of the message
		commitValues[3] = header.substring(index, header.length());

		/* Date Format */
		DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
		try {

			Date commitDate = df.parse(commitValues[2]);
			newCommit = new Commit(commitValues[0], commitValues[1], commitDate, commitValues[3]);
			for (File file : files) {
				newCommit.addFile(file);
			}
			return newCommit;
		} catch (ParseException e) {
			System.err.println("Parse Exception: " + commitValues[2] + ")");
			return null;
		}

	}
}

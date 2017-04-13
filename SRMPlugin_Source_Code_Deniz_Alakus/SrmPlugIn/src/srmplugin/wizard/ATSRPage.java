package srmplugin.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;

import atsr.controller.transformer.impls.CommitTransformer;
import atsr.controller.transformer.impls.DocuTransfromer;
import atsr.controller.transformer.impls.IssueTransformer;
import atsr.model.Settings;
import atsr.model.ValidateResult;
import atsr.view.TransformationView;
import srmplugin.SRMSettings;
import srmprocess.DBConnection;

public class ATSRPage extends WizardPage implements Observer {
	
	private Shell shell;
	private Text repo_path_field;
	private Text issue_path_field;
	private Text docu_path_field;
	private Text database_field;
	private Text git_field;
	
	private static boolean ignoreOldFiles = true;

	protected ATSRPage() {
		super("ATSR Settings","Repository Transformation",null);
		this.initSettings();
	}

	@Override
	public void createControl(Composite parent) {	
		
		Composite composite =  new Composite(parent, SWT.NULL);
		this.shell = composite.getShell();
		
		GridLayout gl = new GridLayout();
		gl.numColumns = 3;
		composite.setLayout(gl);
		
		GridData gd = new GridData();
		gd.widthHint = 160;
		gd.horizontalSpan = 2;
		
		new Label(composite, SWT.NULL).setText("Database: ");
		database_field = new Text(composite, SWT.SINGLE);
		database_field.setLayoutData(gd);
		database_field.setText(SRMSettings.project_name);
		
		gd = new GridData();
		gd.widthHint = 160;
		
		new Label(composite, SWT.NULL).setText("Git.exe path: ");
		git_field = new Text(composite, SWT.BORDER);
		git_field.setLayoutData(gd);
		if(settings.getGitPath() != null)
			git_field.setText(settings.getGitPath());
		
		gd = new GridData();
		gd.widthHint = 85;
		gd.horizontalAlignment = SWT.BEGINNING;
		Button git_btn = new Button(composite, SWT.NONE);
		git_btn.setLayoutData(gd);
		git_btn.setText("...");
		git_btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog select_file = new FileDialog(shell);
				String path = select_file.open();
				if (path != null) {
					settings.setGitPath(path);
					git_field.setText(path);
					
					IEclipsePreferences node = ConfigurationScope.INSTANCE
							  .getNode("com.srmplugin");
					node.put("gitpath", path);
					try {
						node.flush();
					} catch (BackingStoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
				
		Group repo = new Group(composite, SWT.NULL);
		Group issue = new Group(composite, SWT.NULL);
		Group docu = new Group(composite, SWT.NULL);
		
		gd = new GridData();
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		
		repo.setText("Repository");
		repo.setLayout(gl);
		repo.setLayoutData(gd);
		
		issue.setText("Issue");
		issue.setLayout(gl);
		issue.setLayoutData(gd);
		
		docu.setText("Docu");
		docu.setLayout(gl);
		docu.setLayoutData(gd);
		
		gd = new GridData();
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		
		repo_path_field = new Text(repo, SWT.BORDER);
		repo_path_field.setLayoutData(gd);
		repo_path_field.setText(SRMSettings.project_path);
		
		issue_path_field = new Text(issue, SWT.BORDER);
		issue_path_field.setLayoutData(gd);
		
		docu_path_field = new Text(docu, SWT.BORDER);
		docu_path_field.setLayoutData(gd);
		
		gd = new GridData();
		gd.widthHint = 85;
		
		Button repo_btn = new Button(repo, SWT.NONE);
		repo_btn.setLayoutData(gd);
		repo_btn.setText("...");
		repo_btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				DirectoryDialog select_folder = new DirectoryDialog(shell);
				select_folder.setFilterPath(SRMSettings.project_path);
				String path = select_folder.open();
				if(path != null) {
					repo_path_field.setText(path);
					SRMSettings.commit_path = path;
				}
			}
		});
		
		Button issue_btn = new Button(issue, SWT.NONE);
		issue_btn.setLayoutData(gd);
		issue_btn.setText("...");
		issue_btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog select_file = new FileDialog(shell);
				select_file.setFilterExtensions(new String[] {"*.csv;*.txt"});
				select_file.setFilterPath(SRMSettings.project_path);
				String path = select_file.open();
				if (path != null) {
					issue_path_field.setText(path);
					SRMSettings.issue_path = path;
				}
			}
		});
		
		Button docu_btn = new Button(docu, SWT.NONE);
		docu_btn.setLayoutData(gd);
		docu_btn.setText("...");
		docu_btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog select_file = new FileDialog(shell);
				select_file.setFilterExtensions(new String[] {"*.csv;*.txt"});
				select_file.setFilterPath(SRMSettings.project_path);
				String path = select_file.open();
				if (path != null) {
					docu_path_field.setText(path);
					SRMSettings.docu_path = path;
				}
			}
		});
		
		Button btnTransform = new Button(repo, SWT.NONE);
		btnTransform.setLayoutData(gd);
		btnTransform.setText("Transform");
		btnTransform.addSelectionListener(new CommitTransformListener());
		
		Button ignoreButton  = new Button(repo, SWT.CHECK);
		ignoreButton.setText("Ignore deleted files");
		ignoreButton.setSelection(true); //by default we want to ignore deleted files, that are not part of the current project anymore.
		
		//If user changes Checkbox, this extracts current checkbox status. Status will be used for transformation.
		ignoreButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event){
				Button button = (Button) event.getSource();
				ignoreOldFiles = button.getSelection();
			}
		});
		
		Button btnTransform_1 = new Button(issue, SWT.NONE);
		btnTransform_1.setLayoutData(gd);
		btnTransform_1.setText("Transform");
		btnTransform_1.addSelectionListener(new IssueTransformListener());
		
		Button btnTransform_2 = new Button(docu, SWT.NONE);
		btnTransform_2.setLayoutData(gd);
		btnTransform_2.setText("Transform");
		btnTransform_2.addSelectionListener(new DocuTransformListener());
		
		composite.pack();
		
		setControl(composite);
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return DBConnection.getDBConnection().commitTablesExist();
	}

	private TransformationView transformationView;
	private Settings settings;

	
	/**
	 * ActionListener for the commit button in transformationView
	 * 
	 * @author Simon Lehmann
	 *
	 */
	class CommitTransformListener extends SelectionAdapter {

		/**
		 * Starting the transformation of the log-data into the database
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (SRMSettings.commit_path != null) {
				settings.setDatabaseName(SRMSettings.project_name);
				try {
					ATSRPage.this.getContainer().run(true, true, pmonitor -> {
						try {
							new CommitTransformer(SRMSettings.commit_path, settings,
									getController(), pmonitor).run();
						} catch (SQLException e1) {
							JOptionPane.showMessageDialog(transformationView,
									"No connection to the database! \nCheck the settings!", "SQL Error",
									JOptionPane.ERROR_MESSAGE);
						}
						pmonitor.done();
					});
					ATSRPage.this.getWizard().getContainer().updateButtons();
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(transformationView, "Please enter a repository path!", "Info",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}

	}

	/**
	 * ActionListener for the issue button in transformationView
	 * 
	 * @author Simon Lehmann
	 *
	 */
	class IssueTransformListener extends SelectionAdapter {

		/**
		 * Starting the transformation of a issue-file into the database
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (SRMSettings.issue_path != null) {
				settings.setDatabaseName(SRMSettings.project_name);
				try {
					ATSRPage.this.getContainer().run(true, true, pmonitor -> {
						try {
							new IssueTransformer(SRMSettings.issue_path, settings, getController(), pmonitor).run();
						} catch (SQLException e1) {
							JOptionPane.showMessageDialog(transformationView,
									"No connection to the database! \nCheck the settings!", "SQL Error",
									JOptionPane.ERROR_MESSAGE);
						}
						pmonitor.done();
					});
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(transformationView, "Please enter a issue path!", "Info",
						JOptionPane.INFORMATION_MESSAGE);
			}

		}

	}

	/**
	 * ActionListener for the docu button in transformationView
	 * 
	 * @author Simon Lehmann
	 *
	 */
	class DocuTransformListener extends SelectionAdapter {

		/**
		 * Starting the transformation of a docu-file into the database
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (SRMSettings.docu_path != null) {
				settings.setDatabaseName(SRMSettings.project_name);
				try {
					ATSRPage.this.getContainer().run(true, true, pmonitor -> {
						try {
							new DocuTransfromer(SRMSettings.docu_path, settings, getController(), pmonitor).run();
						} catch (SQLException e1) {
							JOptionPane.showMessageDialog(transformationView,
									"No connection to the database! \nCheck the settings!", "SQL Error",
									JOptionPane.ERROR_MESSAGE);
						}
						pmonitor.done();
					});
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(transformationView, "Please enter a docu path!", "Info",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}

	}
	
	
	/**
	 * Check if there is already a settings-file. Is there a file import it.
	 */
	private void initSettings() {
		settings = new Settings();

		// Default Values
		settings.setUsername("root");
		settings.setPassword("");
		settings.setDatabaseName(SRMSettings.project_name);
		settings.setIpAddress("localhost");
		settings.setPort(3306);
		
		IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode("com.srmplugin");
		String gitpath = node.get("gitpath", "empty");
		
		if(gitpath.equals("empty")) {
			if(System.getProperty("os.name").indexOf("win") >= 0)
				settings.setGitPath("\"C:" + File.separator + "Program Files" + File.separator + "Git" + File.separator
						+ "bin" + File.separator + "git.exe\"");
			else
				settings.setGitPath("git");
		} else {
			settings.setGitPath(gitpath);
		}
	}
	
	
	private Observer getController() {
		return this;
	}
	
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof Integer) {
			int value = (int) arg;
//			this.transformationView.setProgressValue(value);
			if ((value == 100) || (value == 0)) {
//				transformationView.setButtonEnable(true);				
			}
		} else if (arg instanceof ValidateResult) {
			ValidateResult val = (ValidateResult) arg;
			if (val.getCode() == 100) {
//				transformationView.setDatabaseConnected();
			} else {
//				transformationView.setDatabaseNotConnected();
			}
		}
	}

	public static boolean getIgnoreOldFiles() {
		return ignoreOldFiles;
	}


}

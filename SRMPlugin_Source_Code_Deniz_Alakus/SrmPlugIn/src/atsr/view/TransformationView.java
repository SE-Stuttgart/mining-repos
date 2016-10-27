package atsr.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import atsr.model.Transformations;

/**
 * The main view of this application Transform with the buttons from file or log
 * into a database
 * 
 * @author Simon Lehmann
 *
 */
@SuppressWarnings("serial")
public class TransformationView extends JFrame {

	private JButton btnCommitTransform;
	private JButton btnIssueTransform;
	private JButton btnDocuTransform;
	private JTextField txfCommitTransform;
	private JTextField txfIssueTransform;
	private JTextField txfDocuTransform;
	private JMenuItem mntmExit;
	private JMenuItem mntmShowSettings;
	private JProgressBar transformProgress;
	private JLabel lblDbConn;
	private JTextField txfDbName;

	/**
	 * Constructor
	 * 
	 * @param commitTransformListener
	 *            - ActionListener of the commit transform button
	 * @param issueTransformListener
	 *            - ActionListener of the issue transform button
	 * @param docuTransformListener
	 *            - ActionListener of the docu transform button
	 * @param exitListener
	 *            - ActionListener of the exit menu item
	 * @param showSettingsListener
	 *            - ActionListener of the preferences menu item
	 */
	public TransformationView(ActionListener commitTransformListener, ActionListener issueTransformListener,
			ActionListener docuTransformListener, ActionListener exitListener, ActionListener showSettingsListener) {
		super("ATSR");

		this.initForm();

		btnCommitTransform.addActionListener(commitTransformListener);
		btnIssueTransform.addActionListener(issueTransformListener);
		btnDocuTransform.addActionListener(docuTransformListener);
		//mntmExit.addActionListener(exitListener);
		mntmShowSettings.addActionListener(showSettingsListener);

	}

	/**
	 * Set the new value of the progressbar at the bottom
	 * 
	 * @param value
	 *            - The percentage value of the transformation-progress
	 */
	public void setProgressValue(int value) {
		if (value < 1) {
			transformProgress.setValue(0);
		} else if (value > 99) {
			transformProgress.setValue(100);
		} else {
			transformProgress.setValue(value);
		}
	}

	/**
	 * Initialize the user interface
	 */
	private void initForm() {
		//this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		this.setBounds(200, 200, 510, 400);
		this.setResizable(false);

		// ---------- MenuBar -------------------------------

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);

		JMenu mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);

		mntmShowSettings = new JMenuItem("Preferences");
		mnSettings.add(mntmShowSettings);

		JPanel projectPane = new JPanel();
		getContentPane().add(projectPane, BorderLayout.NORTH);
		projectPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		lblDbConn = new JLabel("");
		projectPane.add(lblDbConn);

		// ---------- ProgressPane --------------------------

		JPanel progressPane = new JPanel();
		progressPane.setLayout(new BorderLayout());
		this.getContentPane().add(progressPane, BorderLayout.SOUTH);

		transformProgress = new JProgressBar(0, 100);
		transformProgress.setStringPainted(true);
		progressPane.add(transformProgress, BorderLayout.CENTER);

		// ---------- TransformPane -------------------------

		JPanel transformPane = new JPanel();
		transformPane.setLayout(new GridLayout(4, 1));
		this.getContentPane().add(transformPane, BorderLayout.CENTER);

		JPanel dbPane = new JPanel();
		FlowLayout flowLayout = (FlowLayout) dbPane.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		dbPane.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Database", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		transformPane.add(dbPane);

		JLabel lblDbName = new JLabel("Name:");
		dbPane.add(lblDbName);

		txfDbName = new JTextField();
		dbPane.add(txfDbName);
		txfDbName.setColumns(10);

		// ---------- CommitPane ----------------------------

		JPanel commitPane = new JPanel();
		commitPane.setBorder(new TitledBorder("Repository"));
		commitPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		transformPane.add(commitPane);

		txfCommitTransform = new JTextField(30);
		commitPane.add(txfCommitTransform);

		JButton btnCommitFileChooser = new JButton("...");
		btnCommitFileChooser.addActionListener(new CommitFileChooserListener());
		commitPane.add(btnCommitFileChooser);

		btnCommitTransform = new JButton("Transform");
		commitPane.add(btnCommitTransform);

		// ---------- IssuePane -----------------------------

		JPanel issuePane = new JPanel();
		issuePane.setBorder(new TitledBorder("Issue"));
		issuePane.setLayout(new FlowLayout(FlowLayout.LEFT));
		transformPane.add(issuePane);

		txfIssueTransform = new JTextField(30);
		issuePane.add(txfIssueTransform);

		JButton btnIssueFileChooser = new JButton("...");
		btnIssueFileChooser.addActionListener(new IssueFileChooserListener());
		issuePane.add(btnIssueFileChooser);

		btnIssueTransform = new JButton("Transform");
		issuePane.add(btnIssueTransform);

		// ---------- DocuPane ------------------------------

		JPanel docuPane = new JPanel();
		docuPane.setBorder(new TitledBorder("Docu"));
		docuPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		transformPane.add(docuPane);

		txfDocuTransform = new JTextField(30);
		docuPane.add(txfDocuTransform);

		JButton btnDocuFileChooser = new JButton("...");
		btnDocuFileChooser.addActionListener(new DocuFileChooserListener());
		docuPane.add(btnDocuFileChooser);

		btnDocuTransform = new JButton("Transform");
		docuPane.add(btnDocuTransform);
		this.pack();
	}

	/**
	 * Set the Icon of the database-status to "connected"
	 */
//	public void setDatabaseConnected() {
//		Image dbConn = new ImageIcon(getClass().getResource("/res/connected_shadow.png")).getImage()
//				.getScaledInstance(300, 30, Image.SCALE_SMOOTH);
//		lblDbConn.setIcon(new ImageIcon(dbConn));
//	}

	/**
	 * Set the Icon of the database-status to "not connected"
	 */
//	public void setDatabaseNotConnected() {
//		Image dbConn = new ImageIcon(getClass().getResource("/res/not_connected_shadow.png")).getImage()
//				.getScaledInstance(300, 30, Image.SCALE_SMOOTH);
//		lblDbConn.setIcon(new ImageIcon(dbConn));
//	}

	/**
	 * Set the Icon of the database-status to "waiting"
	 */
//	public void waitForDatabaseConnection() {
//		lblDbConn.setIcon(new ImageIcon(getClass().getResource("/res/loading.gif")));
//	}

	/**
	 * Set all Transformationbuttons to the state
	 * 
	 * @param state
	 */
	public void setButtonEnable(boolean state) {
		btnCommitTransform.setEnabled(state);
		btnDocuTransform.setEnabled(state);
		btnIssueTransform.setEnabled(state);
	}

	// Getter of the JTextfields

	public String getCommitPath() {
		return txfCommitTransform.getText();
	}

	public String getIssuePath() {
		return txfIssueTransform.getText();
	}

	public String getDocuPath() {
		return txfDocuTransform.getText();
	}

	public String getDbName() {
		return txfDbName.getText().toLowerCase();
	}

	public void setDbName(String name) {
		txfDbName.setText(name);
	}

	// FileChooser ActionListener

	class CommitFileChooserListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String path = getPath(Transformations.Commit);
			if (path != "") {
				if (path.contains(" ")) {
					txfCommitTransform.setText("\"" + path + "\"");
				} else {
					txfCommitTransform.setText(path);
				}
			}
		}

	}

	class IssueFileChooserListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String path = getPath(Transformations.Issue);
			if (path != "") {
				txfIssueTransform.setText(path);
			}
		}

	}

	class DocuFileChooserListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String path = getPath(Transformations.Docu);
			if (path != "") {
				txfDocuTransform.setText(path);
			}
		}

	}

	/**
	 * Open a Filechooser and return the path of the selected file
	 * 
	 * @param type
	 *            - Enum which transformation type
	 * @return the path of the file
	 */
	private String getPath(Transformations type) {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		switch (type) {
		case Commit:
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			break;
		case Issue:
		case Docu:
			String[] extension = { "csv" };
			FileNameExtensionFilter filter = new FileNameExtensionFilter("csv", extension);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(filter);
			break;
		}

		int returnVal = fc.showOpenDialog(TransformationView.this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile().getAbsolutePath();
		}
		return "";
	}
}

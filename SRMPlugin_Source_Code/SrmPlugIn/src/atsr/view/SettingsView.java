package atsr.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import atsr.controller.ValidateDatabaseConnection;
import atsr.model.ValidateResult;

/**
 * The settings dialog to change the Preferences
 * 
 * @author Simon Lehmann
 *
 */
@SuppressWarnings("serial")
public class SettingsView extends JDialog implements Observer {

	private JButton btnConfirm;
	private JButton btnCancel;
	private JTextField txfGitPath;
	private JTextField txfUsername;
	private JTextField txfPassword;
	private JTextField txfIpAddress;
	private JTextField txfPort;
	private JLabel lblUsernameIcon;
	private JLabel lblPasswordIcon;
	private JLabel lblPortIcon;
	private JLabel lblIpAddressIcon;
	private JLabel lblStatus;
	private int highestValidId;
	private int validId;
	private String password;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            - parent object
	 * @param confirmListener
	 *            - ActionListener for the confirm button
	 * @param cancelListener
	 *            - ActionListener for the cancel button
	 * @param username
	 * @param password
	 * @param ipAddress
	 * @param port
	 * @param gitPath
	 */
	public SettingsView(JFrame parent, ActionListener confirmListener, ActionListener cancelListener, String username,
			String password, String ipAddress, String port, String gitPath) {

		super(parent);
		this.setModalityType(DEFAULT_MODALITY_TYPE);
		this.initForm(username, password, ipAddress, port, gitPath);
		btnConfirm.addActionListener(confirmListener);
		btnCancel.addActionListener(cancelListener);

		// only for the first DocumentListener update event (showDialog)
		this.password = password;
		
		// For Observer interface
		highestValidId = 0;
		
		// For DocumentFilter 
		validId = 0;
	}

	/**
	 * initialize the dialog
	 * 
	 * @param port
	 * @param ipAddress
	 * @param password
	 * @param username
	 * @param gitPath
	 */
	private void initForm(String username, String password, String ipAddress, String port, String gitPath) {
		this.setBounds(300, 300, 500, 250);
		this.setTitle("Preferences");
		this.setResizable(false);
		getContentPane().setLayout(new BorderLayout());

		// ---------- ButtonPane ----------------------------

		JPanel buttonPane = new JPanel(new FlowLayout());
		this.getContentPane().add(buttonPane, BorderLayout.SOUTH);

		btnConfirm = new JButton("OK");
		buttonPane.add(btnConfirm);

		btnCancel = new JButton("Cancel");
		buttonPane.add(btnCancel);

		// ---------- SettingsPane --------------------------

		JPanel settingsPane = new JPanel(new BorderLayout());
		this.getContentPane().add(settingsPane, BorderLayout.CENTER);

		// ---------- GitPane -------------------------------

		JPanel gitPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		gitPane.setBorder(new TitledBorder("Git (git.exe)"));
		settingsPane.add(gitPane, BorderLayout.SOUTH);

		JLabel lblGitPath = new JLabel("Path:");
		gitPane.add(lblGitPath);

		txfGitPath = new JTextField(30);
		txfGitPath.setText(gitPath);
		gitPane.add(txfGitPath);

		JButton btnGitPath = new JButton("...");
		btnGitPath.addActionListener(new GitPathFileChooserListener());
		gitPane.add(btnGitPath);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		settingsPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		// ---------- DatabasePane --------------------------

		JPanel databasePane = new JPanel(new GridLayout(2, 2));
		panel.add(databasePane);

		// ---------- UsernamePane --------------------------

		JPanel usernamePane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		databasePane.add(usernamePane);

		JLabel lblUsername = new JLabel("Username:");
		usernamePane.add(lblUsername);

		txfUsername = new JTextField(10);
		txfUsername.setText(username);
		txfUsername.getDocument().addDocumentListener(new ValidateDocumentListener());
		usernamePane.add(txfUsername);

		lblUsernameIcon = new JLabel("");
		usernamePane.add(lblUsernameIcon);

		// ---------- IpAddressPane -------------------------

		JPanel ipAddressPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		databasePane.add(ipAddressPane);

		JLabel lblIpAddress = new JLabel("Database IP:");
		ipAddressPane.add(lblIpAddress);

		txfIpAddress = new JTextField(10);
		txfIpAddress.setText(ipAddress);
		txfIpAddress.getDocument().addDocumentListener(new ValidateDocumentListener());
		ipAddressPane.add(txfIpAddress);

		lblIpAddressIcon = new JLabel("");
		ipAddressPane.add(lblIpAddressIcon);

		// ---------- PasswordPane --------------------------

		JPanel passwordPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		databasePane.add(passwordPane);

		JLabel lblPassword = new JLabel("Password:");
		passwordPane.add(lblPassword);

		txfPassword = new JTextField(10);
		txfPassword.setText(password);
		txfPassword.getDocument().addDocumentListener(new ValidateDocumentListener());
		passwordPane.add(txfPassword);

		lblPasswordIcon = new JLabel("");
		passwordPane.add(lblPasswordIcon);

		// ---------- PortPane ------------------------------

		JPanel portPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		databasePane.add(portPane);

		JLabel lblPort = new JLabel("Database Port:");
		portPane.add(lblPort);

		txfPort = new JTextField(10);
		txfPort.setText(port);
		txfPort.getDocument().addDocumentListener(new ValidateDocumentListener());
		((AbstractDocument) txfPort.getDocument()).setDocumentFilter(new PortDocumentFilter());
		portPane.add(txfPort);

		lblPortIcon = new JLabel("");
		portPane.add(lblPortIcon);

		JPanel validateaPane = new JPanel();
		panel.add(validateaPane, BorderLayout.SOUTH);

		lblStatus = new JLabel("");
		lblStatus.setForeground(Color.RED);
		lblStatus.setFont(new Font("Tahoma", Font.ITALIC, 11));
		validateaPane.add(lblStatus);
	}

	/**
	 * Set the databaseconnection-denied-icon at the given label
	 * 
	 * @param label
	 */
	private void setIconDenied(JLabel label) {
		Image userIcon = new ImageIcon(getClass().getResource("/res/denied.png")).getImage().getScaledInstance(16, 16,
				Image.SCALE_SMOOTH);
		label.setIcon(new ImageIcon(userIcon));

		this.validate();
		this.repaint();
	}
	/**
	 * Set the databaseconnection-confirm-icon at the given label
	 * 
	 * @param label
	 */
	private void setIconConfirmed(JLabel label) {
		Image userIcon = new ImageIcon(getClass().getResource("/res/confirm.png")).getImage().getScaledInstance(16, 16,
				Image.SCALE_SMOOTH);
		label.setIcon(new ImageIcon(userIcon));

		this.validate();
		this.repaint();
	}
	
	/**
	 * Set the databaseconnection-loading-icon at the given label
	 * 
	 * @param label
	 */
	private void setIconLoading(JLabel label) {
		label.setIcon(new ImageIcon(getClass().getResource("/res/loading.gif")));

		this.validate();
		this.repaint();
	}
	
	/**
	 * remove the icon at the given label
	 * 
	 * @param label
	 */
	private void removeIcon(JLabel label) {
		label.setIcon(null);

		this.validate();
		this.repaint();
	}

	/* Getter and setter of the JTextfields */

	public String getUsername() {
		return txfUsername.getText();
	}

	public String getPassword() {
		return txfPassword.getText();
	}

	public String getIpAddress() {
		return txfIpAddress.getText();
	}

	public String getPort() {
		return txfPort.getText();
	}

	public String getGitPath() {
		return txfGitPath.getText();
	}

	public void showDialog() {
		//invoke the DocumentListener update event
		txfPassword.setText(password + "x");
		txfPassword.setText(password);
		
		// Set the dialog visible
		this.setVisible(true);
		
	}

	// FileChooser ActionListener

	/**
	 * ActionListener of the FilChooser for the Git path
	 * 
	 * @author Simon Lehmann
	 *
	 */
	class GitPathFileChooserListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.setMultiSelectionEnabled(false);
			String[] extension = { "exe" };
			FileNameExtensionFilter filter = new FileNameExtensionFilter("exe", extension);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(filter);

			int returnVal = fc.showOpenDialog(SettingsView.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String path = fc.getSelectedFile().getAbsolutePath();
				if (path.contains(" ")) {
					txfGitPath.setText("\"" + path + "\"");
				} else {
					txfGitPath.setText(path);
				}
			}
		}
	}

	/**
	 * Check if the values port, ip, username and password are correct
	 * 
	 * @author Simon
	 *
	 */
	class ValidateDocumentListener implements DocumentListener {
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			validate(e);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			validate(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			validate(e);
		}

		private void validate(DocumentEvent e) {
			btnConfirm.setEnabled(false);
			btnCancel.setEnabled(false);
			if (e.getDocument().equals(txfIpAddress.getDocument()) || e.getDocument().equals(txfPort.getDocument())) {
				setIconLoading(lblPortIcon);
				setIconLoading(lblIpAddressIcon);				
			}
			int port = 0;
			try {
				port = Integer.parseInt(getPort());
			} catch (NumberFormatException ex) {
				port = -1;
			}
			ValidateDatabaseConnection val = new ValidateDatabaseConnection(validId, getUsername(), getPassword(),
					getIpAddress(), port);
			val.addObserver(getObeserver());
			new Thread(val).start();
			validId++;
		}

	}

	/**
	 * DocumentFilter of the port JTextfield (only digits)
	 * 
	 * @author Simon
	 *
	 */
	class PortDocumentFilter extends DocumentFilter {
		Pattern regEx = Pattern.compile("\\d*");

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
				throws BadLocationException {
			Matcher matcher = regEx.matcher(text);
			if (!matcher.matches()) {
				return;
			}
			super.replace(fb, offset, length, text, attrs);
		}
	}

	/**
	 * Get updates from the validation of the databaseconnection
	 */
	@Override
	public synchronized void update(Observable o, Object arg) {
		if (arg instanceof ValidateResult) {
			ValidateResult val = (ValidateResult) arg;
			if (highestValidId <= val.getValidId()) {
				switch (val.getCode()) {
				case 100:
					setIconConfirmed(lblIpAddressIcon);
					setIconConfirmed(lblPasswordIcon);
					setIconConfirmed(lblPortIcon);
					setIconConfirmed(lblUsernameIcon);
					lblStatus.setText("");
					break;
				case 0:
					setIconDenied(lblPortIcon);
					setIconDenied(lblIpAddressIcon);
					removeIcon(lblPasswordIcon);
					removeIcon(lblUsernameIcon);
					lblStatus.setText("No Connection. Port or/and IP-Address are incorrect");
					break;
				case 1045:
					setIconDenied(lblPasswordIcon);
					setIconDenied(lblUsernameIcon);
					setIconConfirmed(lblIpAddressIcon);
					setIconConfirmed(lblPortIcon);
					lblStatus.setText("No Access. Username or/and Password are incorrect");
					break;
				default:
					break;
				}
				highestValidId = val.getValidId();
			}
			btnConfirm.setEnabled(true);
			btnCancel.setEnabled(true);
		}
	}

	/**
	 * 
	 * @return the settingsView as Observer
	 */
	public Observer getObeserver() {
		return this;
	}
}

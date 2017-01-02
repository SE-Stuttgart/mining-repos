package srmplugin;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JLabel;

import srmprocess.Communication;
import srmprocess.DBConnection;

public class WordCloud extends ViewPart {
	public static final String ID = "SrmPlugIn.WordCloud";
	List<String> filenames = new ArrayList<String>();

	// the listener we register with the selection service
	private ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			// we ignore our own selections
			if (sourcepart != WordCloud.this) {
				filenames.clear();
				determineThePathOfTheSelectedFileAndSendIt(sourcepart, selection);
			}
		}

	};

	private void determineThePathOfTheSelectedFileAndSendIt(IWorkbenchPart sourcepart, ISelection selection) {
		// TODO Auto-generated method stub

	}

	DBConnection dataBaseCon = DBConnection.getDBConnection();

	public void OnStart() {

		System.out.println("Created Word Cloud Table");
	}

	
	private void addFilenameToList(String filepath) {
		if (((filepath) != (null))
				&& (filepath).length() > 0
				&& !(filepath).equals(" ")
				&& filepath.contains("/")
				&& filepath.contains(".")) {

			String filename = getFileName(filepath);
			filenames.add(filename);
		}
		
	}
	
	/**
	 * Extracts the filename from a given property path string
	 * 
	 * @param pathString
	 * @return
	 */
	public String getFileName(String pathString) {
		String filename = "!~UNEXPECTED ERROR~!";

		String path = pathString;
		int lastDash;
		int lastDot;
		try {

			if (path.contains(".") && path.contains("/")) {
				lastDash = path.lastIndexOf("/");
				lastDot = path.lastIndexOf(".");
				if (lastDot > lastDash) {
					filename = path.substring(lastDash + 1, lastDot);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return filename;
	}

	@Override
	public void createPartControl(Composite parent) {
		// registriere Listener
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);

		BundleContext ctx = FrameworkUtil.getBundle(WordCloud.class).getBundleContext();

		Composite swtAwtComponent = new Composite(parent, SWT.EMBEDDED);
		java.awt.Frame frame = SWT_AWT.new_Frame(swtAwtComponent);
		javax.swing.JPanel panel = new javax.swing.JPanel();

		EventHandler handler = event -> {

			if (Communication.control) {
				// TODO: Clear the panel

				System.out.println("1");

			} else {

				if (parent.getDisplay().getThread() == Thread.currentThread()) {

					// add clickable labels for filenames tied to filepaths via
					// some ID

					// get file name of path
					// src/main/java/de/MainClass.java -> MainClass

					System.out.println("### DEBUG ### " + ((String) event.getProperty("file")));

					addFilenameToList((String) event.getProperty("file"));
					

					for (String file : filenames) {
						System.out.println(file);

					}
					System.out.println("################");
					System.out.println("################");

				} else {
					parent.getDisplay().syncExec(() -> System.out.println("3"));
				}
			}
		};

		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, "viewcommunicationfile/*");
		ctx.registerService(EventHandler.class, handler, properties);

		OnStart();

		panel.setForeground(Color.white);

		frame.add(panel);
		frame.setForeground(Color.white);

	}

	

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}

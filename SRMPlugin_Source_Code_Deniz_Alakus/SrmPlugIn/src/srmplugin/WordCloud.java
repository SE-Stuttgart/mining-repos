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
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JLabel;

import srmprocess.Communication;
import srmprocess.DBConnection;

public class WordCloud extends ViewPart {
	public static final String ID = "SrmPlugIn.WordCloud";

	// the listener we register with the selection service
	private ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			// we ignore our own selections
			if (sourcepart != WordCloud.this) {
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
				//TODO: Clear the panel
				
				System.out.println("1");
				
			} else {

				if (parent.getDisplay().getThread() == Thread.currentThread()) {

					//add clickable labels for filenames tied to filepaths via some ID
					
					System.out.println(event.getProperty("file"));
				} else {
					parent.getDisplay().syncExec( () -> System.out.println("3") );
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

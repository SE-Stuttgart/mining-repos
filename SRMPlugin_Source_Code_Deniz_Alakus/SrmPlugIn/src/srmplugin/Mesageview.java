package srmplugin;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import srmprocess.Communication;
import srmprocess.DBConnection;

public class Mesageview extends ViewPart {
	public static final String ID = "SrmPlugIn.Mesageview";

	DBConnection dataBaseCon = DBConnection.getDBConnection();;
	Communication communication = new Communication();

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		// Create the containing tab folder
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);

		// Create each tab and set its text, tool tip text, image, and control
		TabItem commit = new TabItem(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		commit.setText("Commit Information ");
		commit.setToolTipText("Commit Information");
		commit.setControl(getTabcommitControl(tabFolder));

		TabItem issue = new TabItem(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		issue.setText("Issue Information");
		issue.setToolTipText("Issue Information");
		issue.setControl(getTabIssueControl(tabFolder));

		TabItem docu = new TabItem(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		docu.setText("Path Information");
		docu.setToolTipText("Path Information");

		docu.setControl(getTabDocuControl(tabFolder));

	}

	/**
	 * 
	 * commitViedeki listview olusturma metodunu oldugu bibi buraya kopyaladik
	 * 
	 */

	private enum Control {
		CommitID, CommitMessage, Path, Pathdescription, IssueId, Issuedescription, Issuetype
	}

	// Commit Information Tab des Message View definiert.
	// Dieser Tab beinhaltet eine Tabelle mit Spalten "Commit-ID und Commit Message"
	// Benoetigte Informationen werden in dieser Methode Ueberrmittelt.

	private org.eclipse.swt.widgets.Control getTabcommitControl(TabFolder tabFolder) {
		// Create some labels and text fields
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		
		Table t = new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.minimumWidth = SWT.DEFAULT;
		t.setLayoutData(gd);
		
		TableColumn tc = new TableColumn(t, SWT.NULL);
		tc.setText(Control.CommitID.toString());
		tc.pack();
		tc = new TableColumn(t, SWT.NULL);
		tc.setText(Control.CommitMessage.toString());
		tc.setWidth(300);
		
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		
		t.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Communication.control = true;
				// Control Event zur Issue Information Tab des Message View gesendet
				communication.ViewCommunication("IssueId", communication.view("Control"),
						"viewcommunicationIssueId/syncEvent");
				communication.ViewCommunication("Issuedescription", communication.view("Control"),
						"viewcommunicationIssuedescription/syncEvent");
				communication.ViewCommunication("Issuetype", communication.view("Control"),
						"viewcommunicationIssuetype/syncEvent");

				int index = 0, j = 0;
				// Selektierte Commit-Message wird in einem String-Array gespeichert
				String[] bufferValue = t.getSelection()[0].getText(1).split(" ");
				for (; j < DBConnection.issueData.size(); j++) {
					for (int i = 0; i < bufferValue.length; i++) {
						if (bufferValue[i].equals(DBConnection.issueData.get(j).get(0))
								&& index < DBConnection.issueData.size()) {
							// Falls in der Commit-Message eine Issue-ID gefunden wurde,
							// werden Issue-ID, Issue-Description und Issue Type an die
							// Issue Information Tab des Message View  gesendet
							Communication.control = false;
							communication.ViewCommunication("IssueId",
									communication.view(DBConnection.issueData.get(j).get(0)),
									"viewcommunicationIssueId/syncEvent");
							communication.ViewCommunication("Issuedescription",
									communication.view(DBConnection.issueData.get(j).get(1)),
									"viewcommunicationIssuedescription/syncEvent");
							communication.ViewCommunication("Issuetype",
									communication.view(DBConnection.issueData.get(j).get(2)),
									"viewcommunicationIssuetype/syncEvent");
							index++;

						}
					}

				}
				// Falls keine Issue-ID vorhanden ist
				if (index == 0 && j == DBConnection.issueData.size()) {
					// index üzerinden oynama yapacaz ama önce issue degerlerini yeniden oluturalim

					Communication.control = true;
					// Control Event zur Issue Information Tab des Message View gesendet.
					communication.ViewCommunication("IssueId", communication.view("Control"),
							"viewcommunicationIssueId/syncEvent");
					communication.ViewCommunication("Issuedescription", communication.view("Control"),
							"viewcommunicationIssuedescription/syncEvent");
					communication.ViewCommunication("Issuetype", communication.view("Control"),
							"viewcommunicationIssuetype/syncEvent");

					Communication.control = false;
					// Fehler Event zur Issue Information Tab des Message View gesendet.
					communication.ViewCommunication("IssueId", communication.view("Die gewählte "),
							"viewcommunicationIssueId/syncEvent");
					communication.ViewCommunication("Issuedescription",
							communication.view("commitId enhält keine Issue"),
							"viewcommunicationIssuedescription/syncEvent");
					communication.ViewCommunication("Issuetype", communication.view("Mesasge"),
							"viewcommunicationIssuetype/syncEvent");
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		EventHandler handler = (final Event event) -> {
			if (Communication.control) {
				t.removeAll();
			} else {
				if (composite.getDisplay().getThread() == Thread.currentThread()) {
					ArrayList<String[]> commitdata = (ArrayList<String[]>)event.getProperty("commitdata");
					for(String[] d : commitdata) {
						TableItem ti = new TableItem(t, SWT.NULL);
						ti.setText(d);
					}
					t.getColumn(1).pack();
				} else {
					composite.getDisplay().syncExec( () -> {
						ArrayList<String[]> commitdata = (ArrayList<String[]>)event.getProperty("commitdata");
						for(String[] d : commitdata) {
							TableItem ti = new TableItem(t, SWT.NULL);
							ti.setText(d);
						}
					});
					t.getColumn(1).pack();
				}
			}
		};

		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, "viewcommunicationcommitdata/*");
		BundleContext ctx = FrameworkUtil.getBundle(Mesageview.class).getBundleContext();
		ctx.registerService(EventHandler.class, handler, properties);

		return composite;

	}

	// Issue Information Tab des Message View definiert.
	// In diesem TAb werden drei ListViewer "Commit-ID und Commit Message"
	// genutzt, benötige information werden in dieser Methode überrmittelt.

	private org.eclipse.swt.widgets.Control getTabIssueControl(TabFolder tabFolder) {
		// Create a multi-line text field
		Composite composite = new Composite(tabFolder, SWT.NONE);

		Vector<Integer> ID = new Vector<>();
		ID.addElement(5);
		ID.addElement(100);
		ID.addElement(100);
		PartControl(composite, "IssueId", "viewcommunicationIssueId/*", ID, Control.IssueId);
		// Call commitIdMSG werte
		Vector<Integer> msg = new Vector<>();
		msg.addElement(107);
		msg.addElement(250);
		msg.addElement(100);
		PartControl(composite, "Issuedescription", "viewcommunicationIssuedescription/*", msg,
				Control.Issuedescription);

		Vector<Integer> type = new Vector<>();
		type.addElement(360);
		type.addElement(100);
		type.addElement(100);
		PartControl(composite, "Issuetype", "viewcommunicationIssuetype/*", type, Control.Issuetype);

		return composite;

	}

	// Path Information Tab des Message View definiert.
	// In diesem Tab werden zwei ListViewer "Commit-ID" und "Commit Message"
	// genutzt, benoetige Informationen werden in dieser Methode Ueberrmittelt.

	private org.eclipse.swt.widgets.Control getTabDocuControl(TabFolder tabFolder) {

		Composite composite = new Composite(tabFolder, SWT.NONE);

		Vector<Integer> ID = new Vector<>();
		ID.addElement(5);
		ID.addElement(100);
		ID.addElement(100);
		PartControl(composite, "Pathdescription", "viewcommunicationdocuPathdescription/*", ID,
				Control.Pathdescription);
		// Call commitIdMSG werte
		Vector<Integer> msg = new Vector<>();
		msg.addElement(107);
		msg.addElement(400);
		msg.addElement(100);
		PartControl(composite, "Path", "viewcommunicationdocuPath/*", msg, Control.Path);
		return composite;

	}

	// ---- Hier werden LiswVIewer erzeugt....
	public void PartControl(final Composite Partparent, final String str, String strevent, Vector<Integer> setXY, Control cntrl) {

		Label lblChooseACommit = new Label(Partparent, SWT.NONE);
		lblChooseACommit.setBounds(setXY.get(0), 0, 100, 20);
		lblChooseACommit.setText(cntrl.toString());

		final ListViewer listViewer = new ListViewer(Partparent, SWT.H_SCROLL | SWT.V_SCROLL);

		final List list = listViewer.getList();
		list.setBounds(setXY.get(0), 30, setXY.get(1), setXY.get(2));

		// Hier werden gesendete Event empfängt, entkapselt und in zugehörigen View angezeigt.
		BundleContext ctx = FrameworkUtil.getBundle(Mesageview.class).getBundleContext();
		
		EventHandler handler = (final Event event) -> {
			if (Communication.control) {
				listViewer.getList().removeAll();
			} else {
				if (Partparent.getDisplay().getThread() == Thread.currentThread()) {
					listViewer.add(event.getProperty((str)));
				} else {
					Partparent.getDisplay().syncExec( () -> listViewer.add(event.getProperty(str)));
				}
			}
		};

		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, strevent);
		ctx.registerService(EventHandler.class, handler, properties);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {}

	public void dispose() {
		// important: We need do unregister our listener when the view is disposed
		super.dispose();
	}

}
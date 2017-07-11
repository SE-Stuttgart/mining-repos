package srmplugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Layout;
//import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.part.ViewPart;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

//import javax.print.attribute.standard.Copies;

//import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.layout.FillLayout;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.layout.RowLayout;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import srmprocess.Communication;
import srmprocess.DBConnection;
import srmprocess.Process;

public class CommittedChanges extends ViewPart {
	public static final String ID = "SrmPlugIn.CommittedChanges";
	DBConnection dataBaseCon = DBConnection.getDBConnection();
	Communication communication = new Communication();
	Process process = new Process();
	java.util.List<ServiceRegistration<EventHandler>> regList = new ArrayList<ServiceRegistration<EventHandler>>();

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		// Create the containing tab folder
		
		TabFolder tabFolder = new TabFolder(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		// Create each tab and set its text, tool tip text,
		// image, and control

		TabItem CCRanking = new TabItem(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		CCRanking.setText("Ranking Information");
		CCRanking.setToolTipText("Change Commit Ranking Information");
		CCRanking.setControl(getTabCCRankingControl(parent, tabFolder));

		TabItem commit = new TabItem(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		commit.setText("Commit Information ");
		commit.setToolTipText("Commit Informaion");
		commit.setControl(getTabcommitControl(tabFolder));
	}

	// ------Ranking Information Tab des Commit Changes View definiert.
	private org.eclipse.swt.widgets.Control getTabCCRankingControl(final Composite parent, TabFolder tabFolder) {

		// Create some labels and text fields
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new FillLayout());

		final ListViewer listViewer = new ListViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		final List list = listViewer.getList();

		// Hier werden gesendete Event empfangen, entkapselt und in der zugehoerigen
		// View angezeigt.
		BundleContext ctx = FrameworkUtil.getBundle(CommittedChanges.class).getBundleContext();
		EventHandler handler = event -> {

				if (Communication.control) {
					listViewer.getList().removeAll();
				}
				else {
					if (parent.getDisplay().getThread() == Thread.currentThread()) {
						listViewer.add(event.getProperty("serachcommitid"));
					} else {
						parent.getDisplay().syncExec( () -> listViewer.add(event.getProperty("serachcommitid")));
					}
				}
			};

		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, "viewcommunicationserachcommitid/*");
		regList.add(ctx.registerService(EventHandler.class, handler, properties));

		// Falls eine Commit-ID Gruppe aus dem
		// Ranking Information TAb des Commit Changes Views ausgew�hlt werden,
		// werden zwei Werte errmittelt.

		list.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				// ----------Zuerst werden Control Events zur alle Views -ausser
				// Selbst- gesendet.

				Communication.control = true;
				// Hier werden Control Event zum Coupled Changes View gesendet.
				communication.ViewCommunication("file", communication.view("Control"),
						"viewcommunicationfile/syncEvent");
				// Hier werden Control Event zum Commit Information Tab des
				// Message Views gesendet.
				communication.ViewCommunication("commitid", communication.view("Control"),
						"viewcommunicationcommitid/syncEvent");
				communication.ViewCommunication("commitidmasage", communication.view("Control"),
						"viewcommunicationcommitidmasage/syncEvent");
				// Hier werden Control Event zum Issue Information Tab des
				// Message Views gesendet.
				communication.ViewCommunication("IssueId", communication.view("Control"),
						"viewcommunicationIssueId/syncEvent");
				communication.ViewCommunication("Issuedescription", communication.view("Control"),
						"viewcommunicationIssuedescription/syncEvent");
				communication.ViewCommunication("Issuetype", communication.view("Control"),
						"viewcommunicationIssuetype/syncEvent");
				// Hier werden Control Event zum Path Information Tab des
				// Message Views gesendet.
				communication.ViewCommunication("Pathdescription", communication.view(communication.view("Control")),
						"viewcommunicationdocuPathdescription/syncEvent");
				communication.ViewCommunication("Path", communication.view(communication.view("Control")),
						"viewcommunicationdocuPath/syncEvent");
				// Hier werden Control Event zum Commit Information Tab des
				// Commit Changes Views gesendet.
				communication.ViewCommunication("CommitRanking", communication.view("Control"),
						"viewcommunicationcommitranking/syncEvent");
				communication.ViewCommunication("CommitmessageRanking", communication.view("Control"),
						"viewcommunicationcommitmessageranking/syncEvent");

				Communication.control = false;

				int a = list.getSelectionIndex();
				// Selektierte Commit-ID Gruppe in der Key "" nachgescuht danach
				// entsprechende Value sortiert danach
				// Cluster()-Method �bermittel.
				process.searchKlassifikation(Process.KlassifikationErgebnis.get(a));

				// Alle Cluster/File Gruppe Iterativ Coupled Changes View
				// gesendet
				for (int s = 0; s < Process.ClusterErgebnis.size(); s++) {
					int index = 0;
					// Alle Items/Files, die in einer File Gruppe sich befindet,
					// werden iterativ gesendet.
					Communication.control = false;
					for (int x = Process.ClusterErgebnis.get(s).size() - 1; x > 1
							+ Integer.parseInt(Process.ClusterErgebnis.get(s).get(0)); x--) {
						if (!Process.ClusterErgebnis.get(s).get(x).equals("null")) {
							communication.ViewCommunication("file",
									communication.view(String.valueOf(s + 1) + "." + index + " "
											+ Process.ClusterErgebnis.get(s).get(x)),
									"viewcommunicationfile/syncEvent");
							index++;
						}

					}
					// Nach alle File gesendet werden, werden eine leere Zeile
					// in Coupled Changes View hinzugef�gt.
					communication.ViewCommunication("file", communication.view(" "), "viewcommunicationfile/syncEvent");

				}

				// Alle Commit-Id der selektierten Commit-Id Gruppe und
				// gekoppelte Message werden
				// Commit Information Tab des Commit Changes View gesendet.
				for (int i = 0; i < Process.KlassifikationErgebnis.get(a).size(); i++) {
					communication.ViewCommunication("CommitRanking",
							communication.view(Process.KlassifikationErgebnis.get(a).get(i)),
							"viewcommunicationcommitranking/syncEvent");
					communication.ViewCommunication("CommitmessageRanking",
							communication
									.view(dataBaseCon.ReadCommitMessage(Process.KlassifikationErgebnis.get(a).get(i))),
							"viewcommunicationcommitmessageranking/syncEvent");
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		return composite;
	}

	// Commit Information Tab des Commit Cahnges View definiert.
	// In diesem TAb werden zwei ListViewer "Commit-ID und Commit Message"
	// genutzt,
	// F�r die Wiederhollende Codest�ck zu vermeiden, werden eine Methode
	// definirt,
	// ben�tige information werden in dieser Methode �berrmittelt.

	private org.eclipse.swt.widgets.Control getTabcommitControl(TabFolder tabFolder) {

		// Create some labels and text fields
		Composite composite = new Composite(tabFolder, SWT.NONE);

		// Hier wird eine ListViewer f�r Commit-ID erzeugt.
		Vector<Integer> ID = new Vector<>();
		ID.addElement(5);
		ID.addElement(100);
		ID.addElement(150);
		PartControl(composite, "CommitRanking", "viewcommunicationcommitranking/*", ID, Control.CommitID);
		// Hier wird eine ListViewer f�r Commit-Message erzeugt.
		Vector<Integer> msg = new Vector<>();
		msg.addElement(115);
		msg.addElement(300);
		msg.addElement(150);
		PartControl(composite, "CommitmessageRanking", "viewcommunicationcommitmessageranking/*", msg,
				Control.CommitMessage);
		return composite;
	}

	public enum Control {
		// CommitID,CommitMesage
		CommitID, CommitMessage
	}

	/// ---------In dieser Methode werden zwei Listviewer erzuegt.

	public void PartControl(final Composite Partparent, final String str, String strevent, Vector<Integer> setXY, Control cntrl) {

		// Zuerst werden Label erzeugt.
		Label lblChooseACommit = new Label(Partparent, SWT.NONE);

		lblChooseACommit.setBounds(setXY.get(0), 0, 100, 20);
		lblChooseACommit.setText(cntrl.toString());

		final ListViewer listViewer = new ListViewer(Partparent, SWT.H_SCROLL | SWT.V_SCROLL);
		final List list = listViewer.getList();
		list.setBounds(setXY.get(0), 30, setXY.get(1), setXY.get(2));

		// Hier werden gesendete Event empf�ngt, entkapselt und in zugeh�rigen
		// View angezeigt.
		BundleContext ctx = FrameworkUtil.getBundle(Mesageview.class).getBundleContext();
		EventHandler handler = new EventHandler() {

			public void handleEvent(final Event event) {
				if (Communication.control) {
					listViewer.getList().removeAll();
				} else {

					if (Partparent.getDisplay().getThread() == Thread.currentThread()) {
						listViewer.add(event.getProperty((str)));
					} else {
						Partparent.getDisplay().syncExec(new Runnable() {
							public void run() {
								listViewer.add(event.getProperty(str));
							}
						});
					}
				}

			}

		};

		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, strevent);
		regList.add(ctx.registerService(EventHandler.class, handler, properties));

		// Falls ein Commit-ID selektiert wird,...
		if (cntrl == Control.CommitID) {
			list.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {

					int a = list.getSelectionIndex();
					Communication communication = new Communication();

					Communication.control = true;
					// Hier werden Control Event zum Issue Information Tab des
					// Message Views gesendet.
					communication.ViewCommunication("IssueId", communication.view("Control"),
							"viewcommunicationIssueId/syncEvent");
					communication.ViewCommunication("Issuedescription", communication.view("Control"),
							"viewcommunicationIssuedescription/syncEvent");
					communication.ViewCommunication("Issuetype", communication.view("Control"),
							"viewcommunicationIssuetype/syncEvent");

					int index = 0, j = 0;
					// Commit-Mesasge des selektierten Commit-ID wird selektiert
					// danach in String array gespeichert.
					String[] bufferValue = dataBaseCon.ReadCommitMessage(list.getItem(a)).split(" ");
					for (; j < DBConnection.issueData.size(); j++) {

						for (int i = 0; i < bufferValue.length; i++) {

							if (bufferValue[i].equals(DBConnection.issueData.get(j).get(0))
									&& index < DBConnection.issueData.size()) {
								// Falls in der Commit-Message ein Issue-ID
								// gefunden w�rde,
								// werden Issue-ID und gekoppelte
								// Issue-Description und Issue Type werden
								// Issue Information Tab des Message View
								// gesendet.
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
					if (index == 0 && j == DBConnection.issueData.size()) { // index
																			// �zerinden
																			// oynama
																			// yapacaz
																			// ama
																			// �nce
																			// issue
																			// degerlerini
																			// yeniden
																			// oluturalim

						// Control Event zur Issue Information Tab des Message
						// View gesendet.
						Communication.control = true;
						communication.ViewCommunication("IssueId", communication.view("Control"),
								"viewcommunicationIssueId/syncEvent");
						communication.ViewCommunication("Issuedescription", communication.view("Control"),
								"viewcommunicationIssuedescription/syncEvent");
						communication.ViewCommunication("Issuetype", communication.view("Control"),
								"viewcommunicationIssuetype/syncEvent");

						// Fehler Event zur Issue Information Tab des Message
						// View gesendet.
						Communication.control = false;
						communication.ViewCommunication("IssueId", communication.view("Die gew�hlte "),
								"viewcommunicationIssueId/syncEvent");
						communication.ViewCommunication("Issuedescription",
								communication.view("commitId enh�lt keine Issue"),
								"viewcommunicationIssuedescription/syncEvent");
						communication.ViewCommunication("Issuetype", communication.view("Mesasge"),
								"viewcommunicationIssuetype/syncEvent");

					}

				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

			});

		}
		// Falls Commit-Message ausgew�hlt wird,
		if (cntrl == Control.CommitMessage) {

			list.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {

					Communication communication = new Communication();
					// Control Event zur Issue Information Tab des Message View
					// gesendet.
					Communication.control = true;
					communication.ViewCommunication("IssueId", communication.view("Control"),
							"viewcommunicationIssueId/syncEvent");
					communication.ViewCommunication("Issuedescription", communication.view("Control"),
							"viewcommunicationIssuedescription/syncEvent");
					communication.ViewCommunication("Issuetype", communication.view("Control"),
							"viewcommunicationIssuetype/syncEvent");

					int index = 0, j = 0;
					// Selektierte Commit-Mesasge wird selektiert danach in
					// String array gespeichert.
					String[] bufferValue = list.getItem(list.getSelectionIndex()).split(" ");

					for (; j < DBConnection.issueData.size(); j++) {

						for (int i = 0; i < bufferValue.length; i++) {

							if (bufferValue[i].equals(DBConnection.issueData.get(j).get(0))
									&& index < DBConnection.issueData.size()) {
								// Falls in der Commit-Message ein Issue-ID
								// gefunden w�rde,
								// werden Issue-ID und gekoppelte
								// Issue-Description und Issue Type werden
								// Issue Information Tab des Message View
								// gesendet.
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

					if (index == 0 && j == DBConnection.issueData.size()) {

						// Control Event zur Issue Information Tab des Message
						// View gesendet.
						Communication.control = true;
						communication.ViewCommunication("IssueId", communication.view("Control"),
								"viewcommunicationIssueId/syncEvent");
						communication.ViewCommunication("Issuedescription", communication.view("Control"),
								"viewcommunicationIssuedescription/syncEvent");
						communication.ViewCommunication("Issuetype", communication.view("Control"),
								"viewcommunicationIssuetype/syncEvent");

						// Fehler Event zur Issue Information Tab des Message
						// View gesendet.
						Communication.control = false;
						communication.ViewCommunication("IssueId", communication.view("Die gew�hlte "),
								"viewcommunicationIssueId/syncEvent");
						communication.ViewCommunication("Issuedescription",
								communication.view("commitId enh�lt keine Issue"),
								"viewcommunicationIssuedescription/syncEvent");
						communication.ViewCommunication("Issuetype", communication.view("Mesasge"),
								"viewcommunicationIssuetype/syncEvent");

					}

				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

			});

		}

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {

	}

	public void dispose() {
		// important: We need do unregister our listener when the view is
		// disposed

		for(ServiceRegistration<EventHandler> reg : regList){
			reg.unregister();			
		}

		super.dispose();
	}

}
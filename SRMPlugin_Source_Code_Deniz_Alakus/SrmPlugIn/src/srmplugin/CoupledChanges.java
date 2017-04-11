package srmplugin;

import java.util.ArrayList;



import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

//import org.eclipse.core.resources.IProject;
//import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

//import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.MouseEvent;
//import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.layout.GridData;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

//import org.osgi.framework.ServiceReference;
//import org.osgi.service.event.Event;
//import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
//import org.swtchart.internal.Grid;

import FPGA.FPGrowthAlgorithmus;

//import javafx.event.ActionEvent;

import org.eclipse.swt.widgets.List;
import org.eclipse.jface.viewers.ListViewer;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionListener;

import org.eclipse.ui.IWorkbenchPart;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map.Entry;

import srmprocess.Communication;
import srmprocess.DBConnection;
import srmprocess.Process;;

public class CoupledChanges extends ViewPart {
	public static final String ID = "SrmPlugIn.CoupledChanges";

	@SuppressWarnings("unused")
	
	private TableViewer viewer;
	DBConnection dataBaseCon = DBConnection.getDBConnection();
	Communication communication = new Communication();
	Process process = new Process();
	// public static int CoupledChangesviewvalue=1;
	// public static int docuviewvalue=0;
	// R�ckkuplung commitid icinde kullanmis oldumuz klassifikation y�nteminden
	// sonra commitid enfazla auftreten yapan group belirlemek iicn kullanildi
	private ArrayList<Entry<?, Integer>> commitsort;

	private IWorkbenchWindow window;
	private IWorkbenchPage activePage;

	private IProject theProject;
	private IResource theResource;

	public String workspaceName;
	public String projectName;
	public String fileName;
	
	private String endOfTransmissionString = "-End of Transmission-";

	// the listener we register with the selection service
	private ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			// we ignore our own selections
			if (sourcepart != CoupledChanges.this) {
				determineThePathOfTheSelectedFileAndSendIt(sourcepart, selection);
			}
		}
	};

	
	/*
	 * Calculate the path of the selected File.
	 */
	public void determineThePathOfTheSelectedFileAndSendIt(IWorkbenchPart sourcepart, ISelection selection) {

		if (selection instanceof IStructuredSelection) {
			// Here the path of the selected File is calculated
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			activePage = window.getActivePage();
			ISelection selection1 = activePage.getSelection();

			if (selection1 instanceof ITreeSelection) {
				TreeSelection treeSelection = (TreeSelection) selection1;
				if (treeSelection.getPaths().length != 0) {

					TreePath[] treePaths = treeSelection.getPaths();
					TreePath treePath = treePaths[0];
					Object firstSegmentObj = treePath.getFirstSegment();
					theProject = (IProject) ((IAdaptable) firstSegmentObj).getAdapter(IProject.class);
					Object lastSegmentObj = treePath.getLastSegment();
					theResource = (IResource) ((IAdaptable) lastSegmentObj).getAdapter(IResource.class);
					// theFile = (IFile) ((IAdaptable)
					// lastSegmentObj).getAdapter(IFile.class);
					workspaceName = theResource.getWorkspace().getRoot().getLocation().toOSString();
					projectName = theProject.getName();
//					fileName = theResource.getFullPath().toOSString();
//					fileName = fileName.replace("\\", "/").substring(1);
					fileName = theResource.getFullPath().removeFirstSegments(1).toOSString();
					fileName = fileName.replace("\\", "/");

					// Cluster- und Klassifikationsanalyse
					IPerspectiveDescriptor perspective = activePage.getPerspective();
					process.Run(fileName, perspective.getId());
					// Ergebnisse der Cluster- und Klassifikationsanalyse werden
					// zum Coupled Changes View und Commit Changes View gesendet.
					CommunicationEvent(perspective.getId());
				}
			}
		}
	}

	/*
	 * Ergebnisse der Cluster- und Klassifikationsanalyse werden zum Coupled
	 * Changes View und Commit Changes View gesendet.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void CommunicationEvent(String perspective) {

		// ----------Zuerst werden Control Events zu allen Views gesendet.
		Communication.control = true;
		// Hier werden Control Event zur Coupled Changes View gesendet.
		communication.ViewCommunication("file", communication.view("Control"), "viewcommunicationfile/syncEvent");
		// Hier werden Control Event zum Commit Information Tab des Message
		// Views gesendet.
		communication.ViewCommunication("commitdata", communication.view("Control"),
				"viewcommunicationcommitdata/syncEvent");
		// Hier werden Control Event zum Issue Information Tab des Message Views
		// gesendet.
		communication.ViewCommunication("IssueId", communication.view("Control"), "viewcommunicationIssueId/syncEvent");
		communication.ViewCommunication("Issuedescription", communication.view("Control"),
				"viewcommunicationIssuedescription/syncEvent");
		communication.ViewCommunication("Issuetype", communication.view("Control"),
				"viewcommunicationIssuetype/syncEvent");
		// Hier werden Control Event zum Path Information Tab des Message Views
		// gesendet.
		communication.ViewCommunication("Pathdescription", communication.view(communication.view("Control")),
				"viewcommunicationdocuPathdescription/syncEvent");
		communication.ViewCommunication("Path", communication.view(communication.view("Control")),
				"viewcommunicationdocuPath/syncEvent");
		// Hier werden Control Event zum Ranking Information Tab des Commit
		// Changes Views gesendet.
		communication.ViewCommunication("serachcommitid", communication.view("Control"),
				"viewcommunicationserachcommitid/syncEvent");
		// Hier werden Control Event zum Commit Information Tab des Commit
		// Changes Views gesendet.
		communication.ViewCommunication("CommitRanking", communication.view("Control"),
				"viewcommunicationcommitranking/syncEvent");
		communication.ViewCommunication("CommitmessageRanking", communication.view("Control"),
				"viewcommunicationcommitmessageranking/syncEvent");

		/*
		 * 
		 * Falls in der outputtable keine selektierter Pfad vorhanden ist, werden
		 * Fehler Event zur Coupled Chages View und Ranking Information Tab des
		 * Commit Changes View gesendet.
		 * 
		 */
		if (DBConnection.sqlprocedureInput.size() == 0) {
			Communication.control = false;
			communication.ViewCommunication("file",
					communication.view("The selected File does not exist in the Outputtable"),
					"viewcommunicationfile/syncEvent");
			communication.ViewCommunication("serachcommitid",
					communication.view("The selected File does not exist in the Outputtable"),
					"viewcommunicationserachcommitid/syncEvent");
		}

		else {
			// Bevor Cluster Ergebnisse zum Coupled Changes gesendet
			// werden zuerst In der Datenbank eine neue "Clusteroutputtable"
			// Outputtable erzeuegt.
			dataBaseCon.CreateOutputTable("Clusteroutputtable");
			// Alle Cluster/File Gruppe Iterativ Coupled Changes View gesendet
			for (int s = 0; s < Process.ClusterErgebnis.size(); s++) {
				Communication.control = false;
				int index = 0;
				// Alle Items/Files, die in einer File Gruppe sich befindet,
				// werden iterativ gesendet.
				for (int x = Process.ClusterErgebnis.get(s).size() - 1; x > 1
						+ Integer.parseInt(Process.ClusterErgebnis.get(s).get(0)); x--) {
					if (!Process.ClusterErgebnis.get(s).get(x).equals("null")) {
						communication
								.ViewCommunication("file",
										communication.view(String.valueOf(s + 1) + "." + index + " "
												+ Process.ClusterErgebnis.get(s).get(x)),
								"viewcommunicationfile/syncEvent");
						index++;
					}
				}
				// Nachdem alle Files gesendet wurden, wird eine leere Zeile in
				// Coupled Changes View hinzugefuegt.
				communication.ViewCommunication("file", communication.view(" "), "viewcommunicationfile/syncEvent");
				// In Coupled Changes View angezeigte Cluster/File Gruppe werden
				// in Outputtable gespeichert.
				dataBaseCon.WriteIntoOutputTable("Clusteroutputtable", Process.ClusterErgebnis.get(s));

			}
			
			//Nachdem alle Cluster an Coupled Changes & Word Cloud View gesendet wurden
			// wird jetzt die Wordcloud erzeugt.
			// WordCloud.createWordCloud();
			
			if (perspective.equals("SrmPlugIn.perspective2")) {
				// Klassifikationsergenisse werden zuerst sortiert.
				commitsort = process.sortValue();
				for (int i = 0; i < commitsort.size(); i++) {
					Communication.control = false;
					// Falls Commit-ID Gruppe in den Transaktionen mindestens
					// zweimal aufgetreten wuerden,
					// werden zum Ranking Information Tab des Commit Changes Views
					// gesendet.
					if (commitsort.get(i).getValue() >= 2) {
						communication.ViewCommunication("serachcommitid",
								communication.view(commitsort.get(i).getValue().toString() + " "
										+ commitsort.get(i).getKey().toString()),
								"viewcommunicationserachcommitid/syncEvent");
						// gesendete Commit-Id Gruppe werden noch in einem separaten
						// Array gespeichert.
						// Falls Benutzer im Ranking Information TAb des Commit
						// Changes View eine Gruppe auswaehlen wuerde,
						// wird es in diesem Array gesucht und weiter bearbeitet.
						Process.KlassifikationErgebnis.add((java.util.List<String>) commitsort.get(i).getKey());
					}
				} 
			}				
		}

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(final Composite parent) {

		// hier werden Listener registert.
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);

		final ListViewer listViewer = new ListViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		final List list = listViewer.getList();

		// Hier werden gesendete Events empfangen, entkapselt und in der zugehoerigen
		// View angezeigt.
		BundleContext ctx = FrameworkUtil.getBundle(CoupledChanges.class).getBundleContext();
		
		EventHandler handler = event -> {

			if (Communication.control) {
				listViewer.getList().removeAll();
			} else {

				if (parent.getDisplay().getThread() == Thread.currentThread()) {

					if(!event.getProperty("file").equals(endOfTransmissionString)){
					listViewer.add(event.getProperty("file"));
					}
				} else {
					parent.getDisplay().syncExec( () -> listViewer.add(event.getProperty("file")));
				}
			}
		};

		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, "viewcommunicationfile/*");
		ctx.registerService(EventHandler.class, handler, properties);

		// Falls ein File aus der File-Gruppe ausgewaehlt wird, werden zwei
		// Werte ermittelt.
		list.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				// Falls in der ausgewaehlten Zeile ein Item vorhanden ist,..
				if (!list.getItem(list.getSelectionIndex()).equals(" ")) {

					if (DBConnection.sqlprocedureInput.size() != 0) {

						// find Clusternummer im String
						String s = list.getItem(list.getSelectionIndex());
						s = s.substring(0, s.indexOf("."));
						int a = Integer.parseInt(s) - 1;

						Communication.control = true;
						// Hier werden Control Event zum Commit Information Tab
						// des Message Views gesendet.
						communication.ViewCommunication("commitdata", communication.view("Control"),
								"viewcommunicationcommitdata/syncEvent");
						// Hier werden Control Event zum Issue Information Tab
						// des Message Views gesendet.
						communication.ViewCommunication("IssueId", communication.view("Control"),
								"viewcommunicationIssueId/syncEvent");
						communication.ViewCommunication("Issuedescription", communication.view("Control"),
								"viewcommunicationIssuedescription/syncEvent");
						communication.ViewCommunication("Issuetype", communication.view("Control"),
								"viewcommunicationIssuetype/syncEvent");
						// Hier werden Control Event zum Path Information Tab
						// des Message Views gesendet.
						communication.ViewCommunication("Pathdescription",
								communication.view(communication.view("Control")),
								"viewcommunicationdocuPathdescription/syncEvent");
						communication.ViewCommunication("Path", communication.view(communication.view("Control")),
								"viewcommunicationdocuPath/syncEvent");

						Communication.control = false;
						// Jeder Pfad der ausgewaehlten File Gruppe und gekoppelte
						// Path Description
						// werden zum Path Information Tab des Message View
						// gesendet.
						for (int w = 1 + FPGrowthAlgorithmus.maxSupport
								+ Integer.parseInt(
										Process.ClusterErgebnis.get(a).get(1)); w > FPGrowthAlgorithmus.maxSupport
												+ 1; w--) {

							String temp = Process.ClusterErgebnis.get(a).get(w).substring(0,
									1 + Process.ClusterErgebnis.get(a).get(w).lastIndexOf("/"));

							// Falls in der Path Datenabank Path der
							// selektierten File nicht existiert, werden Fehler
							// Event gesendet.
							if (dataBaseCon.ReadDocuTable(temp).size() != 0) {

								communication.ViewCommunication("Pathdescription",
										communication.view(dataBaseCon.ReadDocuTable(temp).get(1)),
										"viewcommunicationdocuPathdescription/syncEvent");
								communication.ViewCommunication("Path",
										communication.view(dataBaseCon.ReadDocuTable(temp).get(0)),
										"viewcommunicationdocuPath/syncEvent");
							}
							// hier werden Path und gekoppelte Path Description
							// zum Path Information Tab des Message View
							// gesendet.
							else {
								communication.ViewCommunication("Pathdescription", communication.view("Leer "),
										"viewcommunicationdocuPathdescription/syncEvent");
								communication.ViewCommunication("Path", communication.view("Fehler"),
										"viewcommunicationdocuPath/syncEvent");

							}

						}
						
						ArrayList<String[]> commitdata = new ArrayList<>();
						
						for (int i = 2; i < Integer.parseInt(Process.ClusterErgebnis.get(a).get(0)) + 2; i++) {
							commitdata.add(new String[] {
									Process.ClusterErgebnis.get(a).get(i),
									dataBaseCon.ReadCommitMessage(Process.ClusterErgebnis.get(a).get(i))
							});
						}
						
						communication.ViewCommunication("commitdata",
								commitdata,
								"viewcommunicationcommitdata/syncEvent");
						
						// Falls selektierte Zeile kein Item vorhanden ist,...
					}
				} else {

					Communication.control = true;
					// Hier werden Control Event zum Commit Information Tab des
					// Message Views gesendet.
					communication.ViewCommunication("commitdata", communication.view("Control"),
							"viewcommunicationcommitdata/syncEvent");
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
					communication.ViewCommunication("Pathdescription",
							communication.view(communication.view("Control")),
							"viewcommunicationdocuPathdescription/syncEvent");
					communication.ViewCommunication("Path", communication.view(communication.view("Control")),
							"viewcommunicationdocuPath/syncEvent");

				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		// viewer.getControl().setFocus();
	}

	// registerte listener werden gel�scht.
	public void dispose() {
		// important: We need do unregister our listener when the view is
		// disposed
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);

		super.dispose();
	}
}
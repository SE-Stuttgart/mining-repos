package srmplugin;

import srmplugin.wordcloud.WordCloudLabelProvider;
import srmplugin.wordcloud.DateChecker;
import srmplugin.wordcloud.FilePathToClusterMap;
import srmplugin.wordcloud.MyWord;
import srmplugin.wordcloud.SingleSelectionTagCloudViewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.gef.cloudio.internal.ui.TagCloud;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;

import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import srmprocess.Communication;
import srmprocess.DBConnection;
import srmprocess.Process;

@SuppressWarnings("restriction") // This is for suppressing the discouraged
									// access warnings for Cloudio which is not
									// yet officially released.
public class WordCloud extends ViewPart {
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "SrmPlugIn.WordCloud";

	/**
	 * Selection listener for getting the filename / path of Package Explorer
	 * selections
	 */
	private ISelectionListener selListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			// we ignore our own selections
			if (sourcepart != WordCloud.this) {
				determineThePathOfTheSelectedFileAndSendIt(sourcepart, selection);
			}
		}
	};

	Process process = new Process();
	DBConnection dataBaseCon = DBConnection.getDBConnection();
	Communication communication = new Communication();
	private ArrayList<Entry<?, Integer>> commitsort;

	private IWorkbenchWindow window;
	private IWorkbenchPage activePage;

	private IProject theProject;
	private IResource theResource;

	public String workspaceName;
	public String projectName;
	public String fileName;

	private String endOfTransmissionString = "-End of Transmission-";

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

					workspaceName = theResource.getWorkspace().getRoot().getLocation().toOSString();
					projectName = theProject.getName();
					fileName = theResource.getFullPath().removeFirstSegments(1).toOSString();
					fileName = fileName.replace("\\", "/");

					// Cluster- und Klassifikationsanalyse
					IPerspectiveDescriptor perspective = activePage.getPerspective();
					process.Run(fileName, perspective.getId());
					// Ergebnisse der Cluster- und Klassifikationsanalyse werden
					// zum Coupled Changes View und Commit Changes View
					// gesendet.
					CommunicationEvent(perspective.getId());
				}
			}
		}
	}

	/*
	 * Ergebnisse der Cluster- und Klassifikationsanalyse werden zum Word Cloud
	 * View und Commit Changes View gesendet.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void CommunicationEvent(String perspective) {

		// ----------Zuerst werden Control Events zu allen Views gesendet.
		Communication.control = true;
		// Hier werden Control Event zur Coupled Changes View und zur Word Cloud
		// View gesendet.
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
		 * Falls in der outputtable keine selektierter Pfad vorhanden ist,
		 * werden Fehler Event zur Coupled Changes View und Ranking Information
		 * Tab des Commit Changes View gesendet.
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
			// Bevor Cluster Ergebnisse zur WordCloud View gesendet
			// werden zuerst In der Datenbank eine neue "Clusteroutputtable"
			// Outputtable erzeuegt.
			dataBaseCon.CreateOutputTable("Clusteroutputtable");
			// Alle Cluster/File Gruppe Iterativ Word Cloud View gesendet
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
				// Nachdem alle Files eines Clusters gesendet wurden, wird eine
				// leere Zeile in
				// die Coupled Changes View eingefuegt.
				communication.ViewCommunication("file", communication.view(" "), "viewcommunicationfile/syncEvent");
				// In Word CLoud View angezeigte Cluster/File Gruppe werden
				// in Outputtable gespeichert.
				dataBaseCon.WriteIntoOutputTable("Clusteroutputtable", Process.ClusterErgebnis.get(s));

			}

			// Nachdem alle Cluster an Word Cloud View gesendet wurden
			// wird jetzt die ein endOfTransmissionString gesendet der
			// signalisiert, dass die WordCloud aktualisiert werden muss.
			communication.ViewCommunication("file", communication.view(endOfTransmissionString),
					"viewcommunicationfile/syncEvent");

			if (perspective.equals("SrmPlugIn.perspective2")) {
				// Klassifikationsergenisse werden zuerst sortiert.
				commitsort = process.sortValue();
				for (int i = 0; i < commitsort.size(); i++) {
					Communication.control = false;
					// Falls Commit-ID Gruppe in den Transaktionen mindestens
					// zweimal aufgetreten wuerden,
					// werden zum Ranking Information Tab des Commit Changes
					// Views
					// gesendet.
					if (commitsort.get(i).getValue() >= 2) {
						communication.ViewCommunication("serachcommitid",
								communication.view(commitsort.get(i).getValue().toString() + " "
										+ commitsort.get(i).getKey().toString()),
								"viewcommunicationserachcommitid/syncEvent");
						// gesendete Commit-Id Gruppe werden noch in einem
						// separaten
						// Array gespeichert.
						// Falls Benutzer im Ranking Information TAb des Commit
						// Changes View eine Gruppe auswaehlen wuerde,
						// wird es in diesem Array gesucht und weiter
						// bearbeitet.
						Process.KlassifikationErgebnis.add((java.util.List<String>) commitsort.get(i).getKey());
					}
				}
			}
		}

	}

	private SingleSelectionTagCloudViewer viewer;

	private TagCloud cloud;
	private Action action1;
	private Action action2;
	WordCloudLabelProvider labelProvider;

	/**
	 * The constructor.
	 */
	public WordCloud() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		parent.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				System.out.println("RESIZE");
				cloud.zoomFit();
			}
		});

		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(selListener);
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
		IWorkbenchPart active = page.getActiveEditor();

		// This part Listener detects the active editor and starts cluster
		// analysis for this file.
		IPartListener2 pl = new IPartListener2() {

			public void partActivated(IWorkbenchPartReference ref) {
				if (ref instanceof IEditorReference) {
					System.out.println(ref.getTitleToolTip());

					window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					activePage = window.getActivePage();

					// Cluster- und Klassifikationsanalyse
					IPerspectiveDescriptor perspective = activePage.getPerspective();
					if (ref.getTitleToolTip() != null) {
						String filepath = ref.getTitleToolTip();
						if (filepath.contains("/")) {
							int index = filepath.indexOf("/");							
							String path = filepath.substring(index + 1);
							process.Run(path, perspective.getId());
							// Ergebnisse der Cluster- und
							// Klassifikationsanalyse
							// werden
							// zum Coupled Changes View und Commit Changes View
							// gesendet.
							CommunicationEvent(perspective.getId());
						}
					}
				}

			}

			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partOpened(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partHidden(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partVisible(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

		};
		page.addPartListener(pl);

		BundleContext ctx = FrameworkUtil.getBundle(WordCloud.class).getBundleContext();

		cloud = new TagCloud(parent, SWT.HORIZONTAL | SWT.VERTICAL);
		// viewer = new TagCloudViewer(cloud);
		viewer = new SingleSelectionTagCloudViewer(cloud);

		// Init Word Cloud Data Structures

		labelProvider = new WordCloudLabelProvider(cloud.getFont());

		FilePathToClusterMap filePathToClusterMap = new FilePathToClusterMap(labelProvider);

		String clusterPath1 = "Select a file in the Project";
		String clusterPath2 = "Wordsize represents level of coupling";
		String clusterPath3 = "Click on the Wordcloud to view corresponding commit-information";

		filePathToClusterMap.putInClusterHashMap(clusterPath1);
		filePathToClusterMap.putInClusterHashMap(clusterPath2);
		filePathToClusterMap.putInClusterHashMap(clusterPath3);
		List<MyWord> wordList = filePathToClusterMap.getWordList();

		// When the selection of file in Project explorer gets changed, this
		// gets executed
		EventHandler handler = event -> {
			if (Communication.control) {
				// Removes all files from the WordCloud word supply.
				filePathToClusterMap.getMap().clear();
				wordList.clear();
			} else {
				if (parent.getDisplay().getThread() == Thread.currentThread()) {
					// insert and process all incoming filepaths
					String currentClusterPath = (String) event.getProperty("file");

					if (currentClusterPath.equals(endOfTransmissionString)) {
						// TODO Here one can start to implement a sorting by
						// last file edit date to get weights for color shading
						// the words.
						// DateChecker dateChecker = new DateChecker();
						// dateChecker.createDateTable(wordList);
						viewer.setInput(wordList);
					} else {
						filePathToClusterMap.putInClusterHashMap(currentClusterPath);
					}

				} else {

				}
			}
		};

		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, "viewcommunicationfile/*");
		ctx.registerService(EventHandler.class, handler, properties);

		viewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void dispose() {

			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				List<?> list = (List<?>) newInput;
				if (list == null || list.size() == 0)
					return;

			}

			@Override
			public Object[] getElements(Object inputElement) {
				return ((List<?>) inputElement).toArray();
			}
		});

		viewer.setLabelProvider(labelProvider);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			// TODO

			// If a word of the WordCloud is selected, get the path from the
			// MyWord tooltip.
			// Make a Look up in the ClusterHashMap for this filepath.
			// Send all clusters this file is part of to the message View.
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = ((IStructuredSelection) event.getSelection());

				if (!selection.isEmpty()) {

					String path = null;
					printSelection(selection);
					if (isWord(selection)) {
						path = getPath(selection);
					} else {
						System.err.println("selection can't be casted to MyWord, abort");
						return;
					}
					if (DBConnection.getDBConnection() != null)
						if (DBConnection.sqlprocedureInput != null) {
							if (DBConnection.sqlprocedureInput.size() != 0) {

								List<String> clusterList;
								if (!path.equals(null)) {
									// find all Cluster this file is part of
									clusterList = getClustersOfThisPath(filePathToClusterMap, path);
								} else {
									System.err.println("path of file is null, abort");
									return;
								}

								List<Integer> listOfClustersInt;

								if (!clusterList.isEmpty()) {
									// get only the leading cluster numbers
									listOfClustersInt = getLeadingClusterNumberAsIntList(clusterList);
								} else {
									System.err.println("clusterList is empty, abort");
									return;
								}

								// Send Control Event to inform other views of
								// incoming
								// message
								sendControlEvent();

								// Send Information to Path Information Tab
								// TODO

								// Send Information to Commit Information Tab of
								// Message
								// View

								// here the commit Id and Commit messages are
								// being sent to the Commit Information tab of
								// Message
								// View.

								// TODO remove duplicates, sort by #of same ID.

								// Init data structures for sorting, eliminating
								// duplicates and sending Messages to
								// MessageView
								HashMap<MyWord, Integer> commitCountHashMap = new HashMap<MyWord, Integer>();
								ArrayList<String[]> commitdata = new ArrayList<>();

								// Iterate over all clusternumbers
								for (Iterator<Integer> iter = listOfClustersInt.iterator(); iter.hasNext();) {
									// Current Clusternumber
									Integer clusterNum = iter.next();

									// Process all commit messages of the
									// current
									// Clusternumber.
									for (int i = 2; i < Integer.parseInt(Process.ClusterErgebnis.get(clusterNum).get(0))
											+ 2; i++) {

										// Put in HashMap and count occurences.
										// Will
										// be
										// used later to eliminate duplicates
										// and
										// sort
										// by relevance.

										// Using MyWord class because it is
										// immutable. A
										// string array would not work because
										// of
										// mutability of arrays.
										// This word contains: commitID,
										// CommitMessage,
										// occurence value 1
										String id = Process.ClusterErgebnis.get(clusterNum).get(i);
										String message = dataBaseCon
												.ReadCommitMessage(Process.ClusterErgebnis.get(clusterNum).get(i));
										MyWord commitIdAndMessage = new MyWord(id, message, 1);

										if (commitCountHashMap.containsKey(commitIdAndMessage)) {
											// If already in Map, increment
											// occurence
											// value.
											int occ = commitCountHashMap.get(commitIdAndMessage);
											commitCountHashMap.put(commitIdAndMessage, occ + 1);
										} else {
											// If not already in Map, put it in
											// with
											// occurence value == 1.
											commitCountHashMap.put(commitIdAndMessage, 1);
										}
									}
								}

								// HashMap has no duplicates. Still need to sort
								// by
								// value.

								// Sort
								Object[] objectArray = commitCountHashMap.entrySet().toArray();

								Arrays.sort(objectArray, new Comparator() {

									@Override
									public int compare(Object o1, Object o2) {
										return ((Map.Entry<MyWord, Integer>) o2).getValue()
												.compareTo(((Map.Entry<MyWord, Integer>) o1).getValue());
									}

								});

								// Send Data to Message View ordered by number
								// of
								// occunrences descending.
								for (Object o : objectArray) {

									String id = ((MyWord) ((Map.Entry<MyWord, Integer>) o).getKey()).getPath();
									String message = ((MyWord) ((Map.Entry<MyWord, Integer>) o).getKey()).getWord();
									System.out.println(
											id + " : " + message + " : " + ((Map.Entry<MyWord, Integer>) o).getValue());

									commitdata.add(new String[] { id, message });

								}
								communication.ViewCommunication("commitdata", commitdata,
										"viewcommunicationcommitdata/syncEvent");

							}
						} else {
							System.err.println("Please Follow the SRM Plug-in Wizard.");
							JOptionPane.showMessageDialog(new JFrame(),
									"Please open a source file in the Project explorer \n "
											+ "and follow the SRM Plug-in Wizard. \n");
							return;
						}
					// Implement WordCloudView -> MessageView dataflow here!

				}
			}

			/**
			 * Appends all Commit Messages belonging to a Cluster specified by
			 * clusterNum to the String List commitdata.
			 * 
			 * @param commitdata
			 * @param clusterNum
			 */
			private void appendMessagesOfCluster(ArrayList<String[]> commitdata, Integer clusterNum) {
				for (int i = 2; i < Integer.parseInt(Process.ClusterErgebnis.get(clusterNum).get(0)) + 2; i++) {
					commitdata.add(new String[] { Process.ClusterErgebnis.get(clusterNum).get(i),
							dataBaseCon.ReadCommitMessage(Process.ClusterErgebnis.get(clusterNum).get(i)) });
				}
			}

			/**
			 * Sends Control Event to Commit/Issue/Path Information Tabs so they
			 * can clear their data.
			 */
			private void sendControlEvent() {
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
				communication.ViewCommunication("Pathdescription", communication.view(communication.view("Control")),
						"viewcommunicationdocuPathdescription/syncEvent");
				communication.ViewCommunication("Path", communication.view(communication.view("Control")),
						"viewcommunicationdocuPath/syncEvent");

				Communication.control = false;
			}

			private List<String> getClustersOfThisPath(FilePathToClusterMap filePathToClusterMap, String path) {
				List<String> clusterList = new ArrayList<String>();
				try {

					HashMap<String, List<String>> clusterHashMap = filePathToClusterMap.getMap();
					if (clusterHashMap.containsKey(path)) {
						clusterList = clusterHashMap.get(path);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return clusterList;
			}

			private List<Integer> getLeadingClusterNumberAsIntList(List<String> clusterList) {
				List<Integer> listOfClustersInt = new ArrayList<Integer>();
				for (Iterator<String> iter = clusterList.iterator(); iter.hasNext();) {
					String s = iter.next();
					s = s.substring(0, s.indexOf("."));
					int a = Integer.parseInt(s) - 1;
					listOfClustersInt.add(a);
				}
				return listOfClustersInt;
			}
		});

		cloud.setBounds(0, 0, parent.getBounds().width, parent.getBounds().height);
		cloud.zoomFit();

		viewer.getCloud().setMaxFontSize(100);
		viewer.getCloud().setMinFontSize(15);

		viewer.setInput(wordList);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "cloudioViewtest.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	protected String getPath(IStructuredSelection selection) {
		return ((MyWord) selection.getFirstElement()).getPath();
	}

	protected boolean isWord(IStructuredSelection selection) {
		if (selection.getFirstElement() instanceof MyWord) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * DEBUG Console Output Method to print selection contents
	 * 
	 * @param selection
	 */
	protected void printSelection(IStructuredSelection selection) {
		for (Iterator<Object> iter = selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			System.out.println("Selected Word: " + ((MyWord) obj).getWord() + ", Path: " + ((MyWord) obj).getPath()
					+ ", Count: " + ((MyWord) obj).getCount());

		}

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				WordCloud.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(), "Sample View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}

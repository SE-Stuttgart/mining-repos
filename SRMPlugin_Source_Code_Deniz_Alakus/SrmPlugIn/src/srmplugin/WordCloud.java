package srmplugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;


import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import FPGA.FPGrowthAlgorithmus;
import srmplugin.wordcloud.WordPlacer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JLabel;
import javax.swing.text.BadLocationException;

import srmprocess.Communication;
import srmprocess.DBConnection;
import srmprocess.Process;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.IMarkSelection;

public class WordCloud extends ViewPart {
	public static final String ID = "SrmPlugIn.WordCloud";
	
	@SuppressWarnings("unused")
	
	
	static List<String> filenames = new ArrayList<String>();
	
	DBConnection dataBaseCon = DBConnection.getDBConnection();
	Communication communication = new Communication();
	Process process = new Process();
	
	//parent Composite to hold the all Wordcloud Labels.
	private static Composite par;
	
	private static List<Label> labelList = new ArrayList<Label>();
	private ArrayList<Entry<?, Integer>> commitsort;
	
	private IWorkbenchWindow window;
	private IWorkbenchPage activePage;

	private IProject theProject;
	private IResource theResource;

	public String workspaceName;
	public String projectName;
	public String fileName;
	
	public Shell shell;
	
	
	
	// the listener we register with the selection service
	private ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			// we ignore our own selections
			if (sourcepart != WordCloud.this) {
				filenames.clear();
				
				//determine path of selected file in ProjectExplorer
				//determineThePathOfTheSelectedFileAndSendIt(sourcepart, selection);
				
				try {
					showSelection(sourcepart, selection);
				} catch (org.eclipse.jface.text.BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	/**
	 * Shows the given selection in this view.
	 * 
	 */
	public void showSelection(IWorkbenchPart sourcepart, ISelection selection) throws org.eclipse.jface.text.BadLocationException {
		setContentDescription(sourcepart.getTitle() + " (" + selection.getClass().getName() + ")");
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			showItems(ss.toArray());
		}
		if (selection instanceof ITextSelection) {
			ITextSelection ts  = (ITextSelection) selection;
			showText(ts.getText());
		}
		if (selection instanceof IMarkSelection) {
			IMarkSelection ms = (IMarkSelection) selection;
		}
			
		
	}
	
	private void showItems(Object[] items) {
		
	}
	
	private void showText(String text) {
		
	}
	
	

	/*
	 * Calculate the path of the selected file in the Package explorer
	 * Run Cluster analysis and send results to WordCloudView.
	 */
	private void determineThePathOfTheSelectedFileAndSendIt(IWorkbenchPart sourcepart, ISelection selection) {
		
		if(selection instanceof IStructuredSelection) {
			// Calculate the path of the selected file
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			activePage = window.getActivePage();
			ISelection selection1 = activePage.getSelection();
			
			if(selection1 instanceof ITreeSelection) {
				TreeSelection treeSelection = (TreeSelection) selection1;
				if(treeSelection.getPaths().length != 0) {
					
					TreePath[] treePaths = treeSelection.getPaths();
					TreePath treePath = treePaths[0];
					Object firstSegmentObj =  treePath.getFirstSegment();
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
					// zur Word Cloud View und Commit Changes View gesendet.
					System.out.println(perspective.getId());
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
		
		
	}

	

	public void OnStart() {

		System.out.println("Created Word Cloud Table");
	}

	/**
	 * Takes filepath, extracts filename, appends filename to filenamelist
	 * 
	 * @param filepath
	 */
	private void addFilenameToList(String filepath) {
		if (((filepath) != (null)) && (filepath).length() > 0 && !(filepath).equals(" ") && filepath.contains("/")
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
		

	}


	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * after the WordCloud View has revieved all Cluster Information it can now
	 * start to manipulate the data to create a WordCloud.
	 */
	public static void createWordCloud() {
		// TODO Auto-generated method stub

		HashMap<String, Integer> countedFiles = countFileOccurences(filenames);
		Map<String, Integer> sortedFiles = sortByValue(countedFiles);
		
		
		
		if(labelList.size()>0) {
			clearLabels();
		}
		createWordTags(sortedFiles);
		
		par.layout();
		/*
		WordPlacer wp = new WordPlacer(sortedFiles);
		wp.placeWords();
		 */
		__printHashMap(sortedFiles);
		__printEveryWordInHashMap(sortedFiles);

		/**
		 * Hashtable<String, Integer> numbers = new Hashtable<String,
		 * Integer>(); numbers.put("one", 1); numbers.put("two", 2);
		 * numbers.put("three", 3);
		 * 
		 * To retrieve a number, use the following code:
		 * 
		 * Integer n = numbers.get("two"); if (n != null) {
		 * System.out.println("two = " + n); }
		 **/

	}
	
	
	

	private static void clearLabels() {
		for(Label l : labelList) {
			l.dispose();
		}
		labelList.clear();
		
	}

	private static void createWordTags(Map<String, Integer> sortedFiles) {
		
		
		
		
		for(Map.Entry<String,Integer> entry : sortedFiles.entrySet()){
			
			
			Label l = new Label(par,SWT.LEAD);
			l.setText(entry.getKey());
			
			
			FontData[] fD = l.getFont().getFontData();
			fD[0].setHeight(entry.getValue());
			l.setFont( new Font(l.getDisplay(),fD[0]));
			
			
			
			l.addMouseListener(new MouseAdapter() {
				@Override
				   public void mouseUp(MouseEvent event) {
				      super.mouseUp(event);

				      if (event.getSource() instanceof Label) {
				         Label label = (Label)event.getSource();

				         System.out.println("Label was clicked: " + label.getText());
				      }
				   }
			});
			
			
			
			labelList.add(l);
			
			
		}
		
	}

	/**
	 * Counts the number of occurences of a key in a HashMap
	 * 
	 * @param filesToCount
	 * @return countedFiles
	 */
	private static HashMap<String, Integer> countFileOccurences(List<String> filesToCount) {
		HashMap<String, Integer> countedFiles = new HashMap<String, Integer>();

		// Iterate over the unsorted filename list.
		for (String file : filesToCount) {

			int count = 0;
			// if the file is already in the Hashtable increment the count.
			if (countedFiles.get(file) != null) {
				count = countedFiles.get(file);
				countedFiles.put(file, count + 1);
			}
			// if the file is not in the Hashtable insert it.
			else {
				countedFiles.put(file, 1);
			}

		}
		return countedFiles;
	}

	/**
	 * Takes a Map and sorts by Value descending.
	 * 
	 * @param unsorted
	 *            map
	 * @return sorted map
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	/**
	 * DEBUG-method to view content of a HashMap.
	 * 
	 * @param sortedFiles
	 */
	public static void __printHashMap(Map<String, Integer> sortedFiles) {
		Set<String> keys = sortedFiles.keySet();
		for (String key : keys) {
			System.out.println("Key: " + key + " || Value: " + sortedFiles.get(key));
		}

	}

	/**
	 * DEBUG-method only for Zwischenvortrag.
	 * 
	 * @param sortedFiles
	 */
	public static void __printEveryWordInHashMap(Map<String, Integer> sortedFiles) {
		Set<String> keys = sortedFiles.keySet();
		for (String key : keys) {
			int value = sortedFiles.get(key);
			for (int i = 0; i < value; i++) {
				System.out.println(key);
			}
		}
	}

}

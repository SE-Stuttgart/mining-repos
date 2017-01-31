package srmplugin;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

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
import java.util.stream.Collectors;

import javax.swing.JLabel;

import srmprocess.Communication;
import srmprocess.DBConnection;

@SuppressWarnings("unused")
public class WordCloud extends ViewPart {
	public static final String ID = "SrmPlugIn.WordCloud";
	static List<String> filenames = new ArrayList<String>();
	private Label label;
	private Shell shell;
	
	
	

	
	

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
		// registriere Listener
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);

		BundleContext ctx = FrameworkUtil.getBundle(WordCloud.class).getBundleContext();

		label = new Label(parent , 0);
		label.setText("HelloWorld");
		label.setAlignment(SWT.CENTER);
		
		label.addMouseListener(new MouseAdapter() {
			@Override
			   public void mouseUp(MouseEvent event) {
			      super.mouseUp(event);

			      if (event.getSource() instanceof Label) {
			         Label label = (Label)event.getSource();

			         System.out.println("Label was clicked: " + label.getText());
			      }
			   }
		});
		
		shell = new Shell();
		
	    
		

		EventHandler handler = event -> {

			if (Communication.control) {
				// TODO: Clear the panel

				System.out.println("TODO: Clear all labels!");

			} else {

				if (parent.getDisplay().getThread() == Thread.currentThread()) {

					// TODO: add clickable labels for filenames tied to
					// filepaths via
					// some ID

					// get file name of path
					// src/main/java/de/MainClass.java -> MainClass
					addFilenameToList((String) event.getProperty("file"));

				} else {
					parent.getDisplay().syncExec(() -> System.out.println("3"));
				}
			}
		};

		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, "viewcommunicationfile/*");
		ctx.registerService(EventHandler.class, handler, properties);

		OnStart();

		

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
		
		createWordTags(sortedFiles);

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

	private static void createWordTags(Map<String, Integer> sortedFiles) {
		
		JLabel[] labelList = new JLabel[sortedFiles.size()-1];
		int i=0;
		
		for(Map.Entry<String,Integer> entry : sortedFiles.entrySet()){
			labelList[i].add(new JLabel("Test"));
			/**
			panelRef.add(labelList[i]);
			panelRef.validate();
			panelRef.repaint();
			**/
			i++;
		}
		
		i=0;
		
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

package srmplugin;

//import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
//import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

//import FPGA.FPGrowthAlgorithmus;
//import srmprocess.DBConnection;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "SrmPlugIn"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	// Initiliare Object varaible
//	FPGrowthAlgorithmus fpgaAlg = new FPGrowthAlgorithmus();
//	DBConnection dataBaseConn = new DBConnection();

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		SRMSettings.minsupport = 0.1;

		/// Release the Input Table
		/// dataBaseConn.ReadInputTabletest(); //liest test inputtable
		/*dataBaseConn.ReadIssueTable();
		dataBaseConn.ReadInputTable(); // liest reale inputtable

		if (dataBaseConn.mincounter < 11) {
			MessageDialog messageDialog = new MessageDialog(new Shell(), "MessageDialog", null,
					"Es liegen weniger als 11 Einträge in der Transaktionsdatenbank vor.\n Bitte wählen Sie eine neue Inputtabelle aus!",
					MessageDialog.ERROR, new String[] { "OK" }, 1);

			if (messageDialog.open() == 1) {
				*//**
				 * minsup(x)=(e^((-a*x-b) )+c) Quelle :
				 * http://data-mining.philippe-fournier-viger.com/how-to-auto-
				 * adjust-the-minimum-support-threshold-according-to-the-data-
				 * size/
				 *//*
				fpgaAlg.runAlgorithm(Math.pow(Math.E, ((-0.4 * dataBaseConn.mincounter) - 0.2)) + 0.2);
			}
		} else {

			fpgaAlg.runAlgorithm(Math.pow(Math.E, ((-0.4 * dataBaseConn.mincounter) - 0.2)) + 0.2);
		}
		if (FPGrowthAlgorithmus.output.size() == 0) {
			MessageDialog messageDialog = new MessageDialog(new Shell(), "MessageDialog", null,
					"Frequent Itemset Analyse liefert keine Ergebnisse!\n ändern Sie den Wert des minsupp und führen Sie die Analyse erneut durch!",
					MessageDialog.ERROR, new String[] { "OK" }, 1);
			if (messageDialog.open() == 1) {
			}
		}
		dataBaseConn.CreateOutputTable("outputtable");
		dataBaseConn.WriteIntoOutputTable("outputtable");*/

		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}

package srmplugin.wizard;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import FPGA.FPGrowthAlgorithmus;
import srmplugin.SRMSettings;
import srmprocess.DBConnection;

public class Wizard extends org.eclipse.jface.wizard.Wizard {
	
	ATSRPage atsr;
	SelectCommitersPage commits;
	WizardPage currentPage;

	private FPGrowthAlgorithmus fpgaAlg = new FPGrowthAlgorithmus(); //must be member or it wont work

	@Override
	public void addPages() {
		atsr = new ATSRPage();
		addPage(atsr);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if(currentPage instanceof ATSRPage) {
			commits = new SelectCommitersPage();
			addPage(commits);
		}
		return commits;
	}

	@Override
	public boolean needsPreviousAndNextButtons() {
		return true;
	}
	
	@Override
	public boolean needsProgressMonitor() {
		return true;
	}
	
	

	@Override
	public boolean performFinish() {
		boolean finish = true;
		if(currentPage instanceof SelectCommitersPage) {
			DBConnection dataBaseConn = DBConnection.getDBConnection();
			
			if(commits.selectedCommitter.equals(">All<"))
				dataBaseConn.ReadInputTable(null);
			else
				dataBaseConn.ReadInputTable(commits.selectedCommitter);
			
			fpgaAlg.runAlgorithm(SRMSettings.minsupport);
			dataBaseConn.CreateOutputTable("outputtable");
			dataBaseConn.WriteIntoOutputTable("outputtable");
			dataBaseConn.ReadIssueTable();

			if (dataBaseConn.mincounter < 11) {
				MessageDialog messageDialog = new MessageDialog(this.getShell(), "MessageDialog", null,
						"There are less than eleven entries in the transaction table of this author.\n"
						+ "Please choose another Author!",
						MessageDialog.WARNING, new String[] { "OK" }, 1);
				
				messageDialog.open();
				finish = false;
			}
			if (FPGrowthAlgorithmus.output.size() == 0) {
				MessageDialog messageDialog = new MessageDialog(this.getShell(), "MessageDialog", null,
						"Frequent Itemset Analyse could not come up with results!\n"
						+ "Lower the Minimum Support value and run the analysis again!",
						MessageDialog.WARNING, new String[] { "OK" }, 1);

				messageDialog.open();
				finish = false;
			}
		}
		return finish;
	}

}

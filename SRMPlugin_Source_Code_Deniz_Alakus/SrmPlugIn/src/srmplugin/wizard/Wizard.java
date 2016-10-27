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
			
			if(commits.selectedCommitter.equals(">Alle<"))
				dataBaseConn.ReadInputTable(null);
			else
				dataBaseConn.ReadInputTable(commits.selectedCommitter);
			
			fpgaAlg.runAlgorithm(SRMSettings.minsupport);
			dataBaseConn.CreateOutputTable("outputtable");
			dataBaseConn.WriteIntoOutputTable("outputtable");
			dataBaseConn.ReadIssueTable();

			if (dataBaseConn.mincounter < 11) {
				MessageDialog messageDialog = new MessageDialog(this.getShell(), "MessageDialog", null,
						"Es liegen weniger als 11 Einträge in der Transaktionsdatenbank vor.\n"
						+ "Bitte wählen Sie eine neue Inputtabelle aus!",
						MessageDialog.WARNING, new String[] { "OK" }, 1);
				
				messageDialog.open();
				finish = false;
			}
			if (FPGrowthAlgorithmus.output.size() == 0) {
				MessageDialog messageDialog = new MessageDialog(this.getShell(), "MessageDialog", null,
						"Frequent Itemset Analyse liefert keine Ergebnisse!\n"
						+ "Ändern Sie den Minimum Support Wert und führen Sie die Analyse erneut durch!",
						MessageDialog.WARNING, new String[] { "OK" }, 1);

				messageDialog.open();
				finish = false;
			}
		}
		return finish;
	}

}

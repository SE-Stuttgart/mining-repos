/*******************************************************************************
 * If you modify this Program, or any covered work, by linking or
 * combining it with FPGA (or a modified version of that
 * library), containing parts covered by the terms of EPL, the licensors of this Program grant you additional permission to convey the resulting work. Corresponding source for a non-source form of such a combination shall include the source code for the parts of FPGA used as well as that of the covered work.
 * 
 * 
 * SPMF GPL Exception
 * 
 * Section7 Exception
 * 
 * As a special exception to the terms and conditions of the GNU General Public License Version 3 (the "GPL"): You are free to convey a modified version that is formed entirely from this file (for purposes of this exception, the "Program" under the GPL) and the works identified at (http://www.philippe-fournier-viger.com/spmf/index.php?link=license.php) (each an "Excepted Work"), which are conveyed to you by Philippe Fournier-Viger and licensed under one the licenses identified in the Excepted License List below, as long as:
 * 
 *    1. You obey the GPL in all respects for the Program and the modified version, except for Excepted Work which are identifiable sections of the modified version.
 * 
 * 2. All Excepted Works which are identifiable sections of the modified version, are distributed subject to the Excepted License.
 *          
 * If the above conditions are not met, then the Program may only be copied, modified, distributed or used under the terms and conditions of the GPL.
 * 
 * Excepted License List
 * 
 *     * Eclipse Public License: version 1.0
 ******************************************************************************/
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

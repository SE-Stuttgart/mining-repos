package srmplugin.wizard;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class NWizardDialog extends WizardDialog {

	public NWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
		super.setShellStyle(SWT.APPLICATION_MODAL | SWT.MODELESS | SWT.SHELL_TRIM);		
		setBlockOnOpen(false);
	}
}

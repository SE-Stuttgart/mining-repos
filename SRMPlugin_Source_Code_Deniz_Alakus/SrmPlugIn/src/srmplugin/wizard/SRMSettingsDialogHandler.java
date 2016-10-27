package srmplugin.wizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import srmplugin.SRMSettings;
import srmprocess.DBConnection;

public class SRMSettingsDialogHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		if(editorPart  != null)
		{
			IFileEditorInput input = (IFileEditorInput)editorPart.getEditorInput();
			IFile file = input.getFile();
			IProject activeProject = file.getProject();
			String activeProjectName = activeProject.getName();
			SRMSettings.project_name = activeProjectName;
			SRMSettings.project_path = activeProject.getLocation().toOSString();
			
			if(activeProject.getLocation().append(".git").toFile().exists()) {
				SRMSettings.commit_path = SRMSettings.project_path;
			} else {
				SRMSettings.commit_path = null;
			}
			
			DBConnection.getDBConnection().setDatabse(SRMSettings.project_name);
		}
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell parent = window.getShell();
		Wizard w = new Wizard();
		NWizardDialog wizardDialog = new NWizardDialog(parent, w);
		NWizardDialog.setDefaultImage(null);
		wizardDialog.setPageSize(470, 420);
		wizardDialog.setTitle("SRM Konfiguration");
		wizardDialog.addPageChangedListener(e -> {
			w.currentPage = (WizardPage) wizardDialog.getCurrentPage();
			System.out.println(w.currentPage);
		});
		wizardDialog.open();
		
		return null;
	}
	
}
package srmplugin.wizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

import srmplugin.SRMSettings;

public class SRMAnalyse extends AbstractHandler {

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
		}
		
		return null;
	}

}

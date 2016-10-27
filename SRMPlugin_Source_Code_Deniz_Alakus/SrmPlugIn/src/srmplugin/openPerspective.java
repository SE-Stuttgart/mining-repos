package srmplugin;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.handlers.HandlerUtil;

public class openPerspective extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			PlatformUI.getWorkbench().showPerspective("SrmPlugIn.perspective", HandlerUtil.getActiveWorkbenchWindow(event));
		} catch (WorkbenchException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
package srmplugin;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Preferences extends AbstractHandler {

	public static int maxSize = 200;
	public static int minSize = 20;
	public static boolean analyzeOnEditorSelect = true;
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		
		PreferencesDialog dialog = new PreferencesDialog(new Shell());
		
		dialog.open();
		
		return null;

	}

}

package srmplugin;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {

		defineActions(layout);
		defineLayout(layout);
	}

	public void defineActions(IPageLayout layout) {
		
		// Add "show views".
		layout.addShowViewShortcut("SrmPlugIn.CoupledChanges");
		layout.addShowViewShortcut("SrmPlugIn.Mesageview");
		layout.addShowViewShortcut("SrmPlugIn.WordCloud");
	}

	public void defineLayout(IPageLayout layout) {
		// Editors are placed for free.
		String editorArea = layout.getEditorArea();

		// Place navigator and outline to left of
		// editor area.
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.26, editorArea);
		left.addView(IPageLayout.ID_PROJECT_EXPLORER);

		IFolderLayout leftbottom = layout.createFolder("leftbottom", IPageLayout.BOTTOM, (float) 0.6,
				IPageLayout.ID_PROJECT_EXPLORER);
		leftbottom.addView("SrmPlugIn.CoupledChanges");
		
		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.6, editorArea);
		bottom.addView("SrmPlugIn.WordCloud");
		
		IFolderLayout bottom2 = layout.createFolder("bottom2", IPageLayout.RIGHT, (float) 0.5,
				"SrmPlugIn.WordCloud");
		bottom2.addView("SrmPlugIn.Mesageview");
	}
}

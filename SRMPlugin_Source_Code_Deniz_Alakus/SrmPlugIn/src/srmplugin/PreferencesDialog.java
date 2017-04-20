package srmplugin;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

import static org.eclipse.swt.events.SelectionListener.*;

public class PreferencesDialog extends TitleAreaDialog {

	private int minFontSize = Preferences.minSize;
	private int maxFontSize = Preferences.maxSize;

	private boolean clusterAnalysisOnEditorSelection = Preferences.analyzeOnEditorSelect;

	public PreferencesDialog(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void create() {
		super.create();
		setTitle("SRM Plug-in Preferences");
		setMessage("Choose your preferences", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		container.setLayout(layout);

		// createEditorReAnalyze(container);

		createGroups(container);

		// createMaxWords(container);
		// createMaxFontsize(container);
		//
		return area;
	}

	private void createGroups(Composite container) {
		Group grp1 = new Group(container, SWT.NULL);
		grp1.setText("Analyze on Editor Selection");
		grp1.setLayout(new FillLayout());

		Label lbAnalyzeOnEditorSelect = new Label(grp1, SWT.NONE);
		lbAnalyzeOnEditorSelect.setText("Perform cluster analysis on editor selection   ");
		lbAnalyzeOnEditorSelect.setAlignment(SWT.LEFT);

		Button check = new Button(grp1, SWT.CHECK);
		check.setAlignment(SWT.CENTER);
		check.setSelection(Preferences.analyzeOnEditorSelect);

		check.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clusterAnalysisOnEditorSelection = check.getSelection();
			}
		});

		Group grp3 = new Group(container, SWT.NULL);
		grp3.setText("Maximum Word Size");
		grp3.setLayout(new FillLayout());

		Label lbMaxSize = new Label(grp3, SWT.NONE);
		lbMaxSize.setText("Select the maximum word size \n to be displayed in the wordcloud");

		Scale scMaxSize = new Scale(grp3, SWT.HORIZONTAL);
		scMaxSize.setMaximum(400);
		scMaxSize.setMinimum(40);
		scMaxSize.setSelection(Preferences.maxSize);

		Label maxsizeindi = new Label(grp3, SWT.NONE);
		maxsizeindi.setText("   " + String.valueOf(scMaxSize.getSelection()));

		scMaxSize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int val = scMaxSize.getSelection();
				maxFontSize = val;

				maxsizeindi.setText("   " + String.valueOf(val));
				maxsizeindi.pack();

			}
		});

		Group grp4 = new Group(container, SWT.NULL);
		grp4.setText("Minimum Word Size");
		grp4.setLayout(new FillLayout());

		Label lbMinSize = new Label(grp4, SWT.NONE);
		lbMinSize.setText("Select the minimum word size \n to be displayed in the wordcloud   ");

		Scale scMinSize = new Scale(grp4, SWT.HORIZONTAL);
		scMinSize.setMaximum(40);
		scMinSize.setMinimum(4);
		scMinSize.setSelection(Preferences.minSize);

		Label minsizeindi = new Label(grp4, SWT.NONE);
		minsizeindi.setText("   " + String.valueOf(scMinSize.getSelection()));

		scMinSize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int val = scMinSize.getSelection();
				minFontSize = val;

				minsizeindi.setText("   " + String.valueOf(val));
				minsizeindi.pack();

			}
		});

	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void saveInput() {
		Preferences.analyzeOnEditorSelect = clusterAnalysisOnEditorSelection;
		Preferences.maxSize = maxFontSize;
		Preferences.minSize = minFontSize;

	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

}

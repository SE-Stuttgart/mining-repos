package srmplugin.wizard;

import java.util.ArrayList;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import srmplugin.SRMSettings;
import srmprocess.DBConnection;

public class SelectCommitersPage extends WizardPage {
	
	DBConnection dataBaseConn = DBConnection.getDBConnection();;
	private Table t;
	boolean enableFinish = false;
	String selectedCommitter = null;
	
	public SelectCommitersPage() {
		super("Select Committers","Choose Committers",null);
	}
	
	@Override
	public void createControl(Composite parent) {
		
		Object[] data = dataBaseConn.getCommiters(50);
		ArrayList<String> committers = (ArrayList<String>) data[0];
		ArrayList<String> commitcount = (ArrayList<String>) data[1];
		
		Composite composite =  new Composite(parent, SWT.NULL);
		GridLayout gl = new GridLayout(3, false);
		gl.horizontalSpacing = 10;
		composite.setLayout(gl);
		
		Label lblMinsupp = new Label(composite, SWT.NONE);
		lblMinsupp.setText("Minimum Support:");
		
		final Scale minsupport = new Scale(composite, SWT.NONE);
		Label minsuppl = new Label(composite, SWT.NULL);
		
		
		// Der Minimum Supportwert ist zwischen 0.02 und 0.2
		minsupport.setMinimum(20); 		// 2%
		minsupport.setMaximum(200);		// 20%
		minsupport.setIncrement(1);
		minsupport.setSelection((int) (SRMSettings.minsupport * 1000)); 					// 100
		minsuppl.setText(Double.toString(Math.floor(SRMSettings.minsupport*1000)/10)+"%");	// 10%
		minsuppl.pack();
		minsupport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int val = minsupport.getSelection();
				float valf = ((float) val) / 10; // verwendet fuer die Anzeige in %
				minsuppl.setText(Float.toString(valf)+"%");
				minsuppl.pack();
				// Skaliere in den korrekten Bereich [0.02,0.2]
				SRMSettings.minsupport = ((float) val)/1000.0;
			}
		});
		
		GridData gd = new GridData();
		gd.widthHint = 200;
		minsupport.setLayoutData(gd);
		
		t = new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		t.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				SelectCommitersPage.this.enableFinish = true;
				SelectCommitersPage.this.setPageComplete(true);
				TableItem[] selection = t.getSelection();
				selectedCommitter = selection[0].getText(0);
				System.out.println(selectedCommitter);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) { }
			
		});
		gd = new GridData();
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		t.setLayoutData(gd);
		
		new TableColumn(t, SWT.NULL).setText("Committer");
		new TableColumn(t, SWT.NULL).setText("#Commits");
		
		TableItem ti = new TableItem(t, SWT.NULL);
		ti.setText(new String[] {">All<", "-"});
		
		for (int i=0; i<committers.size(); i++) {
			ti = new TableItem(t, SWT.NULL);
			ti.setText(0, committers.get(i));
			ti.setText(1, commitcount.get(i));
			System.out.println("Committer: "+committers.get(i)+" - "+commitcount.get(i));
		}
		
		t.getColumn(0).pack();
		t.getColumn(1).pack();
		
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		
		setControl(composite);
		
	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	@Override
	public boolean isPageComplete() {
		return enableFinish;
	}
}

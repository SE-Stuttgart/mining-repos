package srmplugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import java.awt.Color;

import javax.swing.JLabel;

import srmprocess.DBConnection;

public class WordCloud extends ViewPart {
	public static final String ID = "SrmPlugIn.WordCloud";
	
		
	DBConnection dataBaseCon = DBConnection.getDBConnection();
	
	public void OnStart(){
		dataBaseCon.CreateOutputTable("wctable");
		System.out.println("Created Word Cloud Table");
	}

		@Override
		public void createPartControl(Composite parent) {
			OnStart();
			Composite swtAwtComponent = new Composite(parent, SWT.EMBEDDED);
			java.awt.Frame frame = SWT_AWT.new_Frame( swtAwtComponent );
		    javax.swing.JPanel panel = new javax.swing.JPanel( );
		    panel.add(new JLabel("Test"));
		    panel.setForeground(Color.white);
		    
		    frame.add(panel);
		    frame.setForeground(Color.white);
		    
			
		}

		@Override
		public void setFocus() {
			// TODO Auto-generated method stub
			
		}
		
		
		
		
		
}

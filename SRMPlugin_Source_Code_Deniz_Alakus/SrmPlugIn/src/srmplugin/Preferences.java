package srmplugin;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class Preferences extends AbstractHandler {

	
	
	public static int months = 10;
	public static Date now;
	
	
	public static Date getLimit(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.MONTH, -months);
		return cal.getTime();
	}


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	

}

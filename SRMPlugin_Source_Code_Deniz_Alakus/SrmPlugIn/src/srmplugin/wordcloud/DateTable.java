package srmplugin.wordcloud;

import java.util.Date;
import java.util.HashMap;

public class DateTable {
	public static HashMap<String, Date> lastUsedTable = new HashMap<String, Date>();
		
	public static Date getLastCommitDate(String file){		
		return lastUsedTable.get(file);
	}
	
	
	
}

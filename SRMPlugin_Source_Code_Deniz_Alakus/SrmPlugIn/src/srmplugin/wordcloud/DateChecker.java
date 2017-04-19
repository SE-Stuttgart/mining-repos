package srmplugin.wordcloud;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;

import srmprocess.DBConnection;

public class DateChecker {

	public DateChecker() {

	}

	Map<String, Date> DateOfLastEdit = new HashMap<>();
	long newestPerFile = 0;
	long millis;
	
	Date newestDate;
	Date fileDate;

	public void createDateTable(List<MyWord> wordList) {
		Map<String, Date> LastUsedTable = new HashMap<>();
		for (MyWord word : wordList) {
			
			DBConnection dbconn = DBConnection.getDBConnection();
			String fileID = dbconn.getFileID(word.getPath());
			List<String> allUsagesOfFile = dbconn.getUsagesOfFile(fileID);

			for (String commitID : allUsagesOfFile) {
				String currentDate = dbconn.getDateFromCommitTable(commitID);
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
				try {
					fileDate = df.parse(currentDate);
					millis = fileDate.getTime();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (millis != 0) {					
					if(millis > newestPerFile){
						newestPerFile = millis;
						newestDate = fileDate;
					}
					
				}

			}
			
			LastUsedTable.put(word.getPath(), newestDate);

			
		}
		
		DateOfLastEdit = LastUsedTable;
		

	}

	

	public Color check(String filepath) {
		Color color = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		return color;
	}

}

package srmplugin.wordcloud;

import java.awt.geom.Area;
import java.util.Map;
import java.util.Random;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.Workbench;

import srmplugin.WordCloud;

public class WordPlacer {

	private Area area;
	private int minx,miny,maxx,maxy;
	private Random random;
	
	
	Map<String,Integer> words;
	IViewPart part;
	IWorkbench workbench;
	
	public WordPlacer(Map<String,Integer> sortedWords){
		words = sortedWords;
		
		
		part = workbench.getActiveWorkbenchWindow().getActivePage()
			    .findView(WordCloud.ID);
			if (part instanceof WordCloud) {
			    WordCloud view = (WordCloud) part;
			    
			    // now access whatever internals you can get to
			    
			}
		
	}
	
	public void placeWords(){
		
		
		
	}
	
	
}

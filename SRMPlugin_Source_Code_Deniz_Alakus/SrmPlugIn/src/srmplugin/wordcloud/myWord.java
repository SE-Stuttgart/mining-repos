package srmplugin.wordcloud;

public class myWord {
	
	private String path, word;
	private int count;
	
	public myWord(String path, String word, int count){
		this.path = path;
		this.word = word;
		this.count = count;
	}
	
	public String getWord(){
		return word;
	}
	
	public String getPath(){
		return path;
	}
	
	public float getCount(){
		return count;
	}
	
	@Override
	public boolean equals(Object obj){
		return (obj instanceof myWord) && ((myWord) obj).path.equals(path)
										   && ((myWord) obj).word.equals(word);
	}
	
	

}

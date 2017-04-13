package srmplugin.wordcloud;

public final class MyWord {
	
	private final String path, word;
	private final int count;
	
	public MyWord(String path, String word, int count){
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
		return (obj instanceof MyWord) && ((MyWord) obj).path.equals(path)
										   && ((MyWord) obj).word.equals(word);
	}
	
	@Override
	public int hashCode(){
		return path.hashCode() ^ word.hashCode();
		
	}
	

}

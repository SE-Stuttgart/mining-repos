package srmplugin.wordcloud;

public final class UniqueFile {
	
	private final String filepath, filename;
	
	public UniqueFile(String filepath, String filename){
		this.filepath = filepath;
		this.filename = filename;
	}
	
	public String getFilepath(){
		return filepath;
	}
	
	public String getFilename(){
		return filename;
	}
	
	@Override
	public int hashCode(){
		return filepath.hashCode() ^ filename.hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		return (obj instanceof UniqueFile) && ((UniqueFile) obj).filepath.equals(filepath)
										   && ((UniqueFile) obj).filepath.equals(filepath);
	}
}

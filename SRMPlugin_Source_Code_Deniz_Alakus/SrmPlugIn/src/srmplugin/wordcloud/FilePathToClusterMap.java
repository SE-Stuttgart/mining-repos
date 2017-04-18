package srmplugin.wordcloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import srmplugin.wordcloud.WordCloudLabelProvider;
import srmplugin.wordcloud.MyWord;

public class FilePathToClusterMap {
	private WordCloudLabelProvider labelProvider;
	private HashMap<String,List<String>> filePathToClusterMap;
	List<MyWord> wordList;
	
	public FilePathToClusterMap(WordCloudLabelProvider labelProvider){
		this.labelProvider = labelProvider;
		this.wordList = new ArrayList<MyWord>();
		this.filePathToClusterMap = new HashMap<String,List<String>>();
	}
	
	public HashMap<String,List<String>> getMap(){
		return this.filePathToClusterMap;
	}
	
	public List<MyWord> getWordList(){
		return this.wordList;
	}
	
	/**
	 * Insert the given clusterPath into the given clusterHashMap.
	 * Splits the clusterPath into Cluster and Path.
	 * Then checks if Path is already contained. 
	 * If yes append cluster.
	 * If no create key and add cluster.
	 * 
	 * @param clusterAndPath - Filepath containing clusternumber and filepath
	 * @param clusterHashMap - Hashmap that has key filepath and value: List of all clusters this file is part of.
	 */
	public void putInClusterHashMap(String clusterAndPath){
		String cluster = getCluster(clusterAndPath);
		String path = getPath(clusterAndPath);
		
		
		if(filePathToClusterMap.containsKey(path)){
			//increment value of this key
			List<String> clusterList = filePathToClusterMap.get(path);
			clusterList.add(cluster);
			filePathToClusterMap.put(path, clusterList);
			int count = filePathToClusterMap.get(path).size();
			checkMaxOccurences(count);
		} else {
			//create key with value 1
			List<String> clusterList = new ArrayList<>();
			clusterList.add(cluster);
			filePathToClusterMap.put(path, clusterList);
		}
		updateWordList(clusterAndPath);
	}
	
	private void updateWordList(String clusterAndPath) {
		
		String path = getPath(clusterAndPath);
		int count = filePathToClusterMap.get(path).size();
		String name = getName(clusterAndPath);
		MyWord currentWord = new MyWord(path, name, count);
		if(wordList.contains(currentWord)){
			wordList.remove(currentWord);
			wordList.add(currentWord);
		} else {
			//Word not yet part of the wordCloud. Create word
			wordList.add(currentWord);
		}	
	}
	
	/**
	 * Checks and sets the MaxOccurences parameter of the labelProvider to 
	 * determine the weight (wordsize) of the WordCloud words.
	 * @param count - compare this value against the current maxOccurence value
	 */
	private void checkMaxOccurences(int count) {
		int maxCount = labelProvider.getMaxOccurrences();
		if(maxCount < count){
			labelProvider.setMaxOccurrences(count);
		}
		
	}

	
	/**
	 * removes leading and trailing whitespaces from string
	 * splits string by whitespace delimiter
	 * 
	 * @param clusterAndPath - takes String in format "2.2 path/folder/filename"
	 * @return the filepath "path/folder/filename"
	 */
	private String getPath(String clusterAndPath) {
		int index = clusterAndPath.trim().indexOf(" ");
		String path = clusterAndPath.substring(index+1);
		return path;
	}

	/**
	 * removes leading and trailing whitespaces from string
	 * splits string by whitespace delimiter
	 * 
	 * @param clusterAndPath - takes String in format "2.2 path/folder/filename"
	 * @return the clusternumber "2.2"
	 */
	private String getCluster(String clusterAndPath) {
		String[] splitStr = clusterAndPath.trim().split("\\s+");
		return splitStr[0];
	}
	
	/**
	 * Inputs are structured like this: "1.2 path/folder/filename.xyz"
	 * 
	 * @param clusterAndPath
	 * @return return filename.xyz of the given clusterpath
	 */
	private String getName(String clusterAndPath) {
		
		int index = clusterAndPath.lastIndexOf("/");
		String fileName = clusterAndPath.substring(index + 1);
		return fileName;
	}
}

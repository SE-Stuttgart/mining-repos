package atsr.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Object hold all data of one Commit
 * 
 * @author Simon Lehmann
 *
 */
public class Commit {

	private List<File> files;
	private String id;
	private String author;
	private Date date;
	private String message;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            - Commit id (seven digits as hex)
	 * @param author
	 *            - Commit author (full name)
	 * @param date
	 *            - Commit date
	 * @param message
	 *            - Commit message
	 */
	public Commit(String id, String author, Date date, String message) {
		this.files = new ArrayList<File>();
		this.id = id;
		this.author = author;
		this.date = date;
		this.message = message;
	}

	/**
	 * Adds a file to the files-list of this commit
	 * 
	 * @param file
	 *            - a new file
	 */
	public void addFile(File file) {
		files.add(file);
	}

	/**
	 * 
	 * @return the size of the files-list
	 */
	public int numberOfFiles() {
		return files.size();
	}

	/* Getter and setter */

	public List<File> getFiles() {
		return files;
	}

	public String getId() {
		return id;
	}

	public String getAuthor() {
		return author;
	}

	public Date getDate() {
		return date;
	}

	public String getMessage() {
		return message;
	}
}

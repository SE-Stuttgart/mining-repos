package atsr.model;

/**
 * Object hold all data of one Docu
 * 
 * @author Simon Lehmann
 *
 */
public class Docu {

	private int id;
	private String path;
	private String description;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            - Docu id
	 * @param path
	 *            - Docu path
	 * @param description
	 *            - Docu description
	 */
	public Docu(int id, String path, String description) {
		this.setId(id);
		this.setPath(path);
		this.setDescription(description);
	}

	/* Getter and setter */

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

package atsr.model;

/**
 * Object hold all data of one Issue
 * 
 * @author Simon Lehmann
 *
 */
public class Issue {

	private int id;
	private String status;
	private String type;
	private String description;

	/**
	 * Constructor
	 * 
	 * An issue as line in the file splitted by ';' the four values are at
	 * position [0][4][10][21]
	 * 
	 * @param id
	 *            - Issue id
	 * @param status
	 *            - Issue status
	 * @param type
	 *            - Issue type
	 * @param description
	 *            - Issue description
	 */
	public Issue(int id, String status, String type, String description) {
		this.setId(id);
		this.setStatus(status);
		this.setType(type);
		this.setDescription(description);
	}

	/* Getter and setter */

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}

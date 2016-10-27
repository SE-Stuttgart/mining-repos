package atsr.model;

/**
 * Contains the result of the ValidationDatabaseConnection class.
 * The observers evaluate only this ValidateResult to get the information.
 *  
 * @author Simon
 *
 */
public class ValidateResult {

	private int validId;
	private int code;
	private String msg;

	/**
	 * Consturctor
	 * 
	 * @param validId - Id of the ValidationDatabaseConnection Thread
	 * @param code - Code of the MySQLException or "100" if it successful connected
	 * @param msg - Message to the Code
	 */
	public ValidateResult(int validId, int code, String msg) {
		this.setValidId(validId);
		this.setCode(code);
		this.setMessage(msg);
	}

	/* Getter and Setter */
	
	public int getValidId() {
		return validId;
	}

	private void setValidId(int validId) {
		this.validId = validId;
	}

	public int getCode() {
		return code;
	}

	private void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return msg;
	}

	private void setMessage(String msg) {
		this.msg = msg;
	}
}

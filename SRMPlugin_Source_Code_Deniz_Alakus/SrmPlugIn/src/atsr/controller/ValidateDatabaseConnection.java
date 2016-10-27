package atsr.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Observable;

import atsr.model.ValidateResult;

/**
 * Verify the database connection and submit the result every Observer
 * 
 * @author Simon
 *
 */
public class ValidateDatabaseConnection extends Observable implements Runnable {

	private int validId;
	private String username;
	private String password;
	private String ipAddress;
	private int port;

	/**
	 * Constructor with all parameter for the database connection
	 * 
	 * @param validId - id of the thread to get the order
	 * @param username - database username
	 * @param password - database password
	 * @param ipAddress - database IP-Address
	 * @param port - database port
	 */
	public ValidateDatabaseConnection(int validId, String username, String password, String ipAddress, int port) {
		this.validId = validId;
		this.username = username;
		this.password = password;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	/**
	 * Open a connection to the database and notify all Observer with the result
	 */
	@Override
	public void run() {
		try {
			if (!ipAddress.equals("")) {
				String url = "jdbc:mysql://" + ipAddress + ":" + port + "/";
				Connection conn = DriverManager.getConnection(url, username, password);
				this.notify(100, "successful connected");
				conn.close();
			} else {
				if (ipAddress.equals("")) {
					this.notify(0, "Empty IP-Address");
				}
			}
		} catch (SQLException e) {
			this.notify(e.getErrorCode(), e.getMessage());
		}
	}

	/**
	 * Notify the observer
	 * 
	 * @param code - ErrorCode of the SQLException or "100" for successful connected
	 * @param msg - Errormessage or successmessage
	 */
	private void notify(int code, String msg) {
		this.setChanged();
		this.notifyObservers(new ValidateResult(validId, code, msg));
	}
	
	/* Getter */
	
	public int getId() {
		return validId;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getPort() {
		return port;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ValidateDatabaseConnection) {
			ValidateDatabaseConnection val = (ValidateDatabaseConnection) obj;
			if (this.getIpAddress().equals(val.getIpAddress()) && this.getUsername().equals(val.getUsername())
					&& this.getPassword().equals(val.getPassword()) && (this.getPort() == val.getPort())) {
				return true;
			}
		}
		return false;
	}
}

package atsr.model;

import java.io.Serializable;

/**
 * The Settings as object
 * 
 * @author Simon Lehmann
 *
 */
@SuppressWarnings("serial")
public class Settings implements Serializable {

	private String username;
	private String password;
	private String databaseName;
	private String ipAddress;
	private int port;
	private String gitPath;

	/**
	 * Constructor
	 */
	public Settings() {
	}

	/* Getter and setter */

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getGitPath() {
		return gitPath;
	}

	public void setGitPath(String gitPath) {
		this.gitPath = gitPath;
	}
}

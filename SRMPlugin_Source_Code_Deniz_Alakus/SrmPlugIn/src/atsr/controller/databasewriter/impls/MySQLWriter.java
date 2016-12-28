package atsr.controller.databasewriter.impls;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import atsr.controller.databasewriter.DatabaseWriter;
import atsr.model.Commit;
import atsr.model.Docu;
import atsr.model.Issue;
import atsr.model.Settings;

/**
 * Implementation of the interface DatabaseWriter
 * 
 * @author Simon Lehmann
 *
 */
public class MySQLWriter extends Observable implements DatabaseWriter {

	private Connection conn;
	private IProgressMonitor pmonitor;
	public int numFiles;

	/**
	 * Constructor
	 * 
	 * @param settings
	 *            - The settings written at settingsView
	 * @param obs
	 *            - The observer get notifications of the progress
	 */
	public MySQLWriter(Settings settings, IProgressMonitor pmonitor) throws SQLException {
		this.pmonitor = pmonitor;
		
//		String url = "jdbc:mysql://" + settings.getIpAddress() + ":" + settings.getPort() + "/";
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("MySQLWriter: Treiber nicht gefunden");
		}
		String url = "jdbc:h2:"+Platform.getConfigurationLocation().getURL().getPath()
				+settings.getDatabaseName()
				+";DATABASE_TO_UPPER=false;IGNORECASE=TRUE";

		conn = DriverManager.getConnection(url);
		
//		ResultSet res = conn.getMetaData().getCatalogs();
		
//		boolean dbExists = false;
//		while (res.next()) {
//			if (settings.getDatabaseName().equals(res.getString(1))) {
//				dbExists = true;
//			}
//		}
//		if (!dbExists) {
//			Statement stmt = conn.createStatement();
//			String query = "create schema " + settings.getDatabaseName();
//			stmt.executeUpdate(query);
//			stmt.close();
//		}
//		conn = DriverManager.getConnection(url + settings.getDatabaseName(), settings.getUsername(), settings.getPassword());
		
	}
	
	/**
	 * Close the databaseconnection
	 */
	@Override
	public void close() throws SQLException {
		conn.close();
	}
	
	/**
	 * Write commits into the MySQL database
	 */
	@Override
	public void writeCommits(List<Commit> commits) throws SQLException {
		// TODO Remove only for Test
		Date beginn = new Date();

		if (commitTablesExist()) {
			if (!commitTablesAreEmpty()) {
				// Create backup of the tables
				//createCommitBackup();
				// Clear tables
				clearCommitTables();
			}
		} else {
			// Create tables
			createCommitTables();
		}

		// 6% of the work is done now
		this.notifyProgress(6);

		// Describe tables
		writeInCommitTables(commits);

		// TODO Remove only for Test
		Date end = new Date();
		System.out.println("END: [" + (end.getTime() - beginn.getTime()) + "]");
	}

	/**
	 * Check if the three tables exist in the database. If only one or two exist
	 * they will be dropped and return false.
	 * 
	 * @return true - if "committable", "filetable" and "usagetable" exist
	 * @throws SQLException
	 */
	private boolean commitTablesExist() throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet res = meta.getTables(null, null, null, new String[] { "TABLE" });

		boolean commitTableExists = false;
		boolean fileTableExists = false;
		boolean usageTableExists = false;

		while (res.next()) {
			switch (res.getString("TABLE_NAME")) {
			case "committable":
			case "COMMITTABLE":
				commitTableExists = true;
				break;
			case "filetable":
			case "FILETABLE":
				fileTableExists = true;
				break;
			case "usagetable":
			case "USAGETABLE":
				usageTableExists = true;
				break;
			}
		}
		res.close();

		// Only if all three tables exists we don't have to create new tables
		if (commitTableExists && fileTableExists && usageTableExists) {
			return true;
		}

		Statement stmt = conn.createStatement();

		// Drop all existing tables
		if (usageTableExists) {
			String usageQuery = "drop table usagetable";
			stmt.executeUpdate(usageQuery);
		}
		if (commitTableExists) {
			String commitQuery = "drop table committable";
			stmt.executeUpdate(commitQuery);
		}
		if (fileTableExists) {
			String fileQuery = "drop table filetable";
			stmt.executeUpdate(fileQuery);
		}
		stmt.close();
		return false;
	}

	/**
	 * Check if the three tables "committable", filetable" and "usagetable" are
	 * empty. Is only one not empty it return false.
	 * 
	 * @return true - If all tables are empty
	 * @throws SQLException
	 */
	private boolean commitTablesAreEmpty() throws SQLException {
		String commitQuery = "select * from committable";
		String fileQuery = "select * from filetable";
		String usageQuery = "select * from usagetable";

		boolean empty = true;

		Statement stmt = conn.createStatement();

		ResultSet commitRs = stmt.executeQuery(commitQuery);
		// Is committable empty?
		if (commitRs.next()) {
			empty = false;
		}

		ResultSet fileRs = stmt.executeQuery(fileQuery);
		// Is filetable empty?
		if (fileRs.next()) {
			empty = false;
		}

		ResultSet usageRs = stmt.executeQuery(usageQuery);
		// Is usagetable empty?
		if (usageRs.next()) {
			empty = false;
		}
		stmt.close();
		return empty;
	}

	/**
	 * Create a backup of the current "committable", "issuetable" and
	 * "usagetable" with the timestamp of the actual date.
	 * 
	 * @throws SQLException
	 */
	private void createCommitBackup() throws SQLException {
		Statement stmt = conn.createStatement();
		DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();

		// Backup committable
		String commitTablename = df.format(date) + "_committable";
		String commitCreateTable = "create Table " + commitTablename + " like committable";
		String commitInsertTable = "insert " + commitTablename + " select * from committable";
		stmt.executeUpdate(commitCreateTable);
		stmt.executeUpdate(commitInsertTable);

		// Backup filetable
		String fileTablename = df.format(date) + "_filetable";
		String fileCreateTable = "create Table " + fileTablename + " like filetable";
		String fileInsertTable = "insert " + fileTablename + " select * from filetable";
		stmt.executeUpdate(fileCreateTable);
		stmt.executeUpdate(fileInsertTable);

		// Backup usagetable
		String usageTablename = df.format(date) + "_usagetable";
		String usageCreateTable = "create Table " + usageTablename + " like usagetable";
		String usageInsertTable = "insert " + usageTablename + " select * from usagetable";
		stmt.executeUpdate(usageCreateTable);
		stmt.executeUpdate(usageInsertTable);

		stmt.close();
	}

	/**
	 * Delete all entries from the three tables
	 * 
	 * @throws SQLException
	 */
	private void clearCommitTables() throws SQLException {
		String usageQuery = "delete from usagetable";
		String fileQuery = "delete from filetable";
		String commitQuery = "delete from committable";

		Statement stmt = conn.createStatement();

		// Order is important
		stmt.executeUpdate(usageQuery);
		stmt.executeUpdate(fileQuery);
		stmt.executeUpdate(commitQuery);

		stmt.close();
	}
	
	private void dropCommitTables() throws SQLException {
		String usageQuery = "drop table usagetable";
		String fileQuery = "drop table filetable";
		String commitQuery = "drop table committable";

		Statement stmt = conn.createStatement();

		// Order is important
		stmt.executeUpdate(usageQuery);
		stmt.executeUpdate(fileQuery);
		stmt.executeUpdate(commitQuery);

		stmt.close();
	}

	/**
	 * Create the three tables
	 * 
	 * @throws SQLException
	 */
	private void createCommitTables() throws SQLException {
		String usageQuery = "create table usagetable " + "(id int not null, " + "commit_id varchar(12), "
				+ "file_id int, " + "primary key(id), " + "foreign key(commit_id) references committable(id), "
				+ "foreign key(file_id) references filetable(id))";
		String fileQuery = "create table filetable " + "(id int not null, " + "path varchar(255), "
				+ "primary key(id))";
		String commitQuery = "create table committable " + "(id varchar(12) not null, " + "author varchar(255), "
				+ "date datetime, " + "message longtext, " + "primary key(id))";

		Statement stmt = conn.createStatement();

		// Order is important
		stmt.executeUpdate(commitQuery);
		stmt.executeUpdate(fileQuery);
		stmt.executeUpdate(usageQuery);

		stmt.close();
	}

	/**
	 * Write into the three tables with the commits and notify the progress.
	 * 
	 * @param commits
	 *            - The commits in one list
	 * @throws SQLException
	 */
	private void writeInCommitTables(List<Commit> commits) throws SQLException {
		// ID's for the tables
		int fileId = 0;
		int usageId = 0;

		// Commit entry
		String commitSql = "insert into committable (id, author, date, message) values (?,?,?,?)";
		PreparedStatement commitStmt = conn.prepareStatement(commitSql);

		// File entry
		String fileSql = "insert into filetable (id, path) values (?,?)";
		PreparedStatement fileStmt = conn.prepareStatement(fileSql);

		// Usage entry
		String usageSql = "insert into usagetable (id, commit_id, file_id) values (?,?,?)";
		PreparedStatement usageStmt = conn.prepareStatement(usageSql);

		// Already exists File Statement
		Statement stmt = conn.createStatement();

		// Calculate progress time
		int numberOfFiles = 0;
		for (Commit commit : commits) {
			numberOfFiles += commit.getFiles().size();
		}
		
		numFiles = numberOfFiles;
		pmonitor.beginTask("Schreibe Commits", numFiles);
		
		for (Commit commit : commits) {
			
			// Breche Transformation ab, sobald User requested
			if(pmonitor.isCanceled()) {
				dropCommitTables();
				break;
			}
			
			commitStmt.clearParameters();
			commitStmt.setString(1, commit.getId());
			commitStmt.setString(2, commit.getAuthor());
			commitStmt.setTimestamp(3, new Timestamp(commit.getDate().getTime()));
			commitStmt.setString(4, commit.getMessage());

			commitStmt.executeUpdate();

			for (File file : commit.getFiles()) {
				
				pmonitor.subTask("File - "+file.toString());
				// File already contained in table
				stmt = conn.createStatement();
				// MySql need two backslashes to find a backslash in varchar
				String mySQLSearchPath = file.getPath().replace("\\", "\\\\");
				// If the char "'" is not replaced that leads to an error
				mySQLSearchPath = mySQLSearchPath.replace("\'", "");
				ResultSet rs = stmt.executeQuery("select id from filetable where path = '" + mySQLSearchPath + "'");

				if (rs.next()) {
					// It already exists in the fileTable -> write only into
					// usagetable
					int currentFileId = rs.getInt(1);

					usageStmt.clearParameters();
					usageStmt.setInt(1, usageId);
					usageStmt.setString(2, commit.getId());
					usageStmt.setInt(3, currentFileId);

					usageStmt.executeUpdate();

					usageId++;
				} else {
					// It doesn't exists in the fileTable -> write into
					// filetable and usagetable
					fileStmt.clearParameters();
					fileStmt.setInt(1, fileId);
					fileStmt.setString(2, file.getPath());

					fileStmt.executeUpdate();

					usageStmt.clearParameters();
					usageStmt.setInt(1, usageId);
					usageStmt.setString(2, commit.getId());
					usageStmt.setInt(3, fileId);

					usageStmt.executeUpdate();

					usageId++;
					fileId++;
				}

				rs.close();
				pmonitor.worked(1);
			}
		}
		stmt.close();
		commitStmt.close();
		fileStmt.close();
		usageStmt.close();
	}

	/**
	 * Write issues into the MySQL database
	 */
	@Override
	public void writeIssues(List<Issue> issues) throws SQLException {
		// TODO Remove only for Test
		Date beginn = new Date();

		if (issueTableExists()) {
			if (!issueTableIsEmpty()) {
				// Create backup of the tables
				//createIssueBackup();
				// Clear tables
				clearIssueTable();
			}
		} else {
			// Create tables
			createIssueTable();
		}

		// 6% of the work is done now
		this.notifyProgress(6);

		// Describe tables
		writeInIssueTable(issues);

		// TODO Remove only for Test
		Date end = new Date();
		System.out.println("END: [" + (end.getTime() - beginn.getTime()) + "]");
	}

	/**
	 * Check if the "issuetable" exists in the database.
	 * 
	 * @return true - if "issuetable" exists
	 * @throws SQLException
	 */
	private boolean issueTableExists() throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet res = meta.getTables(null, null, null, new String[] { "TABLE" });

		boolean issueTableExists = false;

		while (res.next()) {
			if (res.getString("TABLE_NAME").equals("issuetable")) {
				issueTableExists = true;
			}
		}
		if (issueTableExists) {
			return true;
		}
		return false;
	}

	/**
	 * Check if "issuetable" is empty.
	 * 
	 * @return true - If "issuetable" is empty
	 * @throws SQLException
	 */
	private boolean issueTableIsEmpty() throws SQLException {
		String issueQuery = "select * from issuetable";

		boolean empty = true;

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(issueQuery);

		// Is committable empty?
		if (rs.next()) {
			empty = false;
		}

		stmt.close();

		return empty;
	}

	/**
	 * Create a backup of the current "issuetable" with the timestamp of the
	 * actual date.
	 * 
	 * @throws SQLException
	 */
	private void createIssueBackup() throws SQLException {
		Statement stmt = conn.createStatement();
		DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();

		// Backup issuetable
		String issueTablename = df.format(date) + "_issuetable";
		String issueCreateTable = "create Table " + issueTablename + " like issuetable";
		String issueInsertTable = "insert " + issueTablename + " select * from issuetable";
		stmt.executeUpdate(issueCreateTable);
		stmt.executeUpdate(issueInsertTable);

		stmt.close();
	}

	/**
	 * Delete all entries from the "issuetable"
	 * 
	 * @throws SQLException
	 */
	private void clearIssueTable() throws SQLException {
		String issueQuery = "delete from issuetable";
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(issueQuery);
		stmt.close();
	}

	/**
	 * Create the "issuetable"
	 * 
	 * @throws SQLException
	 */
	private void createIssueTable() throws SQLException {
		String issueQuery = "create table issuetable " + "(id int not null, " + "status varchar(255), "
				+ "type varchar(255), " + "description longtext, " + "primary key(id))";

		Statement stmt = conn.createStatement();
		stmt.executeUpdate(issueQuery);
		stmt.close();
	}

	/**
	 * Write into the "issuetable" with its issues and notify the progress.
	 * 
	 * @param issues
	 *            - The issues in one list
	 * @throws SQLException
	 */
	private void writeInIssueTable(List<Issue> issues) throws SQLException {
		// Issue entry
		pmonitor.beginTask("Schreibe Issues", issues.size());
		String issueSql = "insert into issuetable (id, status, type, description) values (?,?,?,?)";
		PreparedStatement issueStmt = conn.prepareStatement(issueSql);

		Issue issue;
		for (int i = 0; i < issues.size(); i++) {
			if(pmonitor.isCanceled()) {
				clearIssueTable();
				break;
			}
			issue = issues.get(i);
			issueStmt.clearParameters();
			issueStmt.setInt(1, issue.getId());
			issueStmt.setString(2, issue.getStatus());
			issueStmt.setString(3, issue.getType());
			issueStmt.setString(4, issue.getDescription());

			issueStmt.executeUpdate();
			
			pmonitor.worked(1);
			// The rest 94% of the work is in progress
			//this.notifyProgress(6 + ((i * 94) / issues.size()));
		}

		issueStmt.close();
	}

	/**
	 * Write docus into the MySQL database
	 */
	@Override
	public void writeDocus(List<Docu> docus) throws SQLException {
		// TODO Remove only for Test
		Date beginn = new Date();

		if (docuTableExists()) {
			if (!docuTableIsEmpty()) {
				// Create backup of the tables
				//createDocuBackup();
				// Clear tables
				clearDocuTable();
			}
		} else {
			// Create tables
			createDocuTable();
		}

		// 6% of the work is done now
		this.notifyProgress(6);

		// Describe tables
		writeInDocuTable(docus);

		// TODO Remove only for Test
		Date end = new Date();
		System.out.println("END: [" + (end.getTime() - beginn.getTime()) + "]");
	}

	/**
	 * Check if the "docutable" exists in the database.
	 * 
	 * @return true - if "docutable" exists
	 * @throws SQLException
	 */
	private boolean docuTableExists() throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet res = meta.getTables(null, null, null, new String[] { "TABLE" });

		boolean docuTableExists = false;

		while (res.next()) {
			if (res.getString("TABLE_NAME").equals("docutable")) {
				docuTableExists = true;
			}
		}
		if (docuTableExists) {
			return true;
		}
		return false;
	}

	/**
	 * Check if "docutable" is empty.
	 * 
	 * @return true - If "docutable" is empty
	 * @throws SQLException
	 */
	private boolean docuTableIsEmpty() throws SQLException {
		String docuQuery = "select * from docutable";

		boolean empty = true;

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(docuQuery);

		// Is committable empty?
		if (rs.next()) {
			empty = false;
		}

		stmt.close();

		return empty;
	}

	/**
	 * Create a backup of the current "docutable" with the timestamp of the
	 * actual date.
	 * 
	 * @throws SQLException
	 */
	private void createDocuBackup() throws SQLException {
		Statement stmt = conn.createStatement();
		DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();

		// Backup docutable
		String docuTablename = df.format(date) + "_docutable";
		String docuCreateTable = "create Table " + docuTablename + " like docutable";
		String docuInsertTable = "insert " + docuTablename + " select * from docutable";
		stmt.executeUpdate(docuCreateTable);
		stmt.executeUpdate(docuInsertTable);

		stmt.close();
	}

	/**
	 * Delete all entries from the "docutable"
	 * 
	 * @throws SQLException
	 */
	private void clearDocuTable() throws SQLException {
		String docuQuery = "delete from docutable";
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(docuQuery);
		stmt.close();
	}

	/**
	 * Create the "docutable"
	 * 
	 * @throws SQLException
	 */
	private void createDocuTable() throws SQLException {
		String docuQuery = "create table docutable " + "(id int not null, " + "path varchar(255), "
				+ "description longtext, " + "primary key(id))";

		Statement stmt = conn.createStatement();
		stmt.executeUpdate(docuQuery);
		stmt.close();
	}

	/**
	 * Write into the "docutable" with its docus and notify the progress.
	 * 
	 * @param docus
	 *            - The docu in one list
	 * @throws SQLException
	 */
	private void writeInDocuTable(List<Docu> docus) throws SQLException {
		// Docu entry
		pmonitor.beginTask("Schreibe Docus", docus.size());
		String ducoSql = "insert into docutable (id, path, description) values (?,?,?)";
		PreparedStatement docuStmt = conn.prepareStatement(ducoSql);

		Docu docu;
		for (int i = 0; i < docus.size(); i++) {
			if(pmonitor.isCanceled()) {
				clearDocuTable();
				break;
			}
			docu = docus.get(i);
			docuStmt.clearParameters();
			docuStmt.setInt(1, docu.getId());
			docuStmt.setString(2, docu.getPath());
			docuStmt.setString(3, docu.getDescription());

			docuStmt.executeUpdate();
			
			pmonitor.worked(1);
			// The rest 94% of the work is in progress
			//this.notifyProgress(6 + ((i * 94) / docus.size()));
		}

		docuStmt.close();
	}

	/**
	 * Notify the observer with an integer value.
	 * 
	 * @param value
	 *            - the progress value between 0 - 100
	 */
	private void notifyProgress(int value) {
		this.setChanged();
		this.notifyObservers(value);
	}

}

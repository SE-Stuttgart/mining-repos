package atsr.controller.databasewriter;

import java.sql.SQLException;
import java.util.List;
import java.util.Observer;

import atsr.model.Commit;
import atsr.model.Docu;
import atsr.model.Issue;

/**
 * The general interface two write into a database
 * 
 * @author Simon Lehmann
 *
 */
public interface DatabaseWriter {

	/**
	 * 
	 * @param commits
	 *            - A list of all commits of a git repository
	 * @throws SQLException
	 */
	public void writeCommits(List<Commit> commits) throws SQLException;

	/**
	 * 
	 * @param issues
	 *            - A list of all issues of one file
	 * @throws SQLException
	 */
	public void writeIssues(List<Issue> issues) throws SQLException;

	/**
	 * 
	 * @param docus
	 *            - A list of all docus of one file
	 * @throws SQLException
	 */
	public void writeDocus(List<Docu> docus) throws SQLException;

	/**
	 * Close the Database Connection
	 * 
	 * @throws SQLException
	 */
	public void close() throws SQLException;
	
	/**
	 * 
	 * @param obs
	 *            - add a new observer
	 */
	public void addObserver(Observer o);

}

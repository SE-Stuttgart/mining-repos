/*******************************************************************************
 * If you modify this Program, or any covered work, by linking or
 * combining it with FPGA (or a modified version of that
 * library), containing parts covered by the terms of EPL, the licensors of this Program grant you additional permission to convey the resulting work. Corresponding source for a non-source form of such a combination shall include the source code for the parts of FPGA used as well as that of the covered work.
 * 
 * 
 * SPMF GPL Exception
 * 
 * Section7 Exception
 * 
 * As a special exception to the terms and conditions of the GNU General Public License Version 3 (the "GPL"): You are free to convey a modified version that is formed entirely from this file (for purposes of this exception, the "Program" under the GPL) and the works identified at (http://www.philippe-fournier-viger.com/spmf/index.php?link=license.php) (each an "Excepted Work"), which are conveyed to you by Philippe Fournier-Viger and licensed under one the licenses identified in the Excepted License List below, as long as:
 * 
 *    1. You obey the GPL in all respects for the Program and the modified version, except for Excepted Work which are identifiable sections of the modified version.
 * 
 * 2. All Excepted Works which are identifiable sections of the modified version, are distributed subject to the Excepted License.
 *          
 * If the above conditions are not met, then the Program may only be copied, modified, distributed or used under the terms and conditions of the GPL.
 * 
 * Excepted License List
 * 
 *     * Eclipse Public License: version 1.0
 ******************************************************************************/
package srmprocess;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import FPGA.FPGrowthAlgorithmus;

public class DBConnection {

	private Connection conn = null;
	private String database = ""; // Databasename
	private static DBConnection dbcon = null;
	private Statement statement; // Statement

	// simdi ise buraya yazilacak database readstoreprocedure new Storeprozedure
	// okunan degerler buraya
	public static List<List<String>> sqlprocedureInput;//

	// CommidMesagetable dan okunan degrler buraya prozodedurdan gelen sonuc
	public static List<List<String>> sqlcommitInput;

	// Issutabledan okunan degerler buraya
	public static List<List<String>> issueData = new ArrayList<List<String>>();

	// MinSupoort hesaplamak icin gerekli olan TRansaktion sayisinin
	// kaydedilmesinde
	public int mincounter;

	private DBConnection() {
	}

	// Erzeuge ein Singleton Objekt
	public static DBConnection getDBConnection() {
		if (DBConnection.dbcon == null)
			DBConnection.dbcon = new DBConnection();
		return DBConnection.dbcon;
	}

	private void createConnection() {
		try {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection("jdbc:h2:" + Platform.getConfigurationLocation().getURL().getPath()
					+ database + ";DATABASE_TO_UPPER=false;IGNORECASE=TRUE");
			/*
			 * System.out.println("jdbc:h2:"+Platform.getConfigurationLocation()
			 * .getURL().getPath() +database
			 * +";DATABASE_TO_UPPER=false;IGNORECASE=TRUE");
			 */
		} catch (ClassNotFoundException e) {
			System.out.println("Treiber nicht gefunden");
		} catch (SQLException e) {
			System.out.println("Connect nicht moeglich");
		}
	}

	private Connection getConnection() {
		if (conn == null)
			return null;
		else if (database.length() == 0)
			return null;

		return conn;
	}

	public boolean isConnected() {
		return conn != null;
	}

	public boolean isConnectedToDatabase() {
		return isConnected() && database.length() > 0;
	}

	public void setDatabse(String dbname) {
		database = dbname;
		if (isConnected()) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		createConnection();
	}

	public boolean existsTable(String name) {
		conn = this.getConnection();
		if (conn == null)
			return false;

		DatabaseMetaData metadata;
		ResultSet resultSet;
		try {
			metadata = conn.getMetaData();
			resultSet = metadata.getTables(null, null, name, null);
			if (resultSet.next())
				return true;
			else
				return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean commitTablesExist() {
		conn = this.getConnection();
		if (conn == null)
			return false;

		boolean commitTableExists = false;
		boolean fileTableExists = false;
		boolean usageTableExists = false;

		try {
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet res = meta.getTables(null, null, null, new String[] { "TABLE" });
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
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Only if all three tables exists we don't hava to create new tables
		if (commitTableExists && fileTableExists && usageTableExists) {
			return true;
		} else
			return false;
	}

	/**
	 * zum Testen
	 *
	 * //---------------This Method reads the data from the
	 * inputtestdatabasetable---------- //-------saves it into an array and send
	 * the array as an input to the fpgaAlg------
	 * 
	 * public void ReadInputTabletest() { mincounter=0; List<List<String>> res =
	 * new ArrayList<>(); conn = getInstance(); if(conn != null) { Statement
	 * query = null; String sql; ResultSet result = null; try { query =
	 * conn.createStatement(); sql = "SELECT * FROM inputtable2"; result =
	 * query.executeQuery(sql); } catch (SQLException e1) {
	 * e1.printStackTrace(); } try { ResultSetMetaData data =
	 * result.getMetaData(); int numcols = data.getColumnCount();
	 * 
	 * while(result.next()){ List<String> row = new ArrayList<>(numcols); int i
	 * = 1; while (i <= numcols) { if(!result.getString(i).equals("")){
	 * row.add(result.getString(i)); } i++; } mincounter++; res.add(row); } }
	 * catch (SQLException e) { e.printStackTrace(); }
	 * FPGrowthAlgorithmus.input=res; // System.out.println("FOGA
	 * giden"+""+res);
	 * 
	 * }}
	 * 
	 */

	// ---------------This Method reads the data from the
	// inputdatabasetable----------
	// -------saves it into an array and send the array as an input to the
	// fpgaAlg------

	public void ReadInputTable(String author) {

		mincounter = 0;
		List<List<String>> res = new ArrayList<>();

		if ((conn = getConnection()) != null) {

			try (Statement query = conn.createStatement()) {

				String sql;
				ResultSet result = null;

				if (author == null)
					sql = "SELECT commit_id, file_id FROM usagetable";
				else
					// Wähle lediglich (commit_id,file_id) Paare aus, die einem
					// bestimmten Autor gehören
					sql = "SELECT commit_id, file_id " + "FROM usagetable WHERE commit_id IN ( "
							+ "SELECT c.id FROM committable c " + "WHERE c.author = '" + author + "' )";

				result = query.executeQuery(sql);

				List<String> temp = new ArrayList<>();
				int index = 0;

				while (result.next()) {

					// Null werte wird überprüft
					if (result.getString(2).toString().equals("")) {

						// hier wird überprüft, ob die letze Tranaktionen null
						// ??
						if (result.isLast()) {
							if (index > 1) {
								res.add(temp);
								mincounter++;
							}
						}

					} else {
						// erste CommidId wird gespeichert.
						if (temp.isEmpty()) {
							temp.add(result.getString(1).toString());
							temp.add(ReadFile(result.getString(2).toString()));
							index++;
							// Solange Commidid gleich bleibt,werden nun fileid
							// gespeichert.

						} else if (temp.get(0).equals(result.getString(1)) && !result.isLast()) {
							temp.add(ReadFile(result.getString(2).toString()));
							index++;
						}
						// wird Commidid geändert...
						else {

							if (result.isLast()) {
								if (temp.get(0).equals(result.getString(1))) {
									temp.add(ReadFile(result.getString(2).toString()));
									index++;
								} else {
								}

							}
							if (index > 1) {
								res.add(temp);
								mincounter++;
								temp = new ArrayList<>();
								temp.add(result.getString(1).toString());
								temp.add(ReadFile(result.getString(2).toString()));
								index = 1;

							} else {
								temp = new ArrayList<>();
								temp.add(result.getString(1).toString());
								temp.add(ReadFile(result.getString(2).toString()));
								index = 1;
							}
						}
					}
				} // while
			} catch (SQLException e) {
				e.printStackTrace();
			}

			FPGrowthAlgorithmus.input = res;
		}
	}

	// determine filepath through file ID. Convert FilePath and send to
	// ReadInputTable()
	public String ReadFile(String FileID) {
		String resfileID = new String();
		conn = getConnection();
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();
				String sql = "SELECT path FROM filetable WHERE id ='" + FileID + "'";
				ResultSet result = query.executeQuery(sql);
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {
					int i = 1;
					assert numcols == 1;
					while (i <= numcols) {
						resfileID += result.getString(i++);
					}

				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return resfileID.replace("\\", "/");
	}

	// This places reads the data from the Commid Message Table
	// We are calling in Commit View
	// We've done it this way for next time we see a new object
	// Issue information is different value here, do not list here

	///////////////////////// Commidview de cagiriyoruz
	// Her cagirmada yeni obje �retmemiz geconnrektigi icin bu sekiilde yaptik
	/// Issu info farkli degerler oldugu iicn burada list yapcaz ki duger
	// degerelrede ulasas bilek

	public void readCommitTable() {
		List<List<String>> readcommittable = new ArrayList<>();

		conn = getConnection();
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();
				String sql = "SELECT * FROM committable";
				ResultSet result = query.executeQuery(sql);
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {
					List<String> row = new ArrayList<>(numcols);
					int i = 1;
					while (i <= numcols) {
						row.add(result.getString(i++));
					}
					readcommittable.add(row);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void readFileTable() {
		List<List<String>> readfiletable = new ArrayList<>();

		conn = getConnection();
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();
				String sql = "SELECT * FROM filetable";
				ResultSet result = query.executeQuery(sql);
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {
					List<String> row = new ArrayList<>(numcols);
					int i = 1;
					while (i <= numcols) {
						row.add(result.getString(i++));
					}
					readfiletable.add(row);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void readUsageTable() {
		List<List<String>> readusagetable = new ArrayList<>();

		conn = getConnection();
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();
				String sql = "SELECT * FROM usagetable";
				ResultSet result = query.executeQuery(sql);
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {
					List<String> row = new ArrayList<>(numcols);
					int i = 1;
					while (i <= numcols) {
						row.add(result.getString(i++));
					}
					readusagetable.add(row);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public String ReadCommitMessage(String searchedCommitID) {
		String resCommidId = new String();
		conn = getConnection();
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();
				String sql = "SELECT message FROM committable WHERE id ='" + searchedCommitID + "'";
				ResultSet result = query.executeQuery(sql);
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {
					int i = 1;
					while (i <= numcols) {
						resCommidId += result.getString(i++);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return resCommidId;
	}
	
	public String ReadCommitAuthor(String searchedCommitID) {
		String resCommidId = new String();
		conn = getConnection();
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();
				String sql = "SELECT author FROM committable WHERE id ='" + searchedCommitID + "'";
				ResultSet result = query.executeQuery(sql);
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {
					int i = 1;
					while (i <= numcols) {
						resCommidId += result.getString(i++);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return resCommidId;
	}
	
	public String ReadCommitDate(String searchedCommitID) {
		String resCommidId = new String();
		conn = getConnection();
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();
				String sql = "SELECT date FROM committable WHERE id ='" + searchedCommitID + "'";
				ResultSet result = query.executeQuery(sql);
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {
					int i = 1;
					while (i <= numcols) {
						resCommidId += result.getString(i++);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return resCommidId;
	}
	
	//TODO Methods for getting Color Tone for words in Wordcloud
	
	public List<List<String>> getFileID(String path) {		
		List<List<String>> readfiletable = new ArrayList<>();
		String replPath = path.replace("/", "\\");
		

		conn = getConnection();
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();
				String sql = "SELECT * FROM filetable WHERE path = '" + replPath + "'";
				ResultSet result = query.executeQuery(sql);
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {
					List<String> row = new ArrayList<>(numcols);
					int i = 1;
					while (i <= numcols) {
						row.add(result.getString(i++));
					}
					readfiletable.add(row);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return readfiletable;
	}
	
	public List<String> getUsagesOfFile(String fileID) {
		List<String> usagetable = new ArrayList<>();
		String row = null;

		conn = getConnection();
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();
				String sql = "SELECT commit_id FROM usagetable WHERE file_id = '" + fileID + "'";
				ResultSet result = query.executeQuery(sql);
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {					
					int i = 1;
					while (i <= numcols) {
						row = result.getString(i++).toString();
					}
					usagetable.add(row);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return usagetable;

	}
	
	public String getDateFromCommitTable(String commitID) {
		List<List<String>> readcommittable = new ArrayList<>();

		conn = getConnection();
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();
				String sql = "SELECT date FROM committable WHERE id = '"+commitID+"'";
				ResultSet result = query.executeQuery(sql);
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {
					List<String> row = new ArrayList<>(numcols);
					int i = 1;
					while (i <= numcols) {
						row.add(result.getString(i++));
					}
					readcommittable.add(row);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return readcommittable.get(0).get(0);
	}
	
	
	
	
	
	
	
	

	// This places reads the data from the issue Table
	public void ReadIssueTable() {
		List<List<String>> readissuetable = new ArrayList<>();
		conn = getConnection();
		if (conn != null && existsTable("issuetable")) {
			Statement query;
			try {

				query = conn.createStatement();
				String sql = "SELECT * FROM issuetable";
				ResultSet result = query.executeQuery(sql);
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {
					List<String> row = new ArrayList<>(numcols);
					int i = 1;
					while (i <= numcols) {
						row.add(result.getString(i++));
					}
					readissuetable.add(row);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			issueData = readissuetable;
		}
	}

	// This places reads the data from the docu Table
	// call sqlprozedure
	public List<String> ReadDocuTable(String Selected_File) {

		List<String> res = new ArrayList<>();

		if ((conn = getConnection()) != null && existsTable("docutable")) {

			PreparedStatement cstmt = null;

			try {
				cstmt = conn.prepareStatement("SELECT * from docutable WHERE path LIKE ?");
				cstmt.setString(1, Selected_File);
				ResultSet result = cstmt.executeQuery();
				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {

					int i = 2;
					while (i <= numcols) {
						res.add(result.getString(i++));
					}
				}
				result.close();

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					if (cstmt != null)
						cstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		return res;
	}

	// This places call sqlprozedure for inputtable
	public void ReadOutputTable(String selectedFile) {

		sqlprocedureInput = new ArrayList<>();
		sqlcommitInput = new ArrayList<>();
		List<List<String>> res = new ArrayList<>();
		PreparedStatement cstmt = null;

		if ((conn = getConnection()) != null) {

			try (Statement s = conn.createStatement()) {

				// only get one row
				ResultSet result = s.executeQuery("SELECT * FROM outputtable LIMIT 1");
				ResultSetMetaData metaData = result.getMetaData();

				// return if outputtable empty
				if (!result.next())
					return;

				int itemcount = 0;
				// find out how many "Item" columns exist
				for (int i = 4; i <= metaData.getColumnCount(); i++) {
					if (metaData.getColumnName(i).contains("Item")) {
						itemcount++;
					}
				}

				// build where clause from Item count
				StringBuilder whereclause = new StringBuilder(160);

				if (itemcount > 1) {
					whereclause.append("Item1 LIKE ? OR ");
					for (int i = 2; i <= itemcount - 1; i++) {
						whereclause.append("Item" + i + " LIKE ? OR ");
					}
					whereclause.append("Item" + itemcount + " LIKE ?");
				} else
					whereclause.append("Item1 LIKE ?");

				String sql = "SELECT * FROM outputtable WHERE " + whereclause;
				cstmt = conn.prepareStatement(sql);

				for (int i = 1; i <= itemcount; i++) {
					cstmt.setString(i, selectedFile);
				}

				result = cstmt.executeQuery();

				ResultSetMetaData data = result.getMetaData();
				int numcols = data.getColumnCount();
				while (result.next()) {
					List<String> row = new ArrayList<>(numcols);
					int i = 2;
					while (i <= numcols) {

						if (result.getString(i) != null) {
							row.add(result.getString(i));
						}
						i++;
					}
					res.add(row);
				}
				result.close();

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					if (cstmt != null)
						cstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			sqlprocedureInput = res; // notwendig
										// Clusteranalyse(Coupledchangesview)!
			sqlcommitInput = res; // notwendig für
									// Klassifikation(Commitchangesview)!
		}
	}

	// ------this method is used to insert to the Outputtable-------
	public void WriteIntoOutputTable(String outputTableName) {
		String Nullval = null;
		conn = getConnection();

		int i = 0;
		if (conn != null) {
			try {
				statement = conn.createStatement();
				for (int l = 0; l < FPGrowthAlgorithmus.output.size(); l++) {
					String sqlSTR = "INSERT INTO " + outputTableName + "(";
					sqlSTR += "Support, Length, ";
					for (int y = 1; y <= FPGrowthAlgorithmus.maxSupport; y++) {
						sqlSTR += "CommitID" + y + ",";
					}
					int w;
					for (w = 1; w < FPGrowthAlgorithmus.maxLength; w++) {
						sqlSTR += "Item" + w + ", ";
					}
					sqlSTR += "Item" + w + ") VALUES (";
					sqlSTR += "'" + FPGrowthAlgorithmus.output.get(l).get(0) + "'" + ",";
					sqlSTR += "'" + FPGrowthAlgorithmus.output.get(l).get(1) + "'" + ",";
					if (Integer.parseInt(FPGrowthAlgorithmus.output.get(l).get(0)) == FPGrowthAlgorithmus.maxSupport) {
						for (int x = 2; x < FPGrowthAlgorithmus.maxSupport + 2; x++) {
							sqlSTR += "'" + FPGrowthAlgorithmus.output.get(l).get(x) + "'" + ",";
						}
					} else if (Integer
							.parseInt(FPGrowthAlgorithmus.output.get(l).get(0)) < FPGrowthAlgorithmus.maxSupport) {
						int index = 0;
						for (int x = 2; x < FPGrowthAlgorithmus.maxSupport + 2; x++) {
							index++;
							if (index <= Integer.parseInt(FPGrowthAlgorithmus.output.get(l).get(0))) {
								sqlSTR += "'" + FPGrowthAlgorithmus.output.get(l).get(x) + "'" + ",";
							} else {
								sqlSTR += "'" + Nullval + "'" + ",";
							}
						}
					}
					if (Integer.parseInt(FPGrowthAlgorithmus.output.get(l).get(1)) == FPGrowthAlgorithmus.maxLength) {
						int z = Integer.parseInt(FPGrowthAlgorithmus.output.get(l).get(0)) + 2;
						for (; z < FPGrowthAlgorithmus.maxLength
								+ Integer.parseInt(FPGrowthAlgorithmus.output.get(l).get(0)) + 1; z++) {
							sqlSTR += "'" + FPGrowthAlgorithmus.output.get(l).get(z) + "'" + ",";
						}
						sqlSTR += "'" + FPGrowthAlgorithmus.output.get(l).get(z) + "'";
						sqlSTR += " )";
					} else if (Integer
							.parseInt(FPGrowthAlgorithmus.output.get(l).get(1)) < FPGrowthAlgorithmus.maxLength) {
						int index = 0;
						int z = Integer.parseInt(FPGrowthAlgorithmus.output.get(l).get(0)) + 2;
						for (; z < FPGrowthAlgorithmus.maxLength
								+ Integer.parseInt(FPGrowthAlgorithmus.output.get(l).get(0)) + 1; z++) {

							index++;
							if (index <= Integer.parseInt(FPGrowthAlgorithmus.output.get(l).get(1))
									&& z != FPGrowthAlgorithmus.output.get(l).size()) {
								// if
								// (z==FPGrowthAlgorithmus.output.get(l).size())
								// break;
								sqlSTR += "'" + FPGrowthAlgorithmus.output.get(l).get(z) + "'" + ",";
							} else {
								sqlSTR += "'" + Nullval + "'" + ",";
							}
						}

						sqlSTR += "'" + Nullval + "'";
						sqlSTR += " )";

					}
					statement.executeUpdate(sqlSTR);
					System.out.println("insert table" + (i++));
				}
			}

			catch (SQLException se) {
				// Handle errors for JDBC
				se.printStackTrace();
			} catch (Exception e) {
				// Handle errors for Class.forName
				e.printStackTrace();
			} finally {

			}

		}

	}

	// this method is used to create the Cluster- und outputtable in the
	// Datenbank
	/**
	 * After the Frequent-Itemset-Analysis by the FPGrowthAlgorithm terminated,
	 * this function creates the Outputtable. Old Outputtables are being
	 * overwritten, thus only containing up-to-date information. This function
	 * only creates the table structure. It is filled with data from the
	 * FPGrowthAlgorithm later on.
	 */
	public void CreateOutputTable(String outputTableName) {
		conn = getConnection();
		if (conn != null) {
			try {
				String myTableName;
				myTableName = "DROP TABLE IF EXISTS  " + outputTableName;
				statement = conn.createStatement();
				statement.executeUpdate(myTableName);
				myTableName = "CREATE TABLE  " + outputTableName + "(id INTEGER NOT NULL AUTO_INCREMENT, ";
				myTableName += "Support VARCHAR(300) ,";
				myTableName += "Length VARCHAR(300) ,";

				for (int i = 1; i <= FPGrowthAlgorithmus.maxSupport; i++)
					myTableName += " CommitID" + i + " VARCHAR(300) null , ";

				for (int j = 1; j <= FPGrowthAlgorithmus.maxLength; j++)
					myTableName += " Item" + j + " VARCHAR(300) null , ";
				myTableName += " PRIMARY KEY ( id)) ";
				statement = conn.createStatement();
				statement.executeUpdate(myTableName);
				System.out.println(outputTableName + " Table Created");
			} catch (SQLException e) {
				System.out.println("An error has occured on Table Creation");
				e.printStackTrace();
			}
		}
	}

	// Cluster Ergebnisse in CLusteroutputtable gespeichert
	public void WriteIntoOutputTable(String outputTableName, List<String> cluster) {

		conn = getConnection();
		if (conn != null) {
			try {
				statement = conn.createStatement();
				// System.out.println("insert " + outputTableName + "
				// table_______START");
				String sqlSTR = "INSERT INTO " + outputTableName + "(";
				sqlSTR += "Support, Length, ";
				for (int y = 1; y <= FPGrowthAlgorithmus.maxSupport; y++) {
					sqlSTR += "CommitID" + y + ",";
				}
				int w;
				for (w = 1; w < FPGrowthAlgorithmus.maxLength; w++) {
					sqlSTR += "Item" + w + ", ";
				}
				sqlSTR += "Item" + w + ") VALUES (";

				int c = 0;
				for (; c < cluster.size() - 1; c++) {
					sqlSTR += "'" + cluster.get(c) + "'" + ",";
				}
				sqlSTR += "'" + cluster.get(c) + "'";
				sqlSTR += " )";
				// System.out.println("SQL-String: " + sqlSTR );
				statement.executeUpdate(sqlSTR);
				// System.out.println("insert " + outputTableName + "
				// table_______END");
			}

			catch (SQLException se) {
				// Handle errors for JDBC
				se.printStackTrace();
			} catch (Exception e) {
				// Handle errors for Class.forName
				e.printStackTrace();
			} finally {

			}

		}

	}

	// Lese Committers mit >= nCommitters Commits aus
	public Object[] getCommiters(int nCommitters) {
		ArrayList<String> committers = new ArrayList<>();
		ArrayList<String> commitcount = new ArrayList<>();

		if ((conn = getConnection()) != null) {
			try (Statement s = conn.createStatement()) {
				String q = "SELECT author, COUNT(author) " + "FROM committable "
						+ "GROUP BY author HAVING COUNT(author) >= " + nCommitters + " "
						+ "ORDER BY COUNT(author) DESC";
				ResultSet res = s.executeQuery(q);
				while (res.next()) {
					committers.add(res.getString(1));
					commitcount.add(res.getString(2));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new Object[] { committers, commitcount };
	}

}

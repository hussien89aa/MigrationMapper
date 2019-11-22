package com.database.mysql;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.library.source.MigratedLibraries;
import com.algorithims.sa.*;
import com.project.settings.AppSettings;
import com.project.settings.DatabaseLogin;
import com.segments.build.Segment;

public class MigrationMappingDB {
	int CHARACTER_A_ASCII = 65;

	public MigrationMappingDB() {

		// System.out.println("Opened database successfully");
	}

	// return 0 if not found
	public int getMigrationMappingID(Segment segment) {

		int migrationMappingID = 0;
		// int migrationRuleID= new
		// MigrationRuleDB().getMigrationRuleID(FromLibrary,ToLibrary);
		Statement stmt = null;
		try {
			Connection c = null;
			Class.forName("com.mysql.cj.jdbc.Driver");
			c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

			// c.setAutoCommit(false);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String sql = "SELECT * FROM MigrationMapping where FromCode='" + arrayListToString(segment.removedCode)
					+ "' and  ToCode='" + arrayListToString(segment.addedCode) + "' and MigrationRuleID="
					+ MigratedLibraries.ID;
			// System.out.println(sql);
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				migrationMappingID = rs.getInt("MigrationMappingID");
				break;
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

		}

		return migrationMappingID;
	}

	/*
	 * Check if the segment is valid 0- not valid 1- valid 2- not verified yet
	 */

	public int getMigrationConfidentMapping(Segment segment) {

		int migrationMappingID = 0;
		// int migrationRuleID= new
		// MigrationRuleDB().getMigrationRuleID(FromLibrary,ToLibrary);
		Statement stmt = null;
		try {
			Connection c = null;
			Class.forName("com.mysql.cj.jdbc.Driver");
			c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

			// c.setAutoCommit(false);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String sql = "SELECT isVaildMapping FROM MigrationConfidentMapping where FromCode='"
					+ arrayListToString(segment.removedCode) + "' and  ToCode='" + arrayListToString(segment.addedCode)
					+ "' and MigrationRuleID=" + MigratedLibraries.ID;
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				migrationMappingID = rs.getInt("isVaildMapping");
				break;
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

		}

		return migrationMappingID;
	}

	public int getTotalCorrectMapping() {

		int totalCorrectMapping = 0;
		// int migrationRuleID= new
		// MigrationRuleDB().getMigrationRuleID(FromLibrary,ToLibrary);
		Statement stmt = null;
		try {
			Connection c = null;
			Class.forName("com.mysql.cj.jdbc.Driver");
			c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

			// c.setAutoCommit(false);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String sql = "SELECT count(*) as totalCorrectMapping FROM MigrationConfidentMapping where MigrationRuleID="
					+ MigratedLibraries.ID;
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				totalCorrectMapping = rs.getInt("totalCorrectMapping");
				break;
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

		}

		return totalCorrectMapping;
	}

	public void addMigrationMapping(Segment segment) {

		// int migrationRuleID= new
		// MigrationRuleDB().getMigrationRuleID(FromLibrary,ToLibrary);
		// if(migrationRuleID==0){
		// System.err.println("Cannot find MigrationRule from "+ FromLibrary + " to "+
		// ToLibrary);
		// return;
		// }
		int migrationMappingID = getMigrationMappingID(segment);
		if (migrationMappingID != 0) {
			// that mean this mapping already in database
			return; // TODO: fix this problem
		}

		// Statement stmt = null;
		try {
			Connection c = null;
			Class.forName("com.mysql.cj.jdbc.Driver");
			c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

			c.setAutoCommit(false);
			// stmt = c.createStatement();
			String sql = "INSERT INTO MigrationMapping (MigrationRuleID,FromCode,ToCode) VALUES (?,?,?);";
			PreparedStatement stmt = c.prepareStatement(sql);
			stmt.setInt(1, MigratedLibraries.ID);
			stmt.setString(2, arrayListToString(segment.removedCode));
			stmt.setString(3, arrayListToString(segment.addedCode));
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		System.out.println("Records created successfully");
	}

	public void addMigrationMappingWithConfident(Segment segment, ConfidentType confidentType) {
		// only library without version

		if (AppSettings.isTest == false) {
			addMigrationMapping(segment);
			// Not Used now
			new MappingConfident().addMappingConfident(segment, confidentType);
		}
	}

	public static String arrayListToString(ArrayList<String> listOfCode) {
		StringBuilder textCode = new StringBuilder();
		for (String lineCode : listOfCode)
			textCode.append(lineCode).append('\n');
		return textCode.toString();
	}

	public static String arrayListToStringSP(ArrayList<String> listOfCode) {
		StringBuilder textCode = new StringBuilder();
		for (int i = 0; i < listOfCode.size(); i++) {
			if (i != (listOfCode.size() - 1)) {
				textCode.append(listOfCode.get(i)).append('\n');
			} else {
				textCode.append(listOfCode.get(i));
			}
		}

		return textCode.toString();
	}

	public ArrayList<Segment> getFunctionMapping(String MigrationRuleID, boolean onlyOneToOne,
			boolean onlyValidMapping) {
		ArrayList<Segment> listOfFunctions = new ArrayList<Segment>();
		Statement stmt = null;
		try {
			Connection c = null;
			Class.forName("com.mysql.cj.jdbc.Driver");
			c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

			c.setAutoCommit(false);
			stmt = c.createStatement();
			// and MappingConfident.isVaildMapping=1
			String sql = "SELECT FromCode,ToCode, MappingConfident.isVaildMapping,MigrationMapping.MigrationMappingID FROM MigrationMapping INNER JOIN MappingConfident ON MigrationMapping.MigrationMappingID= MappingConfident.MigrationMappingID  WHERE MigrationRuleID in("
					+ MigrationRuleID + ") GROUP BY FromCode,ToCode, MappingConfident.isVaildMapping,MigrationMapping.MigrationMappingID";
			 
			ResultSet rs = stmt.executeQuery(sql);
			// System.out.println(sql);
			// HashMap<String, Integer> funMapping = new HashMap<String, Integer>();
			ArrayList<String> usedFunctions = new ArrayList<String>();
			// int rows=0;
			// int id=0;
			while (rs.next()) {

				Segment segment = new Segment();
				String addedCode = rs.getString("ToCode").trim();
				segment.MigrationMappingID = rs.getInt("MigrationMappingID") ;
				segment.isVaildMapping = rs.getInt("isVaildMapping");
				for (String lineCode : addedCode.split(";")) {
					lineCode = lineCode.trim().replaceAll("\\h", " ");
					if (usedFunctions.contains(lineCode) == false) {
						segment.addedCode.add(lineCode);
						// usedFunctions.add(lineCode); //Not used any more
					}

				}

				String removedCode = rs.getString("FromCode").trim();
				for (String lineCode : removedCode.split(";")) {
					lineCode = lineCode.trim().replaceAll("\\h", " ");
					if (usedFunctions.contains(lineCode) == false) {
						segment.removedCode.add(lineCode);
						// usedFunctions.add(lineCode);//Not used any more
					}

				}

				// In case we want only valid mapping
				if (onlyValidMapping == true && segment.isVaildMapping != 1) {
					continue;
				}
				// get all type of mapping
				if (segment.addedCode.size() > 0 && segment.removedCode.size() > 0 && onlyOneToOne == false) {
					listOfFunctions.add(segment);
					// only get one to one mapping
				} else if (segment.addedCode.size() == 1 && segment.removedCode.size() == 1 && onlyOneToOne == true) {

					listOfFunctions.add(segment);
				}
				// rows++;
			}
			// System.out.println("rows: " + rows);
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

		}

		return listOfFunctions;
	}
	
	public ArrayList<String> getCommitsWithMapping(int MigrationRuleID,int migrationMappingID) {
		ArrayList<String> listOfCommits = new ArrayList<String>();
		Statement stmt = null;
		try {
			Connection c = null;
			Class.forName("com.mysql.cj.jdbc.Driver");
			c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

			c.setAutoCommit(false);
			stmt = c.createStatement();
			// and MappingConfident.isVaildMapping=1
			 String sql="SELECT AppLink,SUBSTRING_INDEX(CommitID, '_', -1) as CommitID from MigrationSegments INNER JOIN Repositories on MigrationSegments.AppID=Repositories.AppID WHERE FromCode like (SELECT concat('%',FromCode,'%') from MigrationMapping WHERE MigrationMappingID="+ migrationMappingID  + ") and ToCode like (SELECT concat('%',MigrationMapping.ToCode,'%') from MigrationMapping WHERE MigrationMappingID="+ migrationMappingID +") and MigrationRuleID="+ MigrationRuleID +" group by AppLink,SUBSTRING_INDEX(CommitID, '_', -1)";
 
			ResultSet rs = stmt.executeQuery(sql);
		 
			while (rs.next()) {

				listOfCommits.add(rs.getString("AppLink").trim() +  "/commit/"+ rs.getString("CommitID").trim()); 
			 
 
			}
			// System.out.println("rows: " + rows);
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

		}

		return listOfCommits;
	}

	public void getMigrationRules() {

		Statement stmt = null;
		try {
			Connection c = null;
			Class.forName("com.mysql.cj.jdbc.Driver");
			c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

			c.setAutoCommit(false);
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT FromLibrary, FromCode, ToLibrary,  ToCode   FROM MigrationMapping inner join  MigrationRules where MigrationMapping.MigrationRuleID= MigrationRules.ID;");
			System.out.println("MigrationRuleID\tFromCode\tToCode");

			while (rs.next()) {

				System.out.println("From:" + rs.getString("FromLibrary") + "-->\n" + rs.getString("FromCode") + "\nTo:"
						+ rs.getString("ToLibrary") + "-->\n" + rs.getString("ToCode") + "\n**********\n");

			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

		}

	}

	public static void main(String args[]) {
		System.out.println("list of library mapping");
		new MigrationMappingDB().getMigrationRules();

	}
}

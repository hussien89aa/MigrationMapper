package com.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.library.source.MigratedLibraries;
import com.algorithims.sa.*;
import com.project.settings.DatabaseLogin;
import com.segments.build.Segment;

public class MappingConfident {
	public void addMappingConfident(Segment segment, ConfidentType confidentType) {

		int migrationMappingID = new MigrationMappingDB().getMigrationMappingID(segment);
		if (migrationMappingID == 0) {
			System.err.println("Cannot find Migration Mapping for " + segment.removedCode.toString() + " to "
					+ segment.addedCode.toString());
			return;
		}

		Statement stmt = null;
		try {
			Connection c = null;
			Class.forName("com.mysql.cj.jdbc.Driver");
			c = DriverManager.getConnection(DatabaseLogin.url, DatabaseLogin.username, DatabaseLogin.password);

			c.setAutoCommit(false);
			stmt = c.createStatement();
			String sql = "INSERT INTO MappingConfident (MigrationMappingID,ConfidentTypeID,ConfidentValue) "
					+ "VALUES (" + migrationMappingID + "," + confidentType.ordinal() + "," + segment.frequency + ");";
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		System.out.println("Records created successfully");
	}

	// return 0 if not found
	public int isVaildMapping(int MigrationMappingID) {
		int isVaildMapping = -1;
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
			String sql = "SELECT isVaildMapping from MappingConfident WHERE MigrationMappingID=" + MigrationMappingID
					+ " LIMIT 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				isVaildMapping = rs.getInt("isVaildMapping");
				break;
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

		}

		return isVaildMapping;
	}
}

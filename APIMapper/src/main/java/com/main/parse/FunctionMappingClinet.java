package com.main.parse;

import java.util.ArrayList;
import java.util.LinkedList;

import com.algorithims.sa.SubstitutionAlgorithm;
import com.database.mysql.MigrationRule;
import com.database.mysql.MigrationRuleDB;
import com.database.mysql.MigrationSegmentsDB;
import com.database.mysql.RepositoriesDB;
import com.library.source.MigratedLibraries;
import com.segments.build.Segment;

/*
 * This clinet using SA to map functions of two libraries
 */
public class FunctionMappingClinet {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		 new FunctionMappingClinet().run();
	}
	
	void run() {
		
		RepositoriesDB repositoriesDB = new RepositoriesDB();

		LinkedList<MigrationRule> migrationRules = new MigrationRuleDB().getMigrationRulesWithoutVersion(1);
		for (MigrationRule migrationRule : migrationRules) {
			 MigratedLibraries.ID = migrationRule.ID;
			
			ArrayList<Segment> segmentList = new MigrationSegmentsDB().getSegmentsObj(migrationRule.ID);
			new SubstitutionAlgorithm(segmentList).start();
			
		}
		
		
	}

}

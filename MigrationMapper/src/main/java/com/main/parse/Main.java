package com.main.parse;

import java.util.Scanner;

import com.algorithims.sa.*;
import com.project.settings.AppSettings;
import com.project.settings.ProjectType;

public class Main {

	public static void main(String[] args) {

		Scanner scanner = new Scanner(System.in);
		System.out.println(
				"Enter type of projects that you want to Scan:\n0 - Maven projects ( pom.xml) \n1 - Android Projects (build.gradle) ");
		int projectType = scanner.nextInt();
		switch (projectType) {
		case 0:
			AppSettings.projectType = ProjectType.Java;
			break;
		case 1:
			AppSettings.projectType = ProjectType.Android;
			break;
		default:
			AppSettings.projectType = ProjectType.Java;
			break;
		}

		// 1 Collection
		new CollectorClient().startOnlineSearch();
		// 2- Find migration rule
		new MigrationRulesClient().start();
		// 3- Find code segments
		new DetectorClient().start();
		// 4- Collect Docs
		new DocManagerClient().run();

		// 5- Print Fragments results as HTML 
		new FragmentDocsMapperClient().run();
		
		// 6- Apply SA algorithm
		new FunctionMappingClient().run();
		 
		// 7- Print Method mapping results as HTML 
		new MethodsDocsMapperClient().run();
		

	}

}

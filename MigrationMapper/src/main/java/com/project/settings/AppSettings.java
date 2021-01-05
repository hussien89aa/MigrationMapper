package com.project.settings;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

import com.mysql.cj.xdevapi.JsonParser;

public class AppSettings {
	// Type of the project we want to test
	public static ProjectType projectType = ProjectType.Java;
	public static boolean isTest = false; // Make this true when you run test client
	public static boolean isUsingLD = true; // set if our search using library doumenation or not
	
	static public void loadAppSettings() {
		 
	        try {   
	    
	            String jsonString = new String(Files.readAllBytes(Paths.get("data/config.json")), StandardCharsets.UTF_8);
	            JSONObject obj = new JSONObject(jsonString);
	            JSONObject dbConnectionobj = obj.getJSONObject("dbConnection");
	    		DatabaseLogin.url = dbConnectionobj.getString("url");
	    		DatabaseLogin.username = dbConnectionobj.getString("userName");
	    		DatabaseLogin.password = dbConnectionobj.getString("password");
	    		
	    		JSONObject githubConnectionobj = obj.getJSONObject("githubConnection");
	    		GithubLogin.userName = githubConnectionobj.getString("developerToken");
	    		
	    	
	    		
	    		if(DatabaseLogin.username.contentEquals("Add user name here") || 
	    				DatabaseLogin.password.contentEquals("Add password here")  || 
	    				GithubLogin.userName.contentEquals("Add Github developer kep here") ) {
	    			System.err.println(" The database and github info in config.json didnot setup correctly yet. Please set up the correct information and rerun the tool");
	    			
	    		}else {
	    			System.out.println("=== Loaded setting info ===");
		    		System.out.println("DatabaseLogin.url ="+ DatabaseLogin.url);
		    		System.out.println("DatabaseLogin.username ="+ DatabaseLogin.username);
		    		System.out.println("DatabaseLogin.password ="+ DatabaseLogin.password);
		    		System.out.println("GithubLogin.userName ="+ GithubLogin.userName);
		    		System.out.println("==========================");
	    		}
				
			} catch (Exception e) {
				System.err.println("Cannot load database and github connection info, make sure config.json is there and correct ");
				System.err.println( e.getMessage());
				System.exit(0);
			}
		
	}
	
	public static void main(String[] args) {
		
		loadAppSettings();
	}

}

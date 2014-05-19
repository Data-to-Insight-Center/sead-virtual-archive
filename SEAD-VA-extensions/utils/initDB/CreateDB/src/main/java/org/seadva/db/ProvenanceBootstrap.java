package org.seadva.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class ProvenanceBootstrap {

	public static void init() throws SQLException{
		createProvTable();
	}
	private static final String PROVENANCE_TBL = "PROVENANCE";
	private static final String EVENT_TBL = "EVENT";
	private static void createProvTable() throws SQLException{
		
			Connection conn = InitDerby.dbInstance.getConnection();
	        
	        Statement pst = conn.createStatement();
	        
	        //Drop table
	        String dropTable = "DROP TABLE "+ PROVENANCE_TBL;
	        try {
				pst.executeUpdate(dropTable);
			} catch (SQLException e) {
				System.out.println("Table "+PROVENANCE_TBL +" was created.");
			}
	        
	        dropTable = "DROP TABLE "+ EVENT_TBL;
	        try {
				pst.executeUpdate(dropTable);
			} catch (SQLException e) {
				System.out.println("Table "+EVENT_TBL +" was created.");
			}
	        
	        
	        //Create table
	        String createTable = "CREATE TABLE " + PROVENANCE_TBL +
                   " (id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                   " datasetId VARCHAR(255)," +
                   " datasetTitle VARCHAR(255)," +
                   " submitterId VARCHAR(255)," +
                   " sipId VARCHAR(255)," +
                   " wfInstanceId VARCHAR(255)," +
                   " date timestamp," +
                   " status VARCHAR(255)," +
                   " PRIMARY KEY ( id ))";
	        
	        int result = pst.executeUpdate(createTable);
	        
	        createTable = "CREATE TABLE " + EVENT_TBL +
	                   " (eventId INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
	                   " sipId VARCHAR(255)," +
	                   " eventType VARCHAR(255)," +
	                   " percentage INTEGER," +
	                   " date timestamp, " +
	                   " status VARCHAR(255)," +
                       " detail VARCHAR(1023)," +
	                   " PRIMARY KEY ( eventId ))";
	        result = pst.executeUpdate(createTable);
	        
          
	        //User tables
	        //crop and create tables
	        
            pst.close();
            
            conn.close();
        
	}
}

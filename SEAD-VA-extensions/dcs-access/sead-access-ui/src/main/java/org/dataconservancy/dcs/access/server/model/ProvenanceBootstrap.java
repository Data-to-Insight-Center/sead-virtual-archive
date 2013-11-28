/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.dcs.access.server.model;

import java.sql.Connection;
import java.sql.Statement;


public class ProvenanceBootstrap {

	public static void init(){
		//create database
		//create tables
		createProvTable();
	}
	static DatabaseSingleton dbInstance;
	private static final String PROVENANCE_TBL = "PROVENANCE";
	private static final String EVENT_TBL = "EVENT";
	private static void createProvTable(){
		try
        {
			if(dbInstance==null)
			dbInstance = DatabaseSingleton.getInstance("");
	
			
	        Connection conn = dbInstance.getConnection();
	        
	        Statement pst = conn.createStatement();
	        
	        //Drop table
	        String dropTable = "DROP TABLE "+ PROVENANCE_TBL;
	        pst.executeUpdate(dropTable);
	        
	        dropTable = "DROP TABLE "+ EVENT_TBL;
	        pst.executeUpdate(dropTable);
	        
	        
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
	                   " PRIMARY KEY ( eventId ))";
	        result = pst.executeUpdate(createTable);
	        
          
	        //User tables
	        //crop and create tables
	        
            pst.close();
            
            conn.close();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
	}
}

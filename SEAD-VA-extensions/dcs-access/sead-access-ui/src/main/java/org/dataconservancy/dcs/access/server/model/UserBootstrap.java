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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.dataconservancy.dcs.access.server.UserServiceImpl;
import org.dataconservancy.dcs.access.shared.Person;
import org.dataconservancy.dcs.access.shared.RegistrationStatus;
import org.dataconservancy.dcs.access.shared.Role;


public class UserBootstrap {

	public static void init(){
		//create database
		//create tables
		createPersonTable();
//		viewPersonTable();
	}
	static DatabaseSingleton dbInstance;
	private static final String PERSON_TBL = "users";
	private static void createPersonTable(){
		try
        {
			if(dbInstance==null)
			dbInstance = DatabaseSingleton.getInstance("");
	
			
	        Connection conn = dbInstance.getConnection();
	        
	        Statement pst = conn.createStatement();
	        
	        //Drop table
	        String dropTable = "DROP TABLE "+ PERSON_TBL;
	        pst.executeUpdate(dropTable);
	        
	        //Create table
	        String createTable = "CREATE TABLE " + PERSON_TBL +
                   " (FIRSTNAME VARCHAR(255)," +
                   " LASTNAME VARCHAR(255)," +
                   " EMAILADDRESS VARCHAR(255)," +
                   " PASSWORD VARCHAR(255)," +
                   " REGSTATUS VARCHAR(255)," +
                   " ROLE VARCHAR(255)," +
                   " PRIMARY KEY ( EMAILADDRESS ))";
	        
	        int result = pst.executeUpdate(createTable);
	        //Insert admin
	        
	        String query = "INSERT INTO " + PERSON_TBL +
	           		" VALUES(?,?,?,?,?,?)";
	        PreparedStatement pStmnt = conn.prepareStatement(query);
	        
	        pStmnt.setString(1,"SEAD");
	        pStmnt.setString(2,"Administrator");
	        pStmnt.setString(3,"seadva@gmail.com");
	        pStmnt.setString(4,new UserServiceImpl().hashPassword("password"));
	        pStmnt.setString(5,RegistrationStatus.APPROVED.getPrefix());
	        pStmnt.setString(6,Role.ROLE_ADMIN.getName());
            
            result = pStmnt.executeUpdate();
	        pst.close();
            
            conn.close();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
	}
	
	private static void viewPersonTable(){
		try
        {
			if(dbInstance==null)
			dbInstance = DatabaseSingleton.getInstance("");
	
			
	        Connection conn = dbInstance.getConnection();
	        
	        Statement stmt = conn.createStatement();

	        String sql = "SELECT * FROM "+PERSON_TBL;
	        ResultSet results = stmt.executeQuery(sql);
	        while(results.next())
	         {
	        	 System.out.println(results.getString("FIRSTNAME"));
	        	 System.out.println(results.getString("LASTNAME"));
	        	 System.out.println(results.getString("EMAILADDRESS"));
	        	 System.out.println(results.getString("PASSWORD"));
	        	 System.out.println(results.getString("ROLE"));
	        	 System.out.println(results.getString("REGSTATUS"));
	         }
	        conn.close();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
	}
}

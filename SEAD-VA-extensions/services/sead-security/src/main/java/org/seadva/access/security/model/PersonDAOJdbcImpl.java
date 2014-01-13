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

package org.seadva.access.security.model;

import org.seadva.access.security.RegistrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersonDAOJdbcImpl  implements PersonDAO {

	private static Statement stmt = null;
   
	DatabaseSingleton dbInstance;
	
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
	private static final String PERSON_TBL = "users";
	
	public PersonDAOJdbcImpl(String databasePath) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		dbInstance = DatabaseSingleton.getInstance(databasePath);
	}
	
	@Override
	public Person selectPerson(String emailAddress) {
		log.debug("Selecting person with email address of {}", emailAddress);
		String query = "SELECT * FROM " + PERSON_TBL
				+ " WHERE EMAILADDRESS = ?";

		Person person =null;
		try{
	 	
		 Connection conn = dbInstance.getConnection();
	     PreparedStatement pst = conn.prepareStatement(query);
	        
          pst.setString(1,emailAddress);
     
         ResultSet results = pst.executeQuery();
         
         while(results.next())
         {
        	 person = new Person();
        	 person.setFirstName(results.getString("FIRSTNAME"));
        	 person.setLastName(results.getString("LASTNAME"));
        	 person.setEmailAddress(results.getString("EMAILADDRESS"));
        	 person.setPassword(results.getString("PASSWORD"));
        	 person.setRole(Role.fromString(results.getString("ROLE")));
        	 if(results.getString("REGSTATUS").equalsIgnoreCase("pending"))
        		 person.setRegistrationStatus(RegistrationStatus.PENDING);
        	 else if(results.getString("REGSTATUS").equalsIgnoreCase("approved"))
        		 person.setRegistrationStatus(RegistrationStatus.APPROVED);
         }
         conn.close();
		}
		catch(Exception e){e.printStackTrace();}
         
		return person;

	}

	

	@Override
	public void insertPerson(Person person) {
		log.debug("Insert Person {} into DB" , person);
		String query = "INSERT INTO " + PERSON_TBL +
           		" VALUES(?,?,?,?,?,?)";

        if (person.getRegistrationStatus() == null) {
            throw new RuntimeException("Person " + person + " must have a non-null registration status.");
        }
        try
        {
        	

   	           //Get a connection
	        Connection conn = dbInstance.getConnection(); 
	        
	        //stmt = conn.createStatement();
	        PreparedStatement pst = conn.prepareStatement(query);
        
            pst.setString(1,person.getFirstName());
            pst.setString(2,person.getLastName());
            pst.setString(3,person.getEmailAddress());
            pst.setString(4,person.getPassword());
            if(person.getRegistrationStatus()==RegistrationStatus.PENDING)
        	    pst.setString(5,"pending");
            else if(person.getRegistrationStatus()==RegistrationStatus.APPROVED)
         	    pst.setString(5,"approved");
            pst.setString(6,""); 
            int result = pst.executeUpdate();
          
            pst.close();
            
            conn.close();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
	}

	@Override
	public List<Person> getAllUsers(){
		log.debug("Selecting all users");
		String query = "SELECT * FROM " + PERSON_TBL;

		List<Person> people = new ArrayList<Person>();
		 
		try{
		  //Get a connection
	      Connection conn = dbInstance.getConnection();
	      
	      stmt = conn.createStatement();
	         
         ResultSet results = stmt.executeQuery(query);
         
        
         
         while(results.next())
         {
        	 Person person = new Person();
        	 person.setFirstName(results.getString("FIRSTNAME"));
        	 person.setLastName(results.getString("LASTNAME"));
        	 person.setEmailAddress(results.getString("EMAILADDRESS"));
        	 person.setPassword(results.getString("PASSWORD"));
        	 person.setRole(Role.fromString(results.getString("ROLE")));
        	 person.setRegistrationStatus(RegistrationStatus.fromString(results.getString("REGSTATUS")));
        	 
        	 people.add(person);
         }
         conn.close();
		}
		catch(Exception e){e.printStackTrace();}
         
		return people;
	
	}
	


/*
	@Override
	public void deletePerson(String emailAddress) {
		log.debug("Deleting person with email address of {}", emailAddress);
		String query = "DELETE FROM " + PERSON_TBL + " WHERE EMAIL_ADDRESS = ?";
		jdbcTemplate.update(query, new Object[] { emailAddress });

	}

	/**
	 * Update a person record given a person with matching Email address Email
	 * Address is Primary Key and cannot be updated
	 */
	@Override
	public void updatePerson(Person person) {
		log.debug("Updating person {}", person);
		String query = "UPDATE " + PERSON_TBL + " SET FIRSTNAME = ? "
				+ "   , LASTNAME = ?" + " , PASSWORD = ?"
				+ "   , REGSTATUS = ? , ROLE = ? " + " WHERE EMAILADDRESS = ? ";
		
        try
        {
        	

   	 
	        Connection conn = dbInstance.getConnection();
	        
	        //stmt = conn.createStatement();
	        PreparedStatement pst = conn.prepareStatement(query);
        
            pst.setString(1,person.getFirstName());
            pst.setString(2,person.getLastName());
            
            pst.setString(3,person.getPassword());
            
            pst.setString(4,person.getRegistrationStatus().getPrefix());
            pst.setString(5, person.getRole().getName());
            
            
            pst.setString(6,person.getEmailAddress());
            
            int result = pst.executeUpdate();
          
          
            //stmt.close();
            pst.close();
            conn.close();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }

	}

}

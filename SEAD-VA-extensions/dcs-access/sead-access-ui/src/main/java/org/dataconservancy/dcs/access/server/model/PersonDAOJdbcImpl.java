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

import org.dataconservancy.dcs.access.server.database.DBConnectionPool;
import org.dataconservancy.dcs.access.server.database.ObjectPool;
import org.dataconservancy.dcs.access.shared.Person;
import org.dataconservancy.dcs.access.shared.RegistrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//import org.dataconservancy.dcs.access.server.model.Role;

public class PersonDAOJdbcImpl  implements PersonDAO {

	
   
//	DatabaseSingleton dbInstance;
	
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
	private static final String PERSON_TBL = "users";
	
	RoleDAO roleDAO;
	
	protected ObjectPool<Connection> connectionPool = null;
	protected Connection getConnection() throws SQLException {
	     return connectionPool.getEntry();
	}
	
	public PersonDAOJdbcImpl(String configPath) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		connectionPool = DBConnectionPool.getInstance();
	//	dbInstance = DatabaseSingleton.getInstance(configPath);
		roleDAO = new RoleDAOJdbcImpl(configPath);
	}
	
	@Override
	public Person selectPerson(String emailAddress) {
		log.debug("Selecting person with email address of {}", emailAddress);
		String query = "SELECT * FROM " + PERSON_TBL
				+ " WHERE EMAILADDRESS = ?";
		
		Person person =null;
		
		Connection conn = null;
		PreparedStatement pst = null;
		try{
	 	
			conn = getConnection();
			pst = conn.prepareStatement(query);
		// Connection conn = dbInstance.getConnection();
	    // PreparedStatement pst = conn.prepareStatement(query);
	        
         pst.setString(1,emailAddress);
     
         ResultSet results = pst.executeQuery();
         int role_id = 0;
         while(results.next())
         {
        	 person = new Person();
        	 person.setFirstName(results.getString("FIRSTNAME"));
        	 person.setLastName(results.getString("LASTNAME"));
        	 person.setEmailAddress(results.getString("EMAILADDRESS"));
        	 person.setPassword(results.getString("PASSWORD"));
        	 person.setVivoId(results.getString("VIVOID"));
        	 person.setRegistryId(results.getString("REGISTRYID"));
        	 role_id = results.getInt("ROLEID");
        	 if(results.getString("REGSTATUS").equalsIgnoreCase("pending"))
        		 person.setRegistrationStatus(RegistrationStatus.PENDING);
        	 else if(results.getString("REGSTATUS").equalsIgnoreCase("approved"))
        		 person.setRegistrationStatus(RegistrationStatus.APPROVED);
        	 person.setRole(roleDAO.getRole(role_id));
         }

         pst.close();
		} catch(Exception e){e.printStackTrace();}
		finally {
	         if (pst != null) {
	             try {
	            	 pst.close();
	             } catch (SQLException e) {
	                 log.warn("Unable to close statement", e);
	             }
	             pst = null;
	         }
	         connectionPool.releaseEntry(conn);
		}
         
		return person;

	}

	

	@Override
	public void insertPerson(Person person) {
		log.debug("Insert Person {} into DB" , person);
		String query = "INSERT INTO " + PERSON_TBL +
           		" (FIRSTNAME, LASTNAME, EMAILADDRESS, PASSWORD, REGSTATUS, ROLEID, VIVOID) VALUES(?,?,?,?,?,?,?)";

        if (person.getRegistrationStatus() == null) {
            throw new RuntimeException("Person " + person + " must have a non-null registration status.");
        }

		Connection conn = null;
		PreparedStatement pst = null;
		try{
	 	
			conn = getConnection();
			pst = conn.prepareStatement(query);
			
            pst.setString(1,person.getFirstName());
            pst.setString(2,person.getLastName());
            pst.setString(3,person.getEmailAddress());
            pst.setString(4,person.getPassword());
            if(person.getRegistrationStatus()== RegistrationStatus.PENDING)
        	    pst.setString(5,"pending");
            else if(person.getRegistrationStatus()== RegistrationStatus.APPROVED)
         	    pst.setString(5,"approved");
            pst.setInt(6,roleDAO.getRoleIdByName(person.getRole().getName()));
            pst.setString(7, person.getVivoId()); 
            int result = pst.executeUpdate();
            
            pst.close();
		} catch(Exception e){e.printStackTrace();}
		finally {
	         if (pst != null) {
	             try {
	            	 pst.close();
	             } catch (SQLException e) {
	                 log.warn("Unable to close statement", e);
	             }
	             pst = null;
	         }
	         connectionPool.releaseEntry(conn);
		}
	}

	@Override
	public List<Person> getAllUsers(String key, String constraintValue, String type){
		log.debug("Selecting all users");
		String query = "SELECT * FROM " + PERSON_TBL;
		
		if(key!=null && constraintValue!=null){
			if(type!=null && type.equalsIgnoreCase("int"))
				query += " where "+key+"="+constraintValue;
			else
				query += " where "+key+"='"+constraintValue+"'";
		}

		List<Person> people = new ArrayList<Person>();
		 

		Connection conn = null;
		Statement pst = null;
		try{
	 	
		 conn = getConnection();
		 pst = conn.createStatement();
         
         ResultSet results = pst.executeQuery(query);
         
        
         
         while(results.next())
         {
        	 Person person = new Person();
        	 person.setFirstName(results.getString("FIRSTNAME"));
        	 person.setLastName(results.getString("LASTNAME"));
        	 person.setEmailAddress(results.getString("EMAILADDRESS"));
        	 person.setPassword(results.getString("PASSWORD"));
        	 person.setRole(roleDAO.getRole(results.getInt("ROLEID")));
        	 person.setVivoId(results.getString("VIVOID"));
        	 person.setRegistryId(results.getString("REGISTRYID"));
        	 person.setRegistrationStatus(RegistrationStatus.fromString(results.getString("REGSTATUS")));
        	 
        	 people.add(person);
         }
         pst.close();
		} catch(Exception e){e.printStackTrace();}
		finally {
	         if (pst != null) {
	             try {
	            	 pst.close();
	             } catch (SQLException e) {
	                 log.warn("Unable to close statement", e);
	             }
	             pst = null;
	         }
	         connectionPool.releaseEntry(conn);
		}
		return people;
	
	}


/*
	@Override
	public void deletePerson(String emailAddress) {
		log.debug("Deleting person with email address of {}", emailAddress);
		String query = "DELETE FROM " + PERSON_TBL + " WHERE EMAIL_ADDRESS = ?";
		jdbcTemplate.update(query, new Object[] { emailAddress });

	}        */

	/**
	 * Update a person record given a person with matching Email address Email
	 * Address is Primary Key and cannot be updated
	 */
	@Override
	public void updatePerson(Person person) {
		log.debug("Updating person {}", person);
		
		
		Connection conn = null;
		PreparedStatement pst = null;
		try{
			String query = "UPDATE " + PERSON_TBL + " SET "
					+ " REGSTATUS = ? , REGISTRYID = ?, ROLEID = ?" + " WHERE EMAILADDRESS = ? ";
				
	 	
			conn = getConnection();
			pst = conn.prepareStatement(query);

			
           
            //Updating Role and Registration Status, provided the email address (id in user database)
            pst.setString(1,person.getRegistrationStatus().getPrefix());
            pst.setString(2, person.getRegistryId());
            pst.setInt(3, roleDAO.getRoleIdByName(person.getRole().getName()));
            pst.setString(4,person.getEmailAddress());
            
            int result = pst.executeUpdate();
          
          
            pst.close();
		} catch(Exception e){e.printStackTrace();}
		finally {
	         if (pst != null) {
	             try {
	            	 pst.close();
	             } catch (SQLException e) {
	                 log.warn("Unable to close statement", e);
	             }
	             pst = null;
	         }
	         connectionPool.releaseEntry(conn);
		}

	}

}

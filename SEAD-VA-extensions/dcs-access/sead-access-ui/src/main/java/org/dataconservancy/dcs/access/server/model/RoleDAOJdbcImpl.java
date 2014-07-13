/*
 * Copyright 2014 The Trustees of Indiana University
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
import org.dataconservancy.dcs.access.shared.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDAOJdbcImpl  implements RoleDAO {

	 
    private final Logger log = LoggerFactory.getLogger(this.getClass());
	private static final String ROLE_TBL = "roles";
	
	protected ObjectPool<Connection> connectionPool = null;
	protected Connection getConnection() throws SQLException {
	     return connectionPool.getEntry();
	}
	public RoleDAOJdbcImpl(String configPath) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		//dbInstance = DatabaseSingleton.getInstance(configPath);
		connectionPool = DBConnectionPool.getInstance();
	}
	

	@Override
	public List<Role> getAllRoles(){
		log.debug("Selecting all roles");
		String query = "SELECT * FROM " + ROLE_TBL;

		List<Role> roles = new ArrayList<Role>();
		 
		Connection conn = null;
		 PreparedStatement pst = null;
		 try{
	 	
			conn = getConnection();
		  Statement stmt = conn.createStatement();
	         
         ResultSet results = stmt.executeQuery(query);
         
        
         
         while(results.next())
        	 roles.add(Role.fromString(results.getString("ROLE")));

         conn.close();
		}
		catch(Exception e){e.printStackTrace();}
         
		return roles;
	}


	@Override
	public Role getRole(int role_id) {
		String query = "SELECT * FROM " + ROLE_TBL
				+ " WHERE ROLEID = ?";
		
		Role role = Role.ROLE_NONSEADUSER;
		Connection conn = null;
		 PreparedStatement pst = null;
		 try{
	 	
			conn = getConnection();
			pst = conn.prepareStatement(query);
	        
	      pst.setInt(1,role_id);
	         
	      ResultSet results = pst.executeQuery();
         while(results.next())
        	 role = Role.fromString(results.getString("ROLE"));

         conn.close();
		}
		catch(Exception e){e.printStackTrace();}
         
		return role;
	}
	
	@Override
	public int getRoleIdByName(String roleName) {
		String query = "SELECT * FROM " + ROLE_TBL
				+ " WHERE ROLE = ?";
		
		int roleId  =0;
		Connection conn = null;
		PreparedStatement pst = null;
		 try{
	 	
			conn = getConnection();
			pst = conn.prepareStatement(query);
	        
	      pst.setString(1, roleName);
	         
	      ResultSet results = pst.executeQuery();
          while(results.next())
        	 roleId = results.getInt("ROLEID");

         conn.close();
		}
		catch(Exception e){e.printStackTrace();}
         
		return roleId;
	}
	

}

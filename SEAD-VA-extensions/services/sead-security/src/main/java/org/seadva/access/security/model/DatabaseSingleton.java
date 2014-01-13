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

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;


public class DatabaseSingleton {
	   private static DatabaseSingleton instance = null;
	   
	   
	   
	   private static int maxConnections =5; 
	   private GenericObjectPool connectionPool = null;
	   DataSource dataSource;
	   
	  
	   public static String dbUrl;
	   public static String driver;
	   
	   protected DatabaseSingleton() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		   
		   Class.forName(this.driver).newInstance();

	        connectionPool = new GenericObjectPool();
	        connectionPool.setMaxActive(10);

	        Properties props = new Properties();

	        ConnectionFactory cf =
	                new DriverConnectionFactory(new org.apache.derby.jdbc.EmbeddedDriver(),
	                        dbUrl,
	                		props);
	        PoolableConnectionFactory pcf =
	                new PoolableConnectionFactory(cf, connectionPool,
	                        null, null, false, true);
	        
	        this.dataSource = new PoolingDataSource(connectionPool);

		}
	  
	   public static DatabaseSingleton getInstance(String databasePath) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
	      if(instance == null) {
              dbUrl = databasePath;
              driver  = "org.apache.derby.jdbc.EmbeddedDriver";
	    	  instance = new DatabaseSingleton();
	      }
	      return instance;
	   }
	   
	   
	  
	   public Connection getConnection() throws Exception {
		   
		   return dataSource.getConnection();
	   }
}



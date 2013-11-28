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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;



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
	  
	   public static DatabaseSingleton getInstance(String configPath) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
	      if(instance == null) {
	    	String fileContents = readFile(configPath);
	    	  
            String[] pairs = fileContents.trim().split(
                    "\\w*\n|\\=\\w*");

            for (int i = 0; i + 1 < pairs.length;) {
                String name = pairs[i++].trim();
                String value = pairs[i++].trim();


                if (name.equals("dbUrl")) {
                    dbUrl = value;
                }
            }
                        
	    	  driver  = "org.apache.derby.jdbc.EmbeddedDriver";
	    	  instance = new DatabaseSingleton();
	      }
	      return instance;
	   }
	   
	   
	  
	   public Connection getConnection() throws Exception {
		   
		   return dataSource.getConnection();
	   }
	   
	   private static String readFile(String path){
		   if(!(new File(path)).exists())
			   return "";
		   BufferedReader br = null;
		   String result = "";
		   
			try {
	 
				String sCurrentLine;
	 
				br = new BufferedReader(new FileReader(path));
	 
				while ((sCurrentLine = br.readLine()) != null) {
					result += sCurrentLine+"\n";
				}
	 
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null)br.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			return result;
	   }
}



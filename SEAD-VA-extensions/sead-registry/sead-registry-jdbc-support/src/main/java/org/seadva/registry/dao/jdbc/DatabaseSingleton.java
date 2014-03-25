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

package org.seadva.registry.dao.jdbc;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


public class DatabaseSingleton {
	   private static DatabaseSingleton instance = null;
	   
	   
	   
	   private static int maxConnections =5; 
	   private GenericObjectPool connectionPool = null;
	   DataSource dataSource;
	   
	  
	   public static String dbUrl;
       private static String user;
       private static String password;
	   public static String driver;
	   
	   protected DatabaseSingleton() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

           Class.forName(this.driver).newInstance();

	        connectionPool = new GenericObjectPool();
	        connectionPool.setMaxActive(10);

	        Properties props = new Properties();
            props.setProperty("user",user);
            props.setProperty("password",password);


	        ConnectionFactory cf =
	                new DriverConnectionFactory(new com.mysql.jdbc.Driver(),
	                        dbUrl,
	                		props);
	        PoolableConnectionFactory pcf =
	                new PoolableConnectionFactory(cf, connectionPool,
	                        null, null, false, true);
	        
	        this.dataSource = new PoolingDataSource(connectionPool);

		}
	  
	   public static DatabaseSingleton getInstance() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException {
	      if(instance == null) {
              InputStream inputStream =
                DatabaseSingleton.class.getResourceAsStream(
                              "./Config.properties"
                      );
              StringWriter writer = new StringWriter();
              IOUtils.copy(inputStream, writer);

              String result = writer.toString();
              String[] pairs = result.trim().split(
                      "\n|\\=");


              for (int i = 0; i + 1 < pairs.length;) {
                  String name = pairs[i++].trim();
                  String value = pairs[i++].trim();
                  if(name.equalsIgnoreCase("user"))
                      user = value;
                  else  if(name.equalsIgnoreCase("password"))
                      password  =value;
                  else  if(name.equalsIgnoreCase("dbUrl"))
                      dbUrl  =value;
              }

              driver  = "com.mysql.jdbc.Driver";
              instance = new DatabaseSingleton();
          }
	      return instance;
	   }
	   
	   
	  
	   public Connection getConnection() throws Exception {
		   
		   return dataSource.getConnection();
	   }


}



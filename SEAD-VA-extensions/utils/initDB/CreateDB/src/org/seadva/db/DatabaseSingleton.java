package org.seadva.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.sql.PreparedStatement;
import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;


import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;

public class DatabaseSingleton {
	   private static DatabaseSingleton instance = null;
	   
	   
	   
	   private static int maxConnections =5; 
	   private GenericObjectPool connectionPool = null;
	   DataSource dataSource;
	   
	  
	   public static String dbUrl;
	   public static String driver;
	   
	   protected DatabaseSingleton(String path) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		   Class.forName(this.driver).newInstance();
		   connectionPool = new GenericObjectPool();
	       connectionPool.setMaxActive(10);
	       
	       Properties props = new Properties();

	       ConnectionFactory cf =
	                new DriverConnectionFactory(new org.apache.derby.jdbc.EmbeddedDriver(),
	                       "jdbc:derby:"+path+";create=true",
	                        props);
	       PoolableConnectionFactory pcf =
	                new PoolableConnectionFactory(cf, connectionPool,
	                        null, null, false, true);
	        
	       this.dataSource = new PoolingDataSource(connectionPool);

		}
	   
	   public static DatabaseSingleton getInstance(String path) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
	      if(instance == null) {
	    	  driver  = "org.apache.derby.jdbc.EmbeddedDriver";
	    	  instance = new DatabaseSingleton(path);
	      }
	      return instance;
	   }
	   
	   
	  
	   public Connection getConnection() throws SQLException{
		   
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



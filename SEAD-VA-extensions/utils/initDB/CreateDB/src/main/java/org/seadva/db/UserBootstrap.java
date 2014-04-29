package org.seadva.db;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class UserBootstrap {

	public static void init() throws SQLException{
		createPersonTable();
	}
	private static final String PERSON_TBL = "users";
	private static void createPersonTable() throws SQLException{

	        Connection conn = InitDerby.dbInstance.getConnection();
	        
	        Statement pst = conn.createStatement();
	        
	        //Drop table
	        String dropTable = "DROP TABLE "+ PERSON_TBL;
	        try {
				pst.executeUpdate(dropTable);
			} catch (SQLException e) {
				System.out.println("Table "+ PERSON_TBL +" was created.");
			}
	        
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
	        pStmnt.setString(4,hashPassword("password"));
	        pStmnt.setString(5,"approved");
	        pStmnt.setString(6,"admin");
            
            result = pStmnt.executeUpdate();
	        pst.close();
            
            conn.close();
	}
	 public static String hashPassword(String password)
	  {
	    String hashword = null;
	    try {
	      MessageDigest md5 = MessageDigest.getInstance("MD5");
	      md5.update(password.getBytes());
	      BigInteger hash = new BigInteger(1, md5.digest());
	      hashword = hash.toString(16);
	    }
	    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
	    {
	    }
	    return pad(hashword, 32, '0');
	  }
	 
	 private static String pad(String s, int length, char pad) {
		    StringBuffer buffer = new StringBuffer(s);
		    while (buffer.length() < length) { 
		      buffer.insert(0, pad);
		    }
		    return buffer.toString();
		  }
}

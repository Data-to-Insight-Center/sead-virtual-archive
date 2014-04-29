package org.seadva.db;

import java.sql.SQLException;

public class InitDerby {
	
	public static DatabaseSingleton dbInstance;
	
	public static void main(String[] args) {
		if(args.length<1){
			System.out.println("Please provide a path to database like this: initDerby /tmp/db");
			return;
		}
		if(dbInstance==null)
			try {
				dbInstance = DatabaseSingleton.getInstance(args[0]);
				ProvenanceBootstrap.init();
				UserBootstrap.init();
			}
            catch (SQLException e) {
				String msg = e.getMessage();
				if(e.getSQLState().equalsIgnoreCase("XJ041"))
					msg = "Directory "+args[0]+" already exists.";
				System.out.println("Sorry encountered an error:"+msg);
				System.exit(1);
			} catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        System.out.println("Created DB at "+args[0]);
				
	}
}

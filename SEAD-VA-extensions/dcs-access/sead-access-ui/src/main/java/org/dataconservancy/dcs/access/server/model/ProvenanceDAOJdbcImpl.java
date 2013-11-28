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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.access.server.util.ServerConstants;
import org.dataconservancy.dcs.access.server.util.StatusReader.Status;
import org.dataconservancy.dcs.access.shared.Event;
import org.dataconservancy.dcs.access.shared.ProvenanceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dataconservancy.dcs.access.shared.ProvenaceDataset;

public class ProvenanceDAOJdbcImpl  implements ProvenanceDAO {

	DatabaseSingleton dbInstance;
	
	
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
	private static final String PROVENANCE_TBL = "PROVENANCE";
	private static final String EVENT_TBL = "EVENT";
	
	public ProvenanceDAOJdbcImpl(String configPath) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		dbInstance = DatabaseSingleton.getInstance(configPath);
	}

	@Override
	public void insertProvenanceRecord(ProvenanceRecord provenanceRecord) {
		//check if prov record exists, if not create a new one or update the old one in both the tables as needed
		
        try
        {
        	Connection conn = dbInstance.getConnection(); 
        	String query = "SELECT STATUS FROM " + PROVENANCE_TBL
      				+ " WHERE SIPID = ?";
        	PreparedStatement pst = conn.prepareStatement(query);
      		  
        	pst.setString(1, provenanceRecord.getSipId());
           
            ResultSet results = pst.executeQuery();
            boolean update = false;
            boolean insert = true;
            
            while(results.next()){
            	String status = results.getString("STATUS");
          	  	if(status!=null)
          	  	{  
          		  if(org.dataconservancy.dcs.access.shared.Status.fromString(status)!=provenanceRecord.getStatus())//if record has changed, do update else update and insert are both false
          			  update = true;
          			  
          		  insert = false;
          	  	  break;
          	  	}
            }
            
    		if(insert){
    			insertSip(conn,provenanceRecord);
	            //for each event
    			for(Event event: provenanceRecord.getEvents()){
    				insertEvent(conn, provenanceRecord.getSipId(), event);
    			}

	            
    		}
    		else{ if(update){
    			String updateStmt = "UPDATE " + PROVENANCE_TBL +
						" SET DATE=?, status=? WHERE SIPID=?";
	

			    pst = conn.prepareStatement(updateStmt);
			
			    pst.setTimestamp(1, new Timestamp(provenanceRecord.getDate().getTime())
			    		//ServerConstants.dateFormat.format(provenanceRecord.getDate()))
			    		);
			    pst.setString(2, provenanceRecord.getStatus().getText());
			    pst.setString(3, provenanceRecord.getSipId());
			    
			    int result = pst.executeUpdate();
    		}
    		else{  
    		for(Event event: provenanceRecord.getEvents()){
				    //See if each event has to be updated or inserted
				    query = "SELECT * FROM " + EVENT_TBL
		      				+ " WHERE SIPID = ? and EVENTTYPE=?";
		        	pst = conn.prepareStatement(query);
		      		    
		            pst.setString(1, provenanceRecord.getSipId());
		            pst.setString(2, event.getEventType());
		           
		            results = pst.executeQuery();
		            boolean updateEvent = false;
		            int previousPercent = 0;
		            while(results.next()){
		          	  if(results.getString("eventId")!=null){
		          		  previousPercent = results.getInt("percentage");
		          		  updateEvent = true;
		          		  break;
		          	  }
		            }
		            if(!updateEvent){
		            	insertEvent(conn, provenanceRecord.getSipId(), event);
		            }
		            else{
		            	String updateStmt = "UPDATE " + EVENT_TBL +
					       		" SET percentage=?, DATE=?, STATUS=? WHERE SIPID=? AND EVENTTYPE=?";
					    
					    pst = conn.prepareStatement(updateStmt);
					    
					    int totalPercent = event.getEventPercent() + previousPercent;
					    pst.setInt(1, totalPercent);
					    pst.setTimestamp(2,  new Timestamp(event.getEventDate().getTime())
					    		//ServerConstants.dateFormat.format(event.getEventDate())
					    		);
					    
					    if(totalPercent==100)
				        	 pst.setString(3, Status.Completed.getText());
				         else
				        	 pst.setString(3, Status.Pending.getText());
					    pst.setString(4, provenanceRecord.getSipId());
					    pst.setString(5, event.getEventType());
					  
					    int result = pst.executeUpdate();
		            	
		            }
    		}
			    }
    		}
    		
    		pst.close();
    		conn.close();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
	}

	private void insertEvent(Connection conn, String sipId, Event event) throws SQLException{
		 String insertStmt = "INSERT INTO " + EVENT_TBL +
            		" (sipId, eventType, percentage, status, date) VALUES(?,?,?,?,?)";
         
		 PreparedStatement pst = conn.prepareStatement(insertStmt);
         
         pst.setString(1, sipId);
         pst.setString(2, event.getEventType());
         pst.setInt(3, event.getEventPercent());
         if(event.getEventPercent()==100)
        	 pst.setString(4, Status.Completed.getText());
         else
        	 pst.setString(4, Status.Pending.getText());
 		pst.setTimestamp(5,   new Timestamp(event.getEventDate().getTime())
 		//ServerConstants.dateFormat.format(event.getEventDate())
 		);
 		
        pst.executeUpdate();
	}
	
	private void insertSip(Connection conn, ProvenanceRecord provenanceRecord) throws SQLException{
		String insertStmt = "INSERT INTO " + PROVENANCE_TBL +
				" (datasetId, submitterId, sipId, status,date, wfInstanceId,datasetTitle) VALUES(?,?,?,?,?,?,?)";


		PreparedStatement pst = conn.prepareStatement(insertStmt);
	
		pst.setString(1, provenanceRecord.getDatasetId());
		pst.setString(2, provenanceRecord.getSubmitterId());
		pst.setString(3, provenanceRecord.getSipId());
		pst.setString(4, provenanceRecord.getStatus().getText());
		pst.setTimestamp(5,  new Timestamp(provenanceRecord.getDate().getTime())
				//ServerConstants.dateFormat.format(provenanceRecord.getDate())
				);
		pst.setString(6, provenanceRecord.getWfInstanceId());
		pst.setString(7, provenanceRecord.getDatasetTitle());

		pst.executeUpdate();
	}
	
	


	@Override
	public List<ProvenaceDataset> getProvenanceForSubmitter(String submitterId) {
		
		List<ProvenaceDataset> provDatasets = new ArrayList<ProvenaceDataset>();
		List<ProvenanceRecord> provenance = new ArrayList<ProvenanceRecord>();
		String query = "SELECT * FROM " + PROVENANCE_TBL
				+ " WHERE SUBMITTERID = ? ORDER BY DATASETID, WFINSTANCEID";
		String datasetId = null;
		String wfInstanceId = null;
		String datasetTitle = null;
	
		try{    
			Connection conn = dbInstance.getConnection();
			PreparedStatement pst = conn.prepareStatement(query);
	        
	         pst.setString(1,submitterId);
	     
	         ResultSet results = pst.executeQuery();
	         
	         String previousDatasetId = "";
	         String previousWfInstanceId = "";
	          		
	         
	         ProvenaceDataset provDataset = new ProvenaceDataset();
	         while(results.next())
	         {
	        	 ProvenanceRecord provenanceRecord = new ProvenanceRecord();
	        	 provenanceRecord.setSubmitterId(submitterId);
	        	 datasetId = results.getString("DATASETID");
	        	 provenanceRecord.setDatasetId(datasetId);
	        	 datasetTitle = results.getString("DATASETTITLE");
	        	 provenanceRecord.setDatasetTitle(datasetTitle);
	        	 wfInstanceId = results.getString("WFINSTANCEID");
	        	 provenanceRecord.setWfInstanceId(wfInstanceId);
	        	 provenanceRecord.setSipId(results.getString("SIPID"));
	        	 provenanceRecord.setDate(new Date(results.getTimestamp("DATE").getTime()));
	        	 String status = results.getString("STATUS");
	        	 if(Status.fromString(status)==Status.Pending)
	        		 provenanceRecord.setStatus(org.dataconservancy.dcs.access.shared.Status.Pending);
	        	 else if(Status.fromString(status)==Status.Failed)
	        		 provenanceRecord.setStatus(org.dataconservancy.dcs.access.shared.Status.Failed);
	        	 if(Status.fromString(status)==Status.Completed)
	        		 provenanceRecord.setStatus(org.dataconservancy.dcs.access.shared.Status.Completed);
	        	 
	            	 query = "SELECT * FROM " + EVENT_TBL
	         				+ " WHERE SIPID=?";
	         		
	            	 PreparedStatement pstEvent = conn.prepareStatement(query);
	         		 
	            	 pstEvent.setString(1,  provenanceRecord.getSipId());
	              
	                  ResultSet eventResults = pstEvent.executeQuery();
	                  while(eventResults.next()){
	                	  Event event = new Event();
	                	  event.setId(eventResults.getInt("eventId"));
	                	  event.setEventPercent(eventResults.getInt("percentage"));
	                	  event.setEventType(eventResults.getString("eventtype"));
	                	  event.setEventDate(
	                			  new Date(eventResults.getTimestamp("DATE").getTime()));
	                	  provenanceRecord.addEvent(event);
	                  }
	        	 
	             if(datasetId.equalsIgnoreCase(previousDatasetId)){
	            	 if(wfInstanceId.equalsIgnoreCase(previousWfInstanceId))
	            	 {
	            		 provenance.add(provenanceRecord); 
	            	 }
	            	 else{
	            		 provDataset.provRecordbyWf.put(previousWfInstanceId,provenance);
	            		 provenance = new ArrayList<ProvenanceRecord>();
	            		 provenance.add(provenanceRecord);
	            	 }
	             }
	             else{
	            	 if(provDataset.dataSetId!=null)
	            		 provDatasets.add(provDataset);
	            	 provDataset = new ProvenaceDataset();
	            	 provDataset.dataSetId = datasetId;
	            	 provDataset.datasetTitle = datasetTitle;
	            	 provenance = new ArrayList<ProvenanceRecord>();
	            	 provenance.add(provenanceRecord);
	             }
	             previousDatasetId = datasetId;
	             previousWfInstanceId = wfInstanceId;
	             if(datasetId!=null){
		        	 provDataset.provRecordbyWf.put(wfInstanceId,provenance);
		        	 provDatasets.add(provDataset);
		         }
	         }
	         
	         conn.close();
		}
		catch(Exception e){
			e.printStackTrace();
			
			throw new RuntimeException(e);
			}
         
		return provDatasets;
	}
	@Override
	public List<ProvenaceDataset> getProvForSubmitterWf(String submitterId, String wfInstanceId, Date latestDate) {
		
		List<ProvenaceDataset> provDatasets = new ArrayList<ProvenaceDataset>();
		List<ProvenanceRecord> provenance = new ArrayList<ProvenanceRecord>();
		String query = "SELECT * FROM " + PROVENANCE_TBL
				+ " WHERE SUBMITTERID = ? AND  WFINSTANCEID = ? AND DATE(DATE) > DATE(TIMESTAMP('"+ServerConstants.dateFormat.format(latestDate)
				.replace("T", " ").replace("Z", " ")+"')) ORDER BY DATASETID, WFINSTANCEID";//SQL
		String datasetId = null;
		String datasetTitle = null;

		
		try{
		 Connection conn = dbInstance.getConnection();
	     PreparedStatement pst = conn.prepareStatement(query);
	        
         pst.setString(1,submitterId);
         pst.setString(2,wfInstanceId);
         
         ResultSet results = pst.executeQuery();
         
         String previousDatasetId = "";
         String previousWfInstanceId = "";
          		
         ProvenaceDataset provDataset = new ProvenaceDataset();
         while(results.next())
         {
        	 ProvenanceRecord provenanceRecord = new ProvenanceRecord();
        	 provenanceRecord.setSubmitterId(submitterId);
        	 datasetId = results.getString("DATASETID");
        	 provenanceRecord.setDatasetId(datasetId);
        	 datasetTitle = results.getString("DATASETTITLE");
        	 provenanceRecord.setDatasetTitle(datasetTitle);
        	 provenanceRecord.setWfInstanceId(wfInstanceId);
        	 provenanceRecord.setSipId(results.getString("SIPID"));
        	 provenanceRecord.setDate(new Date(results.getTimestamp("DATE").getTime()));
        	 String status = results.getString("STATUS");
        	 if(Status.fromString(status)==Status.Pending)
        		 provenanceRecord.setStatus(org.dataconservancy.dcs.access.shared.Status.Pending);
        	 else if(Status.fromString(status)==Status.Failed)
        		 provenanceRecord.setStatus(org.dataconservancy.dcs.access.shared.Status.Failed);
        	 if(Status.fromString(status)==Status.Completed)
        		 provenanceRecord.setStatus(org.dataconservancy.dcs.access.shared.Status.Completed);
        	 
            	 //read from table
            	 query = "SELECT * FROM " + EVENT_TBL
         				+ " WHERE SIPID =?";
         		
            	 PreparedStatement pst2 = conn.prepareStatement(query);
         	        
            	 pst2.setString(1, provenanceRecord.getSipId());
                  
                  ResultSet eventResults = pst2.executeQuery();
                  while(eventResults.next()){
                	  Event event = new Event();
                	  event.setId(eventResults.getInt("eventId"));
                	  event.setEventPercent(eventResults.getInt("percentage"));
                	  event.setEventType(eventResults.getString("eventtype"));
                	  event.setEventDate(
                			  new Date(eventResults.getTimestamp("DATE").getTime())
                			  );
                	  provenanceRecord.addEvent(event);
                  }
                  
             if(datasetId.equalsIgnoreCase(previousDatasetId)){
            	 if(wfInstanceId.equalsIgnoreCase(previousWfInstanceId))
            	 {
            		 provenance.add(provenanceRecord); 
            	 }
            	 else{
            		 provDataset.provRecordbyWf.put(previousWfInstanceId,provenance);
            		 provenance = new ArrayList<ProvenanceRecord>();
            		 provenance.add(provenanceRecord);
            	 }
             }
             else{
            	 if(provDataset.dataSetId!=null)
            		 provDatasets.add(provDataset);
            	 provDataset = new ProvenaceDataset();
            	 provDataset.dataSetId = datasetId;
            	 provDataset.datasetTitle = datasetTitle;
            	 provenance = new ArrayList<ProvenanceRecord>();
            	 provenance.add(provenanceRecord);
             }
             previousDatasetId = datasetId;
             previousWfInstanceId = wfInstanceId;
            
         }
         if(datasetId!=null){
        	 provDataset.provRecordbyWf.put(wfInstanceId,provenance);
        	 provDatasets.add(provDataset);
         }
         conn.close();
		}
		catch(Exception e){
			e.printStackTrace();
			}
         
		return provDatasets;
	}
	
	public String toString(ProvenaceDataset dataset){
		
		String provDatasets = "{\"proveRec\":[{"+
								"\"id\":"+"\""+dataset.dataSetId+"\"";
		if(dataset.datasetTitle!=null)
			provDatasets+=",\"name\":"+"\""+dataset.datasetTitle+"\"";
		provDatasets+= ",\"type\":"+"\"Dataset\""+"}";
		for (Map.Entry<String,List<ProvenanceRecord>> entry : dataset.provRecordbyWf.entrySet()) 
		{
			provDatasets += "," +
					"{\"id\":"+"\""+entry.getKey()+"\""+
					",\"parentId\":"+"\""+dataset.dataSetId+"\""+
					",\"type\":"+"\"Workflow Instance\""+
					"}";
			for(ProvenanceRecord record: entry.getValue()){
				provDatasets += provToString(record);
			}
		}
		provDatasets+="]}";
		return provDatasets;
	}
	public
	String provToString(ProvenanceRecord provRecord){
		String record = ",{"+
				"\"id\":"+"\""+provRecord.getSipId()+"\""+
				",\"parentId\":"+"\""+provRecord.getWfInstanceId()+"\""+
				",\"status\":"+"\""+provRecord.getStatus().getText()+"\""+
				",\"type\":"+"\"Sub-workflow (SIP)\""+
				",\"date\":"+ "\""+ServerConstants.dateFormat.format(provRecord.getDate())+"\""+
				"}";
		for(Event event:provRecord.getEvents()){
			record+=","+eventToJson(event,provRecord.getSipId());
		}
		return record;
	}
	 public String eventToJson(Event event, String sipId){
		 
		 Status status; 
	 
		 if(event.getEventPercent()==100)
			 status =Status.Completed;
		 else
			 status = Status.Pending;
			String eventStr = "{"+
							"\"id\":"+"\""+String.valueOf(event.getId())+"\""+
							",\"type\":"+"\"sub-event\""+
							",\"parentId\":"+"\""+sipId+"\""+
							",\"name\":"+"\""+event.getEventType()+"\""+
							",\"date\":" + "\""+ServerConstants.dateFormat.format(event.getEventDate())+"\""+
							",\"status\":"+"\""+status.getText()+"\""+"}";
			return eventStr;
		}
	 
	 public void executeQuery(String query) {
			
			
			Connection conn;
			try {
				conn = dbInstance.getConnection();
				PreparedStatement pst = conn.prepareStatement(query);
		        
		        
		         ResultSet results = pst.executeQuery();
		         
		         String previousDatasetId = "";
		         String previousWfInstanceId = "";
		          		
		         
		         ProvenaceDataset provDataset = new ProvenaceDataset();
		         while(results.next())
		         {
		        	 System.out.println(results.getString("id"));
		         }
		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
	 }

}

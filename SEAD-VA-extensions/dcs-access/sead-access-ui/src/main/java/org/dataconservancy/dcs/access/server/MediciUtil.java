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

package org.dataconservancy.dcs.access.server;

import com.google.gwt.user.client.ui.CheckBox;
import com.sun.org.apache.xerces.internal.xni.*;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.dataconservancy.dcs.access.client.model.CollectionNode;
import org.dataconservancy.dcs.access.client.model.FileNode;
import org.dataconservancy.dcs.access.client.upload.model.DeliverableUnit;
import org.dataconservancy.dcs.access.shared.CheckPointDetail;
import org.dataconservancy.dcs.access.shared.MediciInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to query Medici
 */
public class MediciUtil {
	
	public Map<String,List<String>> xmlAttrReader(String xml, String key, String value) throws XmlPullParserException, IOException{
    	
    	Map<String,List<String>> result = new HashMap<String, List<String>>();
    	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput ( new StringReader (xml) );
        int eventType = xpp.getEventType();
        int keyInt=0;
        int valInt=0;
        
        String keyStr="";
        String valStr="";
        
        while (eventType != xpp.END_DOCUMENT) {
        if(eventType == xpp.START_TAG) {
             ;
            if(xpp.getName().equals("binding")&&xpp.getAttributeValue(0).equals(key))
            	keyInt=1;
            		
             
             else if(xpp.getName().equals("binding")&&xpp.getAttributeValue(0).equals(value))
            	 valInt=1;
         }
         else if(eventType == xpp.TEXT) {
        	 if(xpp.getText().replace(" ","").length()==0)
        	 {
        		 eventType = xpp.next();
        		 continue;
        	 }
              if(keyInt==1&&valInt==0)
              	 keyStr= xpp.getText();
               else if(keyInt==1&&valInt==1){
              	 valStr= xpp.getText();
              	 List<String> temp;
              	 if(result.containsKey(keyStr))
              		 temp = result.get(keyStr);
              	 else
              		 temp = new ArrayList<String>();
              	 
              	 temp.add(valStr);
          		 result.put(keyStr, temp);
              	 keyInt=valInt=0;
               }
         }
         eventType = xpp.next();
        }
    	
    	return result;
    }
    
public List<String> xmlSinglAttrReader(String xml, String key){
    	
    	XmlPullParserFactory factory = null;
		try {
			factory = XmlPullParserFactory.newInstance();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        factory.setNamespaceAware(true);
        XmlPullParser xpp = null;
		try {
			xpp = factory.newPullParser();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        try {
			xpp.setInput ( new StringReader (xml.replaceAll("&", "&amp;")) );
		
	        int eventType = xpp.getEventType();
	        int keyInt=0;
	        
	        String keyStr="";
	        List<String> result = new ArrayList<String>();
	        
	        while (eventType != xpp.END_DOCUMENT) {
	        if(eventType == xpp.START_TAG) {
	             ;
	            if(xpp.getName().equals("binding")&&xpp.getAttributeValue(0).equals(key))
	            	keyInt=1;         		
	         }
	         else if(eventType == xpp.TEXT) {
	        	 if(xpp.getText().replace(" ","").length()==0)
	        	 {
	        		 eventType = xpp.next();
	        		 continue;
	        	 }
	              if(keyInt==1){
	              	 keyStr= xpp.getText();
	              	 result.add(keyStr);
	              	 keyInt=0;
	               }
	         }
	         eventType = xpp.next();
	        }
	        return result;
        } catch (XmlPullParserException e) {
			e.printStackTrace();
        	return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    	
    }

	public CollectionNode xmlDUAttrReader(String xml) {
	try{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    factory.setNamespaceAware(true);
	    XmlPullParser xpp = factory.newPullParser();
	
	    xpp.setInput ( new StringReader (xml.replaceAll("&", "&amp;")) );
	    int eventType = xpp.getEventType();
	    int title=0;
	   // int creator =0;
	   // int abstrct =0;
	    
	    CollectionNode du = null;
	    while (eventType != xpp.END_DOCUMENT) {
	    if(eventType == xpp.START_TAG) {
	    	if(xpp.getName().equals("results")){
	    		du = new CollectionNode();
	    	}
	    	else if(xpp.getName().equals("binding")&&xpp.getAttributeValue(0).equals("title")&&title==0)
	    		title=1;
	     }
	     else if(eventType == xpp.TEXT) {
	    	 if(xpp.getText().replace(" ","").length()==0)
	    	 {
	    		 eventType = xpp.next();
	    		 continue;
	    	 }
	    	 else if(title==1){
	    		 
	    		 String titleStr =xpp.getText();
	   	      	 String[] subTitle = titleStr.split("/");
	
	          	 du.setTitle(subTitle[subTitle.length-1]);
	          	 title=2;
	           }

	     }
	     
	     eventType = xpp.next();
	    }
		
		return du;
	} catch (XmlPullParserException e) {
		e.printStackTrace();
    	return null;
	} catch (IOException e) {
		e.printStackTrace();
		return null;
	}
	}
	
	public FileNode xmlFileAttrReader(String xml){
		try{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    factory.setNamespaceAware(true);
	    XmlPullParser xpp = factory.newPullParser();

	    xpp.setInput ( new StringReader (xml.replaceAll("&", "&amp;")) );
	    int eventType = xpp.getEventType();
	    //int record=0;
	    int name=0;
	   
	    FileNode file = null;
	    while (eventType != xpp.END_DOCUMENT) {
	    if(eventType == xpp.START_TAG) {
	    	if(xpp.getName().equals("result")){
	    		file = new FileNode();
	    		
	    		//record =1;
	    	}
	    	if(xpp.getName().equals("binding")&&xpp.getAttributeValue(0).equals("name"))
	    		name=1;
	     }
	     else if(eventType == xpp.TEXT) {
	    	 if(xpp.getText().replace(" ","").length()==0)
	    	 {
	    		 eventType = xpp.next();
	    		 continue;
	    	 }
	    	 else if(name==1){
	          	 file.setTitle(xpp.getText());
	          	 name=0;
	           }
	     }
	   
	     eventType = xpp.next();
	    }
		
		return file;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
        	return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	List<String> loadIds;
	public String checkpointedSIP(String guid, String tmpHome){//use a file for persistent checkpointing
		String lastFile = null;
		loadIds = new ArrayList<String>();
		
		if(new File(tmpHome+"/"+guid+"_sip_0.xml").exists()){
			if(new File(tmpHome+"/checkpoint").exists()){
				
				BufferedReader checkPointBr = null;
				 
				try {
		 
					String sCurrentLine;
					checkPointBr = new BufferedReader(new FileReader(tmpHome+"/checkpoint"));
		 
					while ((sCurrentLine = checkPointBr.readLine()) != null) {
						if(sCurrentLine.contains(guid)){
							
							String[] links = sCurrentLine.split("\t");

                            lastFile = links[0];
							if(links.length>1)
								if(links[1].length()>2)
									loadIds.add(links[1]);
							continue;
						}
					}
		 
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (checkPointBr != null)
							checkPointBr.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}	
				
			}
			if(lastFile==null)
				lastFile =  guid+"_sip_-1.xml";
		}
		if(lastFile==null)
			return null;
		return  tmpHome+"/"+lastFile;
	}
	
	public List<String> getLoadIds() {
		return loadIds;
	}


	public void setLoadIds(List<String> loadIds) {
		this.loadIds = loadIds;
	}


	public int checkpointedSIPCount(String guid, String tmpHome){//use a file for persistent checkpointing
		int n = 0;
		
		if(new File(tmpHome+"/checkpointCount").exists()){
				
				BufferedReader checkPointBr = null;
				 
				try {
		 
					String sCurrentLine;
					
		 
					checkPointBr = new BufferedReader(new FileReader(tmpHome+"/checkpointCount"));
		 
					while ((sCurrentLine = checkPointBr.readLine()) != null) {
						if(sCurrentLine.contains(guid)){
							String[] arr = sCurrentLine.split("\t");
							n = Integer.parseInt(arr[arr.length-1]);
							break;
						}
					}
		 
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (checkPointBr != null)
							checkPointBr.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}	
		}
		return n;
	}
	
	public void createCheckpoint(String sipFilePath, String previousStatusUrl, int n, String tmpHome){
		File checkpointFile = new File(tmpHome+"/checkpoint");
		String sipFileName = sipFilePath.split("/")[sipFilePath.split("/").length-1];
		try {
			if(!checkpointFile.exists())
				checkpointFile.createNewFile();
			FileWriter fileWriter = new FileWriter(checkpointFile.getAbsoluteFile(),true);
	    	BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
	    	bufferWriter.write(sipFileName+"\t"+previousStatusUrl+"\n");
	        bufferWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String guid = sipFileName.split("_")[0];
		int count = checkpointedSIPCount(guid, tmpHome);
		if(count==0){
			File checkpointCountFile = new File(tmpHome+"/checkpointCount");
			try {
				if(!checkpointCountFile.exists())
					checkpointCountFile.createNewFile();
				FileWriter fileWriter = new FileWriter(checkpointCountFile.getAbsoluteFile(),true);
		    	BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
		    	bufferWriter.write(guid + "\t" + n + "\n");
		        bufferWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
 	}
}

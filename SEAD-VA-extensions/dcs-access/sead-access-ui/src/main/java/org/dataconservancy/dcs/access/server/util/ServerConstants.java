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

package org.dataconservancy.dcs.access.server.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.access.shared.MediciInstance;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ServerConstants{
	static{
		try {
			
			Map<String,String> passwords = new ServerConstants().loadPasswords();
			Iterator pwds = passwords.entrySet().iterator();
			while(pwds.hasNext()){
				Map.Entry<String, String> pwd = (Map.Entry<String, String> )pwds.next();
				if(pwd.getKey().equals("email-sender"))
					emailPassword = pwd.getValue();
			}
			acrInstances = new ServerConstants().loadAcrInstances();
		} catch (IOException e) {
			e.printStackTrace();
		}	

	}

	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	public static String FORMAT_IANA_SCHEME = "http://www.iana.org/assignments/media-types/";
	public static String acrPassword;
	public static String emailPassword;
	public static List<MediciInstance> acrInstances;

	private Map<String,String> loadPasswords() throws IOException{
		Map<String,String> passwords = new HashMap<String,String>();
//		System.out.println(getClass().getResource("../../../../../../passwords.xml").getPath());
		InputStream inputStream = 
				getClass().getResourceAsStream(
				"../../../../../../passwords.xml"
				);
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer);
		
		
    	XmlPullParserFactory factory = null;
		try {
			factory = XmlPullParserFactory.newInstance();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
        factory.setNamespaceAware(true);
        XmlPullParser xpp = null;
		try {
			xpp = factory.newPullParser();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

        try {
			
        	xpp.setInput ( new StringReader (writer.toString()) );
			int eventType = xpp.getEventType();
	        int name = 0;
	        int value = 0;
	        
	        String nameStr="";
	        String valueStr="";
	        
	        while (eventType != xpp.END_DOCUMENT) {
	        if(eventType == xpp.START_TAG) {
	            if(xpp.getName().equals("name"))
	            	name = 1;         		
	            if(xpp.getName().equals("password"))
	            	value = 1;
	         }
	         else if(eventType == xpp.TEXT) {
	        	 if(value==1){
	        		 valueStr = xpp.getText();
	        		 passwords.put(nameStr, valueStr);
	        		 value = 0;
	        	 }      		 
	        	 else if(name==1){
	        		 nameStr= xpp.getText(); 
	        		 name = 0;
	        	 }
	         }
	         eventType = xpp.next();
	        }
        } catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return passwords;
	}
	
	private List<MediciInstance> loadAcrInstances() throws IOException{
		List<MediciInstance> instances = new ArrayList<MediciInstance>();
		InputStream inputStream = 
				getClass().getResourceAsStream(
				"../../../../../../acrInstances.xml"
				);
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer);
		
		
    	XmlPullParserFactory factory = null;
		try {
			factory = XmlPullParserFactory.newInstance();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
        factory.setNamespaceAware(true);
        XmlPullParser xpp = null;
		try {
			xpp = factory.newPullParser();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

        try {
			
        	xpp.setInput ( new StringReader (writer.toString()) );
			int eventType = xpp.getEventType();
	        int id = 0;
	        int url = 0;
	        int remoteAPI = 0;
	        int title = 0;
	        int type = 0;
	        int pwd = 0;
	        int user = 0;

	        MediciInstance instance = null;
	        
	        while (eventType != xpp.END_DOCUMENT) {
	        if(eventType == xpp.START_TAG) {
	            if(xpp.getName().equals("instance"))
	            	instance = new MediciInstance();         		
	            if(xpp.getName().equals("id"))
	            	id = 1;
	            if(xpp.getName().equals("user"))
	            	user = 1;
	            if(xpp.getName().equals("password"))
	            	pwd = 1;
	            if(xpp.getName().equals("url"))
	            	url = 1;
	            if(xpp.getName().equals("remoteAPI"))
	            	remoteAPI = 1;
	            if(xpp.getName().equals("title"))
	            	title = 1;
	            if(xpp.getName().equals("type"))
	            	type = 1;
	         }
	         else if(eventType == xpp.TEXT) {
	        	 if(id==1){
	        		 instance.setId(Integer.parseInt(xpp.getText()));
	        		 id = 0;
	        	 }      		 
	        	 else if(url==1){
	        		 instance.setUrl(xpp.getText());
	        		 url = 0;
	        	 }
	        	 else if(remoteAPI==1){
	        		 instance.setRemoteAPI(xpp.getText());
	        		 remoteAPI = 0;
	        	 }
	        	 else if(title ==1){
	        		 instance.setTitle(xpp.getText());
	        		 title = 0;
	        	 }
	        	 else if(pwd ==1){
	        		 instance.setPassword(xpp.getText());
	        		 pwd = 0;
	        	 }
	        	 else if(type==1){
	        		 instance.setType(xpp.getText());
	        		 type = 0;
	        	 }
	        	 else if(user==1){
	        		 instance.setUser(xpp.getText());
	        		 user = 0;
	        	 }
	         }
	         else if(eventType == xpp.END_TAG) {
	            if(xpp.getName().equals("instance"))
	            	instances.add(instance);
	         }
	         eventType = xpp.next();
	        }
	        
        } catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return instances;
	}
}

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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.access.shared.Event;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class StatusReader {
	public enum Status {
		Completed("completed"), Pending("pending"), Timeout("timeout"), Failed("failed");
		
	
		  private String text; 

		  Status(String text) {
		    this.text = text;
		  }

		  public String getText() {
		    return this.text;
		  }

		  public static Status fromString(String text) {
		    if (text != null) {
		      for (Status b : Status.values()) {
		        if (text.equalsIgnoreCase(b.text)) {
		          return b;
		        }
		      }
		    }
		    return null;
		  }
		  };
	

    public Status getStatus(String process, String statusUrl, int expectedCount) throws InterruptedException{
    	
    	try {
    		
    		URL status = new URL(statusUrl);
    		InputStream input =
    				status.openStream();
            
    		int count = pullParse(input, process);
			System.out.println("Actual count="+count+" Expected Count"+expectedCount+"\n");
			System.out.println("Status Url"+statusUrl+"\n");
			if(count ==-1)
				return Status.Failed;
			else if(count>=expectedCount)
				return Status.Completed;
			else
				return Status.Pending;
    		//}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//pull parser
    	return null;
    }
    
    
 public String getDetails(String process, String statusUrl, int expectedCount) throws InterruptedException{
    	
    	try {
    		
    		URL status = new URL(statusUrl);
    		InputStream input =
    				status.openStream();
            
    		String details = pullParseEventDetail(input, process);
    		System.out.println("Status Url"+statusUrl+"\n");
			
    		return details;
    		//}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//pull parser
    	return null;
    }
   
    public int pullParse(InputStream input, String process) throws IOException{
		
		XmlPullParserFactory factory;
		int count=0;
		try {
			factory = XmlPullParserFactory.newInstance();
		
		    factory.setNamespaceAware(true);
		    XmlPullParser xpp = factory.newPullParser();
	
		    xpp.setInput (input,null);// new StringReader (xml.replaceAll("&", "&amp;")) );
		    int eventType = xpp.getEventType();
		    //int record=0;
		    int dcsEvent=0;
		   
		   
		    while (eventType != XmlPullParser.END_DOCUMENT) {
		    if(eventType == XmlPullParser.START_TAG) {
		    	if(xpp.getName().equals("eventType"))
		    		dcsEvent=1;
		     }
		     else if(eventType == XmlPullParser.TEXT) {
		    	 if(dcsEvent==1)
		    	 {
		    		// System.out.println(xpp.getText());
                     if(xpp.getText().equalsIgnoreCase(process)) {
		    			 count++;
                         dcsEvent =0 ;
                     }

		    	 }
		    	 
		     }
		    
	   
		     eventType = xpp.next();
		    }
		}
		catch (XmlPullParserException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
		return count;
	}
    

public String pullParseEventDetail(InputStream input, String process) throws IOException{
	
	XmlPullParserFactory factory;
	int count=0;
	try {
		factory = XmlPullParserFactory.newInstance();
	
	    factory.setNamespaceAware(true);
	    XmlPullParser xpp = factory.newPullParser();

	    xpp.setInput (input,null);// new StringReader (xml.replaceAll("&", "&amp;")) );
	    int eventType = xpp.getEventType();
	    //int record=0;
	    int dcsEvent=0;
	    int dcsEventDetail=0;
	    int processType=0;
	   
	    while (eventType != XmlPullParser.END_DOCUMENT) {
	    if(eventType == XmlPullParser.START_TAG) {
	    	
	    	if(xpp.getName().equals("eventType"))
	    		dcsEvent=1;
	    	if(processType==1&&xpp.getName().equals("eventDetail")){
	    		dcsEventDetail = 1;
	    		dcsEvent =0 ;
	    	}
	    	
	     }
	     else if(eventType == XmlPullParser.TEXT) {
	    	 	 if(dcsEventDetail==1&&processType==1&&count==1){
		    		 return xpp.getText();
		    	 } 	
                 if(xpp.getText().equalsIgnoreCase(process)) {
                	 processType=1;
	    			 count++;
                     //
                 }

	    	 
	    	 
	    	 
	    	 
	     }
   
	     eventType = xpp.next();
	    }
	}
	catch (XmlPullParserException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
	
	return null;
}

public Map<Date,List<Event>> getEvents(String statusUrl, Date latestDate) throws IOException{
	
		URL status = new URL(statusUrl);
		InputStream input =
			status.openStream();
		Map<Date,List<Event>> events = new HashMap<Date,List<Event>>();
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
		
		    factory.setNamespaceAware(true);
		    XmlPullParser xpp = factory.newPullParser();
	
		    xpp.setInput (input,null);
		    
		    int eventType = xpp.getEventType();
		    
		    int dcsEventType=0;
		    int eventDetail = 0;
		    int eventDate = 0;
		    
		    Event event = null;
		    while (eventType != XmlPullParser.END_DOCUMENT) {
		    	 if(eventType == XmlPullParser.START_TAG) {
		    		if(xpp.getName().equals("Event"))
		    			event = new Event();
		 	    	if(xpp.getName().equals("eventType"))
		 	    		dcsEventType=1;
		 	    	if(xpp.getName().equals("eventDate"))
		 	    		eventDate=1;
		 	    	if(xpp.getName().equals("eventDetail"))
		 	    		eventDetail=1;
		 	    	
		 	    	
		 	     }
		 	     else if(eventType == XmlPullParser.TEXT) {
			    	 	 if(dcsEventType==1){
			    	 		 if(event==null)
			    	 			event = new Event();
			    	 		event.setEventType(xpp.getText());
			    	 		dcsEventType=0;
				    	 } 	
			    	 	 
		 	    	 	 if(eventDate==1) {
		 	    	 		String s = xpp.getText().replace("Z","-0500");
		 	    	 		
		 	    	 		Date date = null;
							try {
								date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(s);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							 if(event==null)
				    	 			event = new Event();
		 	    	        event.setEventDate(
		                			  date);
		                	eventDate=0;
		                 }
		 	    	 	 if(eventDetail==1) {
		 	    	 		 if(event==null)
				    	 			event = new Event();
		                	  event.setEventDetail(xpp.getText());
		                	  eventDetail=0;
		                 }
		 	     }
		    	 if(eventType == XmlPullParser.END_TAG) {
			    		if(xpp.getName().equals("Event"))
			    		{	
			    			List<Event> eventList = events.get(event.getEventType());
			    			if(eventList == null)
			    				eventList = new ArrayList<Event>();
			    			if(event.getEventDate().after(latestDate))
			    			{
			    				eventList.add(event);
			    				events.put(event.getEventDate(), eventList);
			    			}
			    		}
		    	 }
		    	 eventType = xpp.next();
		    }
		}
		catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		
		return events;
	}

}

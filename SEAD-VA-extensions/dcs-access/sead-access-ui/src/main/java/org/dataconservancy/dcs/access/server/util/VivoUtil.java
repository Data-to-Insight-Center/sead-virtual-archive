/*
#
# Copyright 2013 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
*/

package org.dataconservancy.dcs.access.server.util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.dataconservancy.dcs.access.shared.Person;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.util.*;

/**
 * Queries VIVO for metadata
 */
public final class VivoUtil {


    public static Set<Person> getAllUsers(){

    	Set<Person> persons = new LinkedHashSet<Person>();
        String query ="output=xml&query=";
        try{
            query+=java.net.URLEncoder.encode("SELECT DISTINCT ?id ?fName ?lName WHERE"+ 
            		"{ ?id <http://vivoweb.org/ontology/core#authorInAuthorship> ?position . "+
            		"?id <http://xmlns.com/foaf/0.1/firstName> ?fName . ?id <http://xmlns.com/foaf/0.1/lastName> ?lName . } "
            			, "ISO-8859-1");
       

	        String xml = cmdExec(query, "http://sead-vivo.d2i.indiana.edu:3030/SEAD-VIVO/sparql");
	        if(xml.contains("Error")||xml.contains("Exception"))
	            return null;
	        persons.addAll(xmlAttrReader(xml));
	        
	        query ="output=xml&query=";
	        query +=java.net.URLEncoder.encode("SELECT DISTINCT ?id ?fName ?lName WHERE"+ 
	        		"{ ?id <http://vivoweb.org/ontology/core#personInPosition> ?position . "+
	        		"?id <http://xmlns.com/foaf/0.1/firstName> ?fName . ?id <http://xmlns.com/foaf/0.1/lastName> ?lName . } "
	        			, "ISO-8859-1");
	        
	        xml = cmdExec(query, "http://sead-vivo.d2i.indiana.edu:3030/SEAD-VIVO/sparql");
	        if(xml.contains("Error")||xml.contains("Exception"))
	            return null;
	        persons.addAll(xmlAttrReader(xml));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        };
        
        return persons;
    }

    public static List<Person> xmlAttrReader(String xml){

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
            int id=0;
            int fName=0;
            int lName=0;
            
            String keyStr="";
            List<Person> results = new ArrayList<Person>();

            Person person = null;
            int count =0;
            
            while (eventType != xpp.END_DOCUMENT) {
                if(eventType == xpp.START_TAG) {

                    String name = xpp.getName();
                    //String attr = xpp.getAttributeValue(0);
                    if(name.equals("binding")){
                        if(xpp.getAttributeValue(0).equals("id"))
                        		id =1;
                        else if(xpp.getAttributeValue(0).equals("fName"))
                                fName =1;
                        else if(xpp.getAttributeValue(0).equals("lName"))
                                lName =1;
                                  
                    }
                }
                else if(eventType == xpp.TEXT) {
                    String text = xpp.getText();
                    if(text.replace(" ","").length()==0)
                    {
                        eventType = xpp.next();
                        continue;
                    }
                    if(id==1){
                    	if(person == null){
                    		person = new Person();
                    		count = 0;
                    	}
                    	else
                    		count++;
                    	person.setEmailAddress(text);
                        id = 0;
                    }
                    if(fName==1){
                    	if(person == null){
                    		person = new Person();
                    		count = 0;
                    	}
                    	else
                    		count++;
                    	person.setFirstName(text);
                        fName = 0;
                    }
                    if(lName==1){
                    	if(person == null){
                    		person = new Person();
                    		count=0;
                    	}
                    	else
                    		count++;
                    	person.setLastName(text);
                        lName = 0;
                    }
                    if(count ==2){
                    	results.add(person);
                    	person = null;
                    	count = 0;
                    }
                }
                eventType = xpp.next();
            }
            return results;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }




    public static String cmdExec(String query, String sparqlEndpoint){
        String cmd =
                "curl -s -S -X POST --data-binary \"" +
                        query+
                        "\" "+sparqlEndpoint;
        CommandLine cmdLine = CommandLine.parse(cmd);
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PumpStreamHandler psh = new PumpStreamHandler(stdout);

        executor.setStreamHandler(psh);

        int exitValue;
        try {
            exitValue = executor.execute(cmdLine);
        } catch (ExecuteException e) {
            //logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            //.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }


        //String str = "";
        BufferedReader br =null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(
                            new ByteArrayInputStream(
                                    stdout.toByteArray()
                            )
                    )
            );
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String output_line = null;
        String xml ="";
        try {
            int flag=0;

            while((output_line = br.readLine()) != null)
            {
                if(output_line.contains("--:--")){
                    if(output_line.contains("<?xml version"))
                        output_line = output_line.substring(output_line.indexOf("<?xml version"));
                    else
                        continue;
                }
                xml+=output_line;

            }


        } catch (IOException e) {
            //logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
        return xml;
    }

    public static String getAffiliation(String vivoCreatorId){

        String queryDuCreator ="output=xml&query=";
        try{
            queryDuCreator+=java.net.URLEncoder.encode("SELECT ?email WHERE { "+
                    "<"+vivoCreatorId+"> <http://vivoweb.org/ontology/core#primaryEmail> ?email . " +
                    " }", "ISO-8859-1");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        };

        String xmlCreator = cmdExec(queryDuCreator, "http://sead-vivo.d2i.indiana.edu:3030/SEAD-VIVO/sparql");
        if(xmlCreator.contains("Error")||xmlCreator.contains("Exception"))
            return null;
        List<String> creatorEmail = xmlSinglAttrReader(xmlCreator, "email");
        if(creatorEmail!=null&&creatorEmail.size()>0)
        {
            String tempEmail =creatorEmail.get(0);
            tempEmail = tempEmail.replace("(AT)","@").replace("(DOT)",".");
            return  emailInstitutionMap.get(tempEmail.split("@")[tempEmail.split("@").length-1]);
        }
        else
            return null;
    }
    

    static Map<String,String> emailInstitutionMap = new HashMap<String, String>();
    static {
        emailInstitutionMap.put("illinois.edu","University of Illinois");
        emailInstitutionMap.put("indiana.edu","Indiana University");
    }
    
    public static Map<String,String> vivoVAInstiutionMap = new HashMap<String, String>();
    static {
    	vivoVAInstiutionMap.put("University of Illinois", "Ideals");
        vivoVAInstiutionMap.put("UIUC", "Ideals");
        vivoVAInstiutionMap.put(
        		"Indiana University",
        		//"IU SDA"
        		"IU Scholarworks"
        		);
    }
    
    public static List<String> xmlSinglAttrReader(String xml, String key){

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

                    String name = xpp.getName();
                    //String attr = xpp.getAttributeValue(0);
                    if(name.equals("literal")
                        //&&xpp.getAttributeValue(0).equals("\""+key+"\"")
                            )
                        keyInt=1;
                }
                else if(eventType == xpp.TEXT) {
                    String text = xpp.getText();
                    if(text.replace(" ","").length()==0)
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
}

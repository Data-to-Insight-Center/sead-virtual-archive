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
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
*/
package org.dataconservancy.dcs.index.dcpsolr;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FgdcMapping {
    private final static String COLLECTION_TITLE = "FGDC";
    private final static double INVALID_NUM = -99999;

    //Elements to extract from the metadata
    private static final String WEST_BOUND_LONGITUDE ="westbc";
    private static final String EAST_BOUND_LONGITUDE = "eastbc";
    private static final String NORTH_BOUND_LATITUDE = "northbc";
    private static final String SOUTH_BOUND_LATITUDE = "southbc";
    private static final String ABSTRACT = "abstract";
    private static final String TITLE = "title";
    private static final String ORIGIN = "origin";

    public Map<Enum,String> map(String fgdcMetadata) {

        parse(fgdcMetadata);
        Map<Enum,String> solrElements = new HashMap<Enum, String>();
        try{
            
                double westLongitude = INVALID_NUM;
                double eastLongitude = INVALID_NUM;
                double southLatitude = INVALID_NUM;
                double northLatitude = INVALID_NUM;
    
               // if( parser.hasElement(WEST_BOUND_LONGITUDE) && parser.hasElement(EAST_BOUND_LONGITUDE)
                //        && parser.hasElement(SOUTH_BOUND_LATITUDE) && parser.hasElement(NORTH_BOUND_LATITUDE) ) {
    
                String numberString = getValue(WEST_BOUND_LONGITUDE).get(0);
                if( numberString != null && !numberString.isEmpty()) {
                    westLongitude = Double.parseDouble( numberString );
                }

                numberString = getValue(EAST_BOUND_LONGITUDE).get(0);
                if( numberString != null && !numberString.isEmpty()) {
                    eastLongitude = Double.parseDouble( numberString );
                }

                numberString = getValue(SOUTH_BOUND_LATITUDE).get(0);
                if( numberString != null && !numberString.isEmpty()) {
                    southLatitude = Double.parseDouble( numberString );
                }

                numberString = getValue(NORTH_BOUND_LATITUDE).get(0);
                if( numberString != null && !numberString.isEmpty()) {
                    northLatitude = Double.parseDouble( numberString );
                }

                solrElements.put(SeadSolrField.FgdcField.WESTLON,Double.toString(westLongitude));
                solrElements.put(SeadSolrField.FgdcField.EASTLON,Double.toString(eastLongitude));
                solrElements.put(SeadSolrField.FgdcField.SOUTHLAT,Double.toString(southLatitude));
                solrElements.put(SeadSolrField.FgdcField.NORTHLAT,Double.toString(northLatitude));


                String studyAbstract = getValue(ABSTRACT).get(0);
                String title = getValue(TITLE).get(0);
                String origin = getValue(ORIGIN).get(0);
                solrElements.put(SeadSolrField.FgdcField.ORIGIN,origin);
                solrElements.put(SeadSolrField.FgdcField.LOCATION,title);
                solrElements.put(SeadSolrField.FgdcField.ABSTRACT,studyAbstract);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return solrElements;
    }
    

        private SAXBuilder parser = new SAXBuilder();
        private Document mdFile;

        private class XMLElement {
            public String parent;
            public String name;
            public String value;

            public XMLElement(String parent, String nodeName, String nodeValue){
                name = nodeName;
                value = nodeValue;
                this.parent = parent;
            }
        }

        //Stores all child elements
        private ArrayList<XMLElement> childElements;

        //Stores the name of all non child elements
        private ArrayList<String> elements;

       

        private void parse(String metadata){
            try
            {
                elements = new ArrayList<String>();
                childElements = new ArrayList<XMLElement>();
                if( metadata != null) {
                    InputStream mdStream = IOUtils.toInputStream(metadata);
                    try {
                        mdFile = parser.build(mdStream);
                    } catch (JDOMException e) {
                    } catch (IOException e) {
                        System.out.println("Parsing exception: " + e);
                    }

                    if( mdFile != null)
                    {
                        Element root = (Element) mdFile.getRootElement();
                        parseMetaData(root);
                    }
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        public boolean isValid(){
            return (mdFile != null);
        }

        private void parseMetaData(Element element){

            if( element.getChildren().size() > 0){
                elements.add(element.getName());
                for( int i = 0; i < element.getChildren().size(); i++ ){
                    parseMetaData((Element)element.getChildren().get(i));
                }
            }
            else {
                Element parent = element.getParentElement();
                if (parent != null) {
                    childElements.add(new XMLElement(parent.getName(), element.getName(), element.getValue()));
                }
            }

        }

        public List<String> getValue(String name){

            ArrayList<String> values = new ArrayList<String>();

            for( XMLElement e : childElements) {
                if( e.name.equalsIgnoreCase(name)){
                    values.add(e.value);
                }
            }

            return values;
        }

        public List<String> getChildValue(String parentName){
            ArrayList<String> values = new ArrayList<String>();

            for( XMLElement e : childElements){
                if( e.parent.equalsIgnoreCase(parentName)){
                    values.add(e.value);
                }
            }

            return values;
        }

        public String getRootNodeName(){
            String rootName = "";

            if( mdFile != null ) {
                Element root = (Element) mdFile.getRootElement();

                if( root != null ){
                    rootName = root.getName();
                }
            }
            return rootName;
        }

        public boolean hasElement(String name) {
            boolean hasElement = elements.contains(name);

            if( !hasElement ) {
                for( XMLElement e : childElements) {
                    if( e.name.equalsIgnoreCase(name)){
                        hasElement = true;
                        break;
                    }
                }
            }

            return hasElement;
        }

        public void printElements()
        {
            for(String elementName : elements){
                System.out.println(elementName);
            }

            for( XMLElement e : childElements) {
                System.out.println(e.name);
            }
        }

}

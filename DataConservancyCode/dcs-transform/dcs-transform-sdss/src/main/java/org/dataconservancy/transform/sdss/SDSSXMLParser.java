/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.transform.sdss;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.dataconservancy.model.dcs.DcsMetadata;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class SDSSXMLParser {
    
    private SAXBuilder parser = new SAXBuilder();
    private Document mdFile; 
    
    private Map<String, String> elements;
    private static final String RESOURCE_ELEMENT = "RESOURCE";
    private static final String TABLE_ELEMENT = "TABLE";
    private static final String FIELD_ELEMENT = "FIELD";
    private static final String DATA_ELEMENT = "DATA";
    private static final String TABLE_DATA_ELEMENT = "TABLEDATA";
    private static final String TABLE_ROW_ELEMENT = "TR";
    private static final String TD_ELEMENT = "TD";
    private static final String NAME_ATTRIBUTE = "name";    
    
  
    public SDSSXMLParser(DcsMetadata metadata) {
        
        elements = new HashMap<String, String>();
        
        if( metadata != null && metadata.getMetadata() != null) {
            InputStream mdStream = IOUtils.toInputStream(metadata.getMetadata());
            try {
                mdFile = parser.build(mdStream);
            } catch (JDOMException e) {           
            } catch (IOException e) {
                System.out.println("Parsing exception: " + e);
            }            
            
            if( mdFile != null){
                Element root = (Element) mdFile.getRootElement();
                parseMetaData(root);
            }
        }
    }
    
    public boolean isValid(){
        return (mdFile != null);
    }
    
    @SuppressWarnings("unchecked")
    private void parseMetaData(Element element){
   
        Element root = (Element) mdFile.getRootElement();
        Element resource = null;
      
        resource = root.getChild(RESOURCE_ELEMENT, root.getNamespace());
       
        if( resource != null ){
            Element table = resource.getChild(TABLE_ELEMENT, resource.getNamespace());
            if( table != null ){
                List<Element> tableInformation = table.getChildren();
                ArrayList<String> dataNames = new ArrayList<String>();
                for( Element e : tableInformation ){
                    if( e.getName().equalsIgnoreCase(FIELD_ELEMENT)){
                        String name = e.getAttributeValue(NAME_ATTRIBUTE);
                        if( name != null && !name.isEmpty()){
                            dataNames.add(name);
                        }
                    }
                    else if( e.getName().equals(DATA_ELEMENT)){
                        Element tableData = e.getChild(TABLE_DATA_ELEMENT, e.getNamespace());
                        if( tableData != null ){
                            Element tr = tableData.getChild(TABLE_ROW_ELEMENT, tableData.getNamespace());
                            if( tr != null ){
                                List<Element> dataRows = tr.getChildren();
                                int nameIterator = 0;
                                for( Element data : dataRows ){
                                    if( data.getName().equalsIgnoreCase(TD_ELEMENT) && nameIterator < dataNames.size() ){
                                        elements.put(dataNames.get(nameIterator), data.getValue());
                                        nameIterator++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public String getValue(String name){
        
       return elements.get(name);
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
        return elements.containsKey(name);
    }    
    
    public void printElementNames() {
        Iterator it = elements.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, String> mapEntry = (Map.Entry<String, String>) it.next();

            System.out.println( mapEntry.getKey() );
        }
    }
}

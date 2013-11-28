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
package org.dataconservancy.transform.dryvalleys;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class DryValleyXMLParser {
    
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
    
    public DryValleyXMLParser(DcsMetadata metadata) {
        createParser(metadata);    
    }
    
    public DryValleyXMLParser(DcsMetadataRef mdRef) {
        //Get metadata from ref
        DcsMetadata md = null;
        createParser(md);
    }
    
    private void createParser(DcsMetadata metadata){
        elements = new ArrayList<String>();
        childElements = new ArrayList<XMLElement>();
        if( metadata != null && metadata.getMetadata() != null) {
            InputStream mdStream = IOUtils.toInputStream(metadata.getMetadata());
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

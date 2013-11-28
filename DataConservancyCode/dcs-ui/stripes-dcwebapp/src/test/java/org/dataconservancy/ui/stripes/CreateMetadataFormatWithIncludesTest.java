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
package org.dataconservancy.ui.stripes;

import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;

import static org.junit.Assert.assertEquals;

public class CreateMetadataFormatWithIncludesTest {
    
    private SAXParserFactory saxFactory;
    private SAXParser saxParser;
    private DefaultHandler handler;
    private ArrayList<DcsMetadataScheme> includes;
    private ArrayList<String> keys;
    private final static String NAME = "fgdc";
    private final static String VERSION = "1.0.0 20030801";
    private URL mainSchemaURL;
    
    @Before
    public void setupParser() throws ParserConfigurationException, SAXException, MalformedURLException {
        saxFactory = SAXParserFactory.newInstance();
        saxParser = saxFactory.newSAXParser();
        includes = new ArrayList<DcsMetadataScheme>();
        mainSchemaURL = new URL("http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd");       
    }

    /**
     * TODO: Ignored because the website used to load the scheme from fgdc.gov is down/inaccessible causing test failure.
     * @throws SAXException
     * @throws IOException
     */
    @Ignore
    @Test
    public void testCreatingMetadataObjectsFromXSD() throws SAXException, IOException {
        
        DcsMetadataFormat fgdcFormat = new DcsMetadataFormat();
        fgdcFormat.setName(NAME);
        fgdcFormat.setVersion(VERSION);
        
        final String includePath = mainSchemaURL.toString().substring(0, mainSchemaURL.toString().lastIndexOf("/")+1);
        keys = new ArrayList<String>();
        handler = new DefaultHandler() {
            public void startElement(String uri, String localName,String qName, 
                                     Attributes attributes) throws SAXException {
                if (qName.equalsIgnoreCase("xsd:include")) {
                    int index = attributes.getIndex("schemaLocation");
                    
                    if (index != -1) {
                        DcsMetadataScheme scheme = new DcsMetadataScheme();
                        scheme.setName(NAME);
                        scheme.setSchemaVersion(VERSION);
                        keys.add(attributes.getValue(index));
                        scheme.setSchemaUrl(includePath + attributes.getValue(index));
                        includes.add(scheme);
                    }
                }                 
            }
        };        

        InputStream xsdInputStream = mainSchemaURL.openStream();
        saxParser.parse(xsdInputStream, handler);
        
        fgdcFormat.setSchemes(includes);
        assertEquals(10, includes.size());
        
        keys.add(NAME);
        keys.add(mainSchemaURL.toString());
        
        BasicRegistryEntryImpl<DcsMetadataFormat> fgdcRegistryEntry = new BasicRegistryEntryImpl<DcsMetadataFormat>();
        fgdcRegistryEntry.setEntryType("DcsMetadataFormat");
        fgdcRegistryEntry.setKeys(keys);
        fgdcRegistryEntry.setEntry(fgdcFormat);
        fgdcRegistryEntry.setDescription("DcsMetadataFormat:" + NAME);
        fgdcRegistryEntry.setId("Format:" + NAME + ":" + VERSION);
        
        assertEquals(12, fgdcRegistryEntry.getKeys().size());
    }
}
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
package org.dataconservancy.ui.model.builder.xstream;

import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_DATE;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_DESCRIPTION;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_ENDDATE;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_FUNDINGENTITY;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_ID;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_NAME;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_NUMBER;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_PRINCIPLEINVESTIGATORID;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_PROJECT;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_PUBLISHER;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_STARTDATE;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_STORAGEALLOCATED;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_STORAGEUSED;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.transform.dom.DOMSource;

public class ProjectConverterTest extends BaseConverterTest {
    static final String XMLNS = "http://dataconservancy.org/schemas/bop/1.0";
    final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("d M yyyy");

    private String XML;

     /**
     * Used to wrap Project serializations in a {@literal <projects>} element for validation purposes.
     */
    private final static String PROJECTS_WRAPPER = "<projects>\n%s\n</projects>";
    
    private void setupXML() {
        XML = "<" + E_PROJECT + " " + E_ID + "=\"" + projectOne.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                "       <" + E_NAME + ">" + projectOne.getName() + "</" + E_NAME + ">\n" +
                "       <" + E_NUMBER + ">" + projectOne.getNumbers().get(0) + "</" + E_NUMBER + ">\n" +
                "       <" + E_NUMBER + ">" + projectOne.getNumbers().get(1) + "</" + E_NUMBER + ">\n" +
                "       <" + E_DESCRIPTION + ">" + projectOne.getDescription() + "</" + E_DESCRIPTION + ">\n" +
                "       <" + E_PUBLISHER + ">" + projectOne.getPublisher() + "</" + E_PUBLISHER + ">\n" +
                "       <" + E_STORAGEALLOCATED + ">" + projectOne.getStorageAllocated() + "</" + E_STORAGEALLOCATED + ">\n" +
                "       <" + E_STORAGEUSED + ">" + projectOne.getStorageUsed() + "</" + E_STORAGEUSED + ">\n" +
                "       <" + E_STARTDATE + ">\n" +
                "           <" + E_DATE + ">" + fmt.print(projectOne.getStartDate()) + "</" + E_DATE + ">\n" +
                "       </" + E_STARTDATE + ">\n" +
                "       <" + E_ENDDATE + ">\n" +
                "           <" + E_DATE + ">" + fmt.print(projectOne.getEndDate()) + "</" + E_DATE + ">\n" +
                "       </" + E_ENDDATE + ">\n" +
                "       <" + E_PRINCIPLEINVESTIGATORID + ">" + admin.getId() + "</" + E_PRINCIPLEINVESTIGATORID + ">\n" +
                "       <" + E_FUNDINGENTITY + ">" + projectOne.getFundingEntity() + "</" + E_FUNDINGENTITY + ">\n" +
                "    </" + E_PROJECT + ">";
    }
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setupXML();
    }
    
    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(projectOne));
    }

    @Test
    public void testUnmarshal() {      
        assertEquals("Expected: " + projectOne + " Actual: " + x.fromXML(XML), projectOne, x.fromXML(XML));
        assertEquals(projectOne, x.fromXML(x.toXML(projectOne)));
    }

    /**
     * Test which insures that the expected XML is valid, marshaled XML is valid, and round-tripped XML is valid.
     *
     * @throws Exception
     */
    @Test
    public void testMarshalIsValid() throws Exception {
        // Verify assumptions: test that our expected XML is valid.
        final String expectedXml = String.format(BOP_WRAPPER, String.format(PROJECTS_WRAPPER, XML));

        final Document expectedDom = parser.parse(IOUtils.toInputStream(expectedXml));
        validator.validate(new DOMSource(expectedDom));

        // Verify that our serialized XML is valid.
        final String actualXml = String.format(BOP_WRAPPER, String.format(PROJECTS_WRAPPER, x.toXML(projectOne)));
        final Document actualDom = parser.parse(IOUtils.toInputStream(actualXml));
        validator.validate(new DOMSource(actualDom));

        // Verify that round-tripped XML is valid (XML -> Object -> XML)
        final String roundTrippedXml = String.format(BOP_WRAPPER, String.format(PROJECTS_WRAPPER, x.toXML(x.fromXML(XML))));
        final Document roundTrippedDom = parser.parse(IOUtils.toInputStream(roundTrippedXml));
        validator.validate(new DOMSource(roundTrippedDom));
    }

    /**
     * Tests that there are no errors when fields on project are left empty. 
     */
    @Test
    public void testEmptyFields() {        
        
        //Test we are starting with good conversion
        assertEquals(projectOne, x.fromXML(x.toXML(projectOne)));

        //Remove each param and make sure serialization still works correctly. 
        projectOne.setDescription(null);
        assertEquals(projectOne, x.fromXML(x.toXML(projectOne)));

        projectOne.setFundingEntity(null);
        assertEquals(projectOne, x.fromXML(x.toXML(projectOne)));

        projectOne.setNumbers(null);
        assertEquals(projectOne, x.fromXML(x.toXML(projectOne)));

        projectOne.setStartDate(null);
        assertEquals(projectOne, x.fromXML(x.toXML(projectOne)));

        projectOne.setEndDate(null);
        assertEquals(projectOne, x.fromXML(x.toXML(projectOne)));

        projectOne.setName(null);
        assertEquals(projectOne, x.fromXML(x.toXML(projectOne)));

        projectOne.setId(null);
        assertEquals(projectOne, x.fromXML(x.toXML(projectOne)));
        
        projectOne.setPublisher(null);
        Project result = (Project) x.fromXML(x.toXML(projectOne));
        projectOne.setPublisher("Johns Hopkins Data Management Service");
        assertEquals(projectOne, result);
        
        //Test empty storage params
        projectOne = new Project();
        projectOne.setDescription(projectOne.getDescription());
        projectOne.setFundingEntity(projectOne.getFundingEntity());
        projectOne.setNumbers(projectOne.getNumbers());
        projectOne.setPublisher(projectOne.getPublisher());
        projectOne.setStartDate(projectOne.getStartDate());
        projectOne.setEndDate(projectOne.getEndDate());
        projectOne.setName(projectOne.getName());
        projectOne.setId(projectOne.getId());
        assertEquals(projectOne, x.fromXML(x.toXML(projectOne)));

    }

}

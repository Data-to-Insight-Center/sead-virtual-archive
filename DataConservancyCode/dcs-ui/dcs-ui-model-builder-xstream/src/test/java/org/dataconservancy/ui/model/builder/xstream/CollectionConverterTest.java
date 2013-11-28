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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.dom.DOMSource;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.ui.model.Collection;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class CollectionConverterTest extends BaseConverterTest implements ConverterConstants {
    static final String XMLNS = "http://dataconservancy.org/schemas/bop/1.0";
    final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("d M yyyy");
    private String XML;
    private String XML2;
    private String XML3;
    private Collection collectionTwo;
    private Collection collectionThree;

    private void setupXMLString() {
        XML =
                "    <" + E_COLLECTION + " " + E_ID + "=\"" + collectionWithData.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                        // "        <" + E_ID + ">" + COL1_ID + "</" + E_ID + ">\n" +
                        "        <" + E_COL_ALTERNATE_IDS + ">\n" +
                        "            <" + E_COL_ALTERNATE_ID + ">" + collectionWithData.getAlternateIds().get(0) + "</" + E_COL_ALTERNATE_ID + ">\n" +
                        "        </" + E_COL_ALTERNATE_IDS + ">\n" +
                        "        <" + E_COL_TITLE + ">" + collectionWithData.getTitle() + "</" + E_COL_TITLE + ">\n" +
                        "        <" + E_COL_SUMMARY + ">" + collectionWithData.getSummary() + "</" + E_COL_SUMMARY + ">\n" +
                        "        <" + E_COL_CITABLE_LOCATOR + ">" + collectionWithData.getCitableLocator() + "</" + E_COL_CITABLE_LOCATOR + ">\n" +
                        "        <" + E_COL_CONTACT_INFOS + ">\n" +
                        "            <" + E_COL_CONTACT_INFO + ">\n" +
                        "                <" + E_CONTACT_NAME + ">" + contactInfoOne.getName() + "</" + E_CONTACT_NAME + ">\n" +
                        "                <" + E_CONTACT_ROLE + ">" + contactInfoOne.getRole() + "</" + E_CONTACT_ROLE + ">\n" +
                        "                <" + E_CONTACT_EMAIL + ">" + contactInfoOne.getEmailAddress() + "</" + E_CONTACT_EMAIL + ">\n" +
                        "                <" + E_CONTACT_PHONE + ">" + contactInfoOne.getPhoneNumber() + "</" + E_CONTACT_PHONE + ">\n" +
                        "                <" + E_CONTACT_ADDRESS + ">\n" +
                        "                    <" + E_STREET_ADDRESS + ">" + contactInfoOne.getPhysicalAddress().getStreetAddress() + "</" + E_STREET_ADDRESS + ">\n" +
                        "                    <" + E_CITY + ">" + contactInfoOne.getPhysicalAddress().getCity() + "</" + E_CITY + ">\n" +
                        "                    <" + E_COUNTRY + ">" + contactInfoOne.getPhysicalAddress().getCountry() + "</" + E_COUNTRY + ">\n" +
                        "                </" + E_CONTACT_ADDRESS + ">\n" +
                        "            </" + E_COL_CONTACT_INFO + ">" +
                        "            <" + E_COL_CONTACT_INFO + ">\n" +
                        "                <" + E_CONTACT_NAME + ">" + contactInfoTwo.getName() + "</" + E_CONTACT_NAME + ">\n" +
                        "                <" + E_CONTACT_ROLE + ">" + contactInfoTwo.getRole() + "</" + E_CONTACT_ROLE + ">\n" +
                        "                <" + E_CONTACT_EMAIL + ">" + contactInfoTwo.getEmailAddress() + "</" + E_CONTACT_EMAIL + ">\n" +
                        "                <" + E_CONTACT_PHONE + ">" + contactInfoTwo.getPhoneNumber() + "</" + E_CONTACT_PHONE + ">\n" +
                        "            </" + E_COL_CONTACT_INFO + ">\n" +
                        "        </" + E_COL_CONTACT_INFOS + ">\n" +
                        "        <" + E_COL_CREATORS + ">\n" +
                        "            <" + E_COL_CREATOR + ">\n" +
                        "                <" + E_GIVEN_NAMES + ">" + creatorOne.getGivenNames() + "</" + E_GIVEN_NAMES + ">\n" +
                        "                <" + E_MIDDLE_NAMES + ">" + creatorOne.getMiddleNames() + "</" + E_MIDDLE_NAMES + ">\n" +
                        "                <" + E_FAMILY_NAMES + ">" + creatorOne.getFamilyNames() + "</" + E_FAMILY_NAMES + ">\n" +
                        "                <" + E_NAME_PREFIX + ">" + creatorOne.getPrefixes() + "</" + E_NAME_PREFIX + ">\n" +
                        "                <" + E_NAME_SUFFIX + ">" + creatorOne.getSuffixes() + "</" + E_NAME_SUFFIX + ">\n" +
                        "            </" + E_COL_CREATOR + ">\n" +
                        "            <" + E_COL_CREATOR + ">\n" +
                        "                <" + E_GIVEN_NAMES + ">" + creatorTwo.getGivenNames() + "</" + E_GIVEN_NAMES + ">\n" +
                        "                <" + E_MIDDLE_NAMES + ">" + creatorTwo.getMiddleNames() + "</" + E_MIDDLE_NAMES + ">\n" +
                        "                <" + E_FAMILY_NAMES + ">" + creatorTwo.getFamilyNames() + "</" + E_FAMILY_NAMES + ">\n" +
                        "                <" + E_NAME_PREFIX + ">" + creatorTwo.getPrefixes() + "</" + E_NAME_PREFIX + ">\n" +
                        "                <" + E_NAME_SUFFIX + ">" + creatorTwo.getSuffixes() + "</" + E_NAME_SUFFIX + ">\n" +
                        "            </" + E_COL_CREATOR + ">\n" +
                        "        </" + E_COL_CREATORS + ">\n" +
                        "        <" + E_COL_PUBLICATION_DATE + ">\n" +
                        "            <" + E_DATE + ">" + fmt.print(collectionWithData.getPublicationDate()) + "</" + E_DATE + ">\n" +
                        "        </" + E_COL_PUBLICATION_DATE + ">\n" +
                        "        <" + E_COL_DEPOSIT_DATE + ">\n" +
                        "            <" + E_DATE + ">" + fmt.print(collectionWithData.getDepositDate()) + "</" + E_DATE + ">\n" +
                        "        </" + E_COL_DEPOSIT_DATE + ">\n" +
                        "        <" + E_COL_DEPOSITOR + ">" + admin.getEmailAddress() + "</" + E_COL_DEPOSITOR + ">\n" +
                        "        <" + E_COL_PARENT_PROJECT + ">" + projectOne.getId() + "</" + E_COL_PARENT_PROJECT + ">\n" +
                        "    </" + E_COLLECTION + ">\n";

        XML2 = 
                "    <" + E_COLLECTION + " " + E_ID + "=\"" + collectionTwo.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                        "        <" + E_COL_ALTERNATE_IDS + ">\n" +
                        "            <" + E_COL_ALTERNATE_ID + ">" + collectionWithData.getAlternateIds().get(0) + "</" + E_COL_ALTERNATE_ID + ">\n" +
                        "        </" + E_COL_ALTERNATE_IDS + ">\n" +
                        "        <" + E_COL_TITLE + ">" + collectionWithData.getTitle() + "</" + E_COL_TITLE + ">\n" +
                        "        <" + E_COL_SUMMARY + ">" + collectionWithData.getSummary() + "</" + E_COL_SUMMARY + ">\n" +
                        "        <" + E_COL_CITABLE_LOCATOR + ">" + collectionWithData.getCitableLocator() + "</" + E_COL_CITABLE_LOCATOR + ">\n" +
                        "        <" + E_COL_CONTACT_INFOS + ">\n" +
                        "            <" + E_COL_CONTACT_INFO + ">\n" +
                        "                <" + E_CONTACT_NAME + ">" + contactInfoOne.getName() + "</" + E_CONTACT_NAME + ">\n" +
                        "                <" + E_CONTACT_ROLE + ">" + contactInfoOne.getRole() + "</" + E_CONTACT_ROLE + ">\n" +
                        "                <" + E_CONTACT_EMAIL + ">" + contactInfoOne.getEmailAddress() + "</" + E_CONTACT_EMAIL + ">\n" +
                        "                <" + E_CONTACT_PHONE + ">" + contactInfoOne.getPhoneNumber() + "</" + E_CONTACT_PHONE + ">\n" +
                        "                <" + E_CONTACT_ADDRESS + ">\n" +
                        "                    <" + E_STREET_ADDRESS + ">" + contactInfoOne.getPhysicalAddress().getStreetAddress() + "</" + E_STREET_ADDRESS + ">\n" +
                        "                    <" + E_CITY + ">" + contactInfoOne.getPhysicalAddress().getCity() + "</" + E_CITY + ">\n" +
                        "                    <" + E_COUNTRY + ">" + contactInfoOne.getPhysicalAddress().getCountry() + "</" + E_COUNTRY + ">\n" +
                        "                </" + E_CONTACT_ADDRESS + ">\n" +
                        "            </" + E_COL_CONTACT_INFO + ">" +
                        "            <" + E_COL_CONTACT_INFO + ">\n" +
                        "                <" + E_CONTACT_NAME + ">" + contactInfoTwo.getName() + "</" + E_CONTACT_NAME + ">\n" +
                        "                <" + E_CONTACT_ROLE + ">" + contactInfoTwo.getRole() + "</" + E_CONTACT_ROLE + ">\n" +
                        "                <" + E_CONTACT_EMAIL + ">" + contactInfoTwo.getEmailAddress() + "</" + E_CONTACT_EMAIL + ">\n" +
                        "                <" + E_CONTACT_PHONE + ">" + contactInfoTwo.getPhoneNumber() + "</" + E_CONTACT_PHONE + ">\n" +
                        "            </" + E_COL_CONTACT_INFO + ">\n" +
                        "        </" + E_COL_CONTACT_INFOS + ">\n" +
                        "        <" + E_COL_CREATORS + ">\n" +
                        "            <" + E_COL_CREATOR + ">\n" +
                        "                <" + E_GIVEN_NAMES + ">" + creatorOne.getGivenNames() + "</" + E_GIVEN_NAMES + ">\n" +
                        "                <" + E_MIDDLE_NAMES + ">" + creatorOne.getMiddleNames() + "</" + E_MIDDLE_NAMES + ">\n" +
                        "                <" + E_FAMILY_NAMES + ">" + creatorOne.getFamilyNames() + "</" + E_FAMILY_NAMES + ">\n" +
                        "                <" + E_NAME_PREFIX + ">" + creatorOne.getPrefixes() + "</" + E_NAME_PREFIX + ">\n" +
                        "                <" + E_NAME_SUFFIX + ">" + creatorOne.getSuffixes() + "</" + E_NAME_SUFFIX + ">\n" +
                        "            </" + E_COL_CREATOR + ">\n" +
                        "            <" + E_COL_CREATOR + ">\n" +
                        "                <" + E_GIVEN_NAMES + ">" + creatorTwo.getGivenNames() + "</" + E_GIVEN_NAMES + ">\n" +
                        "                <" + E_MIDDLE_NAMES + ">" + creatorTwo.getMiddleNames() + "</" + E_MIDDLE_NAMES + ">\n" +
                        "                <" + E_FAMILY_NAMES + ">" + creatorTwo.getFamilyNames() + "</" + E_FAMILY_NAMES + ">\n" +
                        "                <" + E_NAME_PREFIX + ">" + creatorTwo.getPrefixes() + "</" + E_NAME_PREFIX + ">\n" +
                        "                <" + E_NAME_SUFFIX + ">" + creatorTwo.getSuffixes() + "</" + E_NAME_SUFFIX + ">\n" +
                        "            </" + E_COL_CREATOR + ">\n" +
                        "        </" + E_COL_CREATORS + ">\n" +
                        "        <" + E_COL_PUBLICATION_DATE + ">\n" +
                        "            <" + E_DATE + ">" + fmt.print(collectionWithData.getPublicationDate()) + "</" + E_DATE + ">\n" +
                        "        </" + E_COL_PUBLICATION_DATE + ">\n" +
                        "        <" + E_COL_DEPOSIT_DATE + ">\n" +
                        "            <" + E_DATE + ">" + fmt.print(collectionWithData.getDepositDate()) + "</" + E_DATE + ">\n" +
                        "        </" + E_COL_DEPOSIT_DATE + ">\n" +
                        "        <" + E_COL_DEPOSITOR + ">" + admin.getEmailAddress() + "</" + E_COL_DEPOSITOR + ">\n" +
                        "        <" + E_COL_CHILDREN_IDS + ">\n" +
                        "            <" + E_COL_CHILDREN_ID + ">" + collectionThree.getId() + "</" + E_COL_CHILDREN_ID + ">\n" +
                        "        </" + E_COL_CHILDREN_IDS + ">\n" +
                        "        <" + E_COL_PARENT_PROJECT + ">" + projectOne.getId() + "</" + E_COL_PARENT_PROJECT + ">\n" +
                        "    </" + E_COLLECTION + ">\n";
        XML3 =
                "    <" + E_COLLECTION + " " + E_ID + "=\"" + collectionThree.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                "        <" + E_COL_ALTERNATE_IDS + ">\n" +
                "            <" + E_COL_ALTERNATE_ID + ">" + collectionWithData.getAlternateIds().get(0) + "</" + E_COL_ALTERNATE_ID + ">\n" +
                "        </" + E_COL_ALTERNATE_IDS + ">\n" +
                "        <" + E_COL_TITLE + ">" + collectionWithData.getTitle() + "</" + E_COL_TITLE + ">\n" +
                "        <" + E_COL_SUMMARY + ">" + collectionWithData.getSummary() + "</" + E_COL_SUMMARY + ">\n" +
                "        <" + E_COL_CITABLE_LOCATOR + ">" + collectionWithData.getCitableLocator() + "</" + E_COL_CITABLE_LOCATOR + ">\n" +
                "        <" + E_COL_CONTACT_INFOS + ">\n" +
                "            <" + E_COL_CONTACT_INFO + ">\n" +
                "                <" + E_CONTACT_NAME + ">" + contactInfoOne.getName() + "</" + E_CONTACT_NAME + ">\n" +
                "                <" + E_CONTACT_ROLE + ">" + contactInfoOne.getRole() + "</" + E_CONTACT_ROLE + ">\n" +
                "                <" + E_CONTACT_EMAIL + ">" + contactInfoOne.getEmailAddress() + "</" + E_CONTACT_EMAIL + ">\n" +
                "                <" + E_CONTACT_PHONE + ">" + contactInfoOne.getPhoneNumber() + "</" + E_CONTACT_PHONE + ">\n" +
                "                <" + E_CONTACT_ADDRESS + ">\n" +
                "                    <" + E_STREET_ADDRESS + ">" + contactInfoOne.getPhysicalAddress().getStreetAddress() + "</" + E_STREET_ADDRESS + ">\n" +
                "                    <" + E_CITY + ">" + contactInfoOne.getPhysicalAddress().getCity() + "</" + E_CITY + ">\n" +
                "                    <" + E_COUNTRY + ">" + contactInfoOne.getPhysicalAddress().getCountry() + "</" + E_COUNTRY + ">\n" +
                "                </" + E_CONTACT_ADDRESS + ">\n" +
                "            </" + E_COL_CONTACT_INFO + ">" +
                "            <" + E_COL_CONTACT_INFO + ">\n" +
                "                <" + E_CONTACT_NAME + ">" + contactInfoTwo.getName() + "</" + E_CONTACT_NAME + ">\n" +
                "                <" + E_CONTACT_ROLE + ">" + contactInfoTwo.getRole() + "</" + E_CONTACT_ROLE + ">\n" +
                "                <" + E_CONTACT_EMAIL + ">" + contactInfoTwo.getEmailAddress() + "</" + E_CONTACT_EMAIL + ">\n" +
                "                <" + E_CONTACT_PHONE + ">" + contactInfoTwo.getPhoneNumber() + "</" + E_CONTACT_PHONE + ">\n" +
                "            </" + E_COL_CONTACT_INFO + ">\n" +
                "        </" + E_COL_CONTACT_INFOS + ">\n" +
                "        <" + E_COL_CREATORS + ">\n" +
                "            <" + E_COL_CREATOR + ">\n" +
                "                <" + E_GIVEN_NAMES + ">" + creatorOne.getGivenNames() + "</" + E_GIVEN_NAMES + ">\n" +
                "                <" + E_MIDDLE_NAMES + ">" + creatorOne.getMiddleNames() + "</" + E_MIDDLE_NAMES + ">\n" +
                "                <" + E_FAMILY_NAMES + ">" + creatorOne.getFamilyNames() + "</" + E_FAMILY_NAMES + ">\n" +
                "                <" + E_NAME_PREFIX + ">" + creatorOne.getPrefixes() + "</" + E_NAME_PREFIX + ">\n" +
                "                <" + E_NAME_SUFFIX + ">" + creatorOne.getSuffixes() + "</" + E_NAME_SUFFIX + ">\n" +
                "            </" + E_COL_CREATOR + ">\n" +
                "            <" + E_COL_CREATOR + ">\n" +
                "                <" + E_GIVEN_NAMES + ">" + creatorTwo.getGivenNames() + "</" + E_GIVEN_NAMES + ">\n" +
                "                <" + E_MIDDLE_NAMES + ">" + creatorTwo.getMiddleNames() + "</" + E_MIDDLE_NAMES + ">\n" +
                "                <" + E_FAMILY_NAMES + ">" + creatorTwo.getFamilyNames() + "</" + E_FAMILY_NAMES + ">\n" +
                "                <" + E_NAME_PREFIX + ">" + creatorTwo.getPrefixes() + "</" + E_NAME_PREFIX + ">\n" +
                "                <" + E_NAME_SUFFIX + ">" + creatorTwo.getSuffixes() + "</" + E_NAME_SUFFIX + ">\n" +
                "            </" + E_COL_CREATOR + ">\n" +
                "        </" + E_COL_CREATORS + ">\n" +
                "        <" + E_COL_PUBLICATION_DATE + ">\n" +
                "            <" + E_DATE + ">" + fmt.print(collectionWithData.getPublicationDate()) + "</" + E_DATE + ">\n" +
                "        </" + E_COL_PUBLICATION_DATE + ">\n" +
                "        <" + E_COL_DEPOSIT_DATE + ">\n" +
                "            <" + E_DATE + ">" + fmt.print(collectionWithData.getDepositDate()) + "</" + E_DATE + ">\n" +
                "        </" + E_COL_DEPOSIT_DATE + ">\n" +
                "        <" + E_COL_DEPOSITOR + ">" + admin.getEmailAddress() + "</" + E_COL_DEPOSITOR + ">\n" +
                "        <" + E_COL_PARENT_ID + ">" + collectionTwo.getId() + "</" + E_COL_PARENT_ID + ">\n" +
                "        <" + E_COL_PARENT_PROJECT + ">" + projectOne.getId() + "</" + E_COL_PARENT_PROJECT + ">\n" +
                "    </" + E_COLLECTION + ">\n";

    }

    /**
     * Used to wrap Collection serializations in a {@literal <collections>} element for validation purposes.
     */
    private final static String COLLECTIONS_WRAPPER = "<collections>\n%s\n</collections>";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        collectionWithData.setDepositorId(admin.getEmailAddress());
        collectionTwo = new Collection(collectionWithData);
        collectionTwo.setId("collectionTwo:id");
        collectionTwo.setParentProjectId(projectOne.getId());
        
        collectionThree = new Collection(collectionWithData);
        collectionThree.setId("collectionThree:id");
        collectionThree.setParentId(collectionTwo.getId());
        collectionThree.setParentProjectId(projectOne.getId());
        List<String> childrenIds = new ArrayList<String>();
        childrenIds.add(collectionThree.getId());
        collectionTwo.setChildrenIds(childrenIds);
        setupXMLString();
    }

    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(collectionWithData));
        XMLAssert.assertXMLEqual(XML2, x.toXML(collectionTwo));
        XMLAssert.assertXMLEqual(XML3, x.toXML(collectionThree));
    }

    @Test
    public void testUnmarshal() {
        Collection actual = (Collection) x.fromXML(XML);
        assertEquals(collectionWithData, actual);
        actual = (Collection) x.fromXML(XML2);
        assertEquals(collectionTwo, actual);
        actual = (Collection) x.fromXML(XML3);
        assertEquals(collectionThree, actual);
        assertEquals(collectionWithData, x.fromXML(x.toXML(collectionWithData)));
        assertEquals(collectionTwo, x.fromXML(x.toXML(collectionTwo)));
        assertEquals(collectionThree, x.fromXML(x.toXML(collectionThree)));
    }

    /**
     * Test which insures that the expected XML is valid, marshaled XML is valid, and round-tripped XML is valid.
     * 
     * @throws Exception
     */
    @Test
    public void testMarshalIsValid() throws Exception {
        // Verify assumptions: test that our expected XML is valid.
        final String expectedXml = String.format(BOP_WRAPPER, String.format(COLLECTIONS_WRAPPER, XML));
        final String expectedXml2 = String.format(BOP_WRAPPER, String.format(COLLECTIONS_WRAPPER, XML2));
        final String expectedXml3 = String.format(BOP_WRAPPER, String.format(COLLECTIONS_WRAPPER, XML3));

        final Document expectedDom = parser.parse(IOUtils.toInputStream(expectedXml));
        final Document expectedDom2 = parser.parse(IOUtils.toInputStream(expectedXml2));
        final Document expectedDom3 = parser.parse(IOUtils.toInputStream(expectedXml3));
        validator.validate(new DOMSource(expectedDom));
        validator.validate(new DOMSource(expectedDom2));
        validator.validate(new DOMSource(expectedDom3));

        // Verify that our serialized XML is valid.
        final String actualXml = String.format(BOP_WRAPPER, String.format(COLLECTIONS_WRAPPER, x.toXML(collectionWithData)));
        final String actualXml2 = String
                .format(BOP_WRAPPER, String.format(COLLECTIONS_WRAPPER, x.toXML(collectionTwo)));
        final String actualXml3 = String.format(BOP_WRAPPER,
                String.format(COLLECTIONS_WRAPPER, x.toXML(collectionThree)));
        final Document actualDom = parser.parse(IOUtils.toInputStream(actualXml));
        final Document actualDom2 = parser.parse(IOUtils.toInputStream(actualXml2));
        final Document actualDom3 = parser.parse(IOUtils.toInputStream(actualXml3));
        validator.validate(new DOMSource(actualDom));
        validator.validate(new DOMSource(actualDom2));
        validator.validate(new DOMSource(actualDom3));

        // Verify that round-tripped XML is valid (XML -> Object -> XML)
        final String roundTrippedXml = String.format(BOP_WRAPPER, String.format(COLLECTIONS_WRAPPER, x.toXML(x.fromXML(XML))));
        final String roundTrippedXml2 = String.format(BOP_WRAPPER,
                String.format(COLLECTIONS_WRAPPER, x.toXML(x.fromXML(XML2))));
        final String roundTrippedXml3 = String.format(BOP_WRAPPER,
                String.format(COLLECTIONS_WRAPPER, x.toXML(x.fromXML(XML3))));
        final Document roundTrippedDom = parser.parse(IOUtils.toInputStream(roundTrippedXml));
        final Document roundTrippedDom2 = parser.parse(IOUtils.toInputStream(roundTrippedXml2));
        final Document roundTrippedDom3 = parser.parse(IOUtils.toInputStream(roundTrippedXml3));
        validator.validate(new DOMSource(roundTrippedDom));
        validator.validate(new DOMSource(roundTrippedDom2));
        validator.validate(new DOMSource(roundTrippedDom3));
    }
}
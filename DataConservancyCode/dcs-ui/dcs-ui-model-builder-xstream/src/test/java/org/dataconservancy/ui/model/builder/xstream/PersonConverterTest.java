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

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;

import org.junit.Before;
import org.junit.Test;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import static org.dataconservancy.ui.model.builder.xstream.ConverterTestConstants.PERSONS_WRAPPER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.dom.DOMSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A unit test for the person converter.
 */
public class PersonConverterTest
        extends BaseConverterTest
        implements ConverterConstants {

    static final String XMLNS = "http://dataconservancy.org/schemas/bop/1.0";

    final Logger log = LoggerFactory.getLogger(this.getClass());

    private String XML;

    private void setupXML() {
        XML =
                "    <" + E_PERSON + " " + E_ID + "=\"" + user.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                "        <" + E_PREFIX + ">" + user.getPrefix() + "</" + E_PREFIX + ">\n" +
                "        <" + E_FIRST_NAME + ">" + user.getFirstNames() + "</"+ E_FIRST_NAME + ">\n" +
                "        <" + E_MIDDLE_NAME + ">" + user.getMiddleNames() + "</" + E_MIDDLE_NAME + ">\n" + 
                "        <" + E_LAST_NAME + ">" + user.getLastNames() + "</" + E_LAST_NAME + ">\n" +
                "        <" + E_SUFFIX + ">" + user.getSuffix() + "</" + E_SUFFIX + ">\n" +
                "        <" + E_EMAIL_ADDRESS + ">" + user.getEmailAddress() + "</"+ E_EMAIL_ADDRESS + ">\n" +
                "        <" + E_PREFERRED_PUB_NAME + ">" + user.getPreferredPubName() + "</" + E_PREFERRED_PUB_NAME + ">\n" +
                "        <" + E_BIO + ">" + user.getBio() + "</" + E_BIO + ">\n" +
                "        <" + E_WEBSITE + ">" + user.getWebsite() + "</" + E_WEBSITE + ">\n" +
                "        <" + E_CITY + ">" + user.getCity() + "</" + E_CITY + ">\n" +
                "        <" + E_STATE + ">" + user.getState() + "</" + E_STATE + ">\n" +
                "        <" + E_JOB_TITLE + ">" + user.getJobTitle() + "</" + E_JOB_TITLE + ">\n" +
                "        <" + E_DEPARTMENT + ">" + user.getDepartment() + "</" + E_DEPARTMENT + ">\n" +
                "        <" + E_INST_COMPANY + ">" + user.getInstCompany() + "</" + E_INST_COMPANY + ">\n" +
                "        <" + E_INST_COMPANY_WEBSITE + ">" + user.getInstCompanyWebsite() + "</" + E_INST_COMPANY_WEBSITE + ">\n" +
                "        <" + E_PHONE_NUMBER + ">" + user.getPhoneNumber() + "</" + E_PHONE_NUMBER + ">\n" +
                "        <" + E_PASSWORD + ">" + user.getPassword() + "</" + E_PASSWORD + ">\n" +
                "        <" + E_EXTERNAL_STORAGE_LINKED + ">" + user.isExternalStorageLinked() + "</" + E_EXTERNAL_STORAGE_LINKED + ">\n" +
                "        <" + E_DROPBOX_APP_KEY + ">" + user.getDropboxAppKey() + "</" + E_DROPBOX_APP_KEY + ">\n" +
                "        <" + E_DROPBOX_APP_SECRET + ">" + user.getDropboxAppSecret() + "</" + E_DROPBOX_APP_SECRET + ">\n" +
                "        <" + E_REGISTRATION_STATUS + ">" + user.getRegistrationStatus().name() + "</" + E_REGISTRATION_STATUS + ">\n" +
                "        <" + E_READ_ONLY + ">" + user.getReadOnly() + "</" + E_READ_ONLY + ">\n" +
                "        <" + E_ROLE + ">" + user.getRoles().get(0) + "</" + E_ROLE + ">\n" +
                "    </" + E_PERSON + ">\n";
    }

    @Before
    public void setUp() throws Exception {

        super.setUp();
        setupXML();
    }

    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(user));
        assertTrue(true);
    }

    @Test
    public void testUnmarshal() {
        Person actual = (Person) x.fromXML(XML);
        assertEquals(user, actual);
        assertEquals(user, x.fromXML(x.toXML(user)));
    }

    /**
     * Test which insures that the expected XML is valid, marshaled XML is valid, and round-tripped XML is valid.
     *
     * @throws Exception
     */
    @Test
    public void testMarshalIsValid() throws Exception {
        // Verify assumptions: test that our expected XML is valid.
        final String expectedXml = String.format(BOP_WRAPPER, String.format(PERSONS_WRAPPER, XML));

        final Document expectedDom = parser.parse(IOUtils.toInputStream(expectedXml));
        validator.validate(new DOMSource(expectedDom));

        // Verify that our serialized XML is valid.
        final String actualXml = String.format(BOP_WRAPPER, String.format(PERSONS_WRAPPER, x.toXML(user)));
        final Document actualDom = parser.parse(IOUtils.toInputStream(actualXml));
        validator.validate(new DOMSource(actualDom));

        // Verify that round-tripped XML is valid (XML -> Object -> XML)
        final String roundTrippedXml = String.format(BOP_WRAPPER, String.format(PERSONS_WRAPPER, x.toXML(x.fromXML(XML))));
        final Document roundTrippedDom = parser.parse(IOUtils.toInputStream(roundTrippedXml));
        validator.validate(new DOMSource(roundTrippedDom));
    }

    /**
     * Test that there are no errors when converting people with empty fields.
     */
    @Test
    public void emptyFieldsTest() {
        //Create a person with all fields.
        Person person = new Person();
        person.setId("id");
        person.setFirstNames("first");
        person.setLastNames("last");
        person.setMiddleNames("middle");
        person.setPrefix("prefix");
        person.setSuffix("suffix");
        person.setPassword("pass");
        person.setEmailAddress("email");
        person.setPhoneNumber("phone");
        person.setJobTitle("Job Title");
        person.setDepartment("Department");
        person.setCity("Baltimore");
        person.setState("Maryland");
        person.setBio("bio");
        person.setWebsite("www.website.com");
        person.setInstCompany("Institution/Company");
        person.setInstCompanyWebsite("www.InstitutionCompanyWebsite.com");
        person.setRegistrationStatus(RegistrationStatus.PENDING);
        person.addRole(Role.ROLE_USER);
        person.setExternalStorageLinked(false);
        person.setDropboxAppKey("SomeKey");
        person.setDropboxAppSecret("SomeSecret");

        //Make sure were equal to start
        assertEquals(person, x.fromXML(x.toXML(person)));

        //Test no first name.
        person.setFirstNames(null);
        assertEquals(person, x.fromXML(x.toXML(person)));

        //Test no last name.
        person.setLastNames(null);
        assertEquals(person, x.fromXML(x.toXML(person)));

        //Test no password
        person.setPassword(null);
        assertEquals(person, x.fromXML(x.toXML(person)));

        //Test no email address
        person.setEmailAddress(null);
        assertEquals(person, x.fromXML(x.toXML(person)));

        //Test no phone number
        person.setPhoneNumber(null);
        assertEquals(person, x.fromXML(x.toXML(person)));

        //Test no reg status
        person.setRegistrationStatus(null);
        assertEquals(person, x.fromXML(x.toXML(person)));

        //Test no role and completely empty person
        person.setRoles(null);
        assertEquals(person, x.fromXML(x.toXML(person)));
    }
}

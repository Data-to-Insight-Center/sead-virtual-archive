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

package org.dataconservancy.ui.it;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.junit.Test;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.UiUrlConfig;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.dataconservancy.ui.model.RegistrationStatus.PENDING;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static junit.framework.Assert.assertEquals;

/**
 * Tests related to verifying the behavior of card <a
 * href="https://scm.dataconservancy.org/issues/browse/DC-1002">DC-1002</a> We
 * test the behavior of UserService policies related to the creation of users in
 * the system.
 */
public class HttpPersonIT
        extends BaseIT {

    @Autowired
    protected UiUrlConfig urlConfig;

    //HttpClient to use for testing url calls
    private HttpClient httpClient = new DefaultHttpClient();

    @Autowired
    BusinessObjectBuilder businessObjectBuilder;

    /**
     * Tests adding a user through the API, request has anonymous permissions.
     * Result should be a successful addition of the user, which is verified by
     * checking the content on the response.
     * 
     * @throws java.io.IOException
     * @throws org.apache.http.client.ClientProtocolException
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     * @throws IllegalStateException
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testCreateNewUser() throws ClientProtocolException,
            IOException, IllegalStateException, InvalidXmlException,
            URISyntaxException {

        Person newPerson = new Person();
        newPerson.setId("ooogaboogaboo");
        newPerson.setFirstNames("Hanh");
        newPerson.setLastNames("Vu");
        newPerson.setPrefix("Ms.");
        newPerson.setSuffix("II");
        newPerson.setMiddleNames("Middle");
        newPerson.setPreferredPubName("H. Vu");
        newPerson.setBio("Some bio for the user.");
        newPerson.setWebsite("www.somewebsite.com");
        newPerson.setPassword("bumpassword");
        newPerson.setEmailAddress("nobody@nowhere.com");
        newPerson.setPhoneNumber("507.555.1212");
        newPerson.setJobTitle("Hanh Scientist");
        newPerson.setDepartment("Hanh Department");
        newPerson.setCity("Baltimore");
        newPerson.setState("Maryland");
        newPerson.setInstCompany("Hanh Institution/Company");
        newPerson.setInstCompanyWebsite("www.HanhInstitutionCompany.com");
        newPerson.setRegistrationStatus(RegistrationStatus.PENDING);
        newPerson.setExternalStorageLinked(false);
        newPerson.setDropboxAppKey("SomeKey");
        newPerson.setDropboxAppSecret("SomeSecret");

        HttpPost request = buildAddRequest(newPerson);

        HttpResponse unauthorizedResponse = httpClient.execute(request);
        assertEquals("Unable to add new person. Status code: "
                             + unauthorizedResponse.getStatusLine(),
                     200,
                     unauthorizedResponse.getStatusLine().getStatusCode());

        InputStream content = unauthorizedResponse.getEntity().getContent();
        int contentLength = content.available();

        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(content);
        assertNotNull(bop);

        Header header = unauthorizedResponse.getEntity().getContentType();

        assertEquals("Content-Type: text/xml;charset=UTF-8", header.toString());

        long length = unauthorizedResponse.getEntity().getContentLength();
        assertTrue(length > 0);
        assertEquals(contentLength, length);

        Set<Person> persons = bop.getPersons();
        assertNotNull(persons);
        assertEquals(1, persons.size());

        Person returnedPerson = persons.iterator().next();
        assertNotNull(returnedPerson);

        unauthorizedResponse.getEntity().getContent().close();
        freeResponse(unauthorizedResponse);

    }

    /**
     * Tests adding a user through the API without explicitly specifying an ID.
     * Result should be a successful addition of the user with a system-supplied
     * Id, which is verified by checking the content on the response.
     * 
     * @throws org.apache.http.client.ClientProtocolException
     * @throws java.io.IOException
     * @throws IllegalStateException
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testCreateNewUserWithNullId() throws ClientProtocolException,
            IOException, IllegalStateException, InvalidXmlException,
            URISyntaxException {

        Person toCreate = new Person();
        toCreate.setEmailAddress("bogus1@nowhere.org");
        toCreate.setFirstNames("Bo");
        toCreate.setLastNames("Gus");
        toCreate.setPrefix("Ms.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("B. Gus");
        toCreate.setBio("Some bio for the user.");
        toCreate.setWebsite("www.somewebsite.com");
        toCreate.setPassword("foobarpass");
        toCreate.setPhoneNumber("1234567890");
        toCreate.setJobTitle("Bo Scientist");
        toCreate.setDepartment("Bo Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Bo Institution/Company");
        toCreate.setInstCompanyWebsite("www.BoInstitutionCompany.com");
        toCreate.setRegistrationStatus(PENDING);
        toCreate.setExternalStorageLinked(false);
        toCreate.setDropboxAppKey("SomeKey");
        toCreate.setDropboxAppSecret("SomeSecret");

        HttpPost request = buildAddRequest(toCreate);

        HttpResponse unauthorizedResponse = httpClient.execute(request);
        assertEquals("Unable to add new person. Status code: "
                             + unauthorizedResponse.getStatusLine(),
                     200,
                     unauthorizedResponse.getStatusLine().getStatusCode());
        Bop bop =
                businessObjectBuilder
                        .buildBusinessObjectPackage(unauthorizedResponse
                                .getEntity().getContent());
        assertNotNull(bop);

        Header header = unauthorizedResponse.getEntity().getContentType();

        assertEquals("Content-Type: text/xml;charset=UTF-8", header.toString());

        Set<Person> persons = bop.getPersons();
        assertNotNull(persons);
        assertEquals(1, persons.size());

        Person returnedPerson = persons.iterator().next();
        assertNotNull(returnedPerson);

        assertNotNull(returnedPerson.getId());
        assertTrue(returnedPerson.getId().length() > 0);

        unauthorizedResponse.getEntity().getContent().close();
        freeResponse(unauthorizedResponse);

    }

    /**
     * Tests adding a user through the API specifying an empty ID. Result should
     * be a successful addition of the user with a system-supplied Id, which is
     * verified by checking the content on the response.
     * 
     * @throws org.apache.http.client.ClientProtocolException
     * @throws java.io.IOException
     * @throws IllegalStateException
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testCreateNewUserWithEmptyId() throws ClientProtocolException,
            IOException, IllegalStateException, InvalidXmlException,
            URISyntaxException {

        Person toCreate = new Person();
        toCreate.setEmailAddress("bogus2@nowhere.org");
        toCreate.setFirstNames("Bo");
        toCreate.setLastNames("Gus");
        toCreate.setPrefix("Ms.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("B. Gus");
        toCreate.setBio("Some bio for the user.");
        toCreate.setWebsite("www.somewebsite.com");
        toCreate.setPassword("foobarpass");
        toCreate.setPhoneNumber("1234567890");
        toCreate.setJobTitle("Bo Scientist");
        toCreate.setDepartment("Bo Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Bo Institution/Company");
        toCreate.setInstCompanyWebsite("www.BoInstitutionCompany.com");
        toCreate.setRegistrationStatus(PENDING);
        toCreate.setId("      ");
        toCreate.setExternalStorageLinked(false);
        toCreate.setDropboxAppKey("SomeKey");
        toCreate.setDropboxAppSecret("SomeSecret");

        HttpPost request = buildAddRequest(toCreate);

        HttpResponse unauthorizedResponse = httpClient.execute(request);
        assertEquals("Unable to add new person. Status code: "
                             + unauthorizedResponse.getStatusLine(),
                     200,
                     unauthorizedResponse.getStatusLine().getStatusCode());
        Bop bop =
                businessObjectBuilder
                        .buildBusinessObjectPackage(unauthorizedResponse
                                .getEntity().getContent());
        assertNotNull(bop);

        Header header = unauthorizedResponse.getEntity().getContentType();

        assertEquals("Content-Type: text/xml;charset=UTF-8", header.toString());

        Set<Person> persons = bop.getPersons();
        assertNotNull(persons);
        assertEquals(1, persons.size());

        Person returnedPerson = persons.iterator().next();
        assertNotNull(returnedPerson);

        assertNotNull(returnedPerson.getId());
        assertTrue(returnedPerson.getId().length() > 0);

        unauthorizedResponse.getEntity().getContent().close();
        freeResponse(unauthorizedResponse);

    }

    /**
     * Tests adding a user through the API specifying a whitespace ID. Result
     * should be a successful addition of the user with a system-supplied Id,
     * which is verified by checking the content on the response.
     * 
     * @throws org.apache.http.client.ClientProtocolException
     * @throws java.io.IOException
     * @throws IllegalStateException
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     * @throws java.net.URISyntaxException
     */

    @Test
    public void testCreateNewUserWithWhitespaceId()
            throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {

        Person toCreate = new Person();
        toCreate.setEmailAddress("bogus3@nowhere.org");
        toCreate.setFirstNames("Bo");
        toCreate.setLastNames("Gus");
        toCreate.setPrefix("Ms.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("B. Gus");
        toCreate.setBio("Some bio for the user.");
        toCreate.setWebsite("www.somewebsite.com");
        toCreate.setPassword("foobarpass");
        toCreate.setPhoneNumber("1234567890");
        toCreate.setJobTitle("Bo Scientist");
        toCreate.setDepartment("Bo Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Bo Institution/Company");
        toCreate.setInstCompanyWebsite("www.BoInstitutionCompany.com");
        toCreate.setRegistrationStatus(PENDING);
        toCreate.setId("");
        toCreate.setExternalStorageLinked(false);
        toCreate.setDropboxAppKey("SomeKey");
        toCreate.setDropboxAppSecret("SomeSecret");

        HttpPost request = buildAddRequest(toCreate);

        HttpResponse unauthorizedResponse = httpClient.execute(request);
        assertEquals("Unable to add new person. Status code: "
                             + unauthorizedResponse.getStatusLine(),
                     200,
                     unauthorizedResponse.getStatusLine().getStatusCode());
        Bop bop =
                businessObjectBuilder
                        .buildBusinessObjectPackage(unauthorizedResponse
                                .getEntity().getContent());
        assertNotNull(bop);

        Header header = unauthorizedResponse.getEntity().getContentType();

        assertEquals("Content-Type: text/xml;charset=UTF-8", header.toString());

        Set<Person> persons = bop.getPersons();
        assertNotNull(persons);
        assertEquals(1, persons.size());

        Person returnedPerson = persons.iterator().next();
        assertNotNull(returnedPerson);

        assertNotNull(returnedPerson.getId());
        assertTrue(returnedPerson.getId().length() > 0);

        unauthorizedResponse.getEntity().getContent().close();
        freeResponse(unauthorizedResponse);

    }

    /**
     * Tests adding a user through the API specifying an acceptable ID. Result
     * should be a successful addition of the user with the supplied Id, which
     * is verified by checking the content on the response.
     * 
     * @throws org.apache.http.client.ClientProtocolException
     * @throws java.io.IOException
     * @throws IllegalStateException
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     * @throws java.net.URISyntaxException
     */

    @Test
    public void testCreateNewUserWithExistingId()
            throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {

        Person toCreate = new Person();
        toCreate.setEmailAddress("bogus4@nowhere.org");
        toCreate.setFirstNames("Bo");
        toCreate.setLastNames("Gus");
        toCreate.setPrefix("Ms.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("B. Gus");
        toCreate.setBio("Some bio for the user.");
        toCreate.setWebsite("www.somewebsite.com");
        toCreate.setPassword("foobarpass");
        toCreate.setPhoneNumber("1234567890");
        toCreate.setJobTitle("Bo Scientist");
        toCreate.setDepartment("Bo Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Bo Institution/Company");
        toCreate.setInstCompanyWebsite("www.BoInstitutionCompany.com");
        toCreate.setRegistrationStatus(PENDING);
        toCreate.setId("http:bogus4-valid-id");
        toCreate.setExternalStorageLinked(false);
        toCreate.setDropboxAppKey("SomeKey");
        toCreate.setDropboxAppSecret("SomeSecret");

        HttpPost request = buildAddRequest(toCreate);

        HttpResponse unauthorizedResponse = httpClient.execute(request);
        assertEquals("Unable to add new person. Status code: "
                             + unauthorizedResponse.getStatusLine(),
                     200,
                     unauthorizedResponse.getStatusLine().getStatusCode());
        Bop bop =
                businessObjectBuilder
                        .buildBusinessObjectPackage(unauthorizedResponse
                                .getEntity().getContent());
        assertNotNull(bop);

        Header header = unauthorizedResponse.getEntity().getContentType();

        assertEquals("Content-Type: text/xml;charset=UTF-8", header.toString());

        Set<Person> persons = bop.getPersons();
        assertNotNull(persons);
        assertEquals(1, persons.size());

        Person returnedPerson = persons.iterator().next();
        assertNotNull(returnedPerson);

        assertNotNull(returnedPerson.getId());
        assertEquals(returnedPerson.getId(), toCreate.getId());

        unauthorizedResponse.getEntity().getContent().close();
        freeResponse(unauthorizedResponse);

    }

    /**
     * Tests adding a user through the API without explicitly specifying a
     * registration status. Result should be a successful addition of the user
     * with a system-supplied registration status of PENDING, which is verified
     * by checking the content on the response.
     * 
     * @throws org.apache.http.client.ClientProtocolException
     * @throws java.io.IOException
     * @throws IllegalStateException
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testCreateNewUserWithoutRegistrationStatus()
            throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {

        Person newPerson = new Person();
        newPerson.setFirstNames("Moe");
        newPerson.setLastNames("Howard");
        newPerson.setPrefix("Mr.");
        newPerson.setSuffix("II");
        newPerson.setMiddleNames("Middle");
        newPerson.setPreferredPubName("M. Howard");
        newPerson.setBio("Some bio for the user.");
        newPerson.setWebsite("www.somewebsite.com");
        newPerson.setPassword("bumpassword");
        newPerson.setEmailAddress("moe@nowhere.com");
        newPerson.setPhoneNumber("507.555.1212");
        newPerson.setJobTitle("Moe Scientist");
        newPerson.setDepartment("Moe Department");
        newPerson.setCity("Baltimore");
        newPerson.setState("Maryland");
        newPerson.setInstCompany("Moe Institution/Company");
        newPerson.setInstCompanyWebsite("www.MoeInstitutionCompany.com");
        newPerson.setExternalStorageLinked(false);
        newPerson.setDropboxAppKey("SomeKey");
        newPerson.setDropboxAppSecret("SomeSecret");

        HttpPost request = buildAddRequest(newPerson);

        HttpResponse unauthorizedResponse = httpClient.execute(request);
        assertEquals("Unable to add new person. Status code: "
                             + unauthorizedResponse.getStatusLine(),
                     200,
                     unauthorizedResponse.getStatusLine().getStatusCode());
        Bop bop =
                businessObjectBuilder
                        .buildBusinessObjectPackage(unauthorizedResponse
                                .getEntity().getContent());
        assertNotNull(bop);

        Header header = unauthorizedResponse.getEntity().getContentType();

        assertEquals("Content-Type: text/xml;charset=UTF-8", header.toString());

        Set<Person> persons = bop.getPersons();
        assertNotNull(persons);
        assertEquals(1, persons.size());

        Person returnedPerson = persons.iterator().next();
        assertNotNull(returnedPerson);
        assertEquals(RegistrationStatus.PENDING,
                     returnedPerson.getRegistrationStatus());

        unauthorizedResponse.getEntity().getContent().close();
        freeResponse(unauthorizedResponse);

    }

    /**
     * Test attempting to add a user through the API with bad XML supplied.
     * Result should be a 415 error for bad XML.
     * 
     * @throws org.apache.http.client.ClientProtocolException
     * @throws java.io.IOException
     * @throws IllegalStateException
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testBadXmlException() throws ClientProtocolException,
            IOException, IllegalStateException, InvalidXmlException,
            URISyntaxException {
        HttpPost request = null;

        String arguments = "/person";
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        sink.write(6); //provide invalid xml where the bop should be
        ByteArrayEntity personEntity = new ByteArrayEntity(sink.toByteArray());
        personEntity.setContentEncoding("UTF-8");
        URL baseUrl = urlConfig.getBaseUrl();

        URI uri =
                URIUtils.createURI(baseUrl.getProtocol(),
                                   baseUrl.getHost(),
                                   baseUrl.getPort(),
                                   arguments,
                                   null,
                                   null);

        request = new HttpPost(uri);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setEntity(personEntity);

        HttpResponse unauthorizedResponse = httpClient.execute(request);
        assertEquals("Did not receive expected bad XML exception "
                             + unauthorizedResponse.getStatusLine(),
                     415,
                     unauthorizedResponse.getStatusLine().getStatusCode());
        freeResponse(unauthorizedResponse);
    }

    /**
     * Builds the add request for the supplied Person.
     * 
     * @param person
     * @return
     * @throws java.io.UnsupportedEncodingException
     * @throws java.net.URISyntaxException
     */
    private HttpPost buildAddRequest(Person person)
            throws UnsupportedEncodingException, URISyntaxException {
        HttpPost request = null;

        String arguments = "/person";

        ByteArrayOutputStream sink = new ByteArrayOutputStream();

        Bop bop = new Bop();
        bop.addPerson(person);
        businessObjectBuilder.buildBusinessObjectPackage(bop, sink);
        // businessObjectBuilder.buildPerson(person, sink);
        ByteArrayEntity personEntity = new ByteArrayEntity(sink.toByteArray());
        personEntity.setContentEncoding("UTF-8");

        URL baseUrl = urlConfig.getBaseUrl();

        URI uri =
                URIUtils.createURI(baseUrl.getProtocol(),
                                   baseUrl.getHost(),
                                   baseUrl.getPort(),
                                   arguments,
                                   null,
                                   null);

        request = new HttpPost(uri);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setEntity(personEntity);
        return request;
    }

}

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

package org.dataconservancy.ui.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpStatus;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.api.support.MockHttpServletRequestFactory;
import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.util.ETagCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;

import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.APPLICATION_XML;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ETAG;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.LAST_MODIFIED;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.LOCATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the behavior of PersonController Created by IntelliJ IDEA. User: HanhVu
 * Date: 5/24/12 Time: 5:01 PM To change this template use File | Settings |
 * File Templates.
 */
@DirtiesDatabase(DirtiesDatabase.AFTER_EACH_TEST_METHOD)
@DirtiesContext
public class PersonControllerTest
        extends BaseUnitTest {

    private Bop bop;

    private PersonController personController;

    @Autowired
    private UserService userService;

    @Autowired
    private BusinessObjectBuilder businessObjectBuilder;

    MockHttpServletResponse resp = new MockHttpServletResponse();

    ByteArrayOutputStream sink = new ByteArrayOutputStream();

    final MockHttpServletRequest mockReq = MockHttpServletRequestFactory
            .newMockRequest("POST", "/person", "test.org", 8080);

    @Before
    public void setUp() {
        RequestUtil requestUtil = new RequestUtil();

        personController =
                new PersonController(userService,
                                     businessObjectBuilder,
                                     requestUtil);

        bop = new Bop();
    }

    /**
     * Tests adding a person using the controller. Ensures that the correct
     * status code is returned and that the serialization returned matches the
     * person created. This is also a test case for the creation of person with
     * preset id.
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test
    public void testAddPerson() throws BizInternalException,
            BizPolicyException, IOException, InvalidXmlException {

        bop.addPerson(newUser);
        businessObjectBuilder.buildBusinessObjectPackage(bop, sink);

        personController.handlePersonPostRequest(APPLICATION_XML,
                                                 sink.toByteArray(),
                                                 mockReq,
                                                 resp);
        //Test that response is not null
        assertNotNull(resp);
        //Test that create response code was returned
        assertEquals(200, resp.getStatus());

        ByteArrayInputStream bais =
                new ByteArrayInputStream(resp.getContentAsByteArray());

        int contentLenth = bais.available();
        Bop returnedBop =
                businessObjectBuilder.buildBusinessObjectPackage(bais);
        //Test that bop was successfully built from the response
        assertNotNull(returnedBop);

        Set<Person> respPersons = returnedBop.getPersons();
        //Test that a set of persons was returned in the response
        assertNotNull(respPersons);
        //Test that only one person was included in the returned set
        assertEquals(1, respPersons.size());

        Person respPerson = respPersons.iterator().next();
        //Have to set the ogriinal person id to the id set by the biz service.
        newUser.setId(respPerson.getId());
        //Test that the original user is the same as the person returned by the controller
        assertEquals(newUser, respPerson);
        //Test that the original user was created in the system
        assertEquals(newUser, userService.get(newUser.getId()));

        //Check for expected headers
        assertNotNull(resp.getHeader(LOCATION));
        assertEquals(resp.getHeader(LOCATION), respPerson.getId());
        assertNotNull(resp.getHeader(ETAG));
        assertNotNull(resp.getHeader(LAST_MODIFIED));
        assertNotNull(resp.getContentType());
        //Test that the actual received content-length is the same the declared content lenghth on response header
        assertEquals(contentLenth, resp.getContentLength());

        //*** note: this test is only expected to pass if the underlying policy doesn't dictate that the Person
        //object has to be altered before creation.
        //Test that a correct Etag was sent with the response
        assertEquals(ETagCalculator.calculate(Integer.toString(bop.hashCode())),
                     resp.getHeader(ETAG));
    }

    /**
     * Tests passing the controller a serialization containing more than one
     * person. A bad request 400 status is expected to be returned.
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test
    public void testBadRequestAddingMoreThanOnePerson()
            throws BizInternalException, BizPolicyException, IOException,
            InvalidXmlException {
        Person user2 = new Person();
        user2.setId("http:abcde");
        user2.setFirstNames("Person2");
        user2.setLastNames("Person2LastName");
        user2.setPrefix("Mr.");
        user2.setSuffix("II");
        user2.setMiddleNames("Middle");
        user2.setPreferredPubName("P. Person2LastName");
        user2.setBio("Some bio for the user.");
        user2.setWebsite("www.somewebsite.com");
        user2.setPassword("Person2Password");
        user2.setEmailAddress("Person2@EmailAddress.com");
        user2.setPhoneNumber("123456789");
        user2.setRegistrationStatus(RegistrationStatus.PENDING);
        bop.addPerson(newUser);
        bop.addPerson(user2);

        businessObjectBuilder.buildBusinessObjectPackage(bop, sink);

        personController.handlePersonPostRequest(APPLICATION_XML,
                                                 sink.toByteArray(),
                                                 mockReq,
                                                 resp);
        //Test that response is not null
        assertNotNull(resp);
        //Test that create response code was returned
        assertEquals(HttpStatus.SC_BAD_REQUEST, resp.getStatus());
        assertNull(userService.get(newUser.getId()));
        assertNull(userService.get(user2.getId()));
    }

    /**
     * Tests passing the controller a serialization containing more than one
     * person. A bad request 400 status is expected to be returned.
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test
    public void testBadRequestAddPersonWithNoLastName()
            throws BizInternalException, BizPolicyException, IOException,
            InvalidXmlException {
        Person user2 = new Person();
        user2.setId("http:abcde");
        user2.setFirstNames("Person2");
        user2.setLastNames("Person2LastName");
        user2.setPrefix("Mr.");
        user2.setSuffix("II");
        user2.setMiddleNames("Middle");
        user2.setPreferredPubName("P. Person2LastName");
        user2.setBio("Some bio for the user.");
        user2.setWebsite("www.somewebsite.com");
        user2.setPassword("Person2Password");
        user2.setEmailAddress("Person2@EmailAddress.com");
        user2.setPhoneNumber("123456789");
        user2.setJobTitle("Person2 Scientist");
        user2.setDepartment("Person2 Department");
        user2.setCity("Baltimore");
        user2.setState("Maryland");
        user2.setInstCompany("Person2 Institution/Company");
        user2.setInstCompanyWebsite("www.Person2InstitutionCompany.com");
        user2.setRegistrationStatus(RegistrationStatus.PENDING);
        user2.setExternalStorageLinked(false);
        user2.setDropboxAppKey("SomeKey");
        user2.setDropboxAppSecret("SomeSecret");
        bop.addPerson(newUser);
        bop.addPerson(user2);

        businessObjectBuilder.buildBusinessObjectPackage(bop, sink);

        personController.handlePersonPostRequest(APPLICATION_XML,
                                                 sink.toByteArray(),
                                                 mockReq,
                                                 resp);
        //Test that response is not null
        assertNotNull(resp);
        //Test that create response code was returned
        assertEquals(HttpStatus.SC_BAD_REQUEST, resp.getStatus());
        assertNull(userService.get(newUser.getId()));
        assertNull(userService.get(user2.getId()));
    }

    /**
     * Tests that a bad request (400) is returned when the user is specified
     * with a status that is not allowed by the user services policyConsult.
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test
    public void testNewUserWithInvalidRegistrationStatus()
            throws BizInternalException, BizPolicyException, IOException,
            InvalidXmlException {
        Person user2 = new Person();
        user2.setId("http:abcde");
        user2.setFirstNames("Person2");
        user2.setLastNames("Person2LastName");
        user2.setPrefix("Mr.");
        user2.setSuffix("II");
        user2.setMiddleNames("Middle");
        user2.setPreferredPubName("P. Person2LastName");
        user2.setBio("Some bio for the user.");
        user2.setWebsite("www.somewebsite.com");
        user2.setPassword("Person2Password");
        user2.setEmailAddress("Person2@EmailAddress.com");
        user2.setPhoneNumber("123456789");
        user2.setJobTitle("Person2 Scientist");
        user2.setDepartment("Person2 Department");
        user2.setCity("Baltimore");
        user2.setState("Maryland");
        user2.setInstCompany("Person2 Institution/Company");
        user2.setInstCompanyWebsite("www.Person2InstitutionCompany.com");
        user2.setExternalStorageLinked(false);
        user2.setDropboxAppKey("SomeKey");
        user2.setDropboxAppSecret("SomeSecret");

        //Set user's registration status to something that's not allowed
        List<RegistrationStatus> allowedRegistrations =
                userService.getPolicyConsultant()
                        .allowedRegistrationStatusOnCreate();
        RegistrationStatus notAllowedStatus = null;
        for (RegistrationStatus status : RegistrationStatus.values()) {
            if (!allowedRegistrations.contains(status)) {
                notAllowedStatus = status;
                break;
            }
        }

        user2.setRegistrationStatus(RegistrationStatus.BLACK_LISTED);

        bop.addPerson(user2);
        businessObjectBuilder.buildBusinessObjectPackage(bop, sink);

        personController.handlePersonPostRequest(APPLICATION_XML,
                                                 sink.toByteArray(),
                                                 mockReq,
                                                 resp);
        //Test that response is not null
        assertNotNull(resp);
        //Test that create response code was returned
        assertEquals(HttpStatus.SC_BAD_REQUEST, resp.getStatus());
        assertNull(userService.get(user2.getId()));
    }

    /**
     * Tests that when a user with empty registration status is supplied, the
     * created person in the system would have the default value for
     * registration status.
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test
    public void testAddNewPersonWithEmptyRegistrationStatuses()
            throws BizInternalException, BizPolicyException, IOException,
            InvalidXmlException {

        //Set incoming person's registration status to null
        newUser.setRegistrationStatus(null);
        bop.addPerson(newUser);
        businessObjectBuilder.buildBusinessObjectPackage(bop, sink);

        personController.handlePersonPostRequest(APPLICATION_XML,
                                                 sink.toByteArray(),
                                                 mockReq,
                                                 resp);
        //Test that response is not null
        assertNotNull(resp);
        //Test that create response code was returned
        assertEquals(200, resp.getStatus());

        ByteArrayInputStream bais =
                new ByteArrayInputStream(resp.getContentAsByteArray());

        int contentLenth = bais.available();
        Bop returnedBop =
                businessObjectBuilder.buildBusinessObjectPackage(bais);
        //Test that bop was successfully built from the response
        assertNotNull(returnedBop);

        Set<Person> respPersons = returnedBop.getPersons();
        //Test that a set of persons was returned in the response
        assertNotNull(respPersons);
        //Test that only one person was included in the returned set
        assertEquals(1, respPersons.size());

        Person respPerson = respPersons.iterator().next();
        //verify assumption
        assertNull(newUser.getRegistrationStatus());
        //Test that the created person has a defaut registration status
        assertEquals(userService.getPolicyConsultant()
                             .getDefaultRegistrationStatus(),
                     respPerson.getRegistrationStatus());

        //Have to set the original person's reg status to the created person's reg status to compare the rest of
        //the fields
        newUser.setRegistrationStatus(respPerson.getRegistrationStatus());
        //Have to set the ogriinal person id to the id set by the biz service.
        newUser.setId(respPerson.getId());
        //Test that the original user is the same as the person returned by the controller
        assertEquals(newUser, respPerson);
        //Test that the original user was created in the system
        assertEquals(newUser, userService.get(newUser.getId()));

        //Check for expected headers
        assertNotNull(resp.getHeader(LOCATION));
        assertEquals(resp.getHeader(LOCATION), respPerson.getId());
        assertNotNull(resp.getHeader(ETAG));
        assertNotNull(resp.getHeader(LAST_MODIFIED));
        assertNotNull(resp.getContentType());
        //Test that the actual received content-length is the same the declared content lenghth on response header
        assertEquals(contentLenth, resp.getContentLength());
    }

    /**
     * Tests that when a user with an empty id is supplied, the system mints and
     * assign id to the created person.
     * 
     * @throws InvalidXmlException
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     */
    @Test
    public void testAddNewPersonWithEmptyId() throws InvalidXmlException,
            BizInternalException, BizPolicyException, IOException {
        //Set incoming person's id to null
        newUser.setId(null);
        bop.addPerson(newUser);
        businessObjectBuilder.buildBusinessObjectPackage(bop, sink);

        personController.handlePersonPostRequest(APPLICATION_XML,
                                                 sink.toByteArray(),
                                                 mockReq,
                                                 resp);
        //Test that response is not null
        assertNotNull(resp);
        //Test that create response code was returned
        assertEquals(200, resp.getStatus());

        ByteArrayInputStream bais =
                new ByteArrayInputStream(resp.getContentAsByteArray());

        int contentLenth = bais.available();
        Bop returnedBop =
                businessObjectBuilder.buildBusinessObjectPackage(bais);
        //Test that bop was successfully built from the response
        assertNotNull(returnedBop);

        Set<Person> respPersons = returnedBop.getPersons();
        //Test that a set of persons was returned in the response
        assertNotNull(respPersons);
        //Test that only one person was included in the returned set
        assertEquals(1, respPersons.size());

        Person respPerson = respPersons.iterator().next();
        assertNull(newUser.getId());
        assertNotNull(respPerson.getId());
        //Have to set the ogriinal person id to the id set by the biz service.
        newUser.setId(respPerson.getId());
        //Test that the original user is the same as the person returned by the controller
        assertEquals(newUser, respPerson);
        //Test that the original user was created in the system
        assertEquals(newUser, userService.get(newUser.getId()));

        //Check for expected headers
        assertNotNull(resp.getHeader(LOCATION));
        assertEquals(resp.getHeader(LOCATION), respPerson.getId());
        assertNotNull(resp.getHeader(ETAG));
        assertNotNull(resp.getHeader(LAST_MODIFIED));
        assertNotNull(resp.getContentType());
        //Test that the actual received content-length is the same the declared content lenghth on response header
        assertEquals(contentLenth, resp.getContentLength());
    }

    /**
     * Tests supplying person with empty role lists. Expected that person is
     * created successfully with a default role per policy
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test
    public void testAddNewPersonWithEmptyRolesList()
            throws BizInternalException, BizPolicyException, IOException,
            InvalidXmlException {
        //set person's role list to an empty list
        newUser.setRoles(new ArrayList<Role>());
        bop.addPerson(newUser);
        businessObjectBuilder.buildBusinessObjectPackage(bop, sink);

        personController.handlePersonPostRequest(APPLICATION_XML,
                                                 sink.toByteArray(),
                                                 mockReq,
                                                 resp);
        //Test that response is not null
        assertNotNull(resp);
        //Test that create response code was returned
        assertEquals(200, resp.getStatus());

        ByteArrayInputStream bais =
                new ByteArrayInputStream(resp.getContentAsByteArray());

        int contentLenth = bais.available();
        Bop returnedBop =
                businessObjectBuilder.buildBusinessObjectPackage(bais);
        //Test that bop was successfully built from the response
        assertNotNull(returnedBop);

        Set<Person> respPersons = returnedBop.getPersons();
        //Test that a set of persons was returned in the response
        assertNotNull(respPersons);
        //Test that only one person was included in the returned set
        assertEquals(1, respPersons.size());

        Person respPerson = respPersons.iterator().next();

        //verify assumption
        assertTrue(newUser.getRoles().isEmpty());
        assertNotNull(respPerson.getRoles());

        for (Role acceptedRole : userService
                .getPolicyConsultant()
                .getRolesForRegistrationStatus(respPerson.getRegistrationStatus())) {
            assertTrue(respPerson.getRoles().contains(acceptedRole));
        }

        //Assigned created person's role to supplied user's roles to compare the rest of the fields
        newUser.setRoles(respPerson.getRoles());

        //Have to set the ogriinal person id to the id set by the biz service.
        newUser.setId(respPerson.getId());
        //Test that the original user is the same as the person returned by the controller
        assertEquals(newUser, respPerson);
        //Test that the original user was created in the system
        assertEquals(newUser, userService.get(newUser.getId()));

        //Check for expected headers
        assertNotNull(resp.getHeader(LOCATION));
        assertEquals(resp.getHeader(LOCATION), respPerson.getId());
        assertNotNull(resp.getHeader(ETAG));
        assertNotNull(resp.getHeader(LAST_MODIFIED));
        assertNotNull(resp.getContentType());
        //Test that the actual received content-length is the same the declared content lenghth on response header
        assertEquals(contentLenth, resp.getContentLength());

        //*** note: this test is only expected to pass if the underlying policy doesn't dictate that the Person
        //object has to be altered before creation.
        //Test that a correct Etag was sent with the response
        assertEquals(ETagCalculator.calculate(Integer.toString(bop.hashCode())),
                     resp.getHeader(ETAG));
    }

    /**
     * Tests supplying a person with non-acceptable roles (per policy). Expect:
     * the new person is created successfully, but with defaults role (per
     * policy)
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test
    public void testAddNewPersonWithWrongRoles() throws BizInternalException,
            BizPolicyException, IOException, InvalidXmlException {

        //Setting a pending user's roles to an approved user's roles
        List<Role> nonAcceptableRoles =
                userService
                        .getPolicyConsultant()
                        .getRolesForRegistrationStatus(RegistrationStatus.APPROVED);

        newUser.setRoles(nonAcceptableRoles);
        bop.addPerson(newUser);
        businessObjectBuilder.buildBusinessObjectPackage(bop, sink);

        personController.handlePersonPostRequest(APPLICATION_XML,
                                                 sink.toByteArray(),
                                                 mockReq,
                                                 resp);
        //Test that response is not null
        assertNotNull(resp);
        //Test that create response code was returned
        assertEquals(200, resp.getStatus());
        ByteArrayInputStream bais =
                new ByteArrayInputStream(resp.getContentAsByteArray());

        int contentLenth = bais.available();
        Bop returnedBop =
                businessObjectBuilder.buildBusinessObjectPackage(bais);
        //Test that bop was successfully built from the response
        assertNotNull(returnedBop);

        Set<Person> respPersons = returnedBop.getPersons();
        //Test that a set of persons was returned in the response
        assertNotNull(respPersons);
        //Test that only one person was included in the returned set
        assertEquals(1, respPersons.size());

        Person respPerson = respPersons.iterator().next();

        assertNotNull(respPerson.getRoles());

        //Make sure the created person's roles are acceptable per policy.
        for (Role acceptedRole : userService
                .getPolicyConsultant()
                .getRolesForRegistrationStatus(respPerson.getRegistrationStatus())) {
            assertTrue(respPerson.getRoles().contains(acceptedRole));
        }
        //Test that the roles were changed during the creation of the person
        assertFalse(newUser.getRoles().equals(respPerson.getRoles()));

        //Assigned created person's role to supplied user's roles to compare the rest of the fields
        newUser.setRoles(respPerson.getRoles());

        //Have to set the ogriinal person id to the id set by the biz service.
        newUser.setId(respPerson.getId());
        //Test that the original user is the same as the person returned by the controller
        assertEquals(newUser, respPerson);
        //Test that the original user was created in the system
        assertEquals(newUser, userService.get(newUser.getId()));

        //Check for expected headers
        assertNotNull(resp.getHeader(LOCATION));
        assertEquals(resp.getHeader(LOCATION), respPerson.getId());
        assertNotNull(resp.getHeader(ETAG));
        assertNotNull(resp.getHeader(LAST_MODIFIED));
        assertNotNull(resp.getContentType());
        //Test that the actual received content-length is the same the declared content lenghth on response header
        assertEquals(contentLenth, resp.getContentLength());

        //assertNotNull(resp.getContentLength());

        //*** note: this test is only expected to pass if the underlying policy doesn't dictate that the Person
        //object has to be altered before creation.
        //Test that a correct Etag was sent with the response
        assertEquals(ETagCalculator.calculate(Integer.toString(bop.hashCode())),
                     resp.getHeader(ETAG));
    }

    /**
     * Test that we cannot add the same person twice. Expect a
     * DuplicateKeyException
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test(expected = DuplicateKeyException.class)
    public void testAddSameUserTwice() throws BizInternalException,
            BizPolicyException, IOException, InvalidXmlException {
        bop.addPerson(newUser);
        businessObjectBuilder.buildBusinessObjectPackage(bop, sink);

        personController.handlePersonPostRequest(APPLICATION_XML,
                                                 sink.toByteArray(),
                                                 mockReq,
                                                 resp);
        //Test that response is not null
        assertNotNull(resp);
        //Test that create response code was returned
        assertEquals(200, resp.getStatus());

        //try it again
        personController.handlePersonPostRequest(APPLICATION_XML,
                                                 sink.toByteArray(),
                                                 mockReq,
                                                 resp);
    }

}

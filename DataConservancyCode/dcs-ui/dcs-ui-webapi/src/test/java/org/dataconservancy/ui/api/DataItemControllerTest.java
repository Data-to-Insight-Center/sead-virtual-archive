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

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.model.builder.xstream.CollectionConverter;
import org.dataconservancy.ui.model.builder.xstream.DataItemConverter;
import org.dataconservancy.ui.model.builder.xstream.ProjectConverter;
import org.dataconservancy.ui.model.builder.xstream.XstreamBusinessObjectBuilder;
import org.dataconservancy.ui.model.builder.xstream.XstreamBusinessObjectFactory;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.DataItemBizService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.test.support.model.BaseModelTest;
import org.dataconservancy.ui.util.ETagCalculator;
import org.joda.time.DateTime;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ACCEPT;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ACCEPT_OCTET_STREAM;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.APPLICATION_XML;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_LENGTH;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_TYPE;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ETAG;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.IF_MATCH;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.IF_NONE_MATCH;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.LAST_MODIFIED;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Status.DEPOSITED;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.DATASET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests insuring the correct behavior of the Data Item Controller under expected and exceptional conditions.
 */
public class DataItemControllerTest extends BaseUnitTest {

    /**
     * The Request URI used for most if not all of the tests
     */
    private static final String REQUEST_STRING = "/item/1234";

    /**
     * The class under test.
     */
    private DataItemController underTest;

    /**
     * The Archive Deposit Info for the DataItem
     */
    private ArchiveDepositInfo dataSetAdi = new ArchiveDepositInfo();

    /**
     * The Deposit Date Time, one day in the past.
     */
    private DateTime depositDateTime = DateTime.now().minusDays(1);

    /**
     * Instantiates and configures the DataItemController under test.  The collaborators for the DataItemController
     * are mocked to succeed for most tests.  Individual tests can override the mocked behavior.
     * @throws BizPolicyException 
     * @throws ArchiveServiceException 
     */
    @Before
    public void setUpDataItemControllerUnderTest() throws ArchiveServiceException, BizPolicyException {

        UserService userService = mock(UserService.class);

        RequestUtil requestUtil = mock(RequestUtil.class);
        when(requestUtil.buildRequestUrl(any(HttpServletRequest.class))).thenReturn(dataItemOne.getId());

        AuthorizationService authzService = mock(AuthorizationService.class);
        when(authzService.canRetrieveDataSet(any(Person.class), any(DataItem.class))).thenReturn(true);

        BusinessObjectBuilder bob = mock(BusinessObjectBuilder.class);

        ArchiveService archiveService = mock(ArchiveService.class);
        when(archiveService.listDepositInfo(dataItemOne.getId(), DEPOSITED)).thenReturn(Arrays.asList(dataSetAdi));

        DataItemBizService dataItemBizService = mock(DataItemBizService.class);
        when(dataItemBizService.getDataItem(dataItemOne.getId(), admin)).thenReturn(dataItemOne);
        when(dataItemBizService.getDataItem(dataItemOne.getId(), null)).thenReturn(dataItemOne);

        underTest = newDataItemController(userService, requestUtil, authzService, bob, archiveService, dataItemBizService);
        underTest = spy(underTest);

        when(underTest.getAuthenticatedUser()).thenReturn(admin);
    }

    /**
     * Sets up and configures the Archive Deposit Info for the DataItem
     */
    @Before
    public void setUpAdi() {
        dataSetAdi.setArchiveId("1234");
        dataSetAdi.setDepositDateTime(depositDateTime);
        dataSetAdi.setDepositStatus(DEPOSITED);
        dataSetAdi.setObjectId(dataItemOne.getId());
        dataSetAdi.setObjectType(DATASET);
    }

    /**
     * Instantiates a fresh instance of Data Item Controller with the supplied collaborators.
     *
     * @param userService the User Service
     * @param requestUtil the Request Utility class
     * @param authzService the Authorization Service
     * @param bob the Business Object Builder
     * @param archiveService the ArchiveService
     * @return a new instance of Data Item Controller
     */
    private DataItemController newDataItemController(UserService userService, RequestUtil requestUtil,
                                                     AuthorizationService authzService, BusinessObjectBuilder bob,
                                                     ArchiveService archiveService, DataItemBizService dataItemBizService) {
        return new DataItemController(userService, authzService, archiveService, bob, requestUtil, dataItemBizService);
    }

    /**
     * Insures that an empty value for an accept header is considered un-acceptable.
     *
     * @throws Exception
     */
    @Test
    public void testEmptyAcceptHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String acceptHeaderValue = "";
        req.addHeader(ACCEPT, acceptHeaderValue);
        underTest.handleDataItemGetRequest(acceptHeaderValue, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_NOT_ACCEPTABLE, res.getStatus());
    }

    /**
     * Insures that a non-existent accept header is considered acceptable.
     *
     * @throws Exception
     */
    @Test
    public void testNonExistentAcceptHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        underTest.handleDataItemGetRequest(null, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that a non-sensical value for an accept header is considered un-acceptable.
     *
     * @throws Exception
     */
    @Test
    public void testNonSensicalAcceptHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String acceptHeaderValue = "this is a value that is completely non-sensical for an Accept header";
        req.addHeader(ACCEPT, acceptHeaderValue);
        underTest.handleDataItemGetRequest(acceptHeaderValue, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_NOT_ACCEPTABLE, res.getStatus());
    }

    /**
     * Insures that "application/*" is an acceptable value for an accept header.
     *
     * @throws Exception
     */
    @Test
    public void testApplicationAcceptWildcardHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String acceptHeaderValue = "application/*";
        req.addHeader(ACCEPT, acceptHeaderValue);
        underTest.handleDataItemGetRequest(acceptHeaderValue, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());

    }

    /**
     * Insures that "application/xml" is an acceptable value for an accept header.
     *
     * @throws Exception
     */
    @Test
    public void testApplicationXmlAcceptHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String acceptHeaderValue = APPLICATION_XML;
        req.addHeader(ACCEPT, acceptHeaderValue);
        underTest.handleDataItemGetRequest(acceptHeaderValue, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that "application/octet-stream" is an acceptable value for an accept header.
     *
     * @throws Exception
     */
    @Test
    public void testApplicationOctetStreamAcceptHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String acceptHeaderValue = ACCEPT_OCTET_STREAM;
        req.addHeader(ACCEPT, acceptHeaderValue);
        underTest.handleDataItemGetRequest(acceptHeaderValue, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that having the desired (and acceptable) content type somewhere in the middle of the
     * header is still an allowed value.
     *
     * @throws Exception
     */
    @Test
    public void testAcceptHeaderInMiddle() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String acceptHeaderValue = "application/json;q=0.5, " + ACCEPT_OCTET_STREAM;
        req.addHeader(ACCEPT, acceptHeaderValue);
        underTest.handleDataItemGetRequest(acceptHeaderValue, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that having the desired (and acceptable) content type with a q-value is an allowed value.
     *
     * @throws Exception
     */
    @Test
    public void testAcceptHeaderInMiddleWithQvalue() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String acceptHeaderValue = "application/json;q=0.5, " + ACCEPT_OCTET_STREAM + ";q=0.8";
        req.addHeader(ACCEPT, acceptHeaderValue);
        underTest.handleDataItemGetRequest(acceptHeaderValue, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that "*&#47;*" is an acceptable value for an accept header.
     *
     * @throws Exception
     */
    @Test
    public void testWildCardAcceptHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String acceptHeaderValue = "*/*";
        req.addHeader(ACCEPT, acceptHeaderValue);
        underTest.handleDataItemGetRequest(acceptHeaderValue, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that unauthenticated requests result in a 200.
     *
     * Note that as of the writing of this test, there isn't any concept of embargoing data items or collections.  So
     * in the future, more appropriate tests should be written:
     * <ul>
     *     <li>Unauthenticated requests for non-embargoed data items (this is what this test currently does, as
     *         the code currently stands)</li>
     *     <li>Authenticated requests for embargoed data items</li>
     *     <li>Unauthenticated requests for embargoed data items</li>
     *     <li>Authenticated requests for non-embargoed data items</li>
     * </ul>
     *
     *
     * @throws Exception
     */
    @Test
    public void testUnauthenticatedRequest() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(underTest.getAuthenticatedUser()).thenReturn(null);
        
        assertNull("Did not expect to have an authenticated user: expected 'null', was " + underTest
                .getAuthenticatedUser(), underTest.getAuthenticatedUser());

        underTest.handleDataItemGetRequest(null, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that unauthorized requests result in a 403.
     *
     * @throws Exception
     */
    @Test
    public void testUnauthorizedRequest() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        AuthorizationService authZService = underTest.getAuthzService();
        when(authZService.canRetrieveDataSet(any(Person.class), any(DataItem.class))).thenReturn(false);

        underTest.handleDataItemGetRequest(null, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, res.getStatus());
    }

    /**
     * Insures that if the request util class returns a null dataset identifier, a 500 is returned.
     *
     * @throws Exception
     */
    @Test
    public void testRequestWithNullDataSetId() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        RequestUtil requestUtil = underTest.getRequestUtil();
        when(requestUtil.buildRequestUrl(any(HttpServletRequest.class))).thenReturn(null);

        underTest.handleDataItemGetRequest(null, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, res.getStatus());
    }


    /**
     * Insures that if the request util class returns a empty dataset identifier, a 500 is returned.
     *
     * @throws Exception
     */
    @Test
    public void testRequestWithEmptyDataSetId() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        RequestUtil requestUtil = underTest.getRequestUtil();
        when(requestUtil.buildRequestUrl(any(HttpServletRequest.class))).thenReturn("  ");

        underTest.handleDataItemGetRequest(null, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, res.getStatus());
    }


    /**
     * Insures that if the request util class returns a zero-length dataset identifier, a 500 is returned.
     *
     * @throws Exception
     */
    @Test
    public void testRequestWithZeroLengthDataSetId() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        RequestUtil requestUtil = underTest.getRequestUtil();
        when(requestUtil.buildRequestUrl(any(HttpServletRequest.class))).thenReturn("  ");

        underTest.handleDataItemGetRequest(null, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, res.getStatus());
    }

    /**
     * Insures that if a DataItem cannot be resolved then a 404 is returned.
     * 
     * @throws Exception
     */
    @Test
    public void testRequestWithUnresolvableDataSetId() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(underTest.getDataItem(dataItemOne.getId())).thenReturn(null);

        underTest.handleDataItemGetRequest(null, null, null, null, req, res);

        assertEquals(HttpServletResponse.SC_NOT_FOUND, res.getStatus());
    }

    /**
     * Insures that if the value of the If-Match header equals the current version of a DataItem, the DataItem
     * representation will be retrieved; that is, a 200 is returned.
     *
     * @throws Exception
     */
    @Test
    public void testIfMatchOk() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String etag = ETagCalculator.calculate(String.valueOf(dataItemOne.hashCode()));
        req.addHeader(IF_MATCH, etag);

        underTest.handleDataItemGetRequest(null, etag, null, null, req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that if the value of the If-None-Match header equals the current version of a DataItem, the DataItem
     * representation will not be retrieved; that is, a 412 is returned.
     *
     * @throws Exception
     */
    @Test
    public void testIfNoneMatchNotOk() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String etag = ETagCalculator.calculate(String.valueOf(dataItemOne.hashCode()));
        req.addHeader(IF_NONE_MATCH, etag);

        underTest.handleDataItemGetRequest(null, null, etag, null, req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_PRECONDITION_FAILED, res.getStatus());
    }

    /**
     * Insures that ETags are not calculated using {@link System#identityHashCode(Object)}
     *
     * @throws Exception
     */
    @Test
    public void testEtagCalculationDoesNotUseIdentityHashcode() throws Exception {
        final String expectedEtag = ETagCalculator.calculate(String.valueOf(dataItemOne.hashCode()));

        assertFalse("The hashcode method of DataItem should not be based on object identity",
                expectedEtag.equals(String.valueOf(System.identityHashCode(dataItemOne))));
        assertEquals(expectedEtag, underTest.calculateEtag(dataItemOne));
    }

    /**
     * Insures that if the value of the If-Match header does not equal the current version of a DataItem, a 412 is
     * returned.
     *
     * @throws Exception
     */
    @Test
    public void testIfMatchNotOk() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String etag = "non-sensical etag";
        assertFalse(etag.equals(String.valueOf(dataItemOne.hashCode())));
        req.addHeader(IF_MATCH, etag);

        underTest.handleDataItemGetRequest(null, etag, null, null, req, res);

        assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, res.getStatus());
    }

    /**
     * Insures that if the value of the If-None-Match header does not equal the current version of a DataItem, and
     * the request resolved to a DataItem, then a 200 is returned.
     *
     * @throws Exception
     */
    @Test
    public void testIfNoneMatchOk() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String etag = "non-sensical etag";
        assertFalse(etag.equals(String.valueOf(dataItemOne.hashCode())));
        req.addHeader(IF_NONE_MATCH, etag);

        underTest.handleDataItemGetRequest(null, null, etag, null, req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that a wildcard value ('*') for the If-Match header is successful.
     *
     * @throws Exception
     */
    @Test
    public void testIfMatchWildcard() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String etag = "*";
        req.addHeader(IF_MATCH, etag);

        underTest.handleDataItemGetRequest(null, etag, null, null, req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that a wildcard value ('*') for the If-None-Match header is successful when
     * the DataItem exists.  A 304 not-modified should be returned.
     *
     * @throws Exception
     */
    @Test
    public void testIfNoneMatchWildcard() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String etag = "*";
        req.addHeader(IF_NONE_MATCH, etag);

        underTest.handleDataItemGetRequest(null, null, etag, null, req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_NOT_MODIFIED, res.getStatus());
    }

    /**
     * Insures that when requesting a non-existent resource with an If-Match header equal to '*',
     * a 412 is returned (not a 404)
     *
     * @throws Exception
     */
    @Test
    public void testIfMatchWildcardWithNonExistentResource() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String etag = "*";
        req.addHeader(IF_MATCH, etag);

        when(underTest.getDataItem(dataItemOne.getId())).thenReturn(null);

        underTest.handleDataItemGetRequest(null, etag, null, null, req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_PRECONDITION_FAILED, res.getStatus());
    }

    /**
     * Insures that when requesting a non-existent resource with an If-None-Match header equal to '*',
     * a 404 is returned
     *
     * @throws Exception
     */
    @Test
    public void testIfNoneMatchWildcardWithNonExistentResource() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final String etag = "*";
        req.addHeader(IF_NONE_MATCH, etag);

        when(underTest.getDataItem(dataItemOne.getId())).thenReturn(null);

        underTest.handleDataItemGetRequest(null, null, etag, null, req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_NOT_FOUND, res.getStatus());
    }

    /**
     * Insures that if multiple etags are in a If-Match header, and one of those etags matches a current
     * representation of a dataset, the request will succeed.
     *
     * @throws Exception
     */
    @Test
    public void testIfMatchMultipleEtagsOneIsValid() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        StringBuilder etag = new StringBuilder(ETagCalculator.calculate(String.valueOf(dataItemOne.hashCode())));
        etag.append(", ").append("a nonsensical etag");
        req.addHeader(IF_MATCH, etag);

        underTest.handleDataItemGetRequest(null, etag.toString(), null, null, req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_OK, res.getStatus());

        // repeat with etags in a different order
        etag = new StringBuilder("non sensical etag");
        etag.append(", ");
        etag.append(ETagCalculator.calculate(String.valueOf(dataItemOne.hashCode())));

        underTest.handleDataItemGetRequest(null, etag.toString(), null, null, req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that if multiple etags are in a If-None-Match header, and one of those etags matches a current
     * representation of a dataset, the request will not continue (return a 412 precodition failed)
     *
     * @throws Exception
     */
    @Test
    public void testIfNoneMatchMultipleEtagsOneIsValid() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        StringBuilder etag = new StringBuilder(ETagCalculator.calculate(String.valueOf(dataItemOne.hashCode())));
        etag.append(", ").append("a nonsensical etag");
        req.addHeader(IF_NONE_MATCH, etag);

        underTest.handleDataItemGetRequest(null, null, etag.toString(), null, req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_PRECONDITION_FAILED, res.getStatus());

        // repeat with etags in a different order

        req = new MockHttpServletRequest("GET", REQUEST_STRING);
        res = new MockHttpServletResponse();

        etag = new StringBuilder("non sensical etag");
        etag.append(", ");
        etag.append(ETagCalculator.calculate(String.valueOf(dataItemOne.hashCode())));
        req.addHeader(IF_NONE_MATCH, etag);

        underTest.handleDataItemGetRequest(null, null, etag.toString(), null, req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_PRECONDITION_FAILED, res.getStatus());
    }

    /**
     * Insure that requests with a If-Modified-Since header for an existing object with a last modification date
     * less than the If-Modified-Since header results in a 304.
     *
     * @throws Exception
     */
    @Test
    public void testIfModifiedSinceOk() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final DateTime ifModifiedSince = DateTime.now();
        req.addHeader(LAST_MODIFIED, DateUtility.toRfc822(ifModifiedSince));

        underTest.handleDataItemGetRequest(null, null, null, ifModifiedSince.toDate(), req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_NOT_MODIFIED, res.getStatus());
    }

    /**
     * Insure that requests with a If-Modified-Since header for an existing object with a last modification date
     * greater than the If-Modified-Since header results in a 200.
     *
     * @throws Exception
     */
    @Test
    public void testIfModifiedSinceNotOk() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final DateTime ifModifiedSince = DateTime.now().minusDays(2);
        req.addHeader(LAST_MODIFIED, DateUtility.toRfc822(ifModifiedSince));

        underTest.handleDataItemGetRequest(null, null, null, ifModifiedSince.toDate(), req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that requests with a future date for If-Modified-Since result in a 200.
     *
     * @throws Exception
     */
    @Test
    public void testIfModifiedSinceFutureDate() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        final DateTime ifModifiedSince = DateTime.now().plusDays(2);
        req.addHeader(LAST_MODIFIED, DateUtility.toRfc822(ifModifiedSince));

        underTest.handleDataItemGetRequest(null, null, null, ifModifiedSince.toDate(), req, res);

        assertEquals(res.getErrorMessage(), HttpServletResponse.SC_OK, res.getStatus());
    }

    /**
     * Insures that a successful request has all of the headers specified by the API, with correct values.
     *
     * @throws Exception
     */
    @Test
    public void testVerifyResponseHeaders() throws Exception {
        final String expectedLastModified = DateUtility.toRfc822(dataSetAdi.getDepositDateTime());
        final String expectedEtag = ETagCalculator.calculate(String.valueOf(dataItemOne.hashCode()));
        final String expectedContentType = APPLICATION_XML;
        final int lowContentLength = 570;
        final int highContentLength = 800;

        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();

        XstreamBusinessObjectFactory f = new XstreamBusinessObjectFactory();
        underTest.setBob(new XstreamBusinessObjectBuilder(f.createInstance()));

        underTest.handleDataItemGetRequest(null, null, null, null, req, res);

        assertEquals(expectedLastModified, res.getHeader(LAST_MODIFIED));
        assertEquals(expectedEtag, res.getHeader(ETAG));
        assertEquals(expectedContentType, res.getHeader(CONTENT_TYPE));
        
        int contentLength = Integer.parseInt((String)res.getHeader(CONTENT_LENGTH));
        assertTrue("Content Length out of bounds: " + contentLength, contentLength > lowContentLength && contentLength < highContentLength);
    }
}

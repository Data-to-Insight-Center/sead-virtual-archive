
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
package org.dataconservancy.ui.api.support;

import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.model.*;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Class;
import java.lang.String;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


/**
 * Tests for the utility class used to build the HTTP response header.
 */
public class ResponseHeaderUtilTest {

    /**
     * The class under test
     */
    private ResponseHeaderUtil classUnderTest = new ResponseHeaderUtil();

    /**
     * Mock of collaborating RequestUtil class
     */
    private RequestUtil mockRequestUtil = mock(RequestUtil.class);

    /**
     * Mock of collaborating Collection class
     */
    private Collection mockCollection = mock(Collection.class);

    /**
     * Ensure the request fails when If-Match header is empty.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfMatchReturnsFalseWhenEmptyIfMatch() throws IOException {
        boolean actualIsMatched = this.classUnderTest.handleIfMatch(null, null, null, "   ", mockCollection, null, null, null);
        assertFalse(actualIsMatched);
    }

    /**
     * Ensure the request succeeds when the object is null.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfMatchReturnsTrueWhenObjectIsNull() throws IOException {
        boolean actualIsMatched = this.classUnderTest.handleIfMatch(null, null, null, "If-Match-String", null, null, null, null);
        assertTrue(actualIsMatched);
    }

    /**
     * Ensure the request succeeds and sends a 412 error when the object is null
     * and the If-Match header is a wildcard.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfMatchReturnsTrueAndSends412ErrorWhenObjectIsNullAndIfMatchIsWildcard() throws IOException {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequestUtil.buildRequestUrl(mockRequest)).thenReturn("http://fake.url/If-Match-String");

        boolean actualIsMatched = this.classUnderTest.handleIfMatch(mockRequest, mockResponse, mockRequestUtil, "*", null, null, null, "Collection");
        verify(mockResponse).sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Unable to resolve Collection 'http://fake.url/If-Match-String'");
        assertTrue(actualIsMatched);
    }

    /**
     * Ensure the request succeeds but sends a 412 error when the object's eTag
     * does not match the If-Match header.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfMatchReturnsTrueAndSends412ErrorForNonMatchingEtag() throws IOException {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        boolean actualIsMatched = this.classUnderTest.handleIfMatch(null, mockResponse, null, "If-Match-String", mockCollection, "Object eTag", "Object ID", "Collection");
        verify(mockResponse).sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "If-Match header 'If-Match-String' did not match the requested Collection representation: 'Object ID' ('Object eTag')");
        assertTrue(actualIsMatched);
    }

    /**
     * Ensure the request fails when the object's eTag matches the If-Match
     * header.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfMatchReturnsFalseForMatchingEtag() throws IOException {
        boolean actualIsMatched = this.classUnderTest.handleIfMatch(null, null, null, "If-Match-String", mockCollection, "If-Match-String", null, null);
        assertFalse(actualIsMatched);
    }

    /**
     * Ensure the request fails when the last modified date and time is null.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfModifiedSinceReturnsFalseWhenLastModifiedIsNull() throws IOException {
        Date mockDate = mock(Date.class);

        boolean actualIsModifiedSince = this.classUnderTest.handleIfModifiedSince(null, null, mockDate, null);
        assertFalse(actualIsModifiedSince);
    }

    /**
     * Ensure the request fails when the modified since date and time is null.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfModifiedSinceReturnsFalseWhenIsModifiedSinceIsNull() throws IOException {
        boolean actualIsModifiedSince = this.classUnderTest.handleIfModifiedSince(null, null, null, DateTime.now());
        assertFalse(actualIsModifiedSince);
    }

    /**
     * Ensure the request fails when the modified since date and time is after
     * the current date and time.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfModifiedSinceReturnsFalseWhenIsModifiedSinceIsAfterNow() throws IOException {
        boolean actualIsModifiedSince = this.classUnderTest.handleIfModifiedSince(null, null, null, DateTime.now().plusHours(1));
        assertFalse(actualIsModifiedSince);
    }

    /**
     * Ensure the request fails when the last modified date and time is after
     * the modified since date and time.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfModifiedSinceReturnsFalseWhenLastModifiedIsAfterIsModified() throws IOException {
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.add(Calendar.HOUR, 1);

        boolean actualIsModifiedSince = this.classUnderTest.handleIfModifiedSince(null, null, nowCalendar.getTime(), DateTime.now());
        assertFalse(actualIsModifiedSince);
    }

    /**
     * Ensure the request succeeds and sends a 304 error when the modified
     * since date and time is between right now and the last modified date and
     * time.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfModifiedSinceReturnsTrueAndSends304WhenIfModifiedSinceIsBetweenLastModifiedAndNow() throws IOException {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.roll(Calendar.YEAR, -1);
        Date ifModifiedSince = nowCalendar.getTime();
        DateTime lastModified = DateTime.now().minusYears(2);

        boolean actualIsModifiedSince = this.classUnderTest.handleIfModifiedSince(null, mockResponse, ifModifiedSince, lastModified);
        assertTrue(actualIsModifiedSince);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_MODIFIED);
    }

    /**
     * Ensure the request fails when the If-None-Match header is empty.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfNoneMatchReturnsFalseWhenIfNoneMatchIsEmpty() throws IOException {
        boolean actualIfNoneMatch = this.classUnderTest.handleIfNoneMatch(null, null, "    ", null, null, null, null, null);
        assertFalse(actualIfNoneMatch);
    }

    /**
     * Ensure the request fails when the object is null.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfNoneMatchReturnsFalseWhenObjectIsNull() throws IOException {
        boolean actualIfNoneMatch = this.classUnderTest.handleIfNoneMatch(null, null, "*", null, null, null, null, null);
        assertFalse(actualIfNoneMatch);
    }

    /**
     * Ensure the request succeeds and a 304 error is sent when the object isn't
     * null, the If-None-Match header is a wildcard, and the modified since date
     * and time is null.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfNoneMatchReturnsTrueAndSends304WhenIfNoneMatchIsWildcardAndModifiedSinceIsNull() throws IOException {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        String objectEtag = "Object eTag";
        String lastModified = DateUtility.toRfc822(DateTime.now());

        boolean actualIfNoneMatch = this.classUnderTest.handleIfNoneMatch(null, mockResponse, "*", mockCollection, objectEtag, null, DateTime.now(), null);
        assertTrue(actualIfNoneMatch);
        verify(mockResponse).addHeader(ResponseHeaderUtil.LAST_MODIFIED, lastModified);
        verify(mockResponse).addHeader(ResponseHeaderUtil.ETAG, objectEtag);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_MODIFIED);
    }

    /**
     * Ensure the request succeeds and a 412 error is sent when the object isn't
     * null and the If-None-Match header matches the object's eTag.
     *
     * @throws IOException
     */
    @Test
    public void testHandleIfNoneMatchReturnsTrueAndSends412ForMatchingEtag() throws IOException {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        String objectEtag = "Object eTag";
        String objectId = "Object ID";
        String ifNoneMatch = "Other eTag, Object eTag, Another eTag";

        boolean actualIfNoneMatch = this.classUnderTest.handleIfNoneMatch(null, mockResponse, ifNoneMatch, mockCollection, objectEtag, objectId, null, null);
        assertTrue(actualIfNoneMatch);
        verify(mockResponse).sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "If-None-Match header 'Other eTag, Object eTag, Another eTag' matched the requested Collection representation: 'Object ID' ('Object eTag')");
    }

    /**
     * Ensure the HTTP response header has the expected fields.`
     *
     * @throws IOException
     */
    @Test
    public void testSetResponseHeaderFieldsSetsExpectedFields() throws IOException {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream mockOutputStream = mock(ByteArrayOutputStream.class);
        String objectEtag = "Object eTag";
        DateTime currentDateTime = DateTime.now();

        when(mockOutputStream.size()).thenReturn(50);

        this.classUnderTest.setResponseHeaderFields(mockResponse, objectEtag, mockOutputStream, currentDateTime);
        verify(mockResponse).setHeader(ResponseHeaderUtil.ETAG, objectEtag);
        verify(mockResponse).setHeader(ResponseHeaderUtil.CONTENT_LENGTH, "50");
        verify(mockResponse).setHeader(ResponseHeaderUtil.CONTENT_TYPE, ResponseHeaderUtil.APPLICATION_XML);
        verify(mockResponse).setHeader(ResponseHeaderUtil.LAST_MODIFIED, DateUtility.toRfc822(currentDateTime));
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
    }

}

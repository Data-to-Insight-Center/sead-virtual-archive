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
package org.dataconservancy.dcs.lineage.http;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.lineage.api.LineageService;
import org.dataconservancy.dcs.lineage.http.support.RequestUtil;
import org.dataconservancy.dcs.lineage.http.support.TestRequestUtil;
import org.dataconservancy.dcs.lineage.impl.LineageEntryImpl;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.Calendar;

import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertEmptyResponse;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertEtagEquals;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertNotEmptyResponse;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertEtagNull;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.prepareHandleLatestLineageGetRequest;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.prepareHandleLineageGetRequest;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.prepareHandleOriginalLineageGetRequest;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.prepareHandleSearchLineageGetRequest;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.verifyHandleLatestLineageGetRequestMocks;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.verifyHandleLineageGetRequestMocks;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.verifyHandleOriginalLineageGetRequestMocks;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.verifyHandleSearchLineageGetRequestMocks;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the behavior of the LineageController with regard to generating (or not) an ETag header under certain
 * conditions.
 */
public class EtagLineageControllerTest {

    private MockHttpServletRequest mockRequest = new MockHttpServletRequest();


    /**
     * Asserts no ETag will be in the model for a non-existent lineage id.
     *
     * @throws Exception
     */
    @Test
    public void testEtagHeaderValueWithEmptyResponseForGetLineage() throws Exception {

        LineageController underTest = new LineageController(mock(LineageService.class), mock(IdService.class), mock(LookupQueryService.class));

        mockRequest.setRequestURI("/lineage/non-matching-lineage-id-part");

        Model m = underTest.handleLineageGetRequest(null, null, mockRequest);

        // Assert the query didn't match any lineage or entity
        assertEmptyResponse(m);

        // Since the Lineage wasn't found, the ETag value should be null (there's nothing to calculate an
        // Etag over).

        assertEtagNull(m);
    }

    /**
     * Asserts the correct ETag will be in the model for an existing lineage id.
     *
     * @throws Exception
     */
    @Test
    public void testEtagHeaderValueForGetLineage() throws Exception {
        final String entityId = "entityId";
        final String idPart = "lineageId";
        final String lineageId = idPart;
        final LineageEntryImpl entry = new LineageEntryImpl(entityId, lineageId, Calendar.getInstance().getTimeInMillis());

        TestRequestUtil.ContextHolder ctx = prepareHandleLineageGetRequest(entry);

        mockRequest.setRequestURI("/lineage/" + idPart);

        LineageController underTest = ctx.getUnderTest();

        Model m = underTest.handleLineageGetRequest(null, null, mockRequest);

        // Verify the mocks were called as expected.
        verifyHandleLineageGetRequestMocks(ctx, entry);

        // Assert the query generated a response
        assertNotEmptyResponse(m);

        // Since the Lineage was found, a valid ETag should be found
        assertEtagEquals(m, RequestUtil.calculateDigest(Arrays.asList(entry.getEntityId())));
    }

    /**
     * Asserts no ETag will be in the model for a request that doesn't specify a lineage id.
     *
     * @throws Exception
     */
    @Test
    public void testEtagHeaderValueWithEmptyResponseForEmptyGetRequest() throws Exception {

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        LineageController underTest = new LineageController(mock(LineageService.class), mock(IdService.class), mock(LookupQueryService.class));

        mockRequest.setRequestURI("/lineage/");

        Model m = underTest.handleLineageGetRequest(null, null, mockRequest);

        // Assert the response is empty
        assertEmptyResponse(m);

        // Since the Lineage wasn't found, the ETag value should be null (there's nothing to calculate an
        // Etag over).
        assertEtagNull(m);
    }

    /**
     * Asserts that no ETag will be in the model when a non-existent lineageId is specified for an "original" request.
     *
     * @throws Exception
     */
    @Test
    public void testEtagHeaderValueWithEmptyResponseForOriginalGetRequest() throws Exception {

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        LineageController underTest = new LineageController(mock(LineageService.class), mock(IdService.class), mock(LookupQueryService.class));

        mockRequest.setRequestURI("/lineage/original?id=fooLineageId");

        Model m = underTest.handleOriginalGetRequest(null, null, null);

        // Assert that the response is empty
        assertEmptyResponse(m);

        // Since the Lineage wasn't found, the ETag value should be null (there's nothing to calculate an
        // Etag over).
        assertEtagNull(m);
    }

    /**
     * Asserts that a proper ETag will be in the model when a existent lineageId is specified for an "original" request.
     *
     * @throws Exception
     */
    @Test
    public void testEtagHeaderValueForOriginalGetRequest() throws Exception {
        final LineageEntryImpl entry = new LineageEntryImpl("entityId", "lineageId", Calendar.getInstance().getTimeInMillis());

        TestRequestUtil.ContextHolder ctx = prepareHandleOriginalLineageGetRequest(entry);
        LineageController underTest = ctx.getUnderTest();

        Model m = underTest.handleOriginalGetRequest(null, null, entry.getLineageId());

        verifyHandleOriginalLineageGetRequestMocks(ctx, entry);

        // Assert the query generated a response
        assertNotEmptyResponse(m);

        // Since the Lineage was found, a valid ETag should be found
        assertEtagEquals(m, RequestUtil.calculateDigest(Arrays.asList(entry.getEntityId())));
    }

    /**
     * Asserts that no ETag will be in the model when a non-existent lineageId is specified for an "latest" request.
     *
     * @throws Exception
     */
    @Test
    public void testEtagHeaderValueWithEmptyResponseForLatestGetRequest() throws Exception {

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        LineageController underTest = new LineageController(mock(LineageService.class), mock(IdService.class), mock(LookupQueryService.class));

        mockRequest.setRequestURI("/lineage/latest?id=fooLineageId");

        Model m = underTest.handleOriginalGetRequest(null, null, null);

        // Assert an empty response
        assertEmptyResponse(m);

        // Since the Lineage wasn't found, the ETag value should be null (there's nothing to calculate an
        // Etag over).
        assertEtagNull(m);
    }


    /**
     * Asserts that the correct ETag will be in the model when a existing lineageId is specified for an "latest" request.
     *
     * @throws Exception
     */
    @Test
    public void testEtagHeaderValueForLatestGetRequest() throws Exception {
        final String lineageId = "lineageId";
        final String entityId = "entityId";

        final LineageEntryImpl entry = new LineageEntryImpl(entityId, lineageId, Calendar.getInstance().getTimeInMillis());

        // Prepare the mock objects on a LineageController
        TestRequestUtil.ContextHolder ctx = prepareHandleLatestLineageGetRequest(entry);

        // Execute the test
        LineageController underTest = ctx.getUnderTest();

        Model m = underTest.handleLatestGetRequest(null, null, lineageId, -1L);

        // Verify the mocks were called as expected.
        verifyHandleLatestLineageGetRequestMocks(ctx, entry);

        // Assert the query generated a response
        assertNotEmptyResponse(m);

        // Since the Lineage was found, a valid ETag should be found
        assertEtagEquals(m, RequestUtil.calculateDigest(Arrays.asList(entityId)));
    }

    /**
     * Asserts that a no ETag is present in the model for a "search" request that returns no entities.
     *
     * @throws Exception
     */
    @Test
    public void testEtagHeaderValueWithEmptyResponseForSearchRequest() throws Exception {

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        LineageController underTest = new LineageController(mock(LineageService.class), mock(IdService.class), mock(LookupQueryService.class));

        mockRequest.setRequestURI("/lineage/search?id=fooLineageId");

        Model m = underTest.handleOriginalGetRequest(null, null, null);

        // Assert an empty response
        assertEmptyResponse(m);

        // Since the Lineage wasn't found, the ETag value should be null (there's nothing to calculate an
        // Etag over).
        assertEtagNull(m);
    }

    /**
     * Asserts that the correct ETag is present in the model for a "search" request that returns entities.
     *
     * @throws Exception
     */
    @Test
    public void testEtagHeaderValueForSearchRequest() throws Exception {
        final LineageEntryImpl fromEntry = new LineageEntryImpl("fromEntityId", "lineageId", Calendar.getInstance().getTimeInMillis() - 10000);
        final LineageEntryImpl toEntry = new LineageEntryImpl("toEntityId", "lineageId", Calendar.getInstance().getTimeInMillis());

        TestRequestUtil.ContextHolder ctx = prepareHandleSearchLineageGetRequest(fromEntry, toEntry);

        LineageController underTest = ctx.getUnderTest();
        Model m = underTest.handleSearchGetRequest(null, null, "lineageId", "fromEntityId", "toEntityId");

        // Verify the mocks were called as expected.
        verifyHandleSearchLineageGetRequestMocks(ctx, fromEntry, toEntry);

        // Assert the query generated a response
        assertNotEmptyResponse(m);

        // Since the Lineage was found, a valid ETag should be found
        assertEtagEquals(m, RequestUtil.calculateDigest(Arrays.asList(fromEntry.getEntityId(), toEntry.getEntityId())));
    }


}

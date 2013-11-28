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
import org.dataconservancy.dcs.lineage.http.support.TestRequestUtil;
import org.dataconservancy.dcs.lineage.impl.LineageEntryImpl;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;

import java.util.Calendar;

import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertEmptyResponse;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertNotEmptyResponse;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertNotNullLineage;
import static org.dataconservancy.dcs.lineage.http.support.ModelAssert.assertNullLineage;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.prepareHandleLatestLineageGetRequest;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.prepareHandleLineageGetRequest;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.prepareHandleOriginalLineageGetRequest;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.prepareHandleSearchLineageGetRequest;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.verifyHandleLatestLineageGetRequestMocks;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.verifyHandleLineageGetRequestMocks;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.verifyHandleOriginalLineageGetRequestMocks;
import static org.dataconservancy.dcs.lineage.http.support.TestRequestUtil.verifyHandleSearchLineageGetRequestMocks;
import static org.mockito.Mockito.mock;

/**
 * Tests the behavior of the LineageController with regard to placing the proper value of Lineage in the model for
 * various conditions.
 */
public class LineageLineageControllerTest {

    private MockHttpServletRequest mockRequest = new MockHttpServletRequest();

    /**
     * Asserts no Lineage will be in the model for a non-existent lineage id.
     *
     * @throws Exception
     */
    @Test
    public void testHandleEmptyGetRequest() throws Exception {
        LineageController underTest = new LineageController(mock(LineageService.class), mock(IdService.class), mock(LookupQueryService.class));

        mockRequest.setRequestURI("/lineage/non-matching-lineage-id-part");

        Model m = underTest.handleLineageGetRequest(null, null, mockRequest);

        // A lineage should not be found.
        assertNullLineage(m);

        // The response should be empty if no lineage was found.
        assertEmptyResponse(m);
    }

    /**
     * Asserts a Lineage will be in the model for a successful "latest" lineage request.
     *
     * @throws Exception
     */
    @Test
    public void testHandleLatestGetRequest() throws Exception {
        final String lineageId = "lineageId";
        final String entityId = "entityId";

        final LineageEntryImpl entry = new LineageEntryImpl(entityId, lineageId, Calendar.getInstance().getTimeInMillis());

        // Prepare the mock objects on a LineageController
        TestRequestUtil.ContextHolder ctx = prepareHandleLatestLineageGetRequest(entry);

        LineageController underTest = ctx.getUnderTest();

        // Execute the test
        Model m = underTest.handleLatestGetRequest(null, null, lineageId, -1L);

        // Verify the mocks were called as expected.
        verifyHandleLatestLineageGetRequestMocks(ctx, entry);

        // Assert that a lineage is found
        assertNotNullLineage(m);

        // Since a lineage was found, the response should not be empty
        assertNotEmptyResponse(m);
    }

    /**
     * Asserts a Lineage will not be in the model for a "latest" lineage request where the lineage
     * doesn't exist.
     *
     * @throws Exception
     */
    @Test
    public void testHandleLatestGetRequestForNonExistentLineage() throws Exception {
        final String lineageId = "non-existent-lineageId";

        // Prepare the mock objects on a LineageController
        TestRequestUtil.ContextHolder ctx = prepareHandleLatestLineageGetRequest(null);

        LineageController underTest = ctx.getUnderTest();

        // Execute the test
        Model m = underTest.handleLatestGetRequest(null, null, lineageId, -1L);

        // Verify the mocks were called as expected.
        verifyHandleLatestLineageGetRequestMocks(ctx, null);

        // Assert that a lineage is not found
        assertNullLineage(m);

        // Since a lineage was not found, the response should be empty
        assertEmptyResponse(m);
    }

    /**
     * Asserts that a lineage will be in the model for a successful lineage request
     *
     * @throws Exception
     */
    @Test
    public void testHandleLineageGetRequest() throws Exception {
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

        // Assert that a Lineage was found
        assertNotNullLineage(m);

        // Since a lineage was found, the should have generated a response.
        assertNotEmptyResponse(m);
    }

    /**
     * Asserts that a lineage will not be in the model for a non-existent lineage request
     *
     * @throws Exception
     */
    @Test
    public void testHandleLineageGetRequestForNonExistentLineage() throws Exception {
        final String idPart = "non-existent-lineageId";

        TestRequestUtil.ContextHolder ctx = prepareHandleLineageGetRequest(null);

        mockRequest.setRequestURI("/lineage/" + idPart);

        LineageController underTest = ctx.getUnderTest();

        Model m = underTest.handleLineageGetRequest(null, null, mockRequest);

        // Verify the mocks were called as expected.
        verifyHandleLineageGetRequestMocks(ctx, null);

        // Assert that a Lineage was not found
        assertNullLineage(m);

        // Since a lineage was not found, there should be no response.
        assertEmptyResponse(m);
    }

    /**
     * Asserts that a lineage will be found in the model for an existing, original lineage request.
     * 
     * @throws Exception
     */
    @Test
    public void testHandleOriginalGetRequest() throws Exception {
        final LineageEntryImpl entry = new LineageEntryImpl("entityId", "lineageId", Calendar.getInstance().getTimeInMillis());

        TestRequestUtil.ContextHolder ctx = prepareHandleOriginalLineageGetRequest(entry);
        LineageController underTest = ctx.getUnderTest();

        Model m = underTest.handleOriginalGetRequest(null, null, entry.getLineageId());

        verifyHandleOriginalLineageGetRequestMocks(ctx, entry);

        // Assert a lineage was found in the model
        assertNotNullLineage(m);

        // Assert the query generated a response
        assertNotEmptyResponse(m);
    }

    /**
     * Asserts that a lineage will not be found in the model for an non-existent original entity request.
     *
     * @throws Exception
     */
    @Test
    public void testHandleOriginalGetRequestWithNonExistentLineage() throws Exception {
        TestRequestUtil.ContextHolder ctx = prepareHandleOriginalLineageGetRequest(null);
        LineageController underTest = ctx.getUnderTest();

        Model m = underTest.handleOriginalGetRequest(null, null, null);

        verifyHandleOriginalLineageGetRequestMocks(ctx, null);

        // Assert a lineage was not found in the model
        assertNullLineage(m);

        // Assert that the response was empty
        assertEmptyResponse(m);
    }

    /**
     * Assert that a lineage will be found in the model for a valid search request
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchRequest() throws Exception {
        final LineageEntryImpl fromEntry = new LineageEntryImpl("fromEntityId", "lineageId", Calendar.getInstance().getTimeInMillis() - 10000);
        final LineageEntryImpl toEntry = new LineageEntryImpl("toEntityId", "lineageId", Calendar.getInstance().getTimeInMillis());

        TestRequestUtil.ContextHolder ctx = prepareHandleSearchLineageGetRequest(fromEntry, toEntry);

        LineageController underTest = ctx.getUnderTest();
        Model m = underTest.handleSearchGetRequest(null, null, "lineageId", fromEntry.getEntityId(), toEntry.getEntityId());

        // Verify the mocks were called as expected.
        verifyHandleSearchLineageGetRequestMocks(ctx, fromEntry, toEntry);

        // Assert that a Lineage was found
        assertNotNullLineage(m);

        // Assert the query generated a response
        assertNotEmptyResponse(m);
    }

    /**
     * Asserts that a search for a non existent lineage will result in a
     * no corresponding lineage in the model.
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchRequestWithNonExistentLineage() throws Exception {
        TestRequestUtil.ContextHolder ctx = prepareHandleSearchLineageGetRequest(null, null);

        LineageController underTest = ctx.getUnderTest();
        Model m = underTest.handleSearchGetRequest(null, null, "lineageId", null, null);

        // Verify the mocks were called as expected.
        verifyHandleSearchLineageGetRequestMocks(ctx, null, null);

        // Assert that a Lineage was not found
        assertNullLineage(m);

        // Assert the query generated an empty response
        assertEmptyResponse(m);
    }

    /**
     * Asserts that a search for an empty lineage will result in a
     * corresponding lineage in the model.
     *
     * @throws Exception
     */
    @Test
    @Ignore("TODO")
    public void testHandleSearchRequestWithEmptyLineage() throws Exception {
        // TODO
    }
}

/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.dataconservancy.ui.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.DataItemTransport;
import org.dataconservancy.ui.services.DataItemTransportService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_TYPE;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.APPLICATION_JSON;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the behavior of the AdiAjaxController
 */
// @DirtiesContext
public class AdiAjaxControllerTest extends BaseUnitTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataItemTransportService dataItemTransportService;
    
    private List<DataItemTransport> dataItemTransportList;

    private String dataItemIds;

    private AdiAjaxController adiAjaxController;

    @Before
    public void setup() throws ArchiveServiceException, BizPolicyException {
        dataItemTransportList = new ArrayList<DataItemTransport>();
        dataItemTransportList.add(new DataItemTransport(dataItemOne));
        dataItemTransportList.add(new DataItemTransport(dataItemTwo));

        dataItemIds = dataItemOne.getId() + "," + dataItemTwo.getId();

        adiAjaxController = new AdiAjaxController(dataItemTransportService);
    }
    
    @Test
    public void testGetDataItemTransportsForNullCollection() throws IOException{
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        adiAjaxController.getDataItemTransportsForCollection(null, mockResponse);

        assertEquals(200, mockResponse.getStatus());
        assertEquals(APPLICATION_JSON, mockResponse.getHeader(CONTENT_TYPE));
        assertTrue(mockResponse.getContentAsString().contains(AdiAjaxController.COLLECTION_NOT_FOUND));
    }
    
    @Test
    public void testGetDataItemTransportsForInvalidCollection() throws IOException, ArchiveServiceException, BizPolicyException {
        DataItemTransportService mockDataItemTransportService = mock(DataItemTransportService.class);
        when(mockDataItemTransportService.retrieveDataItemTransportList(anyString(), anyInt(), anyInt())).thenThrow(new ArchiveServiceException());
        AdiAjaxController adiAjaxControllerWithMock = new AdiAjaxController(mockDataItemTransportService);
        
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        adiAjaxControllerWithMock.getDataItemTransportsForCollection("invalid collection", mockResponse);

        assertEquals(200, mockResponse.getStatus());
        assertEquals(APPLICATION_JSON, mockResponse.getHeader(CONTENT_TYPE));
        assertTrue(mockResponse.getContentAsString().contains("Error retrieving data sets"));
    }

     @Test
    public void testGetDataItemTransportsForCollectionWithoutData() throws IOException {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        adiAjaxController.getDataItemTransportsForCollection(collectionNoData.getId(), mockResponse);

        assertEquals(200, mockResponse.getStatus());
        assertEquals(APPLICATION_JSON, mockResponse.getHeader(CONTENT_TYPE));
        assertTrue(mockResponse.getContentAsString().contains("No Data Items were found"));
    }
    

    @Test
    public void testGetDataItemTransportsForCollectionWithData() throws 
            IOException {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        adiAjaxController.getDataItemTransportsForCollection(collectionWithData.getId(), mockResponse);

        assertEquals(200, mockResponse.getStatus());
        assertEquals(APPLICATION_JSON, mockResponse.getHeader(CONTENT_TYPE));
        assertTrue(mockResponse.getContentAsString().contains("Total Number of Data Items in "));
        assertTrue(mockResponse.getContentAsString().contains("2"));
    }
    
    @Test
    public void testGetDataItemTransportsForNullDataItemIds() throws ArchiveServiceException, BizPolicyException, IOException {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        
        adiAjaxController.getDataItemTransportsForDataItems(null, mockResponse);
        
        assertEquals(200, mockResponse.getStatus());
        assertEquals(APPLICATION_JSON, mockResponse.getHeader(CONTENT_TYPE));
        assertTrue(mockResponse.getContentAsString().contains(AdiAjaxController.DATA_ITEMS_NOT_PROVIDED));
    }
    
    @Test
    public void testGetDataItemTransportsWithDataRetrievalError() throws ArchiveServiceException, BizPolicyException, IOException {
        DataItemTransportService mockDataItemTransportService = mock(DataItemTransportService.class);
        when(mockDataItemTransportService.retrieveDataItemTransport(anyString())).thenThrow(new ArchiveServiceException());
        AdiAjaxController adiAjaxControllerWithMock = new AdiAjaxController(mockDataItemTransportService);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        adiAjaxControllerWithMock.getDataItemTransportsForDataItems("any id", mockResponse);

        assertEquals(200, mockResponse.getStatus());
        assertEquals(APPLICATION_JSON, mockResponse.getHeader(CONTENT_TYPE));

        assertTrue(mockResponse.getContentAsString().contains("Error retrieving Data Item"));
    }
    
    @Test
    public void testGetDataItemTransportsForInvalidDataItemId() throws ArchiveServiceException, BizPolicyException, IOException {
        DataItemTransportService mockDataItemTransportService = mock(DataItemTransportService.class);
        when(mockDataItemTransportService.retrieveDataItemTransport(anyString())).thenReturn(null);
        AdiAjaxController adiAjaxControllerWithMock = new AdiAjaxController(mockDataItemTransportService);
        
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        adiAjaxControllerWithMock.getDataItemTransportsForDataItems("invalid data item id", mockResponse);

        assertEquals(200, mockResponse.getStatus());
        assertEquals(APPLICATION_JSON, mockResponse.getHeader(CONTENT_TYPE));
        
        String jsonResponse = mockResponse.getContentAsString();
        assertTrue(jsonResponse.contains(AdiAjaxController.DATA_ITEMS_NOT_UPDATED));
        assertTrue(jsonResponse.contains("invalid data item id"));
        assertTrue(jsonResponse.contains(AdiAjaxController.NO_DATA_ITEMS_UPDATED));
    }
    
    @Test
    public void testGetDataItemTransportsForValidIds() throws ArchiveServiceException, BizPolicyException, IOException {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        adiAjaxController.getDataItemTransportsForDataItems(dataItemIds, mockResponse);

        assertEquals(200, mockResponse.getStatus());
        assertEquals(APPLICATION_JSON, mockResponse.getHeader(CONTENT_TYPE));
        
        String jsonResponse = mockResponse.getContentAsString();
        assertTrue(jsonResponse.contains("Total Number of Data Items updated"));
        assertTrue(jsonResponse.contains("2"));
        assertTrue(jsonResponse.contains(dataItemOne.getId()));
        assertTrue(jsonResponse.contains(dataItemTwo.getId()));
    }
}

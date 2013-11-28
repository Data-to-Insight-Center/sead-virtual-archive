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

import java.io.FileInputStream;
import java.io.IOException;

import java.net.URLConnection;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

import org.dataconservancy.ui.services.MetadataFileBizService;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.BizPolicyException.Type;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.FileBizService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.UserService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;

import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_DISPOSITION;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ETAG;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.LAST_MODIFIED;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: HanhVu
 * Date: 6/7/12
 * Time: 5:40 PM
 * To change this template use File | Settings | File Templates.
 */

@DirtiesContext
public class FileControllerTest extends BaseUnitTest {

    private final static String FILES_ONLY_ZIP = "/SampleFilePackages/Windows/WindowsZipFile.zip";
    
    private FileController fileController;
    
    private FileController metadataFileController;

    @Autowired
    private UserService userService;

    @Autowired
    private ArchiveService archiveService;

    /**
     * The Request URI used for most if not all of the tests
     */
    private static final String REQUEST_STRING = "/file/1234";
    
    //modified-since date that is before the actual date (for test purpose)
    static DateTime beforeModifiedDate = DateTime.now().minusDays(2);

    //Actual modified date of the test file.
    static DateTime lastModifiedDate = DateTime.now();

    //modified-since date that is after the actual date (for test purpose)
    static DateTime afterModifiedDate = DateTime.now().plusDays(2);

    /**
     * Instantiates a fresh instance of Data Item Controller with the supplied collaborators.
     *
     * @param userService the User Service
     * @param fileBizService the FileBizService
     * @param requestUtil the Request Utility class
     * @param archiveService the ArchiveService
     * @param metadataFileBizService the MetaDataFileBizService                      
     * @return a new instance of Data Item Controller
     */
    private FileController newFileController(UserService userService, RequestUtil requestUtil,                                                     
                                                     ArchiveService archiveService, FileBizService fileBizService, MetadataFileBizService metadataFileBizService) {
        return new FileController(userService, fileBizService, requestUtil, archiveService, metadataFileBizService);
    }
    
    /**
     * Instantiates and configures the DataItemController under test.  The collaborators for the DataItemController
     * are mocked to succeed for most tests.  Individual tests can override the mocked behavior.
     * @throws BizPolicyException 
     * @throws ArchiveServiceException 
     * @throws RelationshipConstraintException 
     */
    @Before
    public void setUpFileControllerUnderTest() throws ArchiveServiceException, BizPolicyException, RelationshipConstraintException {

        UserService userService = mock(UserService.class);

        RequestUtil requestUtil = mock(RequestUtil.class);
        when(requestUtil.buildRequestUrl(any(HttpServletRequest.class))).thenReturn(dataFileOne.getId());

        RequestUtil metadataRequestUtil = mock(RequestUtil.class);
        when(metadataRequestUtil.buildRequestUrl(any(HttpServletRequest.class))).thenReturn(metadataFileOne.getId());
        
        AuthorizationService authzService = mock(AuthorizationService.class);
        when(authzService.canRetrieveDataSet(any(Person.class), any(DataItem.class))).thenReturn(true);

        FileBizService fileBizService = mock(FileBizService.class);
        when(fileBizService.getFile(dataFileOne.getId(), admin)).thenReturn(dataFileOne);
        when(fileBizService.getFile(dataFileOne.getId(), null)).thenReturn(dataFileOne);
        when(fileBizService.getLastModifiedDate(dataFileOne.getId())).thenReturn(lastModifiedDate);
        when(fileBizService.getFile(metadataFileOne.getId(), admin)).thenReturn(null);
        when(fileBizService.getFile(metadataFileOne.getId(), null)).thenReturn(null);
        
        MetadataFileBizService metadataFileBizService = mock(MetadataFileBizService.class);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileOne.getId())).thenReturn(metadataFileOne);
        when(metadataFileBizService.retrieveMetadataFile(dataFileOne.getId())).thenReturn(null);
        when(metadataFileBizService.getLastModifiedDate(metadataFileOne.getId())).thenReturn(lastModifiedDate);

        fileController = newFileController(userService, requestUtil, archiveService, fileBizService, metadataFileBizService);
        fileController = spy(fileController);
        
        metadataFileController =  newFileController(userService, metadataRequestUtil, archiveService, fileBizService, metadataFileBizService);
        metadataFileController = spy(metadataFileController);
                
        when(fileController.getAuthenticatedUser()).thenReturn(admin);
        
    }

    /**
     * Test attempt to retrieve a good file by an admin, where a good file is one that exists and is retrievable by
     * authorized user.
     *
     * Expected: Status 200
     *           Etag header
     *           Content-Disposition header
     *           Content-Type header
     *           Content-Lenth header
     *           Last-modified header
     *           File bytestream
     *
     * @throws IOException
     */
    @Test
    public void testGetFileRequestByAdmin() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        final int lowContentLength = 4;
        final int highContentLength = 8;
        
        fileController.handleFileGetRequest(null, null, null, req, res);
        
        //Test status code
        assertEquals(200, res.getStatus());
        //Test headers
        assertNotNull(res.getHeader(ETAG));
        assertNotNull(res.getHeader(CONTENT_DISPOSITION));
        assertNotNull(res.getContentType());
        assertNotNull(res.getHeader(LAST_MODIFIED));
        assertEquals(rfcDateFormatter(lastModifiedDate), res.getHeader(LAST_MODIFIED));

        assertTrue("Content Length out of bounds: " + res.getContentLength(), res.getContentLength() > lowContentLength && res.getContentLength() < highContentLength);
      
        byte[] originalContent = DATA_FILE_ONE_CONTENT.getBytes();
        assertEquals(new String(originalContent), new String(res.getContentAsByteArray()).trim());
    }

    /**
     * Test sending a get file request as a user with no permission to retrieve the file.
     * Expected return code: 403
     * @throws BizPolicyException
     * @throws IOException
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     */
    @Test
    public void testGetFileRequestByUnauthorizedUser() throws BizPolicyException, IOException, ArchiveServiceException,
            RelationshipConstraintException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        FileBizService bizService = fileController.getFileBizService();
        when(bizService.getFile(dataFileOne.getId(), admin)).thenThrow(new BizPolicyException("Mock exception",
                        Type.AUTHORIZATION_ERROR));

        //run the handle request
        fileController.handleFileGetRequest(null, null, null,
                req, res);
        //Test status code
        assertEquals(403, res.getStatus());
    }

    /**
     * Test sending a request to retrieve a non existing file.
     * Expected return code: 404
     * @throws BizPolicyException
     * @throws IOException
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     */
    @Test
    public void testGetFileRequestNonExistingFile () throws BizPolicyException, IOException, ArchiveServiceException,
            RelationshipConstraintException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        FileBizService bizService = fileController.getFileBizService();
        when(bizService.getFile(dataFileOne.getId(), admin)).thenReturn(null);

        MetadataFileBizService metaBizService = fileController.getMetadataFileBizService();
        when(metaBizService.retrieveMetadataFile(dataItemOne.getId())).thenReturn(null);

        //run the handle request
        fileController.handleFileGetRequest("foo", "application/*", beforeModifiedDate.toDate(),
                req, res);
        //Test status code
        assertEquals(404, res.getStatus());
    }

    /**
     * Test trying to retrieve a file that has not been modified since the provided if-modified-date.
     * Expected return code: 304
     * @throws BizPolicyException
     * @throws IOException
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     */
    @Test
    public void testGetFileRequestNotUpdatedSinceFile () throws BizPolicyException, IOException, ArchiveServiceException,
            RelationshipConstraintException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        //run the handle request
        fileController.handleFileGetRequest("foo", "application/*", afterModifiedDate.toDate(),
                req, res);
        //Test status code
        assertEquals(304, res.getStatus());
    }

    /**
     * Test trying to retrieve a file as a user with registration status of pending.
     * Expected return code: 401
     * @throws BizPolicyException
     * @throws IOException
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     */
    @Test
    public void testGetFileRequestByPendingUser() throws BizPolicyException, IOException, ArchiveServiceException,
            RelationshipConstraintException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        
        FileBizService bizService = fileController.getFileBizService();
        when(bizService.getFile(dataFileOne.getId(), admin)).thenThrow(new BizPolicyException("mock exception",
                                                                                              BizPolicyException.Type.AUTHENTICATION_ERROR));

        //run the handle request
        fileController.handleFileGetRequest("foo", "application/*", null,
                req,res);
        //Test status code
        assertEquals(401, res.getStatus());
    }

    /**
     * Test handling a good file request with null "Accept" header and null "If-Modified-Since" header
     * Expected: Status 200
     *           Etag header
     *           Content-Disposition header
     *           Content-Type header
     *           Content-Lenth header
     *           Last-modified header
     *           File bytestream
     * @throws IOException
     */
    @Test
    public void testGetFileRequestNullAcceptModifiedSinceHeader() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        final int lowContentLength = 4;
        final int highContentLength = 8;
        
        //run the handle request
        fileController.handleFileGetRequest("foo", null, null,
                req, res);
        //Test status code
        assertEquals(200, res.getStatus());
        //Test headers
        assertNotNull(res.getHeader(ETAG));
        assertNotNull(res.getHeader(CONTENT_DISPOSITION));
        assertNotNull(res.getContentType());
        assertNotNull(res.getHeader(LAST_MODIFIED));
        assertEquals(rfcDateFormatter(lastModifiedDate), res.getHeader(LAST_MODIFIED));
        assertTrue("Content Length out of bounds: " + res.getContentLength(), res.getContentLength() > lowContentLength && res.getContentLength() < highContentLength);

        byte[] originalContent = DATA_FILE_ONE_CONTENT.getBytes();
        assertEquals(new String(originalContent), new String(res.getContentAsByteArray()).trim());
    }

    /**
     * Test handling a good file request with specific "Accept" header
     * Expected: Status 200
     *           Etag header
     *           Content-Disposition header
     *           Content-Type header
     *           Content-Lenth header
     *           Last-modified header
     *           File bytestream
     * @throws IOException
     */
    @Test
    public void testGetFileRequestSpecificAcceptHeader() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        final int lowContentLength = 4;
        final int highContentLength = 8;
        String expectedMimeType = URLConnection.getFileNameMap().getContentTypeFor(dataFileOne.getName());
        
        //run the handle request
        fileController.handleFileGetRequest("foo", expectedMimeType, null,
                req, res);
        //Test status code
        assertEquals(200, res.getStatus());
        //Test headers
        assertNotNull(res.getHeader(ETAG));
        assertNotNull(res.getHeader(CONTENT_DISPOSITION));
        assertNotNull(res.getContentType());
        assertNotNull(res.getHeader(LAST_MODIFIED));
        assertEquals(rfcDateFormatter(lastModifiedDate), res.getHeader(LAST_MODIFIED));
        assertTrue("Content Length out of bounds: " + res.getContentLength(), res.getContentLength() > lowContentLength && res.getContentLength() < highContentLength);
        byte[] originalContent = DATA_FILE_ONE_CONTENT.getBytes();
        assertEquals(new String(originalContent), new String(res.getContentAsByteArray()).trim());
    }

    /**
     * Test requesting for a file with non acceptable mime type
     * Expected return code 406
     * @throws IOException
     */
    @Test
    public void testGetFileRequestUnacceptedMimeType() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        //run the handle request
        fileController.handleFileGetRequest("foo", "image/*", null,
                req, res);
        //Test status code
        assertEquals(406, res.getStatus());
    }

    /**
     * Test handling exception on unresolveable file id on a good file retrieval request.
     * Expected return code: 500
     * @throws BizPolicyException
     * @throws IOException
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     */
    @Test
    public void testGetFileRequestUnresolvableId() throws BizPolicyException, IOException, ArchiveServiceException,
            RelationshipConstraintException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        RequestUtil util = fileController.getRequestUtil();
        when(util.buildRequestUrl(any(HttpServletRequest.class))).thenReturn("  ");

        fileController.handleFileGetRequest("foo", "application/*", null,
                req, res);
        //Test status code
        assertEquals(500, res.getStatus());
    }


    /**
     * Test handling of ArchiveServiceException file id on a good file retrieval request.
     * Expected return code: 500
     * @throws BizPolicyException
     * @throws IOException
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     */
    @Test
    public void testGetFileRequestHandlingArchiveServiceException() throws BizPolicyException, IOException, ArchiveServiceException,
            RelationshipConstraintException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        FileBizService fileBizService = fileController.getFileBizService();
        when(fileBizService.getFile(dataFileOne.getId(), admin)).thenThrow(new ArchiveServiceException("Mocked exception"));
        fileController.handleFileGetRequest("foo", "application/*", null,
                req, res);
        //Test status code
        assertEquals(500, res.getStatus());
    }
    /**
     * Test handling of RelationshipConstraintException on a good file retrieval request.
     * Expected return code: 500
     * @throws BizPolicyException
     * @throws IOException
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     */
    @Test
    public void testGetFileRequestHandlingRelationshipConstraintException() throws BizPolicyException, IOException, ArchiveServiceException,
            RelationshipConstraintException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        FileBizService fileBizService = fileController.getFileBizService();
        when(fileBizService.getFile(dataFileOne.getId(), admin)).thenThrow(new RelationshipConstraintException("Mocked exception"));
        fileController.handleFileGetRequest("foo", "application/*", null,
                req, res);
        //Test status code
        assertEquals(500, res.getStatus());
    }

    /**
     * Test handling a good non-text file request with specific "Accept" header
     * Expected: Status 200
     *           Etag header
     *           Content-Disposition header
     *           Content-Type header
     *           Content-Lenth header
     *           Last-modified header
     *           File bytestream
     * @throws IOException
     */
    @Test
    public void testGetFileRequestBinaryFile() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "file/foo");
        MockHttpServletResponse res = new MockHttpServletResponse();

        java.io.File binaryFile = new java.io.File(FileControllerTest.class.getResource(FILES_ONLY_ZIP).toURI());
        byte fileContents[] = IOUtils.toByteArray(new FileInputStream(binaryFile));
        long expectedContentLength = binaryFile.length();
        DataFile binaryDataFile = new DataFile("foo", "ZipFileName.zip", binaryFile.toURI().toURL().toExternalForm(),
                "application/zip", binaryFile.getPath(), binaryFile.length(), new ArrayList<String>());

        FileBizService fileBizService = fileController.getFileBizService();
        when(fileBizService.getFile("foo", admin)).thenReturn(binaryDataFile);
        when(fileBizService.getLastModifiedDate("foo")).thenReturn(lastModifiedDate);
        RequestUtil requestUtil = fileController.getRequestUtil();
        when(requestUtil.buildRequestUrl(any(HttpServletRequest.class))).thenReturn("foo");

        String expectedMimeType = URLConnection.getFileNameMap().getContentTypeFor(binaryDataFile.getName());

        //run the handle request
        fileController.handleFileGetRequest("foo", null, null, req, res);
        //Test status code
        assertEquals(200, res.getStatus());
        //Test headers
        assertNotNull(res.getHeader(ETAG));
        assertNotNull(res.getHeader(CONTENT_DISPOSITION));
        assertNotNull(res.getContentType());
        assertEquals(expectedMimeType,  res.getContentType());
        assertNotNull(res.getHeader(LAST_MODIFIED));
        assertEquals(rfcDateFormatter(lastModifiedDate), res.getHeader(LAST_MODIFIED));
        assertEquals(expectedContentLength,  res.getContentLength());

        byte [] responseContent = res.getContentAsByteArray();
        assertArrayEquals(fileContents, responseContent);

    }
    
    /**
     * Tests whether or not the response to a get file request has the correct
     * file name and size in the content disposition field of the header.
     * 
     * @throws IOException
     */
    @Test
    public void testGetFileRequestContentDisposition() throws IOException {
        
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        String expectedContentDispositionString = "attachment; filename=\"" + dataFileOne.getName() + "\";size=" + dataFileOne.getSize();

        when(fileController.getAuthenticatedUser()).thenReturn(admin);

        fileController.handleFileGetRequest("foo", "*/*", null, req, res);

        String actualContentDispositionString =
                (String) res.getHeader(CONTENT_DISPOSITION);

        // Test requestheader content disposition
        assertEquals(expectedContentDispositionString,
                     actualContentDispositionString);
    }

    /**
     * Tests whether a metadata file request returns the proper metadata file
     */
    @Test
    public void testGetMetadataFile() throws Exception{
        MockHttpServletRequest req = new MockHttpServletRequest("GET", REQUEST_STRING);
        MockHttpServletResponse res = new MockHttpServletResponse();
        final int lowContentLength = 5;
        final int highContentLength = 20;

        metadataFileController.handleFileGetRequest(null, null, null, req, res);

        //Test status code
        assertEquals(200, res.getStatus());
        //Test headers
        assertNotNull(res.getHeader(ETAG));
        assertNotNull(res.getHeader(CONTENT_DISPOSITION));
        assertNotNull(res.getContentType());
        assertNotNull(res.getHeader(LAST_MODIFIED));
        assertEquals(rfcDateFormatter(lastModifiedDate), res.getHeader(LAST_MODIFIED));
        assertTrue("Content Length out of bounds: " + res.getContentAsString().length(), res.getContentAsString().length() > lowContentLength && res.getContentAsString().length() < highContentLength);

        byte[] originalContent = METADATA_FILE_ONE_CONTENT.getBytes();
        assertEquals(new String(originalContent), new String(res.getContentAsByteArray()).trim());
    }
    
    private String rfcDateFormatter(DateTime date) {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder().appendPattern("EEE, dd MMM yyyy HH:mm:ss Z")
                .toFormatter();
        return fmt.print(date);
    }
}

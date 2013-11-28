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
package org.dataconservancy.access.connector;

import java.io.IOException;
import java.io.InputStream;

import java.security.NoSuchAlgorithmException;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Executes integration tests against the Data Conservancy HTTP API.  Tests may be executed against a local instance
 * of the HTTP API, or a remote instance.
 * <p/>
 * These tests confirm the behaviors of the DCS HTTP APIs, and are meant to validate the behaviors of the
 * {@link AbstractHttpConnectorTest}.  The {@code AbstractHttpConnectorTest} mocks the behavior of a DCS instance, and
 * this test is meant to validate that the mocked behavior is consistent with actual behavior.
 *
 * @see org.dataconservancy.access.connector.LocalHttpAccessApiIT
 * @see org.dataconservancy.access.connector.RemoteHttpAccessApiIT
 * 
 * TODO Test file upload and sip deposit
 */
public abstract class HttpAccessApiIT extends AbstractConnectorIT {

    /**
     * The configuration for the Data Conservancy Access HTTP API
     */
    private HttpAccessApiConfig accessApi;

    /**
     * The expected fixity of a known DCS File entity.
     */
    private DcsFixity fileFixity;

    /**
     * An instance of HttpClient used to execute requests.
     */
    private HttpClient client;

    /**
     * The ArchiveStore, only used with local tests.
     *
     * @see HttpAccessApiConfig#isLocal()
     */
    @Autowired
    private ArchiveStore store;

    /**
     * Instantiates our HttpClient used to execute queries.
     * Instantiates the HttpAccessApiConfig.
     * Performs assertions on the known DCS File entity, only used with local tests.
     *
     * @throws EntityNotFoundException
     * @throws InvalidXmlException
     * @throws NoSuchAlgorithmException
     * @throws EntityTypeException
     * @throws IOException
     * @see HttpAccessApiConfig#isLocal()
     */
    @Before
    public void setUp() throws EntityNotFoundException, InvalidXmlException, EntityTypeException, IOException, NoSuchAlgorithmException {
        // Our HttpClient
        client = new DefaultHttpClient();

        // HttpAccessApiConfig
        accessApi = getHttpAccessApiConfig();
        assertNotNull("Access API config must not be null.", accessApi);

        LOG.debug("Executing HTTP API integration tests against {} (isLocal: {})", accessApi.getBaseEndpoint(), accessApi.isLocal());
        
        // Only perform the "in archive" assertions if we are dealing with a local access API (running on localhost)
        if (accessApi.isLocal()) {
            final String fileId = accessApi.getExtantFileEntity();

            // Assert that the fileId actually identifies a DcsFile entity
            assertTrue("Missing test DcsFile entity: " + fileId, allTestEntitiesById.containsKey(fileId));
            assertTrue(allTestEntitiesById.get(fileId) instanceof DcsFile);
            // Assert the file entity exists in the archive
            final Dcp archiveDcp = assertEntityExistsInArchive(fileId);
            assertEquals(1, archiveDcp.getFiles().size());

            // Assert that the entity found on the filesystem is the same entity returned by the archive
            final DcsFile fileEntity = (DcsFile) allTestEntitiesById.get(fileId);
            assertEquals(fileEntity, archiveDcp.getFiles().iterator().next());

            // Assert that the content of the file entity exists in the archive and has the proper checksum
            fileFixity = fileEntity.getFixity().iterator().next();
            assertContentExistsInArchive(fileId, fileFixity);
        } else {
            LOG.info("Skipping \"in archive\" tests because we are executing tests against a remote instance of " +
                    "the HTTP API.");
        }
    }

    @After
    public void tearDown() {
        client.getConnectionManager().shutdown();        
    }

    /**
     * Provide the HttpAccessApiConfig.  Subclasses will provide a local or remote configuration.
     *
     * @return
     */
    protected abstract HttpAccessApiConfig getHttpAccessApiConfig();

    /**
     * Ensures that a known File entity can be retrieved from the HTTP Access API, by checking for a 200 response and
     * building the DcsFile from the response. If the API is local, further checks are done.
     *
     * @throws IOException
     * @throws InvalidXmlException
     * @throws EntityNotFoundException
     */
    @Test
    public void testGetEntity() throws IOException, InvalidXmlException, EntityNotFoundException {
        final String fileId = accessApi.getExtantFileEntity();

        final Dcp dcp = getEntity(fileId);
        assertTrue(dcp.getFiles() != null);
        assertEquals(1, dcp.getFiles().size());

        if (accessApi.isLocal()) {
            final DcsFile actual = dcp.getFiles().iterator().next();
            final DcsFile expected = (DcsFile) allTestEntitiesById.get(fileId);
            assertDcsFileEquality(expected, actual);
        }
    }

    /**
     * Ensures that a known, <em>extant</em> File entity can be retrieved from the HTTP Access API, by checking for a 200
     * response and building the DcsFile from the response.
     *
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test
    public void testGetExtantFileEntity() throws IOException, InvalidXmlException {
        final String extantFileEntity = accessApi.getExtantFileEntity();
        final Dcp dcp = getEntity(extantFileEntity);
        assertNotNull(dcp);
        assertEquals(1, dcp.getFiles().size());

        final DcsFile fileEntity = dcp.getFiles().iterator().next();
        assertTrue("File entity " + extantFileEntity + " should be extant, but it isn't.", fileEntity.isExtant());
    }

    /**
     * Ensures that a known, <em>non-extant</em> File entity can be retrieved from the HTTP Access API, by checking for a 200
     * response and building the DcsFile from the response.
     *
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test
    public void testGetNonExtantFileEntity() throws IOException, InvalidXmlException {
        final String nonExtantEntity = accessApi.getNonExtantFileEntity();
        final Dcp dcp = getEntity(nonExtantEntity);
        assertNotNull(dcp);
        assertEquals(1, dcp.getFiles().size());

        final DcsFile fileEntity = dcp.getFiles().iterator().next();
        assertFalse("File entity " + nonExtantEntity + " should not be extant, but it is.", fileEntity.isExtant());
    }

    /**
     * Ensures that a known, <em>extant</em> datastream can be retrieved from the HTTP Access API.
     *
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test
    public void testGetExtantDatastream() throws IOException, InvalidXmlException, NoSuchAlgorithmException {
        final String extantFileEntity = accessApi.getExtantFileEntity();
        final Dcp dcp = getEntity(extantFileEntity);
        final DcsFile fileEntity = dcp.getFiles().iterator().next();
        final InputStream in = getDatastream(fileEntity);
        assertNotNull(in);
        // verify the content
        assertFixtyDigestEqual(fileEntity.getFixity().iterator().next(), in);
    }

    /**
     * Ensures that a known, <em>non-extant</em> datastream can be retrieved from the HTTP Access API.
     *
     * @throws IOException
     * @throws InvalidXmlException
     */
    @Test
    public void testGetNonExtantDatastream() throws IOException, InvalidXmlException, NoSuchAlgorithmException {
        final String extantFileEntity = accessApi.getNonExtantFileEntity();
        final Dcp dcp = getEntity(extantFileEntity);
        final DcsFile fileEntity = dcp.getFiles().iterator().next();
        final InputStream in = getDatastream(fileEntity);
        // verify the content
        assertFixtyDigestEqual(fileEntity.getFixity().iterator().next(), in);
    }

    /**
     * Attempts to retrieve a datastream of a File entity.  This test does not encode the datastream URL at all.
     *
     * @throws IOException
     * @throws EntityTypeException
     * @throws EntityNotFoundException
     * @throws InvalidXmlException
     * @throws NoSuchAlgorithmException
     */
    @Test
    public void testGetDatastreamUnencodedId() throws IOException, EntityTypeException, EntityNotFoundException, InvalidXmlException, NoSuchAlgorithmException {
        final String fileId = accessApi.getExtantFileEntity();

        final String dsId = accessApi.getDatastreamEndpoint() + "/" + fileId;
        LOG.debug("Retrieving datastream id {}", dsId);
        final HttpResponse resp = client.execute(new HttpGet(dsId));
        assertNotNull(resp);
        assertEquals("Error retrieving datastream " + dsId + ": " + resp.getStatusLine().getReasonPhrase(), 200, resp.getStatusLine().getStatusCode());
        assertNotNull(resp.getEntity());

        assertFixtyDigestEqual(fileFixity, resp.getEntity().getContent());
    }

    /**
     * Attempts to retrieve a datastream of a File entity.  This test encodes the <code>/</code> character of the
     * File entity ID.
     *
     * @throws IOException
     * @throws EntityTypeException
     * @throws EntityNotFoundException
     * @throws InvalidXmlException
     * @throws NoSuchAlgorithmException
     */
    @Test
    public void testGetDatastreamForwardSlashPercentEncodedId() throws IOException, EntityTypeException, EntityNotFoundException, InvalidXmlException, NoSuchAlgorithmException {
        final String dsId = accessApi.getDatastreamEndpoint() + "/" + accessApi.getExtantFileEntity().replaceAll("/", "%2F");
        LOG.debug("Retrieving datastream id {}", dsId);
        final HttpResponse resp = client.execute(new HttpGet(dsId));
        assertNotNull(resp);
        assertEquals("Error retrieving datastream " + dsId + ": " + resp.getStatusLine().getReasonPhrase(), 200, resp.getStatusLine().getStatusCode());        assertNotNull(resp.getEntity());

        assertFixtyDigestEqual(fileFixity, resp.getEntity().getContent());
    }

    /**
     * Attempts to retrieve a datastream of a File entity.  This test encodes the <code>/</code> and <code>:</code>
     * characters of the File entity ID.
     *
     * @throws IOException
     * @throws EntityTypeException
     * @throws EntityNotFoundException
     * @throws InvalidXmlException
     * @throws NoSuchAlgorithmException
     */
    @Test
    public void testGetDatastreamFullyPercentEncodedId() throws IOException, EntityTypeException, EntityNotFoundException, InvalidXmlException, NoSuchAlgorithmException {
        final String dsId = accessApi.getDatastreamEndpoint() + "/" + accessApi.getExtantFileEntity().replaceAll("/", "%2F").replaceAll(":", "%3A");
        LOG.debug("Retrieving datastream id {}", dsId);
        final HttpResponse resp = client.execute(new HttpGet(dsId));
        assertNotNull(resp);
        assertEquals("Error retrieving datastream " + dsId + ": " + resp.getStatusLine().getReasonPhrase(), 200, resp.getStatusLine().getStatusCode());        assertNotNull(resp.getEntity());

        assertFixtyDigestEqual(fileFixity, resp.getEntity().getContent());
    }

    /**
     * Retrieve the entity from the HTTP access API.  Assertions are made on the response code, the entity body, and
     * the built sip.
     *
     * @param entityId the entity id to retrieve
     * @return the Dcp object containing the entity
     * @throws IOException
     * @throws InvalidXmlException
     */
    private Dcp getEntity(String entityId) throws IOException, InvalidXmlException {
        final HttpGet req = new HttpGet(entityId);
        final HttpResponse resp = client.execute(req);

        assertNotNull(resp);
        final String errMsg = "Error retrieving entity " + entityId;
        assertEquals(errMsg + " expected 200 HTTP response.", 200, resp.getStatusLine().getStatusCode());
        assertNotNull(errMsg + " HTTP entity body was null.", resp.getEntity());
        final InputStream entityStream = resp.getEntity().getContent();
        assertNotNull(errMsg + " HTTP entity body InputStream was null.", entityStream);

        final Dcp dcp = mb.buildSip(entityStream);
        assertNotNull("Model builder produced a null Dcp SIP.", dcp);
        entityStream.close();
        return dcp;
    }

    private InputStream getDatastream(DcsFile file) throws IOException {
        LOG.debug(file.toString());
        final String fileEntity = file.getId();
        final String encodedFileEntity = fileEntity.replaceAll("/", "%2F");
        final HttpGet req = new HttpGet(accessApi.getDatastreamEndpoint() + "/" + fileEntity);
        final HttpResponse resp = client.execute(req);
        final String errMsg = "Error retrieving datastream '" + encodedFileEntity + "'";
        assertNotNull(errMsg + " response object was null", resp);
        assertNotNull(errMsg + " HTTP entity object was null", resp.getEntity());
        final InputStream content = resp.getEntity().getContent();
        assertNotNull(errMsg + " HTTP entity InputStream was null", content);

        return content;
    }

    /**
     * Asserts that the entity is in the archive and can be deserialized to DCP packaging.
     *
     * @param entityId the entity
     * @return the DCP package containing the entity
     * @throws EntityNotFoundException if the entity is not found
     * @throws InvalidXmlException     if the entity stream cannot be deserialized to DCP packaging
     */
    private Dcp assertEntityExistsInArchive(final String entityId) throws EntityNotFoundException, InvalidXmlException {
        // Assert that the archive store contains the entity of interest, and that it's the correct type
        final InputStream archiveDcpStream = store.getPackage(entityId);
        assertNotNull("Could not find DCP package " + entityId + " in the archive.", archiveDcpStream);
        final Dcp archiveDcp = mb.buildSip(archiveDcpStream);
        assertNotNull(archiveDcp);
        return archiveDcp;
    }

    /**
     * Assert that the content for the supplied DcsFile entity exists in the archive and has the expected checksum.
     *
     * @param fileEntityId   the file entity id
     * @param expectedFixity the expected fixity value
     * @throws EntityTypeException
     * @throws EntityNotFoundException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private void assertContentExistsInArchive(final String fileEntityId, final DcsFixity expectedFixity)
            throws EntityTypeException, EntityNotFoundException, NoSuchAlgorithmException, IOException {
        final InputStream archiveContentStream = store.getContent(fileEntityId);
        assertNotNull(archiveContentStream);
        assertFixtyDigestEqual(expectedFixity, archiveContentStream);
    }

    /**
     * Does a field by field equality check of DcsFile, ignoring the source field which is changed by the
     * access HTTP layer.
     *
     * @param expected the expected file
     * @param actual   the actual file
     */
    private void assertDcsFileEquality(final DcsFile expected, final DcsFile actual) {
        assertNotNull(expected);
        assertNotNull(actual);

        final Collection<DcsFixity> expectedFixity = expected.getFixity();
        final Collection<DcsFixity> actualFixity = actual.getFixity();
        final Collection<DcsFormat> expectedFormat = expected.getFormats();
        final Collection<DcsFormat> actualFormat = actual.getFormats();
        final Collection<DcsMetadata> expectedMd = expected.getMetadata();
        final Collection<DcsMetadata> actualMd = actual.getMetadata();
        final Collection<DcsMetadataRef> expectedMdRef = expected.getMetadataRef();
        final Collection<DcsMetadataRef> actualMdRef = actual.getMetadataRef();
        final String expectedName = expected.getName();
        final String actualName = actual.getName();
        final long expectedSize = expected.getSizeBytes();
        final long actualSize = actual.getSizeBytes();
        final Boolean expectedValid = expected.getValid();
        final Boolean actualValid = actual.getValid();

        assertFieldEquals("fixity", expectedFixity, actualFixity);
        assertFieldEquals("formats", expectedFormat, actualFormat);
        assertFieldEquals("metadata", expectedMd, actualMd);
        assertFieldEquals("metadataref", expectedMdRef, actualMdRef);
        assertFieldEquals("name", expectedName, actualName);
        assertFieldEquals("sizebytes", expectedSize, actualSize);
        assertFieldEquals("valid", expectedValid, actualValid);
    }

    private void assertFieldEquals(final String fieldName, Object expectedFieldValue, Object actualFieldValue) {
        final String errMsg = "Field %s was not equal.  Expected %s Was %s";
        assertEquals(String.format(errMsg, fieldName, expectedFieldValue, actualFieldValue),
                expectedFieldValue, actualFieldValue);
    }

}

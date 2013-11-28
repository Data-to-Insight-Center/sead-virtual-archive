/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.mhf.finders;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.mhf.finder.api.MetadataFindingException;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.test.support.BuilderTestUtil;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.MetadataFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Tests for producing MetadataInstance objects from bytestreams that are encapsulated by MetadataFile or DataFile.
 */
public class EmbeddedMetadataFinderTest {

    /** String written to the temp file backing the {@link #dataFile DataFile} */
    private static final String DATA = "I am some data.";

    /** String written to the temp file backing the {@link #dataFile MetadataFile} */
    private static final String META_DATA = "I am some meta data.";

    /** Mime type of the temp files */
    private static final String FORMAT = "text/plain";

    private static final String METADATA_FORMAT_ID = "dataconservancy.org:metadata-format:fgdc";

    /** Temp file backing the {@link #dataFile DataFile} */
    private File tmpData;

    /** DataFile used in tests, backed by {@link #tmpData} */
    private DataFile dataFile;

    /** Temp file backing the {@link #mdFile MetadataFile} */
    private File tmpMdData;

    /** MetadataFile used in tests, backed by {@link #tmpMdData} */
    private MetadataFile mdFile;

    /**
     * The finder under test
     */
    private EmbeddedMetadataFinder underTest;

    /**
     * Creates temporary files containing metadata and data.  Instantiates a DataFile and MetadataFile backed by
     * the temporary files.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        tmpData = createTempDataFile(IOUtils.toInputStream(DATA));
        dataFile = createDataFile(tmpData);

        tmpMdData = createTempDataFile(IOUtils.toInputStream(META_DATA));
        mdFile = createMetaDataFile(tmpMdData);

        underTest = new EmbeddedMetadataFinder(BuilderTestUtil.newXstreamModelBuilder());
    }

    /**
     * Cleans up the temporary files.
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        if (tmpData != null && tmpData.exists()) {
            FileUtils.deleteQuietly(tmpData);
        }

        if (tmpMdData != null && tmpMdData.exists()) {
            FileUtils.deleteQuietly(tmpMdData);
        }
    }

    /**
     * Insures that the EmbeddedMetadataFinder finds and returns the MetadataInstance on the sample DataFile.
     *
     * @throws Exception
     */
    @Test
    public void testFindMetadataOnDataFile() throws Exception {
        final Collection<MetadataInstance> instances = underTest.findMetadata(dataFile);

        assertNotNull(instances);
        assertEquals(1, instances.size());

        final MetadataInstance instance = instances.iterator().next();
        assertEquals(FORMAT, instance.getFormatId());

        InputStream inputStream = null;
        try {
            inputStream = instance.getContent();
            final String actualContent = IOUtils.toString(inputStream);
            assertNotNull(inputStream);
            assertEquals(DATA, actualContent);
        } catch (Exception e) {
            //Ignore
        } finally {
            inputStream.close();
        }
    }

    /**
     * Insures that the EmbeddedMetadataFinder finds and returns the MetadataInstance on the sample MetadataFile.
     *
     * @throws Exception
     */
    @Test
    public void testFindMetadataOnMetadataFile() throws Exception {
        final Collection<MetadataInstance> instances = underTest.findMetadata(mdFile);

        assertNotNull(instances);
        assertEquals(1, instances.size());

        final MetadataInstance instance = instances.iterator().next();
        assertEquals(METADATA_FORMAT_ID, instance.getFormatId());

        InputStream inputStream = null;
        try {
            inputStream = instance.getContent();
            final String actualContent = IOUtils.toString(inputStream);
            assertNotNull(inputStream);
            assertEquals(META_DATA, actualContent);
        } catch (Exception e) {
            //Ignore
        } finally {
            inputStream.close();
        }

    }

    /**
     * This test is no longer valid because InputStream from metadata files is not opened at the time of MetadataInstance
     * creation, but when MetadataInstance.getContent() is called.
     *
     * Insures that when a DataFile has an unresolvable 'source' field, that the proper
     * exception is thrown when attempting to find metadata on the DataFile.
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testFindMetadataWithUnresolvableSource() throws Exception {
        // We could mimic an "unresolvable" source in a number of ways
        // In this case, we provide a nonsense http:// url source, that we hope
        // never resolves.
        final String unresolvableSource = "http://avzyeqb38.fas31.z1d";
        dataFile.setSource(unresolvableSource);
        URL temp = new URL("http://powirwersdf.sdf");

        Throwable caught = null;
        Throwable cause = null;

        try {
            underTest.findMetadata(dataFile);
        } catch (MetadataFindingException e) {
            caught = e;
            cause = e.getCause();
        }

        assertNotNull(caught);
        assertNotNull(cause);

        assertEquals(MetadataFindingException.class, caught.getClass());

        // This may be a little overboard.  We really should just be testing the MFE, probably.
        assertEquals(UnknownHostException.class, cause.getClass());
    }

    /**
     * This test is no longer valid because InputStream from metadata files is not opened at the time of MetadataInstance
     * creation, but when MetadataInstance.getContent() is called.
     *
     * Insures that when a MetadataFile has a 'source' field pointing to a non-existent file, that the proper
     * exception is thrown when attempting to find metadata on the MetadataFile.
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testFindMetadataWithUnreadableInputStream() throws Exception {
        // We could mimic an "unreadable" inputstream in a number of ways
        // In this case, we delete the File that is pointed to by the source,
        // so that when the source field is read, the file it is pointing to no longer exists.
        assertTrue(tmpMdData.exists());
        FileUtils.deleteQuietly(tmpMdData);
        assertFalse(tmpMdData.exists());

        Throwable caught = null;
        Throwable cause = null;

        try {
            underTest.findMetadata(mdFile);
        } catch (MetadataFindingException e) {
            caught = e;
            cause = e.getCause();
        }

        assertNotNull(caught);
        assertNotNull(cause);

        assertEquals(MetadataFindingException.class, caught.getClass());

        // This may be a little overboard.  We really should just be testing the MFE, probably.
        assertEquals(FileNotFoundException.class, cause.getClass());
    }

    /**
     * Insures that a MFE is thrown when a DataFile with no state (specifically, a null source) is supplied to
     * the finder.
     *
     * @throws Exception
     */
    @Test(expected = MetadataFindingException.class)
    public void testFindMetadataOnEmptyDataFile() throws Exception {
        underTest.findMetadata(new DataFile());
    }

    /**
     * Creates a unique temporary file on the file system, and writes the data in {@code tmpFileData} to the temporary
     * file.
     *
     * @param tmpFileData the data to write to the file, closed by this method
     * @return
     * @throws IOException
     */
    private File createTempDataFile(InputStream tmpFileData) throws IOException {
        File tmpData = File.createTempFile("EmbeddedMetadataFinderTest-sampledata-", ".txt");
        FileOutputStream fos = new FileOutputStream(tmpData);
        IOUtils.copy(tmpFileData, fos);
        fos.close();
        tmpFileData.close();
        return tmpData;
    }

    /**
     * Creates a DataFile, using the supplied File for its source.
     *
     * @param tmpData
     * @return
     * @throws MalformedURLException
     */
    private DataFile createDataFile(File tmpData) throws MalformedURLException {
        DataFile file = new DataFile();
        file.setFormat(FORMAT);
        file.setSource(tmpData.toURI().toURL().toExternalForm());
        return file;
    }

    /**
     * Creates a MetadataFile, using the supplied File for its source.
     *
     * @param tmpData
     * @return
     * @throws MalformedURLException
     */
    private MetadataFile createMetaDataFile(File tmpData) throws MalformedURLException {
        MetadataFile file = new MetadataFile();
        file.setMetadataFormatId(METADATA_FORMAT_ID);
        file.setFormat(FORMAT);
        file.setSource(tmpData.toURI().toURL().toExternalForm());
        return file;
    }

}


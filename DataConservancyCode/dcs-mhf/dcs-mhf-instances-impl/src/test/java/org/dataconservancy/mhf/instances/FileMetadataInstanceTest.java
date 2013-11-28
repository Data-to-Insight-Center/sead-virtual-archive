package org.dataconservancy.mhf.instances;

import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: hanh
 * Date: 3/11/13
 * Time: 12:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileMetadataInstanceTest {
    FileMetadataInstance fileMetadataInstance;
    FileMetadataInstance fileMetadataInstancePlus;
    FileMetadataInstance fileMetadataInstancePlusPlus;
    FileMetadataInstance fileMetadataInstance3;
    private static final String sampleFile = "/SampleMetadataFiles/sample2.xml";
    private static final String sampleFile2 = "/SampleMetadataFiles/fgdc_sample_adopted_tmdls_jan12.shp.xml";



    @Before
    public void setUp() throws MalformedURLException {
        fileMetadataInstance = new FileMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID, this.getClass().getResource(sampleFile));
        fileMetadataInstancePlus = new FileMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID, this.getClass().getResource(sampleFile));
        fileMetadataInstancePlusPlus = new FileMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID, this.getClass().getResource(sampleFile));
        fileMetadataInstance3 = new FileMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID, this.getClass().getResource(sampleFile2));

    }

    @Test
    public void testReadInstanceContentMoreThanOnce() throws IOException {
        boolean failedToReadInstanceContentTwice = false;
        InputStream inputStream = null;
        InputStream inputStream2 = null;

        try {
            inputStream = fileMetadataInstance.getContent();
            inputStream2 = fileMetadataInstance.getContent();
        } catch (IOException e) {
            failedToReadInstanceContentTwice = true;
        } finally {
            inputStream.close();
            inputStream2.close();
        }
        assertFalse(failedToReadInstanceContentTwice);
        assertNotNull(inputStream);
        assertNotNull(inputStream2);
    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertFalse(fileMetadataInstance.equals(fileMetadataInstance3));
        assertFalse(fileMetadataInstance3.equals(fileMetadataInstance));
    }


    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(fileMetadataInstance.equals(fileMetadataInstancePlus));
        assertTrue(fileMetadataInstancePlus.equals(fileMetadataInstance));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(fileMetadataInstance.equals(fileMetadataInstancePlus));
        assertTrue(fileMetadataInstancePlus.equals(fileMetadataInstancePlusPlus));
        assertTrue(fileMetadataInstance.equals(fileMetadataInstancePlusPlus));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(fileMetadataInstance.equals(fileMetadataInstancePlusPlus));
        assertTrue(fileMetadataInstance.equals(fileMetadataInstancePlusPlus));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(fileMetadataInstance.equals(null));
    }

}

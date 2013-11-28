package org.dataconservancy.dcs.contentdetection.droid;

import org.dataconservancy.dcs.contentdetection.api.ContentDetectionService;
import org.dataconservancy.dcs.contentdetection.impl.droid.DroidContentDetectionServiceImpl;
import org.dataconservancy.dcs.contentdetection.impl.droid.DroidDriver;
import org.dataconservancy.model.dcs.DcsFormat;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

/**
 * Tests DroidContentDetectionServiceImpl getter methods
 */
public class DroidContentDetectionServiceImplTest extends DroidBaseUnitTest{

    /**
     * Test retrieving the version of the detector used for content detection
     */
    @Test
    public void testGetDetectorVersion(){
        DroidContentDetectionServiceImpl dcdsi = new DroidContentDetectionServiceImpl();
        Assert.assertTrue("6.1.2".equals(dcdsi.getDetectorVersion()));
    }

    /**
     * Test retrieving the detector used for content detection
     */
    @Test
    public void testGetDetectorName(){
        DroidContentDetectionServiceImpl dcdsi = new DroidContentDetectionServiceImpl();
        Assert.assertTrue("DROID".equals(dcdsi.getDetectorName()));
    }

    /**
     * Test to ensure that a file with unknown format returns expected "application/octet-stream" type
     */
    @Test
    public void testDetectUnknownFormat() throws URISyntaxException {
        File file = new File(this.getClass().getResource(RANDOM_FILE).toURI());

        DroidDriver dd = new DroidDriver();
        List<DcsFormat> formats = dd.detectFormats(file);
        junit.framework.Assert.assertTrue(formats.size() == 0);

        ContentDetectionService cds = new DroidContentDetectionServiceImpl();
        formats = cds.detectFormats(file);
        junit.framework.Assert.assertTrue(formats.size() == 1);
        junit.framework.Assert.assertTrue(formats.contains(dcsFormatUnknown));
    }

    /**
     * This test insures that the Droid version information contained in the droid-version.properties file is
     * up-to-date with the current DROID implementation.  The version and name of the current DROID implementation is
     * obtained from a resource bundle in the droid-command-line module.
     *
     * We maintain a separate droid-version.properties file to avoid the hassle of a runtime dependency on
     * droid-command-line and its resource bundle.
     *
     * @throws Exception
     */
    @Test
    public void testPropertiesAndResourceBundleInSync() throws Exception {
        DroidContentDetectionServiceImpl dcdsi = new DroidContentDetectionServiceImpl();
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ResourceBundle bundle = ResourceBundle.getBundle("options", Locale.US, cl);
        String versionString = bundle.getString("version_no");
        assertEquals(dcdsi.getDetectorVersion(), versionString);
    }
}

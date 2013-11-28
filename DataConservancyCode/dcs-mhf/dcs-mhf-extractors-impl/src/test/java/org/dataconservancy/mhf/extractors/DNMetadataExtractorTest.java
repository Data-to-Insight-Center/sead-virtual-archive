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
package org.dataconservancy.mhf.extractors;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.tiff.TiffMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * Unit test class to demonstrated some functionalities of the Drew Noakes' metadata extractor library
 */
public class DNMetadataExtractorTest {

    private final static String sampleJPEGfile = "/SampleImageFiles/purple.JPG";
    private final static String sampleTIFFIPTCfile = "/SampleImageFiles/sample-iptc-exif-geo.tiff";
    private final static String sampleTIFFfile = "/SampleImageFiles/Night.tif";
    Metadata jpegFileMetadata;
    Metadata tiffIPTCFileMetadata;
    Metadata tifFileMetadata;

    /**
     * Test that various EXIF and GPS metadata is found on the in a JPEG_FORMAT_ID file that is known to contain such data.
     */
    @Test
    public void testExtractEXIFMetadataFromJPEG() throws URISyntaxException, JpegProcessingException, IOException {

        URL url =  this.getClass().getResource(sampleJPEGfile);
        File jpegFile = new File(url.toURI());

        Metadata metadata = new Metadata();
        jpegFileMetadata = JpegMetadataReader.readMetadata(jpegFile);

        for (Directory directory : jpegFileMetadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                System.out.println(tag);
            }
        }
        // obtain the Exif directory
        ExifSubIFDDirectory exifDirectory = jpegFileMetadata.getDirectory(ExifSubIFDDirectory.class);
        assertNotNull(exifDirectory);

        System.out.println(exifDirectory.getObject(ExifSubIFDDirectory.TAG_USER_COMMENT));

        // query the tag's value
        assertNotNull(exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));

        GpsDirectory gpsDirectory = jpegFileMetadata.getDirectory(GpsDirectory.class);

        assertNotNull(gpsDirectory);
        assertNotNull(gpsDirectory.getObject(GpsDirectory.TAG_GPS_ALTITUDE));
        assertNotNull(gpsDirectory.getObject(GpsDirectory.TAG_GPS_ALTITUDE_REF));
        assertNotNull(gpsDirectory.getObject(GpsDirectory.TAG_GPS_LONGITUDE));
        assertNotNull(gpsDirectory.getObject(GpsDirectory.TAG_GPS_LONGITUDE_REF));

    }

    /**
     * Test that various EXIF metadata is found on the in a TIFF_FORMAT_ID file that is known to contain such data.
     */
    @Test
    @Ignore("FIXME: Add the sample tiff file refered to by url2 below.")
    public void testExtractEXIFMetadataFromTIFF() throws URISyntaxException, IOException {
        URL url2 =  this.getClass().getResource(sampleTIFFfile);
        File tifFile = new File(url2.toURI());
        System.out.println(tifFile);
        tifFileMetadata = TiffMetadataReader.readMetadata(tifFile);
        /*for (Directory directory : tifFileMetadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                System.out.println(tag);
            }
        }*/
        // obtain the Exif directory
        ExifSubIFDDirectory exifDirectory = tifFileMetadata.getDirectory(ExifSubIFDDirectory.class);
        assertNotNull(exifDirectory);

        // query the tag's value
        assertNotNull(exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
        assertNotNull(exifDirectory.getObject(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED));


    }
    //TODO:Find sample file with IPTC information on it.
    @Ignore
    @Test
    public void testExtractIPTCMetadataFromTIFF() throws URISyntaxException, IOException {

        URL url3 =  this.getClass().getResource(sampleTIFFIPTCfile);
        File iptcJpgFile = new File(url3.toURI());
        tiffIPTCFileMetadata = TiffMetadataReader.readMetadata(iptcJpgFile);
        /*
        for (Directory directory : tiffIPTCFileMetadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                System.out.println(tag);
            }
        }*/
        // obtain the Exif directory
        ExifSubIFDDirectory exifDirectory = tiffIPTCFileMetadata.getDirectory(ExifSubIFDDirectory.class);
        assertNotNull(exifDirectory);


        GpsDirectory gpsDirectory = jpegFileMetadata.getDirectory(GpsDirectory.class);

        assertNotNull(gpsDirectory);
        assertNotNull(gpsDirectory.getObject(GpsDirectory.TAG_GPS_ALTITUDE));
        assertNotNull(gpsDirectory.getObject(GpsDirectory.TAG_GPS_ALTITUDE_REF));
        assertNotNull(gpsDirectory.getObject(GpsDirectory.TAG_GPS_LONGITUDE));
        assertNotNull(gpsDirectory.getObject(GpsDirectory.TAG_GPS_LONGITUDE_REF));

    }

    //TODO: find JPEG_FORMAT_ID files with embedded ITPC
    @Ignore
    @Test
    public void testExtractIPTCMetadataFromJPEGFile() {

    }
}

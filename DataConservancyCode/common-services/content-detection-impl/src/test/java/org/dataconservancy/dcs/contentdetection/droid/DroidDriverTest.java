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
package org.dataconservancy.dcs.contentdetection.droid;


import junit.framework.Assert;
import org.dataconservancy.dcs.contentdetection.api.ContentDetectionService;
import org.dataconservancy.dcs.contentdetection.impl.droid.DroidContentDetectionServiceImpl;
import org.dataconservancy.dcs.contentdetection.impl.droid.DroidDriver;
import org.dataconservancy.model.dcs.DcsFormat;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Tests DcsFormats derived from DROID content detection process
 */
public class DroidDriverTest extends DroidBaseUnitTest {


    /**
     * Test to ensure that MS Word format detection attempt returns expected DcsFormat output
     * @throws URISyntaxException
     */
    @Test
    public void testDetectWordFormat() throws URISyntaxException {
        File file = new File(this.getClass().getResource(WORD_FILE).toURI());
        DroidDriver dd = new DroidDriver();
        List<DcsFormat> formats = dd.detectFormats(file);
        Assert.assertTrue(formats.size() > 0);
        Assert.assertTrue(formats.contains(dcsFormatWord));
    }

    /**
     * Test to ensure that text format detection attempt returns expected DcsFormat output
     * @throws URISyntaxException
     */
    @Test
    public void testDetectTextFormat() throws URISyntaxException {
        File file = new File(this.getClass().getResource(TEXT_FILE).toURI());
        DroidDriver dd = new DroidDriver();
        List<DcsFormat> formats = dd.detectFormats(file);
        Assert.assertTrue(formats.size() > 0);
        Assert.assertTrue(formats.contains(dcsFormatText));
        Assert.assertTrue(formats.contains(dcsFormatMimeText));
    }

    /**
     * Test to ensure that PDF format detection attempt returns expected DcsFormat output
     * @throws URISyntaxException
     */
    @Test
    public void testDetectPDFFormat() throws URISyntaxException {
        File file = new File(this.getClass().getResource(PDF_FILE).toURI());
        DroidDriver dd = new DroidDriver();
        List<DcsFormat> formats = dd.detectFormats(file);
        Assert.assertTrue(formats.size() > 0);
        Assert.assertTrue(formats.contains(dcsFormatPDF));
        Assert.assertTrue(formats.contains(dcsFormatMimePDF));
    }

    /**
     * Test to ensure that a file with unknown format returns no results from DROID
     *
     */
    @Test
    public void testDetectUnknownFormat() throws URISyntaxException{
        File file = new File(this.getClass().getResource(RANDOM_FILE).toURI());
        DroidDriver dd = new DroidDriver();
        List<DcsFormat> formats = dd.detectFormats(file);
        Assert.assertTrue(formats.size() == 0);
    }
}

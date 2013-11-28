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
 *    File nullFile = new File; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.ui.util;



import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Test class for the utility class ContentTypeUtil which provides mime types for files
 */
public class ContentTypeUtilTest {
    private final static String TEXT_FILE = "/SampleMimeTestFiles/SampleTextDocument";
    private final static String EXCEL_FILE = "/SampleMimeTestFiles/SampleExcelSpreadsheet.xls";
    private final static String EXCEL_WRONG_EXTENSION = "/SampleMimeTestFiles/SampleExcelSpreadsheet.txt";
    private final static String WORD_FILE = "/SampleMimeTestFiles/SampleWordDocument.doc";
    private final static String WORD_XML_FILE = "/SampleMimeTestFiles/SampleWordDocumentXML.docx";

    private final static String DEFAULT_MIME_TYPE_VALUE = "application/octet-stream";
    private final static String TEXT_MIME_TYPE_VALUE =  "text/plain";
    private final static String EXCEL_MIME_TYPE_VALUE = "application/vnd.ms-excel";
    private final static String WORD_MIME_TYPE_VALUE = "application/msword";
    private final static String WORD_XML_MIME_TYPE_VALUE  = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    /**
     * Test that an empty file gets the default mime type
     * @throws IOException
     */
    @Test
    public void testEmptyFileIsOctetStream() throws IOException{
        File tempfile = File.createTempFile("emptyFile",null);
        tempfile.deleteOnExit();
        String mimetype = ContentTypeUtil.detectMimeType(tempfile);
        assertEquals(DEFAULT_MIME_TYPE_VALUE, mimetype);
    }


    /**
     * Test that a text file with no extension gets the correct mime type
     * @throws IOException, URISyntaxException
     */
    @Test
    public void testTextFileNoExtensionMimeType() throws IOException, URISyntaxException {
        URL fileUrl = this.getClass().getResource(TEXT_FILE);
        assertNotNull("Cannot find classpath resource " + TEXT_FILE, fileUrl);
        File file = new File(fileUrl.toURI());
        String mimetype = ContentTypeUtil.detectMimeType(file);
        assertEquals(TEXT_MIME_TYPE_VALUE, mimetype);
    }

    /**
     * Test that an excel file gets teh correct mime type
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testExcelFileMimeType() throws IOException, URISyntaxException {
        URL fileUrl = this.getClass().getResource(EXCEL_FILE);
        assertNotNull("Cannot find classpath resource " + EXCEL_FILE, fileUrl);
        File file = new File(fileUrl.toURI());
        String mimetype = ContentTypeUtil.detectMimeType(file);
        assertEquals(EXCEL_MIME_TYPE_VALUE, mimetype);
    }

    /**
     * Test that an excel file with the wrong extension gets the correct mime type
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testExcelFileWrongExtensionMimeType() throws IOException, URISyntaxException {
        URL fileUrl = this.getClass().getResource(EXCEL_WRONG_EXTENSION);
        assertNotNull("Cannot find classpath resource " + EXCEL_WRONG_EXTENSION, fileUrl);
        File file = new File(fileUrl.toURI());
        String mimetype = ContentTypeUtil.detectMimeType(file);
        assertEquals(EXCEL_MIME_TYPE_VALUE, mimetype);
    }

    /**
     * Test that a word (.doc) file gets the correct mime type
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testWordFileMimeType() throws IOException, URISyntaxException{
        URL fileUrl = this.getClass().getResource(WORD_FILE);
        assertNotNull("Cannot find classpath resource " + WORD_FILE, fileUrl);
        File file = new File(fileUrl.toURI());
        String mimetype = ContentTypeUtil.detectMimeType(file);
        assertEquals(WORD_MIME_TYPE_VALUE, mimetype);
    }

    /**
     * test that a word xml (.docx) file gets teh correct mime type
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testWordXmlFileMimeType() throws IOException, URISyntaxException{
        URL fileUrl = this.getClass().getResource(WORD_XML_FILE);
        assertNotNull("Cannot find classpath resource " + WORD_XML_FILE, fileUrl);
        File file = new File(fileUrl.toURI());
        String mimetype = ContentTypeUtil.detectMimeType(file);
        assertEquals(WORD_XML_MIME_TYPE_VALUE, mimetype);
    }
}



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

import org.dataconservancy.dcs.contentdetection.impl.droid.DroidDriver;
import org.dataconservancy.dcs.contentdetection.impl.droid.DroidIdentifier;
import org.dataconservancy.dcs.contentdetection.impl.droid.DroidSignatureFile;
import org.dataconservancy.model.dcs.DcsFormat;
import org.junit.Before;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 *  Base setup test class for use with DROID unit test classes
 */
public class DroidBaseUnitTest {
    
    public final static String PRONOM_SCHEME_URI = "http://www.nationalarchives.gov.uk/PRONOM/";
    public final static String MIME_TYPE_SCHEME_URI = "http://www.iana.org/assignments/media-types/";

    private DroidIdentifier droidIdentifer;
    private DroidDriver droidDriver;
    private DroidSignatureFile droidSignatureFile;
    private String signatureFilePath;
    private String containerSignatureFilePath;
    protected DcsFormat dcsFormatPDF;
    protected DcsFormat dcsFormatMimePDF;
    protected DcsFormat dcsFormatText;
    protected DcsFormat dcsFormatMimeText;
    protected DcsFormat dcsFormatWord;
    protected DcsFormat dcsFormatUnknown;
    protected static final String PDF_FILE = "/TestPDFFile.pdf";
    protected static final String TEXT_FILE = "/TestTextFile.txt";
    protected static final String WORD_FILE = "/TestWordDocFile.doc";
    protected static final String RANDOM_FILE = "/TestRandomData";



    @Before
    public void setUp() throws URISyntaxException {
        
        droidIdentifer = new DroidIdentifier();
        droidDriver = new DroidDriver();
        droidSignatureFile = new DroidSignatureFile();

        dcsFormatPDF = new DcsFormat();
        dcsFormatPDF.setFormat("fmt/18");
        dcsFormatPDF.setName("Acrobat PDF 1.4 - Portable Document Format");
        dcsFormatPDF.setSchemeUri(PRONOM_SCHEME_URI);
        dcsFormatPDF.setVersion("1.4");

        dcsFormatMimePDF= new DcsFormat();
        dcsFormatMimePDF.setFormat("application/pdf");
        dcsFormatMimePDF.setName("application/pdf");
        dcsFormatMimePDF.setSchemeUri(MIME_TYPE_SCHEME_URI);

        dcsFormatText = new DcsFormat();
        //this will probably change in the future
        dcsFormatText.setFormat("x-fmt/111");
        dcsFormatText.setName("Plain Text File");
        dcsFormatText.setSchemeUri(PRONOM_SCHEME_URI);
        //dcsFormatText.setVersion(null);
        
        dcsFormatMimeText = new DcsFormat();
        dcsFormatMimeText.setFormat("text/plain");
        dcsFormatMimeText.setName("text/plain");
        dcsFormatMimeText.setSchemeUri(MIME_TYPE_SCHEME_URI);
       //dcsFormatMimeText.setVersion(null);

        dcsFormatWord = new DcsFormat();
        dcsFormatWord.setFormat("fmt/40");
        dcsFormatWord.setSchemeUri(PRONOM_SCHEME_URI);

        dcsFormatUnknown = new DcsFormat();
        dcsFormatUnknown.setFormat("application/octet-stream");
        dcsFormatUnknown.setName("application/octet-stream");
        dcsFormatUnknown.setSchemeUri(MIME_TYPE_SCHEME_URI);
        
        // TODO: Add actual path to the files when they're retrieved.
        signatureFilePath = "SomePathInResources";
        containerSignatureFilePath = "SomePathInResources";
    }

}

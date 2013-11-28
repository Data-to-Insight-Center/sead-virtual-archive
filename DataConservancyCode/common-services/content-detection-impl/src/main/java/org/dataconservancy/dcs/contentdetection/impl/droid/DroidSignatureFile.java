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
package org.dataconservancy.dcs.contentdetection.impl.droid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Downloads the most recent DROID signature file to be used for content detection. 
 *
 */
public class DroidSignatureFile {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final static String DEFAULT_SIGNATURE_FILE = "/SignatureFiles/DROID_SignatureFile_V68.xml";
    private final static String DEFAULT_CONTAINER_FILE = "/SignatureFiles/container-signature.xml";

    private File signatureFile;

    public String getSignatureFile() {

        //TODO: make an attempt to download the latest signature file using a SignatureManager

        //if getting most recent fails, fall back on local default file

        String sigfilePath = getResource(DEFAULT_SIGNATURE_FILE);
        if (sigfilePath == null) {
            log.warn("Unable to locate the DROID signature file '" + DEFAULT_SIGNATURE_FILE + "' on the " +
                    "classpath.");
            return null;
        }

        return sigfilePath;
    }
    
    public String getContainerSignatureFile() {

        //TODO: make an attempt to download the latest signature file using a SignatureManager

        //if getting most recent fails, fall back on local default file

        String sigfilePath = getResource(DEFAULT_CONTAINER_FILE);
        if (sigfilePath == null) {
            log.warn("Unable to locate the DROID signature file '" + DEFAULT_CONTAINER_FILE + "' on the " +
                    "classpath.");
            return null;
        }

        return sigfilePath;
    }


    private String getResource(String resource) {
        URL resourceUrl = this.getClass().getResource(resource);
        if (resourceUrl == null) {
            log.warn("Unable to locate a DROID resource '" + resource + "' on the " +
                    "classpath.");
            return null;
        }


        return new File(resourceUrl.getPath()).getPath();
    }
}

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.model.dcs.DcsFormat;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;

/**
 * Wraps the Droid api and provides an easy method to be called by the service implementation.
 * 
 */
public class DroidDriver {
    public final static String PRONOM_SCHEME_URI = "http://www.nationalarchives.gov.uk/PRONOM/";
    public final static String MIME_TYPE_SCHEME_URI = "http://www.iana.org/assignments/media-types/";
    DroidIdentifier droidIdentifier = new DroidIdentifier();

    /**
     * Takes a file and returns a list of DcsFormats after running file identification.
     * 
     * @param file
     * @return List<DcsFormat>
     */
    public List<DcsFormat> detectFormats(File file) {

        List<DcsFormat> formats = new ArrayList<DcsFormat>();
        IdentificationResultCollection identificationResultCollection = droidIdentifier.detectFormat(file);
        for (IdentificationResult identificationResult : identificationResultCollection.getResults()){
            DcsFormat dcsPronomFormat = new DcsFormat();
            dcsPronomFormat.setSchemeUri(PRONOM_SCHEME_URI);
            dcsPronomFormat.setFormat(identificationResult.getPuid());
            if(identificationResult.getName() != null){
               dcsPronomFormat.setName(identificationResult.getName());
            }
            if(identificationResult.getVersion() != null){
                dcsPronomFormat.setVersion(identificationResult.getVersion());
            }
            if(!formats.contains(dcsPronomFormat)){
                formats.add(dcsPronomFormat);
            }

            //mime type may be null
            if(identificationResult.getMimeType() !=  null && !identificationResult.getMimeType().isEmpty()){
                DcsFormat dcsMimeType = new DcsFormat();
                dcsMimeType.setSchemeUri(MIME_TYPE_SCHEME_URI);
                dcsMimeType.setFormat(identificationResult.getMimeType());
                dcsMimeType.setName(identificationResult.getMimeType());

                if(!formats.contains(dcsMimeType)){
                    formats.add(dcsMimeType);
                }
            }
        }

        return formats;
    }

}

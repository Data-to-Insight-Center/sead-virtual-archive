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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.dataconservancy.dcs.contentdetection.api.ContentDetectionService;

import org.dataconservancy.model.dcs.DcsFormat;

/**
 * An implementation of {@code ContentDetectionService}
 *
 */
public class DroidContentDetectionServiceImpl implements ContentDetectionService {

    public final static String MIME_TYPE_SCHEME_URI = "http://www.iana.org/assignments/media-types/";

    private final static String DROID_PROPERTIES_RESOURCE = "/droid-version.properties";

    private final static String DROID_NAME = "dcs.contentdetection.impl.droid.name";

    private final static String DROID_VERSION = "dcs.contentdetection.impl.droid.version";

    DroidDriver droidDriver = new DroidDriver();

    @Override
    public List<DcsFormat> detectFormats(File file) {

        List<DcsFormat> detectedFormats =  droidDriver.detectFormats(file);
        //according to the contract, must return this format if none is detected
        if(detectedFormats.size() == 0){
            String unknownType = "application/octet-stream";
            DcsFormat unknownFormat = new DcsFormat();
            unknownFormat.setSchemeUri(MIME_TYPE_SCHEME_URI);
            unknownFormat.setFormat(unknownType);
            unknownFormat.setName(unknownType);
            detectedFormats.add(unknownFormat);
        }

        return detectedFormats;
    }

    @Override
    public String getDetectorName() {
        return loadDroidProperties().getProperty(DROID_NAME);
    }

    @Override
    public String getDetectorVersion() {
        return loadDroidProperties().getProperty(DROID_VERSION);
    }

    private Properties loadDroidProperties() {
        URL droidPropertiesResource = this.getClass().getResource(DROID_PROPERTIES_RESOURCE);
        if (droidPropertiesResource == null) {
            throw new RuntimeException("Unable to locate " + DROID_PROPERTIES_RESOURCE + " on the classpath!");
        }

        Properties droidProps = new Properties();
        try {
            droidProps.load(droidPropertiesResource.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load DROID properties located at " +
                    droidPropertiesResource.toString() + ": " + e.getMessage(), e);
        }

        return droidProps;
    }
}
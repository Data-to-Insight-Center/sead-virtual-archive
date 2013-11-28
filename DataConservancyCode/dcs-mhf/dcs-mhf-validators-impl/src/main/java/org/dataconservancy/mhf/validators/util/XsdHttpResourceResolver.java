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
package org.dataconservancy.mhf.validators.util;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Arrays;

import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;

/**
 * A Basic Http Schema Resource resolver checks to see that the schema can be resolved at the url provided and returns
 * a registry entry with the scheme details.
 */
public class XsdHttpResourceResolver extends BaseResourceResolver<DcsMetadataScheme> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected RegistryEntry<DcsMetadataScheme> resolveResourceInternal(String resourceString, String baseUrl) {
        RegistryEntry<DcsMetadataScheme> scheme = null;
        if (baseUrl != null && !baseUrl.isEmpty()  && !resourceString.startsWith(baseUrl)) {
            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl.concat("/");
            }
            
            resourceString = baseUrl + resourceString;
        }

        HttpURLConnection connection = null;

        try {
            if (resourceString.startsWith("http")) {
                connection = (HttpURLConnection) new URL(resourceString).openConnection();
                if (connection.getResponseCode() >= 200 && connection.getResponseCode() <= 299) {
                    DcsMetadataScheme entry = new DcsMetadataScheme();
    
                    entry.setName(resourceString.substring(resourceString.lastIndexOf("/")));
                    entry.setSchemaUrl(resourceString);
                    entry.setSource(resourceString);
                    scheme = new BasicRegistryEntryImpl<DcsMetadataScheme>(resourceString, entry,
                            "dataconservancy.types:registry-entry:metadatascheme", Arrays.asList(resourceString), "");
                } 
            }
        } catch (Exception e) {
            log.error("Error resolving resource '" + resourceString + "' (base URL: '" + baseUrl + "'): " +
                    e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return scheme;
    }
    
}
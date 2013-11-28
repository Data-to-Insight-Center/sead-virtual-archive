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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.ui.util;

import org.dataconservancy.ui.exceptions.EZIDMetadataException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.PersonName;

/**
 * A helper class that will create the metadata to pass to a citable locator service. 
 */
public class EZIDCollectionMetadataGeneratorImpl implements EZIDMetadataGenerator<Collection>{

    private String scheme;
    private String hostName;
    private String port;
    private String contextPath;
    private String publisher;
    
    /**
     * Generates EZIDMetadata for a collection. Collection publication date, and creator fields are required.
     * {@inheritDoc}
     */
    @Override
    public EZIDMetadata generateMetadata(Collection collection) throws EZIDMetadataException {
        EZIDMetadata metadata = new EZIDMetadata();
        metadata.addMetadata("_status", "reserved");
        metadata.addMetadata("_profile", "datacite");
        metadata.addMetadata("_target", buildSplashURL(collection));        
        metadata.addMetadata("datacite.resourcetype", "Collection/Data");
        metadata.addMetadata("datacite.title", collection.getTitle());
        metadata.addMetadata("datacite.publisher", publisher);
        
        if (collection.getPublicationDate() != null) {
            metadata.addMetadata("datacite.publicationyear", String.valueOf(collection.getPublicationDate().getYear()));
        } else {
            throw new EZIDMetadataException("Publication Date must be set to generate EZID metadata");
        }
        
        if (!collection.getCreators().isEmpty()) {
            for (PersonName creator : collection.getCreators() ) {
                String name = creator.getFamilyNames();
                if (!creator.getGivenNames().isEmpty()) {
                    name += ", " + creator.getGivenNames();
                } 
                metadata.addMetadata("datacite.creator", name);
            }
        } else {
            throw new EZIDMetadataException("Creators must be set to generate EZID metadata");
        }
        
        return metadata;
    }
    
    private String buildSplashURL(Collection collection) {
        String url = scheme + "://" + hostName;
        if (!port.isEmpty()) {
            url += ":" + port;
        } 
        
        if (!contextPath.isEmpty()) {
            if (!contextPath.startsWith("/")) {
                url += "/";
            }
            url += contextPath;
        }

        if (!url.endsWith("/")) {
            url += "/";
        }
        url += "collection/collection_splash.action?collectionId=" + collection.getId();
        return url;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public void setHost(String host) {
        this.hostName = host;
    }
    
    public String getHost() {
        return hostName;
    }
    
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    
    public String getContextPath() {
        return contextPath;
    }
    
    public void setPort(String port) {
        this.port = port;
    }
    
    public String getPort() {
        return port;
    }
}

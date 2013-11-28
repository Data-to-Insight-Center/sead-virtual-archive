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

import java.io.IOException;

import java.util.ArrayList;

/**
 * A helper class that will create the metadata to pass to a citable locator service. 
 */
public class EZIDMetadata {

    class MetadataPair {
        private String param;
        private String value;
        
        public MetadataPair(String param, String value) {
            this.param = param;
            this.value = value;
        }
        
        public String getParam() {
            return param;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    private ArrayList<MetadataPair> metadataList;
    
    public EZIDMetadata() {
        metadataList = new ArrayList<MetadataPair>();
    }
    
    public void addMetadata(String name, String value) {
        metadataList.add(new MetadataPair(name, value));
    }
    
    /**
     * This method will return an escaped string representing the metadata. This string is ready to be passed to ezid service.
     * @return formattedMetadataString
     * @throws IOException 
     */
    public String serialize() throws IOException {
       StringBuffer b = new StringBuffer();
        
        for (MetadataPair md : metadataList) {
            b.append(escape(md.getParam()) + ": " + escape(md.getValue()) + "\n");
        }
        
        String formatedMetadataString = b.toString();
        return formatedMetadataString;
    }
    
    private String escape (String s) {
        return s.replace("%", "%25").replace("\n", "%0A").
          replace("\r", "%0D").replace(":", "%3A");
    }
    
    protected ArrayList<MetadataPair> getMetadata() {
        return metadataList;
    }
}
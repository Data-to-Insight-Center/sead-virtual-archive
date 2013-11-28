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
package org.dataconservancy.archive.impl.fcrepo.dto;

import java.util.HashMap;

/**
 * Top level DTO for Fedora Digital Objects.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class FedoraDigitalObject {

    private String objectPid;

    //private String schemaVersion;
    private String objectSchemaVersion = "1.1"; // This is the only supported version.

    private String objectLabel;

    private String objectFormat;

    private String objectEncoding;

    private String objectNamespace;

    private String objectOwnerId;

    private String objectLogMessage;

    private Boolean ignoreMime;

    private ObjectProperties objectProperties;

    private HashMap<String, Datastream> datastreamMap =
            new HashMap<String, Datastream>();

    public ObjectProperties getObjectProperties() {
        return objectProperties;
    }

    public void setObjectProperties(ObjectProperties objectProperties) {
        this.objectProperties = objectProperties;
    }

    public HashMap<String, Datastream> getDatastreamMap() {
        return datastreamMap;
    }

    //public void setDatastreamMap(HashMap<String, Datastream> datastreamMap) {
    //    this.datastreamMap = datastreamMap;
    //}

    public String getObjectPid() {
        return objectPid;
    }

    public void setObjectPid(String objectPid) {
        this.objectPid = objectPid;
    }

    public String getObjectSchemaVersion() {
        return objectSchemaVersion;
    }

    public void setObjectSchemaVersion(String objectSchemaVersion) {
        this.objectSchemaVersion = objectSchemaVersion;
    }

    public String getObjectLabel() {
        return objectLabel;
    }

    public void setObjectLabel(String objectLabel) {
        this.objectLabel = objectLabel;
    }

    public String getObjectFormat() {
        return objectFormat;
    }

    public void setObjectFormat(String objectFormat) {
        this.objectFormat = objectFormat;
    }

    public String getObjectEncoding() {
        return objectEncoding;
    }

    public void setObjectEncoding(String objectEncoding) {
        this.objectEncoding = objectEncoding;
    }

    public String getObjectNamespace() {
        return objectNamespace;
    }

    public void setObjectNamespace(String objectNamespace) {
        this.objectNamespace = objectNamespace;
    }

    public String getObjectOwnerId() {
        return objectOwnerId;
    }

    public void setObjectOwnerId(String objectOwnerId) {
        this.objectOwnerId = objectOwnerId;
    }

    public String getObjectLogMessage() {
        return objectLogMessage;
    }

    public void setObjectLogMessage(String objectLogMessage) {
        this.objectLogMessage = objectLogMessage;
    }

    public Boolean getIgnoreMime() {
        return ignoreMime;
    }

    public void setIgnoreMime(Boolean ignoreMime) {
        this.ignoreMime = ignoreMime;
    }

    /*
     * public String getSchemaVersion() { return schemaVersion; } public void
     * setSchemaVersion(String schemaVersion) { // Currently this is a NOOP
     * since the only supported // version is 1.1. //this.schemaVersion =
     * schemaVersion; }
     */

}

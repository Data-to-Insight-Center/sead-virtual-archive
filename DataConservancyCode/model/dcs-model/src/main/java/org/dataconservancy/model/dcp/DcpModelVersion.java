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
package org.dataconservancy.model.dcp;

/**
 * Data Conservancy Packaging version information
 */
public enum DcpModelVersion {

    VERSION_1_0(1.0f, "http://dataconservancy.org/schemas/dcp/1.0");

    private float versionNumber;
    private String xmlns;

    private DcpModelVersion(float version, String xmlns) {
        if (version < 0) {
            throw new IllegalArgumentException("Version number must be greater than 0");
        }
        if (xmlns == null || xmlns.trim().length() == 0) {
            throw new IllegalArgumentException("XML namespace must not be null or the empty string.");
        }
        this.versionNumber = version;
        this.xmlns = xmlns;
    }

    public float getVersionNumber() {
        return versionNumber;
    }

    public String getXmlns() {
        return xmlns;
    }

    public static DcpModelVersion fromXmlns(String xmlns) {
        for ( DcpModelVersion v : values() ) {
            if (v.getXmlns().equals(xmlns)) {
                return v;
            }
        }

        return null;
    }

    public static DcpModelVersion fromVersionNumber(float version) {
        for ( DcpModelVersion v : values() ) {
            if (v.getVersionNumber() == version) {
                return v;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "DcpModelVersion{" +
                "versionNumber=" + versionNumber +
                ", xmlns='" + xmlns + '\'' +
                '}';
    }
}

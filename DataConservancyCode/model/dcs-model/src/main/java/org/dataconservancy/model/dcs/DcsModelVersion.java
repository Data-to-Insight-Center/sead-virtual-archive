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
package org.dataconservancy.model.dcs;

/**
 * Encapsulates information pertaining to a specific version of the Data Conservancy data model.
 */
public enum DcsModelVersion {

    /**
     * Version 1.0 of the Data Conservancy Object Model
     */
    VERSION_1_0(1.0f, "http://dataconservancy.org/schemas/dcm/1.0");

    private float versionNumber;
    private String xmlns;

    private DcsModelVersion(float version, String xmlns) {
        if (version < 0) {
            throw new IllegalArgumentException("Version number must be greater than 0");
        }
        if (xmlns == null || xmlns.trim().length() == 0) {
            throw new IllegalArgumentException("XML namespace must not be null or the empty string.");
        }
        this.versionNumber = version;
        this.xmlns = xmlns;
    }

    /**
     * The version number for this <code>DcsModelVersion</code>.
     *
     * @return the version number
     */
    public float getVersionNumber() {
        return versionNumber;
    }

    /**
     * The XML namespace for this <code>DcsModelVersion</code>.
     *
     * @return the XML namespace
     */
    public String getXmlns() {
        return xmlns;
    }

    /**
     * Composes a <code>DcsModelVersion</code> based on the XML namespace.
     *
     * @param xmlns the XML namespace
     * @return the <code>DcsModelVersion</code> or <code>null</code> if not found
     */
    public static DcsModelVersion fromXmlns(String xmlns) {
        for ( DcsModelVersion v : values() ) {
            if (v.getXmlns().equals(xmlns)) {
                return v;
            }
        }

        return null;
    }

    /**
     * Composes a <code>DcsModelVersion</code> based on the version number.
     *
     * @param version the version number
     * @return the <code>DcsModelVersion</code> or <code>null</code> if not found
     */
    public static DcsModelVersion fromVersionNumber(float version) {
        for ( DcsModelVersion v : values() ) {
            if (v.getVersionNumber() == version) {
                return v;
            }
        }

        return null;
    }

}

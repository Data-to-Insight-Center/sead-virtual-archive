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

/**
 * DTO for Fedora Content Location elements.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class ContentLocation {

    private String locationURL;

    private String type;

    public String getLocationURL() {
        return locationURL;
    }

    public void setLocationURL(String locationURL) {
        this.locationURL = locationURL;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}

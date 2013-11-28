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

import java.util.ArrayList;

/**
 * DTO for Fedora Datastream elements.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class Datastream {

    private String id;

    private String controlGroup;

    private String state;

    private String versionable;

    private ArrayList<DatastreamVersion> versionList =
            new ArrayList<DatastreamVersion>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getControlGroup() {
        return controlGroup;
    }

    public void setControlGroup(String controlGroup) {
        this.controlGroup = controlGroup;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVersionable() {
        return versionable;
    }

    public void setVersionable(String versionable) {
        this.versionable = versionable;
    }

    public ArrayList<DatastreamVersion> getVersionList() {
        return versionList;
    }

    //public void setVersionList(ArrayList<DatastreamVersion> versionList) {
    //    this.versionList = versionList;
    //}

}

/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.bagit.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Lists active workspaces in ACR as XML with corresponding Id,
 * which needs to be passed as argument to getBag function to create
 * bags from SEAD ACR collections.
 */
@XmlRootElement
public class ActiveWorkspaces {
    private List<ActiveWorkspace> spaceList= new ArrayList<ActiveWorkspace>();

    public List<ActiveWorkspace> getSpaceList() {
        return spaceList;
    }

    public void setSpaceList(List<ActiveWorkspace> activeWorkspace) {
        this.spaceList = activeWorkspace;
    }

    public void addActiveWorkspace(ActiveWorkspace activeWorkspace) {
        this.spaceList.add(activeWorkspace);
    }
}

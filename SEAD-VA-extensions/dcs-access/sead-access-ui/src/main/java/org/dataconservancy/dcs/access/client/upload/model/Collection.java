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
package org.dataconservancy.dcs.access.client.upload.model;

import java.util.ArrayList;
import java.util.List;

public class Collection
        extends Entity {

    private CoreMetadata coremd;

    private String parent;

    private List<Metadata> metadata;

    public Collection() {
        this.coremd = new CoreMetadata();
        this.metadata = new ArrayList<Metadata>();
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public CoreMetadata getCoreMetadata() {
        return coremd;
    }

    public void setCoreMetadata(CoreMetadata coremd) {
        this.coremd = coremd;
    }
    
    public List<Metadata> metadata() {
        return metadata;
    }
}

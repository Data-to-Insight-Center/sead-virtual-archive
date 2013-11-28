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
package org.dataconservancy.dcs.ingest.ui.client.model;

import java.util.ArrayList;
import java.util.List;

public class DeliverableUnit
        extends Entity {

    private CoreMetadata coremd;

    private List<String> parents;

    private List<String> collections;

    private List<Metadata> metadata;

    private List<String> datafiles;

    public DeliverableUnit() {
        this.coremd = new CoreMetadata();
        this.metadata = new ArrayList<Metadata>();
        this.datafiles = new ArrayList<String>();
        this.parents = new ArrayList<String>();
        this.collections = new ArrayList<String>();
    }

    public List<Metadata> metadata() {
        return metadata;
    }

    public List<String> files() {
        return datafiles;
    }

    public List<String> parents() {
        return parents;
    }
    
    public List<String> collections() {
        return collections;
    }

    public CoreMetadata getCoreMetadata() {
        return coremd;
    }

    public void setCoreMetadata(CoreMetadata coremd) {
        this.coremd = coremd;
    }
}

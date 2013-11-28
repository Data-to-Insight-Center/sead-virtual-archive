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

import com.google.gwt.user.client.rpc.IsSerializable;

public class Package
        implements IsSerializable {

    private List<Collection> collections;

    private List<File> files;

    private List<DeliverableUnit> dus;
    
    private List<Repository> repositories;

    public Package() {
        this.collections = new ArrayList<Collection>();
        this.files = new ArrayList<File>();
        this.dus = new ArrayList<DeliverableUnit>();
        this.repositories = new ArrayList<Repository>();
    }

    public List<Collection> collections() {
        return collections;
    }

    public List<File> files() {
        return files;
    }

    public List<DeliverableUnit> deliverableUnits() {
        return dus;
    }

	public List<Repository> getRepositories() {
		return repositories;
	}

	public void setRepositories(List<Repository> repositories) {
		this.repositories = repositories;
	}
}

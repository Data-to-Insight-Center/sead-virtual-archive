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

package org.dataconservancy.dcs.access.http.dataPackager;

import org.seadva.archive.ArchiveEnum;
import org.seadva.archive.SeadArchiveStore;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

/**
 * Package creator loads archive stores from Spring
 */
public class PackageCreatorBase {

    protected Map<ArchiveEnum.Archive,SeadArchiveStore> archiveStores;
    protected String cachePath;

    @Required
    public void setArchiveStores(Map<ArchiveEnum.Archive, SeadArchiveStore> archiveStores) {
        this.archiveStores = archiveStores;
    }

    @Required
    public void setCachePath(String cachePath) {
        this.archiveStores = archiveStores;
    }
}

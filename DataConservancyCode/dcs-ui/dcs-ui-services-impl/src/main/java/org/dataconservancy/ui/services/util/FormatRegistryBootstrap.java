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
package org.dataconservancy.ui.services.util;

import java.util.Iterator;

import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.services.ArchiveService;

public class FormatRegistryBootstrap {
    
    private ArchiveService archiveService;
    private TypedRegistry<DcsMetadataFormat> memoryRegistry;
    
    public FormatRegistryBootstrap(ArchiveService archiveService, TypedRegistry<DcsMetadataFormat> memoryRegistry) {
        this.archiveService = archiveService;
        this.memoryRegistry = memoryRegistry;
    }
    
    public void bootstrapFormats() {
        Iterator<RegistryEntry<DcsMetadataFormat>> iter = memoryRegistry.iterator();
        
        while(iter.hasNext()) {
            try {
                archiveService.deposit(iter.next());
            } catch (ArchiveServiceException e) {
                throw new RuntimeException(e);
            }
        }
        
        try {
            archiveService.pollArchive();
        } catch (ArchiveServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
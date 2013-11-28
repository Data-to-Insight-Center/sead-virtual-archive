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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant;

/**
 * Implementation of a {@code TypedRegistry<DcsMetadataScheme>} that wraps a {@code TypedRegistry<DcsMetadataFormat>}
 *
 */
public class MetadataSchemeRegistryWrapper implements TypedRegistry<DcsMetadataScheme> {

    private TypedRegistry<DcsMetadataFormat> formatRegistry;
    private String description;
    
    /**
     * Base constructor MetadataSchemeRegistryWrapper
     * @param id The id of the registry - not currently used
     * @param description A text description of the registry.
     * @param formatRegistry The {@code TypedRegistry<DcsMetadataFormat>} that this registry will wrap. This registry will interact
     * with all schemes stored in the {@code DcsMetadataFormat}s in the provided registry. 
     */
    public MetadataSchemeRegistryWrapper(String id, String description, TypedRegistry<DcsMetadataFormat> formatRegistry) {
        this.description = description;
        this.formatRegistry = formatRegistry;
    }
    
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<RegistryEntry<DcsMetadataScheme>> lookup(String... keys) {
        final Set<RegistryEntry<DcsMetadataScheme>> results = new HashSet<RegistryEntry<DcsMetadataScheme>>();
        Iterator<RegistryEntry<DcsMetadataFormat>> iter = formatRegistry.iterator();
        
        while (iter.hasNext()) {
            final Collection<DcsMetadataScheme> schemes = iter.next().getEntry().getSchemes();

            for (DcsMetadataScheme scheme : schemes) {
                RegistryEntry<DcsMetadataScheme> schemeRegistryEntry = asRegistryEntry(scheme);
                if (schemeRegistryEntry.getKeys().containsAll(Arrays.asList(keys))) {
                    results.add(schemeRegistryEntry);
                }
            }
        }

        return results;
    }

    @Override
    public Iterator iterator() {
        Set<RegistryEntry<DcsMetadataScheme>> schemeRegistryEntries = new HashSet<RegistryEntry<DcsMetadataScheme>>();
        
        Iterator<RegistryEntry<DcsMetadataFormat>> formatIter = formatRegistry.iterator();
        while (formatIter.hasNext()) {
            DcsMetadataFormat format = formatIter.next().getEntry();
            for (DcsMetadataScheme scheme : format.getSchemes()) {
                schemeRegistryEntries.add(asRegistryEntry(scheme));
            }
        }
        return schemeRegistryEntries.iterator();
    }

    @Override
    public String getType() {
        return MetadataRegistryConstant.METADATASCHEME_REGISTRY_ENTRY_TYPE;
    }

    @Override
    public RegistryEntry<DcsMetadataScheme> retrieve(String id) {
        Iterator<RegistryEntry<DcsMetadataFormat>> iter = formatRegistry.iterator();
        while (iter.hasNext()) {
            final Collection<DcsMetadataScheme> schemes = iter.next().getEntry().getSchemes();

            for (DcsMetadataScheme scheme : schemes) {
                RegistryEntry<DcsMetadataScheme> schemeRegistryEntry = asRegistryEntry(scheme);
                if (schemeRegistryEntry.getId().equals(id)) {
                    return schemeRegistryEntry;
                }
            }
        }

        return null;
    }
    
    /**
     * Converts a DcsMetadataScheme to a RegistryEntry.
     * <dl>
     *     <dt>Registry Entry ID</dt>
     *     <dd>Mapped from the DcsMetadataScheme source</dd>
     *     <dt>Registry Entry Keys</dt>
     *     <dd>Two keys mapped from the DcsMetadataScheme schema URL: the full schema URL, and the portion of the url
     *         after the last slash.</dd>
     *     <dt>Registry Entry Type</dt>
     *     <dd>dataconservancy.types:registry-entry:metadatascheme</dd>
     *     <dt>Registry Entry Description</dt>
     *     <dd>"Metadata Scheme for &lt;<em>schemeUrl</em>&gt;"</dd>
     * </dl>
     * Note: package-private for unit testing.
     *
     * @param scheme the DcsMetadataScheme
     * @return the DcsMetadataScheme converted to a RegistryEntry
     */
    RegistryEntry<DcsMetadataScheme> asRegistryEntry(DcsMetadataScheme scheme) {
        Set<String> keys = new HashSet<String>();
        final String schemaUrl = scheme.getSchemaUrl();
        keys.add(schemaUrl);
        keys.add(schemaUrl.substring(schemaUrl.lastIndexOf("/") + 1));
        return new BasicRegistryEntryImpl<DcsMetadataScheme>(scheme.getSource(), scheme,
                MetadataRegistryConstant.METADATASCHEME_REGISTRY_ENTRY_TYPE, keys, "Metadata Scheme for " + schemaUrl);
    }
}
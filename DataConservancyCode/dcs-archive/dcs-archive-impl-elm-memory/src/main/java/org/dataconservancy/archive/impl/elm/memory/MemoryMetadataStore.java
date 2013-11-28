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
package org.dataconservancy.archive.impl.elm.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.archive.impl.elm.Metadata;
import org.dataconservancy.archive.impl.elm.MetadataStore;

/**
 * Stores all metadata in memory.
 * <p>
 * Implemented by a LinkedHashMap, so the iterator order in
 * {@link #getAll(String...)} is the order in which deposited.
 * </p>
 */
public class MemoryMetadataStore
        implements MetadataStore {

    private Map<String, Metadata> metadata =
            new LinkedHashMap<String, Metadata>();

    @Override
    public Metadata get(String id) {
        return metadata.get(id);
    }

    @Override
    public Metadata add(String id, String type, String src) {
        Metadata md = new MetadataImpl(id, type, src);
        metadata.put(id, md);
        return md;
    }

    @Override
    public Iterable<Metadata> getAll(String... type) {

        if (type.length == 0) {
            return metadata.values();
        }

        List<Metadata> matching = new ArrayList<Metadata>();
        Set<String> types = new HashSet<String>(Arrays.asList(type));

        for (Metadata m : metadata.values()) {
            if (types.contains(m.getType())) {
                matching.add(m);
            }
        }

        return matching;
    }

    @Override
    public void remove(String id) {
        metadata.remove(id);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    private class MetadataImpl
            implements Metadata {

        private final String id;

        private final String type;

        private final String src;

        private final Map<String, String> links =
                new LinkedHashMap<String, String>();

        public String getId() {
            return id;
        }

        public MetadataImpl(String id, String type, String src) {
            this.id = id;
            this.type = type;
            this.src = src;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getSrc() {
            return src;
        }

        @Override
        public void addLinks(Map<String, String> idAndTypeMap) {
            links.putAll(idAndTypeMap);
        }

        @Override
        public Map<String, String> getLinks() {
            return links;
        }
    }

}

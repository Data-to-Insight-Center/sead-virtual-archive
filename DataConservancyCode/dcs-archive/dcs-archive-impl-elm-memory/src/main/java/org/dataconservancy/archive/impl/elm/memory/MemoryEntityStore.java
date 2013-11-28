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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.impl.elm.EntityStore;

/** Stores all entities in memory.
 */
public class MemoryEntityStore
        implements EntityStore {

    private Map<String, byte[]> content = new LinkedHashMap<String, byte[]>();

    public InputStream get(String entityId) throws EntityNotFoundException {
        if (content.containsKey(entityId)) {
            return new ByteArrayInputStream(content.get(entityId));
        } else {
            throw new EntityNotFoundException(entityId);
        }
    }

    public void put(String entityId, InputStream stream) {
        try {
            content.put(entityId, IOUtils.toByteArray(stream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void remove(String key) {
        if (content.containsKey(key)) {
            content.remove(key);
        }
    }

}

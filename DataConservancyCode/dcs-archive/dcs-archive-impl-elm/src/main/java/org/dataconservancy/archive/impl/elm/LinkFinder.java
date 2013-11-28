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
package org.dataconservancy.archive.impl.elm;

import java.util.Map;

import org.dataconservancy.model.dcs.DcsEntity;

/**
 * Finds relevant outbound links from a given entity.
 */
public interface LinkFinder {

    /**
     * Gets all outbound links from a given entity. Map<rel_id, Map<id, type>>
     * 
     * @param entity
     *        Entity from which to derive outbound links, which point from the
     *        object to some external entity.
     * @return Map of the form Map(rel_id, Map(id, type)) where rel_id is a
     *         related object identifier, id is the given object's identifier,
     *         and type is the given object's type.
     */
    public Map<String, Map<String, String>> getOutboundLinks(DcsEntity entity);
}

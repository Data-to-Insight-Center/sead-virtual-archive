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

import java.io.InputStream;

import java.util.Set;

/**
 * Logic used in determining the content of a DIP.
 */
public interface DipLogic {

    /**
     * Apply dip logic to the given entity.
     * <p>
     * Introspects on the entity and its metadata, and produces a list of nodes
     * that are possibly relevant for inclusion in a DIP. If the entity itself
     * is among these nodes, then it is considered 'accepted' into the entities
     * within a DIP.
     * </p>
     * 
     * @param id
     *        Identity of the entity to which dip logic is being applied.
     * @param content
     *        Content of the entity to which dip logic is being applied.
     * @param md
     *        Metadata of the entity to which dip logic is being applied.
     * @param initial
     *        True, if the id represents the initial entity in a particular
     *        extration sequence.
     * @return Set of all applicable entities identified by applying the dip
     *         logic to this entity.
     */
    public Set<String> extractEntities(String id,
                                       InputStream content,
                                       Metadata md,
                                       boolean initial);
}

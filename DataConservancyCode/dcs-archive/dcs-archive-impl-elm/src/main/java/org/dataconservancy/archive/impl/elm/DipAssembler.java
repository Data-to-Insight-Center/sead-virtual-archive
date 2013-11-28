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
import java.util.Set;

import org.dataconservancy.archive.api.EntityNotFoundException;

/**
 * Assembles the entities that comprise a DIP.
 * <p>
 * Applies given dip logic to stored entities in order to derive the content of
 * a DIP.
 * </p>
 */
public interface DipAssembler {

    /**
     * Apply the given DIP logic to the given entity.
     * 
     * @param id
     *        Identity of the initial entity to which DIP logic is applied.
     * @param extractor
     *        DIP logic.
     * @return Map, keyed on etity type, containing a set of eligible entities
     *         for each type.
     * @throws EntityNotFoundException
     *         In the case that the initial id is not found.
     */
    public Map<String, Set<String>> getEntities(String id, DipLogic extractor)
            throws EntityNotFoundException;
}

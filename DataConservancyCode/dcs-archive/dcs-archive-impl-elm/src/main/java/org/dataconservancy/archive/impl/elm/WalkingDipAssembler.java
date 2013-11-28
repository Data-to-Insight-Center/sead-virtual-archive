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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.archive.api.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of DipAssembler based on recursive graph walking.
 */
public class WalkingDipAssembler
        implements DipAssembler {

    private static final Logger log =
            LoggerFactory.getLogger(WalkingDipAssembler.class);

    private final MetadataStore mStore;

    private final EntityStore eStore;

    public WalkingDipAssembler(EntityStore e, MetadataStore m) {
        eStore = e;
        mStore = m;
    }

    public Map<String, Set<String>> getEntities(String id, DipLogic logic)
            throws EntityNotFoundException {

        Map<String, Set<String>> dipEntities =
                new HashMap<String, Set<String>>();

        Metadata md = mStore.get(id);

        if (md != null) {
            walkFullDip(id, md, dipEntities, new HashSet<String>(), logic, true);
        } else {
            throw new EntityNotFoundException(id);
        }

        return dipEntities;
    }

    private void walkFullDip(String entity,
                             Metadata entityMetadata,
                             Map<String, Set<String>> entitiesSoFar,
                             Set<String> banned,
                             DipLogic logic,
                             boolean initial) {
        try {
            Set<String> candidates =
                    logic.extractEntities(entity,
                                          eStore.get(entity),
                                          entityMetadata,
                                          initial);

            /*
             * If the logic engine selects the node itself, then we consider it
             * approved, and add it to the result entity set. Otherwise, we
             * consider it rejectd, and ban it.
             */
            if (candidates.contains(entity)) {
                addToMap(entity, entityMetadata.getType(), entitiesSoFar);
                candidates.remove(entity);
            } else {
                banned.add(entity);
            }

            for (String id : candidates) {

                if (id.equals(entity)) {
                    addToMap(id, entityMetadata.getType(), entitiesSoFar);
                } else {
                    Metadata m = mStore.get(id);

                    /*
                     * If we encounter a node that exists, has not been selected
                     * yet, and is not banned, then investigate it.
                     */
                    if (m != null
                            && !existsInMap(id, m.getType(), entitiesSoFar)
                            && !(banned.contains(m.getId()))) {
                        walkFullDip(id, m, entitiesSoFar, banned, logic, false);
                    }
                }
            }
        } catch (EntityNotFoundException e) {
            /* Let's just log this */
            log.warn(String.format("In creating full dip, %s not found", entity));
        }
    }

    private static void addToMap(String id,
                                 String type,
                                 Map<String, Set<String>> entities) {
        if (entities.get(type) == null) {
            entities.put(type, new HashSet<String>());
        }
        entities.get(type).add(id);
    }

    private static boolean existsInMap(String id,
                                       String type,
                                       Map<String, Set<String>> entities) {

        if (entities.containsKey(type)) {
            return entities.get(type).contains(id);
        }
        return false;
    }
}

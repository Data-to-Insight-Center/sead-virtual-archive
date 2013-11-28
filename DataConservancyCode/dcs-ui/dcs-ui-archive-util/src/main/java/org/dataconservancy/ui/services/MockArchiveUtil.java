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
package org.dataconservancy.ui.services;

import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A mock implementation of {@code ArchiveUtil}.
 * <p/>
 * This implementation is backed by a {@code Map} of entities, keyed by their identifier.  When entities are
 * {@link #addEntity(org.dataconservancy.model.dcs.DcsEntity...) added}, a {@link ID_MAPPING_STRATEGY mapping strategy}
 * is consulted before the entity is added to the backing map.  <em>This strategy determines which entity identifiers
 * are used as keys in the backing map</em>.  Note that the mapping strategy can be modified after instances of
 * {@code MockArchiveUtil} are created. This allows clients of {@code MockArchiveUtil} to set their own strategy
 * at will.
 * <p/>
 * Consider this example entity and the table below for mapping examples:
 * <pre>
 * DcsDeliverableUnit
 * - entityId: 'duEntityId'
 * - formerExternalRefs: 'businessId1', 'businessId2'
 * - alternateIds: 'altId1', 'altId2'
 * </pre>
 * <table>
 *     <tr><th>Strategy</th><th>Map contents (key->value)</th></tr>
 *     <tr><td>{@code ENTITY_ID}</td><td><ul><li>{@code duEntityId} -> {@code DcsDeliverableUnit}</li></ul></td></tr>
 *     <tr><td>{@code ENTITY_AND_ALT_ID}</td><td><ul><li>{@code duEntityId} -> {@code DcsDeliverableUnit}</li>
 *                                                   <li>{@code altId1} -> {@code DcsDeliverableUnit}</li>
 *                                                   <li>{@code altId1} -> {@code DcsDeliverableUnit}</li>
 *                                               </ul></td></tr>
 *     <tr><td>{@code ENTITY_AND_FORMER_REFS}</td><td><ul><li>{@code duEntityId} -> {@code DcsDeliverableUnit}</li>
 *                                                   <li>{@code businessId1} -> {@code DcsDeliverableUnit}</li>
 *                                                   <li>{@code businessId2} -> {@code DcsDeliverableUnit}</li>
 *                                               </ul></td></tr>
 *     <tr><td>{@code ENTITY_ALT_AND_FORMER_REFS}</td><td><ul><li>{@code duEntityId} -> {@code DcsDeliverableUnit}</li>
 *                                                   <li>{@code altId1} -> {@code DcsDeliverableUnit}</li>
 *                                                   <li>{@code altId1} -> {@code DcsDeliverableUnit}</li>
 *                                                   <li>{@code businessId1} -> {@code DcsDeliverableUnit}</li>
 *                                                   <li>{@code businessId2} -> {@code DcsDeliverableUnit}</li>
*                                                </ul></td></tr>
 *     <tr><td>{@code FORMER_REFS}</td><td><ul><li>{@code businessId1} -> {@code DcsDeliverableUnit}</li>
 *                                              <li>{@code businessId2} -> {@code DcsDeliverableUnit}</li>
*                                          </ul></td></tr>
 *     <tr><td>{@code FORMER_REFS_AND_ALT_ID}</td><td><ul><li>{@code businessId1} -> {@code DcsDeliverableUnit}</li>
 *                                              <li>{@code businessId2} -> {@code DcsDeliverableUnit}</li>
 *                                              <li>{@code altId1} -> {@code DcsDeliverableUnit}</li>
 *                                                   <li>{@code altId1} -> {@code DcsDeliverableUnit}</li>
 *                                          </ul></td></tr>
 * </table>
 * <p/>
 */
public class MockArchiveUtil extends BaseArchiveUtil {

    /**
     * Strategy used when storing entities in the backing {@code Map}
     *
     * @see MockArchiveUtil#addEntity(org.dataconservancy.model.dcs.DcsEntity...)
     */
    public enum ID_MAPPING_STRATEGY {
        /**
         * Map entity identifiers to entity objects only.
         */
        ENTITY_ID,

        /**
         * Map entity identifiers and entity alternate ids to entity objects.
         */
        ENTITY_AND_ALT_ID,

        /**
         * Map entity identifiers and entity former external references to entity objects
         */
        ENTITY_AND_FORMER_REFS,

        /**
         * Map entity identifiers, alternate identifiers, and former external references to entity objects
         */
        ENTITY_ALT_AND_FORMER_REFS,

        /**
         * Map former external references only
         */
        FORMER_REFS,

        /**
         * Map former refs and alternate ids only
         */
        FORMER_REFS_AND_ALT_ID

    }

    /**
     * The default mapping strategy used.
     */
    public static ID_MAPPING_STRATEGY DEFAULT_MAPPING_STRATEGY = ID_MAPPING_STRATEGY.FORMER_REFS;

    /**
     * The backing Map, available to subclasses who override any of the {@code protected map...} methods.
     */
    protected final Map<String, Set<DcsEntity>> entityMap = new HashMap<String, Set<DcsEntity>>();

    private ID_MAPPING_STRATEGY mappingStrategy = DEFAULT_MAPPING_STRATEGY;

    /**
     * Returns the current id mapping strategy in use.
     *
     */
    public ID_MAPPING_STRATEGY getMappingStrategy() {
        return mappingStrategy;
    }

    /**
     * Set the mapping strategy to use when storing entities in the backing {@code Map}
     *
     * @param mappingStrategy the mapping strategy
     */
    public void setMappingStrategy(ID_MAPPING_STRATEGY mappingStrategy) {
        this.mappingStrategy = mappingStrategy;
    }

    /**
     * Returns an entity from the backing map using the provided identifier.  If the identified entity has not been
     * {@link #addEntity(org.dataconservancy.model.dcs.DcsEntity...) added}, or if the
     * {@link #getMappingStrategy() mapping strategy} did not map the provided identifier, this method will return
     * {@code null}.
     *
     * @param id the entity identifier
     * @return the entity, or {@code null} if not found.
     */
    @Override
    public DcsEntity getEntity(String id) {
        if (entityMap.containsKey(id)) {
            return entityMap.get(id).iterator().next();
        }

        return null;
    }

    public Set<DcsEntity> getEntities(String id) {
        return entityMap.get(id);
    }

    /**
     * Adds entities to the backing {@code Map} using the {@link #getMappingStrategy() current mapping strategy}.
     * <em>Existing key/value pairs will be silently overwritten</em>.  If you want to intercept mapping calls,
     * override one of the {@code protected map...} methods.
     *
     * @param entities the entities to add
     */
    public final void addEntity(DcsEntity... entities) {
        switch (mappingStrategy) {
            case ENTITY_ID:
                mapEntityIds(entities);
                break;

            case ENTITY_AND_ALT_ID:
                mapEntityIds(entities);
                mapAlternateIds(entities);
                break;

            case ENTITY_AND_FORMER_REFS:
                mapEntityIds(entities);
                mapFormerRefs(entities);
                break;

            case ENTITY_ALT_AND_FORMER_REFS:
                mapEntityIds(entities);
                mapAlternateIds(entities);
                mapFormerRefs(entities);
                break;

            case FORMER_REFS:
                mapFormerRefs(entities);
                break;

            case FORMER_REFS_AND_ALT_ID:
                mapFormerRefs(entities);
                mapAlternateIds(entities);
                break;
        }
    }

    /**
     * Returns the bare backing {@code Map}
     *
     * @return the backing map
     */
    public Map<String, Set<DcsEntity>> getEntities() {
        return entityMap;
    }

    /**
     * Places the supplied entities in the backing map, keyed by their entity identifiers.
     *
     * @param entities the entities to add in the backing map
     */
    protected void mapEntityIds(DcsEntity... entities) {
        for (DcsEntity e : entities) {
            Set<DcsEntity> value;
            if (entityMap.containsKey(e.getId())) {
                value = entityMap.get(e.getId());
            } else {
                value = new HashSet<DcsEntity>();
            }
            value.add(e);
            entityMap.put(e.getId(), value);
        }
    }

    /**
     * Places the supplied entities in the backing map, keyed by their alternate identifiers.
     *
     * @param entities the entities to add in the backing map
     */
    protected void mapAlternateIds(DcsEntity... entities) {
        for (DcsEntity e : entities) {
            for (DcsResourceIdentifier altId : e.getAlternateIds()) {
                Set<DcsEntity> value;
                if (entityMap.containsKey(altId.getIdValue())) {
                    value = entityMap.get(altId.getIdValue());
                } else {
                    value = new HashSet<DcsEntity>();
                }

                value.add(e);
                entityMap.put(altId.getIdValue(), value);
            }
        }
    }

    /**
     * Places the supplied entities in the backing map, keyed by their former external references.
     *
     * @param entities the entities to add in the backing map
     */
    protected void mapFormerRefs(DcsEntity... entities) {
        for (DcsEntity e : entities) {
            if (e instanceof DcsDeliverableUnit) {
                for (String formerRef : ((DcsDeliverableUnit) e).getFormerExternalRefs()) {
                    Set<DcsEntity> value;
                    if (entityMap.containsKey(formerRef)) {
                        value = entityMap.get(formerRef);
                    } else {
                        value = new HashSet<DcsEntity>();
                    }
                    value.add(e);
                    entityMap.put(formerRef, value);
                }
            }
        }
    }
}

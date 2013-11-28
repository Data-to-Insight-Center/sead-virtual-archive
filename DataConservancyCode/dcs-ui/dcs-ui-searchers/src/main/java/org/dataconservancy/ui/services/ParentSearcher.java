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

import org.dataconservancy.model.dcs.DcsEntity;

import java.util.Collection;

/**
 * Abstraction for performing parent searches.
 */
public interface ParentSearcher {

    /**
     * Obtain the parents of the supplied entity.  The supplied {@code entityId} should identify a {@code DcsCollection}
     * or {@code DcsDeliverableUnit}, since they are the only entities that carry the 'parent' attribute in the DCS
     * data model.  The response may have {@code DcsDeliverableUnit}s and/or {@code DcsCollection}s.  Ancestors (like
     * grandparents) will not be found, only parents.
     *
     * @param entityId the DCS entity identifier to find parents of
     * @return the ancestors of the supplied entity identifier, never {@code null}
     */
    Collection<DcsEntity> getParentsOf(String entityId);

    /**
     * Similar to {@link #getParentsOf(String)}, but constrain the returned {@code Collection} to contain only classes
     * of {@code constraint} type.  The only values of {@code constraint} that make sense are
     * {@code DcsDeliverableUnit.class} and {@code DcsCollection.class}.
     *
     * @param entityId   the DCS entity identifier to find parents of
     * @param constraint the result will only contain classes of this type
     * @param <T>        the result will only contain classes of this type
     * @return the ancestors of the supplied entity identifier, never {@code null}
     */
    <T extends DcsEntity> Collection<T> getParentsOf(String entityId, Class<T> constraint);

}

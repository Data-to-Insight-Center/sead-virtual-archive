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
 * Abstraction for performing ancestry searches of the DCS archive.
 */
public interface AncestrySearcher {

    /**
     * Obtain all the ancestors of the supplied entity, optionally including the identified entity in the results.  The
     * supplied {@code entityId} can specify any entity type.  The response may include any entity type.
     *
     * @param entityId the DCS entity identifier to find ancestors of
     * @param inclusive whether or not the supplied entityId should be included in the results
     * @return the ancestors of the supplied entity identifier, never {@code null}
     */
    Collection<DcsEntity> getAncestorsOf(String entityId, boolean inclusive);
    
}

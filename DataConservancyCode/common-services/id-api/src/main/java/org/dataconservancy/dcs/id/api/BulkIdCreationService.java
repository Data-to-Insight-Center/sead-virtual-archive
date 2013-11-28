/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.dcs.id.api;

import java.util.List;

/**
 * Provides for creation of DCS identifiers in bulk.
 */
public interface BulkIdCreationService {

    /**
     * Creates {@code count} DCS identifier objects associated with the given type.  It is the intent that
     * implementations may optimize the creation of identifiers if the caller knows they will need more than one
     * Identifier created for higher-level operations of the DCS.
     * <p>
     * The value for 'type' will be persisted with the identifier, and
     * retrievable by {@link Identifier#getType()}. It is opaque to the
     * IdService implementation. By convention, identifiers representing DCS
     * entities should use the values enumerated in {@link Types}
     * </p>
     *
     * @param count the number of identifiers to create, must be a positive integer
     * @param type the type associated with the identiifer
     * @return the identifiers, in the order they were created; the size of the returned list will be equal to
     *         {@code count}
     */
    public List<Identifier> create(int count, String type);

}

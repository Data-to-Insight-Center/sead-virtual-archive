/*
 * Copyright 2013 Johns Hopkins University
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.mhf.representation.api;

/**
 * Encapsulates a set of logical rules for representing metadata attribute.
 *
 * @param <T> the type of the underlying representation object
 */
public interface MetadataRepresentation<T> {

    /**
     * An identifier for the type of representation being encapsulated by this {@code MetadataRepresentation}
     *
     * @return the type string
     */
    public String getRepresentationId();

    /**
     * Obtain the representation itself.  It should be of the type identified by {@link #getRepresentationId()}.
     *
     * @return
     */
    public T getRepresentation();

}

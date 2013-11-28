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
package org.dataconservancy.dcs.id.api;

import java.net.URL;

/**
 * Common services: identifier service. API to create and look up DCS identifier
 * objects.
 * 
 * @author Bill Steel
 * @version $Id: IdService.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public interface IdService {

    /**
     * Creates a DCS identifier object associated with the given type.
     * <p>
     * The value for 'type' will be persisted with the identifier, and
     * retrievable by {@link Identifier#getType()}. It is opaque to the
     * IdService implementation. By convention, identifiers representing DCS
     * entities should use the values enumerated in {@link Types}
     * </p>
     * 
     * @param type
     * @return Identifier
     */
    public Identifier create(String type);

    /**
     * Locates an Identifier based upon its uid.
     * 
     * @param uid
     *        identifier unique identifier
     * @return Identifier
     * @throws IdentifierNotFoundException
     */
    public Identifier fromUid(String uid) throws IdentifierNotFoundException;

    /**
     * Returns the strongly typed Identifier object corresponding to the
     * provided URL.
     * 
     * @param url
     *        Identifier URL
     * @return Identifier
     * @throws IdentifierNotFoundException
     */
    public Identifier fromUrl(URL url) throws IdentifierNotFoundException;

}

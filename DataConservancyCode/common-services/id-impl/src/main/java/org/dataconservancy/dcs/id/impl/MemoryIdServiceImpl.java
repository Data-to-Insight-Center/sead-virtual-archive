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
package org.dataconservancy.dcs.id.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;

/**
 * Implements the IdService api.
 * 
 * @author Bill Steel
 * @version $Id: MemoryIdServiceImpl.java 5805 2013-04-17 13:44:01Z emetsger $
 */
public class MemoryIdServiceImpl
        implements IdService, BulkIdCreationService {

    public MemoryIdServiceImpl() {
    };

    /**
     * Creates a DCS identifier object *
     * <p>
     * Identifiers are composed of an internal unique identifier (the uid) and
     * additional type information. This type information provides insight into
     * the entity being labeled by a given identifier. {@link Types} contains a
     * static enumeration of type values used to represent entities in the DCS
     * model. The value of 'type' is not constrained, and is in fact opaque to
     * the IDService implementation.
     * </p>
     * 
     * @param obj
     * @return Identifier
     * @throws IdentifierSyntaxException
     */
    public Identifier create(String type) {

        //type identifier with the T parameter.
        Identifier identifier = IdMint.mint(type);

        return identifier;
    }

    @Override
    public List<Identifier> create(int count, String type) {
        if (count < 1) {
            throw new IllegalArgumentException("Count must be a positive integer.");
        }

        ArrayList<Identifier> ids = new ArrayList<Identifier>(count);

        for (int i = 0; i < count; i++) {
            ids.add(this.create(type));
        }

        return ids;
    }

    /**
     * Accepts a String identifier and returns the Identifier object.
     * 
     * @param uid
     * @return Identifier
     * @throws IdentifierNotFoundException
     */
    public Identifier fromUid(String uid) throws IdentifierNotFoundException {

        return MemoryIdStoreAccess.retrieveIdentifierByUid(uid);

    }

    /**
     * Returns the Identifier object corresponding to the provided URL.
     * 
     * @param url
     * @return Identifier
     * @throws IdentifierNotFoundException
     */
    public Identifier fromUrl(URL url) throws IdentifierNotFoundException {

        return MemoryIdStoreAccess.retrieveIdentifierByUri(url);
    }

}

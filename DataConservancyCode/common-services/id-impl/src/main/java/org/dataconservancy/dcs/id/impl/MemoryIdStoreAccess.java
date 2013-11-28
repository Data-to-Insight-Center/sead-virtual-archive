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

import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.impl.idstore.MemoryIdStore;

/**
 * Data Access into the Identifier store.
 * 
 * @author Bill Steel
 * @version $Id: MemoryIdStoreAccess.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public class MemoryIdStoreAccess {

    /**
     * Retrieves a strongly typed DCS Identifier by "looking up" the Identifier
     * by the corresponding UID.
     * 
     * @param uid
     * @return Identifier
     * @throws IdentifierNotFoundException
     */
    public static Identifier retrieveIdentifierByUid(String uid)
            throws IdentifierNotFoundException {

        return MemoryIdStore.selectIdentifierByUid(uid);

    }

    /**
     * Retrieves a strongly typed DCS Identifier by "looking up" the Identifier
     * by the corresponding URI.
     * 
     * @param uri
     * @return Identifier
     * @throws IdentifierNotFoundException
     */
    public static Identifier retrieveIdentifierByUri(URL uri)
            throws IdentifierNotFoundException {

        return MemoryIdStore.selectIdentifierByUri(uri);
    }

}

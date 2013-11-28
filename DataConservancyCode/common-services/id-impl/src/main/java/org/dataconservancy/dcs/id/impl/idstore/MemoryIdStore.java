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
package org.dataconservancy.dcs.id.impl.idstore;

import java.net.URL;

import java.util.HashMap;

import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STUB implementation of an Identifier Store. Will need to be implemented
 * properly for Y1P
 * 
 * @author Bill Steel
 * @version $Id: MemoryIdStore.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public class MemoryIdStore {

    static Logger log = LoggerFactory.getLogger(MemoryIdStore.class);

    static HashMap<String, Identifier> idStoreByUID =
            new HashMap<String, Identifier>();

    static HashMap<URL, Identifier> idStoreByURI =
            new HashMap<URL, Identifier>();

    public static void insertIdentifier(Identifier identifier) {

        idStoreByUID.put(identifier.getUid(), identifier);
        idStoreByURI.put(identifier.getUrl(), identifier);

    }

    public static Identifier selectIdentifierByUid(String uid)
            throws IdentifierNotFoundException {

        log.debug("Selecting Identifier by uid from ID Store. UID: " + uid);

        if (idStoreByUID.containsKey(uid)) {
            return idStoreByUID.get(uid);
        } else
            throw new IdentifierNotFoundException();

    }

    public static Identifier selectIdentifierByUri(URL uri)
            throws IdentifierNotFoundException {

        log.info("Selecting Identifier by URI from ID Store. URI: "
                + uri.toString());

        if (idStoreByURI.containsKey(uri)) {
            return idStoreByURI.get(uri);
        } else
            throw new IdentifierNotFoundException();

    }
}

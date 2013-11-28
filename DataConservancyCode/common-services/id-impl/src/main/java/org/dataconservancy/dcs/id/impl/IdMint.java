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

import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.impl.idstore.MemoryIdStore;

/**
 * Class to mint new DCS identifier objects for DCS entities.
 * 
 * @author Bill Steel
 * @version $Id: IdMint.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public class IdMint {

    // For Y1P identifiers will be prefixed with this.
    private static final String uidPrefix = "y1p-";

    /**
     * Mints DCS Identifier object that is associated with the given type.
     * 
     * @param type
     *        String providing type information to be persisted with the
     *        identifier.
     * @return identifier
     * @throws IdentifierSyntaxException
     */
    public static Identifier mint(String type) {

        //mint the UID
        String uid = mintUid();

        Identifier identifier = new IdentifierImpl(type, uid);

        //Store the identifier
        MemoryIdStore.insertIdentifier(identifier);

        return identifier;
    }

    /**
     * Mints a uid for the Identifier.
     * 
     * @return uid
     */
    private static String mintUid() {

        return uidPrefix + UidGenerator.generateNextUID().toString();

    }

}

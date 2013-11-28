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
package org.dataconservancy.deposit.sword.server;

import org.apache.abdera.protocol.server.CollectionInfo;
import org.dataconservancy.deposit.sword.SWORDPackaging;

/**
 * Extends CollectionInfo with SWORD collection extensions
 */
public interface SWORDCollectionInfo extends CollectionInfo {

    /**
     * Get all SWORD accepted packaging formats. If empty, that implies
     * that any packaging is accepted.
     *
     * @return array of accepted SWORD packaging formats.
     */
    public SWORDPackaging[] getAcceptedPackaging();

    /**
     * Set SWORD collection policy text or URI
     *
     * @return String containing policy,
     *         or a URI to a document containing policy
     */
    String getCollectionPolicy();

    /**
     * Get SWORD collection-level treatment statement
     *
     * @return String containing statement,
     *         or a URI to a document containing statement.
     */
    public String getTreatment();

    /**
     * Get SWORD mediation supported
     *
     * @return true if mediation is supported.
     */
    public boolean getMediation();

    /**
     * URIs that dereference to SWORD service documents directing clients
     * to nested service extension
     *
     * @return array containing service doc URI strings
     */
    public String[] getServiceURIs();
}

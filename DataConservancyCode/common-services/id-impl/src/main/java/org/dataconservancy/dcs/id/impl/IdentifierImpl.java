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

import java.io.Serializable;

import java.net.URL;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic DCS Identifier implementation
 * 
 * @author Bill Steel
 * @version $Id: IdentifierImpl.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public class IdentifierImpl
        implements Identifier, Serializable {

    private static final long serialVersionUID = 1L;

    static Logger log = LoggerFactory.getLogger(IdentifierImpl.class);

    /*
     * Unique ID within DCS. For Y1P this is in the format: y1p:#
     */
    private String uid;

    /*
     * URI of the identifier. In the format: http://dataconservancy.org/<uid>
     */
    private URL url;

    private String uriPrefix = "http://dataconservancy.org/";

    /*
     * Type of DCS entity that the Identifier object is typed with.
     */
    private String type;

    /**
     * Construct an identifier using the given type ant unique identifier.
     * 
     * @param type
     *        String containing a 'type' value to persist with the identifier.
     * @param uid
     *        Unique identifier.
     * @throws IdentifierSyntaxException
     */
    public IdentifierImpl(String type, String uid) {
        this.uid = uid;
        try {
            this.url = constructUri(uid);
        } catch (Exception e) {
            throw new RuntimeException("Error creating url from uid: " + uid);
        }
        this.type = type;
        log.info("Created new Identifier object with uid: " + uid);
    }

    /*
     * Construct the URL for this identifier
     */
    private URL constructUri(String uid) throws Exception {

        return new URL(uriPrefix + uid);
    }

    /**
     * Get a unique URL corresponding to the identifier.
     * <p>
     * This url may be used as the 'public' form of the identifier. These URLs
     * should be considered potentially resolvable, depending on the nature and
     * state of the object it describes.
     * </p>
     * 
     * @return Unique, stable, possibly resolvable URL.
     */
    public URL getUrl() {

        return this.url;
    }

    /**
     * Get a unique string associated with this identifier.
     * <p>
     * Should be considered opaque, but uniquely associated with an Identifier.
     * May be used as an internal representation. See also
     * {@link IdService#fromUid(String)}.
     * </p>
     * 
     * @return
     */
    public String getUid() {
        return this.uid;
    }

    public String toString() {
        return getUid();
    }

    /*
     * The type the identifier is associated with.
     */
    public String getType() {
        return this.type;
    }

}

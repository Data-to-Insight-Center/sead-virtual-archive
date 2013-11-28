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
 * Generic DCS Identifier of type parameter <T>. Identifier is typed with the
 * class of the DCS entity it is being used to identify.
 * 
 * @author Bill Steel
 * @version $Id: Identifier.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public interface Identifier {

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
    public URL getUrl();

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
    public String getUid();

    /**
     * Return a 'type' value associated with this identifier.
     * <p>
     * Geerally, there is a convention that identifiers for DCS entities use the
     * vocabulary specified in {@link Types}. Otherwise, if this identifier is
     * not for a Dcs Entity, the value of 'type' is free to follow any other
     * convention.
     * </p>
     * 
     * @return
     */
    public String getType();

}

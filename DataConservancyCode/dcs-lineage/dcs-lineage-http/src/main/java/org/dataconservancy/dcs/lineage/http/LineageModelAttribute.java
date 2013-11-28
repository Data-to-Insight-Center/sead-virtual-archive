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
package org.dataconservancy.dcs.lineage.http;

/**
 * Attribute names used to populate the {@link org.springframework.ui.Model Model} returned from the
 * {@link LineageController}.  The {@link org.springframework.web.servlet.View View} will use these
 * attributes to retrieve objects from the {@code Model}.
 * <p/>
 * Typically the names in this {@code enum} will be used as keys like: {@code ETAG.name()}.
 */
public enum LineageModelAttribute {

    /**
     * The value of the ETag header that is used in the HTTP response.  If the response is empty (that is, the
     * query didn't match a Lineage, or constraints on the query resulted in an empty Lineage), the value
     * in the model will be {@code null}.
     */
    ETAG,

    /**
     * The value of the Last-Modified header that is used in the HTTP response.  If the response is empty (that is, the
     * query didn't match a Lineage, or constraints on the query resulted in an empty Lineage), the value in the model
     * will be {@code null}.
     */
    LASTMODIFIED,

    /**
     * The DCS entities of the Lineage, to be serialized in the response output stream.  If the HTTP request results in
     * an empty Lineage, or the Lineage is not found, then this will be an empty {@code Collection}, not {@code null}.
     */
    ENTITIES,

    /**
     * The {@code Lineage} referenced by {@link #ID}.  If the HTTP request results in an empty Lineage, then this will
     * be an empty {@code Lineage}.  If the Lineage could not be found, this will be {@code null}.
     */
    LINEAGE,

    /**
     * The value of If-Modified-Since HTTP request header, if it was provided; {@code null} otherwise.
     */
    IFMODIFIEDSINCE,

    /**
     * The value of the Accept HTTP request header, if it was provided; {@code null} otherwise.
     */
    ACCEPT,

    /**
     * The identifier of the lineage or the entity in the HTTP request.  This will be {@code null} if {@link #LINEAGE}
     * is null after a search between two entities, or if an empty lineage request is performed.
     */
    ID
}

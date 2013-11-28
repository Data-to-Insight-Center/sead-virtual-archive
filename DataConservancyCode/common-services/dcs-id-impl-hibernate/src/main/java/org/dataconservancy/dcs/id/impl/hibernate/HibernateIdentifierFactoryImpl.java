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
package org.dataconservancy.dcs.id.impl.hibernate;

import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.impl.hibernate.HibernateIdentifierFactory;
import org.dataconservancy.dcs.id.impl.hibernate.IdWrapper;
import org.dataconservancy.dcs.id.impl.hibernate.TypeInfo;

/**
 * Generate identifiers for the HibernateIdService with either the
 * lineage or entity URL prefix.
 *
 */
public class HibernateIdentifierFactoryImpl implements HibernateIdentifierFactory {
    private String urlEntityTypePrefix = "entity/";
    private String urlLineageTypePrefix = "lineage/";

    public Identifier createNewIdentifier(String urlPrefix, String typeString) {
        TypeInfo info = new TypeInfo();
        info.setType(typeString);

        return new IdWrapper(info, getTypedUrlPrefix(urlPrefix, typeString));
    }

    public Identifier createIdentifierWithExistingPersistentId(String urlPrefix, TypeInfo existingPersistentId) {
        return new IdWrapper(existingPersistentId, this.getTypedUrlPrefix(urlPrefix, existingPersistentId.getType()));
    }

    public String getUrlEntityTypePrefix() {
        return urlEntityTypePrefix;
    }

    /**
     * Set text value for the part of the url prefix that indicate the id is of an entity type
     *
     * @param urlEntityTypePrefix
     */
    public void setUrlEntityTypePrefix(String urlEntityTypePrefix) {
        this.urlEntityTypePrefix = urlEntityTypePrefix;
    }

    public String getUrlLineageTypePrefix() {
        return urlLineageTypePrefix;
    }

    /**
     * Set text value for the part of the url prefix that depends on id id of a lineage type
     *
     * @param urlLineageTypePrefix
     */
    public void setUrlLineageTypePrefix(String urlLineageTypePrefix) {
        this.urlLineageTypePrefix = urlLineageTypePrefix;
    }

    /**
     * Get the full url prefix for an id, including its type-dependent segment.
     *
     * @param idType
     * @return
     */
    protected String getTypedUrlPrefix(String urlPrefix, String idType) {
        if (idType.equals(Types.LINEAGE.getTypeName()) || idType.equals(Types.LINEAGE.name())) {
            return urlPrefix + urlLineageTypePrefix;
        }  else {
            return urlPrefix + urlEntityTypePrefix;
        }
    }
}

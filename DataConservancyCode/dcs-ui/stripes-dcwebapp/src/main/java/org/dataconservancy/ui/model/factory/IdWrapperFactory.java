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
package org.dataconservancy.ui.model.factory;

import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.impl.hibernate.HibernateIdentifierFactoryImpl;
import org.dataconservancy.dcs.id.impl.hibernate.IdWrapper;
import org.dataconservancy.dcs.id.impl.hibernate.TypeInfo;

/**
 * Generate identifiers for the HibernateIdService with UI specific URL prefixes.
 *
 */
public class IdWrapperFactory extends HibernateIdentifierFactoryImpl {
    private String urlProjectTypePrefix = "project/";
    private String urlPersonTypePrefix = "person/";
    private String urlDataSetTypePrefix = "item/";
    private String urlDataFileTypePrefix = "file/";
    private String urlCollectionTypePrefix = "collection/";
    private String urlMetadataFileTypePrefix = "file/";


    /**
     * Get the full url prefix for an id, including its type-dependent segment.
     * 
     * @param idType
     * @return urlPrefix
     */
    protected String getTypedUrlPrefix(String urlPrefix, String idType) {
        if (idType.equals(Types.PROJECT.getTypeName()) || idType.equals(Types.PROJECT.name())) {
            return urlPrefix + urlProjectTypePrefix;
        } else if (idType.equals(Types.PERSON.getTypeName()) || idType.equals(Types.PERSON.name())) {
            return urlPrefix + urlPersonTypePrefix;
        } else if (idType.equals(Types.DATA_SET.getTypeName()) || idType.equals(Types.DATA_SET.name())) {
            return urlPrefix + urlDataSetTypePrefix;
        } else if (idType.equals(Types.DATA_FILE.getTypeName()) || idType.equals(Types.DATA_FILE.name())) {
            return urlPrefix + urlDataFileTypePrefix;
        } else if (idType.equals(Types.COLLECTION.getTypeName()) || idType.equals(Types.COLLECTION.name())){
            return urlPrefix + urlCollectionTypePrefix;
        } else if (idType.equals(Types.METADATA_FILE.getTypeName()) || idType.equals(Types.METADATA_FILE.name())){
            return urlPrefix + urlMetadataFileTypePrefix;
        }  else {
            return super.getTypedUrlPrefix(urlPrefix, idType);
        }
    }
}

/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.mhf.representation.api;

/**
 * Names used for {@code AttributeSet} instances.
 * <p/>
 * Metadata Attribute considered to be CORE metadata attributes when they are one of the Dublin-Core attributes.
 * {@code AttributeSet}s containing these core metadata attributes are called core metadata attribute sets. Because each
 * of the business objects contains its own subset of the full Dublin-Core attribute set, its associated core
 * metadata attribute set is prefixed with its business object type name.
 * TODO: consider if this should be exposed at the -api level?
 * TODO: consider if this should be moved to the -representation-api module or to the -representations-impl module?
 */
public class MetadataAttributeSetName {

    /**
     * Indicates a group of Dublin-core-like metadata attributes for any business object
     */
    public static final String CORE_METADATA = "dataconservancy.org:mhf:schema:attribute-set:bo:core:1.0";

    /**
     * Indicates a group of Dublin-core-like metadata attributes for project
     */
    public static final String PROJECT_CORE_METADATA = "dataconservancy.org:mhf:schema:attribute-set:bo:project:core:1.0";

    /**
     * Indicates a group of Dublin-core-like metadata attributes for collection
     */
    public static final String COLLECTION_CORE_METADATA = "dataconservancy.org:mhf:schema:attribute-set:bo:collection:core:1.0";

    /**
     * Indicates a group of Dublin-core-like metadata attributes for data item
     */
    public static final String DATAITEM_CORE_METADATA = "dataconservancy.org:mhf:schema:attribute-set:bo:dataitem:core:1.0";

    /**
     * Indicates a group of Dublin-core-like metadata attributes for data file
     */
    public static final String DATAFILE_CORE_METADATA = "dataconservancy.org:mhf:schema:attribute-set:bo:datafile:core:1.0";

    /**
     * Indicates a group of Dublin-core-like metadata attributes for metadata file
     */
    public static final String METADATA_CORE_METADATA = "dataconservancy.org:mhf:schema:attribute-set:bo:metadatafile:core:1.0";

    /**
     * Indicates a group of system related metadata attributes
     */
    public static final String SYSTEM_METADATA = "dataconservancy.org:mhf:schema:attribute-set:bo:system:1.0";

    /**
     * Indicates a group of spatial metadata attributes
     */
    public static final String SPATIAL_METADATA = "dataconservancy.org:mhf:schema:attribute-set:spatial:1.0";

    /**
     * Indicates a group of temporal metadata attributes
     */
    public static final String TEMPORAL_METADATA = "dataconservancy.org:mhf:schema:attribute-set:temporal:1.0";

    /**
     * Indicates a group fo keyword metadata
     */
    public static final String KEYWORD_METADATA = "dataconservancy.org:mhf:schema:attribute-set:keyword:1.0";

    /**
     * Indicates a group fo free-text metadata
     */
    public static final String FREE_TEXT_METADATA = "dataconservancy.org:mhf:schema:attribute-set:freetext:1.0";

    /**
     * Indicates a group fo fulltext free-form metadata
     */
    public static final String COPY_RIGHT_METADATA = "dataconservancy.org:mhf:schema:attribute-set:copyright:1.0";

    /**
     * Indicate a group of BagIt metadata attributes
     */
    public static final String BAGIT_METADATA = "BagIt";

    /**
     * Indicate a group of Data Conservancy specific BagIt metadata attributes
     */
    public static final String BAGIT_PROFILE_DATACONS_METADATA = "BagIt-Profile-DataCons";

}

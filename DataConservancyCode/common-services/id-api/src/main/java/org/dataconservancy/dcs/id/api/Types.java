/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.id.api;

import java.lang.String;

/**
 * Enumerates type values representing DCS concepts.
 * <p>
 * Identifiers may be associated with a 'type'. By convention, identifiers that
 * refer to DCS entities should use the values enumerated here.
 * </p>
 */
public enum Types {
    /** Collection entity type */
    COLLECTION("COLLECTION"),

    /** DeliverableUnit entity type */
    DELIVERABLE_UNIT("DeliverableUnit"),

    /** Event entity type */
    EVENT("Event"),

    /** File entity type */
    FILE("File"),

    /** Manifestation enity type */
    MANIFESTATION("Manifestation"),

    /** Lineage type */
    LINEAGE("Lineage"),

    /** UI ID Types */

    /** Project type */
    PROJECT("PROJECT"),

    /** Person type */
    PERSON("PERSON"),

    /** DataSet type */
    DATA_SET("DATA_SET"),

    /** Data File type */
    DATA_FILE("DATA_FILE"),

    /**
     * Id Type associated with (deposit) package
     */
    PACKAGE("Package"),

    /**
     * Id Type associated with metadata files
     */
    METADATA_FILE("Metadata file"),

    /**
     * Id Type associated with ContactInfo
     */
    CONTACT_INFO("Contact Info");

    String typeName;

    Types(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return this.typeName;
    }
}

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
package org.seadva.registry.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DcsDBField {
    /** 
     * The name used by Solr to identify a field.
     */
    public interface DBPropertyName {

        public String dbPropertyName();
    }

    public enum EntityTypeValue {
        COLLECTION("Collection"), DELIVERABLE_UNIT("DeliverableUnit"), EVENT(
        "Event"), MANIFESTATION("Manifestation"), FILE("File");

        private final String fieldvalue;

        EntityTypeValue(String fieldvalue) {
            this.fieldvalue = fieldvalue;
        }

        public String solrValue() {
            return fieldvalue;
        }
    }

    public enum EntityField
    implements DBPropertyName {
        ID("id"), TYPE("entityType"), ANCESTRY("ancestry"), METADATA_REF("metadataRef");

        private final String fieldname;

        EntityField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }


    public enum ManifestationField
    implements DBPropertyName {
        DELIVERABLE_UNIT("deliverableunit"), TECH("manifestationTech"), DATE_CREATED("manifestationDateCreated");

        private final String fieldname;

        ManifestationField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }

    public enum ManifestationFileField
    implements DBPropertyName {
        FILE_REF("fileRef"), PATH("filePath"), DYNAMIC_MF_REL_PREFIX("mf_rel_");

        private final String fieldname;

        ManifestationFileField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }


    public enum EventField
    implements DBPropertyName {
        DATE("eventDate"), TYPE("eventType"), OUTCOME("eventOutcome"), DETAIL("eventDetail"), TARGET("eventTarget"), DYNAMIC_DATE_TYPE_PREFIX("event_date_");
        ;

        private final String fieldname;

        EventField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }

    public enum FixityField
    implements DBPropertyName {
        ALGORITHM("fixityAlgorithm"), VALUE("fixityValue");
        ;

        private final String fieldname;

        FixityField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }

    public enum FormatField
    implements DBPropertyName {
        VERSION("formatVersion"), SCHEMA("formatSchema"), NAME("formatName"), FORMAT("format");
        ;

        private final String fieldname;

        FormatField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }

    public enum FileField
    implements DBPropertyName {
        SIZE("SizeOrDuration"), NAME("fileName"), SOURCE("fileSource"), VALID("fileValid"), EXTANT("fileExtant");

        private final String fieldname;

        FileField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }

    public enum CollectionField
    implements DBPropertyName {
        PARENT("parent");

        private final String fieldname;

        CollectionField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }

    public enum DeliverableUnitField
    implements DBPropertyName {
        PARENT("parent"),
        COLLECTIONS("collection"), FORMER_REFS("former"), 
        DIGITAL_SURROGATE("digitalSurrogate"), LINEAGE("lineage");

        public final String fieldname;

        DeliverableUnitField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }

    public enum MetadataField
    implements DBPropertyName {
        TEXT("metadataText"), SEARCH_TEXT("metadataSearchText"), SCHEMA("metadataSchema");

        public final String fieldname;

        MetadataField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }

    public enum CoreMetadataField
    implements DBPropertyName {
        TITLE("title"), CREATOR("creator"), ABSTRACT("abstract"), CONTRIBUTOR("contributor"),
        SUBJECT("subject"), TYPE("type"), RIGHTS("rights");
        
        private final String fieldname;

        CoreMetadataField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }

    public enum RelationField implements DBPropertyName {
        TARGET("relatedTo"), RELATION("hasRelationship");

        private final String fieldname;

        RelationField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }
    
    public enum ResourceIdentifierField implements DBPropertyName {
        AUTHORITY("resourceAuthority"), TYPE("resourceType"), VALUE("resourceValue");
        
        private final String fieldname;

        ResourceIdentifierField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String dbPropertyName() {
            return fieldname;
        }
    }
}

/*
#
# Copyright 2013 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
*/
package org.dataconservancy.dcs.index.dcpsolr;

public class SeadSolrField extends DcsSolrField{

    public enum EntityTypeValue {
        D1LOG("D1Log");

        private final String fieldvalue;

        EntityTypeValue(String fieldvalue) {
            this.fieldvalue = fieldvalue;
        }

        public String solrValue() {
            return fieldvalue;
        }
    }

    public enum EntityField
    implements SolrName {
        ABSTRACT("abstract"),PUBDATE("pubdate"),ANCESTRY("ancestry"),IMMEDIATEANCESTRY("immediateancestry"),
        PARENTDU("parentdu"), MDUPDATE_DATE("metadataUpdateDate");

        private final String fieldname;

        EntityField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum EventField
    implements SolrName {
        DATE("eventDate"), TYPE("eventType"), OUTCOME("eventOutcome"), DETAIL("eventDetail"), TARGET("eventTarget"), DYNAMIC_DATE_TYPE_PREFIX("event_date_");
        ;

        private final String fieldname;

        EventField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum DetailLogField
            implements SolrName {
        IPADDRESS("eventIpAddress"), USERAGENT("eventUserAgent"), SUBJECT("eventSubject"), NODEIDENTIFIER("eventNodeIdentifier");

        private final String fieldname;

        DetailLogField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum FixityField
    implements SolrName {
        ALGORITHM("fixityAlgorithm"), VALUE("fixityValue");
        ;

        private final String fieldname;

        FixityField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum FileField
    implements SolrName {
        DEPOSITDATE("event_date_archive");

        private final String fieldname;

        FileField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum CollectionField
    implements SolrName {
        PARENT("parent");

        private final String fieldname;

        CollectionField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum DeliverableUnitField
    implements SolrName {
        PARENT("parent"),  LOCATION("location"),
        COLLECTIONS("collection"), FORMER_REFS("former"), 
        DIGITAL_SURROGATE("digitalSurrogate"),
        SIZEBYTES("fileSize"), FILENO("fileNo");

        public final String fieldname;

        DeliverableUnitField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum CoreMetadataField
    implements SolrName {
        TITLE("title"), CREATOR("creator"),CONTACT("contact"),
        SUBJECT("subject"), TYPE("type"), RIGHTS("rights");
        
        private final String fieldname;

        CoreMetadataField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum DataLocationField implements SolrName {
        NAME("dataLocationName"), TYPE("dataLocationType"), LOCATION("dataLocationValue");

        private final String fieldname;

        DataLocationField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum PrimaryDataLocationField implements SolrName {
        NAME("primaryDataLocationName"), TYPE("primaryDataLocationType"), LOCATION("primaryDataLocationValue");

        private final String fieldname;

        PrimaryDataLocationField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum CreatorField implements SolrName {
        NAME("creator"), CREATORID("creatorId"), CREATORIDTYPE("creatorIdType");

        private final String fieldname;

        CreatorField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum SubmitterField implements SolrName {
        NAME("submitter"), SUBMITTERID("submitterId"), SUBMITTERIDTYPE("submitterIdType");

        private final String fieldname;

        SubmitterField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }

    public enum FgdcField implements SolrName {
        LOCATION("fgdc.location"), ORIGIN("fgdc.origin"), PUBLISHER("fgdc.publisher"), ABSTRACT("fgdc.abstract")
        ,WESTLON ("fgdc.westbc"),EASTLON ("fgdc.eastbc"), NORTHLAT("fgdc.northbc"), SOUTHLAT("fgdc.southbc");

        private final String fieldname;

        FgdcField(String fieldname) {
            this.fieldname = fieldname;
        }

        public String solrName() {
            return fieldname;
        }
    }
}

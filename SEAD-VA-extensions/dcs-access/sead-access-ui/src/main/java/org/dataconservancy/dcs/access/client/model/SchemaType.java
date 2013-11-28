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
package org.dataconservancy.dcs.access.client.model;

// TODO copied from solr impl...
//TODO this is formatted incorrectly because eclipse refuses to format...

public class SchemaType {
    public enum Name {
        FGDC("FGDC"), ISO19115("ISO19115"), DDI("DDI"), HTML("HTML");

        private final String fieldvalue;


        Name(String fieldvalue) {
            this.fieldvalue = fieldvalue;
        }

        public String nameValue() {
            return fieldvalue;
        }
    }
}

/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.archive;

/**
 *Enumerated types for Archive storage types in SEAD VA
 */
public final class ArchiveEnum {

        public static enum ArchiveType {

        CLOUD("cloud"), REPOSITORY("Repository");

        private String text;

        ArchiveType(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public ArchiveType fromString(String text) {
            if (text != null) {
                for (ArchiveType archiveType : ArchiveType.values()) {
                    if (text.equalsIgnoreCase(archiveType.text)) {
                        return archiveType;
                    }
                }
            }
            return null;
        }

    }

    public static enum Archive {

        AMAZON_S3("Amazon S3", ArchiveType.CLOUD),
        AMAZON_GLACIER("Amazon Glacier", ArchiveType.CLOUD),
        SDA("IU SDA", ArchiveType.CLOUD),
        UIUC_IDEALS("Ideals", ArchiveType.REPOSITORY),
        IU_SCHOLARWORKS("IU Scholarworks", ArchiveType.REPOSITORY),
        LOCAL_DSPACE("DSpace local instance", ArchiveType.REPOSITORY);

        private final String archive;
        private final ArchiveType type;

        private Archive(String archive, ArchiveType type) {
            this.archive = archive;
            this.type = type;
        }


        public String getArchive() {
            return archive;
        }
        public ArchiveType getType(){
            return type;
        }

        public static Archive fromString(String text) {
            if (text != null) {
                for (Archive archiveObj : Archive.values()) {
                    if (text.equalsIgnoreCase(archiveObj.archive)) {
                        return archiveObj;
                    }
                }
            }
            return null;
        }
    }
}

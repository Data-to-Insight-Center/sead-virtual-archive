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
package org.dataconservancy.archive.api;

/**
 * Types of DCS entities that may exist in the <code>ArchiveStore</code>.
 *
 * @see <a href="https://wiki.library.jhu.edu/x/DoKx">DCP Serialization Format</a>
 */
public enum EntityType {

    COLLECTION ("Collection"),
    DELIVERABLE_UNIT ("DeliverableUnit"),
    MANIFESTATION ("Manifestation"),
    FILE ("File"),
    EVENT ("Event");
    
    private final String string;
    
    
    private EntityType(String val) {
       string = val;
    }
    
    @Override
    public String toString() {
        return string;
    }
}

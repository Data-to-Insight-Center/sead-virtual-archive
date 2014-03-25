/*
 * Copyright 2014 The Trustees of Indiana University
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

package org.seadva.registry.api;

/**
 * Resource Types
 */
public enum ResourceType {

    FILE("file"), AGGREGATION("aggregation"), REPOSITORY("repository"), CONTAINER("container"), PERSON("person"), RULE("rule");

    private String text;

    ResourceType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static ResourceType fromString(String text) {
        if (text != null) {
            for (ResourceType b : ResourceType.values()) {
                if (text.equalsIgnoreCase(b.text)) {
                    return b;
                }
            }
        }
        return null;
    }
}

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

package org.dataconservancy.dcs.id.api;


import java.util.Map;

/**
 * Metadata types
 */
public interface IdMetadata {


    public Map<Metadata,String> getMetadata();

    public void setMetadata(Map<Metadata, String> mdata);

    public enum Metadata{

        TITLE("title"),CREATOR("creator"),TARGET("target"),PUBLISHER("publisher"),PUBDATE("pubdate");

        private final String prefix;

        Metadata(String prefix) {
            this.prefix = prefix;
        }


    }

}

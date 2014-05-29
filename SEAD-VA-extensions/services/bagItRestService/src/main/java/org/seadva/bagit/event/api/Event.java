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
package org.seadva.bagit.event.api;

/**
 * Events in generation of Bags
 */
public enum Event {
    UNZIP_BAG,
    PARSE_FETCH,
    PARSE_SIP,
    PARSE_DIRECTORY,
    PARSE_ACR_COLLECTION,
    GENERATE_ORE,
    GENERATE_FGDC,
    GENERATE_FETCH,
    GENERATE_MANIFEST,
    ZIP_BAG,
    GENERATE_SIP,
    GENERATE_DATA_DIR,
    TAR_BAG,
    UNTAR_BAG,
    GENERATE_BAGITTXT,
    GENERATE_BAGINFO,
    GENERATE_TAGMANIFEST,
    GENERATE_DPNTAGFILE,
    GENERATE_DPNORE,
    GENERATE_DPNSIP,
    ERROR
}

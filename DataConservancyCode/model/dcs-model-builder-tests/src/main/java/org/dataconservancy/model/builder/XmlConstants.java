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
package org.dataconservancy.model.builder;

/**
 * Contains XML string constants used to wrap DCS entities in DCP XML.
 */
class XmlConstants {
    final static String DOC_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    final static String E_DCP_OPEN = DOC_START + "<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\">\n";
    final static String E_DCP_CLOSE = "</dcp>";
    final static String DCP_WRAPPER = E_DCP_OPEN + "%s" + E_DCP_CLOSE;
    final static String E_FILES_OPEN = "<Files>\n";
    final static String E_FILES_CLOSE = "</Files>";
    final static String FILES_WRAPPER = E_FILES_OPEN + "%s" + E_FILES_CLOSE;
    final static String E_MANIFESTATIONS_OPEN = "<Manifestations>\n";
    final static String E_MANIFESTATIONS_CLOSE = "</Manifestations>";
    final static String MANIFESTATIONS_WRAPPER = E_MANIFESTATIONS_OPEN + "%s" + E_MANIFESTATIONS_CLOSE;
    final static String E_DU_OPEN = "<DeliverableUnits>\n";
    final static String E_DU_CLOSE = "</DeliverableUnits>";
    final static String DU_WRAPPER = E_DU_OPEN + "%s" + E_DU_CLOSE;
}

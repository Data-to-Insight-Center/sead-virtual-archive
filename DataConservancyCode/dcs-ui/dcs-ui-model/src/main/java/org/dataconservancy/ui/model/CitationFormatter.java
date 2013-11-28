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
package org.dataconservancy.ui.model;

import org.dataconservancy.ui.model.Citation;


public interface CitationFormatter {
    /**
     * Default values to be put on the citation string if other values are not supplied
     */
    public static final String DEFAULT_CREATOR = "[creator(s)]";
    public static final String DEFAULT_LOCATOR = "[locator]";
    public static final String DEFAULT_TITLE = "[title]";
    public static final String DEFAULT_VERSION = "[Version]";

    /**
     * Supported Citation Formats.
     */
    public static enum CitationFormat{
        /**
         * Format for Earth Science Information Partners
         */
        ESIP

    }


    /**
     * outputs a citation string with html formatting
     * @param citation
     */
    public String formatHtml(Citation citation);
}

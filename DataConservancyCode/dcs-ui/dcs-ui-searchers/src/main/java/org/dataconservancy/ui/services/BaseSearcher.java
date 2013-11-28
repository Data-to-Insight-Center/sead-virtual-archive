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
package org.dataconservancy.ui.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base {@code Searcher} implementation.  Provides a {@code Logger} instance and Strings for reporting errors.
 */
public abstract class BaseSearcher {

    /**
     * Logger instance.
     */
    final Logger log = LoggerFactory.getLogger(this.getClass());


    /**
     * Error string when a search cannot be performed.
     * Parameters: original search query, specific error message (e.g. Exception.getMessage())
     */
    final static String ERR_PERFORMING_SEARCH = "Error performing search query [%s]: %s";
}

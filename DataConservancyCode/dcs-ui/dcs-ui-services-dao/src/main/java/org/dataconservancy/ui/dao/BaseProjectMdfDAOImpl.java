/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.ui.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Base DAO impl for persisting Project and MetadataFile relationships.
 */
public abstract class BaseProjectMdfDAOImpl {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Throws an IllegalArgumentException if the supplied string is null or empty.
     *
     * @param id
     */
    void checkId(String id) {
        if (id == null || id.trim().length() == 0) {
            throw new IllegalArgumentException("An identifier was null or empty.");
        }
    }

    /**
     * Throws an IllegalArgumentException if any of the supplied strings is null or empty.
     *
     * @param ids
     */
    void checkId(List<String> ids) {
        for (String id : ids) {
            checkId(id);
        }
    }
}

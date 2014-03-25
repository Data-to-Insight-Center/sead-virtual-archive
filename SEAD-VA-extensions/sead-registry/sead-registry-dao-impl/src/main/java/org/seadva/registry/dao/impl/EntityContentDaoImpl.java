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

package org.seadva.registry.dao.impl;

import org.seadva.registry.dao.EntityContentDao;

/**
 * Can be implemented by any implementation (eg:db ) to manipulate Content table
 */
public abstract class EntityContentDaoImpl {

    static String tableName = "entity_content";

    public abstract boolean insertEntityContent(EntityContentDao entityContentDao, String entityId) throws Exception;

    public abstract EntityContentDao getEntityContentForEntity(String entityId) throws Exception;
}

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

import org.seadva.registry.dao.RelationDao;

import java.util.List;

/**
 * Manipulate relation table
 */
public abstract class RelationDaoImpl {

    static String tableName = "relation";

    public abstract boolean insertRelation(RelationDao relationDao) throws Exception ;
    public abstract List<RelationDao> getRelationForEntity(String entityId) throws Exception;
}

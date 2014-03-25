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

import org.seadva.registry.dao.AggregationDao;

import java.util.List;

/**
 * Can be implemented by any implementation (eg:db ) to manipulate Aggregation table
 */
public abstract class AggregationDaoImpl {

    static String tableName = "aggregation";

    public abstract boolean insertAggregation(AggregationDao aggregationDao) throws Exception;

    public abstract List<AggregationDao> getAggregationForParent(String parentId) throws Exception;

    public abstract List<AggregationDao> getAggregationForChild(String childId) throws Exception;
}

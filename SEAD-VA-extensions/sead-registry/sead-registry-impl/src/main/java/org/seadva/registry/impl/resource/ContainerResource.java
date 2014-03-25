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

package org.seadva.registry.impl.resource;

import org.seadva.registry.api.Resource;
import org.seadva.registry.dao.*;

import java.util.List;
import java.util.Map;

/**
 * Container resource
 */
public class ContainerResource implements Resource {

    public EntityDao getEntity() {
        return entity;
    }

    public void setEntity(EntityDao entity) {
        this.entity = entity;
    }

    public Map<String,List<EntityTypeDao>> getEntityType() {
        return entityType;
    }

    public void setEntityType(Map<String,List<EntityTypeDao>> entityType) {
        this.entityType = entityType;
    }

    public Map<String,List<PropertyDao>> getProperties() {
        return properties;
    }

    public void setProperties(Map<String,List<PropertyDao>> properties) {
        this.properties = properties;
    }

    public Map<String,List<AggregationDao>> getAggregations() {
        return aggregations;
    }

    public void setAggregations(Map<String,List<AggregationDao>> aggregations) {
        this.aggregations = aggregations;
    }

    public Map<String,List<RelationDao>> getRelations() {
        return relations;
    }

    public void setRelations(Map<String,List<RelationDao>> relations) {
        this.relations = relations;
    }

    public Map<String,List<EntityDao>> getChildEntities() {
        return childEntities;
    }

    public void setChildEntities(Map<String,List<EntityDao>> childEntities) {
        this.childEntities = childEntities;
    }

    private EntityDao entity;
    private Map<String,List<EntityTypeDao>> entityType;
    private Map<String,List<PropertyDao>> properties;
    private Map<String,List<AggregationDao>> aggregations;
    private Map<String,List<RelationDao>> relations;
    private Map<String,List<EntityDao>> childEntities;

    public ContainerResource(){}

}
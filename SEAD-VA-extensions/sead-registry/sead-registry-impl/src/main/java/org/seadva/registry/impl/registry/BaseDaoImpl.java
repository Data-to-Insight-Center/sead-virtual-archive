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

package org.seadva.registry.impl.registry;

import org.seadva.registry.dao.impl.*;
import org.springframework.beans.factory.annotation.Required;

/**
 * Sets registry db implementations
 */
public class BaseDaoImpl {
    EntityDaoImpl entityDaoImpl;
    EntityTypeDaoImpl entityTypeDaoImpl;
    AggregationDaoImpl aggregationDaoImpl;
    RelationDaoImpl relationDaoImpl;
    PropertyDaoImpl propertyDaoImpl;

    @Required
    public void setEntityDaoImpl(EntityDaoImpl entityDaoImpl){
        this.entityDaoImpl = entityDaoImpl;
    }

    @Required
    public void setEntityTypeDaoImpl(EntityTypeDaoImpl entityTypeDaoImpl){
        this.entityTypeDaoImpl = entityTypeDaoImpl;
    }

    @Required
    public void setAggregationDaoImpl(AggregationDaoImpl aggregationDaoImpl){
        this.aggregationDaoImpl = aggregationDaoImpl;
    }

    @Required
    public void setRelationDaoImpl(RelationDaoImpl relationDaoImpl){
        this.relationDaoImpl = relationDaoImpl;
    }

    @Required
    public void setPropertyDaoImpl(PropertyDaoImpl propertyDaoImpl){
        this.propertyDaoImpl = propertyDaoImpl;
    }
}

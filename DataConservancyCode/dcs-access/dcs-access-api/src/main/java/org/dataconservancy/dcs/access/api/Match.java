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
package org.dataconservancy.dcs.access.api;

import org.dataconservancy.model.dcs.DcsEntity;

/**
 * See {@link org.dataconservancy.dcs.query.api.QueryMatch} 
 *
 */
@Deprecated
public class Match {

    private final DcsEntity entity;

    private final String context;

    public Match(DcsEntity entity, String context) {
        this.entity = entity;
        this.context = context;
    }

    /**
     * @return the entity
     */
    public DcsEntity getEntity() {
        return entity;
    }

    /**
     * @return the context
     */
    public String getContext() {
        return context;
    }

    public String toString() {
        return "{" + entity + ", " + (context == null ? "" : context) + "}";
    }
}

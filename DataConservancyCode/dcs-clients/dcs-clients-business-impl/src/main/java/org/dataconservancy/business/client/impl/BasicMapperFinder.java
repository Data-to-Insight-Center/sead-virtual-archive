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

package org.dataconservancy.business.client.impl;

import java.util.List;
import java.util.Map;

import org.dataconservancy.business.client.BusinessObject;
import org.dataconservancy.business.client.Mapper;

/** Finds Mappers based upon a pre-configured table. */
public class BasicMapperFinder<A>
        implements MapperFinder<A> {
    
    @SuppressWarnings("rawtypes")
    private Map mappers;
    
    public void setMappers(Map<String, List<Mapper<? extends BusinessObject,A>>> m) {
        mappers = m;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <B extends BusinessObject> List<Mapper<B, A>> findMapper(Class<B> businessObjectClass) {
        
        return (List<Mapper<B, A>>) mappers.get(businessObjectClass.getName());
    }

}

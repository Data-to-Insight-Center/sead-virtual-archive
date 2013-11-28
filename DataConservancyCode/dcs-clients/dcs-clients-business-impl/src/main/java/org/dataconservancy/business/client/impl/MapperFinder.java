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

import org.dataconservancy.business.client.BusinessObject;
import org.dataconservancy.business.client.Mapper;

/** Finds Mapper implementations that correspond to business object types */
public interface MapperFinder<A> {

    /**
     * Find Mapper(s) that map the given business object class to archival form.
     * <p>
     * The list of mappers are implementations that are theoretically capable of
     * converting a business object of the given class into archival form. These
     * Mappers, however, may use different Profiles, creating different (and
     * possibly incompatible) archival representations. Safely picking the
     * correct Mapper for a given business object instance involves
     * introspecting into the business object instance, and picking the correct
     * mapper from the list based upon matching profile.
     * </p>
     * 
     * @param businessObjectClass
     *        Class of business object handled by the mapper
     * @param <B>
     *        Specific business object class.
     * @param <A>
     *        Archival class.
     * @return List of Mappers that transform business objects of the given
     *         class into archival form.
     */
    public <B extends BusinessObject> List<Mapper<B, A>> findMapper(Class<B> businessObjectClass);
}

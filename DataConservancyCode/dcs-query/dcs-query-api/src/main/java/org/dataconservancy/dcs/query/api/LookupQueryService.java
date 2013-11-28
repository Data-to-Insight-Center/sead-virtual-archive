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
package org.dataconservancy.dcs.query.api;

/**
 * Extends the {@link QueryService} interface by allowing identified entries to be looked up and
 * returned.
 *
 * @param <T> the domain object
 */
public interface LookupQueryService<T> extends QueryService<T> {

    /**
     * Obtain the domain object by its identifier.
     *
     * @param id the identifier
     * @return the domain object, or null if it cannot be found
     * @throws QueryServiceException if an error occurs during search
     */
    public T lookup(String id) throws QueryServiceException;

}

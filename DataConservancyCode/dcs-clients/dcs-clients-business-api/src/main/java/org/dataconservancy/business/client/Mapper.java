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

package org.dataconservancy.business.client;

import org.dataconservancy.profile.api.Profile;

/** Maps business objects to their archival form and back. */
public interface Mapper<B extends BusinessObject, A> {

    /**
     * Map a BusinessObject to its archival representation.
     * 
     * @param businessObject
     *        instance if a business object.
     * @return Archival representation of the business object.
     */
    public A toArchivalForm(B businessObject);

    /**
     * Create a business object from its archival form.
     * 
     * @param archivalObject
     *        Archival representation of the business object.
     * @return BusinessObject insstance
     */
    public B fromArchivaForm(A archivalObject);
    
    public Profile<A> getProfile();
}

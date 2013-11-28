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

package org.dataconservancy.ui.dao;

import org.dataconservancy.ui.model.PasswordResetRequest;

import java.util.List;

/**  
 * Responsible for CRUD operations on an underlying persistence store for {@link PasswordResetRequest} objects.  
 * PasswordResetRequest objects are keyed by URI.
 */
public interface PasswordResetRequestDAO {

    /**
     * Store a new PasswordResetRequest.
     *
     * @param prr the PasswordResetRequest
     */
    public void add(PasswordResetRequest prr);

    /**
     * List all PasswordResetRequests.
     *
     * @return list of PasswordResetRequests
     */
    public List<PasswordResetRequest> list();

    /**
     * Obtain the identified PasswordResetRequest.
     *
     * @param id the id of the PasswordResetRequest
     * @return the PasswordResetRequest
     */
    public PasswordResetRequest get(String id);

    /**
     * Delete a PasswordResetRequest
     *
     * @param id the URL of the PasswordResetRequest
     */
    public void delete(String id);
}

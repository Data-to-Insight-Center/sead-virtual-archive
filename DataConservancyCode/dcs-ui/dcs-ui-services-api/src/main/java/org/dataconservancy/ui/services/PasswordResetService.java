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

package org.dataconservancy.ui.services;

import org.dataconservancy.ui.exceptions.PasswordResetServiceException;
import org.dataconservancy.ui.model.PasswordResetRequest;
import org.dataconservancy.ui.model.Person;

import java.util.List;

/**
 * This service manages {@code PasswordResetRequest}s.
 */
public interface PasswordResetService {

    /**
     * Add a {@code PasswordResetRequest}. Will not create a new request if there is already a
     * valid (unexpired) request in the system.
     * @param person the {@code Person} creating the request
     * @return the created {@code PasswordResetRequest}; null if the request was not created.
     */
    public PasswordResetRequest create(Person person) throws PasswordResetServiceException;

    /**
     * Retrieve an active (unexpired) {@code PasswordResetRequest}
     * @param id the id of the {@code PasswordResetRequest}
     */
    public PasswordResetRequest getActiveRequest(String id);

    /**
     * Remove a {@code PasswordResetRequest}
     * @param id the id of the {@code PasswordResetRequest} to be removed
     */
    public void remove(String id);

    /**
     * Remove all {@code PasswordResetRequest}s which have expired.
     */
    public void removeExpiredRequests();

    /**
     *  Find all {@code PasswordResetRequest}s in the system
     * @return the List of all  {@code PasswordResetRequest}s
     */
    public List<PasswordResetRequest> findAllRequests();
    
    public void setPasswordResetRequestWindow(int hours);
    
    public int getPasswordResetRequestWindow();
}

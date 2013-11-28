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
package org.dataconservancy.ui.model;

/**
 * Represents the user roles this application can reason about.
 */
public enum Role {

    /**
     * The user is in an administrative role.  This user is allowed to approve or dis-approve registrations.
     */
    ROLE_ADMIN,

    /**
     * This represents a user who has privileges to login to the system.  That is, the user has registered and
     * the registration has been approved.
     */
    ROLE_USER
    
}

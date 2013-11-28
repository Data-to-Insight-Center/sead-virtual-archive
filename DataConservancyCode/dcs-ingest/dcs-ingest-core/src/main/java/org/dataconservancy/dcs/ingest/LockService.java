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

package org.dataconservancy.dcs.ingest;

/**
 * Ingest framework service for managing locks.
 * <p>
 * Creates named {@link LockManager} instances, or retrieves existing ones with
 * a matching ID.
 * </p>
 */
public interface LockService {

    /**
     * Obtain a new or existing lock manager with the given id.
     * 
     * @param id
     *        Arbitrary name/label for a given {@link LockManager}
     * @return A new or existing {@link LockManager}.
     */
    public LockManager getLockManager(String id);

    /** Determine if an existing LockManager with a given identifier is defined */
    public boolean hasLockManager(String id);
}

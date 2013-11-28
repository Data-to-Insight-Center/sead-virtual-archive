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
package org.dataconservancy.ui.dao;

import java.util.List;
import org.dataconservancy.ui.model.Package;

/**
 * Responsible for CRUD operations on an underlying persistence store for {@link Package} objects.
 */
public interface PackageDAO {
    
    /**
     * Obtain the {@code Package} identified by the supplied package object id.  The behavior of this method is
     * undefined when the {@code Package} is not found, but clients of this interface should be prepared to
     * handle {@code null} responses.
     *
     * @param packageId the business id identifying the {@code Package}
     * @return the {@code Package}, if it exists.
     */
	public Package selectPackage(String packageId);


    /**
     * Obtain all the {@code Package}s known to the underlying persistence store.  Despite the fact the
     * return is a {@code List}, there is no guaranteed ordering of the contained objects.
     *
     * @return a {@code List} of all {@code Package}s, in no guaranteed order.
     */
    public List<Package> selectPackage();

    /**
     * Add a {@code Package} to the underlying persistence store.  Behavior is undefined if the {@code Package} already
     * exists.  The supplied {@code Package} must have a not-null and non-empty business id, which will be used as
     * a unique identifier.
     *
     * @param dataPackage the {@code Package} to add
     */
    public void insertPackage(Package dataPackage);

    /**
     * Deletes the {@code Package} or {@code Package}s from the underlying persistence store.  The
     * behavior of this method is undefined if the package does not exist.
     *
     * @param packageId the email address identifying the user to delete
     */
    public void deletePackage(String packageId);

    /**
     * Update the persistence store for the supplied {@code Package}.  The behavior of this method is undefined if the
     * package does not exist.
     *
     * @param dataPackage the {@code Package} to update
     */
    public void updatePackage(Package dataPackage);

}

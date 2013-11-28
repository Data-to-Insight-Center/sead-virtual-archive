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

package org.dataconservancy.ui.services;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * {@code CollectionBizService} provides business operations on {@link Collection}. It is used by {@link org.dataconservancy.ui.api.CollectionController}
 * and several Action Beans to operate on {@link Collection} objects.
 */
public interface CollectionBizService {

    /**
     * Given an existing {@link org.dataconservancy.ui.model.Collection} update the collection with matching business id.
     *  The given user's credentials will be used to authenticate and authorize the request.
     *
     * @param collection
     * @param user
     * @return id of the updated collection.
     * @throws BizPolicyException
     * @throws BizInternalException
     */
    String updateCollection(Collection collection, Person user)
            throws BizPolicyException, BizInternalException;

    /**
     * Create a new {@link org.dataconservancy.ui.model.Collection}
     *
     * @param collection the collection to be created. the collection's id is expected to have already been filled, while
     *                   Collection's depositorId and depositDate will be set in this method.
     * @param user the user who is attempting to create the collection
     * @return id of the created Collection
     *
     * @throws BizPolicyException when there is a policy violation encounter in processing the Collection creation (such
     * as user does not have permission to create collection)
     * @throws BizInternalException when there are errors in creating the collection.
     */
    String createCollection(Collection collection, Person user)
            throws BizPolicyException, BizInternalException;

    /**
     * Given a {@link org.dataconservancy.ui.model.Person}, find list of {@link org.dataconservancy.ui.model.Collection}s that person can retrieve.
     * @param user
     * @throws BizPolicyException, BizInternalException
     * @return a {@code Set} of {@code Collection}s that can be viewed by the {@link org.dataconservancy.ui.model.Person}. May be empty but never {@code null}
     */
    Set<Collection> findByUser(Person user) throws BizPolicyException, BizInternalException;

    /**
     * Attempt to retrieve a collection by id as a user. 
     *
     * @param id
     * @param user The user must be an instance admin or an administrator of the containing project, or a depositor to the collection
     * @throws BizPolicyException If the user doesn't have the proper permissions to retrieve the collection.
     * @return null if a collection with the given id does not exist
     */
    Collection getCollection(String id, Person user) throws BizPolicyException, BizInternalException;
    
    /**
     * Attempt to retrieve a collection by id with no user constraints. 
     *
     * @param id
     * @throws BizPolicyException If the user doesn't have the proper permissions to retrieve the collection.
     * @return null if a collection with the given id does not exist
     */
    Collection getCollection(String id) throws BizPolicyException, BizInternalException;

    /**
     * Given a business id of a {@link org.dataconservancy.ui.model.Collection}, returns its last modified date
     * @param id
     * @throws RelationshipConstraintException
     */
    DateTime getLastModifiedDate(String id) throws RelationshipConstraintException;


    /**
     * Given business id of a {@link org.dataconservancy.ui.model.Collection}, return the number of
     * {@link org.dataconservancy.ui.model.DataItem} associated with it. This method will poll the archive once before checking
     * the count of data items for the collection.
     */
    long retrieveDataItemCount(String id) throws RelationshipException, ArchiveServiceException;

    /**
     * Given a list of business ids of  {@link org.dataconservancy.ui.model.Collection}, return a HashMap of the
     * collection business ids and the number of {@link org.dataconservancy.ui.model.DataItem} associated with each of these ids.
     * This method will poll the archive once before checking the count of data items for the collection.
     * @param collectionIdList
     * @throws RelationshipException
     * @throws ArchiveServiceException
     */
    HashMap<String, Long> retrieveDataItemCountForList(List<String> collectionIdList) throws RelationshipException, ArchiveServiceException;
}

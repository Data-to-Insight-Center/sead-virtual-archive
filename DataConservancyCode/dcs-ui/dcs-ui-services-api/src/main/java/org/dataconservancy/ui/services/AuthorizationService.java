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

import org.apache.http.MethodNotSupportedException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;

/**
 * Provides methods to determine user's permissions to different activities
 */
public interface AuthorizationService {

    //-------------------------------------------------------------------
    //   PROJECT PERMISSIONS
    //-------------------------------------------------------------------
    /**
     * Given a user, determine whether the user has the permission to create a new project
     * in the system.
     *
     * @param user - user whose permission is to be checked.
     * @return <code>true</code> if user has the permission to create an object of said type
     * @return <code>false</code> if use does not have the permission to create an object of said type
     */
    public boolean canCreateProject(Person user);

    /**
     * Given a user, determine whether the user has the permission to read a project.
     *
     * @param user - user whose permission is to be checked.
     * @return true if {@code user} can read {@code project}
     */
    public boolean canReadProject(Person user, Project project);
    
    /**
     * Given a user and a project, determine whether the user is can update a project
     *
     * @param user - user whose permission is o be checked
     * @param project - project to be modified
     * @return <code>true</code> if the user has the permission to modify the object
     * @return <code>false</code> if the user does not have the permission to modify the object
     */
    public boolean canUpdateProject(Person user, Project project);
    
    /**
     * Given a user and a project, determine whether the user has the permissions to view a projects collections
     *
     * @param user - user whose permission is o be checked
     * @param project - project whose collections are being retrieved
     * @return <code>true</code> if the user has the permission to view the collections of the project
     * @return <code>false</code> if the user does not have the permission to view the collections of the project
     */
    public boolean canRetrieveProjectCollections(Person user, Project project);

    //-------------------------------------------------------------------
    //    COLLECTION PERMISSIONS
    //-------------------------------------------------------------------
    /**
     * Given a user, determine whether the user has the permission to create a new Collection in the provided Project.
     *
     * @param user - user whose permission is to be checked.
     * @return <code>true</code> if user has the permission to create an object of said type
     * @return <code>false</code> if use does not have the permission to create an object of said type
     */
    public boolean canCreateCollection(Person user, Project project);

    /**
     * Given a user, determine whether the user has the permission to create a new Collection as a sub-collection of the
     * provided Collection.
     *
     *
     * @param user - user whose permission is to be checked.
     * @return <code>true</code> if user has the permission to create an object of said type
     * @return <code>false</code> if use does not have the permission to create an object of said type
     */
    public boolean canCreateCollection(Person user, Collection parentCollection) throws RelationshipConstraintException;

    /**
     * Given a user and a Collection, determine whether the user has permissions to retrieve the Collection
     * @param user
     * @param collection
     *      * @return true if {@code user} can retrieve {@code collection}
     */
    public boolean canRetrieveCollection(Person user, Collection collection);

    /**
     * Given a user and a collection, determine whether the user can update the collection
     *
     * @param user - user whose permission is to be checked
     * @param collection - collection to be modified
     * @return <code>true</code> if the user has the permission to modify the object
     * @return <code>false</code> if the user does not have the permission to modify the object
     */
    public boolean canUpdateCollection(Person user, Collection collection) throws RelationshipConstraintException;

    /**
     * Given a user, determine whether the user has the permissions to view the list of all collections
     * @param user
     * @return True if the user can view the list of the collections, false if the user can't view the list of collections
     */
    public boolean canViewCollectionList(Person user);

    /**
     *  Given a user and a dataFile, determines whether the user has permission to retrieve the data file
     * @param user
     * @param dataFile
     * @throws RelationshipConstraintException
     * @return true if {@code user} can retrieve {@code dataFile}
     */
    public boolean canRetrieveDataFile(Person user, DataFile dataFile) throws RelationshipConstraintException;

    //----------------------------------------------------------------
    //    DATAITEM/DATASET PERMISSIONS
    //----------------------------------------------------------------
    /**
     * Given a user and a Collection determine whether the user has the right to deposit data into the Collection
     */
    public boolean canDepositToCollection(Person user, Collection collection) throws RelationshipConstraintException;
    /**
     * Given a user and a DataItem, determine whether the user has permissions to retrieve the DataItem.
     *
     * @param user the user who wants to access {@code dataSet}
     * @param dataItem the DataItem access is being requested for
     * @return true if {@code user} can access {@code dataSet}
     */
    public boolean canRetrieveDataSet(Person user, DataItem dataItem);

    //----------------------------------------------------------------
    //   PERMISSIONS TO GRANT USER PERMISSIONS
    //----------------------------------------------------------------
     /**
     * Given the current user, determine whether a specified user can be removed from the specified collection as a depositor.
     * @param currentUser The currently authenticated user.
     * @param userToRemove The user to determine if the authenticated user has permissions to remove.
     * @param collection The collection the user is going to be removed from
     * @return True, if the currentUser has permission to remove the specified user from the collection, false otherwise.
     */
    public boolean canRemoveDepositor(Person currentUser, Person userToRemove, Collection collection) throws RelationshipConstraintException;
    
    /**
     *   
     * Given the current user, determine whether a specified user can be removed from the specified project as an admin.
     * @param currentUser The currently authenticated user.
     * @param userToRemove The user to determine if they can be removed.
     * @param project The project to remove the user from.
     * @return True, if the current user has permissions to remove the other user from the project, false otherwise.
     * @throws RelationshipConstraintException
     */
    public boolean canRemoveAdmin(Person currentUser, Person userToRemove, Project project) throws RelationshipConstraintException;

    /**
     * Given a user and a project, determine whether the user may be added as an admin to the project
     * @param userToMakeProjectAdmin   The person to determine if they can be made a project admin
     * @param project   The project the person is to become an admin of
     * @return  True is the given user can be made an admin of the given project
     */
    public boolean canBecomeProjectAdmin(Person userToMakeProjectAdmin, Project project);

    /**
     * Given a user and a project, determine whether the user has the authorization to edit depositors for collections
     * in the project.
     * @param user
     * @param project
     * @return true if {@code user} can edit depositors for {@code project}
     */
    boolean canEditDepositorsForProject(Person user, Project project);
}

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MethodNotSupportedException;

/**
 * An implementation of {@link AuthorizationService} based on users' system
 * roles and {@link RelationshipService}.
 */
public class AuthorizationServiceImpl
        implements AuthorizationService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private RelationshipService relationshipService;

    public AuthorizationServiceImpl(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @Override
    public boolean canCreateProject(Person user) {
        //if user is an instance admin, he/she can create whatever he/she wants
        if (user.getRoles().contains(Role.ROLE_ADMIN)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canUpdateProject(Person user, Project project) {
        return isAdminForProject(user, project);
    }

    @Override
    public boolean canRetrieveProjectCollections(Person user, Project project) {
        return isAdminForProject(user, project);
    }

    @Override
    public boolean canCreateCollection(Person user, Project project) {
        if (user.getRoles().contains(Role.ROLE_ADMIN)
                || relationshipService.getAdministratorsForProject(project).contains(user)) {
             return true;
        }

        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canCreateCollection(Person user, Collection parentCollection) throws RelationshipConstraintException {
        //If a person can deposit to a collection, he/she can create sub collection under the collection. see DC1423 for
        //requirements
        return canDepositToCollection(user, parentCollection);
    }

    @Override
    public boolean canEditDepositorsForProject(Person user, Project project) {
        return isAdminForProject(user, project);
    }

    private boolean isAdminForProject(Person user, Project project) {

        //If user is an instance admin, he/she can do whatever he/she wants
        if (null != user && null != user.getRoles()
                && user.getRoles().contains(Role.ROLE_ADMIN)) {
            return true;
        }
        if (project == null) {
            return false;
        }
        //Retrieve all project's admins
        Set<Person> projectAdmins =
                relationshipService.getAdministratorsForProject(project);
        //If list of project admins contains the given user, he/she has the permission to update the project
        for (Person person : projectAdmins) {
            if (person.equals(user)) {
                return true;
            }
        }
        /*
         * if (projectAdmins != null && projectAdmins.contains(user)) { return
         * true; }
         */
        return false;
    }

    public boolean canReadProject(Person user, Project project) {
        return isAdminForProject(user, project);
    }

    @Override
    public boolean canRetrieveDataFile(Person user, DataFile dataFile)
            throws RelationshipConstraintException {

        if (dataFile == null) {
            return false;
        }

        return true;

    }

    /**
     * This implementation assumes that a collection only has ONE parent, despite of a the {@link RelationshipService}'s
     * {@code getSuperCollectionIdsForCollectionId() returning a {@link Set}.
     *
     * @param user
     * @param collection
     * @return whether the given user has permission to deposit into to the provided collection.
     * @throws RelationshipConstraintException
     */
    @Override
    public boolean canDepositToCollection(Person user, Collection collection) throws RelationshipConstraintException {

        //Instance admin has this permission
        if (user.getRoles().contains(Role.ROLE_ADMIN)) {
            return true;
        }


        String topCollectionId = collection.getId();
        String ancestorCollectionId = collection.getId();
        Set<Person> collectionDepositors;
        Set<String> ancestorCollectionIds;
        Iterator<String> iter;

        //Walk the graph of collection to see if user is one of the depositors for any collection up the graph.
        do {
            collectionDepositors = relationshipService.getDepositorsForCollection(ancestorCollectionId);

            if (collectionDepositors!= null && collectionDepositors.contains(user)) {
                return true;
            }

            try {
                ancestorCollectionIds = relationshipService.getSuperCollectionIdsForCollectionId(ancestorCollectionId);
                iter = ancestorCollectionIds.iterator();
                //only support one parent lookup
                if (iter.hasNext()) {
                    ancestorCollectionId = iter.next();
                } else {
                    topCollectionId = ancestorCollectionId;
                    ancestorCollectionId = null;
                }

            } catch (RelationshipException e) {
                throw new RelationshipConstraintException("Exception occured when looking up super collection. \n" + e);
            }
        } while (ancestorCollectionId != null);

        //After the collection graph is exhausted. Lookat the top level collection's parent (its containing project)
        //to see if user is that project's admin.
        String projectId = relationshipService.getProjectForCollection(topCollectionId);
        Set<Person> projectAdmins = relationshipService.getAdministratorsForProject(projectId);
        if (projectAdmins.contains(user)) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Anyone can retrieve the data item information regardless his/her role,
     * registration status.
     * 
     * @param user
     *        the user who wants to access {@code dataSet}
     * @param dataItem
     *        the DataItem access is being requested for
     * @return true regardless of the user being provided. Returns {@code false}
     *         is the data item passed in is null.
     */
    @Override
    public boolean canRetrieveDataSet(Person user, DataItem dataItem) {

        if (dataItem == null) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Anyone can retrieve the collection regardless his/her role, registration
     * status.
     * 
     * @param user
     *        the user who wants to access {@code Collection}
     * @param collection
     *        the collection access is being requested for
     * @return true regardless of the user being provided in the parameter
     */
    public boolean canRetrieveCollection(Person user, Collection collection) {
        if (collection == null) {
            return false;
        }

        return true;
    }

    /**
     * An instance admin, or an admin for the containing project, may update a
     * collection
     * 
     * @param user
     *        - user whose permission is to be checked
     * @param collection
     *        - collection to be modified
     * @throws RelationshipConstraintException
     */
    @Override
    public boolean canUpdateCollection(Person user, Collection collection)
            throws RelationshipConstraintException {
        if (collection == null) {
            return false;
        }

        if (user == null) {
            return false;
        }
        // instance admins can do anything
        if (user.getRoles().contains(Role.ROLE_ADMIN)) {
            return true;
        }

        String ancestorCollectionId = collection.getId();
        String topCollectionId = null;
        Set<String> ancestorCollectionIds;
        //Walk the graph of collection to find top level collection
        do {
            try {
                ancestorCollectionIds = relationshipService.getSuperCollectionIdsForCollectionId(ancestorCollectionId);
                Iterator<String> iter = ancestorCollectionIds.iterator();
                //only support one parent lookup
                if (iter.hasNext()) {
                    ancestorCollectionId = iter.next();
                } else {
                    topCollectionId = ancestorCollectionId;
                    ancestorCollectionId = null;
                }

            } catch (RelationshipException e) {
                throw new RelationshipConstraintException("Exception occured when looking up super collection. \n" + e);
            }
        } while (ancestorCollectionId != null);

        //After the collection graph is exhausted. Lookat the top level collection's parent (its containing project)
        //to see if user is that project's admin.
        String projectId = relationshipService.getProjectForCollection(topCollectionId);
        Set<Person> projectAdmins = relationshipService.getAdministratorsForProject(projectId);
        if (projectAdmins.contains(user)) {
            return true;
        }
        return false;
    }

    /**
     * Current behavior is that anyone can view the list of collections.
     */
    @Override
    public boolean canViewCollectionList(Person user) {
        return true;
    }

    @Override
    public boolean canRemoveDepositor(Person currentUser,
                                      Person userToRemove,
                                      Collection collection)
            throws RelationshipConstraintException {
        boolean canRemove = false;
        if (currentUser != null && collection != null) {
            if (!currentUser.getId().equalsIgnoreCase(userToRemove.getId())) {
                if (currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
                    canRemove = true;
                } else {
                    if (!userToRemove.getRoles().contains(Role.ROLE_ADMIN)) {
                        //now see if the user is a project admin
                        Project project =
                                relationshipService
                                        .getProjectForCollection(collection);
                        if (isAdminForProject(currentUser, project)) {
                            if (!isAdminForProject(userToRemove, project)) {
                                canRemove = true;
                            }
                        }
                    }
                }
            }
        }

        return canRemove;
    }

    @Override
    public boolean canRemoveAdmin(Person currentUser,
                                  Person userToRemove,
                                  Project project)
            throws RelationshipConstraintException {

        boolean canRemove = false;
        if (currentUser != null && project != null) {
            if (!currentUser.getId().equalsIgnoreCase(userToRemove.getId())) {
                if (currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
                    canRemove = true;
                }
            }
        }

        return canRemove;
    }

    @Override
    public boolean canBecomeProjectAdmin(Person userToMakeProjectAdmin,
                                         Project project) {
        //this does not depend on the project in this implementation - any approved registered user
        //can be made an admin on any project
        boolean canMakeProjectAdmin = false;
        if (userToMakeProjectAdmin != null
                && project != null
                && userToMakeProjectAdmin.getRegistrationStatus()
                        .equals(RegistrationStatus.APPROVED)) {
            canMakeProjectAdmin = true;
        }
        return canMakeProjectAdmin;
    }
}

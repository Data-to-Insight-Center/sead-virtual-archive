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

import java.util.Set;

import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.ProjectServiceException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;

/**
 * {@code ProjectBizService} provides business operations on {@link Project}. It is used by {@link org.dataconservancy.ui.api.ProjectController}
 * and several Action Beans to operate on {@link Project} objects.
 */
public interface ProjectBizService {

    /**
     * Given a {@link org.dataconservancy.ui.model.Project} update it content to the project with matching business id.
     * If not project is found with matching business id, insert the given project as new. The given user's credentials
     * will be used to authenticate and authorize the request.
     *
     * @param project
     * @param user
     * @return id of the created or updated project.
     * @throws BizPolicyException
     * @throws ProjectServiceException
     */
    String updateProject(Project project, Person user)
            throws BizPolicyException, BizInternalException;

    /**
     * Given a person, find list of project that person administers. If the user has  ROLE_ADMIN, return all projects.
     */
    Set<Project> findByAdmin(Person user);

    /**
     * Returns the collections related to a given project.
     *
     * @param project The project to retrieve the collections for.
     * @param user The user who is trying to retrieve the collection. Must be an administrator of the project.
     * @throws BizPolicyException If the user doesn't have the proper permissions to retrieve the collections
     * @return  the set of collections related to the project, or an empty set if non exist. Returns null if the project passed in is null.
     */
    Set<Collection> getCollectionsForProject(Project project, Person user)
             throws BizPolicyException;
    
    /**
     * Attempt to retrieve a project by id as a user. 
     * 
     * @param id
     * @param user The user must be an instance admin or an administrator of the project.
     * @throws BizPolicyException If the user doesn't have the proper permissions to retrieve the project.
     * @return null if a project with the given id does not exist
     */
    Project getProject(String id, Person user) throws BizPolicyException;
    
    /**
     * Attempt to retrieve a project by id without user constraints. 
     * 
     * @param id
     * @throws BizPolicyException If the user doesn't have the proper permissions to retrieve the project.
     * @return null if a project with the given id does not exist
     */
    Project getProject(String id) throws BizPolicyException;
}


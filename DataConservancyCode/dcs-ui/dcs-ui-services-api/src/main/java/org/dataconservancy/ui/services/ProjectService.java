/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.ui.services;

import java.util.Comparator;
import java.util.List;

import org.dataconservancy.ui.exceptions.ProjectServiceException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;

/**
 * <p>
 * Project service provide utilities methods to manages Project objects.
 * </p>
 */
public interface ProjectService extends Iterable<Project> {
    
    /**
     * 
     * @param id
     * @return Project object with the given id. If no such project is found, return null.
     * 
     */
    public Project get(String id);
    
    /**
     * 
     * @param project
     * @return The created project.
     */
    public Project create(Project project);
    
    /**
     * 
     * @param project
     * @return The updated project.
     * @throws ProjectServiceException
     *             when no updated was made due to non-existing records.
     */
    public Project update(Project project) throws ProjectServiceException;
    
    /**
     * 
     * @param query
     * @param comparator
     */
    public List<Project> find(String query, Comparator<Project> comparator);
    
    /**
     * 
     * @param person
     * @return A list of Projects associated with the provided person.
     */
    public List<Project> findByPI(Person person);
    
    /**
     * 
     * @return A list of all Projects or null if empty
     */
    public List<Project> getAll();
    
    /**
     * Determine whether a project with the given business id exists in the system
     * 
     * @param id
     * @return <code>true</code> if the project with matching business id is found.
     * @return <code>false</code> if no project with matching business id is found.
     */
    public boolean isExisting(String id);
}

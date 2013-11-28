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
package org.dataconservancy.ui.dao;

import java.util.List;

import org.dataconservancy.ui.model.Project;

/**
 * Responsible for CRUD operations on an underlying persistence store for {@link Project} objects.
 */
public interface ProjectDAO {
    
    public List<Project> getProjectList();
    
    public Project selectProject(String id);
    
    public Project insertProject(Project project);
    
    /**
     * 
     * @param id
     *            - Project id
     * @return <p>
     *         <code>int</code> number of rows effected (deleted)
     *         </p>
     *         <p>
     *         If the project with provided <code>id</code> does not exist, number of rows effected will be 0. Therefore
     *         the method will return 0.
     *         </p>
     */
    public int deleteProject(String id);
    
    /**
     * @param project
     *            - The project with changes that needs to be persisted.
     * @return <p>
     *         <code>int</code> number of rows effected (updated)
     *         </p>
     *         <p>
     *         If the project with provided <code>id</code> does not exist, changes to it cannot be persisted,number of
     *         rows effected will be 0. Therefore the method will return 0.
     *         </p>
     */
    public int updateProject(Project project);
    
}

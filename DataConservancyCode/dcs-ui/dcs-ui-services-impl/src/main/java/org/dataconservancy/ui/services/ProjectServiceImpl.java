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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.dataconservancy.ui.dao.PersonDAO;
import org.dataconservancy.ui.dao.ProjectAwardDAO;
import org.dataconservancy.ui.dao.ProjectDAO;
import org.dataconservancy.ui.dao.ProjectPIDAO;
import org.dataconservancy.ui.exceptions.ProjectServiceException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ProjectService}.
 */

public class ProjectServiceImpl implements ProjectService {
    
    private Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);
    
    private ProjectDAO projectDAO;
    private ProjectPIDAO projectPIDAO;
    private PersonDAO personDao;
    private ProjectAwardDAO projectAwardDao;
    
    public ProjectServiceImpl(ProjectDAO projectDao, PersonDAO personDao, ProjectPIDAO projectAdminDao,
            ProjectAwardDAO projectAwardDao) {
        this.projectPIDAO = projectAdminDao;
        this.personDao = personDao;
        this.projectDAO = projectDao;
        this.projectAwardDao = projectAwardDao;
    }
    
    @Override
    public Iterator<Project> iterator() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Project get(String id) {
        log.trace("Obtaining project for id {}", id);
        Project project = projectDAO.selectProject(id);
        if (project != null) {
            List<String> piIds = projectPIDAO.getPIIds(project.getId());
            project.setPis(piIds);
            List<String> numbers = projectAwardDao.getNumbers(project.getId());
            project.setNumbers(numbers);
        }
        return project;
    }
    
    @Override
    public Project create(Project project) {
        try {
            validateProject(project);
        }
        catch (IllegalArgumentException e) {
            throw e;
        }
        log.trace("Creating project {}", project);
        Project insertedProject = projectDAO.insertProject(project);
        log.trace("In ProjectServiceImplt, insertedProject: " + insertedProject.toString());
        projectPIDAO.insertAllProjectPIs(insertedProject);
        projectAwardDao.insertAllProjectNumbers(insertedProject);
        return insertedProject;
    }
    
    @Override
    public Project update(Project project) throws ProjectServiceException {
        
        try {
            validateProject(project);
        }
        catch (IllegalArgumentException e) {
            throw e;
        }
        log.trace("Updating project {}", project);
        
        if (projectDAO.updateProject(project) == 0) {
            throw new ProjectServiceException(
                    "No update was made to the records, most likely because no matching record was found.");
        }
        
        // Look through Pis list, make update and insertion as appropriate
        List<String> existingAdminIds = projectPIDAO.getPIIds(project.getId());
        
        // For each incoming person not found in the existing admins list
        // insert the new person
        for (String person : project.getPis()) {
            // if an admin id doesn't exist in the db for a certain project
            // insert it in to the db
            if (!existingAdminIds.contains(person)) {
                projectPIDAO.insertSingleProjectPI(project.getId(), person);
            }
        }
        
        List<String> projectPisList = new ArrayList<String>();
        for (String person : project.getPis()) {
            projectPisList.add(person);
        }
        // For each person in the existing admins list that does not exist in
        // the
        // incoming project's pis list, remove that existing person
        for (String adminId : existingAdminIds) {
            if (!projectPisList.contains(adminId)) {
                projectPIDAO.deleteSingleProjectAdmin(project.getId(), adminId);
            }
        }
        
        // Same as above, but for numbers.
        List<String> currentProjectNumbers = projectAwardDao.getNumbers(project.getId());
        
        for (String number : project.getNumbers()) {
            if (!currentProjectNumbers.contains(number)) {
                projectAwardDao.insertSingleProjectNumber(project.getId(), number);
            }
        }
        
        for (String number : currentProjectNumbers) {
            if (!project.getNumbers().contains(number)) {
                projectAwardDao.deleteSingleProjectNumber(project.getId(), number);
            }
        }

        
        Project updated = projectDAO.selectProject(project.getId());
        return updated;
    }
    
    @Override
    public List<Project> find(String query, Comparator<Project> comparator) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<Project> findByPI(Person person) {
        List<Project> projects = new ArrayList<Project>();
        List<String> projectIds = projectPIDAO.getProjectIds(person);
        for (String id : projectIds) {
            projects.add(projectDAO.selectProject(id));
        }
        return projects;
    }
    
    @Override
    public List<Project> getAll() {
        List<Project> projects = new ArrayList<Project>();
        for (Project p : projectDAO.getProjectList()) {
            p.setNumbers(projectAwardDao.getNumbers(p.getId()));
            projects.add(p);
        }
        return projects;
    }
    
    @Override
    public boolean isExisting(String id) {
        log.trace("Determining the existence of project for id {}", id);
        Project project = projectDAO.selectProject(id);
        if (project == null) {
            return false;
        }
        else {
            return true;
        }
    }

    private void validateProject(Project project) throws IllegalArgumentException {
        if (project == null)
            throw new IllegalArgumentException("Expect non-null project");
        if (project.getName() == null || project.getName().trim().equals(""))
            throw new IllegalArgumentException("Project's name cannot be null or empty");
        if (project.getStartDate() == null || project.getEndDate() == null)
            throw new IllegalArgumentException("Project's start and end date cannot be null");
        if (project.getId() == null || project.getId().isEmpty()) {
            throw new IllegalArgumentException("Project ID must not be empty or null.");
        }
        
    }
}

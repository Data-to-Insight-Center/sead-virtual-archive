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
package org.dataconservancy.ui.api.support;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dataconservancy.ui.exceptions.ProjectServiceException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.ProjectService;

public class MockProjectService implements ProjectService {

    Map<String, Project> projectMap;
 
    public MockProjectService() {
        projectMap = new HashMap<String, Project>();
    }
    
    @Override
    public Iterator<Project> iterator() {
        return projectMap.values().iterator();
    }
    
    @Override
    public Project get(String id) {
        return projectMap.get(id);
    }

    @Override
    public Project create(Project project) {
        projectMap.put(project.getId(), project);
        return project;
    }

    @Override
    public Project update(Project project) throws ProjectServiceException {
        projectMap.put(project.getId(), project);
        
     
        return project;
    }

    @Override
    public List<Project> find(String query, Comparator<Project> comparator) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Project> findByPI(Person person) {
        ArrayList<Project> matchingProjects = new ArrayList<Project>();
        
        Iterator<Entry<String, Project>> it = projectMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Project> pairs = (Map.Entry<String, Project>)it.next();
            Project proj = pairs.getValue();
            for (String admin : proj.getPis()) {
                if (admin.equals(person.getId())) {
                    matchingProjects.add(proj);
                }
            }
        }
        return matchingProjects;
    }

    @Override
    public List<Project> getAll() {
        ArrayList<Project> projects = new ArrayList<Project>();
        
        Iterator<Entry<String, Project>> it = projectMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Project> pairs = (Map.Entry<String, Project>)it.next();
            projects.add(pairs.getValue());
        }
        
        return projects;
    }

    @Override
    public boolean isExisting(String id) {
        return projectMap.containsKey(id);
    }
}
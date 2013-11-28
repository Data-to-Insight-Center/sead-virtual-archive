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
 * This class is used by {@link org.dataconservancy.ui.dao.ProjectDAO} to manage Project Award Numbers.
 */
public interface ProjectAwardDAO {
    public String getProjectId(String number);
    
    public List<String> getNumbers(String projectId);
    
    public int deleteSingleProjectNumber(String projectId, String number);
    
    public void insertAllProjectNumbers(Project project);
    
    public void insertSingleProjectNumber(String projectId, String number);
}

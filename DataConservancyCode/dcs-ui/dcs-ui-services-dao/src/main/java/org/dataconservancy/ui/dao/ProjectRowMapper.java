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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.dataconservancy.ui.model.Project;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

/**
 * This class provides method to map a row from PROJECT table result set into a
 * {@link org.dataconservancy.ui.model.Project} object.
 */
class ProjectRowMapper implements RowMapper<Project> {
    public Project mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        
        Project project = new Project();
        project.setId(resultSet.getString("ID"));
        project.setDescription(resultSet.getString("DESCRIPTION"));
        project.setName(resultSet.getString("NAME"));
        project.setStartDate(new DateTime(resultSet.getLong("START_DATE")));
        project.setEndDate(new DateTime(resultSet.getLong("END_DATE")));
        project.setStorageAllocated(Long.parseLong(resultSet.getString("STORAGE_ALLOCATED")));
        project.setStorageUsed(Long.parseLong(resultSet.getString("STORAGE_USED")));
        project.setFundingEntity(resultSet.getString("FUNDING_ENTITY"));
        project.setPublisher(resultSet.getString("PUBLISHER"));
        return project;
        
    }
}

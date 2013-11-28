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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.dataconservancy.ui.model.Project;
import org.joda.time.DateTime;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.MetaDataAccessException;

/**
 * JDBC implementation of ProjectDAO.
 */
public class ProjectDAOJdbcImpl extends DcsUiDaoBaseImpl implements ProjectDAO {
    private static final String PROJECT_TBL = "PROJECT";
    
    static final String DATE_PATTERN = "yyyy-MM-dd";
    
    public ProjectDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    }
    
    @Override
    public List<Project> getProjectList() {
        String query = "SELECT * FROM " + PROJECT_TBL;
        
        return (List<Project>) jdbcTemplate.query(query, new ProjectRowMapper());
    }
    
    @Override
    public Project selectProject(String id) {
        String query = "SELECT * FROM " + PROJECT_TBL + " WHERE ID = ?";
        
        List<Project> projects = (List<Project>) jdbcTemplate.query(query, new Object[] { id }, new ProjectRowMapper());
        if (projects.size() == 0) {
            return null;
        }
        else {
            return projects.get(0);
        }
    }
    
    @Override
    public Project insertProject(Project project) {
        InsertPreparedStatementCreator insertPSCreator = new InsertPreparedStatementCreator(project);
        try {
            jdbcTemplate.update(insertPSCreator);
        }
        catch (DataAccessException dae) {
            throw new RuntimeException("Failed to insert the project. Message: " + dae.getMessage(), dae);
        }
        
        // tests expect a copy of project
        return new Project(project);
    }
    
    @Override
    public int deleteProject(String id) {
        String query = "DELETE FROM " + PROJECT_TBL + " WHERE ID = ?";
        return jdbcTemplate.update(query, new Object[] { id });
    }
    
    @Override
    public int updateProject(Project project) {
        String query = "UPDATE " + PROJECT_TBL + " SET NAME = ?" + "   , DESCRIPTION = ?" + "   , START_DATE = ?"
                + "   , END_DATE = ?" + "   , STORAGE_ALLOCATED = ?" + "   , STORAGE_USED = ?"
                + "   , FUNDING_ENTITY = ?" + "   , PUBLISHER = ?" + " WHERE ID = ? ";
        try {
            return jdbcTemplate.update(query,
                    new Object[] { project.getName(), project.getDescription(), project.getStartDate().getMillis(),
                            project.getEndDate().getMillis(), project.getStorageAllocated(), project.getStorageUsed(),
                            project.getFundingEntity(), project.getPublisher(), project.getId() });
        }
        catch (DataAccessException dae) {
            throw new RuntimeException("Failed to update the project. Message: " + dae.getMessage(), dae);
        }
    }
    
    private static class InsertPreparedStatementCreator implements PreparedStatementCreator {
        String query = "INSERT INTO  " + PROJECT_TBL + " VALUES (?,?,?,?,?,?,?,?,?)";
        String projectId;
        String projectName;
        String projectDescription;
        DateTime projectStartDate;
        DateTime projectEndDate;
        long projectStorageAllocated;
        long projectStorageUsage;
        String fundingEntity;
        String publisher;
        
        public InsertPreparedStatementCreator(Project project) {
            if (project.getId() == null || project.getId().isEmpty()) {
                throw new IllegalArgumentException("Project ID must not be empty or null.");
            }
            projectId = project.getId();
            projectName = project.getName();
            projectDescription = project.getDescription();
            projectStartDate = project.getStartDate();
            projectEndDate = project.getEndDate();
            projectStorageAllocated = project.getStorageAllocated();
            projectStorageUsage = project.getStorageUsed();
            fundingEntity = project.getFundingEntity();
            publisher = project.getPublisher();
        }
        
        @Override
        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, projectId);
            ps.setString(2, projectName);
            ps.setString(3, projectDescription);
            ps.setLong(4, projectStartDate.getMillis());
            ps.setLong(5, projectEndDate.getMillis());
            ps.setLong(6, projectStorageAllocated);
            ps.setLong(7, projectStorageUsage);
            ps.setString(8, fundingEntity);
            ps.setString(9, publisher);
            
            return ps;
        }
    }
    
    /**
     * Provides method to check for existence of Person table in current database.
     * 
     */
    private class CheckForProjectTable implements DatabaseMetaDataCallback {
        @Override
        public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
            ResultSet rs = dbmd.getTables(null, null, PROJECT_TBL, null);
            
            if (rs.next()) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    }
    
}

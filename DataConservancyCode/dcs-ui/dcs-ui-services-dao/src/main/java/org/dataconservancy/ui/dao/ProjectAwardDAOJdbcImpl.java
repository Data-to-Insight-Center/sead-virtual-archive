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

/**
 * 
 */
package org.dataconservancy.ui.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.dataconservancy.ui.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author pmeyer
 * 
 */
public class ProjectAwardDAOJdbcImpl extends DcsUiDaoBaseImpl implements ProjectAwardDAO {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private ProjectDAO projectDao;
    
    private static final String INSERTION_SQL = "INSERT INTO PROJECT_AWARD VALUES (?, ?)";
    private static final String NUMBER_SELECTION_SQL = "SELECT PROJECT_NUMBER FROM PROJECT_AWARD WHERE PROJECT_ID = ?";
    private static final String PROJECT_SELECTION_SQL = "SELECT PROJECT_ID FROM PROJECT_AWARD WHERE PROJECT_NUMBER = ?";
    private static final String DELETION_SQL = "DELETE FROM PROJECT_AWARD WHERE PROJECT_ID = ? AND PROJECT_NUMBER = ?";
    private static final String NUMBER_COL = "PROJECT_NUMBER";
    private static final String PROJECT_COL = "PROJECT_ID";
    
    public ProjectAwardDAOJdbcImpl(DataSource dataSource, ProjectDAO projectDao) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource must not be null.");
        }
        
        if (projectDao == null) {
            throw new IllegalArgumentException("ProjectDAO must not be null.");
        }
        
        this.projectDao = projectDao;
        
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    @Override
    public String getProjectId(String number) {
        List<String> projectIds = jdbcTemplate.query(PROJECT_SELECTION_SQL, new Object[] { number },
                new RowMapper<String>() {
                    @Override
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString(PROJECT_COL);
                    }
                });
        return projectIds.get(0);
    }
    
    @Override
    public List<String> getNumbers(String projectId) {
        final String finalProjectId = projectId;
        List<String> numbers = jdbcTemplate.query(NUMBER_SELECTION_SQL, new Object[] { finalProjectId },
                new RowMapper<String>() {
                    @Override
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString(NUMBER_COL);
                    }
                });
        return numbers;
    }
    
    @Override
    public int deleteSingleProjectNumber(String projectId, String number) {
        return jdbcTemplate.update(DELETION_SQL, new Object[] { projectId, number });
    }
    
    @Override
    public void insertAllProjectNumbers(Project project) {
        final String projectId = project.getId();
        final List<String> numbers = project.getNumbers();
        jdbcTemplate.batchUpdate(INSERTION_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, projectId);
                ps.setString(2, numbers.get(i));
            }
            
            @Override
            public int getBatchSize() {
                return numbers.size();
            }
        });
    }
    
    @Override
    public void insertSingleProjectNumber(String projectId, String number) {
        jdbcTemplate.update(INSERTION_SQL, new Object[] { projectId, number });
    }
    
}

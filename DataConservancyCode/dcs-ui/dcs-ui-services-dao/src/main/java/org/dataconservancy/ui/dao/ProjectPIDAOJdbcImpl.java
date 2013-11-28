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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * JDBC implementation of ProjectPIDAO.
 */
public class ProjectPIDAOJdbcImpl extends DcsUiDaoBaseImpl implements ProjectPIDAO {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private ProjectDAO projectDao;
    
    private PersonDAO personDao;
    
    public ProjectPIDAOJdbcImpl(DataSource dataSource, ProjectDAO projectDao, PersonDAO personDao) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource must not be null.");
        }
        
        if (projectDao == null) {
            throw new IllegalArgumentException("ProjectDAO must not be null.");
        }
        
        if (personDao == null) {
            throw new IllegalArgumentException("PersonDAO must not be null.");
        }
        
        this.projectDao = projectDao;
        this.personDao = personDao;
        
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    static final String INSERTION_SQL = "INSERT INTO PROJECT_PI VALUES (?, ?)";
    static final String PI_SELECTION_SQL = "SELECT PERSON_ID FROM PROJECT_PI WHERE PROJECT_ID = ?";
    static final String PROJECT_SELECTION_SQL = "SELECT PROJECT_ID FROM PROJECT_PI WHERE PERSON_ID = ?";
    static final String DELETION_SQL = "DELETE FROM PROJECT_PI WHERE PROJECT_ID = ? AND PERSON_ID = ?";
    static final String PERSON_COL = "PERSON_ID";
    static final String PROJECT_COL = "PROJECT_ID";
    
    @Override
    public List<String> getProjectIds(Person person) {
        final String personID = person.getId();
        List<String> projectIds = jdbcTemplate.query(PROJECT_SELECTION_SQL, new Object[] { personID },
                new RowMapper<String>() {
                    @Override
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString(PROJECT_COL);
                    }
                });
        return projectIds;
    }
    
    @Override
    public List<String> getPIIds(String projectId) {
        final String finalProjectId = projectId;
        List<String> PIIds = jdbcTemplate.query(PI_SELECTION_SQL, new Object[] { finalProjectId },
                new RowMapper<String>() {
                    @Override
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString(PERSON_COL);
                    }
                });
        return PIIds;
    }
    
    @Override
    public void insertAllProjectPIs(Project project) {
        final List<String> pis = project.getPis();
        final String projectId = project.getId();
        jdbcTemplate.batchUpdate(INSERTION_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, projectId);
                ps.setString(2, pis.get(i));
            }
            
            @Override
            public int getBatchSize() {
                return pis.size();
            }
        });
        
    }
    
    @Override
    public void insertSingleProjectPI(String projectId, String PIId) {
        jdbcTemplate.update(INSERTION_SQL, new Object[] { projectId, PIId });
    }
    
    /*
     * functionality not yet needed/tested
     * 
     * @Override public int deleteAdminsByAdminId(String adminId) { String query = "DELETE FROM " + PROJECT_ADMIN_TBL +
     * " WHERE PERSON_ID = ?"; return jdbcTemplate.update(query, new Object[] { adminId }); }
     */
    /*
     * functionality not yet needed/tested
     * 
     * @Override public int deleteAdminIdsByProjectId(int projectId) { String query = "DELETE FROM " + PROJECT_ADMIN_TBL
     * + " WHERE PROJECT_ID = ?"; return jdbcTemplate.update(query, new Object[] { projectId }); }
     */
    @Override
    public int deleteSingleProjectAdmin(String projectId, String adminId) {
        return jdbcTemplate.update(DELETION_SQL, new Object[] { projectId, adminId });
    }
    
}

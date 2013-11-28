/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.ui.dao;

import org.dataconservancy.ui.model.PasswordResetRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jrm
 * Date: 2/13/13
 * Time: 9:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class PasswordResetRequestDAOJdbcImpl implements PasswordResetRequestDAO {
    private static final String TABLE = "PASSWORD_RESET";
    private static final String ID_COLUMN = "ID";

    private JdbcTemplate jdbcTemplate;
    private final PasswordResetRequestRowMapper rowMapper = new PasswordResetRequestRowMapper();

    public PasswordResetRequestDAOJdbcImpl(JdbcTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException(
                    "Spring JDBC template must not be null.");
        }

        this.jdbcTemplate = template;
    }

    public void add(PasswordResetRequest prr) {
        String query = "INSERT INTO  " + TABLE + " VALUES (?,?,?)";

        jdbcTemplate.update(query,
                new Object[] { prr.getId(), prr.getRequestDate().getMillis(), prr.getUserEmailAddress() });
    }

    public List<PasswordResetRequest> list() {
        String query = "SELECT * FROM " + TABLE;

        return (List<PasswordResetRequest>) jdbcTemplate.query(query, new Object[] {},
                rowMapper);
    }

    @Override
    public PasswordResetRequest get(String requestId) {
        String query = "SELECT * FROM " + TABLE + " WHERE " + ID_COLUMN + " = ?";
        try {
            return jdbcTemplate.queryForObject(query, new Object[]{requestId}, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            if (e.getActualSize() == 0) {
                // ignore and return null, because the password reset request doesn't exist in the db.
                return null;
            }
            // otherwise re-throw.
            throw e;
        }
    }
    
    public void delete(String id){
        //log.debug("Deleting Package with objectId of {}", url);
        String query = "DELETE FROM " + TABLE + " WHERE " + ID_COLUMN + " = ?";
        jdbcTemplate.update(query, new Object[] { id });
    }

}

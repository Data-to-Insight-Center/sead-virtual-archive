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
package org.dataconservancy.ui.dao;

import java.util.List;

import org.dataconservancy.ui.model.Discipline;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * JDBC implementation of DisciplineDAO.
 */
public class DisciplineDAOJdbcImpl implements DisciplineDAO {
    private static final String TABLE = "DISCIPLINE";
    private static final String ID_COLUMN = "ID";

    private JdbcTemplate template;
    private final DisciplineRowMapper rowMapper = new DisciplineRowMapper();

    public DisciplineDAOJdbcImpl(JdbcTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException(
                    "Spring JDBC template must not be null.");
        }

        this.template = template;
    }

    public void add(Discipline dis) {
        String query = "INSERT INTO  " + TABLE + " VALUES (?,?)";

        template.update(query,
                new Object[] { dis.getId(), dis.getTitle() });
    }

    public List<Discipline> list() {
        String query = "SELECT * FROM " + TABLE;

        return (List<Discipline>) template.query(query, new Object[] {},
                rowMapper);
    }

    @Override
    public Discipline get(String disciplineId) {
        String query = "SELECT * FROM " + TABLE + " WHERE " + ID_COLUMN + " = ?";
        try {
            return template.queryForObject(query, new Object[]{disciplineId}, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            if (e.getActualSize() == 0) {
                // ignore and return null, because the discipline doesn't exist in the db.
                return null;
            }
            // otherwise re-throw.
            throw e;
        }
    }
}

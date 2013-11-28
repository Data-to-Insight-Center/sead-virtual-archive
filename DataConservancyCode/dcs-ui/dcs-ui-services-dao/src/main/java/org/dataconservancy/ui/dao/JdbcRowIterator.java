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

import org.dataconservancy.ui.model.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.dataconservancy.ui.dao.RelationshipDAOJdbcImpl.REL_COLUMN;
import static org.dataconservancy.ui.dao.RelationshipDAOJdbcImpl.REL_TABLE;
import static org.dataconservancy.ui.dao.RelationshipDAOJdbcImpl.SOURCE_COLUMN;
import static org.dataconservancy.ui.dao.RelationshipDAOJdbcImpl.TARGET_COLUMN;

/**
 * This class implements {@link Iterator} interface to provide a way to iterate through collection of {@link org.dataconservancy.ui.model.Relationship}
 */
public class JdbcRowIterator implements Iterator<Relationship> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Spring JDBC template for query execution.
     */
    private final JdbcTemplate template;

    /**
     * Maximum number of relationships to retrieve using the iterator.
     */
    private final int max;

    /**
     * Maximum number of relationships to retrieve in a single database query.
     */
    private final int step;

    /**
     * The initial offset into the database query results.
     */
    private final int initialOffset;


    private int currentOffset;
    private Iterator<Relationship> currentItr;

    public JdbcRowIterator(JdbcTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("JDBC template must not be null.");
        }


        this.template = template;
        this.initialOffset = 0;
        this.currentOffset = 0;
        this.max = template.queryForInt("SELECT count(*) FROM " + REL_TABLE);

        if (1000 > max) {
            this.step = max;
        } else {
            this.step = 1000;
        }
    }

    public JdbcRowIterator(JdbcTemplate template, int initialOffset, int step, int max) {
        if (initialOffset < 0) {
            throw new IllegalArgumentException("Offset must be zero or greater.");
        }

        if (step < 1) {
            throw new IllegalArgumentException("Step must be one or greater.");
        }

        if (max < 0) {
            throw new IllegalArgumentException("Total must be zero or greater.");
        }

        if (template == null) {
            throw new IllegalArgumentException("JDBC template must not be null.");
        }

        if (step > max) {
            step = max;
        }
        
        this.initialOffset = initialOffset;
        this.currentOffset = initialOffset;
        this.step = step;
        this.max = max;
        this.template = template;

        // initialize the iterator
        refreshCurrentIterator();
    }

    @Override
    public boolean hasNext() {
        // If a current iterator exists: check to see if it is exhausted, and refresh it
        if (currentItr != null && !currentItr.hasNext()) {
            refreshCurrentIterator();
        }

        return (currentItr != null) ? currentItr.hasNext() : false;
    }

    @Override
    public Relationship next() {
        // If a current iterator exists: check to see if it is exhausted, and refresh it
        if (currentItr != null && !currentItr.hasNext()) {
            refreshCurrentIterator();
        }

        if (currentItr != null) {
            return currentItr.next();
        }

        throw new NoSuchElementException("Iterator is exhausted!");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() is not supported by " + this.getClass().getName());
    }


    private void refreshCurrentIterator() {
        if (currentOffset >= initialOffset + max) {
            log.debug("Maximum number of iterations (" + max + ") reached, won't refresh iterator: " + initialOffset+max);
            currentItr = null;
            return;
        }

        final String sql =
                "SELECT " + SOURCE_COLUMN + ", " + TARGET_COLUMN + ", " + REL_COLUMN +
                        " FROM " + REL_TABLE +
                        " OFFSET " + currentOffset + " ROWS " +
                        "FETCH NEXT " + step + " ROWS ONLY";

        final Set<Relationship> rels = new HashSet<Relationship>(step);
        template.query(sql, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                final String source = resultSet.getString(SOURCE_COLUMN);
                final String target = resultSet.getString(TARGET_COLUMN);
                final String relString = resultSet.getString(REL_COLUMN);
                rels.add(new Relationship(source, target, Relationship.RelType.valueOf(relString.toUpperCase())));
            }
        });

        this.currentItr = rels.iterator();
        this.currentOffset = currentOffset + step;
        if (currentOffset > (initialOffset + max)) {
            currentOffset = initialOffset + max;
        }
    }

}

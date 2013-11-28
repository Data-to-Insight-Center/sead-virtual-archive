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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.dataconservancy.ui.model.Relationship.RelType;

/**
 * Implementation of the RelationshipDAO interface using Spring JDBC.  Note: to see the SQL being executed by this
 * class, enable debugging level output for the Spring {@code JdbcTemplate} class.
 */
public class RelationshipDAOJdbcImpl implements RelationshipDAO {

    private JdbcTemplate template;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /** Package-private for testing */
    static final String REL_TABLE = "relationships";

    /** Package-private for testing */
    static final String SOURCE_COLUMN = "source";

    /** Package-private for testing */
    static final String TARGET_COLUMN = "target";

    /** Package-private for testing */
    static final String REL_COLUMN = "reltype";

    private static final String ADD_RELATION_QUERY =
            "INSERT INTO " + REL_TABLE +
                " (" + SOURCE_COLUMN + ", " + TARGET_COLUMN + ", " + REL_COLUMN + ") " +
                " VALUES ( ?, ?, ? )";

    private static final String REMOVE_RELATION_QUERY =
            "DELETE FROM " + REL_TABLE +
                " WHERE " + SOURCE_COLUMN + " = ? " +
                " AND " + TARGET_COLUMN + " = ? " +
                " AND " + REL_COLUMN + " = ?";

    private static final String REMOVE_ALL_RELATION_QUERY =
            "DELETE FROM " + REL_TABLE +
                " WHERE " + SOURCE_COLUMN + " = ? " +
                " OR " + TARGET_COLUMN + " = ?";

    private static final String GET_RELATION_FOR_ID_QUERY =
            "SELECT " + SOURCE_COLUMN + ", " + TARGET_COLUMN + ", " + REL_COLUMN +
                " FROM " + REL_TABLE +
                " WHERE " + SOURCE_COLUMN + " = ? " +
                " OR " + TARGET_COLUMN + " = ?";

    private static final String GET_RELATION_FOR_ID_AND_TYPE_QUERY =
            "SELECT " + SOURCE_COLUMN + ", " + TARGET_COLUMN + ", " + REL_COLUMN +
                " FROM " + REL_TABLE +
                " WHERE (" + SOURCE_COLUMN + " = ? OR " + TARGET_COLUMN + " = ? )" +
                " AND " + REL_COLUMN + " = ?";

    private static final String GET_RELATION_FOR_ID_AND_TYPE_QUERY_SOURCE =
            "SELECT " + SOURCE_COLUMN + ", " + TARGET_COLUMN + ", " + REL_COLUMN +
                    " FROM " + REL_TABLE +
                    " WHERE " + SOURCE_COLUMN + " = ? " +
                    " AND " + REL_COLUMN + " = ?";

    private static final String GET_RELATION_FOR_ID_AND_TYPE_QUERY_TARGET =
            "SELECT " + SOURCE_COLUMN + ", " + TARGET_COLUMN + ", " + REL_COLUMN +
                    " FROM " + REL_TABLE +
                    " WHERE "  + TARGET_COLUMN + " = ? " +
                    " AND " + REL_COLUMN + " = ?";

    private static final String RELATIONSHIP_COUNT_QUERY =
            "SELECT count(*) FROM " + REL_TABLE;

    /**
     * Construct a new RelationshipDAOJdbcImpl.  A fully configured Spring {@code JdbcTemplate} must be supplied
     * on construction.
     *
     * @param template the Spring {@code JdbcTemplate}, must not be null.
     */
    public RelationshipDAOJdbcImpl(JdbcTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Spring JDBC template must not be null.");
        }
        this.template = template;
    }

    @Override
    public void addRelation(String sourceId, String targetId, Relationship.RelType rel) {
        validate(sourceId, targetId, rel);
        try {
            executeUpdate(ADD_RELATION_QUERY, sourceId, targetId, rel.name());
        } catch (DuplicateKeyException e) {
            log.debug("Attempted to add a duplicate relationship: {} {} {} already exists.",
                    new Object[] { sourceId, targetId, rel });
        }
    }

    @Override
    public void addRelations(Set<Relationship> relationships) {
        if (relationships == null) {
            throw new IllegalArgumentException("Relationships must not be null.");
        }

        if (relationships.isEmpty()) {
            return;
        }
        
        final List<Relationship> relsAsList = new ArrayList<Relationship>();
        relsAsList.addAll(relationships);

        try {
            template.batchUpdate(ADD_RELATION_QUERY, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    final String source = relsAsList.get(i).getSource();
                    final String target = relsAsList.get(i).getTarget();
                    final String relType = relsAsList.get(i).getType().name();
                    preparedStatement.setString(1, source);
                    preparedStatement.setString(2, target);
                    preparedStatement.setString(3, relType);
                }

                @Override
                public int getBatchSize() {
                    return relsAsList.size();
                }
            });
        } catch (DuplicateKeyException e) {
            log.debug("Attempted to add a duplicate relationship, will attempt each relationship from the batch individually.");
            for (Relationship rel : relsAsList) {
                addRelation(rel.getSource(), rel.getTarget(), rel.getType());
            }
        }
    }

    @Override
    public void removeRelation(String sourceId, String targetId, Relationship.RelType rel) {
        validate(sourceId, targetId, rel);
        executeUpdate(REMOVE_RELATION_QUERY, sourceId, targetId, rel.name());
    }

    @Override
    public void removeRelations(Set<Relationship> relationships) {
        if (relationships == null) {
            throw new IllegalArgumentException("Relationships must not be null.");
        }

        if (relationships.isEmpty()) {
            return;
        }

        final List<Relationship> relsAsList = new ArrayList<Relationship>();
        relsAsList.addAll(relationships);

        template.batchUpdate(REMOVE_RELATION_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                final String source = relsAsList.get(i).getSource();
                final String target = relsAsList.get(i).getTarget();
                final String relType = relsAsList.get(i).getType().name();
                preparedStatement.setString(1, source);
                preparedStatement.setString(2, target);
                preparedStatement.setString(3, relType);
            }

            @Override
            public int getBatchSize() {
                return relsAsList.size();
            }
        });
    }

    @Override
    public void removeAll(String id) {
        validate(id);
        executeUpdate(REMOVE_ALL_RELATION_QUERY, id, id);
    }

    @Override
    public Map<Relationship.RelType, Set<Relationship>> getRelations(final String id) {
        validate(id);
        String[] params = new String[] { id };
        normalizeParams(params);
        final Map<Relationship.RelType, Set<Relationship>> results = new HashMap<Relationship.RelType, Set<Relationship>>();

        template.query(GET_RELATION_FOR_ID_QUERY, new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                String sourceId = resultSet.getString(SOURCE_COLUMN);
                String targetId = resultSet.getString(TARGET_COLUMN);
                Relationship.RelType relType = RelType.valueOf(resultSet.getString(REL_COLUMN).toUpperCase());
                Relationship rel = new Relationship(sourceId, targetId, relType);
                Set<Relationship> relSet = null;
                if (results.containsKey(relType)) {
                    relSet = results.get(relType);
                } else {
                    relSet = new HashSet<Relationship>();
                    results.put(relType, relSet);
                }

                relSet.add(rel);
                return null;
            }
        }, params[0], params[0]);

        return results;
    }

    @Override
    public Set<Relationship> getRelations(final String id, final Relationship.RelType rel, RelEnd end){
        validate(id);
        validate(rel);
        String[] params = new String[] { id, rel.name() };
        normalizeParams(params);
        final Set<Relationship> results = new HashSet<Relationship>();

        if(null != end && end.equals(RelEnd.SOURCE)) {
              template.query(GET_RELATION_FOR_ID_AND_TYPE_QUERY_SOURCE, new RowMapper<Object>() {
                @Override
                public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                    String sourceId = resultSet.getString(SOURCE_COLUMN);
                    String targetId = resultSet.getString(TARGET_COLUMN);
                    RelType relType = RelType.valueOf(resultSet.getString(REL_COLUMN).toUpperCase());
                    results.add(new Relationship(sourceId, targetId, relType));
                    return null;
                }
            }, params[0], params[1]);
        } else if(null != end && end.equals(RelEnd.TARGET)) {
                template.query(GET_RELATION_FOR_ID_AND_TYPE_QUERY_TARGET, new RowMapper<Object>() {
                @Override
                public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                    String sourceId = resultSet.getString(SOURCE_COLUMN);
                    String targetId = resultSet.getString(TARGET_COLUMN);
                    RelType relType = RelType.valueOf(resultSet.getString(REL_COLUMN).toUpperCase());
                    results.add(new Relationship(sourceId, targetId, relType));
                    return null;
                }
           }, params[0], params[1]);
        } else { // behave like it's null
                template.query(GET_RELATION_FOR_ID_AND_TYPE_QUERY, new RowMapper<Object>() {
                @Override
                public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                    String sourceId = resultSet.getString(SOURCE_COLUMN);
                    String targetId = resultSet.getString(TARGET_COLUMN);
                    RelType relType = RelType.valueOf(resultSet.getString(REL_COLUMN).toUpperCase());
                    results.add(new Relationship(sourceId, targetId, relType));
                    return null;
                }
            }, params[0], params[0], params[1]);
        }
        return results; 
    }

    @Override
    public Set<Relationship> getRelations(final String id, final Relationship.RelType rel) {
        validate(id);
        validate(rel);
        String[] params = new String[] { id, rel.name() };
        normalizeParams(params);
        final Set<Relationship> results = new HashSet<Relationship>();

        template.query(GET_RELATION_FOR_ID_AND_TYPE_QUERY, new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                String sourceId = resultSet.getString(SOURCE_COLUMN);
                String targetId = resultSet.getString(TARGET_COLUMN);
                RelType relType = RelType.valueOf(resultSet.getString(REL_COLUMN).toUpperCase());
                results.add(new Relationship(sourceId, targetId, relType));
                return null;
            }
        }, params[0], params[0], params[1]);

        return results;
    }

    @Override
    public Iterator<Relationship> iterator() {
        return new JdbcRowIterator(template);
    }

    private void executeUpdate(String query, String... params) {
        normalizeParams(params);
        template.update(query, params);
    }

    private void normalizeParams(String[] params) {
        if (params == null) {
            throw new IllegalArgumentException("Cannot normalize null parameters");
        }
    }

    private void validate(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Relationship identifier must not be empty or null.");
        }
    }

    private void validate(Relationship.RelType rel) {
        if (rel == null) {
            throw new IllegalArgumentException("Relationship type must not be null.");
        }
    }

    private void validate(String sourceId, String targetId, Relationship.RelType rel) {
        if (sourceId == null || sourceId.isEmpty()) {
            throw new IllegalArgumentException("Source relationship identifier must not be empty or null.");
        }

        if (targetId == null || targetId.isEmpty()) {
            throw new IllegalArgumentException("Target relationship identifier must not be empty or null.");
        }

        if (rel == null) {
            throw new IllegalArgumentException("Relationship type must not be null.");
        }
    }
}

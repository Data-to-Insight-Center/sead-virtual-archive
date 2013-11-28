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

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.ui.model.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import static org.dataconservancy.ui.dao.RelationshipDAOJdbcImpl.REL_COLUMN;
import static org.dataconservancy.ui.dao.RelationshipDAOJdbcImpl.REL_TABLE;
import static org.dataconservancy.ui.dao.RelationshipDAOJdbcImpl.SOURCE_COLUMN;
import static org.dataconservancy.ui.dao.RelationshipDAOJdbcImpl.TARGET_COLUMN;
import static org.dataconservancy.ui.model.Relationship.RelType.ACCEPTS_DEPOSIT;
import static org.dataconservancy.ui.model.Relationship.RelType.AGGREGATES;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_AGGREGATED_BY;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_DEPOSITOR_FOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class RelationshipDAOJdbcImplTest extends BaseDaoTest {

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate template;

    private RelationshipDAOJdbcImpl underTest;

    static final String DELETE_ALL_ROWS_QUERY =
            "DELETE FROM " + REL_TABLE;

    static final String ROW_COUNT_QUERY =
            "SELECT count(*) FROM " + REL_TABLE;

    static final String SELECT_ALL_QUERY =
            "SELECT * FROM " + REL_TABLE;

    /**
     * Creates the database table and asserts that there are no rows in the table.
     */
    @Before
    public void setUp() {
        template.execute(DELETE_ALL_ROWS_QUERY);
        underTest = new RelationshipDAOJdbcImpl(template);
        assertEquals(0, template.queryForInt(ROW_COUNT_QUERY));
    }

    @After
    public void tearDown() {
        template.execute(DELETE_ALL_ROWS_QUERY);
    }

    /**
     * Tests that the added relationship is in fact added.
     */
    @Test
    public void testAddRelation() throws Exception {
        final String source = "foo";
        final String target = "bar";
        final Relationship.RelType rel = IS_DEPOSITOR_FOR;
        underTest.addRelation(source, target, rel);
        assertEquals(1, template.queryForInt(ROW_COUNT_QUERY));
        template.query(SELECT_ALL_QUERY, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                assertEquals(source, resultSet.getString(SOURCE_COLUMN));
                assertEquals(target, resultSet.getString(TARGET_COLUMN));
                assertEquals(rel.name(), resultSet.getString(REL_COLUMN));
            }
        });
    }

    /**
     * Tests that when adding a Set of Relationships, they are all added.
     */
    @Test
    public void testAddRelations() throws Exception {
        final Set<Relationship> rels = new HashSet<Relationship>();
        final Relationship rel1 = new Relationship("foo", "bar", AGGREGATES);
        final Relationship rel2 = new Relationship("catty", "wumpus", AGGREGATES);
        rels.add(rel1);
        rels.add(rel2);

        underTest.addRelations(rels);
        assertEquals(2, template.queryForInt(ROW_COUNT_QUERY));

        template.query(SELECT_ALL_QUERY, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                Relationship r = new Relationship(resultSet.getString(1), resultSet.getString(2),
                        Relationship.RelType.valueOf(resultSet.getString(3)));
                assertTrue(rels.contains(r));
            }
        });
    }

    /**
     * Tests that when adding a set of relationships, where at least one relationship of the set
     * already exists in the relationship table:
     * <ul>
     *     <li>that the non-duplicate relationships are added</li>
     *     <li>that no exception is thrown when adding the duplicate</li>
     * </ul>
     *
     *
     * @throws Exception
     */
    @Test
    public void testAddSameRelationsTwice() throws Exception {
        // A unique relationship to be added in the subsequent addRelations(...) call
        final Relationship relToAdd = new Relationship("baz", "biz", AGGREGATES);

        // Verify relToAdd doesn't exist
        assertTrue(underTest.getRelations(relToAdd.getTarget()).isEmpty());
        assertTrue(underTest.getRelations(relToAdd.getSource()).isEmpty());
        assertEquals(0, template.queryForInt(ROW_COUNT_QUERY));

        // A relationship that will be duplicated in a subsequent addRelations(...) call.
        final Relationship relToDuplicate = new Relationship("foo", "bar", AGGREGATES);

        // Seed (and verify) the relations table with the relToDuplicate
        underTest.addRelation(relToDuplicate.getSource(), relToDuplicate.getTarget(), relToDuplicate.getType());
        assertEquals(1, template.queryForInt(ROW_COUNT_QUERY));
        template.query(SELECT_ALL_QUERY, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                Relationship r = new Relationship(resultSet.getString(1), resultSet.getString(2),
                        Relationship.RelType.valueOf(resultSet.getString(3)));
                assertEquals(r, relToDuplicate);
            }
        });

        // Create a set of Relationships to add, be sure to include 'relToDuplicate'
        final Set<Relationship> rels = new HashSet<Relationship>();
        rels.add(relToDuplicate);
        rels.add(relToAdd);

        // Now add the Relationships
        underTest.addRelations(rels);

        // Verify that the duplicate rel wasn't added, and that the new rel was added
        assertEquals(2, template.queryForInt(ROW_COUNT_QUERY));
        template.query(SELECT_ALL_QUERY, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                Relationship r = new Relationship(resultSet.getString(1), resultSet.getString(2),
                        Relationship.RelType.valueOf(resultSet.getString(3)));
                assertTrue(r.equals(relToAdd) || r.equals(relToDuplicate));
            }
        });
    }

    /**
     * Tests that when adding a relationship that already exists:
     * <ul>
     * <li>The duplicate is in fact <em>not</em> added</li>
     * <li>No exception is thrown</li>
     * </ul>
     */
    @Test
    public void testAddSameRelationTwice() throws Exception {
        final Relationship rel = new Relationship("foo", "bar", Relationship.RelType.IS_ADMINISTERED_BY);

        underTest.addRelation(rel.getSource(), rel.getTarget(), rel.getType());
        underTest.addRelation(rel.getSource(), rel.getTarget(), rel.getType());

        assertEquals(1, template.queryForInt(ROW_COUNT_QUERY));

        template.query(SELECT_ALL_QUERY, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                assertEquals(rel.getSource(), resultSet.getString(SOURCE_COLUMN));
                assertEquals(rel.getTarget(), resultSet.getString(TARGET_COLUMN));
                assertEquals(rel.getType().name(), resultSet.getString(REL_COLUMN));
            }
        });
    }

    /**
     * Tests that the removed relationship is in fact removed.
     */
    @Test
    public void testRemoveRelation() throws Exception {
        final String source = "foo";
        final String target = "bar";
        final Relationship.RelType rel = IS_DEPOSITOR_FOR;
        underTest.addRelation(source, target, rel);
        underTest.addRelation(target, source, ACCEPTS_DEPOSIT);
        assertEquals(2, template.queryForInt(ROW_COUNT_QUERY));

        underTest.removeRelation(source, target, rel);
        assertEquals(1, template.queryForInt(ROW_COUNT_QUERY));
        template.query(SELECT_ALL_QUERY, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                assertEquals(target, resultSet.getString(SOURCE_COLUMN));
                assertEquals(source, resultSet.getString(TARGET_COLUMN));
                assertEquals(ACCEPTS_DEPOSIT.name(), resultSet.getString(REL_COLUMN));
            }
        });
    }

    /**
     * Tests that when removing the same relationship twice, no exception is thrown.
     */
    @Test
    public void testRemoveSameRelationTwice() throws Exception {
        final Relationship rel = new Relationship("foo", "bar", Relationship.RelType.AGGREGATES);
        underTest.addRelation(rel.getSource(), rel.getTarget(), rel.getType());

        assertEquals(1, template.queryForInt(ROW_COUNT_QUERY));

        underTest.removeRelation(rel.getSource(), rel.getTarget(), rel.getType());
        assertEquals(0, template.queryForInt(ROW_COUNT_QUERY));

        underTest.removeRelation(rel.getSource(), rel.getTarget(), rel.getType());
        assertEquals(0, template.queryForInt(ROW_COUNT_QUERY));
    }

    /**
     * Tests that all of the specified relationships are removed.
     */
    @Test
    public void testRemoveAll() throws Exception {
        final String id1 = "foo";
        final String id2 = "bar";
        final String id3 = "baz";
        final Relationship.RelType rel = IS_DEPOSITOR_FOR;
        final Relationship.RelType inv = ACCEPTS_DEPOSIT;

        underTest.addRelation(id1, id2, rel);
        underTest.addRelation(id2, id1, inv);
        underTest.addRelation(id2, id3, inv);

        assertEquals(3, template.queryForInt(ROW_COUNT_QUERY));
        underTest.removeAll(id1);
        assertEquals(1, template.queryForInt(ROW_COUNT_QUERY));
    }

    /**
     * Tests that when removing a Set of Relationships, they are all removed.
     */
    @Test
    public void testRemoveRelations() throws Exception {
        final Set<Relationship> rels = new HashSet<Relationship>();
        final Relationship rel1 = new Relationship("foo", "bar", AGGREGATES);
        final Relationship rel2 = new Relationship("catty", "wumpus", AGGREGATES);
        rels.add(rel1);
        rels.add(rel2);

        underTest.addRelations(rels);
        assertEquals(2, template.queryForInt(ROW_COUNT_QUERY));

        underTest.removeRelations(rels);
        assertEquals(0, template.queryForInt(ROW_COUNT_QUERY));
    }

    /**
     * Tests that all of the specified relationships for an id are returned.
     */
    @Test
    public void testGetRelationsForId() throws Exception {
        final String id1 = "foo";
        final String id2 = "bar";
        final String id3 = "baz";
        final Relationship.RelType rel = IS_DEPOSITOR_FOR;
        final Relationship.RelType inv = ACCEPTS_DEPOSIT;

        underTest.addRelation(id1, id2, rel);
        underTest.addRelation(id2, id1, inv);
        underTest.addRelation(id2, id3, inv);
        assertEquals(3, template.queryForInt(ROW_COUNT_QUERY));

        Map<Relationship.RelType, Set<Relationship>> rels = underTest.getRelations(id2);
        assertNotNull(rels);
        assertEquals(2, rels.size());
        assertTrue(rels.containsKey(IS_DEPOSITOR_FOR));
        assertTrue(rels.containsKey(ACCEPTS_DEPOSIT));
        assertEquals(1, rels.get(IS_DEPOSITOR_FOR).size());
        assertEquals(2, rels.get(ACCEPTS_DEPOSIT).size());

        final Relationship expectedRel1 = new Relationship(id1, id2, rel);
        final Relationship expectedRel2 = new Relationship(id2, id1, inv);
        final Relationship expectedRel3 = new Relationship(id2, id3, inv);

        assertTrue(rels.get(IS_DEPOSITOR_FOR).contains(expectedRel1));
        assertTrue(rels.get(ACCEPTS_DEPOSIT).contains(expectedRel2));
        assertTrue(rels.get(ACCEPTS_DEPOSIT).contains(expectedRel3));
    }

    /**
     * Tests that all of the specified relationships for an id and type are returned.
     */
    @Test
    public void testGetRelationsForIdAndType() throws Exception {
        final String id1 = "foo";
        final String id2 = "bar";
        final String id3 = "baz";
        final Relationship.RelType rel = IS_DEPOSITOR_FOR;
        final Relationship.RelType inv = ACCEPTS_DEPOSIT;

        underTest.addRelation(id1, id2, rel);
        underTest.addRelation(id2, id1, inv);
        underTest.addRelation(id2, id3, inv);
        assertEquals(3, template.queryForInt(ROW_COUNT_QUERY));

        Set<Relationship> rels = underTest.getRelations(id2, IS_DEPOSITOR_FOR);
        assertNotNull(rels);
        assertEquals(1, rels.size());

        final Relationship expectedRel1 = new Relationship(id1, id2, rel);
        assertEquals(expectedRel1, rels.iterator().next());
    }

    /**
     * Tests that "directed" relationship service test works
     */
    @Test
    public void testGetRelationsForIdAndTypeAndEnd() throws Exception {
        final String id1 = "foo";
        final String id2 = "bar";
        final String id3 = "baz"; 
        final Relationship.RelType rel = AGGREGATES;
        final Relationship.RelType inv = IS_AGGREGATED_BY;

        //models project aggregating collection aggregating dataset
        underTest.addRelation(id1, id2, rel);
        underTest.addRelation(id2, id1, inv);
        underTest.addRelation(id2, id3, rel);
        underTest.addRelation(id3, id2, inv);

        assertEquals(4, template.queryForInt(ROW_COUNT_QUERY));

        //models get dataset for collection
        Set<Relationship> rels = underTest.getRelations(id2, AGGREGATES, RelationshipDAO.RelEnd.SOURCE);
        assertNotNull(rels);
        assertEquals(1, rels.size());

        final Relationship expectedRel1 = new Relationship(id2, id3, rel);
        assertEquals(expectedRel1, rels.iterator().next());

       //models get project for dataset
        Set<Relationship> rels1 = underTest.getRelations(id2, AGGREGATES, RelationshipDAO.RelEnd.TARGET);
        assertNotNull(rels1);
        assertEquals(1, rels1.size());

        final Relationship expectedRel2 = new Relationship(id1, id2, rel);
        assertEquals(expectedRel2, rels1.iterator().next());
        
        //check that null end behaves as it should (undirected)
        Set<Relationship> rels2 = underTest.getRelations(id2, AGGREGATES, null);
        assertNotNull(rels2);
        assertEquals(2, rels2.size());

     }
}

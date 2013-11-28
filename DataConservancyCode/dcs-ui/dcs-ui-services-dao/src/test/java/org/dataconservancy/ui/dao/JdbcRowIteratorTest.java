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

import java.util.NoSuchElementException;

import org.dataconservancy.ui.test.support.BaseSpringAwareTest;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.dataconservancy.ui.dao.RelationshipDAOJdbcImpl.REL_TABLE;
import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.dataconservancy.ui.model.Relationship.RelType.ACCEPTS_DEPOSIT;
import static org.dataconservancy.ui.model.Relationship.RelType.AGGREGATES;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_ADMINISTERED_BY;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_ADMINISTRATOR_FOR;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_AGGREGATED_BY;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_DEPOSITOR_FOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class JdbcRowIteratorTest extends BaseDaoTest {

    @Autowired
    private JdbcTemplate template;

    static final String DELETE_ALL_ROWS_QUERY =
            "DELETE FROM " + REL_TABLE;
    
    private RelationshipDAOJdbcImpl dao;

    @Before
    public void setUp() {
        // Verify assumptions
        template.execute(DELETE_ALL_ROWS_QUERY);
        assertEquals(0, template.queryForInt(RelationshipDAOJdbcImplTest.ROW_COUNT_QUERY));

        dao = new RelationshipDAOJdbcImpl(template);

        // Seed the dao with six relationships
        dao.addRelation("foo", "bar", AGGREGATES);
        dao.addRelation("bar", "foo", IS_AGGREGATED_BY);
        dao.addRelation("baz", "bar", IS_ADMINISTERED_BY);
        dao.addRelation("bar", "baz", IS_ADMINISTRATOR_FOR);
        dao.addRelation("unf", "bunf", IS_DEPOSITOR_FOR);
        dao.addRelation("bunf", "unf", ACCEPTS_DEPOSIT);

        // Verify assumptions
        assertEquals(6, template.queryForInt(RelationshipDAOJdbcImplTest.ROW_COUNT_QUERY));
    }


    @Test
    public void testSimple() throws Exception {
        int step = 1;
        int max = 5;
        int offset = 0;

        JdbcRowIterator underTest = new JdbcRowIterator(template, offset, step, max);
        assertTrue(underTest.hasNext());

        int count = 0;
        while (underTest.hasNext()) {
            underTest.next();
            count++;
            // avoid the NoSuchElementException
            if (count == max) {
                break;
            }
        }

        assertEquals(max, count);
    }

    @Test
    public void testWithEvenStep() {
        int step = 2;
        int offset = 0;
        int max = 5;

        JdbcRowIterator underTest = new JdbcRowIterator(template, offset, step, max);
        assertTrue(underTest.hasNext());

        int count = 0;
        while (underTest.hasNext()) {
            underTest.next();
            count++;
            // avoid the NoSuchElementException
            if (count == max) {
                break;
            }
        }

        assertEquals(max, count);
    }

    @Test
    public void testWithStepEqualToMax() {
        int step = 5;
        int max = step;
        int offset = 0;

        JdbcRowIterator underTest = new JdbcRowIterator(template, offset, step, max);
        assertTrue(underTest.hasNext());

        int count = 0;
        while (underTest.hasNext()) {
            underTest.next();
            count++;
            // avoid the NoSuchElementException
            if (count == max) {
                break;
            }
        }

        assertEquals(max, count);
    }

    @Test
    public void testWithStepGreaterThanMax() {
        int step = 10;
        int max = 5;
        int offset = 0;

        JdbcRowIterator underTest = new JdbcRowIterator(template, offset, step, max);
        assertTrue(underTest.hasNext());

        int count = 0;
        while (underTest.hasNext()) {
            underTest.next();
            count++;
            // avoid the NoSuchElementException
            if (count == max) {
                break;
            }
        }

        assertEquals(max, count);
    }

    @Test(expected = NoSuchElementException.class)
    public void testNoSuchElementExceptionWithEmptyIterator() {
        int step = 1;
        int max = 0;
        int offset = 0;

        JdbcRowIterator underTest = new JdbcRowIterator(template, offset, step, max);
        underTest.next();
    }


    @Test
    public void testNoSuchElementException() {
        int step = 1;
        int max = 5;
        int offset = 0;

        JdbcRowIterator underTest = new JdbcRowIterator(template, offset, step, max);

        for (int count = 0; count < max; count++) {
            underTest.next();
        }

        assertFalse(underTest.hasNext());
        try {
            underTest.next();
            fail("Expected a NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

}

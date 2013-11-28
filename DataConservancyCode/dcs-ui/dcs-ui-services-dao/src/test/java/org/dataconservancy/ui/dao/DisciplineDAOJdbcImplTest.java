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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.dataconservancy.ui.test.support.BaseSpringAwareTest;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.model.Discipline;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class DisciplineDAOJdbcImplTest extends BaseDaoTest {
    private static final String TABLE = "DISCIPLINE";

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate template;

    private DisciplineDAOJdbcImpl disciplineDao;

    @Before
    public void setUp() {
        template.execute("DELETE FROM " + TABLE);
        disciplineDao = new DisciplineDAOJdbcImpl(template);
    }

    @After
    public void tearDown() {
        template.execute("DELETE FROM " + TABLE);
    }

    @Test
    public void testAdd() {
        Discipline dis = new Discipline();
        dis.setIdentifier("id:cow");
        dis.setTitle("Moo!");

        disciplineDao.add(dis);

        String query = "SELECT * FROM " + TABLE;
        List<Discipline> result = (List<Discipline>) template.query(query, new Object[] {},
                new DisciplineRowMapper());
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(dis));
    }

    @Test
    public void testList() {
        List<Discipline> result = disciplineDao.list();

        assertNotNull(result);
        assertEquals(0, result.size());
        
        Discipline dis = new Discipline();
        dis.setIdentifier("id:cow");
        dis.setTitle("Moo!");

        disciplineDao.add(dis);
        
        result = disciplineDao.list();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(dis));
    }

    @Test
    public void testGet() {
        String title = "Title Foo";
        String id = "id:foo";
        Discipline expectedDiscipline = new Discipline(title, id);
        template.execute("INSERT INTO " + TABLE + " VALUES ('" + id +"', '" + title + "')");
        assertEquals(1, template.queryForInt("SELECT count(*) FROM " + TABLE));

        assertEquals(expectedDiscipline, disciplineDao.get(id));
    }

    @Test
    public void testGetNonExistentDiscipline() {
        assertNull(disciplineDao.get("non-existent discipline id"));
    }
}

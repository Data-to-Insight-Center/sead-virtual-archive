/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.registry.dao.jdbc.impl;

import com.sun.jersey.test.framework.JerseyTest;
import org.junit.Test;
import org.seadva.registry.dao.EntityDao;

import static junit.framework.Assert.assertNotNull;

/**
 * Database dao implmentation test cases
 */
public class EntityJdbcImplTest extends JerseyTest {

    public EntityJdbcImplTest() throws Exception {
        super("org.seadva.registry.dao.jdbc.impl");

    }


    @Test
    public void testEntityPut() throws Exception {
        EntityJdbcDaoImpl entityJdbc = new EntityJdbcDaoImpl();
        EntityDao entity = new EntityDao();
        String entity_id = "http://seadva-test/"+ "test2/";
        entity.setEntity_id(entity_id);
        entity.setEntity_name("Test collection");


        entityJdbc.insertEntity(entity);
    }

    @Test
    public void testGetEntity() throws Exception {
        String entity_id ="http://seada-test/"+"test2/";
        EntityJdbcDaoImpl entityJdbc = new EntityJdbcDaoImpl();
        EntityDao entity = entityJdbc.getEntity(entity_id);
        assertNotNull(entity);
    }

}

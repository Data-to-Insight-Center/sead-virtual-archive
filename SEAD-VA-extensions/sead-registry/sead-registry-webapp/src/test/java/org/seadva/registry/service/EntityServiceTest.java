/*
 * Copyright 2014 The Trustees of Indiana University
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

package org.seadva.registry.service;

import com.sun.jersey.test.framework.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.seadva.registry.impl.registry.BaseDaoImpl;
import org.seadva.registry.impl.registry.SeadRegistry;
import org.seadva.registry.service.ResourceService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static junit.framework.Assert.assertNotNull;

/**
 * Handler test cases
 */
public class EntityServiceTest extends JerseyTest {

    public EntityServiceTest() throws Exception {
        super("org.seadva.registry.service");

    }

    @Before
    public void init() throws IllegalAccessException, InstantiationException {
        ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext(new String[] {
                        "testContext.xml"});
        ResourceService.seadRegistry = new SeadRegistry((BaseDaoImpl) appContext.getBean("registry"));
        ResourceService.seadRegistry.init();
    }
    SeadRegistry seadRegistry;



    @Test
    public void testGetCollection() throws Exception {
        ResourceService resourceService = new ResourceService();
        assertNotNull(resourceService.getEntity("http://seada-test/" + "test_coll_id"));
    }

}

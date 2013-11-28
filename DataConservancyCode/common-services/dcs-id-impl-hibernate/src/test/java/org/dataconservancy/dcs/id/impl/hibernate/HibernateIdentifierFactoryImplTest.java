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
package org.dataconservancy.dcs.id.impl.hibernate;

import java.net.URL;

import org.dataconservancy.dcs.id.api.Types;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.Identifier;

import org.springframework.context.support.ClassPathXmlApplicationContext;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HibernateIdentifierFactoryImplTest {

    private static HibernateIdentifierFactory idFactory;

    @BeforeClass
    public static void setUp() {
        ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext("applicationContext.xml");
        idFactory = (HibernateIdentifierFactory) appContext.getBean("identifierFactory");
    }

    @Test
    public void testCreateIdentifierWithExistingPersistentId() {
        String urlPrefix = "http://fakeURL/";
        TypeInfo existingPersistentId = new TypeInfo();
        existingPersistentId.setType("unknown type");
        Identifier identifier = idFactory.createIdentifierWithExistingPersistentId(urlPrefix, existingPersistentId);

        assertEquals(Long.toString(existingPersistentId.getId()), identifier.getUid());
        assertEquals(existingPersistentId.getType(), identifier.getType());
        assertTrue(identifier.getUrl().toString().contains(idFactory.getUrlEntityTypePrefix() + Long.toString(existingPersistentId.getId())));
    }

    @Test
    public void testDifferentPrefixTypes() {
        Identifier entityId = idFactory.createNewIdentifier("http://fakeURL/", Types.DELIVERABLE_UNIT.getTypeName());
        Identifier lineageId = idFactory.createNewIdentifier("http://fakeURL/", Types.LINEAGE.getTypeName());

        assertTrue(entityId.getUrl().toString().contains(idFactory.getUrlEntityTypePrefix()));
        assertTrue(lineageId.getUrl().toString().contains(idFactory.getUrlLineageTypePrefix()));
    }

}


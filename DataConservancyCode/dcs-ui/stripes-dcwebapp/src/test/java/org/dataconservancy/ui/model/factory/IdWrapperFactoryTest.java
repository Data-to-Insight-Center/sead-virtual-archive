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
package org.dataconservancy.ui.model.factory;

import java.net.URL;

import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.impl.hibernate.HibernateIdentifierFactory;
import org.dataconservancy.ui.BaseUnitTest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.Identifier;

import org.springframework.context.support.ClassPathXmlApplicationContext;


import static org.junit.Assert.assertTrue;

@Ignore("TODO: fix to work with a SpringJUnit4Runner")
public class IdWrapperFactoryTest extends BaseUnitTest {

    private static HibernateIdentifierFactory idFactory;

    @BeforeClass
    public static void setUp() {
        ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext("applicationContext.xml");
        idFactory = (HibernateIdentifierFactory) appContext.getBean("uiIdentifierFactory");

    }

    @Test
    public void testDifferentPrefixTypes() throws Exception {
        String urlPrefix = "http://fakeURL/";
        Identifier entityId = idFactory.createNewIdentifier(urlPrefix, Types.DELIVERABLE_UNIT.getTypeName());
        Identifier lineageId = idFactory.createNewIdentifier(urlPrefix, Types.LINEAGE.getTypeName());
        Identifier projectId = idFactory.createNewIdentifier(urlPrefix, Types.PROJECT.name());
        Identifier personId = idFactory.createNewIdentifier(urlPrefix, Types.PERSON.name());
        Identifier dataSetId = idFactory.createNewIdentifier(urlPrefix, Types.DATA_SET.name());
        Identifier dataFileId = idFactory.createNewIdentifier(urlPrefix, Types.DATA_FILE.name());
        Identifier collectionId = idFactory.createNewIdentifier(urlPrefix, Types.COLLECTION.name());
        Identifier metadataFileId = idFactory.createNewIdentifier(urlPrefix, Types.METADATA_FILE.name());

        assertTrue(entityId.getUrl().toString().contains(idFactory.getUrlEntityTypePrefix()));
        assertTrue(lineageId.getUrl().toString().contains(idFactory.getUrlLineageTypePrefix()));
        assertTrue(projectId.getUrl().toString().contains(urlPrefix + "project/"));
        assertTrue(personId.getUrl().toString().contains(urlPrefix + "person/"));
        assertTrue(dataSetId.getUrl().toString().contains(urlPrefix + "item/"));
        assertTrue(dataFileId.getUrl().toString().contains(urlPrefix + "file/"));
        assertTrue(collectionId.getUrl().toString().contains(urlPrefix + "collection/"));
        assertTrue(metadataFileId.getUrl().toString().contains(urlPrefix + "file/"));
    }

}


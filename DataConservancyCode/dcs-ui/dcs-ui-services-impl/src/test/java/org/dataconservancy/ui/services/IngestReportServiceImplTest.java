/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.ui.services;

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_CLASS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.reporting.model.IngestReport;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesDatabase(AFTER_CLASS)
@DirtiesContext
public class IngestReportServiceImplTest extends BaseUnitTest {
    
    private Project project;
    private Collection parentCollectionOne;
    private Collection parentCollectionTwo;
    private Collection childCollectionOne;
    private Collection childCollectionTwo;
    private Collection childCollectionThree;
    
    private Map<String, Integer> dataItemsPerCollection;

    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;
    
    @Autowired
    @Qualifier("ingestReportService")
    private IngestReportService underTest;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private CollectionBizService collectionBizService;

    @Before
    public void setUp() throws RelationshipException, BizPolicyException, BizInternalException {

        project = new Project();
        project.setId(idService.create(Types.PROJECT.name()).getUrl().toString());
        project.setName("Test Project");
        project.setStartDate(new DateTime(20120101));
        project.setEndDate(new DateTime(20120501));
        projectService.create(project);

        parentCollectionOne = new Collection();
        parentCollectionOne.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
        parentCollectionOne.setTitle("Parent Collection 1");
        parentCollectionOne.setSummary("Some summary");
        parentCollectionOne.setParentProjectId(project.getId());
        collectionBizService.createCollection(parentCollectionOne, admin);
        
        parentCollectionTwo = new Collection();
        parentCollectionTwo.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
        parentCollectionTwo.setTitle("Parent Collection 2");
        parentCollectionTwo.setSummary("Some summary");
        parentCollectionTwo.setParentProjectId(project.getId());
        collectionBizService.createCollection(parentCollectionTwo, admin);
        
        childCollectionOne = new Collection();
        childCollectionOne.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
        childCollectionOne.setTitle("Child Collection 1");
        childCollectionOne.setSummary("Some summary");
        childCollectionOne.setParentProjectId(project.getId());
        collectionBizService.createCollection(childCollectionOne, admin);
        
        childCollectionTwo = new Collection();
        childCollectionTwo.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
        childCollectionTwo.setTitle("Child Collection 2");
        childCollectionTwo.setSummary("Some summary");
        childCollectionTwo.setParentProjectId(project.getId());
        collectionBizService.createCollection(childCollectionTwo, admin);
        
        childCollectionThree = new Collection();
        childCollectionThree.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
        childCollectionThree.setTitle("Child Collection 3");
        childCollectionThree.setSummary("Some summary");
        childCollectionThree.setParentProjectId(project.getId());
        collectionBizService.createCollection(childCollectionThree, admin);

        dataItemsPerCollection = new HashMap<String, Integer>();
        dataItemsPerCollection.put(parentCollectionOne.getId(), 6);
        dataItemsPerCollection.put(parentCollectionTwo.getId(), 12);
        dataItemsPerCollection.put(childCollectionOne.getId(), 3);
        dataItemsPerCollection.put(childCollectionTwo.getId(), 0);
        dataItemsPerCollection.put(childCollectionThree.getId(), 3);
        dataItemsPerCollection.put("Test Collection 1", 35);
        dataItemsPerCollection.put("Test Collection 2", 25);
        dataItemsPerCollection.put("Unknown title for collection with id '" + childCollectionThree.getId() + "'", 25);
    }
    
    @Test
    public void testGetDataItemsPerCollectionCount() {
        IngestReport ingestReport = new IngestReport();
        ingestReport.setDataItemsPerCollectionCount(dataItemsPerCollection);
        
        Map<String, Integer> dataItemsPerCollectionCount = underTest.getDataItemsPerCollectionCount(ingestReport);
        Assert.assertNotNull(dataItemsPerCollectionCount);
        Assert.assertEquals(8, dataItemsPerCollectionCount.size());
        
        List<String> existingCollections = new ArrayList<String>();
        List<String> newCollections = new ArrayList<String>();
        for (String key : dataItemsPerCollectionCount.keySet()) {
            if (key.contains("Existing")) {
                existingCollections.add(key);
            }
            else if (key.contains("New")) {
                newCollections.add(key);
            }
        }
        Assert.assertEquals(5, existingCollections.size());
        Assert.assertEquals(3, newCollections.size());
    }

}

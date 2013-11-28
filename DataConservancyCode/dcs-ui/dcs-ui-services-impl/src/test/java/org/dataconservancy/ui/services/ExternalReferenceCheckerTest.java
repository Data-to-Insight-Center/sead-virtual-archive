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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;

import junit.framework.Assert;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.DataItemBizService;
import org.dataconservancy.ui.services.ExternalReferenceChecker;
import org.dataconservancy.ui.services.MetadataFileBizService;
import org.dataconservancy.ui.services.ProjectBizService;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.junit.Before;
import org.junit.Test;

public class ExternalReferenceCheckerTest extends BaseReferenceCheckerTest {
    
    private ExternalReferenceChecker underTest;
    private CollectionBizService collectionBizService;
    private ProjectBizService projectBizService;
    private DataItemBizService dataItemBizService;
    private MetadataFileBizService metadataFileBizService;
    private IdService idService;

    private final String packageId = "http://package1";
    private final String projectId = "http://project1";
    private final String collectionId = "http://collection1";
    private final String dataItemId = "http://dataItem1";
    private final String metadataFileId = "http://metadataFile1";
    private final String subCollectionId = "http://subCollection1";
    private final String versionDataItemId = "http://versionDataItem1";
    private final String collectionOnlyFileId = "http://collectionOnlyFile1";
    private final String dataItemOnlyMetadataFileId = "http://dataItemOnlyFile1";
    private final String fileCollectionMetadataId = "http://fileCollectionMetadata1";
    private final String fileDataItemMetadataId = "http://fileDataItemMetadata1";

    @Before
    public void setup() throws Exception {
        super.setup();
        underTest = new ExternalReferenceChecker();
        collectionBizService = mock(CollectionBizService.class);
        projectBizService = mock(ProjectBizService.class);
        dataItemBizService = mock(DataItemBizService.class);
        metadataFileBizService = mock(MetadataFileBizService.class);
        idService = mock(IdService.class);
        
        underTest.setProjectBizService(projectBizService);
        underTest.setCollectionBizService(collectionBizService);
        underTest.setDataItemBizService(dataItemBizService);
        underTest.setMetadataFileBizService(metadataFileBizService);
        underTest.setIdService(idService);
        
        Attribute packageAggregatesProject = new AttributeImpl("Package-Aggregates-Project", "String", projectId);
        packageAttributeSet.getAttributes().add(packageAggregatesProject);
        Attribute packageAggregatesCollection = new AttributeImpl("Package-Aggregates-Collection", "String",
                collectionId);
        packageAttributeSet.getAttributes().add(packageAggregatesCollection);
        Attribute packageAggregatesDataItem = new AttributeImpl("Package-Aggregates-DataItem", "String", dataItemId);
        packageAttributeSet.getAttributes().add(packageAggregatesDataItem);
        Attribute packageAggregatesFile = new AttributeImpl("Package-Aggregates-File", "String", metadataFileId);
        packageAttributeSet.getAttributes().add(packageAggregatesFile);
        
        Attribute projectAggregatesCollection = new AttributeImpl("Project-Aggregates-Collection", "String",
                collectionId);
        projectAttributeSet.getAttributes().add(projectAggregatesCollection);
        Attribute projectAggregatesFile = new AttributeImpl("Project-Aggregates-File", "String", metadataFileId);
        projectAttributeSet.getAttributes().add(projectAggregatesFile);
        
        Attribute collectionAggregatesCollection = new AttributeImpl("Collection-Aggregates-Collection", "String",
                subCollectionId);
        collectionAttributeSet.getAttributes().add(collectionAggregatesCollection);
        Attribute collectionAggregatesDataItem = new AttributeImpl("Collection-Aggregates-DataItem", "String",
                dataItemId);
        collectionAttributeSet.getAttributes().add(collectionAggregatesDataItem);
        Attribute collectionAggregatesFile = new AttributeImpl("Collection-Aggregates-File", "String", metadataFileId);
        collectionAttributeSet.getAttributes().add(collectionAggregatesFile);
        Attribute collectionAggregatedByProject = new AttributeImpl("Collection-Aggregated-By-Project", "String",
                projectId);
        collectionAttributeSet.getAttributes().add(collectionAggregatedByProject);
        Attribute collectionOnlyAggregatedFile = new AttributeImpl("Collection-Aggregates-File", "String",
                collectionOnlyFileId);
        collectionAttributeSet.getAttributes().add(collectionOnlyAggregatedFile);
        
        Attribute collectionIsPartOfCollection = new AttributeImpl("Collection-IsPartOf-Collection", "String",
                collectionId);
        subCollectionAttributeSet.getAttributes().add(collectionIsPartOfCollection);
        
        Attribute dataItemAggregatesFile = new AttributeImpl("DataItem-Aggregates-File", "String", metadataFileId);
        dataItemAttributeSet.getAttributes().add(dataItemAggregatesFile);
        Attribute dataItemAggregatedByCollection = new AttributeImpl("DataItem-IsPartOf-Collection", "String",
                collectionId);
        dataItemAttributeSet.getAttributes().add(dataItemAggregatedByCollection);
        Attribute dataItemOnlyAggregatedFile = new AttributeImpl("DataItem-Aggregates-File", "String",
                dataItemOnlyMetadataFileId);
        dataItemAttributeSet.getAttributes().add(dataItemOnlyAggregatedFile);
        
        Attribute dataItemIsVersionOf = new AttributeImpl("DataItem-IsVersionOf-DataItem", "String", dataItemId);
        versionedDataItemAttributeSet.getAttributes().add(dataItemIsVersionOf);
        
        Attribute fileIsMetadataForProject = new AttributeImpl("File-IsMetadata-For", "String", projectId);
        fileAttributeSet.getAttributes().add(fileIsMetadataForProject);
        Attribute fileIsMetadataForCollection = new AttributeImpl("File-IsMetadata-For", "String",
                fileCollectionMetadataId);
        fileAttributeSet.getAttributes().add(fileIsMetadataForCollection);
        Attribute fileIsMetadataForDataItem = new AttributeImpl("File-IsMetadata-For", "String", fileDataItemMetadataId);
        fileAttributeSet.getAttributes().add(fileIsMetadataForDataItem);
        
        doAnswer(new Answer<Identifier>() {

            @Override
            public Identifier answer(InvocationOnMock invocation)
                    throws Throwable {
             // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the url of the id to be retrieved", args);
                Assert.assertEquals("Expected one argument: the url of the id to be retrieved",
                          1, args.length);
                assertTrue("Expected argument to be of type URL",
                                  args[0] instanceof URL);
                URL idURL = (URL) args[0];
                Identifier id = null;
                if (idURL.toExternalForm().contains("project")) {
                    id = new IdentifierImpl(Types.PROJECT.name(), idURL.toString());
                } else if (idURL.toExternalForm().contains("collection")){
                    id = new IdentifierImpl(Types.COLLECTION.name(), idURL.toString());
                }
                    
                return id;
            }
             
         }).when(idService).fromUrl(any(URL.class));

    }
    
    @Test
    public void testExternalReferencePass() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {

        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(project);

        Collection collection = new Collection();
        collection.setId(collectionId);
        when(collectionBizService.getCollection(collectionId)).thenReturn(collection);
        
        Collection subCollection = new Collection();
        subCollection.setId(subCollectionId);
        when(collectionBizService.getCollection(subCollectionId)).thenReturn(subCollection);
        
        Collection fileCollectionMetadata = new Collection();
        fileCollectionMetadata.setId(fileCollectionMetadataId);
        when(collectionBizService.getCollection(fileCollectionMetadataId)).thenReturn(fileCollectionMetadata);

        DataItem dataItem = new DataItem();
        dataItem.setId(dataItemId);
        when(dataItemBizService.getDataItem(dataItemId)).thenReturn(dataItem);
        
        DataItem versionDataItem = new DataItem();
        versionDataItem.setId(versionDataItemId);
        when(dataItemBizService.getDataItem(versionDataItemId)).thenReturn(versionDataItem);

        DataItem fileDataItemMetadata = new DataItem();
        fileDataItemMetadata.setId(fileDataItemMetadataId);
        when(dataItemBizService.getDataItem(fileDataItemMetadataId)).thenReturn(fileDataItemMetadata);

        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(metadataFile);

        MetadataFile collectionOnlyMetadataFile = new MetadataFile();
        collectionOnlyMetadataFile.setId(collectionOnlyFileId);
        when(metadataFileBizService.retrieveMetadataFile(collectionOnlyFileId)).thenReturn(collectionOnlyMetadataFile);

        MetadataFile dataItemOnlyMetadataFile = new MetadataFile();
        dataItemOnlyMetadataFile.setId(dataItemOnlyMetadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(dataItemOnlyMetadataFileId)).thenReturn(
                dataItemOnlyMetadataFile);

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId,
                collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId,
                subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId,
                versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + metadataFileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId,
                collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyMetadataFileId,
                dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId,
                fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId,
                fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(0,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
    }
    
    @Test
    public void testInternalReferencePassesThrough() throws StatefulIngestServiceException {        
        Attribute packageAggregatesProject = new AttributeImpl("Package-Aggregates-Project", "String", "urn:uuid:1");
        AttributeSet packageAttributeSet = new AttributeSetImpl("Ore-Rem-Package");
        packageAttributeSet.getAttributes().add(packageAggregatesProject);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(0, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    @Test
    public void testPackageMissingProject() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(null);

        Collection collection = new Collection();
        collection.setId(collectionId);
        when(collectionBizService.getCollection(collectionId)).thenReturn(collection);
        
        DataItem dataItem = new DataItem();
        dataItem.setId(dataItemId);
        when(dataItemBizService.getDataItem(dataItemId)).thenReturn(dataItem);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(metadataFile);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());

    }
    
    @Test
    public void testPackageMissingCollection() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(project);
        
        Collection collection = new Collection();
        collection.setId(collectionId);
        when(collectionBizService.getCollection(collectionId)).thenReturn(null);
        
        DataItem dataItem = new DataItem();
        dataItem.setId(dataItemId);
        when(dataItemBizService.getDataItem(dataItemId)).thenReturn(dataItem);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(metadataFile);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testPackageMissingDataItem() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(project);
        
        Collection collection = new Collection();
        collection.setId(collectionId);
        when(collectionBizService.getCollection(collectionId)).thenReturn(collection);
        
        DataItem dataItem = new DataItem();
        dataItem.setId(dataItemId);
        when(dataItemBizService.getDataItem(dataItemId)).thenReturn(null);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(metadataFile);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }

    @Test
    public void testPackageMissingFile() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(project);
        
        Collection collection = new Collection();
        collection.setId(collectionId);
        when(collectionBizService.getCollection(collectionId)).thenReturn(collection);
        
        DataItem dataItem = new DataItem();
        dataItem.setId(dataItemId);
        when(dataItemBizService.getDataItem(dataItemId)).thenReturn(dataItem);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(null);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testProjectMissingCollection() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(project);
        
        Collection collection = new Collection();
        collection.setId(collectionId);
        when(collectionBizService.getCollection(collectionId)).thenReturn(null);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(metadataFile);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testProjectMissingMetadataFile() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(project);
        
        Collection collection = new Collection();
        collection.setId(collectionId);
        when(collectionBizService.getCollection(collectionId)).thenReturn(collection);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(null);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testCollectionMissingCollection() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(project);
        
        Collection subCollection = new Collection();
        subCollection.setId(subCollectionId);
        when(collectionBizService.getCollection(subCollectionId)).thenReturn(null);
        
        DataItem dataItem = new DataItem();
        dataItem.setId(dataItemId);
        when(dataItemBizService.getDataItem(dataItemId)).thenReturn(dataItem);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(metadataFile);
        
        MetadataFile collectionOnlyMetadataFile = new MetadataFile();
        collectionOnlyMetadataFile.setId(collectionOnlyFileId);
        when(metadataFileBizService.retrieveMetadataFile(collectionOnlyFileId)).thenReturn(collectionOnlyMetadataFile);

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId,
                collectionAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }

    @Test
    public void testCollectionMissingDataItem() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(project);
        
        Collection subCollection = new Collection();
        subCollection.setId(subCollectionId);
        when(collectionBizService.getCollection(subCollectionId)).thenReturn(subCollection);

        DataItem dataItem = new DataItem();
        dataItem.setId(dataItemId);
        when(dataItemBizService.getDataItem(dataItemId)).thenReturn(null);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(metadataFile);
        
        MetadataFile collectionOnlyMetadataFile = new MetadataFile();
        collectionOnlyMetadataFile.setId(collectionOnlyFileId);
        when(metadataFileBizService.retrieveMetadataFile(collectionOnlyFileId)).thenReturn(collectionOnlyMetadataFile);

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId,
                collectionAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testCollectionMissingMetadataFile() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(project);
        
        Collection subCollection = new Collection();
        subCollection.setId(subCollectionId);
        when(collectionBizService.getCollection(subCollectionId)).thenReturn(subCollection);
        
        DataItem dataItem = new DataItem();
        dataItem.setId(dataItemId);
        when(dataItemBizService.getDataItem(dataItemId)).thenReturn(dataItem);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(null);
        
        MetadataFile collectionOnlyMetadataFile = new MetadataFile();
        collectionOnlyMetadataFile.setId(collectionOnlyFileId);
        when(metadataFileBizService.retrieveMetadataFile(collectionOnlyFileId)).thenReturn(collectionOnlyMetadataFile);

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId,
                collectionAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testCollectionMissingProject() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(null);
        
        Collection subCollection = new Collection();
        subCollection.setId(subCollectionId);
        when(collectionBizService.getCollection(subCollectionId)).thenReturn(subCollection);
        
        DataItem dataItem = new DataItem();
        dataItem.setId(dataItemId);
        when(dataItemBizService.getDataItem(dataItemId)).thenReturn(dataItem);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(metadataFile);
        
        MetadataFile collectionOnlyMetadataFile = new MetadataFile();
        collectionOnlyMetadataFile.setId(collectionOnlyFileId);
        when(metadataFileBizService.retrieveMetadataFile(collectionOnlyFileId)).thenReturn(collectionOnlyMetadataFile);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId,
                collectionAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testSubCollectionMissingCollection() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Collection subCollection = new Collection();
        subCollection.setId(collectionId);
        when(collectionBizService.getCollection(collectionId)).thenReturn(null);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId,
                subCollectionAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testDataItemMissingMetadataFile() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Collection collection = new Collection();
        collection.setId(collectionId);
        when(collectionBizService.getCollection(collectionId)).thenReturn(collection);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(null);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testDataItemMissingCollection() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Collection collection = new Collection();
        collection.setId(collectionId);
        when(collectionBizService.getCollection(collectionId)).thenReturn(null);
        
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setId(metadataFileId);
        when(metadataFileBizService.retrieveMetadataFile(metadataFileId)).thenReturn(metadataFile);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + subCollectionId,
                subCollectionAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testDataItemMissingDataItem() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
      
        DataItem versionDataItem = new DataItem();
        versionDataItem.setId(versionDataItemId);
        when(dataItemBizService.getDataItem(versionDataItemId)).thenReturn(versionDataItem);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId,
                versionedDataItemAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testMetadataFileMissingProject() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(null);
        
        Collection fileCollectionMetadata = new Collection();
        fileCollectionMetadata.setId(fileCollectionMetadataId);
        when(collectionBizService.getCollection(fileCollectionMetadataId)).thenReturn(fileCollectionMetadata);
        
        DataItem dataItem = new DataItem();
        dataItem.setId(fileDataItemMetadataId);
        when(dataItemBizService.getDataItem(fileDataItemMetadataId)).thenReturn(dataItem);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, fileAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testMetadataFileMissingCollection() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(project);
        
        Collection fileCollectionMetadata = new Collection();
        fileCollectionMetadata.setId(fileCollectionMetadataId);
        when(collectionBizService.getCollection(fileCollectionMetadataId)).thenReturn(null);
        
        DataItem dataItem = new DataItem();
        dataItem.setId(fileDataItemMetadataId);
        when(dataItemBizService.getDataItem(fileDataItemMetadataId)).thenReturn(dataItem);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, fileAttributeSet);

        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());
        
    }
    
    @Test
    public void testMetadataFileMissingDataItem() throws BizPolicyException, BizInternalException,
            StatefulIngestServiceException, ArchiveServiceException {
        
        Project project = new Project();
        project.setId(projectId);
        when(projectBizService.getProject(projectId)).thenReturn(project);
        
        Collection fileCollectionMetadata = new Collection();
        fileCollectionMetadata.setId(fileCollectionMetadataId);
        when(collectionBizService.getCollection(fileCollectionMetadataId)).thenReturn(fileCollectionMetadata);

        DataItem dataItem = new DataItem();
        dataItem.setId(fileDataItemMetadataId);
        when(dataItemBizService.getDataItem(fileDataItemMetadataId)).thenReturn(null);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, fileAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        Assert.assertEquals(1,
                eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL)
                        .size());        
    }
    
    private class IdentifierImpl implements Identifier {

        String type;
        String uid;
        
        public IdentifierImpl(String type, String uid) {
            this.type = type;
            this.uid = uid;
            
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public URL getUrl() {
           return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getUid() {
            return uid;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getType() {
            return type;
        }
        
    }
}

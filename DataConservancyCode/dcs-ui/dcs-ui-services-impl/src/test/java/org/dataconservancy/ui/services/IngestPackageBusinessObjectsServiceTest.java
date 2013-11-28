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
package org.dataconservancy.ui.services;



import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.SerializationImpl;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.*;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Tests the methods in the CollectionBizService implementation
 */
@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IngestPackageBusinessObjectsServiceTest extends BaseUnitTest {
    
    @Autowired
    CollectionBizService collectionBizService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    DataItemBizService dataItemBizService;
    
    @Autowired
    @Qualifier("testMetadataFileBizService")
    MetadataFileBizService metadataFileBizService;
    
    @Autowired
    MockArchiveDepositInfoDAO mockArchiveDepositInfoDAO;
    
    @Autowired
    @Qualifier("uiIdService")
    IdService idService;
    
    @Autowired
    ProjectBizService projectBizService;
    
    @Autowired
    private ArchiveService inMemoryArchiveService;
    
    private Collection parentCollection;
    private Collection subCollection;
    private DataItem dataItemWithFiles;
    private DataFile pkgDataFileOne;
    private DataFile pkgDataFileTwo;
    
    private MetadataFile collectionMetadataFile;
    private MetadataFile dataItemMetadataFile;
    private MetadataFile dataFileMetadataFile;
    private MetadataFile metadataFileMetadataFile;
    
    private final String parentCollectionLocalId = "2385902";
    private final String subCollectionLocalId = "48269720";
    private final String dataItemWithFilesLocalId = "899028791";
    private final String pkgDataFileOneLocalId = "812304610";
    private final String pkgDataFileTwoLocalId = "23862084";
    private final String collectionMetadataFileLocalId = "8203957218";
    private final String dataItemMetadataFileLocalId = "812789319";
    private final String dataFileMetadataFileLocalId = "5681919";
    private final String metadataFileMetadataFileLocalId = "0198301";
    
    private ArrayList<DcsEvent> events;
    
    private IngestWorkflowState state;
    
    private IngestPackageBusinessObjectsService underTest;
    
    @Before
    public void setup() throws IOException {
        
        setupBusinessObjects();
        setupState();
        
        underTest = new IngestPackageBusinessObjectsService(collectionBizService, projectBizService, dataItemBizService,
                                                       metadataFileBizService, userService, inMemoryArchiveService, idService);
    }
    
    private void setupBusinessObjects() throws IOException {
        parentCollection = new Collection();
        parentCollection.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
        parentCollection.setTitle("Package Collection");
        parentCollection.setSummary("Collection from package");
        parentCollection.setParentProjectId(projectOne.getId());
        
        subCollection = new Collection();
        subCollection.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
        subCollection.setTitle("Package subCollection");
        subCollection.setSummary("Subcollection from package");
        subCollection.setParentId(parentCollection.getId());
        
        dataItemWithFiles = new DataItem();
        dataItemWithFiles.setId(idService.create(Types.DATA_SET.name()).getUrl().toString());
        dataItemWithFiles.setName("Data Item from package with files");
        dataItemWithFiles.setParentId(subCollection.getId());
        
        java.io.File tmp = java.io.File.createTempFile("pkgFileOne", null);
        tmp.deleteOnExit();
        
        PrintWriter out = new PrintWriter(tmp);
        
        out.println(DATA_FILE_ONE_CONTENT);
        out.close();
        
        pkgDataFileOne = new DataFile();
        pkgDataFileOne.setId(idService.create(Types.DATA_FILE.name()).getUrl().toString());
        pkgDataFileOne.setParentId(dataItemWithFiles.getId());
        pkgDataFileOne.setSource(tmp.toURI().toURL().toExternalForm());
        pkgDataFileOne.setName("Data file one");
        
        Resource r = new UrlResource(pkgDataFileOne.getSource());
        pkgDataFileOne.setSize(r.contentLength());
        pkgDataFileOne.setPath(tmp.getParent());
        dataItemWithFiles.addFile(pkgDataFileOne);
        
        java.io.File dfTwoTemp = java.io.File.createTempFile("pkgFileTwo", null);
        dfTwoTemp.deleteOnExit();
        
        out = new PrintWriter(dfTwoTemp);
        
        out.println(DATA_FILE_ONE_CONTENT);
        out.close();
        
        pkgDataFileTwo = new DataFile();
        pkgDataFileTwo.setId(idService.create(Types.DATA_FILE.name()).getUrl().toString());
        pkgDataFileTwo.setParentId(dataItemWithFiles.getId());
        pkgDataFileTwo.setSource(dfTwoTemp.toURI().toURL().toExternalForm());
        pkgDataFileTwo.setName("Data file Two");
        
        r = new UrlResource(pkgDataFileTwo.getSource());
        pkgDataFileTwo.setSize(r.contentLength());
        pkgDataFileTwo.setPath(dfTwoTemp.getParent());
        dataItemWithFiles.addFile(pkgDataFileTwo);
        
        java.io.File collectionMdfTmp = java.io.File.createTempFile("pkgCollectionMdf", null);
        collectionMdfTmp.deleteOnExit();
        
        out = new PrintWriter(collectionMdfTmp);
        
        out.println(DATA_FILE_ONE_CONTENT);
        out.close();
        
        collectionMetadataFile = new MetadataFile();
        collectionMetadataFile.setId(idService.create(Types.METADATA_FILE.name()).getUrl().toString());
        collectionMetadataFile.setParentId(parentCollection.getId());
        collectionMetadataFile.setSource(collectionMdfTmp.toURI().toURL().toExternalForm());
        collectionMetadataFile.setName("Collection Metadata File");
        
        r = new UrlResource(collectionMetadataFile.getSource());
        collectionMetadataFile.setSize(r.contentLength());
        collectionMetadataFile.setPath(collectionMdfTmp.getParent());
        
        java.io.File dataItemMdfTmp = java.io.File.createTempFile("pkgDataItemMdf", null);
        dataItemMdfTmp.deleteOnExit();
        
        out = new PrintWriter(dataItemMdfTmp);
        
        out.println(DATA_FILE_ONE_CONTENT);
        out.close();
        
        dataItemMetadataFile = new MetadataFile();
        dataItemMetadataFile.setId(idService.create(Types.METADATA_FILE.name()).getUrl().toString());
        dataItemMetadataFile.setParentId(dataItemWithFiles.getId());
        dataItemMetadataFile.setSource(dataItemMdfTmp.toURI().toURL().toExternalForm());
        dataItemMetadataFile.setName("Data Item Metadata File");
        
        r = new UrlResource(dataItemMetadataFile.getSource());
        dataItemMetadataFile.setSize(r.contentLength());
        dataItemMetadataFile.setPath(dataItemMdfTmp.getParent());
        
        java.io.File dataFileMdfTmp = java.io.File.createTempFile("pkgFileMdf", null);
        dataFileMdfTmp.deleteOnExit();
        
        out = new PrintWriter(dataFileMdfTmp);
        
        out.println(DATA_FILE_ONE_CONTENT);
        out.close();
        
        dataFileMetadataFile = new MetadataFile();
        dataFileMetadataFile.setId(idService.create(Types.METADATA_FILE.name()).getUrl().toString());
        dataFileMetadataFile.setParentId(pkgDataFileOne.getId());
        dataFileMetadataFile.setSource(dataFileMdfTmp.toURI().toURL().toExternalForm());
        dataFileMetadataFile.setName("Data File metadata file");
        
        r = new UrlResource(dataFileMetadataFile.getSource());
        dataFileMetadataFile.setSize(r.contentLength());
        dataFileMetadataFile.setPath(dataFileMdfTmp.getParent());
        
        java.io.File metadataFileMdfTmp = java.io.File.createTempFile("pkgmetadataFileMdf", null);
        metadataFileMdfTmp.deleteOnExit();
        
        out = new PrintWriter(metadataFileMdfTmp);
        
        out.println(DATA_FILE_ONE_CONTENT);
        out.close();
        
        metadataFileMetadataFile = new MetadataFile();
        metadataFileMetadataFile.setId(idService.create(Types.METADATA_FILE.name()).getUrl().toString());
        metadataFileMetadataFile.setParentId(collectionMetadataFile.getId());
        metadataFileMetadataFile.setSource(metadataFileMdfTmp.toURI().toURL().toExternalForm());
        metadataFileMetadataFile.setName("Metadata File metadata file");
        
        r = new UrlResource(metadataFileMetadataFile.getSource());
        metadataFileMetadataFile.setSize(r.contentLength());
        metadataFileMetadataFile.setPath(metadataFileMdfTmp.getParent());   
    }
    
    private void setupState() {
        state = mock(IngestWorkflowState.class);
        
        AttributeSetManager attrSetManager = mock(AttributeSetManager.class);
        EventManager eventManager = mock(EventManager.class);
        setupEventManager(eventManager);
        BusinessObjectManager boManager = new TestBusinessObjectManager();
        setupBusinessObjectManager(boManager);
        
        PackageDescription description = new DescriptionImpl();
        PackageSerialization serialization = new SerializationImpl();
        org.dataconservancy.packaging.model.Package pkg = new PackageImpl(description, serialization);
        
        when(state.getAttributeSetManager()).thenReturn(attrSetManager);
        when(state.getBusinessObjectManager()).thenReturn(boManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getIngestUserId()).thenReturn(admin.getId());
        when(state.getPackage()).thenReturn(pkg);
    }
    
    private void setupBusinessObjectManager(BusinessObjectManager businessObjectManager) {
        businessObjectManager.add(parentCollectionLocalId, parentCollection, Collection.class);
        businessObjectManager.add(subCollectionLocalId, subCollection, Collection.class);
        businessObjectManager.add(dataItemWithFilesLocalId, dataItemWithFiles, DataItem.class);
        businessObjectManager.add(pkgDataFileOneLocalId, pkgDataFileOne, DataFile.class);
        businessObjectManager.add(pkgDataFileTwoLocalId, pkgDataFileTwo, DataFile.class);
        businessObjectManager.add(collectionMetadataFileLocalId, collectionMetadataFile, MetadataFile.class);
        businessObjectManager.add(dataItemMetadataFileLocalId, dataItemMetadataFile, MetadataFile.class);
        businessObjectManager.add(dataFileMetadataFileLocalId, dataFileMetadataFile, MetadataFile.class);
        businessObjectManager.add(metadataFileMetadataFileLocalId, metadataFileMetadataFile, MetadataFile.class);
    }
    
    private void setupEventManager(EventManager eventManager) {
        
        events = new ArrayList<DcsEvent>();
        
        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation)
                  throws Throwable {
                  DcsEvent event = (DcsEvent) invocation.getArguments()[1];
                  events.add(event);
                  return event;
            }
        }).when(eventManager).addEvent(anyString(), any(DcsEvent.class));

        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation)
                  throws Throwable {
                  String type = (String) invocation.getArguments()[0];
                  DcsEvent event = new DcsEvent();
                  event.setEventType(type);
                  return event;
            }

        }).when(eventManager).newEvent(anyString());
    }
    
    
    @Test
    public void testSuccessfulIngest() throws StatefulIngestServiceException, BizPolicyException, BizInternalException, ArchiveServiceException {
        underTest.execute("foo", state);
        
        assertNotNull(collectionBizService.getCollection(parentCollection.getId()));
        assertNotNull(collectionBizService.getCollection(subCollection.getId()));
        
        DataItem returnedDataItemWithFiles = dataItemBizService.getDataItem(dataItemWithFiles.getId());
        assertNotNull(returnedDataItemWithFiles);
        
        assertEquals(2, returnedDataItemWithFiles.getFiles().size());
      
        assertNotNull(metadataFileBizService.retrieveMetadataFile(collectionMetadataFile.getId()));
        assertNotNull(metadataFileBizService.retrieveMetadataFile(dataItemMetadataFile.getId()));
        assertNotNull(metadataFileBizService.retrieveMetadataFile(metadataFileMetadataFile.getId()));       
        assertNotNull(metadataFileBizService.retrieveMetadataFile(dataFileMetadataFile.getId()));
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testNonExistentCollectionParent() throws StatefulIngestServiceException {
        parentCollection.setParentProjectId("foo");
        
        underTest.execute("foo", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testNullCollectionParent() throws StatefulIngestServiceException {
        parentCollection.setParentProjectId(null);
        underTest.execute("foo", state);
    }    
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testNonExistentSubcollectionParent() throws StatefulIngestServiceException {
        subCollection.setParentId("foo");
        
        underTest.execute("foo", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testNonExistentDataItemParent() throws StatefulIngestServiceException {
        dataItemWithFiles.setParentId("foo");
        
        underTest.execute("foo", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testNullDataItemParent() throws StatefulIngestServiceException {
        dataItemWithFiles.setParentId(null);
        underTest.execute("foo", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testNonExistentMetadataFileParent() throws StatefulIngestServiceException {
        collectionMetadataFile.setParentId("foo");
        underTest.execute("foo", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testNullMetadataFileParent() throws StatefulIngestServiceException {
        collectionMetadataFile.setParentId(null);
        underTest.execute("foo", state);
    }
    
    private class TestBusinessObjectManager implements BusinessObjectManager {
        
        private Map<Key, BusinessObject> businessObjectMap = new HashMap<Key, BusinessObject>();
        private Map<String, Key> businessIdToKeyMap = new HashMap<String, Key>();
        
        @Override
        public <T extends BusinessObject> void add(String localId,
                                                   T boInstance,
                                                   Class<T> boClass) {
            businessObjectMap.put(new Key(localId, boClass), boInstance);
            businessIdToKeyMap.put(boInstance.getId(), new Key(localId, boClass));            
        }

        @Override
        public <T extends BusinessObject> void update(String localId,
                                                      T boInstance,
                                                      Class<T> boClass) {            
        }


        @Override
        public void remove(String localId,
                           Class<? extends BusinessObject> boClass) {
        }

        @Override
        public <T extends BusinessObject> BusinessObject get(String localId,
                                                             Class<T> boClass) {
            return businessObjectMap.get(new Key(localId, boClass));
        }

        @Override
        public <T extends BusinessObject> Set<T> getInstancesOf(Class<T> boClass) {
            Set<T> businessObjects = new HashSet<T>();
            Set<Key> keySet = businessObjectMap.keySet();
            for (Key key : keySet) {
                if (key.getBoClass().equals(boClass)) {
                    businessObjects.add(boClass.cast(businessObjectMap.get(key)));
                }
            }
            return businessObjects;
        }

        @Override
        public BusinessObject get(String businessId) {
            BusinessObject object = null;
            if (businessIdToKeyMap.containsKey(businessId)) {
                object = businessObjectMap.get(businessIdToKeyMap.get(businessId));
            }
            
            return object;
        }

        @Override
        public Map<BusinessObject, String> createMap() {            
            return null;
        }
        
        private class Key {
            private String localId;
            private Class boClass;

            public <T extends BusinessObject> Key (String localId, Class<T> boClass) {
                this.localId = localId;
                this.boClass = boClass;
            }

            private String getLocalId() {
                return this.localId;
            }

            private <T extends BusinessObject> Class<T> getBoClass() {
                return this.boClass;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Key)) return false;

                Key key = (Key) o;

                if (boClass != null ? !boClass.equals(key.boClass) : key.boClass != null) return false;
                if (localId != null ? !localId.equals(key.localId) : key.localId != null) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = localId != null ? localId.hashCode() : 0;
                result = 31 * result + (boClass != null ? boClass.hashCode() : 0);
                return result;
            }
        }

        @Override
        public <T extends BusinessObject> Class<T> getType(String localID) {
            for (Key key : businessObjectMap.keySet()) {
                if (key.getLocalId().equalsIgnoreCase(localID)) {
                    return key.getBoClass();
                }
            }
            
            return null;
        }
        
    }
}
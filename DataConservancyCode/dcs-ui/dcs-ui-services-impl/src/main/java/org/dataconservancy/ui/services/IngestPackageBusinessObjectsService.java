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

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.access.connector.DcsConnectorFault;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;

public class IngestPackageBusinessObjectsService extends BaseIngestService {
    private CollectionBizService collectionBizService;
    private ProjectBizService projectBizService;
    private DataItemBizService dataItemBizService;
    private MetadataFileBizService metadataFileBizService;
    private UserService userService;
    private ArchiveService archiveService;
    private IdService idService;
    
    private static final int DEFAULT_POLL_DELAY = 500;
    private static final int DEFAULT_POLL_COUNT = 30;

    private int pollDelay = DEFAULT_POLL_DELAY;
    private int pollCount = DEFAULT_POLL_COUNT;
    
    public IngestPackageBusinessObjectsService(CollectionBizService collectionBizService,
                                              ProjectBizService projectBizService, DataItemBizService dataItemBizService,
                                              MetadataFileBizService metadataFileBizService, UserService userService, 
                                              ArchiveService archiveService, IdService idService) {
        this.collectionBizService = collectionBizService;
        this.projectBizService = projectBizService;
        this.dataItemBizService = dataItemBizService;
        this.metadataFileBizService = metadataFileBizService;
        this.userService = userService;
        this.archiveService = archiveService;
        this.idService = idService;
    }

    private enum BusinessObjectType {
        PROJECT,
        COLLECTION,
        DATA_ITEM,
        DATA_FILE,
        METADATA_FILE,
    };
    
    public void setPollDelay(int delay) {
        pollDelay = delay;
    }
    
    public int getPollDelay() {
        return pollDelay;
    }
    
    public void setPollCount(int count) {
        pollCount = count;
    }
    
    public int getPollCount() {
        return pollCount;
    }
    
    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        super.execute(depositId, state);
        
        Person user = userService.get(state.getIngestUserId());
        if (user == null) {
            //Fail ingest
        }
        
        try {
            //Deposit all the collections
            for (Collection collectionToIngest : state.getBusinessObjectManager().getInstancesOf(Collection.class)) {
                if (collectionBizService.getCollection(collectionToIngest.getId()) == null) {
                    ingestCollection(collectionToIngest, state, user);
                }
            }
            
            //Map all the data files to their data items for easier deposit
            Map<String, ArrayList<DataFile>> dataFileMap = new HashMap<String, ArrayList<DataFile>>();
            for (DataFile fileToDeposit : state.getBusinessObjectManager().getInstancesOf(DataFile.class)) {
                String dataItemId = fileToDeposit.getParentId();
                if (dataFileMap.containsKey(dataItemId)) {
                    dataFileMap.get(dataItemId).add(fileToDeposit);
                } else {
                    ArrayList<DataFile> dataItemFiles = new ArrayList<DataFile>();
                    dataItemFiles.add(fileToDeposit);
                    dataFileMap.put(dataItemId, dataItemFiles);
                }
            }
    
            //Deposit all the data items
            for (DataItem dataItemToIngest : state.getBusinessObjectManager().getInstancesOf(DataItem.class)) {
                //The underlying services assume the parent is not null so check it here
                if (dataItemToIngest.getParentId() == null)  {
                    throw new StatefulIngestServiceException("Data Item " + dataItemToIngest.getId() + " could not be deposited, parent id was null");
                }
                dataItemBizService.addDataItem(dataItemToIngest, dataItemToIngest.getParentId(), user);
            }
            
            //Finally, deposit all metadata files
            ArrayList<String> depositedMetadataFiles = new ArrayList<String>();
            for (MetadataFile metadataFileToIngest : state.getBusinessObjectManager().getInstancesOf(MetadataFile.class)) {
                ingestMetadataFile(metadataFileToIngest, state, user, depositedMetadataFiles);
            }
            
        } catch (BizPolicyException e) {
            throw new StatefulIngestServiceException(e);
        } catch (BizInternalException e) {
            throw new StatefulIngestServiceException(e);
        } catch (ArchiveServiceException e) {
            throw new StatefulIngestServiceException(e);
        }
    }
    
    private void ingestCollection(Collection collection, IngestWorkflowState state, Person user) throws BizPolicyException, BizInternalException, StatefulIngestServiceException {
      //Check the collection parent is a project this can happen if the project is specified in the collection isPartOf section in the REM.
        if (collection.getParentId() != null && !collection.getParentId().isEmpty()) {
            if (projectBizService.getProject(collection.getParentId()) != null) {
                collection.setParentProjectId(collection.getParentId());
                collection.setParentId(null);
            }
        }
        
        //Check to make sure any parent collections are already deposited
        if (collection.getParentId() != null && !collection.getParentId().isEmpty()) {
            Collection parent = collectionBizService.getCollection(collection.getParentId());
            if (parent == null) {
               Collection parentInPackage = (Collection) state.getBusinessObjectManager().get(collection.getParentId());
               if (parentInPackage == null) {
                   throw new StatefulIngestServiceException("Parent Collection " + collection.getParentId() + " for collection " + collection.getId() + " could not be found.");
               } else {
                   ingestCollection(parentInPackage, state, user);                   
               }
                   
            }
        }
        collectionBizService.createCollection(collection, user);
    }
    
    private void ingestMetadataFile(MetadataFile metadataFile, IngestWorkflowState state, Person user, ArrayList<String> depositedMetadataFiles) throws StatefulIngestServiceException, BizPolicyException, BizInternalException {
        //Retrieve the parent of the data metadata file
        BusinessObject parent = null;
        String parentId = metadataFile.getParentId();
        Identifier id;
        try {
            id = idService.fromUrl(new URL(parentId));
        } catch (MalformedURLException e) {
            throw new StatefulIngestServiceException("Failed to find parent id for metadata file: " + metadataFile.getId(), e);
        } catch (IdentifierNotFoundException e) {
            throw new StatefulIngestServiceException("Failed to find parent id for metadata file: " + metadataFile.getId(), e);
        }
        
        try {
            if (id.getType().equals(Types.PROJECT.name())) {
                parent = projectBizService.getProject(parentId);
            } else if (id.getType().equals(Types.COLLECTION.name())) {
                parent = pollAndQueryBusinessObject(parentId, BusinessObjectType.COLLECTION);
            } else if (id.getType().equals(Types.DATA_SET.name())) {
                parent = pollAndQueryBusinessObject(parentId, BusinessObjectType.DATA_ITEM); 
            } else if (id.getType().equals(Types.METADATA_FILE.name())) {
                //Since we haven't deposited all metadata files yet first check to see if the metadata file is in the package
                MetadataFile parentInPackage = (MetadataFile) state.getBusinessObjectManager().get(parentId);                
                if (parentInPackage != null) {
                    //Check the deposited file list this saves us a potentially expensive poll for a metadata file still in the queue.
                    if (!depositedMetadataFiles.contains(parentId)) {
                        ingestMetadataFile(parentInPackage, state, user, depositedMetadataFiles);
                    }
                }
                parent = pollAndQueryBusinessObject(parentId, BusinessObjectType.METADATA_FILE);               
            } else if (id.getType().equals(Types.DATA_FILE.name())) {
                parent = pollAndQueryBusinessObject(parentId, BusinessObjectType.DATA_FILE);
            }
        } catch(Exception e) {
            throw new StatefulIngestServiceException("Failed to find metadata file parent id '" + parentId + "' (a " +
                    id.getType() + ") for metadata file id '" + metadataFile.getId() + "' " +
                    "(name '" + metadataFile.getName() + "')", e);
        }

        if (parent == null) {
            throw new StatefulIngestServiceException("Failed to find metadata file parent id '" + parentId + "' (a " +
                    id.getType() + ") for metadata file id '" + metadataFile.getId() + "' " +
                    "(name '" + metadataFile.getName() + "')");
        }

        metadataFileBizService.addNewMetadataFile(parent, metadataFile, user);
        depositedMetadataFiles.add(metadataFile.getId());
    }
    
    private BusinessObject pollAndQueryBusinessObject(String id, BusinessObjectType type) throws Exception{       
        int count = 0;
        BusinessObject object = null;
        do {
            final long sleepInterval = count * pollDelay;
           
            try {
                Thread.sleep(sleepInterval);
                archiveService.pollArchive();
                switch (type) {
                    case COLLECTION: 
                        object = collectionBizService.getCollection(id);
                        break;
                    case DATA_ITEM:
                        object = dataItemBizService.getDataItem(id);
                        break;
                    case METADATA_FILE:
                        object = metadataFileBizService.retrieveMetadataFile(id);
                        break;
                    case DATA_FILE:
                        object = dataItemBizService.getDataFile(id);
                        break;
                    default:
                        break;
                }
            } catch (InterruptedException e) {
                // ignore
            }
            count++;
        } while (object == null && count < pollCount);
        
        return object;
    }
}
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.ResourceResolverUtil;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.exceptions.EmptySearchResultsException;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.profile.Profile;
import org.dataconservancy.ui.util.DataSetComparator;
import org.dataconservancy.ui.util.MappingUtil;
import org.xml.sax.SAXException;

import org.dataconservancy.access.connector.DcsClientFault;
import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.access.connector.DcsConnectorFault;
import org.dataconservancy.access.connector.DcsConnectorRuntimeException;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.ui.dao.ArchiveDepositInfoDAO;
import org.dataconservancy.ui.dcpmap.DcpMapper;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.DcpMappingException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Type;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.dataconservancy.ui.util.DcpUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.COLLECTION;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.DATASET;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.DATA_FILE;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.METADATA_FILE;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.REGISTRY_ENTRY;

/**
 * An implementation of {@link ArchiveService}.
 */
public class ArchiveServiceImpl implements ArchiveService {

    private static final DataSetComparator DATA_SET_COMPARATOR = new DataSetComparator();

    /**
     * Error message reported when ancestors cannot be found.  Parameters are: ancestor archive id, type of archive id,
     * and reason.
     */
    private static final String ERROR_FINDING_ANCESTORS = "Error finding ancestors for archive id '%s', a '%s': %s";

    /**
     * Error message reported when the root of the deposited object graph cannot be found.  Parameters are:
     * deposit id
     */
    private static final String ERROR_NO_ROOT_DEPOSIT_DU = "Expected deposit (id '%s') to contain a Root Deposit DU.";

    private static final String ERROR_UNKNOWN_OBJECT_TYPE = "Unknown object type '%s'";

    /**
     * Error message reported when an ArchiveSearchResult object is null.  Parameters are:
     * object type, requested deposit id.
     */
    private static final String ERROR_NULL_ASR = "Unable to retrieve %s with deposit (transaction) id " +
                        "'%s'; the search results from the DCS archive were null.  The identified " +
                        "deposit may be pending, completed with an error, or invalid";

    /**
     * Error when no deposit id is returned after a call to deposit(...).  Parameters are object type, business id.
     */
    private static final String ERROR_NULL_DEPOSIT_ID = "Did not obtain a deposit id for the deposit of %s '%s'.  " +
                        "Deposit ID was null or empty.";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ArchiveDepositInfoDAO deposit_info_dao;
    private final DcpMapper<DataItem> dataset_mapper;
    private final DcpMapper<Collection> col_mapper;
    private final DcpMapper<MetadataFile> metadataFile_mapper;
    private final DcpMapper<DataFile> dataFile_mapper;
    private final org.dataconservancy.profile.api.DcpMapper<RegistryEntry<DcsMetadataFormat>> registryEntry_mapper;
    private final DcsConnector connector;
    private final ArchiveUtil archiveUtil;
    private final DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();


    private final DepositDocumentResolver resolver;

    private AncestrySearcher ancestrySearcher;

    private ParentSearcher parentSearcher;

    private DataItemBusinessObjectSearcher dataItemBoSearcher;

    private DataFileBusinessObjectSearcher dataFileBoSearcher;

    private BusinessObjectSearcher collectionBoSearcher;
    
    private MetadataFileBusinessObjectSearcher metadataFileBoSearcher;

    /**
     * @param deposit_info_dao
     * @param dataset_mapper
     * @param col_mapper
     * @param connector
     * @param poll_sleep       ms to wait between polling the archive for updated deposit
     *                         status
     */
    public ArchiveServiceImpl(ArchiveDepositInfoDAO deposit_info_dao,
                              DcpMapper<DataItem> dataset_mapper,
                              DcpMapper<DataFile> dataFile_mapper,
                              DcpMapper<Collection> col_mapper,
                              DcpMapper<MetadataFile> metadataFile_mapper,
                              org.dataconservancy.profile.api.DcpMapper<RegistryEntry<DcsMetadataFormat>> registryEntry_mapper,
                              DcsConnector connector,
                              Profile<DataItem> dataItemProfile,
                              DepositDocumentResolver resolver,
                              IdService idService,
                              ArchiveUtil archiveUtil,
                              final long poll_sleep) {
        this.deposit_info_dao = deposit_info_dao;
        this.dataset_mapper = dataset_mapper;
        this.dataFile_mapper = dataFile_mapper;
        this.col_mapper = col_mapper;
        this.metadataFile_mapper = metadataFile_mapper;
        this.registryEntry_mapper = registryEntry_mapper;
        this.connector = connector;
        this.resolver = resolver;
        this.archiveUtil = archiveUtil;

        this.collectionBoSearcher = new CollectionBusinessObjectSearcherImpl(connector, idService);

        this.parentSearcher = new ArchiveParentSearcherImpl(connector);
        this.dataItemBoSearcher = new DataItemBusinessObjectSearcherImpl(connector, idService, dataItemProfile, parentSearcher);
        this.dataFileBoSearcher = new DataFileBusinessObjectSearcherImpl(connector, idService);
        this.metadataFileBoSearcher = new MetadataFileBusinessObjectSearcherImpl(connector, idService);
        
        if (poll_sleep > 0) {
            // TODO Use spring scheduler to setup polling?

            Thread thr = new Thread(new Runnable() {
                public void run() {
                    for (;;) {
                        try {
                            pollArchive();
                            Thread.sleep(poll_sleep);
                        } catch (ArchiveServiceException e) {
                            log.error("Failure polling archive", e);
                        } catch (InterruptedException e) {
                            log.info("Polling thread interrupted", e);
                        }
                    }
                }
            }, "Archive Service Poller");
            
            thr.start();
        }
    }

    /**
     * The {@link AncestrySearcher} used to perform ancestry searches in the archive.
     *
     * @return the searcher
     */
    public AncestrySearcher getAncestrySearcher() {
        return ancestrySearcher;
    }

    /**
     * The {@link AncestrySearcher} used to perform ancestry searches in the archive.
     *
     * @param ancestrySearcher the searcher, must not be {@code null}
     */
    public void setAncestrySearcher(AncestrySearcher ancestrySearcher) {
        if (ancestrySearcher == null) {
            throw new IllegalArgumentException("AncestrySearcher must not be null.");
        }
        this.ancestrySearcher = ancestrySearcher;
    }


    /**
     * The {@link ParentSearcher} used to perform parent searches in the archive.
     *
     * @return the searcher
     */
    public ParentSearcher getParentSearcher() {
        return parentSearcher;
    }

    /**
     * The {@link ParentSearcher} used to perform parent searches in the archive.
     *
     * @param parentSearcher the searcher, must not be {@code null}
     */
    public void setParentSearcher(ParentSearcher parentSearcher) {
        if (parentSearcher == null) {
            throw new IllegalArgumentException("ParentSearcher must not be null.");
        }
        this.parentSearcher = parentSearcher;
    }

    /**
     * The {@link DataItemBusinessObjectSearcher} used to find the current state of a DataItem in the archive.
     *
     * @return the searcher
     */
    public DataItemBusinessObjectSearcher getDataItemBoSearcher() {
        return dataItemBoSearcher;
    }

    /**
     * The {@link DataItemBusinessObjectSearcher} used to find the current state of a DataItem in the archive.
     *
     * @param dataItemBoSearcher the searcher, must not be {@code null}
     */
    public void setDataItemBoSearcher(DataItemBusinessObjectSearcher dataItemBoSearcher) {
        if (dataItemBoSearcher == null) {
            throw new IllegalArgumentException("Data Item Business Object Searcher must not be null.");
        }
        this.dataItemBoSearcher = dataItemBoSearcher;
    }

    /**
     * The {@link MetadataFileBusinessObjectSearcher} used to find the current state of a MetadataFile in the archive.
     *
     * @return the searcher
     */
    public MetadataFileBusinessObjectSearcher getMetadataFileBoSearcher() {
        return metadataFileBoSearcher;
    }

    /**
     * The {@link DataItemBusinessObjectSearcher} used to find the current state of a DataItem in the archive.
     *
     * @param dataItemBoSearcher the searcher, must not be {@code null}
     */
    public void setMetadataFileBoSearcher(MetadataFileBusinessObjectSearcher metadataFileBoSearcher) {
        if (metadataFileBoSearcher == null) {
            throw new IllegalArgumentException("Metadata File Business Object Searcher must not be null.");
        }
        this.metadataFileBoSearcher = metadataFileBoSearcher;
    }
    
    public void pollArchive() throws ArchiveServiceException {
        int pendingCount = deposit_info_dao.list(null, Status.PENDING).size();

        if (pendingCount > 0) {
            log.debug("Polling archive: {} pending deposit(s))", pendingCount);
        } else {
            log.debug("Polling archive: no pending deposits to poll.");
            return;
        }

        for (ArchiveDepositInfo info : deposit_info_dao.list(null, Status.PENDING)) {
            synchronized (createObjectIdLock(info.getObjectId())) {
                try {
                    ArchiveDepositInfo updated = retrieveDepositInfo(info);
                    if (updated != null) {
                        deposit_info_dao.update(updated);
                        log.debug("Updated deposit info for deposit_id {} (a {}) (parent deposit id '{}'): assigned " +
                                "archive id {} and updated archive status to {} for object id {}",
                                new Object[]{updated.getDepositId(), updated.getObjectType(),
                                        updated.getParentDepositId(), updated.getArchiveId(), updated.getDepositStatus(),
                                        updated.getObjectId()});
                    } else {
                        log.debug("No update found for deposit_id {} (a {}, status {})",
                                new Object[]{info.getDepositId(), info.getObjectType(), info.getDepositStatus()});
                    }
                } catch (IOException e) {
                    throw new ArchiveServiceException("Failed to read deposit ticket: " + info, e);
                } catch (SAXException e) {
                    throw new ArchiveServiceException("Failed to parse deposit ticket: " + info, e);
                } catch (InvalidXmlException e) {
                    throw new ArchiveServiceException("Failed to parse xml while examining deposit ticket: " + info, e);
                } catch (DcsConnectorFault e) {
                    throw new ArchiveServiceException("Failed to access dcs while updating deposit ticket: " + info, e);
                }
            }
        }
    }

    /**
     * Retrieves the latest ArchiveDepositInfo from the DCS archive.
     *
     * @param pendingUpdate the ArchiveDepositInfo object to be updated
     * @return an updated ArchiveDepositInfo object, or {@code null} if no updates were available
     * @throws IOException
     * @throws SAXException
     * @throws InvalidXmlException
     * @throws DcsConnectorFault
     */
    ArchiveDepositInfo retrieveDepositInfo(ArchiveDepositInfo pendingUpdate) throws IOException,
            SAXException, InvalidXmlException, DcsConnectorFault, ArchiveServiceException {

        if (Status.PENDING != pendingUpdate.getDepositStatus()) {
            return null;
        }

        log.debug("Polling deposit feed: {} (a {}, status {})",
                new Object[]{pendingUpdate.getDepositId(), pendingUpdate.getObjectType(),
                        pendingUpdate.getDepositStatus()});

        final DepositDocument deposit = resolver.resolve(pendingUpdate.getDepositId());
        if (deposit == null) {
            log.debug("Error resolving deposit feed " + pendingUpdate.getDepositId());
            return null;
        }

        if (deposit.isComplete()) {
            ArchiveDepositInfo result = new ArchiveDepositInfo(pendingUpdate);

            if (!deposit.isSuccessful()) {
                result.setDepositStatus(Status.FAILED);
                return result;
            }

            DcsDeliverableUnit depositRootDu = deposit.getRoot();
            if (depositRootDu == null) {
                throw new ArchiveServiceException(String.format(ERROR_NO_ROOT_DEPOSIT_DU,
                        pendingUpdate.getDepositId()));
            }

            // Determine the Root DU of the Deposit Root.  The id of the Root DU is set as the
            // archive id on the ArchiveDepositInfo.
            //
            // The root deposit DU may not be the same as the root DU of the object graph representing the
            // business object.
            //
            // For example, the deposit may have only updated the state of a Collection.  That means
            // that the root of the deposit will be a DU representing Collection state, but the DU representing the
            // root of the entire Collection object graph resides in the archive.

            if (MappingUtil.getPredecessorId(depositRootDu) != null) {
                BusinessObjectState state = null;
                // Search the archive
                switch (pendingUpdate.getObjectType()) {
                    case DATASET:
                        state = dataItemBoSearcher.findLatestState(pendingUpdate.getObjectId());
                        break;
                    case COLLECTION:
                        state = collectionBoSearcher.findLatestState(pendingUpdate.getObjectId());
                        break;
                    case METADATA_FILE:
                        state = metadataFileBoSearcher.findLatestState(pendingUpdate.getObjectId());
                        break;
                    case REGISTRY_ENTRY:                        
                        break;
                    case DATA_FILE:
                        state = dataFileBoSearcher.findLatestState(pendingUpdate.getObjectId());
                        break;
                    default:
                        throw new ArchiveServiceException(String.format(ERROR_UNKNOWN_OBJECT_TYPE,
                                pendingUpdate.getObjectType()));
                }

                if (state == null || state.getRoot() == null) {
                    throw new RuntimeException("Missing state or root du for '" + pendingUpdate.getObjectId() + "'");
                }

                result.setArchiveId(state.getRoot().getId());
                result.setStateId(depositRootDu.getId());
            } else {
                Dcp depositDocDcp = new Dcp();
                DcpUtil.add(depositDocDcp, deposit.getEntities());
                String stateDuId = null;
                switch (pendingUpdate.getObjectType()) {
                    case DATASET:
                        stateDuId = MappingUtil.getStateDuFromDataItemDcp(depositDocDcp).getId();
                        break;
                    case COLLECTION:
                        stateDuId = MappingUtil.getStateDuFromCollectionDcp(depositDocDcp, "collection_state").getId();
                        break;
                    case METADATA_FILE:
                        stateDuId = MappingUtil.getStateDuFromMetadataFileDcp(depositDocDcp, "metadataFile_state").getId();
                        break;
                    case DATA_FILE:
                        stateDuId = MappingUtil.getStateDuFromDataFileDcp(depositDocDcp).getId();
                        break;
                    case REGISTRY_ENTRY:
                        stateDuId = MappingUtil.getRegistryEntryDuFromDcp(depositDocDcp).getId();
                        break;
                    default:
                        throw new ArchiveServiceException(String.format(ERROR_UNKNOWN_OBJECT_TYPE,
                                pendingUpdate.getObjectType()));
                }

                result.setArchiveId(depositRootDu.getId());
                result.setStateId(stateDuId);
            }

            result.setDepositStatus(Status.DEPOSITED);

            return result;
        }

        return null;
    }

    <T> String deposit(String parent_deposit_id, String object_id,
                               T object, DcpMapper<T> mapper) throws ArchiveServiceException {
        ArchiveDepositInfo info = new ArchiveDepositInfo();

        info.setDepositStatus(Status.PENDING);
        info.setDepositDateTime(DateTime.now());
        info.setObjectId(object_id);
        if (parent_deposit_id != null && parent_deposit_id.trim().length() > 0) {
            info.setParentDepositId(parent_deposit_id);
        }

        if (object instanceof Collection) {
            info.setObjectType(Type.COLLECTION);
        } else if (object instanceof DataItem) {
            info.setObjectType(DATASET);
        } else if (object instanceof MetadataFile) {
            info.setObjectType(METADATA_FILE);
        } else if (object instanceof RegistryEntry) {
            info.setObjectType(REGISTRY_ENTRY);
        } else if (object instanceof DataFile) {
            info.setObjectType(DATA_FILE);
        }

        String parent_archive_id = parent_deposit_id == null ? null
                : deposit_info_dao.lookup(parent_deposit_id).getArchiveId();
        
        //If were archiving a metadata file or data file we want to point to the state of the parent not the root
        if (object instanceof MetadataFile || object instanceof DataFile) {
            parent_archive_id = parent_deposit_id == null ? null
                    : deposit_info_dao.lookup(parent_deposit_id).getStateId();
        }

        try {
            Dcp dcp = mapper.toDcp(parent_archive_id, object);
            // Have to upload file separately and update SIP

            java.util.Collection<DcsFile> files = dcp.getFiles();

            for (DcsFile file : files) {
                InputStream is = new URL(file.getSource()).openStream();
                String upload_id = connector.uploadFile(is, -1);
                is.close();

                file.setSource(upload_id);
            }

            dcp.setFiles(files);
            
            if (info.getObjectType() == Type.DATASET) {
                // The DataSet mapper needs additional support.
                // If this deposit is an update of an existing object in the archive, then remove the Root DU from the
                // deposit, and instead have the State DU reference the Root DU in the archive.
                // We know it is an update if the State DU has a predecessor
                
                DcsDeliverableUnit stateDu = MappingUtil.getStateDuFromDataItemDcp(dcp);
                String predecessorStateDuId = MappingUtil.getPredecessorId(stateDu);
           
                if (predecessorStateDuId != null) {
                    // find the parent of the state du, and if it is in the DCP, remove it.
                    String parent = stateDu.getParents().iterator().next().getRef();
                    Map<String, DcsEntity> dcpMap = DcpUtil.asMap(dcp);
                    if (dcpMap.containsKey(parent)) {
                        dcpMap.remove(parent);
                    }
    
                    // get the predecessor from the Archive
                    DcsDeliverableUnit predecessorStateDu = (DcsDeliverableUnit)
                            archiveUtil.getEntity(predecessorStateDuId);
    
                    // determine its parent
                    String predecessorsParentId = predecessorStateDu.getParents().iterator().next().getRef();
    
                    DcsDeliverableUnit updatedStateDu = new DcsDeliverableUnit(stateDu);
                    Set<DcsDeliverableUnitRef> parents = new HashSet<DcsDeliverableUnitRef>();
                    parents.add(new DcsDeliverableUnitRef(predecessorsParentId));
                    updatedStateDu.setParents(parents);
    
                    dcpMap.put(updatedStateDu.getId(), updatedStateDu);
                    dcp = new Dcp();
                    DcpUtil.add(dcp, dcpMap.values());
                }
            }

            if (log.isDebugEnabled()) {
                final String prefix = ("dcp-" + Thread.currentThread().getName()).replace(" ", "_").replace("/", "_").replace("\\", "_");
                final String suffix = ".xml";
                File tmp = File.createTempFile(prefix, suffix);
                log.debug("Copy of deposited SIP: {}", tmp);
                final FileOutputStream sink = new FileOutputStream(tmp);
                builder.buildSip(dcp, sink);
                sink.close();
            }
            
            URL ticket = connector.depositSIP(dcp);

            info.setDepositId(ticket.toExternalForm());
        } catch (DcpMappingException e) {
            throw new ArchiveServiceException(
                    "Failed create SIP for " + object, e);
        } catch (DcsClientFault e) {
            throw new ArchiveServiceException("Failed to deposit " + object, e);
        } catch (IOException e) {
            throw new ArchiveServiceException("Failed to create SIP for "
                    + object, e);
        } catch (DcsConnectorRuntimeException e) {
            throw new ArchiveServiceException("Failed to deposit " + object_id
                    + ": " + e.getMessage(), e);
        }

        deposit_info_dao.add(info);

        return info.getDepositId();
    }
    
    //This is only necessary because business objects use the UI dcp mapper and the mhf stuff uses profile
    String deposit(String object_id,
                       RegistryEntry<DcsMetadataFormat> object, org.dataconservancy.profile.api.DcpMapper<RegistryEntry<DcsMetadataFormat>> mapper) throws ArchiveServiceException {
        ArchiveDepositInfo info = new ArchiveDepositInfo();
        
        info.setDepositStatus(Status.PENDING);
        info.setDepositDateTime(DateTime.now());
        info.setObjectId(object_id);
        
        info.setObjectType(REGISTRY_ENTRY);
      
        try {            
            Dcp dcp = registryEntry_mapper.to(object, null);
            // Have to upload file separately and update SIP
        
            java.util.Collection<DcsFile> files = dcp.getFiles();
        
            for (DcsFile file : files) {
                Resource r = ResourceResolverUtil.resolveFileSource(file.getSource());
               
                InputStream is = r.getInputStream();
                String upload_id = connector.uploadFile(is, -1);
                is.close();
        
                file.setSource(upload_id);
            }
        
            dcp.setFiles(files);
        
            if (log.isDebugEnabled()) {
                final String prefix = ("dcp-" + Thread.currentThread().getName()).replace(" ", "_").replace("/", "_").replace("\\", "_");
                final String suffix = ".xml";
                File tmp = File.createTempFile(prefix, suffix);
                log.debug("Copy of deposited SIP: {}", tmp);
                final FileOutputStream sink = new FileOutputStream(tmp);
                builder.buildSip(dcp, sink);
                sink.close();
            }
            
            URL ticket = connector.depositSIP(dcp);
        
            info.setDepositId(ticket.toExternalForm());
        } catch (DcsClientFault e) {
            throw new ArchiveServiceException("Failed to deposit " + object, e);
        } catch (IOException e) {
            throw new ArchiveServiceException("Failed to create SIP for "
                    + object, e);
        } catch (DcsConnectorRuntimeException e) {
            throw new ArchiveServiceException("Failed to deposit " + object_id
                    + ": " + e.getMessage(), e);
        }
        
        deposit_info_dao.add(info);
        
        return info.getDepositId();
    }

    public String deposit(Collection col) throws ArchiveServiceException {
        return deposit(null, col.getId(), col, col_mapper);
    }

    public String deposit(String deposit_id, DataItem ds)
            throws ArchiveServiceException {

        synchronized (createObjectIdLock(ds.getId())) {
            // Deposit the DataItem first, so we have a deposit id.
            final String dsDepositId = deposit(deposit_id, ds.getId(), ds, dataset_mapper);

            if (dsDepositId == null || dsDepositId.trim().length() == 0) {
                throw new ArchiveServiceException(String.format(ERROR_NULL_DEPOSIT_ID, "DataItem", ds.getId()));
            }

            Status depositStatus = Status.PENDING;
            final long inital_waittime = 1000l;         // initial wait time 1 second
            long total_wait = 0l;                       // total time spent waiting
            long waittime = inital_waittime;            // wait time used in the do/while
            final long backoffFactor = 2;               // back-off multiplier; 2s, 4s, 8s, 16s, etc.
            final long maxwait = inital_waittime * 120; // maximum wait time 120 seconds
            do {
                try {
                    pollArchive();
                    depositStatus = getDepositStatus(dsDepositId);
                    if (depositStatus == Status.PENDING) {
                        Thread.sleep(waittime);
                        total_wait += waittime;
                    }
                } catch (Exception e) {
                    log.debug("Encountered exception while polling archive, but ignoring it: " + e.getMessage(), e);
                }
                waittime *= backoffFactor;
            } while (depositStatus == Status.PENDING && total_wait < maxwait);

            if (depositStatus != Status.DEPOSITED) {
                throw new ArchiveServiceException("Unable to deposit DataItem '" + ds.getId() + "' (status: '" +
                        depositStatus.name() + "')" + (total_wait > maxwait ?
                        ". Operation timed out after " + total_wait + "ms." : ""));
            }

            // Now deposit each data file in the data item

            for (DataFile df : ds.getFiles()) {
                final String dfDepositId = deposit(dsDepositId, df);

                if (dfDepositId == null || dfDepositId.trim().length() == 0) {
                    throw new ArchiveServiceException(String.format(ERROR_NULL_DEPOSIT_ID, "DataFile", df.getId()));
                }

                waittime = inital_waittime;
                total_wait = 0l;
                depositStatus = Status.PENDING;

                do {
                    try {
                        pollArchive();
                        depositStatus = getDepositStatus(dfDepositId);
                        if (depositStatus == Status.PENDING) {
                            Thread.sleep(waittime);
                            total_wait += waittime;
                        }
                    } catch (Exception e) {
                        log.debug("Encountered exception while polling archive, but ignoring it: " + e.getMessage(), e);
                    }
                    waittime *= backoffFactor;
                } while (depositStatus == Status.PENDING && total_wait < maxwait);

                if (depositStatus != Status.DEPOSITED) {
                    throw new ArchiveServiceException("Unable to deposit DataFile '" + df.getId() + "' (status: '" +
                            depositStatus.name() + "')" + (total_wait > maxwait ?
                            ". Operation timed out after " + total_wait + "ms." : ""));
                }
            }

            return dsDepositId;
        }
    }

    private String deposit(String parent_deposit_id, DataFile df) throws ArchiveServiceException {
        return deposit(parent_deposit_id, df.getId(), df, dataFile_mapper);
    }

    public String deposit(String deposit_id, MetadataFile mf)
            throws ArchiveServiceException {
        return deposit(deposit_id, mf.getId(), mf, metadataFile_mapper);
    }
    
    public String deposit(RegistryEntry<DcsMetadataFormat> formatRegistryEntry)
        throws ArchiveServiceException {
        return deposit(formatRegistryEntry.getId(), formatRegistryEntry, registryEntry_mapper);
    }
    
    public Status getDepositStatus(String deposit_id) {
        ArchiveDepositInfo info = deposit_info_dao.lookup(deposit_id);

        if (info == null) {
            return null;
        }

        return info.getDepositStatus();
    }

    private List<String> list(Type type, Status status) {
        List<String> result = new ArrayList<String>();

        for (ArchiveDepositInfo info : deposit_info_dao.list(type, status)) {
            result.add(info.getDepositId());
        }

        return result;
    }

    public List<String> listCollections(Status status) {
        return list(Type.COLLECTION, status);
    }

    <T> ArchiveSearchResult<T> retrieve(String deposit_id, DcpMapper<T> mapper)
            throws ArchiveServiceException {

        if (ancestrySearcher == null) {
            throw new IllegalStateException("AncestrySearcher must not be null: see setAncestrySearcher(...)");
        }

        ArchiveDepositInfo info = deposit_info_dao.lookup(deposit_id);
        if (info == null || info.getDepositStatus() != Status.DEPOSITED) {
            log.warn("Deposit status for '" + deposit_id + "' was '" +
                    (info == null ? "null" : info.getDepositStatus() + "'"));
            return null;
        }

        final Set<T> results;

        try {
            Dcp dcp = null;
            java.util.Collection<DcsEntity> entities = null;
            
            if (info.getObjectType() == DATASET) {
                entities = ancestrySearcher.getAncestorsOf(info.getStateId(), true);
                entities.add(archiveUtil.getEntity(info.getArchiveId()));
            } else if (info.getObjectType() == DATA_FILE) {
                entities = ancestrySearcher.getAncestorsOf(info.getStateId(), true);
                entities.add(archiveUtil.getEntity(info.getArchiveId()));
            } else if (info.getObjectType() == COLLECTION) {
                entities = ancestrySearcher.getAncestorsOf(info.getStateId(), true);
            } else if (info.getObjectType() == METADATA_FILE) {
                entities = ancestrySearcher.getAncestorsOf(info.getStateId(), true);
                entities.add(archiveUtil.getEntity(info.getArchiveId()));
            } else {
                throw new ArchiveServiceException("Cannot retrieve '" + info.getObjectType() + "' object from the " +
                        "archive!");
            }

            if (entities.isEmpty()) {
                final String msg = String.format(ERROR_FINDING_ANCESTORS, info.getArchiveId(), info.getObjectType(),
                        "A search of the archive for ancestors of '" + info.getArchiveId() + "' returned no results.");
                throw new EmptySearchResultsException(msg);
            }

            dcp = new Dcp();
            DcpUtil.add(dcp, entities);

            results = new HashSet<T>();
            results.add(mapper.fromDcp(dcp));
        } catch (RuntimeException e) {
            final String msg = String.format(ERROR_FINDING_ANCESTORS, info.getArchiveId(), info.getObjectType(),
                    e.getMessage());
            throw new ArchiveServiceException(msg, e);
        } catch (DcpMappingException e) {
            throw new ArchiveServiceException("Error mapping search results for " + info.getArchiveId() + ": " +
                    e.getMessage(), e);
        }

        return new ArchiveSearchResult<T>(results, results.size());
    }

    public ArchiveSearchResult<Collection> retrieveCollection(String deposit_id)
            throws ArchiveServiceException {
        return retrieve(deposit_id, col_mapper);
    }

    public List<String> listDataSets(Status status) {
        return list(DATASET, status);
    }

    public ArchiveSearchResult<DataItem> retrieveDataSet(String deposit_id)
            throws ArchiveServiceException {
        ArchiveSearchResult<DataItem> diAsr = retrieve(deposit_id, dataset_mapper);

        if (diAsr == null) {
            throw new ArchiveServiceException(String.format(ERROR_NULL_ASR, "DataItem", deposit_id));
        }

        if (diAsr.getResultCount() == 1) {
            DataItem di = diAsr.getResults().iterator().next();
            if (di == null) {
                throw new ArchiveServiceException("Unable to retrieve DataItem with deposit (transaction) id" +
                        " '" + deposit_id + "'; the DataItem was null.");
            }

            synchronized (createObjectIdLock(di.getId())) {
                for (ArchiveDepositInfo dfInfo : deposit_info_dao.lookupChildren(deposit_id)) {
                    if (dfInfo.getObjectType() == DATA_FILE) {
                        ArchiveSearchResult<DataFile> dfAsr = retrieveDataFile(dfInfo.getDepositId());
                        if (dfAsr == null) {
                            throw new ArchiveServiceException(String.format(ERROR_NULL_ASR, "DataFile", dfInfo.getDepositId()));
                        }
                        for (DataFile df : dfAsr.getResults()) {
                            di.addFile(df);
                        }
                    }
                }
            }
        }

        return diAsr;
    }

    private ArchiveSearchResult<DataFile> retrieveDataFile(String deposit_id)
            throws ArchiveServiceException {
        return retrieve(deposit_id, dataFile_mapper);
    }

    public List<String> listMetadataFiles(Status status) {
        return list(METADATA_FILE, status);
    }
    
    public ArchiveSearchResult<MetadataFile> retrieveMetadataFile(String deposit_id)
            throws ArchiveServiceException {
        return retrieve(deposit_id, metadataFile_mapper);
    }
    
    public List<ArchiveDepositInfo> listDepositInfo(String object_id,
                                                    Status status) {
        List<ArchiveDepositInfo> list;
        synchronized (createObjectIdLock(object_id)) {
            list = deposit_info_dao.listForObject(
                    object_id, status);
        }
        return list;        
    }

    @Override
    public ArchiveSearchResult<DataItem> retrieveDataSetsForCollection(
            String deposit_id, int numberOfResults, int offset) throws ArchiveServiceException {
        final ArchiveSearchResult<DataItem> diAsr =
                retrieveChildren(deposit_id, dataset_mapper, DATA_SET_COMPARATOR, numberOfResults, offset);

        for (DataItem di : diAsr.getResults()) {
            ArchiveDepositInfo adi = deposit_info_dao.listForObject(di.getId(), Status.DEPOSITED).get(0);
            for (ArchiveDepositInfo childAdi : deposit_info_dao.lookupChildren(adi.getDepositId())) {
                if (childAdi.getObjectType() == DATA_FILE ) {
                    final ArchiveSearchResult<DataFile> childAsr = retrieveDataFile(childAdi.getDepositId());
                    if (childAsr == null) {
                        throw new ArchiveServiceException("Unable to retrieve a DataFile for DataItem '" + di.getId() +
                                "': Searching for depositId '" + childAdi.getDepositId() + "' returned a null " +
                                "ArchiveSearchResult.");
                    }
                    
                    if (childAsr.getResultCount() == 0) {
                        throw new ArchiveServiceException("Unable to retrieve a DataFile for DataItem '" + di.getId() +
                                                          "': Searching for depositId '" + childAdi.getDepositId() + "' returned no results.");
                    }
                    
                    di.addFile(childAsr.getResults().iterator().next());
                }
            }
        }

        return diAsr;
    }

    private <T> ArchiveSearchResult<T> retrieveChildren(String deposit_id, DcpMapper<T> mapper,
                                                        Comparator<T> comparator, int numberOfResults, int offset) throws ArchiveServiceException {

        if (ancestrySearcher == null) {
            throw new IllegalStateException("AncestrySearcher must not be null: see setAncestrySearcher(...)");
        }

        if (parentSearcher == null) {
            throw new IllegalStateException("ParentSearcher must not be null: see setParentSearcher(...)");
        }

        LinkedList<T> children = new LinkedList<T>();
        int totalResults = -1;

        // The ADI Deliverable Unit of the deposit; this DU is the head of the object graph.
        ArchiveDepositInfo info = deposit_info_dao.lookup(deposit_id);
        if (info == null || info.getDepositStatus() != Status.DEPOSITED) {
            return null;
        }

        try {
            java.util.Collection<? extends DcsEntity> parents = parentSearcher.getParentsOf(info.getArchiveId(),
                    DcsDeliverableUnit.class);
            parents = filterForLatestDeliverableUnits(parents);

            Iterator<? extends DcsEntity> iter = parents.iterator();

            while (iter.hasNext()) {
                // Retrieve all of the children of each DU that has the deposit DU as a parent.
                DcsEntity e = iter.next();
                if (e == null || !(e instanceof DcsDeliverableUnit)) {
                    continue;
                }

                DcsDeliverableUnit du = (DcsDeliverableUnit) e;
                java.util.Collection<DcsEntity> entities = ancestrySearcher.getAncestorsOf(du.getId(), true);
                entities = filterForLatestDeliverableUnits(entities);

                if (entities.isEmpty()) {
                    throw new ArchiveServiceException("No ancestors found for Deliverable Unit " + du.getId());
                }

                Dcp dcp = new Dcp();
                DcpUtil.add(dcp, entities);

                T entity = null;
                if (dcp != null) {
                    entity = mapper.fromDcp(dcp);
                }

                if (entity != null) {
                    children.add(entity);
                }
            }

            totalResults = children.size();

        } catch (RuntimeException e) {
            throw new ArchiveServiceException("Error retrieving children for " + info.getArchiveId() +
                    ": " + e.getMessage(), e);
        } catch (DcpMappingException e) {
            throw new ArchiveServiceException("Error mapping search results for " + info.getArchiveId() + ": " +
                    e.getMessage(), e);
        }

        handleOffsetAndMax(comparator, numberOfResults, offset, children);

        return new ArchiveSearchResult<T>(children, totalResults);
    }

    /**
     * Accepts a LinkedList of search results, and applies the {@code offset} and {@code maxResults} to the results.
     * <p/>
     * The result list is first sorted using the supplied {@code comparator}.
     * <p/>
     * If a positive offset is supplied, and it is less than the total size of the result list, then the result list up
     * to the offset is cleared.  If the offset is greater than the total size of the list, it is cleared.
     * <p/>
     * If a positive maxResults is supplied and the total size of the result is is greater than maxResults, the extra
     * results are cleared from the list.
     *
     * @param comparator the comparator used to sort the results.
     * @param maxResults the maximum number of results the list should contain
     * @param offset the offset into the list that the results should start with
     * @param results a mutable LinkedList containing the search results
     * @param <T> the type of business object the search results are for
     */
    private <T> void handleOffsetAndMax(Comparator<T> comparator, int maxResults, int offset, LinkedList<T> results) {
        // Sort result.
        Collections.sort(results, comparator);

        // Handle offset.
        if (offset > 0 && offset < results.size()) {
            // Begin the list at the specified offset, as long as the
            // offset is less than the size of the result.
            results.subList(0, offset).clear();
        } else if (offset > 0 && offset >= results.size()) {
            // If the offset is equal or larger than the size
            // of the result, then clear the list.
            results.clear();
        }

        // Handle max results.
        if (maxResults > 0 && results.size() > maxResults) {
            results.subList(maxResults, results.size()).clear();
        }
    }

    private java.util.Collection<DcsEntity> filterForLatestDeliverableUnits(java.util.Collection<? extends DcsEntity> entities) {
        java.util.Collection<DcsEntity> result = new HashSet<DcsEntity>();
        List<String> predecessorDuIds = new ArrayList<String>();

        for (DcsEntity entity : entities) {
            if (!(entity instanceof DcsDeliverableUnit)) {
                continue;
            }

            DcsDeliverableUnit du = (DcsDeliverableUnit)entity;

            for (DcsRelation rel : du.getRelations()) {
                if (rel.getRelUri().equals(DcsRelationship.IS_SUCCESSOR_OF.asString())) {
                    predecessorDuIds.add(rel.getRef().getRef());
                }
            }
        }

        for (DcsEntity entity : entities) {
            if (!(entity instanceof DcsDeliverableUnit)) {
                result.add(entity);
                continue;
            }

            DcsDeliverableUnit du = (DcsDeliverableUnit) entity;

            if (!predecessorDuIds.contains(du.getId())) {
                result.add(du);
            }
        }

        return result;
    }

    /**
     * This method creates a lock based on a business object id, though theoretically it could be used to create a
     * lock using any String object.  This method returns the same object for each unique {@code objectId}.  That is to
     * say, multiple invocations of this method using the same {@code objectId} will return identical objects.
     * <p/>
     * The idea is that if the {@code ArchiveServiceImpl} needs to operate exclusively on a business object, it must
     * request a lock object from this method, synchronize on it, and perform its task.
     * <p/>
     * Sample usage:
     * <pre>
     *     DataItem di = ...;
     *     synchronized (createObjectIdLock(di.getId()) {
     *         // perform exclusive operations
     *     }
     * </pre>
     *
     * @param objectId intended to be the business identifier of the object that requires exclusive access
     * @return an object intented to be used as a mutual exclusion lock for {@code objectId}
     */
    private static String createObjectIdLock(String objectId) {
        final String lockSuffix = "-ArchiveServiceLock";

        return (objectId + lockSuffix).intern();
    }
}

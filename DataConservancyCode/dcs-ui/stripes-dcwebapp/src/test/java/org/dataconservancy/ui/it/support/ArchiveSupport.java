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
package org.dataconservancy.ui.it.support;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.access.connector.DcsConnectorFault;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.model.dcs.support.DcpUtil;
import org.dataconservancy.ui.dcpmap.AbstractVersioningMapper;
import org.dataconservancy.ui.dcpmap.DcpMapper;
import org.dataconservancy.ui.dcpmap.MetadataFileMapper;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.DcpMappingException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.profile.DataItemProfile;
import org.dataconservancy.ui.profile.Profile;
import org.dataconservancy.ui.profile.Profiler;
import org.dataconservancy.ui.services.AncestrySearcher;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.BusinessObjectState;
import org.dataconservancy.ui.services.DataFileBusinessObjectSearcher;
import org.dataconservancy.ui.services.DataItemBusinessObjectSearcher;
import org.dataconservancy.ui.services.MetadataFileBusinessObjectSearcher;
import org.dataconservancy.ui.services.ParentSearcher;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.dataconservancy.ui.util.SolrQueryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support class for interacting with the DCS Archive from ITs.  It encapsulates logic for performing queries against
 * the DCS, including re-usable polling and back-off mechanisms.  <em>This class does not interact with the UI </em>
 * {@code ArchiveService}.</em>
 * <p/>
 * By default, when a polling query like {@link #pollAndQueryArchiveForDataItemDu(String)} is executed, this class will
 * immediately execute a query against the DCS.  If the DU isn't found, it will re-try up to {@link #getPollCount()}
 * times, pausing {@code (pollCount * getPollDelayFactorMs())} milliseconds between polling attempts. Additional methods
 * can be added as needed to this class.  The polling logic can be re-used by implementing the inner
 * {@code PollQuery} interface.
 * <p/>
 * <strong>N.B.:</strong> Not to be mistaken for interacting with the
 * {@link org.dataconservancy.ui.services.ArchiveService}; this interacts directly with the public APIs of the DCS.
 */
public class ArchiveSupport {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DcsConnector connector;
    private final Profile<DataItem> dataItemProfile;
    private final Profiler<DataItem> dataItemProfiler;
    private final DcpMapper<DataItem> dataSetMapper;
    private final DcpMapper<DataFile> dataFileMapper;
    private final DcpMapper<Collection> collectionMapper;
    private final DcpMapper<MetadataFile> metadataFileMapper;
    private final DataItemBusinessObjectSearcher dataItemBusinessObjectSearcher;
    private final DataFileBusinessObjectSearcher dataFileBusinessObjectSearcher;
    private final MetadataFileBusinessObjectSearcher metadataFileBusinessObjectSearcher;
    private final AncestrySearcher ancestrySearcher;
    private final ParentSearcher parentSearcher;
    private final DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    private static final int DEFAULT_POLL_DELAY_FACTOR_MS = 500;
    private static final int DEFAULT_POLL_COUNT = 30;

    private int pollDelayFactorMs = DEFAULT_POLL_DELAY_FACTOR_MS;
    private int pollCount = DEFAULT_POLL_COUNT;

    public ArchiveSupport(DcsConnector connector,
                          Profile<DataItem> dataItemProfile,
                          Profiler<DataItem> profiler,
                          DcpMapper<DataItem> dataItemMapper,
                          DcpMapper<DataFile> dataFileMapper,
                          DcpMapper<Collection> collectionMapper,
                          DataItemBusinessObjectSearcher dataItemBusinessObjectSearcher,
                          DataFileBusinessObjectSearcher dataFileBusinessObjectSearcher,
                          MetadataFileMapper metadataFileMapper,
                          MetadataFileBusinessObjectSearcher metadataFileBusinessObjectSearcher,
                          AncestrySearcher ancestrySearcher,
                          ParentSearcher parentSearcher) {
        this.connector = connector;
        this.dataItemProfile = dataItemProfile;
        this.dataItemProfiler = profiler;
        this.dataSetMapper = dataItemMapper;
        this.dataFileMapper = dataFileMapper;
        this.collectionMapper = collectionMapper;
        this.dataItemBusinessObjectSearcher = dataItemBusinessObjectSearcher;
        this.dataFileBusinessObjectSearcher = dataFileBusinessObjectSearcher;
        this.metadataFileMapper = metadataFileMapper;
        this.metadataFileBusinessObjectSearcher = metadataFileBusinessObjectSearcher;
        this.ancestrySearcher = ancestrySearcher;
        this.parentSearcher = parentSearcher;
    }

    /**
     * When this class queries the DCS, it will re-try this many times before giving up.
     *
     * @return the number of times a query will be executed against the DCS before giving up.
     */
    public int getPollCount() {
        return pollCount;
    }

    /**
     * When this class queries the DCS, it will re-try {@link #getPollCount()} times before giving up.
     *
     * @param pollCount the number of times a query will be executed against the DCS before giving up.
     */
    public void setPollCount(int pollCount) {
        if (pollCount < 1) {
            throw new IllegalArgumentException("Count must be a positive integer");
        }
        this.pollCount = pollCount;
    }


    /**
     * When this class queries the DCS, it will use this delay factor to determine how long it should
     * pause between polling attempts.
     *
     * @return the poll delay factor, in milliseconds
     */
    public int getPollDelayFactorMs() {
        return pollDelayFactorMs;
    }

    /**
     * When this class queries the DCS, it will use {@link #getPollDelayFactorMs()} to determine how long it should
     * pause between polling attempts.
     *
     * @param pollDelayFactorMs the poll delay factor, in milliseconds
     */
    public void setPollDelayFactorMs(int pollDelayFactorMs) {
        if (pollDelayFactorMs < 1) {
            throw new IllegalArgumentException("Delay factor must be a positive integer");
        }
        this.pollDelayFactorMs = pollDelayFactorMs;
    }


    /**
     * Polls the DCS for a Deliverable Unit representing the identified Collection object.
     * <p/>
     * See {@link #setPollCount(int)} and {@link #setPollDelayFactorMs(int)} to control how many times this
     * method polls the archive, and how long it waits between retries.
     *
     * @param businessId the business ID of the Collection
     * @return the DU representing the Collection, or null
     */
    public DcsDeliverableUnit pollAndQueryArchiveForCollectionDu(final String businessId) {
        return pollAndQueryArchive(pollDelayFactorMs, pollCount, new PollQuery<DcsDeliverableUnit>() {
            @Override
            public DcsDeliverableUnit execute() throws DcsConnectorFault {
                final String query = SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit",
                        "former", businessId, "type", AbstractVersioningMapper.ROOT_DELIVERABLE_UNIT_TYPE);
                log.trace("Executing query: {}", query);
                Iterator<DcsEntity> entities = connector.search(query);
                List<DcsDeliverableUnit> dus = new ArrayList<DcsDeliverableUnit>();
                
                while (entities.hasNext()) {
                    DcsEntity entity = entities.next();
                    if (!(entity instanceof DcsDeliverableUnit)) {
                        continue;
                    }

                    DcsDeliverableUnit du = (DcsDeliverableUnit)entity;

                    // Figure out the Root DU: the DU with no predecessor that has no parents
                    if (getPredecessorId(du) == null && du.getParents().isEmpty()) {
                        log.trace("Found collection DU: {}", du);
                        return du;
                    }
                }

                return null;
            }
        });
    }
    
    /**
     * Polls the DCS for a Deliverable Unit representing the identified MetadataFile object.
     * <p/>
     * See {@link #setPollCount(int)} and {@link #setPollDelayFactorMs(int)} to control how many times this
     * method polls the archive, and how long it waits between retries.
     *
     * @param businessId the business ID of the MetadataFile
     * @return the DU representing the MetadataFile, or null
     */
    public DcsDeliverableUnit pollAndQueryArchiveForMetadataFileDu(final String businessId) {
        return pollAndQueryArchive(pollDelayFactorMs, pollCount, new PollQuery<DcsDeliverableUnit>() {
            @Override
            public DcsDeliverableUnit execute() throws DcsConnectorFault {
                final String query = SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit",
                        "former", businessId, "type", AbstractVersioningMapper.ROOT_DELIVERABLE_UNIT_TYPE);
                log.trace("Executing query: {}", query);
                Iterator<DcsEntity> entities = connector.search(query);
                List<DcsDeliverableUnit> dus = new ArrayList<DcsDeliverableUnit>();
                
                while (entities.hasNext()) {
                    DcsEntity entity = entities.next();
                    if (!(entity instanceof DcsDeliverableUnit)) {
                        continue;
                    }

                    DcsDeliverableUnit du = (DcsDeliverableUnit)entity;

                    // Figure out the Root DU: the DU with no predecessor that has no parents
                    if (getPredecessorId(du) == null && du.getParents().isEmpty()) {
                        log.trace("Found collection DU: {}", du);
                        return du;
                    }
                }

                return null;
            }
        });
    }
    
    private String getPredecessorId(DcsDeliverableUnit du) {
        for (DcsRelation rel : du.getRelations()) {
            if (rel.getRelUri().equals(DcsRelationship.IS_SUCCESSOR_OF.asString())) {
                if (rel.getRef() == null || rel.getRef().getRef() == null) {
                    return null;
                }

                return rel.getRef().getRef();
            }
        }

        return null;
    }

    /**
     * Polls the DCS for a Deliverable Unit representing the identified DataItem object.
     * <p/>
     * See {@link #setPollCount(int)} and {@link #setPollDelayFactorMs(int)} to control how many times this
     * method polls the archive, and how long it waits between retries.
     *
     * @param businessId the business ID of the DataItem
     * @return the DU representing the DataItem, or null
     */
    public DcsDeliverableUnit pollAndQueryArchiveForDataItemDu(final String businessId) {
        return pollAndQueryArchive(pollDelayFactorMs, pollCount, new PollQuery<DcsDeliverableUnit>() {
            @Override
            public DcsDeliverableUnit execute() throws DcsConnectorFault {
                final String query = SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit", "type",
                        dataItemProfile.getType(), "former", businessId);
                log.trace("Executing query: {}", query);
                Iterator<DcsEntity> entities = connector.search(query);

                DcsDeliverableUnit datasetDu = null;
                while (entities.hasNext() && datasetDu == null) {
                    DcsDeliverableUnit du = (DcsDeliverableUnit) entities.next();
                    if (du.getFormerExternalRefs().contains(businessId)) {
                        datasetDu = du;
                    }
                }

                return datasetDu;
            }
        });
    }
    
    /**
     * Polls the DCS for an update to the Deliverable Unit representing the identified DataItem object.
     * <p/>
     * See {@link #setPollCount(int)} and {@link #setPollDelayFactorMs(int)} to control how many times this
     * method polls the archive, and how long it waits between retries.
     *
     * @param businessId the business ID of the DataItem
     * @param datasetName the name attached to the updated dataset
     * @return the DU representing the DataItem, or null
     */
    public DcsDeliverableUnit pollAndQueryArchiveForUpdatedDataItemDu(final String businessId, final String datasetName) {
        return pollAndQueryArchive(pollDelayFactorMs, pollCount, new PollQuery<DcsDeliverableUnit>() {
            @Override
            public DcsDeliverableUnit execute() throws DcsConnectorFault {
                BusinessObjectState state = dataItemBusinessObjectSearcher.findLatestState(businessId);
                if (state == null || state.getLatestState() == null || state.getRoot() == null) {
                    return null;
                }

//                System.err.println("Want business id '" + businessId + "' and name '" + datasetName + "':\n" + state);

                if (!state.getLatestState().getTitle().equals(datasetName)) {
                    return null;
                }

                // This is a hack.
                // Basically, we obtain the state of the business object (which is comprised of a root du and a state
                // du), and create a composite DU that represents the state.  Use the State DU but set the ID to the
                // archive ID.
                DcsDeliverableUnit du = state.getLatestState();
                du.setId(state.getRoot().getId());
                return du;
            }
        });
    }

    /**
     * Polls the DCS for a Deliverable Unit representing the identified DataItem object.
     * <p/>
     * See {@link #setPollCount(int)} and {@link #setPollDelayFactorMs(int)} to control how many times this
     * method polls the archive, and how long it waits between retries.
     *
     * @param businessId the business ID of the DataItem
     * @return the DU representing the DataItem, or null
     */
    public DataItem pollAndQueryArchiveForDataItem(final String businessId) {
        final DcsDeliverableUnit dataSetDu = pollAndQueryArchiveForDataItemDu(businessId);

        if (dataSetDu == null) {
            return null;
        }

        return pollAndQueryArchive(pollDelayFactorMs, pollCount, new DataItemPollQuery(businessId));
    }

    /**
     * Polls the DCS for a collection for a supplied business id
     * <p/>
     * See {@link #setPollCount(int)} and {@link #setPollDelayFactorMs(int)} to control how many times this
     * method polls the archive, and how long it waits between retries.
     *
     * @param businessId
     * @return  the Collection, or null
     */
    public Collection pollAndQueryArchiveForCollection(final String businessId) {

        final DcsDeliverableUnit collectionDu = pollAndQueryArchiveForCollectionDu(businessId);
        if (collectionDu == null) {
            log.trace("No Collection DU found for {}", businessId);
            return null;
        }
        
        return pollAndQueryArchive(pollDelayFactorMs, pollCount, new PollQuery<Collection>() {
            @Override
            public Collection execute() throws DcsConnectorFault {
                String query = SolrQueryUtil.createLiteralQuery("ancestry", collectionDu.getId());
                log.trace("Executing query: {}", query);
                Iterator<DcsEntity> entities = connector.search(query);

                Dcp dcp = new Dcp();
                dcp.addEntity(collectionDu);
                while (entities.hasNext()) {
                    dcp.addEntity(entities.next());
                }
                try {
                    return collectionMapper.fromDcp(dcp);
                } catch (DcpMappingException e) {
                    final String msg = "Error mapping a Collection, maybe the DCP was incomplete: " + e.getMessage();
                    log.trace(msg, e);
                    throw new RuntimeException(msg, e);
                }
            }
        });
    }
    
    /**
     * Polls the DCS for a metadata file for a supplied business id
     * <p/>
     * See {@link #setPollCount(int)} and {@link #setPollDelayFactorMs(int)} to control how many times this
     * method polls the archive, and how long it waits between retries.
     *
     * @param businessId
     * @return  the MetadataFile, or null
     */
    public MetadataFile pollAndQueryArchiveForMetadataFile(final String businessId) {

        final DcsDeliverableUnit metadataFileDu = pollAndQueryArchiveForMetadataFileDu(businessId);
        if (metadataFileDu == null) {
            log.trace("No Collection DU found for {}", businessId);
            return null;
        }
        
        return pollAndQueryArchive(pollDelayFactorMs, pollCount, new PollQuery<MetadataFile>() {
            @Override
            public MetadataFile execute() throws DcsConnectorFault {
                String query = SolrQueryUtil.createLiteralQuery("ancestry", metadataFileDu.getId());
                log.trace("Executing query: {}", query);
                Iterator<DcsEntity> entities = connector.search(query);

                Dcp dcp = new Dcp();
                dcp.addEntity(metadataFileDu);
                while (entities.hasNext()) {
                    dcp.addEntity(entities.next());
                }
                try {
                    return metadataFileMapper.fromDcp(dcp);
                } catch (DcpMappingException e) {
                    final String msg = "Error mapping a Collection, maybe the DCP was incomplete: " + e.getMessage();
                    log.trace(msg, e);
                    throw new RuntimeException(msg, e);
                }
            }
        });
    }

    /**
     * Returns a list of deliverable units that are in a collection
     * <p/>
     * See {@link #setPollCount(int)} and {@link #setPollDelayFactorMs(int)} to control how many times this
     * method polls the archive, and how long it waits between retries.
     *
     * @param collectionId    the collection archive identifier
     * @param expectedDuCount the number of expected members in the collection
     * @return the DeliverableUnits that are members of the collection
     */
    public Set<DcsDeliverableUnit> pollAndQueryArchiveForDUsInCollection(final String collectionId, final int expectedDuCount) {
        final Set<DcsDeliverableUnit> dusInCollection = new HashSet<DcsDeliverableUnit>();

        pollAndQueryArchive(pollDelayFactorMs, pollCount, new PollQuery() {
            @Override
            public DcsDeliverableUnit execute() throws DcsConnectorFault {
                final String query = SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit", "type",
                        dataItemProfile.getType(), "parent", collectionId);
                log.trace("Executing query: {}", query);
                Iterator<DcsEntity> entities = connector.search(query);

                while (entities.hasNext()) {
                    DcsDeliverableUnit du = (DcsDeliverableUnit) entities.next();
                    log.trace("Found item {} in collection {}", du, collectionId);
                    dusInCollection.add(du);
                }

                // If the collection doesn't have the expected number of items, it could be that the
                // DCS is still processing the deposits.  So we want to keep polling until we have the expected
                // number of items in the collection.
                if (dusInCollection.size() < expectedDuCount) {
                    log.trace("Continuing to poll because we only have {} of the {} expected items in the collection",
                            dusInCollection.size(), expectedDuCount);
                    return null;  // signify that we want polling to continue by returning null.
                } else {
                    log.trace("Exiting polling because we only have at least {} items in the collection", expectedDuCount);
                    return new DcsDeliverableUnit();  // signify that we want polling to stop by returning a DU.
                }
            }
        });

        return dusInCollection;
    }
    
    /**
     * Returns a list of data items that are in a collection
     * <p/>
     * See {@link #setPollCount(int)} and {@link #setPollDelayFactorMs(int)} to control how many times this
     * method polls the archive, and how long it waits between retries.
     *
     * @param collectionId    the collection archive identifier
     * @param expectedDataItemCount the number of expected members in the collection
     * @return the DataItems that are members of the collection
     */
    public Set<DataItem> pollAndQueryArchiveForDataItemsInCollection(final String collectionId, final int expectedDataItemCount) {
        final Set<DataItem> dataItemsInCollection = new HashSet<DataItem>();

        Set<DcsDeliverableUnit> dusInCollection = pollAndQueryArchiveForDUsInCollection(collectionId, expectedDataItemCount);
        if (dusInCollection != null && !dusInCollection.isEmpty()) {
            for (final DcsDeliverableUnit du : dusInCollection) {

                if (!DataItemProfile.DATASET_TYPE.equals(du.getType())) {
                    continue;
                }

                pollAndQueryArchive(pollDelayFactorMs, pollCount, new PollQuery<DataItem>() {
                    @Override
                    public DataItem execute() throws DcsConnectorFault {
                        String diId = findDataItemBusinessId(du);
                        if (diId == null) {
                            return new DataItem();
                        }

                        final DataItem di = pollAndQueryArchive(pollDelayFactorMs, pollCount,
                                new DataItemPollQuery(diId));
                        if (di != null) {
                            dataItemsInCollection.add(di);
                            return di;
                        }

                        return new DataItem();
                    }
                });
            }
        }

        return dataItemsInCollection;
    }

    /**
     * A query to determine the number of items in a collection in the archive.
     *
     * @param collectionId The dcs archive id of the collection to be checked.
     * @return The number of deliverable units in the collection
     */
    public int queryArchiveForNumberOfItemsInCollection(final String collectionId) {
        final String query = SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit", "type",
                dataItemProfile.getType(), "parent", collectionId);
        log.trace("Executing query: {}", query);
        Iterator<DcsEntity> entities = null;
        try {
            entities = connector.search(query);
        } catch (DcsConnectorFault e) {
            log.error("Error querying archive: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }

        int returnCount = 0;

        if (entities != null) {
            while (entities.hasNext()) {
                entities.next();
                returnCount++;
            }
        }
        return returnCount;
    }

    /**
     * Queries for the manifestations associated with a deliverable unit.
     *
     * @param deliverableUnitId The dcs archive id of the deliverable unit to check for manifestations
     * @return The set of manifestations associated with the deliverable unit.
     */
    public Set<DcsManifestation> queryDeliverableUnitForManifestations(final String deliverableUnitId) {
        final String query = SolrQueryUtil.createLiteralQuery("AND", "entityType", "Manifestation", "ancestry", deliverableUnitId);
        final Set<DcsManifestation> duManifestations = new HashSet<DcsManifestation>();

        log.trace("Executing query: {}", query);
        Iterator<DcsEntity> entities = null;
        try {
            entities = connector.search(query);
        } catch (DcsConnectorFault e) {
            log.error("Error querying archive: " + e.getMessage(), e);
        }


        if (entities != null) {
            while (entities.hasNext()) {
                DcsManifestation man = (DcsManifestation) entities.next();
                log.trace("Found item {} in collection {}", man, deliverableUnitId);
                duManifestations.add(man);
            }
        }
        return duManifestations;
    }

    /**
     * Polls the archive by performing a search of the archive, returning the object from
     * {@link org.dataconservancy.ui.it.support.ArchiveSupport.PollQuery#execute()}.  The returned object may be
     * {@code null}.
     * <p/>
     * The archive is polled {@code times} number of times, waiting {@code delayFactorInMs * times} between each
     * search query.  For example, if this method is called with a 1000ms delay factor, and times equal to 3, the first
     * query will happen with no delay.  If the object is not found ({@code PollQuery.execute(...)} returns
     * {@code null}), then the next call is delayed 1000ms. Subsequent calls will be delayed by 2000ms and 3000ms,
     * respectively.
     *
     * @param delayFactorInMs used to calculate the delay in milliseconds between archie search queries
     * @param times           the number of times to poll the archive
     * @param query           the query logic
     * @return the Deliverable Unit representing the object represented by {@code businessId}.  The return may be {@code null}.
     */
    public <T> T pollAndQueryArchive(int delayFactorInMs, int times, PollQuery<T> query) {
        T object = null;
        int count = 0;
        do {
            final long sleepInterval = count * delayFactorInMs;
            log.trace("Polling archive {} of {} times; sleeping {} ms",
                    new Object[]{count, (times - 1), sleepInterval});
            try {
                Thread.sleep(sleepInterval);
                try {
                    object = query.execute();
                } catch (DcsConnectorFault e) {
                    log.error("Error querying archive: " + e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            } catch (InterruptedException e) {
                // ignore
            }
            count++;
        } while (object == null && count < times);

        return object;
    }

    /**
     * Returns a StringBuilder which lists the collections that are in the archive.
     *
     * @param archiveService the ArchiveService to interrogate
     * @return a StringBuilder
     */
    public static StringBuilder dumpCollectionsInArchive(ArchiveService archiveService) {
        final StringBuilder message = new StringBuilder();
        if (archiveService.listCollections(null).size() > 0) {
            message.append("There were existing collections in the archive:\n");
            for (String id : archiveService.listCollections(null)) {
                final ArchiveSearchResult<Collection> searchResult;
                try {
                    searchResult = archiveService.retrieveCollection(id);
                } catch (ArchiveServiceException e) {
                    message.append("Error retrieving collection [").append(id).append("]: ").append(e.getMessage()).append("\n");
                    continue;
                }
                for (Collection c : searchResult.getResults()) {
                    message.append(c.toString()).append("\n");
                }
            }
        } else {
            message.append("There were no collections in the archive.\n");
        }

        return message;
    }

    /**
     * Encapsulates query logic
     */
    public interface PollQuery<T> {
        public T execute() throws DcsConnectorFault;
    }

    private class DataItemPollQuery implements PollQuery<DataItem> {
        private final String businessId;

        public DataItemPollQuery(String businessId) {
            this.businessId = businessId;
        }

        @Override
        public DataItem execute() throws DcsConnectorFault {
            final BusinessObjectState diState = dataItemBusinessObjectSearcher.findLatestState(businessId);
            if (diState == null) {
                return null;
            }

            try {
                final Dcp diDcp = new Dcp();
                for (DcsEntity e : ancestrySearcher.getAncestorsOf(diState.getLatestState().getId(), true)) {
                    diDcp.addEntity(e);
                    diDcp.addEntity(diState.getRoot());
                }

                if (dataItemProfiler.conforms(diDcp)) {
                    final DataItem di = dataSetMapper.fromDcp(diDcp);
                    for (DcsDeliverableUnit dfDu : parentSearcher.getParentsOf(
                            diState.getLatestState().getId(), DcsDeliverableUnit.class)) {
                        final Dcp dfDcp = new Dcp();
                        for (DcsEntity e : ancestrySearcher.getAncestorsOf(dfDu.getId(), true)) {
                            dfDcp.addEntity(e);
                        }
                        di.addFile(dataFileMapper.fromDcp(dfDcp));
                    }
                    return di;
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    builder.buildSip(diDcp, baos);
                    final String msg = "DCP for business id '" + businessId + "' did not conform to profile " +
                            dataItemProfile + " (DCP was '" + new String(baos.toByteArray()) + "')";
                    log.trace(msg);
                    throw new RuntimeException(msg);
                }

            } catch (DcpMappingException e) {
                log.trace("Error mapping a DataItem, maybe the DCP was incomplete: " + e.getMessage(), e);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        builder.buildSip(diDcp, baos);
//                        final String msg = "Error mapping a DataItem (business id '" + businessId + "'), maybe the " +
//                                "DCP was incomplete:  (query was: '" + query + "', DCP was '" +
//                                new String(baos.toByteArray()) + "')";
//                        log.trace(msg);
                    throw new RuntimeException("foo", e);
            }
        }
    }

    private String findDataItemBusinessId(DcsDeliverableUnit du) {
        for (String extRef : du.getFormerExternalRefs()) {
            if (extRef.contains("/item/")) {
                return extRef;
            }
        }
        return null;
    }
}

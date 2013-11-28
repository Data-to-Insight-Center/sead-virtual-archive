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
package org.dataconservancy.ui.dcpmap;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.security.MessageDigest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.util.DigestNotificationStream;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.profile.support.CollectionMatchStrategy;
import org.dataconservancy.profile.support.MatchOp;
import org.dataconservancy.profile.support.ProfileStatement;
import org.dataconservancy.ui.exceptions.DcpMappingException;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Id;
import org.dataconservancy.ui.profile.DataFileProfile;
import org.dataconservancy.ui.profile.DataItemProfile;
import org.dataconservancy.ui.profile.Profiler;
import org.dataconservancy.ui.services.ArchiveUtil;
import org.dataconservancy.ui.services.BusinessObjectSearcher;
import org.dataconservancy.ui.services.BusinessObjectState;
import org.dataconservancy.ui.services.DataItemBusinessObjectSearcher;
import org.dataconservancy.ui.services.MetadataFileBusinessObjectSearcher;
import org.dataconservancy.ui.util.DcpUtil;
import org.dataconservancy.ui.util.HexEncodingDigestListener;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * This class provides methods to tranform (or map) {@link org.dataconservancy.ui.model.DataItem} objects into valid
 * {@link org.dataconservancy.model.dcp.Dcp} objects and to construct {@link org.dataconservancy.ui.model.DataItem} objects
 * from valid {@link org.dataconservancy.model.dcp.Dcp} objects.
 */
public class DataSetMapper extends GenericMapper<DataItem> implements Profiler<DataItem> {

    /**
     * Format for the "not conformant" error message.
     * Fields: business object type, profile type, profile version, reason
     */
    private static final String ERR_NOT_CONFORMANT = "DCP package did not conform to the %s profile %s:%s: %s";

    /**
     * Logger
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Used to handle updates to business objects already deposited in the archive.
     */
    private DataItemBusinessObjectSearcher boSearcher;
    
    /**
     * Used to look up metadata files already deposited in the archive.
     */
    private MetadataFileBusinessObjectSearcher mfSearcher;

    /**
     * Used to retrieve references that target Files in the archive
     */
    private ArchiveUtil archiveUtil;

    /**
     * Profile statement which evaluates the Root DU &lt;type> of Deliverable Units.
     * Can also be injected.
     */
    private final ProfileStatement duType = new ProfileStatement(MatchOp.EQUAL_TO, DataItemProfile.DATASET_TYPE);

    /**
     * Profile statement which evaluates the State DU &lt;type> of Deliverable Units.
     * Can also be injected.
     */
    private final ProfileStatement duStateType = new ProfileStatement(MatchOp.EQUAL_TO, DataItemProfile.DATASET_STATE_TYPE);

    static final String DATASET_METADATA_TECHENV = DataItemProfile.DATASET_TYPE + ":" + DataItemProfile.DATASET_MAPPER_VERSION + ":DataSetMetadata";

    /**
     * Profile statement which evaluates the technical environment of Manifestations for Data Set Metadata.
     * Can also be injected.
     */
    private final ProfileStatement dataSetMetadataTechEnv = new ProfileStatement(MatchOp.EQUAL_TO, DATASET_METADATA_TECHENV);

    static final String DATASET_FILE_METADATA_TECHENV =  DataItemProfile.DATASET_TYPE + ":" + DataItemProfile.DATASET_MAPPER_VERSION + ":DataSetFileMetadata";

    private final ProfileStatement dataSetFileMetadataTechEnv = new ProfileStatement(MatchOp.EQUAL_TO, DATASET_FILE_METADATA_TECHENV);

    /**
     * The conforming Data Set Root Deliverable Unit (set by {@link #conforms(org.dataconservancy.model.dcp.Dcp)}.  If a
     * package does not conform, this will be {@code null}.
     */
    private DcsDeliverableUnit dataSetDu;

    /**
     * The conforming Data Set State Deliverable Unit (set by {@link #conforms(org.dataconservancy.model.dcp.Dcp)}.  If
     * a package does not conform, this will be {@code null}.
     */
    private DcsDeliverableUnit dataSetStateDu;

    /**
     * The conforming Data Set Manifestation that holds Data Set Metadata (set by
     * {@link #conforms(org.dataconservancy.model.dcp.Dcp)}. If a package does not conform, this will be {@code null}.
     */
    private DcsManifestation dataSetMetadata;

    /**
     * The conforming Data Set Manifestation that holds Data Set File metadata (set by
     * {@link #conforms(org.dataconservancy.model.dcp.Dcp)}.  If a package does not conform, this will be {@code null}.
     */
    private DcsManifestation dataSetFileMetadata;

    /**
     * The conforming DCP package (set by {@link #conforms(org.dataconservancy.model.dcp.Dcp)}. If a package does not
     * conform, this will be {@code null}.
     */
    private Dcp conformingPackage;

    public DataSetMapper(DataItemBusinessObjectSearcher bo, ArchiveUtil archiveUtil,
                         MetadataFileBusinessObjectSearcher mf) {
        if (bo == null) {
            throw new IllegalArgumentException("Business Object Searcher must not be null!");
        }

        if (archiveUtil == null) {
            throw new IllegalArgumentException("Archive Util must not be null!");
        }

        this.boSearcher = bo;
        this.mfSearcher = mf;
        this.archiveUtil = archiveUtil;
    }

    /**
     * {@inheritDoc}
     *
     * @param dcp
     * @return DataItem
     * @throws DcpMappingException
     */
    @Override
    public DataItem fromDcp(Dcp dcp) throws DcpMappingException {
        if (!conforms(dcp)) {
            return null;
        }

        return get(discover(dcp).iterator().next(), null);
    }
    
    /**
     * {@inheritDoc}
     *
     * Produces an archival package that encodes the information of a {@link DataItem} and its contained {@link org.dataconservancy.ui.model.DataFile}s.
     * The {@link DataItemProfile#DATASET_TYPE} and {@link DataItemProfile#DATASET_TYPE} are recorded in the archival package to aid deserialization by
     * {@link #fromDcp(org.dataconservancy.model.dcp.Dcp)}.
     *
     * If the business id of DataItem is the same as an already deposited DataItem, then toDcp produces a SIP whose
     * root DU is the successor of the root DU of the already deposited DataItem. Similarly, a File may indicate it already
     * exists in the archive by setting its id and path to that of the already deposited file. A DcsFile will not be
     * created for such a File. Instead a ManifestationFile will point to the existing DcsFile.
     *
     * @param parent_entity_id {@inheritDoc}
     * @param dataItem {@inheritDoc}
     * @return {@inheritDoc}
     * @throws DcpMappingException {@inheritDoc}
     */
    @Override
    public Dcp toDcp(String parent_entity_id, DataItem dataItem) throws DcpMappingException {
        if (boSearcher == null) {
            throw new IllegalStateException("BusinessObjectSearcher is not set.");
        }
        
        // Insure that we have all the info we need in the business objects before we begin to map the objects.
        try {
            assertValidity(parent_entity_id, dataItem);
        } catch (IllegalArgumentException e) {
            throw new DcpMappingException(e.getMessage(), e);
        }

        // Map the DataItem to a Deliverable Unit
        final DcsDeliverableUnit dataSetDu = mapDataSetDu(parent_entity_id, dataItem);

        final BusinessObjectState state = boSearcher.findLatestState(dataItem.getId());
        String predecessorId = null;
        if (state != null && state.getLatestState() != null) {
            predecessorId = state.getLatestState().getId();
        }

        final DcsDeliverableUnit dataSetStateDu = mapDataSetStateDu(dataSetDu.getId(), predecessorId, dataItem);
        
        // Generate a DcsFile that carries metadata for the DataItem.
        final DcsFile dataSetMd = mapDataSetMetadata(dataItem);

        // Generate a DcsFile that carries metadata for the DataFiles.
        final DcsFile dataFilesMd = mapDataFileMetadata(dataItem.getId(), dataItem.getFiles());

        // Finally, assemble the package
        final Dcp dcp = new Dcp();
        assemblePackage(dcp, dataSetDu, dataSetStateDu, dataSetMd, dataFilesMd, dataItem);

        return dcp;
    }

    @Override
    public boolean conforms(Dcp candidatePackage) {
        if (candidatePackage == null) {
            throw new IllegalArgumentException("DCP package must not be null.");
        }

        // Reset state
        dataSetDu = null;
        dataSetStateDu = null;
        dataSetMetadata = null;
        dataSetFileMetadata = null;
        conformingPackage = null;

        // Evaluate the package to see if a DeliverableUnit matching the proper <type> exists
        for (DcsDeliverableUnit candidateDu : candidatePackage.getDeliverableUnits()) {
            if (candidateDu.getType() != null) {
                if (duType.evaluate(candidateDu.getType())) {
                    dataSetDu = candidateDu;
                } else if (duStateType.evaluate(candidateDu.getType())) {
                    dataSetStateDu = candidateDu;
                }
            }
        }

        // There should be a DU representing the DataItem.  It is the head of the object graph
        if (dataSetDu == null) {
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "no Deliverable Unit for the Root DU of a DataItem was found.");
            log.debug(msg);
            return false;
        }

        // There should be a DU representing the state of the DataItem.  Its parent should be the the
        // dataSetDu (the root DU of the object graph).
        if (dataSetStateDu == null) {
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "no Deliverable Unit for the State DU of a DataItem was found.");
            log.debug(msg);
            return false;
        } else {
            boolean hasCorrectParent = false;

            for (DcsDeliverableUnitRef parentRef : dataSetStateDu.getParents()) {
                if (parentRef.getRef().equals(dataSetDu.getId())) {
                    hasCorrectParent = true;
                    break;
                }
            }

            if (!hasCorrectParent) {
                final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                        "The State DU of the DataItem is expected to have the Root DataItem DU as a parent.");
                log.debug(msg);
                return false;
            }
        }

        // The State DU and the Root DU should not have the same ID, and they shouldn't be equal
        if (dataSetDu.getId().equals(dataSetStateDu.getId())) {
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "The State DU and the Root DU of the DataItem should not have the same identifier.");
            log.debug(msg);
            return false;
        }

        if (dataSetDu.equals(dataSetStateDu)) {
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "The State DU and the Root DU of the DataItem should not be equal.");
            log.debug(msg);
            return false;
        }


        // ... One and only one Collection
        if (dataSetDu.getParents().size() > 1) {
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "Multiple parent collections were found, but a DataItem can only belong to one collection.");
            log.debug(msg);
            
            return false;
        }

        // Loop through the candidate Manifestations, determining which Manifestations represent
        // DataItem metadata, DataItem File metadata, and DataItem Files.
        for (DcsManifestation candidateManifestation : candidatePackage.getManifestations()) {
            if (!candidateManifestation.getDeliverableUnit().equals(dataSetStateDu.getId())) {
                continue;
            }

            final Collection<String> candidateTechEnv = candidateManifestation.getTechnicalEnvironment();

            if (candidateTechEnv != null) {
                if (DataFileProfile.DATA_SET_FILES_TECH_ENV.evaluate(candidateTechEnv, CollectionMatchStrategy.EXACTLY_ONE)) {
                    // noop
                } else if (dataSetMetadata == null &&
                        dataSetMetadataTechEnv.evaluate(candidateTechEnv, CollectionMatchStrategy.EXACTLY_ONE)) {
                    dataSetMetadata = candidateManifestation;
                } else if (dataSetFileMetadata == null &&
                        dataSetFileMetadataTechEnv.evaluate(candidateTechEnv, CollectionMatchStrategy.EXACTLY_ONE)) {
                    dataSetFileMetadata = candidateManifestation;
                }
            }
        }

        // DataSets should have metadata
        if (dataSetMetadata == null) {
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "no Manifestation for DataItem metadata was found");
            log.debug(msg);

            return false;
        }

        // DataItem Files should have corresponding metadata
        if (dataSetFileMetadata == null) {
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "no Manifestation for DataItem File metadata was found");
            log.debug(msg);

            return false;
        }

        if (!dataSetStateDu.getId().equals(dataSetMetadata.getDeliverableUnit())) {
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "DataItem metadata manifestation does not reference the correct Deliverable Unit");
            log.debug(msg);
            return false;
        }

        if (!dataSetStateDu.getId().equals(dataSetFileMetadata.getDeliverableUnit())) {
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "DataItem File metadata manifestation does not reference the correct Deliverable Unit");
            log.debug(msg);
            return false;
        }

        Map<String, DcsEntity> candidateMap = DcpUtil.asMap(candidatePackage);

        if (dataSetMetadata.getManifestationFiles().size() != 1) {
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "Incorrect number of DataItem metadata files.  Expected 1, was " +
                            dataSetMetadata.getManifestationFiles().size());
            log.debug(msg);
            return false;
        }

        for (DcsManifestationFile manF : dataSetMetadata.getManifestationFiles()) {
            String fileRef = manF.getRef().getRef();
            if (candidateMap.containsKey(fileRef) && candidateMap.get(fileRef) instanceof DcsFile) {
                continue;
            }
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "Package is missing DataItem metadata files.");
            log.debug(msg);
            return false;
        }

        if (dataSetFileMetadata.getManifestationFiles().size() != 1) {
            final String msg = String.format(ERR_NOT_CONFORMANT, "DataItem", DataItemProfile.DATASET_TYPE, DataItemProfile.DATASET_MAPPER_VERSION,
                    "Incorrect number of DataItem File metadata files.  Expected 1, was " +
                            dataSetFileMetadata.getManifestationFiles().size());
            log.debug(msg);
            return false;
        }

        conformingPackage = candidatePackage;
        return true;

    }

    @Override
    public Set<String> discover(Dcp conformingPackage) {
        Set<String> dataSetIdentifiers = new HashSet<String>();
        if (dataSetDu != null) {
            dataSetIdentifiers.add(dataSetDu.getId());
        }

        return dataSetIdentifiers;
    }

    @Override
    public DataItem get(String identifier, Map<String, Object> context) {
        if (conformingPackage == null) {
            return null;
        }

        if (!identifier.equals(dataSetDu.getId())) {
            return null;
        }

        DcsFile dataSetMetadataFile = null;
        DcsFile dataSetFileMetadataFile = null;
        final DcsFileRef dataSetMetadataFileRef = dataSetMetadata.getManifestationFiles().iterator().next().getRef();
        final DcsFileRef dataSetFileMetadataFileRef =
                dataSetFileMetadata.getManifestationFiles().iterator().next().getRef();

        // Check the package to see if the files are contained therein
        for (DcsFile f : conformingPackage.getFiles()) {
            if (f.getId().equals(dataSetMetadataFileRef.getRef())) {
                dataSetMetadataFile = f;
                continue;
            }
            if (f.getId().equals(dataSetFileMetadataFileRef.getRef())) {
                dataSetFileMetadataFile = f;
                continue;
            }
        }

        String ref = dataSetMetadataFileRef.getRef();
        if (dataSetMetadataFile == null) {
            // Then attempt to retrieve the reference from the archive
            dataSetMetadataFile = resolveFileRef(ref);
        }

        ref = dataSetFileMetadataFileRef.getRef();
        if (dataSetFileMetadataFile == null) {
            // Then attempt to retrieve the reference from the archive
            dataSetFileMetadataFile = resolveFileRef(ref);
        }

        Properties dataSetMetadata = new Properties();
        try {
            //metadata.load(connector.getStream(metadataFile.getSource()));
            dataSetMetadata.load(new URL(dataSetMetadataFile.getSource()).openStream());
        } catch (Exception e) {
            final String msg = "Error retrieving stream '" + dataSetMetadataFile.getSource() + "' from the archive: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        Properties dataSetFileMetadata = new Properties();
        try {
            dataSetFileMetadata.load(new URL(dataSetFileMetadataFile.getSource()).openStream());
        } catch (Exception e) {
            final String msg = "Error retrieving stream '" + dataSetFileMetadataFile.getSource() + "' from the archive: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        DataItem ds = new DataItem();

        // TODO: handle missing person!
        if (dataSetMetadata.getProperty(MetadataKey.DATASET_DEPOSITOR_ID) != null) {
            ds.setDepositorId(dataSetMetadata.getProperty(MetadataKey.DATASET_DEPOSITOR_ID));
        }

        if (dataSetMetadata.getProperty(MetadataKey.DATASET_ID) != null) {
            ds.setId(dataSetMetadata.getProperty(MetadataKey.DATASET_ID));
        }

        if (dataSetMetadata.getProperty(MetadataKey.DATASET_DESC) != null) {
            ds.setDescription(dataSetMetadata.getProperty(MetadataKey.DATASET_DESC));
        }

        if (dataSetMetadata.getProperty(MetadataKey.DATASET_DEPOSIT_DATE) != null) {
            ds.setDepositDate(DateTime.parse(dataSetMetadata.getProperty(MetadataKey.DATASET_DEPOSIT_DATE)));
        }

        if (dataSetMetadata.getProperty(MetadataKey.DATASET_NAME) != null) {
            ds.setName(dataSetMetadata.getProperty(MetadataKey.DATASET_NAME));
        }
        
        if (dataSetMetadata.getProperty(MetadataKey.DATASET_PARENT_ID) != null) {
            ds.setParentId(dataSetMetadata.getProperty(MetadataKey.DATASET_PARENT_ID));
        }

        return ds;
    }

    @Override
    public String toString() {
        return DataItemProfile.DATASET_TYPE + ":" + DataItemProfile.DATASET_MAPPER_VERSION;
    }

    /**
     * Return the {@link BusinessObjectSearcher} used by this mapper to retrieve entities (that represent business
     * objects) from the archive.
     *
     * @return the {@code BusinessObjectSearcher}, never {@code null}
     */
    BusinessObjectSearcher getBoSearcher() {
        return boSearcher;
    }

    /**
     * Set the {@link BusinessObjectSearcher} used by this mapper to retrieve entities (that represent business objects)
     * from the archive.
     *
     * @param boSearcher the {@code BusinessObjectSearcher}, must not be {@code null}
     * @throws IllegalArgumentException if {@code boSearcher} is {@code null}
     */
    void setBoSearcher(DataItemBusinessObjectSearcher boSearcher) {
        if (boSearcher == null) {
            throw new IllegalArgumentException("Business Object Searcher must not be null.");
        }
        this.boSearcher = boSearcher;
    }

    /**
     * Maps a DataItem to a Deliverable Unit.
     *
     * @param dataItem the DataItem to map
     * @return the DcsDeliverableUnit representing the DataItem
     * @throws DcpMappingException 
     */
    private DcsDeliverableUnit mapDataSetDu(String collectionId, DataItem dataItem) throws DcpMappingException {
        // Map DataItem and File identifiers to the DcsDeliverableUnit formerExternalRef
        final DcsDeliverableUnit dataSetDu = new DcsDeliverableUnit();
        dataSetDu.setId(dataItem.getId());
        dataSetDu.addFormerExternalRef(dataItem.getId());

        // Set the type of the DcsDeliverableUnit, indicating that this DU is carrying business information
        // for a DataItem object
        dataSetDu.setType(DataItemProfile.DATASET_TYPE);

        // Set the title of the DcsDeliverableUnit to the DataItem name
        dataSetDu.setTitle(dataItem.getName());

        // Set the Collection that this dataSetDu belongs to
        dataSetDu.addParent(new DcsDeliverableUnitRef(collectionId));
        
        return dataSetDu;
    }

    private DcsDeliverableUnit mapDataSetStateDu(String rootDuId, String predecesorDuId, DataItem dataItem) {
        final DcsDeliverableUnit dataSetStateDu = new DcsDeliverableUnit();
        dataSetStateDu.setId("DataItemStateDu-" + UUID.randomUUID().toString());

        dataSetStateDu.addFormerExternalRef(dataItem.getId());

        dataSetStateDu.setType(DataItemProfile.DATASET_STATE_TYPE);

        dataSetStateDu.addParent(new DcsDeliverableUnitRef(rootDuId));

        dataSetStateDu.setTitle(dataItem.getName());

        if (predecesorDuId != null) {
            DcsRelation isSuccessorOf = new DcsRelation(DcsRelationship.IS_SUCCESSOR_OF, predecesorDuId);
            dataSetStateDu.addRelation(isSuccessorOf);
        }

        return dataSetStateDu;
    }

    /**
     * Generates metadata for a DataItem as a Properties object (key/value pairs).
     *
     * @param dataItem the DataItem to generate metadata for
     * @return the metadata
     * @see #assertValidity(String, org.dataconservancy.ui.model.DataItem) 
     */
    private Properties generateDataSetMetadata(DataItem dataItem) {
        Properties dataSetProperties = new Properties();

        if (dataItem.getId() == null || dataItem.getId().isEmpty()) {
            // TODO: figure out a better exception
            throw new RuntimeException("DataItem identifier must not be null or empty.");
        }

        dataSetProperties.setProperty(MetadataKey.DATASET_ID, dataItem.getId());
        if (dataItem.getDepositDate() != null) {
            dataSetProperties.setProperty(MetadataKey.DATASET_DEPOSIT_DATE, dataItem.getDepositDate().toDateTimeISO().toString());
        } else {
            log.debug("DataItem ({}) had a null deposit date!", dataItem.getId());
        }

        if (dataItem.getDepositorId() != null) {
            dataSetProperties.setProperty(MetadataKey.DATASET_DEPOSITOR_ID, dataItem.getDepositorId());
        } else {
            log.debug("DataItem ({}) had a null depositor!", dataItem.getId());
        }

        dataSetProperties.setProperty(MetadataKey.DATASET_NAME, dataItem.getName());
        
        if (dataItem.getDescription() != null) {
            dataSetProperties.setProperty(MetadataKey.DATASET_DESC, dataItem.getDescription());
        }
        
        if (dataItem.getParentId() != null) {
            dataSetProperties.setProperty(MetadataKey.DATASET_PARENT_ID, dataItem.getParentId());
        }

        return dataSetProperties;
    }

    private Properties generateDataFileMetadata(List<DataFile> dataFiles) {
        Properties dataFilesProperties = new Properties();

        for (DataFile f : dataFiles) {
            if (f.getId() == null || f.getId().isEmpty()) {
                throw new RuntimeException("File id must not be null or empty."); // TODO: figure out better exeception?
            }
            String propertyKeyPrefix = String.format(MetadataKey.DATAFILE_KEY_PREFIX, f.getId());
            dataFilesProperties.setProperty(propertyKeyPrefix + MetadataKey.DATAFILE_ID, f.getId());
        }
        
        return dataFilesProperties;
    }

    /**
     * Asserts that the parameters provided to the {@link #toDcp(String, org.dataconservancy.ui.model.DataItem) Dcp
     * mapping method} contain all of the information required to produce a meaningful archival (DCP) package.
     *
     * @param collectionTransactionId the identifier of the Collection UI object that this DataItem belongs to
     * @param dataItem                 the DataItem itself
     * @throws IllegalArgumentException if any of the parameters are invalid or don't contain the required information
     */
    private void assertValidity(String collectionTransactionId, DataItem dataItem) {
        if (collectionTransactionId == null || collectionTransactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Collection identifier must not be empty or null.");
        }

        if (dataItem.getId() == null || dataItem.getId().isEmpty()) {
            throw new IllegalArgumentException("DataItem identifier must not be empty or null.");
        }

        if (dataItem.getName() == null || dataItem.getName().isEmpty()) {
            throw new IllegalArgumentException("DataItem (" + dataItem.getId() + ") name must not be empty or null.");
        }
    }

    /**
     * Calculates fixity over an InputStream according to the supplied algorithm.
     *
     * @param in         the InputStream
     * @param digestAlgo the algorithm used to calculate fixity
     * @return a DcsFixity object encapsulating the calculated fixity and algorithm
     * @throws IOException if there is an error calculating the fixity
     */
    private DcsFixity calculateFixity(InputStream in, MessageDigest digestAlgo) throws IOException {
        final HexEncodingDigestListener digestListener = new HexEncodingDigestListener();
        final NullOutputStream devNull = new NullOutputStream();
        final DigestNotificationStream digestIn = new DigestNotificationStream(in, digestAlgo, digestListener);

        IOUtils.copy(digestIn, devNull);

        final String digest = digestListener.getDigest();

        if (digest == null || digest.isEmpty()) {
            throw new IOException("Error calculating fixity for stream: the digest was empty or null.");
        }

        DcsFixity fixity = new DcsFixity();
        fixity.setAlgorithm(digestAlgo.getAlgorithm());
        fixity.setValue(digest);

        return fixity;
    }

    /**
     * Maps a File (from a DataItem) to a DcsFile; includes the generation of fixity.  TODO: format.
     *
     * @param dataFile the data file to map
     * @return the DcsFile containing the DataFile
     * @throws DcpMappingException if there is an error performing the mapping
     */
    private DcsFile mapDataFile(DataFile dataFile) throws DcpMappingException {
        DcsFile dcsFile = new DcsFile();
        
        dcsFile.setId(dataFile.getId());
        // Map File name and source to DcsFile name and source
        dcsFile.setSource(dataFile.getSource());
        dcsFile.setName(dataFile.getName());

        // add ui generated format to use as mime type: identify this format by setting version to "dcs-ui"
        // we need this version specified to correctly do the inverse mapping
        if(dataFile.getFormat() != null && !dataFile.getFormat().isEmpty()){//have to check this, some DcsFormat fields
             //may not be null or empty
             DcsFormat format = new DcsFormat();
             format.setName(dataFile.getFormat());
             format.setFormat(dataFile.getFormat());
             format.setSchemeUri("http://www.iana.org/assignments/media-types/");
             format.setVersion("dcs-ui");
             dcsFile.addFormat(format);
        }

        // Mapped DcsFiles automatically have extant set to 'true'
        dcsFile.setExtant(true);

        // Map the business id to an alternate id.
        dcsFile.addAlternateId(new DcsResourceIdentifier(Id.getAuthority(), dataFile.getId(),
                Types.DATA_FILE.name()));
        
        try{
            Resource r = new UrlResource(dcsFile.getSource());

            if( dataFile.getSize() > 0 ){
                dcsFile.setSizeBytes(dataFile.getSize());
            }
            else{                
                dcsFile.setSizeBytes(r.contentLength());                                    
            }
            
            dcsFile.addFixity(calculateFixity(r.getInputStream(), MessageDigest.getInstance("MD5")));
            dcsFile.addFixity(calculateFixity(r.getInputStream(), MessageDigest.getInstance("SHA1")));    
        } catch (Exception e){
            throw new DcpMappingException("Error calculating file length or fixity: " + e.getMessage(), e);
        }

        return dcsFile;
    }

    /**
     * {@link #generateDataSetMetadata(org.dataconservancy.ui.model.DataItem) Generates} metadata for the DataItem, and
     * maps it to a DcsFile.  Includes the generation of fixity.  TODO: format.
     *
     * @param dataItem the DataItem to generate metadata for
     * @return a DcsFile containing the generated metadata
     * @throws DcpMappingException if there is an error generating or mapping the metadata
     */
    private DcsFile mapDataSetMetadata(DataItem dataItem) throws DcpMappingException {
        DcsFile dataSetMd = new DcsFile();
        dataSetMd.setId(UUID.randomUUID().toString());
        Properties dataSetProperties = generateDataSetMetadata(dataItem);
        java.io.File tmp = null;
        try {

            String tmpString = dataItem.getId().replaceAll("\\W", "");
            tmp = java.io.File.createTempFile("DataSetMetadata" + tmpString + "-", ".txt");
            tmp.deleteOnExit();
            dataSetProperties.store(new FileOutputStream(tmp), "UTF-8");
        } catch (IOException e) {
            String path = null;
            if (tmp != null) {
                path = tmp.getAbsolutePath();
            }

            throw new RuntimeException("Unable to create temporary file " + path + ": " + e.getMessage(), e);
        }
        dataSetMd.setSizeBytes(tmp.length());
        dataSetMd.setExtant(true);
        try {
            dataSetMd.addFixity(calculateFixity(new FileInputStream(tmp), MessageDigest.getInstance("MD5")));
            dataSetMd.addFixity(calculateFixity(new FileInputStream(tmp), MessageDigest.getInstance("SHA1")));
        } catch (Exception e) {
            throw new DcpMappingException("Error calculating file length or fixity: " + e.getMessage(), e);
        }
        dataSetMd.setName(tmp.getName());
        dataSetMd.setSource(tmp.toURI().toString());
        return dataSetMd;
    }

    private DcsFile mapDataFileMetadata(String dataSetId, List<DataFile> files) throws DcpMappingException {
        DcsFile dataFilesMd = new DcsFile();
        dataFilesMd.setId(UUID.randomUUID().toString());
        Properties dataFilesProperties = generateDataFileMetadata(files);

        java.io.File tmp = null;
        try {
            String tmpString = dataSetId.replaceAll("\\W", "");
            tmp = java.io.File.createTempFile("DataSetMetadata" + tmpString + "-", ".txt");
            tmp.deleteOnExit();
            dataFilesProperties.store(new FileOutputStream(tmp), "UTF-8");
        } catch (IOException e) {
            String path = null;
            if (tmp != null) {
                path = tmp.getAbsolutePath();
            }

            throw new RuntimeException("Unable to create temporary file " + path + ": " + e.getMessage(), e);
        }
        dataFilesMd.setSizeBytes(tmp.length());
        dataFilesMd.setExtant(true);
        try {
            dataFilesMd.addFixity(calculateFixity(new FileInputStream(tmp), MessageDigest.getInstance("MD5")));
            dataFilesMd.addFixity(calculateFixity(new FileInputStream(tmp), MessageDigest.getInstance("SHA1")));
        } catch (Exception e) {
            throw new DcpMappingException("Error calculating file length or fixity: " + e.getMessage(), e);
        }
        dataFilesMd.setName(tmp.getName());
        dataFilesMd.setSource(tmp.toURI().toString());
        return dataFilesMd;
    }

    /**
     * Assembles the already-mapped DcsEntities into the supplied Dcp package.  This method doesn't alter the supplied
     * DcsEntities, but does generate intermediate objects (manifestations, manifestation files) needed for producing
     * the final object graph contained in the Dcp.  The DcsEntities supplied to this method must have already been
     * mapped prior to this method being executed.
     *
     * @param toAssemble the Dcp to assemble
     * @param dataSetDU    the DeliverableUnit mapped from the DataItem
     * @param dataFiles  the DcsFiles mapped from the the DataItem files
     * @param dataSetMd  the DcsFile containing generated DataItem metadata
     */
    private void assemblePackage(Dcp toAssemble, DcsDeliverableUnit dataSetDU, DcsDeliverableUnit dataSetStateDu,
                                 DcsFile dataSetMd, DcsFile dataFilesMd, DataItem dataItem) {
        final String isoDateTimeStringNow = DateTime.now().toDateTimeISO().toString();

        // Attach the DataItem metadata file to a single DcsManifestation
        final DcsManifestationFile dataSetMdMetadataManFile = new DcsManifestationFile();
        dataSetMdMetadataManFile.setPath("/" + dataSetMd.getName());
        dataSetMdMetadataManFile.setRef(new DcsFileRef(dataSetMd.getId()));
        final DcsManifestation dataSetMdManifestation = new DcsManifestation();
        dataSetMdManifestation.setId(UUID.randomUUID().toString());
        // FIXME: solr complains about indexing these dates
//        dataSetMdManifestation.setDateCreated(isoDateTimeStringNow);
        dataSetMdManifestation.addTechnicalEnvironment(DATASET_METADATA_TECHENV);
        dataSetMdManifestation.addManifestationFile(dataSetMdMetadataManFile);
        dataSetMdManifestation.setDeliverableUnit(dataSetStateDu.getId());

        // Attach the DataItem File(s) metadata file to a single DcsManifestation
        final DcsManifestationFile dataFileMdManFile = new DcsManifestationFile();
        dataFileMdManFile.setPath("/" + dataFilesMd.getName());
        dataFileMdManFile.setRef(new DcsFileRef(dataFilesMd.getId()));
        final DcsManifestation dataFileMdMan = new DcsManifestation();
        dataFileMdMan.setId(UUID.randomUUID().toString());
        dataFileMdMan.addTechnicalEnvironment(DATASET_FILE_METADATA_TECHENV);
        dataFileMdMan.addManifestationFile(dataFileMdManFile);
        dataFileMdMan.setDeliverableUnit(dataSetStateDu.getId());

        // Attach all of the entities to the Dcp package
        toAssemble.addDeliverableUnit(dataSetDU);
        toAssemble.addDeliverableUnit(dataSetStateDu);
        toAssemble.addFile(dataSetMd);
        toAssemble.addFile(dataFilesMd);
        toAssemble.addManifestation(dataSetMdManifestation);
        toAssemble.addManifestation(dataFileMdMan);
    }

    /**
     * Attempts to resolve the reference {@code ref} by retrieving it from the archive.  Specifically the reference
     * is retrieved, tested for nullity and type, and the resolved reference is returned as a {@code DcsFile}.
     *
     * @param ref the reference to a DcsFile entity in the archive
     * @return the referenced DcsFile
     * @throws RuntimeException if the reference cannot be resolved, or if the reference resolves to an object other
     *                          than a DcsFile instance.
     */
    private DcsFile resolveFileRef(String ref) {
        DcsFile dataSetMetadataFile;
        DcsEntity e = archiveUtil.getEntity(ref);
        if (e == null) {
            throw new RuntimeException("Package does not conform: missing DataItem Metadata File (reference " +
                    "'" + ref + "' not found in the archive)");
        }
        if (!(e instanceof DcsFile)) {
            throw new RuntimeException("Package does not conform: missing DataItem Metadata File (reference " +
                    "'" + ref + "' resolved to a " + e.getClass().getName() + ", " +
                    "but expected a " + DcsFile.class.getName() + ")");
        }

        dataSetMetadataFile = (DcsFile) e;
        return dataSetMetadataFile;
    }



    private static class MetadataKey {
        private static final String DATASET_DEPOSIT_DATE = "dataset.date";
        private static final String DATASET_DEPOSITOR_ID = "dataset.depositorId";
        private static final String DATASET_NAME = "dataset.name";
        private static final String DATASET_DESC = "dataset.description";
        private static final String DATASET_ID = "dataset.identifier";
        private static final String DATASET_PARENT_ID = "dataset.parentId";
        private static final String DATAFILE_KEY_PREFIX = "datafile.%s.";
        private static final String DATAFILE_ID = "id";
    }

}

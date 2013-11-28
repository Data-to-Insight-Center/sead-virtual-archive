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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.UUID;

import javax.xml.namespace.QName;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;

import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.builder.xstream.DcsPullDriver;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.ui.exceptions.DcpMappingException;
import org.dataconservancy.ui.model.Id;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.builder.xstream.MetadataFileConverter;
import org.dataconservancy.ui.profile.MetadataFileProfile;
import org.dataconservancy.ui.services.BusinessObjectState;
import org.dataconservancy.ui.services.MetadataFileBusinessObjectSearcher;

/**
 * This class provides methods to tranform (or map) {@link org.dataconservancy.ui.model.MetadataFile} objects into valid
 * {@link org.dataconservancy.model.dcp.Dcp} objects and to construct {@link org.dataconservancy.ui.model.MetadataFile} objects
 * from valid {@link org.dataconservancy.model.dcp.Dcp} objects.
 */
public class MetadataFileMapper extends AbstractVersioningMapper<MetadataFile> {
    
    private final XStream xstream;
    private static final String COLLECTION_METADATA_FILE_MANIFESTATION_TYPE = "CollectionMetadataFile";
        
    /**
     * Used to handle updates to business objects already deposited in the
     * archive.
     */
    private MetadataFileBusinessObjectSearcher boSearcher;

    public MetadataFileMapper(MetadataFileBusinessObjectSearcher boSearcher) {
        super();
        this.boSearcher = boSearcher;
        xstream = setupXStream();
    }
    
    private XStream setupXStream() {
        final QNameMap qnames = new QNameMap();
        
        final String defaultnsUri ="http://dataconservancy.org/schemas/bop/1.0";
        qnames.setDefaultNamespace(defaultnsUri);

        final DcsPullDriver driver = new DcsPullDriver(qnames);
        
        // The XStream Driver
        XStream x = new XStream(driver);
        x.setMode(XStream.NO_REFERENCES);
        
        // XStream converter, alias, and QName registrations
        x.alias(MetadataFileConverter.E_METADATA_FILE, MetadataFile.class);
        x.registerConverter(new MetadataFileConverter());
        qnames.registerMapping(new QName(defaultnsUri, MetadataFileConverter.E_METADATA_FILE), MetadataFile.class);
        
        return x;
    }
    
    /**
     * {@inheritDoc}
     *
     * Produces an archival package that encodes the information of a {@link MetadataFile}.
     *
     * If the business id of DataItem is the same as an already deposited DataItem, then toDcp produces a SIP whose
     * root DU is the successor of the root DU of the already deposited DataItem. Similarly, a File may indicate it already
     * exists in the archive by setting its id and path to that of the already deposited file. A DcsFile will not be
     * created for such a File. Instead a ManifestationFile will point to the existing DcsFile.
     *
     * @param parent_entity_id {@inheritDoc}
     * @param metadataFile {@inheritDoc}
     * @return {@inheritDoc}
     * @throws DcpMappingException {@inheritDoc}
     */
    @Override
    public Dcp toDcp(String parent_entity_id, MetadataFile metadataFile) throws DcpMappingException {
        
        if (boSearcher == null) {
            throw new IllegalStateException("BusinessObjectSearcher is not set.");
        }
        
        try {
            assertValidity(parent_entity_id, metadataFile);
        } catch (IllegalArgumentException e) {
            throw new DcpMappingException(e.getMessage(), e);
        }
        
        BusinessObjectState state = boSearcher.findLatestState(metadataFile.getId());
        DcsDeliverableUnit state_pred = (state != null) ? state.getLatestState() : null;
        DcsDeliverableUnit root = (state != null) ? state.getRoot() : null;

        Dcp dcp = super.toDcp(null, state_pred == null ? null : state_pred.getId(),
                root == null ? null : root.getId(), metadataFile);
               
        DcsDeliverableUnit statedu = getCurrentStateDu(dcp);
        
        // Save dus so we can modify statedu in the Dcp
        java.util.Collection<DcsDeliverableUnit> dus = dcp.getDeliverableUnits();
        dus.remove(statedu);
        dus.add(statedu);

        if (parent_entity_id != null) {
            DcsRelation isMetadataFor = new DcsRelation(DcsRelationship.IS_METADATA_FOR, parent_entity_id);
            statedu.addRelation(isMetadataFor);
        }

        DcsManifestation dcs_man = new DcsManifestation();
        dcs_man.setId(UUID.randomUUID().toString());

        DcsManifestationFile dcs_mf = new DcsManifestationFile();

        dcs_man.setDeliverableUnit(statedu.getId());
        dcs_man.setType(COLLECTION_METADATA_FILE_MANIFESTATION_TYPE);

        // Check if file already exists in the archive
        DcsFile dcs_file = null;
        boolean file_already_exists;

        if (metadataFile.getSource() == null || metadataFile.getSource().isEmpty()) {
            dcs_file = boSearcher.findMetadataFile(metadataFile.getId());
            if(dcs_file == null) {
                throw new DcpMappingException("Metadata File Source wasn't set expected file to exist in the archive"
                                                      + metadataFile);
            }
            file_already_exists = true;
        } else {
            file_already_exists = false;
        }
        

        if (!file_already_exists) {
            dcs_file = new DcsFile();
            dcs_file.setId("metadatafile0");
        } 

        if (metadataFile.getPath() != null) {
            dcs_mf.setPath(metadataFile.getPath());
        }

        dcs_mf.setRef(new DcsFileRef(dcs_file.getId()));
        dcs_man.addManifestationFile(dcs_mf);

        if (!file_already_exists) {
            if (metadataFile.getName() != null) {
                dcs_file.setName(metadataFile.getName());
            }

            if (metadataFile.getSource() != null) {
                dcs_file.setSource(metadataFile.getSource());
                dcs_file.setExtant(true);
            }

            dcs_file.setSizeBytes(metadataFile.getSize());



            if (metadataFile.getMetadataFormatId() != null) {
                DcsFormat format = new DcsFormat();
                
                format.setFormat(metadataFile.getMetadataFormatId());
                format.setName(metadataFile.getMetadataFormatId());
                format.setSchemeUri(metadataFile.getMetadataFormatId());
              
                dcs_file.addFormat(format);
            }

            if (metadataFile.getId() != null) {
                dcs_file.addAlternateId(new DcsResourceIdentifier(Id
                        .getAuthority(), metadataFile.getId(),
                        Types.METADATA_FILE.name()));
            }

            dcp.addFile(dcs_file);
        }

        if (metadataFile.getId() != null && !metadataFile.getId().isEmpty()) {
            statedu.addFormerExternalRef(metadataFile.getId());
            DcsDeliverableUnit rootDu = getRootDu(dcp);

            if (rootDu != null) {
                dus.remove(rootDu);
                dus.add(rootDu);
                rootDu.addFormerExternalRef(metadataFile.getId());
            }
        }

        dcp.addManifestation(dcs_man);
        
        return dcp;
    }
  
    /**
     * Asserts that the parameters provided to the {@link #toDcp(String, org.dataconservancy.ui.model.MetadataFile) Dcp
     * mapping method} contain all of the information required to produce a meaningful archival (DCP) package.
     *
     * @param parentId the identifier of the parent UI object that this MetadataFile belongs to
     * @param file the MetadataFile itself
     * @throws IllegalArgumentException if any of the parameters are invalid or don't contain the required information
     */
    private void assertValidity(String parentId, MetadataFile file) {
        if (file.getId() == null || file.getId().isEmpty()) {
            throw new IllegalArgumentException("Metadata File identifier must not be empty or null.");
        }

        if (file.getName() == null || file.getName().isEmpty()) {
            throw new IllegalArgumentException("MetadataFile (" + file.getId() + ") name must not be empty or null.");
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * @param dcp
     * @return DataItem
     * @throws DcpMappingException
     */
    @Override
    public MetadataFile fromDcp(Dcp dcp) throws DcpMappingException {
        MetadataFile metadataFile = super.fromDcp(dcp);
        DcsDeliverableUnit statedu = getCurrentStateDu(dcp);
        
        //Search through to find the manifestation that relates to the metadata file contents and the format contents
        for (DcsManifestation man : dcp.getManifestations()) {
            if (man.getType() == null
                    || man.getDeliverableUnit() == null
                    || !man.getType().equals(
                            COLLECTION_METADATA_FILE_MANIFESTATION_TYPE)
                    || !man.getDeliverableUnit().equals(statedu.getId())) {
                continue;
            }

            // Get DcsFile for Manifestation

            if (man.getManifestationFiles().size() != 1) {
                throw new DcpMappingException(
                        "Expected only one ManifestionFile for Manifestation "
                                + man);
            }

            DcsManifestationFile dcs_mf = man.getManifestationFiles()
                    .iterator().next();

            if (dcs_mf.getRef() == null) {
                throw new DcpMappingException(
                        "Expected ref in ManifestionFile " + dcs_mf);
            }

            String file_id = dcs_mf.getRef().getRef();

            if (file_id == null) {
                throw new DcpMappingException(
                        "Expected non-null file id in reference " + dcs_mf);
            }

            DcsFile file = null;

            for (DcsFile df : dcp.getFiles()) {
                if (df.getId() != null && df.getId().equals(file_id)) {
                    file = df;
                    break;
                }
            }

            if (file == null) {
                throw new DcpMappingException(
                        "Could not find DcsFile for Manifestation " + man);
            }

            metadataFile.setSize(file.getSizeBytes());
            metadataFile.setPath(dcs_mf.getPath());

            // Give priority to a format in the registry
            for (DcsFormat format : file.getFormats()) {

                if (format.getFormat() == null || format.getFormat().isEmpty()) {
                    continue;
                }
                
                metadataFile.setMetadataFormatId(format.getName());
            }


            for (DcsResourceIdentifier id : file.getAlternateIds()) {
                if (id.getAuthorityId() != null
                        && id.getAuthorityId().equals(Id.getAuthority())
                        && id.getTypeId() != null
                        && id.getTypeId()
                                .equals(Types.METADATA_FILE.name())) {

                    metadataFile.setId(id.getIdValue());
                }
            }

            metadataFile.setSource(file.getSource());
        }

        return metadataFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void serializeObjectState(MetadataFile object, OutputStream os)
            throws IOException {
        xstream.toXML(object, os);        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MetadataFile deserializeObjectState(InputStream is)
            throws IOException {
        return (MetadataFile) xstream.fromXML(is);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getStateDuType() {
        return MetadataFileProfile.STATE_DU_TYPE;
    }
}

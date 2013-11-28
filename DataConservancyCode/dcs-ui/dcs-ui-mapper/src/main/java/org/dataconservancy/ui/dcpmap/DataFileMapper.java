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
package org.dataconservancy.ui.dcpmap;

import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.ui.exceptions.DcpMappingException;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.Id;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.profile.DataFileProfile;
import org.dataconservancy.ui.services.BusinessObjectState;
import org.dataconservancy.ui.services.DataFileBusinessObjectSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

/**
 *
 */
public class DataFileMapper extends AbstractVersioningMapper<DataFile> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private BusinessObjectBuilder bob;

    private DataFileBusinessObjectSearcher boSearcher;

    public DataFileMapper(BusinessObjectBuilder bob, DataFileBusinessObjectSearcher boSearcher) {
        this.bob = bob;
        this.boSearcher = boSearcher;
    }

    @Override
    public DataFile fromDcp(Dcp dcp) throws DcpMappingException {
        DataFile df = super.fromDcp(dcp);

        if (df == null) {
            throw new DcpMappingException("Unable to map DataFile from DCP: " + dcp);
        }

        DcsFile source = boSearcher.findDataFile(df.getId());
        if (source != null) {
            df.setSource(source.getSource());
            df.setSize(source.getSizeBytes());
            df.setName(source.getName());
            df.setPath(source.getSource());
        }

        return df;
    }

    /**
     *
     * @param parent_entity_id parent of toplevel entities created in the package, null for no parent
     * @param dataFile
     * @return
     * @throws DcpMappingException
     * @throws IllegalArgumentException if any arguments are empty or null; if the DataFile has a null or empty ID.
     */
    @Override
    public Dcp toDcp(String parent_entity_id, DataFile dataFile) throws DcpMappingException {
        assertValidity(parent_entity_id, dataFile);

        DcsDeliverableUnit rootDu = null;
        DcsDeliverableUnit stateDu = null;

        final BusinessObjectState archiveState = boSearcher.findLatestState(dataFile.getId());

        if (archiveState != null && archiveState.getRoot() != null) {
            rootDu = archiveState.getRoot();
        }

        if (archiveState != null && archiveState.getLatestState() != null) {
            stateDu = archiveState.getLatestState();
        }

        final Dcp dcp = super.toDcp(parent_entity_id,
                (stateDu != null ? stateDu.getId() : null),
                (rootDu != null ? rootDu.getId() : null),
                dataFile);

        DcsDeliverableUnit statedu = getCurrentStateDu(dcp);

        // Save dus so we can modify statedu in the Dcp
        java.util.Collection<DcsDeliverableUnit> dus = dcp.getDeliverableUnits();
        dus.remove(statedu);
        dus.add(statedu);

        DcsManifestation dcs_man = new DcsManifestation();
        dcs_man.setId(UUID.randomUUID().toString());

        DcsManifestationFile dcs_mf = new DcsManifestationFile();

        dcs_man.setDeliverableUnit(statedu.getId());
        dcs_man.setType(DataFileProfile.DATAFILE_MANIFESTATION_TYPE);
        dcs_man.addTechnicalEnvironment();

        // Check if file already exists in the archive
        DcsFile dcs_file = null;
        boolean file_already_exists;

        if (dataFile.getSource() == null || dataFile.getSource().isEmpty()) {
            dcs_file = boSearcher.findDataFile(dataFile.getId());
            if(dcs_file == null) {
                throw new DcpMappingException("DataFile Source wasn't set expected file to exist in the archive"
                                                      + dataFile);
            }
            file_already_exists = true;
        } else {
            file_already_exists = false;
        }


        if (!file_already_exists) {
            dcs_file = new DcsFile();
            dcs_file.setId("datafile0");
            dcs_file.addAlternateId(
                    new DcsResourceIdentifier(Id.getAuthority(), dataFile.getId(), Types.DATA_FILE.name()));
        }

        if (dataFile.getPath() != null) {
            dcs_mf.setPath(dataFile.getPath());
        }

        dcs_mf.setRef(new DcsFileRef(dcs_file.getId()));
        dcs_man.addManifestationFile(dcs_mf);

        if (!file_already_exists) {
            if (dataFile.getName() != null) {
                dcs_file.setName(dataFile.getName());
            }

            if (dataFile.getSource() != null) {
                dcs_file.setSource(dataFile.getSource());
                dcs_file.setExtant(true);
            }

            dcs_file.setSizeBytes(dataFile.getSize());

            if (dataFile.getId() != null) {
                dcs_file.addAlternateId(new DcsResourceIdentifier(Id
                        .getAuthority(), dataFile.getId(),
                        Types.DATA_FILE.name()));
            }

            dcp.addFile(dcs_file);
        }

        if (dataFile.getId() != null && !dataFile.getId().isEmpty()) {
            statedu.addFormerExternalRef(dataFile.getId());
            DcsDeliverableUnit dcpRootDu = getRootDu(dcp);

            if (dcpRootDu != null) {
                dus.remove(dcpRootDu);
                dus.add(dcpRootDu);
                dcpRootDu.addFormerExternalRef(dataFile.getId());
                dcpRootDu.addAlternateId(
                        new DcsResourceIdentifier(Id.getAuthority(), dataFile.getId(), Types.DATA_FILE.name()));
            }
        }

        dcp.addManifestation(dcs_man);

        return dcp;
    }

    public BusinessObjectBuilder getBob() {
        return bob;
    }

    public void setBob(BusinessObjectBuilder bob) {
        this.bob = bob;
    }

    public DataFileBusinessObjectSearcher getBoSearcher() {
        return boSearcher;
    }

    public void setBoSearcher(DataFileBusinessObjectSearcher boSearcher) {
        this.boSearcher = boSearcher;
    }

    @Override
    protected void serializeObjectState(DataFile dataFile, OutputStream os) throws IOException {
        bob.buildDataFile(dataFile, os);
    }

    @Override
    protected DataFile deserializeObjectState(InputStream is) throws IOException {
        try {
            return bob.buildDataFile(is);
        } catch (InvalidXmlException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private void assertValidity(String parent_entity_id, DataFile df) {
        if (parent_entity_id == null || parent_entity_id.trim().length() == 0) {
            throw new IllegalArgumentException("Parent entity ID must not be null or empty.");
        }

        if (df == null) {
            throw new IllegalArgumentException("DataFile must not be null.");
        }

        if (df.getId() == null || df.getId().trim().length() == 0) {
            throw new IllegalArgumentException("DataFile ID must not be null or empty.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getStateDuType() {
        return DataFileProfile.DATAFILE_STATEDU_TYPE;
    }
}

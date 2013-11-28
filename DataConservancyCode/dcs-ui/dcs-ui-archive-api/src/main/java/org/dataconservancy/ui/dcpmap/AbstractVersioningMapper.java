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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.ui.exceptions.DcpMappingException;

/**
 * Model an object as a graph in the DCS data model. The root of the graph is a
 * DU which a child DU representing the object state. This allows the graph to
 * be updated with new object versions without changing the DCS identifier of
 * the root du. The state DU is versioned and contains a Manifestation and a
 * DcsFile holding a serialization of the object state.
 */
public abstract class AbstractVersioningMapper<T> implements DcpMapper<T> {
    // TODO Eventually have URI's for these constants.
    private static final String CREATOR = "dcs-ui";
    public static final String ROOT_DELIVERABLE_UNIT_TYPE = "root";
    public static final String STATE_MANIFESTATION_TYPE = "state";

    protected DcsDeliverableUnit getRootDu(Dcp dcp) {
        for (DcsDeliverableUnit test : dcp.getDeliverableUnits()) {
            if (test.getType().equals(ROOT_DELIVERABLE_UNIT_TYPE)) {
                return test;
            }
        }

        return null;
    }

    protected DcsManifestation getFirstManifestationOf(Dcp dcp,
            DcsDeliverableUnit du, String man_type) {
        for (DcsManifestation test : dcp.getManifestations()) {
            if (test.getDeliverableUnit().equals(du.getId())
                    && test.getType().equals(man_type)) {
                return test;
            }
        }

        return null;
    }

    private String getPredecessorId(DcsDeliverableUnit du) {
        for (DcsRelation rel : du.getRelations()) {
            if (rel.getRelUri().equals(
                    DcsRelationship.IS_SUCCESSOR_OF.asString())) {
                if (rel.getRef() == null || rel.getRef().getRef() == null) {
                    return null;
                }

                return rel.getRef().getRef();
            }
        }

        return null;
    }

    protected DcsDeliverableUnit getCurrentStateDu(Dcp dcp) {
        List<DcsDeliverableUnit> statedus = new ArrayList<DcsDeliverableUnit>();

        for (DcsDeliverableUnit test : dcp.getDeliverableUnits()) {
            if (test.getType().equals(getStateDuType())) {
                statedus.add(test);
            }
        }

        // Return du without a successor

        next: for (DcsDeliverableUnit du : statedus) {
            for (DcsDeliverableUnit du2 : statedus) {
                String pred_id = getPredecessorId(du2);

                if (pred_id != null && pred_id.equals(du.getId())) {
                    continue next;
                }
            }

            return du;
        }

        return null;
    }

    private DcsFile getFirstFileOf(Dcp dcp, DcsManifestation man) {
        for (DcsFile test : dcp.getFiles()) {
            for (DcsManifestationFile mf : man.getManifestationFiles()) {
                if (mf.getRef().getRef().equals(test.getId())) {
                    return test;
                }
            }
        }

        return null;
    }

    /**
     * Write the object state to a stream.
     * 
     * @param os
     */
    protected abstract void serializeObjectState(T object, OutputStream os)
            throws IOException;

    /**
     * Recreate the object from the state written to the stream.
     * 
     * @param is
     * @throws IOException
     */
    protected abstract T deserializeObjectState(InputStream is)
            throws IOException;

    /**
     * Create a SIP. If previous_state_du is not null, the SIP will be setup to
     * update an existing graph in the archive.
     * 
     * The DU's are assigned random ids in the sip to deal with unit testing
     * without actual deposit easier.
     * 
     * @param parent_entity_id
     * @param previous_state_du_id
     *            state predecessor id in an existing graph for this object or null
     * @param root_du_id 
     *            id of root du for graph or null if this is the first version
     * @param object
     * @return dcp
     * @throws DcpMappingException
     */
    protected Dcp toDcp(String parent_entity_id,
            String previous_state_du_id, String root_du_id, T object)
            throws DcpMappingException {
        Dcp dcp = new Dcp();

        DcsDeliverableUnit state_du = new DcsDeliverableUnit();

        state_du.setId(UUID.randomUUID().toString());
        state_du.setTitle("title");
        state_du.addCreator(CREATOR);
        state_du.setType(getStateDuType());

        if (previous_state_du_id == null) {
            DcsDeliverableUnit root_du = new DcsDeliverableUnit();

            root_du.setId(UUID.randomUUID().toString());
            root_du.setTitle("title");
            root_du.addCreator(CREATOR);
            root_du.setType(ROOT_DELIVERABLE_UNIT_TYPE);

            if (parent_entity_id != null) {
                root_du.addParent(new DcsDeliverableUnitRef(parent_entity_id));
            }

            state_du.addParent(new DcsDeliverableUnitRef(root_du.getId()));

            dcp.addDeliverableUnit(root_du);
        } else {
            if (root_du_id == null) {
                throw new DcpMappingException("Root du id must be set for updates.");
            }
            
            state_du.addParent(new DcsDeliverableUnitRef(root_du_id));

            DcsRelation rel = new DcsRelation();

            rel.setRef(new DcsEntityReference(previous_state_du_id));
            rel.setRelUri(DcsRelationship.IS_SUCCESSOR_OF.asString());

            state_du.addRelation(rel);
        }

        DcsManifestation state_man = new DcsManifestation();
        DcsManifestationFile state_mf = new DcsManifestationFile();
        DcsFile state_file = new DcsFile();

        state_man.setId(UUID.randomUUID().toString());
        state_file.setId(UUID.randomUUID().toString());

        state_man.setDeliverableUnit(state_du.getId());
        state_mf.setRef(new DcsFileRef(state_file.getId()));
        state_man.addManifestationFile(state_mf);
        state_man.setType(STATE_MANIFESTATION_TYPE);

        try {
            File tmpfile = File.createTempFile("state", null);
            tmpfile.deleteOnExit();
            state_file.setSource(tmpfile.toURI().toURL().toExternalForm());
            state_file.setExtant(true);
            state_file.setName("state.xml");

            FileOutputStream fos = new FileOutputStream(tmpfile);
            serializeObjectState(object, fos);
            fos.close();
            state_file.setSizeBytes(tmpfile.length());
        } catch (IOException e) {
            throw new DcpMappingException(e);
        }

        dcp.addDeliverableUnit(state_du);
        dcp.addManifestation(state_man);
        dcp.addFile(state_file);

        return dcp;
    }

    public T fromDcp(Dcp dcp) throws DcpMappingException {
        DcsDeliverableUnit statedu = getCurrentStateDu(dcp);

        if (statedu == null) {
            throw new DcpMappingException(
                    "Could not find state DcsDeliverableUnit");
        }

        DcsManifestation man = getFirstManifestationOf(dcp, statedu,
                STATE_MANIFESTATION_TYPE);

        if (man == null) {
            throw new DcpMappingException(
                    "Could not find state DcsManifestation");
        }

        DcsFile file = getFirstFileOf(dcp, man);

        if (file == null) {
            throw new DcpMappingException("Could not find needed state DcsFile");
        }

        try {
            InputStream is = new URL(file.getSource()).openStream();
            T result = deserializeObjectState(is);
            is.close();

            return result;
        } catch (Exception e) {
            throw new DcpMappingException("Error mapping content from "
                    + file.getSource(), e);
        }
    }
    
    /**
     * Returns the specific state du type from the concrete mapper.
     * @return The string representing the state du type of the object.
     */
    protected abstract String getStateDuType();
}

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.ui.exceptions.DcpMappingException;

import com.thoughtworks.xstream.XStream;
import org.dataconservancy.ui.model.Collection;

// TODO What attributes should be set in the entities?

/**
 * Model an object as a DU, with a Manifestation, and a DcsFile holding an XML
 * serialization of the object.
 */
public class GenericMapper<T> implements DcpMapper<T> {
    private static final String CREATOR = "dcs-ui";
    private static final String XSTREAM_TECHNICAL_ENV = "XStream";

    /**
     * ID of toplevel Deliverable Unit in SIP.
     */
    protected static final String TOPLEVEL_DELIVERABLE_UNIT_ID = "du";

    private final XStream xstream;

    public GenericMapper() {
        this.xstream = new XStream();
    }

    public Dcp toDcp(String parent_entity_id, T object)
            throws DcpMappingException {
        Dcp dcp = new Dcp();

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        DcsManifestation man = new DcsManifestation();
        DcsManifestationFile mf = new DcsManifestationFile();
        DcsFile file = new DcsFile();

        du.setId(TOPLEVEL_DELIVERABLE_UNIT_ID);
        du.setTitle("title");
        du.addCreator(CREATOR);

        if (parent_entity_id != null) {
            du.addParent(new DcsDeliverableUnitRef(parent_entity_id));
        }

        man.setId("man");
        file.setId("file");

        man.setDeliverableUnit(du.getId());
        mf.setRef(new DcsFileRef(file.getId()));
        man.addManifestationFile(mf);
        man.addTechnicalEnvironment(XSTREAM_TECHNICAL_ENV);

        try {
            File xmlfile = File.createTempFile("metadata", null);
            xmlfile.deleteOnExit();
            file.setSource(xmlfile.toURI().toURL().toExternalForm());
            file.setExtant(true);
            file.setName("metadata.xml");

            FileWriter out = new FileWriter(xmlfile);
            xstream.toXML(object, out);
            out.close();

        } catch (IOException e) {
            throw new DcpMappingException(e);
        }

        dcp.addDeliverableUnit(du);
        dcp.addManifestation(man);
        dcp.addFile(file);

        return dcp;
    }

    public T fromDcp(Dcp dcp) throws DcpMappingException {
        DcsDeliverableUnit du = null;

        for (DcsDeliverableUnit test : dcp.getDeliverableUnits()) {
            if (test.getCreators().contains(CREATOR)) {
                du = test;
                break;
            }
        }

        if (du == null) {
            throw new DcpMappingException(
                    "Could not find needed DcsDeliverableUnit");
        }

        DcsManifestation man = null;

        for (DcsManifestation test : dcp.getManifestations()) {
            if (test.getDeliverableUnit().equals(du.getId())
                    && test.getTechnicalEnvironment().contains(
                            XSTREAM_TECHNICAL_ENV)) {
                man = test;
                break;
            }
        }

        if (man == null) {
            throw new DcpMappingException(
                    "Could not find needed DcsManifestation");
        }

        DcsFile file = null;

        for (DcsFile test : dcp.getFiles()) {
            for (DcsManifestationFile mf : man.getManifestationFiles()) {
                if (mf.getRef().getRef().equals(test.getId())) {
                    file = test;
                }
            }
        }

        if (file == null) {
            throw new DcpMappingException("Could not find needed DcsFile");
        }

        try {
            InputStream is = new URL(file.getSource()).openStream();

            @SuppressWarnings("unchecked")
            T result = (T) xstream.fromXML(is);

            is.close();

            return result;
        } catch (Exception e) {
            throw new DcpMappingException("Error mapping content from "
                    + file.getSource() + ": " + e.getMessage(), e);
        }
    }
}

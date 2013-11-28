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
package org.dataconservancy.archive.impl.fcrepo.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.archive.impl.fcrepo.dto.Datastream;
import org.dataconservancy.archive.impl.fcrepo.dto.DatastreamVersion;

/**
 * XStream converter for Fedora Datastream elements.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class DatastreamConverter
        extends AbstractPackageConverter {

    public static final String CONTROL_GROUP_X = "X";

    public static final String CONTROL_GROUP_E = "E";

    public static final String CONTROL_GROUP_M = "M";

    public static final String CONTROL_GROUP_R = "R";

    public static final String DCID = "DC";

    public static final String RELSXID = "RELS-EXT";

    public static final String DCPXML = "DCPXML";

    public static final String DCSFILE = "FILE";

    public static final String STATE_A = "A";

    public static final String STATE_D = "D";

    public static final String STATE_T = "T";

    public static final String VERSIONABLE_T = "true";

    public static final String VERSIONABLE_F = "false";

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        Datastream ds = (Datastream) source;
        writer.startNode(ds.getClass().getName());

        // The attributes for each Datastream are the ID,
        // CONTROL_GROUP, STATE, and VERSIONABLE.  Also the will
        // be different choices depending on the specific Datastream
        // for the DCS Entity.

        // Some of these could be null, don't add null attributes.  However,
        // we could log schema errors here.
        if (ds.getId() != null) {
            writer.addAttribute("ID", ds.getId());
        } else {
            throw new RuntimeException("Marshalling failed");
        }
        if (ds.getControlGroup() != null) {
            writer.addAttribute("CONTROL_GROUP", ds.getControlGroup());
        }
        if (ds.getState() != null) {
            writer.addAttribute("STATE", ds.getState());
        }
        if (ds.getVersionable() != null) {
            writer.addAttribute("VERSIONABLE", ds.getVersionable());
        }

        // Collect the datastream versions.  Fedora permits datastreams
        // to be versionable or to be limited to one only.
        for (DatastreamVersion dsv : ds.getVersionList()) {
            context.convertAnother(dsv);
        }

        writer.endNode();

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        // Create the Datastream from the XML.
        Datastream ds = new Datastream();
        ds.setId(reader.getAttribute("ID"));
        ds.setControlGroup(reader.getAttribute("CONTROL_GROUP"));
        ds.setState(reader.getAttribute("STATE"));
        ds.setVersionable(reader.getAttribute("VERSIONABLE"));

        // Collect the datastream versions.  Fedora permits datastreams
        // to be versionable or to be limited to one only.
        while (reader.hasMoreChildren()) {

            reader.moveDown();
            DatastreamVersion dsv =
                    (DatastreamVersion) context
                            .convertAnother(ds, DatastreamVersion.class);
            ds.getVersionList().add(dsv);
            reader.moveUp();

        }

        return ds;
    }

    @Override
    public boolean canConvert(Class type) {
        return Datastream.class == type;
    }

}

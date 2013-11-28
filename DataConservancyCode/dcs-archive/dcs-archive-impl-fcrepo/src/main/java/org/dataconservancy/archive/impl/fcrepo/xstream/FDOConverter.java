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

import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.archive.impl.fcrepo.dto.Datastream;
import org.dataconservancy.archive.impl.fcrepo.dto.FedoraDigitalObject;
import org.dataconservancy.archive.impl.fcrepo.dto.ObjectProperties;

/**
 * XStream converter for Fedora Digital Objects.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class FDOConverter
        extends AbstractPackageConverter {

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        // This is a root object which is automatically created
        // by the XStream marshaller.

        // The attributes are the PID, Version, Namespace and Schema.
        FedoraDigitalObject fdo = (FedoraDigitalObject) source;

        // Add the attributes.
        if (fdo.getObjectPid() != null) {
            writer.addAttribute("PID", fdo.getObjectPid());
        } else {
            // DCS requires one but not Fedora.
            throw new RuntimeException("Marshalling failed");
        }

        if (fdo.getObjectSchemaVersion() != null) {
            // This is a default and the only supported version.
            writer.addAttribute("VERSION", "1.1");
        }

        try {

            FDOStaxWriter fdoWriter = (FDOStaxWriter) writer.underlyingWriter();
            XMLStreamWriter xmlWriter = fdoWriter.getXMLStreamWriter();
            String xmlSchemaURI = "http://www.w3.org/2001/XMLSchema-instance";
            String foxmlSchemaLocation =
                    "info:fedora/fedora-system:def/foxml#"
                            + " "
                            + "http://www.fedora.info/definitions/1/0/foxml1-1.xsd";
            xmlWriter.writeNamespace("xsi", xmlSchemaURI);
            //xmlWriter.writeAttribute("xsi:schemaLocation", foxmlSchemaLocation);
            writer.addAttribute("xsi:schemaLocation", foxmlSchemaLocation);

        } catch (XMLStreamException e) {
            throw new RuntimeException("Marshalling failed", e);
        }

        // Add the object properties.
        ObjectProperties op = fdo.getObjectProperties();
        context.convertAnother(op);

        // Process each of the datastreams (no order implied).
        for (Map.Entry<String, Datastream> dse : fdo.getDatastreamMap()
                .entrySet()) {
            context.convertAnother(dse.getValue());
        }

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        FedoraDigitalObject fdo = new FedoraDigitalObject();
        String pid = reader.getAttribute("PID");
        fdo.setObjectPid(pid);

        // Process one set of Object Properties and a variable number
        // of Datastreams.
        while (reader.hasMoreChildren()) {

            reader.moveDown();

            Class nodeClass = null;
            try {
                nodeClass = Class.forName(reader.getNodeName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unmarshalling failed", e);
            }

            // Only two types of children are found in the Fedora schema.
            if (ObjectProperties.class == nodeClass) {
                // Handle the Object Properties.
                ObjectProperties objectProperties =
                        (ObjectProperties) context.convertAnother(fdo,
                                                                  nodeClass);
                fdo.setObjectProperties(objectProperties);
            } else if (Datastream.class == nodeClass) {
                // Handle the Datastream.
                Datastream ds =
                        (Datastream) context.convertAnother(fdo, nodeClass);
                fdo.getDatastreamMap().put(ds.getId(), ds);
            } else {
                throw new RuntimeException("Unmarshalling failed");
            }

            reader.moveUp();
        }

        return fdo;
    }

    @Override
    public boolean canConvert(Class type) {
        return FedoraDigitalObject.class == type;
    }

}

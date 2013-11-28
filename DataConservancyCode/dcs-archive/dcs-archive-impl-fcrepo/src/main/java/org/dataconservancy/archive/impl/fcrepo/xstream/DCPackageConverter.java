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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.archive.impl.fcrepo.dto.DCPackage;

public class DCPackageConverter
        extends AbstractPackageConverter {

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        // The attributes are only namespace related.
        DCPackage dcp = (DCPackage) source;
        writer.startNode(dcp.getClass().getName());

        try {

            FDOStaxWriter fdoWriter = (FDOStaxWriter) writer.underlyingWriter();
            XMLStreamWriter xmlWriter = fdoWriter.getXMLStreamWriter();
            String xmlSchemaURI = "http://www.w3.org/2001/XMLSchema-instance";
            // Do not use a file location for the schema as it will cause
            // unnecessary warnings in Fedora.
            //String dcpSchemaLocation =
            //        "http://dataconservancy.org/schemas/dcp/1.0 dcp.xsd";
            String dcpSchemaLocation =
                    "http://dataconservancy.org/schemas/dcp/1.0 " +
                    "http://dataconservancy.org/schemas/dcp/1.0"; 
            xmlWriter.writeNamespace("xsi", xmlSchemaURI);
            writer.addAttribute("xsi:schemaLocation", dcpSchemaLocation);

        } catch (XMLStreamException e) {
            throw new RuntimeException("Marshalling failed", e);
        }

        // Convert whatever is wrapped.
        for (Object content : dcp.getContentList()) {
            context.convertAnother(content);
        }

        writer.endNode();

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        DCPackage dcp = new DCPackage();

        // Process the content items.
        while (reader.hasMoreChildren()) {

            reader.moveDown();

            Class nodeClass = null;
            try {
                nodeClass = Class.forName(reader.getNodeName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unmarshalling failed", e);
            }

            // Handle the Object Properties.
            Object content = context.convertAnother(dcp, nodeClass);
            dcp.getContentList().add(content);

            reader.moveUp();

        }

        return dcp;
    }

    @Override
    public boolean canConvert(Class type) {
        return DCPackage.class == type;
    }

}

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

import org.dataconservancy.archive.impl.fcrepo.dto.EmbeddedRDF;
import org.dataconservancy.archive.impl.fcrepo.dto.EmbeddedRDFDescription;

/**
 * XStream converter for embedded RDF XML.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class EmbeddedRDFConverter
        extends AbstractPackageConverter {

    public static final String FORMAT_URI =
            "info:fedora/fedora-system:FedoraRELSExt-1.0";

    public static final String VERSION_LABEL = "RDF Statements";

    public static final String MIMETYPE = "application/rdf+xml";

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        //System.out.println("In Embedded RDF Converter Marshall.");
        // There are no attributes on embedded RDF though
        // there is a namespace.

        EmbeddedRDF erdf = (EmbeddedRDF) source;
        writer.startNode(erdf.getClass().getName());

        FDOStaxWriter fdoWriter = (FDOStaxWriter) writer.underlyingWriter();
        XMLStreamWriter xmlWriter = fdoWriter.getXMLStreamWriter();
        String rdfURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        String fedoraModelURI = "info:fedora/fedora-system:def/model#";
        String relURI = "info:fedora/fedora-system:def/relations-external#";
        String dcsURI = "http://dataconservancy.org/ontologies/dcs/1.0/";
        //String dcsrURI = "http://dataconservancy.org/relationships#";

        try {
            xmlWriter.writeNamespace("dcs", dcsURI);
            xmlWriter.writeNamespace("fedora-model", fedoraModelURI);
            xmlWriter.writeNamespace("rel", relURI);
            //xmlWriter.writeNamespace("dcsr", dcsrURI);
            // XStream will do this as currently configured.
            //xmlWriter.writeNamespace("rdf", rdfURI);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Marshalling failed", e);
        }

        // Collect the RDF descriptions.  Normally for REL-EXT there
        // will be just one.
        for (EmbeddedRDFDescription erdfd : erdf.getDescriptionList()) {
            context.convertAnother(erdfd);
        }

        writer.endNode();

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        // Create the XMLContent from the XML.
        EmbeddedRDF rdfContent = new EmbeddedRDF();

        reader.moveDown();

        Class nodeClass = null;
        try {
            nodeClass = Class.forName(reader.getNodeName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unmarshalling failed", e);
        }

        // Normally for RELS-EXT there is just one description element.
        EmbeddedRDFDescription description =
                (EmbeddedRDFDescription) context.convertAnother(rdfContent,
                                                                nodeClass);
        rdfContent.getDescriptionList().add(description);

        reader.moveUp();

        return rdfContent;
    }

    @Override
    public boolean canConvert(Class type) {
        return EmbeddedRDF.class == type;
    }

}

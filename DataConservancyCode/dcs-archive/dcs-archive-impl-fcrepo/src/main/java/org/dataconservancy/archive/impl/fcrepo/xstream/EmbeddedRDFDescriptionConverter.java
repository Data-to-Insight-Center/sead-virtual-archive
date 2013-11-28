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

import java.util.ArrayList;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.archive.impl.fcrepo.dto.EmbeddedRDFDescription;
import org.dataconservancy.archive.impl.fcrepo.dto.EmbeddedRDFElement;

/**
 * XStream converter for embedded RDF Description elements.
 *
 * @author Daniel Davis
 * @version $Id$
 */
public class EmbeddedRDFDescriptionConverter
        extends AbstractPackageConverter {

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        EmbeddedRDFDescription erdfd = (EmbeddedRDFDescription) source;
        writer.startNode(erdfd.getClass().getName());
        writer.addAttribute("rdf:about", erdfd.getAbout());

        // These are a series of RDF Resources each with different
        // node names and values.  Don't try to use the XStream converter,
        // Just make a node with the element name add the value.
        ArrayList<EmbeddedRDFElement> list = erdfd.getResourceList();
        for (EmbeddedRDFElement erdfr : list) {
            writer.startNode(erdfr.getName());
            if (erdfr.getResource() != null) {
                writer.addAttribute("rdf:resource", erdfr.getResource());
            } else if (erdfr.getLiteral() != null) {
                writer.setValue(erdfr.getLiteral());
            }
            writer.endNode();
        }

        writer.endNode();

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        // Create the XMLContent from the XML.
        EmbeddedRDFDescription rdfDescription = new EmbeddedRDFDescription();
        rdfDescription.setAbout(reader.getAttribute("about"));

        // These are a series of Dublin Core Elements each with different
        // node names and values.  Just record the element name and value
        // on an object then push it on a list.
        while (reader.hasMoreChildren()) {

            reader.moveDown();

            // Add the DC entity as a simple name-value pair.
            // Realize there may be duplicate names.
            EmbeddedRDFElement erdfr = new EmbeddedRDFElement();
            erdfr.setName(reader.getNodeName());
            erdfr.setResource(reader.getAttribute("resource"));
            erdfr.setLiteral(reader.getValue());
            rdfDescription.getResourceList().add(erdfr);

            reader.moveUp();

        }

        return rdfDescription;

    }

    @Override
    public boolean canConvert(Class type) {
        return EmbeddedRDFDescription.class == type;
    }

}

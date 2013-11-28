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

import org.dataconservancy.archive.impl.fcrepo.dto.DublinCore;
import org.dataconservancy.archive.impl.fcrepo.dto.EmbeddedRDF;
import org.dataconservancy.archive.impl.fcrepo.dto.XMLContent;

/**
 * XStream converter for embedded XML content.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class XMLContentConverter
        extends AbstractPackageConverter {

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        XMLContent xmlContent = (XMLContent) source;
        writer.startNode(xmlContent.getClass().getName());
        Object content = xmlContent.getContent();

        // There are no attributes however there are several types
        // of embedded XML content.  Delegate to the individual
        // converters to make the instances.

        // TODO This could mark a malformed FDO or may be legal.
        //       I need to check to determine appropriate action. DWD
        if (xmlContent.getContent() == null) {
            throw new RuntimeException("Marshalling failed");
        } else {
            // Dispatch on the object class.  The converter needs to
            // make the new XML node.
            context.convertAnother(content);
        }

        writer.endNode();

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        // Create the XMLContent from the XML.
        XMLContent xmlContent = new XMLContent();

        // Each datastream version has only one content element.  In the
        // Fedora 3.x system objects the embedded XML is a limited set.
        reader.moveDown();

        Class nodeClass = null;
        try {
            nodeClass = Class.forName(reader.getNodeName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unmarshalling failed", e);
        }

        if (DublinCore.class == nodeClass) {
            // Handle Dublin Core
            Object content = context.convertAnother(xmlContent, nodeClass);
            xmlContent.setContent(content);
        } else if (EmbeddedRDF.class == nodeClass) {
            // Handle RDF
            Object content = context.convertAnother(xmlContent, nodeClass);
            xmlContent.setContent(content);
        } else {
            // TODO There may be others Audit, DCS Metadata, RELS-INT.
            //      No action for now.
        }

        reader.moveUp();

        return xmlContent;
    }

    @Override
    public boolean canConvert(Class type) {
        return XMLContent.class == type;
    }

}

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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.archive.impl.fcrepo.dto.DublinCore;
import org.dataconservancy.archive.impl.fcrepo.dto.DublinCoreElement;

/**
 * XStream converter for embedded Dublin Core XML.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class DublinCoreConverter
        extends AbstractPackageConverter {

    public static final String TITLE = "title";

    public static final String CREATOR = "creator";

    public static final String SUBJECT = "subject";

    public static final String DESCRIPTION = "description";

    public static final String PUBLISHER = "publisher";

    public static final String DATE = "date";

    public static final String APPLICATION = "application";

    public static final String IDENTIFIER = "identifier";

    public static final String RELATION = "relation";

    public static final String RIGHTS = "rights";

    public static final String FORMAT_URI =
            "http://www.openarchives.org/OAI/2.0/oai_dc/";

    public static final String VERSION_LABEL = "Dublin Core Record";

    public static final String MIMETYPE = "text/xml";

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        // There are no attributes on Dublin Core though
        // there is a namespace and for the elements.
        // This is not handled well by XStream so we do
        // this manually.
        DublinCore dc = (DublinCore) source;
        writer.startNode(dc.getClass().getName());

        FDOStaxWriter fdoWriter = (FDOStaxWriter) writer.underlyingWriter();
        XMLStreamWriter xmlWriter = fdoWriter.getXMLStreamWriter();
        String xmlSchemaURI = "http://purl.org/dc/elements/1.1/";

        try {
            xmlWriter.writeNamespace("dc", xmlSchemaURI);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Marshalling failed", e);
        }

        // These are a series of Dublin Core Elements each with different
        // node names and values.  Don't try to use the XStream converter,
        // Just make a node with the element name add the value.
        ArrayList<DublinCoreElement> list = dc.getElementList();
        for (DublinCoreElement dce : list) {
            writer.startNode("dc:" + dce.getName());
            writer.setValue(dce.getValue());
            writer.endNode();
        }

        writer.endNode();

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        DublinCore dc = new DublinCore();

        // These are a series of Dublin Core Elements each with different
        // node names and values.  Just record the element name and value
        // on an object then push it on a list.
        while (reader.hasMoreChildren()) {

            reader.moveDown();

            // Add the DC entity as a simple name-value pair.
            // Realize there may be duplicate names.
            DublinCoreElement dce = new DublinCoreElement();
            dce.setName(reader.getNodeName());
            dce.setValue(reader.getValue());
            dc.getElementList().add(dce);

            reader.moveUp();

        }

        return dc;
    }

    @Override
    public boolean canConvert(Class type) {
        return DublinCore.class == type;
    }

}

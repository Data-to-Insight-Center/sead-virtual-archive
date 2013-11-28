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

//import java.io.Writer;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.archive.impl.fcrepo.dto.ContentLocation;
import org.dataconservancy.archive.impl.fcrepo.dto.DatastreamVersion;
import org.dataconservancy.archive.impl.fcrepo.dto.XMLContent;

/**
 * XStream converter for Fedora Datastream Version elements.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class DatastreamVersionConverter
        extends AbstractPackageConverter {

    public static final String DEFAULT_LABEL =
            "Data Conservancy Package Metadata";

    public static final String DEFAULT_FORMAT_URI =
            "http://dataconservancy.org/schemas/dcp/1.0";

    public static final String XMLCONTENT_MIME = "text/xml";
    
    public static final String BASE_VERSION = ".0";

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        DatastreamVersion dsv = (DatastreamVersion) source;
        writer.startNode(dsv.getClass().getName());

        // The attributes for each DatastreamVersion are CREATED,
        // FORMAT_URI, ID, LABEL and MIMETYPE.  Note, underscores are duplicated
        // in the output if the default replacer is used.

        // Some of these could be null, don't add null attributes.  However,
        // we could log schema errors here.
        if (dsv.getCreated() != null) {
            writer.addAttribute("CREATED", dsv.getCreated());
        }

        if (dsv.getFormatURI() != null) {
            writer.addAttribute("FORMAT_URI", dsv.getFormatURI());
        }

        if (dsv.getVersionID() != null) {
            writer.addAttribute("ID", dsv.getVersionID());
        } else {
            throw new RuntimeException("Marshalling failed");
        }

        if (dsv.getLabel() != null) {
            writer.addAttribute("LABEL", dsv.getLabel());
        } else {
            throw new RuntimeException("Marshalling failed");
        }

        if (dsv.getCreated() != null) {
            writer.addAttribute("CREATED", dsv.getCreated());
        }

        if (dsv.getMimeType() != null) {
            writer.addAttribute("MIMETYPE", dsv.getMimeType());
        } else {
            throw new RuntimeException("Marshalling failed");
        }

        // There is only one child on a Datastream version instance. However,
        // there are two possible kinds, embedded XML or a contentLocation.
        // There may be several types of embedded XML.
        context.convertAnother(dsv.getContent());

        writer.endNode();

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        // Create the DatastreamVersion from the XML.
        DatastreamVersion dsv = new DatastreamVersion();
        dsv.setCreated(reader.getAttribute("CREATED"));
        dsv.setFormatURI(reader.getAttribute("FORMAT_URI"));
        dsv.setVersionID(reader.getAttribute("ID"));
        dsv.setLabel(reader.getAttribute("LABEL"));
        dsv.setMimeType(reader.getAttribute("MIMETYPE"));

        // There is only one child on a datastream version instance. However,
        // there are two possible kinds, embedded XML or a contentLocation.
        // There may be several types of embedded XML.
        reader.moveDown();

        Class nodeClass = null;
        try {
            nodeClass = Class.forName(reader.getNodeName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unmarshalling failed", e);
        }

        // Choose the conversion based on the XML element name.
        if (ContentLocation.class == nodeClass) {
            // Handle contentLocation
            Object content = context.convertAnother(dsv, ContentLocation.class);
            dsv.setContent(content);
        } else if (XMLContent.class == nodeClass) {
            // Handle xmlContent.  This could be DC, RDF or other.
            Object content = context.convertAnother(dsv, XMLContent.class);
            dsv.setContent(content);
        } else {
            throw new RuntimeException("Unmarshalling failed");
        }

        reader.moveUp();

        return dsv;
    }

    @Override
    public boolean canConvert(Class type) {
        return DatastreamVersion.class == type;
    }

}

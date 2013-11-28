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

import java.io.ByteArrayOutputStream;

import java.nio.charset.Charset;

import javax.xml.stream.XMLStreamException;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.builder.xstream.DcsStaxReader;
import org.dataconservancy.model.builder.xstream.MetadataConverter;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

public class FDOMetadataConverter
        extends MetadataConverter {

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {

        // This just performs a copy without serialization.  However,
        // it still uses the namespace bindings.
        if (source instanceof DcsMetadata) {

            final DcsMetadata md = (DcsMetadata) source;
            final String schemaURI = md.getSchemaUri();
            final String metadataAsString = md.getMetadata();

            if (!isEmptyOrNull(metadataAsString)) {
                if (!isEmptyOrNull(schemaURI)) {
                    writer.addAttribute(A_SCHEMA, schemaURI);
                }

                //System.out.println("Metadata: " + metadataAsString);
                final FDOStaxWriter staxWriter =
                        (FDOStaxWriter) writer.underlyingWriter();

                try {
                    staxWriter.copyNode(metadataAsString, staxWriter
                            .getXMLStreamWriter());
                } catch (XMLStreamException e) {
                    throw new RuntimeException("Marshalling failed", e);
                }

                // This strips namespaces or uses the global namespace which causes
                // unpredictable problems.  Until this problem is worked out, the
                // XStream copier cannot be used reliably.
                //StringReader in = new StringReader(metadataAsString);
                //FDOXStream x = FDOEntityVisitor.x;
                //HierarchicalStreamReader reader =
                //        x.getDriver().createReader(in);
                //FDOStaxWriter fdoWriter = (FDOStaxWriter) writer.underlyingWriter();
                //XMLStreamWriter xmlWriter = fdoWriter.getXMLStreamWriter();
                //HierarchicalStreamCopier copier =
                //        new HierarchicalStreamCopier();
                //copier.copy(reader, xmlWriter);

            }

        } else if (source instanceof DcsMetadataRef) {

            final DcsMetadataRef mdRef = (DcsMetadataRef) source;
            final String ref = mdRef.getRef();
            if (!isEmptyOrNull(ref)) {
                writer.addAttribute(A_REF, ref);
            }

        } else {
            throw new RuntimeException("Marshalling failed");
        }

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        final String schemaUri = reader.getAttribute(A_SCHEMA);
        final String mdRef = reader.getAttribute(A_REF);

        // If we are dealing with a reference, create the reference and return
        if (!isEmptyOrNull(mdRef)) {
            final DcsMetadataRef ref = new DcsMetadataRef();
            ref.setRef(mdRef);
            return ref;
        }

        // Otherwise we're dealing with a full, in-line DcsMetadata object
        final DcsMetadata md = new DcsMetadata();

        if (!isEmptyOrNull(schemaUri)) {
            md.setSchemaUri(schemaUri);
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        reader.moveDown();
        try {
            ((DcsStaxReader) reader.underlyingReader()).copyNode(out);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Unmarshalling failed", e);
        }
        reader.moveUp();
        final String metadataAsString =
                new String(out.toByteArray(), Charset.forName("UTF-8"));
        md.setMetadata(metadataAsString);

        return md;
    }

}

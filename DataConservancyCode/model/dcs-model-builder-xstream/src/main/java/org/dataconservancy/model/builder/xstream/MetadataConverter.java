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
package org.dataconservancy.model.builder.xstream;

import java.io.ByteArrayOutputStream;

import java.nio.charset.Charset;

import javax.xml.stream.XMLStreamException;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Converts the xml representation of a {@link DcsMetadata} or {@link DcsMetadataRef} to the Java object model and back.
 * <p/>
 * Example XML (inline):
 * <pre>
 * &lt;metadata schemaURI="http://sdss.org/metadata/astroSchema.example.xsd"&gt;
 * &lt;md xmlns:astro="http://sdss.org/astro"&gt;
 * &lt;astro:skyCoverage&gt;all of it&lt;/astro:skyCoverage&gt;
 * &lt;astro:enfOfWorldFactor&gt;-1&lt;/astro:enfOfWorldFactor&gt;
 * &lt;/md&gt;
 * &lt;/metadata&gt;
 * </pre>
 * Example XML (by reference):
 * <pre>
 * &lt;metadata ref="urn:sdss:12345/metadata" /&gt;
 * </pre>
 */
public class MetadataConverter extends AbstractEntityConverter {

    public static final String E_METADATA = "metadata";
    public static final String A_SCHEMA = "schemaURI";
    public static final String A_REF = "ref";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        // Inside <metadata>

        if (source instanceof DcsMetadata) {
            final DcsMetadata md = (DcsMetadata) source;
            final String schemaUri = md.getSchemaUri();
            String metadataBlob = md.getMetadata();

            if (!isEmptyOrNull(metadataBlob)) {
                if (!isEmptyOrNull(schemaUri)) {
                    writer.addAttribute(A_SCHEMA, schemaUri);
                }

                if (metadataBlob.trim().startsWith("<?xml")) {
                    log.trace("Found XML processing instruction in metadata blob: stripping it!");
                    metadataBlob = removeProcessingInstruction(metadataBlob);
                }
                
                try {
                    final DcsStaxWriter staxWriter = (DcsStaxWriter) writer.underlyingWriter();
                    log.trace("Copying metadata blob: \n{}", metadataBlob);
                    staxWriter.copyNode(metadataBlob, staxWriter.getXMLStreamWriter());

                } catch (XMLStreamException e) {
                    throw new ConversionException(e.getMessage(), e);
                }

            }

        } else if (source instanceof DcsMetadataRef) {
            final DcsMetadataRef mdRef = (DcsMetadataRef) source;
            final String ref = mdRef.getRef();
            if (!isEmptyOrNull(ref)) {
                writer.addAttribute(A_REF, ref);
            }
        } else {
            log.warn("Unable to marshal metadata source object {} of type {}", source, source.getClass().getName());
        }
    }

    /**
     * Ugly procedure to remove XML processing instructions (PI) from metadata.
     * <p/>
     * This method is designed to handle the most likely scenario: one supplies a complete XML document as metadata, and
     * the document starts with a PI:
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?>
       &lt;McMurdoDryValleys>
         &lt;FieldPhotos>
           &lt;FieldPhoto>
           &lt;!-- and so on -->
           &lt;/FieldPhoto>
         &lt;/FieldPhotos>
       &lt;/McMurdoDryValleys>
     * </pre>
     * <p/>
     * It is helpful to remember that XStream is processing a stream of XML, and when this MetadataConverter is invoked,
     * XStream is somewhere in the middle of the stream.  If the underlying XML parser encounters a PI from inline
     * metadata, things hit the hizzy.  So PIs need to be removed from inline XML metadata, either prior to parsing,
     * or during parsing.
     *
     * @param metadataBlob the blob of xml metadata, which may start with multiple XML processing instructions
     * @return the XML sans any PIs.  If
     */
    private String removeProcessingInstruction(final String metadataBlob) {
        // Trim the string to remove preceding whitespace
        String trimmed = metadataBlob.trim();

        // Offsets in the string for the start and end positions of the PI
        int piStartOffset = -1;
        int piEndOffset = -1;

        // While we still have characters in the string and the start and end PI positions are not initialized...

        for (int i = 0; i < trimmed.length() && (piStartOffset == -1 || piEndOffset == -1); i++) {

            // loop through the characters of the string and attempt to find the start and end offsets of the PI.

            final char currentChar = trimmed.charAt(i);
            log.trace("Examining character: {}", currentChar);
            if (currentChar == '<') {
                piStartOffset = i;
                log.trace("PI start offset: {}", piStartOffset);
            }
            if (currentChar == '>') {
                piEndOffset = i;
                log.trace("PI end offset: {}", piEndOffset);
            }
        }

        // If a PI was found ...

        if (piEndOffset > 0) {

            // Strip off the PI
            String stripped = trimmed.substring(piEndOffset+1).trim();

            // Check to see if there are more PIs, and call this method recursively
            if (stripped.startsWith("<?xml")) {
                return removeProcessingInstruction(stripped);
            }

            // Return the metadata blob sans PI
            return stripped;
        }

        // Else no PI was found, so return the unadultrated blob.
        
        return metadataBlob;

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
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
                throw new ConversionException(e.getMessage(), e);
            }
            reader.moveUp();
            final String mdValue = new String(out.toByteArray(), Charset.forName("UTF-8"));
            md.setMetadata(mdValue);
        log.trace("Deserialized metadata blob \n{}", mdValue);
        return md;
    }


    @Override
    public boolean canConvert(Class type) {
        return DcsMetadata.class == type || DcsMetadataRef.class == type;
    }
}

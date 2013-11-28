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

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.dcs.*;

import java.util.Collection;
import java.util.Set;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Converts the xml representation of a {@link DcsFile} to the Java object model and back.
 * <p/>
 * Example XML:
 * <pre>
 * &lt;File id="urn:sdss:12345/FITS_FILE" src="http://sdss.org/files/fits/12345.fits"&gt;
 * &lt;fileName&gt;12345.fits&lt;/fileName&gt;
 * &lt;extant&gt;false&lt;/extant&gt;
 * &lt;format&gt;
 * &lt;id scheme="http://www.nationalarchives.gov.uk/PRONOM/"&gt;x-fmt/383&lt;/id&gt;
 * &lt;name&gt;FITS&lt;/name&gt;
 * &lt;version&gt;3.0&lt;/version&gt;
 * &lt;/format&gt;
 * &lt;/File
 * </pre>
 */
public class FileConverter extends AbstractEntityConverter {

    /**
     * The File element name
     */
    public static final String E_FILE = "File";
    /**
     * The Filename element name
     */
    public static final String E_FILENAME = "fileName";
    /**
     * The extant element name
     */
    public static final String E_EXTANT = "extant";
    /**
     * The valid element name
     */
    public static final String E_VALID = "valid";
    /**
     * The size element name
     */
    public static final String E_SIZE = "size";
    /**
     * The alternate identifiers element name.
     */
    public static final String E_ALTERNATEID = "alternateIdentifier";

    /**
     * The identity attribute name
     */
    public static final String A_ID = "id";
    /**
     * The source attribute name
     */
    public static final String A_SRC = "src";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        final DcsFile file = (DcsFile) source;
        final Collection<DcsResourceIdentifier> altIds = file.getAlternateIds();

        if (!isEmptyOrNull(file.getId())) {
            writer.addAttribute(A_ID, file.getId());
        }

        if (!isEmptyOrNull(file.getSource())) {
            writer.addAttribute(A_SRC, file.getSource());
        }
                        
        if (altIds != null) {
            for (DcsResourceIdentifier rid : altIds) {
                writer.startNode(E_ALTERNATEID);
                context.convertAnother(rid);
                writer.endNode();
            }
        }

        if (!isEmptyOrNull(file.getName())) {
            writer.startNode(E_FILENAME);
            writer.setValue(file.getName());
            writer.endNode();
        }

        writer.startNode(E_EXTANT);
        if (file.isExtant()) {
            writer.setValue(Boolean.toString(true));
        } else {
            writer.setValue(Boolean.toString(false));
        }
        writer.endNode();

        if (file.getSizeBytes() > -1) {
            writer.startNode(E_SIZE);
            writer.setValue(String.valueOf(file.getSizeBytes()));
            writer.endNode();
        }

        for (DcsFixity f : file.getFixity()) {
            writer.startNode(FixityConverter.E_FIXITY);
            context.convertAnother(f);
            writer.endNode();
        }

        for (DcsFormat f : file.getFormats()) {
            writer.startNode(FormatConverter.E_FORMAT);
            context.convertAnother(f);
            writer.endNode();
        }
        
        if (file.getValid() != null) {
            writer.startNode(E_VALID);
            writer.setValue(file.getValid().toString());
            writer.endNode();
        }

        for (DcsMetadata m : file.getMetadata()) {
            writer.startNode(MetadataConverter.E_METADATA);
            context.convertAnother(m);
            writer.endNode();
        }

        for (DcsMetadataRef m : file.getMetadataRef()) {
            writer.startNode(MetadataConverter.E_METADATA);
            context.convertAnother(m);
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final DcsFile file = new DcsFile();

        // inside the <File> element
        final String id = reader.getAttribute(A_ID);
        final String src = reader.getAttribute(A_SRC);
        if (!isEmptyOrNull(id)) {
            file.setId(id);
        }
        if (!isEmptyOrNull(src)) {
            file.setSource(src);
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String name = getElementName(reader);//reader.getNodeName();

            if (name.equals(E_FILENAME) || name.equals(E_EXTANT) || name.equals(E_VALID) || name.equals(E_SIZE)) {
                final String value = reader.getValue();

                if (name.equals(E_FILENAME) && !isEmptyOrNull(value)) {
                    file.setName(value);
                }

                if (name.equals(E_EXTANT) && !isEmptyOrNull(value)) {
                    try {
                        file.setExtant(Boolean.parseBoolean(value));
                    } catch (Exception e) {
                        final String msg = "Unable to parse boolean value '" + value + "' for element '" + E_EXTANT + "': " + e.getMessage();
                        log.error(msg, e);
                        throw new ConversionException(msg, e);
                    }
                }

                if (name.equals(E_VALID) && !isEmptyOrNull(value)) {
                    try {
                        file.setValid(Boolean.parseBoolean(value));
                    } catch (Exception e) {
                        final String msg = "Unable to parse boolean value '" + value + "' for element '" + E_VALID + "': " + e.getMessage();
                        log.error(msg, e);
                        throw new ConversionException(msg, e);
                    }
                }

                if (name.equals(E_SIZE) && !isEmptyOrNull(value)) {
                    try {
                        file.setSizeBytes(Long.parseLong(value));
                    }catch (Exception e) {
                        final String msg = "Unable to parse long value '" + value + "' for element '" + E_SIZE + "': " + e.getMessage();
                        log.error(msg, e);
                        throw new ConversionException(msg, e);
                    }
                }
            }

            if (name.equals(FixityConverter.E_FIXITY)) {
                final DcsFixity fixity = (DcsFixity) context.convertAnother(file, DcsFixity.class);
                if (fixity != null) {
                    file.addFixity(fixity);
                }
            }

            if (name.equals(FormatConverter.E_FORMAT)) {
                final DcsFormat format = (DcsFormat) context.convertAnother(file, DcsFormat.class);
                if (format != null) {
                    file.addFormat(format);
                }
            }

            if (name.equals(E_ALTERNATEID)) {
                final DcsResourceIdentifier rid =
                    (DcsResourceIdentifier) context.convertAnother(file, DcsResourceIdentifier.class);
                if (rid != null) { file.addAlternateId(rid); }
                reader.moveUp();
                continue;
            }

            if (name.equals(MetadataConverter.E_METADATA)) {
                final String ref = reader.getAttribute(MetadataConverter.A_REF);
                if (!isEmptyOrNull(ref)) {
                    final DcsMetadataRef mdRef = (DcsMetadataRef) context.convertAnother(file, DcsMetadataRef.class);
                    if (mdRef != null) {
                        file.addMetadataRef(mdRef);
                    }
                } else {
                    final DcsMetadata md = (DcsMetadata) context.convertAnother(file, DcsMetadata.class);
                    if (md != null) {
                        file.addMetadata(md);
                    }
                    continue; // reader is already positioned properly, so we skip the reader.moveUp() call at the bottom of the while(reader.hasMoreChildren()) loop.
                }


            }

            reader.moveUp();
        }

        return file;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsFile.class == type;
    }
}

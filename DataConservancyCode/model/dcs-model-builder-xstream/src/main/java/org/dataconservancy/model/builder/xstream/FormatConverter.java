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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.dcs.DcsFormat;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;


/**
 * Converts the xml representation of a {@link DcsFormat} to the Java object model and back.
 * <p/>
 * Example XML:
 * <pre>
 * &lt;format&gt;
 * &lt;id scheme="http://www.iana.org/assignments/media-types/"&gt;text/csv&lt;/id&gt;
 * &lt;/format&gt;
 * </pre>
 * <p/>
 * <pre>
 * &lt;format&gt;
 * &lt;id scheme="http://www.nationalarchives.gov.uk/PRONOM/"&gt;x-fmt/383&lt;/id&gt;
 * &lt;name&gt;FITS&lt;/name&gt;
 * &lt;version&gt;3.0&lt;/version&gt;
 * &lt;/format&gt;
 * </pre>
 */
public class FormatConverter extends AbstractEntityConverter {

    /**
     * Format element name
     */
    public static final String E_FORMAT = "format";
    /**
     * Format name element name
     */
    public static final String E_NAME = "name";
    /**
     * Format id element name
     */
    public static final String E_ID = "id";
    /**
     * Format version element name
     */
    public static final String E_VERSION = "version";

    /**
     * Format scheme attribute name
     */
    public static final String A_SCHEME = "scheme";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        final DcsFormat format = (DcsFormat) source;

        writer.startNode(E_ID);
        if (format.getSchemeUri() != null) {
            writer.addAttribute(A_SCHEME, format.getSchemeUri());
        }
        
        if (format.getFormat() != null) {
            writer.setValue(format.getFormat());
        }
        
        writer.endNode();


        if (format.getName() != null) {
            writer.startNode(E_NAME);
            writer.setValue(format.getName());
            writer.endNode();
        }

        if (format.getVersion() != null) {
            writer.startNode(E_VERSION);
            writer.setValue(format.getVersion());
            writer.endNode();
        }

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final DcsFormat format = new DcsFormat();

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            if (getElementName(reader).equals(E_ID)) {
                final String schemeAttr = reader.getAttribute(A_SCHEME);
                if (!isEmptyOrNull(schemeAttr)) {
                    format.setSchemeUri(schemeAttr);
                }
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    format.setFormat(value);
                }
                reader.moveUp();
                continue;
            }

            final String value = reader.getValue();
            if (isEmptyOrNull(value)) {
                reader.moveUp();
                continue;
            }

            if (getElementName(reader).equals(E_NAME)) {
                format.setName(value);
            }

            if (getElementName(reader).equals(E_VERSION)) {
                format.setVersion(value);
            }

            reader.moveUp();
        }        

        return format;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsFormat.class == type;
    }
}

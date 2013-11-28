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

import org.dataconservancy.model.dcs.DcsFixity;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Converts the xml representation of a {@link DcsFixity} to the Java object model and back.
 * <p/>
 * Example XML:
 * <pre>
 * &lt;fixity algorithm="md5"&gt;fe5b3b4f78b9bf3ae21cd52c2f349174&lt;/fixity&gt;
 * </pre>
 */
public class FixityConverter extends AbstractEntityConverter {

    /**
     * The fixity element name
     */
    public static final String E_FIXITY = "fixity";
    /**
     * The algorithm attribute name
     */
    public static final String A_ALGO = "algorithm";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);
        final DcsFixity fixity = (DcsFixity) source;

        if (!isEmptyOrNull(fixity.getAlgorithm())) {
            writer.addAttribute(A_ALGO, fixity.getAlgorithm());
        }
        
        if (!isEmptyOrNull(fixity.getValue())) {
            writer.setValue(fixity.getValue());
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final DcsFixity fixity = new DcsFixity();

        final String algo = reader.getAttribute(A_ALGO);
        final String value = reader.getValue();

        // inside the <fixity> element
        if (!isEmptyOrNull(algo)) {
            fixity.setAlgorithm(algo);
        }

        if (!isEmptyOrNull(value)) {
            fixity.setValue(value);
        }

        return fixity;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsFixity.class == type;
    }
}

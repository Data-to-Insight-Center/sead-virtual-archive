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
package org.seadva.model.builder.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.seadva.model.SeadPerson;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Converts the xml representation of a {@link org.dataconservancy.model.dcs.DcsFixity} to the Java object model and back.
 * <p/>
 * Example XML:
 * <pre>
 * &lt;fixity algorithm="md5"&gt;fe5b3b4f78b9bf3ae21cd52c2f349174&lt;/fixity&gt;
 * </pre>
 */
public class PersonConverter extends AbstractEntityConverter {


    public static final String E_NAME = "name";
    public static final String E_ID = "id";
    public static final String E_TYPE = "idType";


    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        final SeadPerson person = (SeadPerson) source;

        if (!isEmptyOrNull(person.getId())) {
            writer.startNode(E_ID);
            writer.setValue(person.getId());
            writer.endNode();
        }

        if (!isEmptyOrNull(person.getName())) {
            writer.startNode(E_NAME);
            writer.setValue(person.getName());
            writer.endNode();
        }

        if (!isEmptyOrNull(person.getIdType())){
            writer.startNode(E_TYPE);
            writer.setValue(person.getIdType());
            writer.endNode();
        }

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        final SeadPerson person = new SeadPerson();

        while (reader.hasMoreChildren()) {
            reader.moveDown();


            final String value = reader.getValue();
            if (isEmptyOrNull(value)) {
                reader.moveUp();
                continue;
            }

            if (getElementName(reader).equals(E_NAME)) {
                person.setName(value);
            }

            if (getElementName(reader).equals(E_ID)) {
                person.setId(value);
            }

            if (getElementName(reader).equals(E_TYPE)) {
                person.setIdType(value);
            }

            reader.moveUp();
        }

        return person;
    }

    @Override
    public boolean canConvert(Class type) {
        return SeadPerson.class == type;
    }
}

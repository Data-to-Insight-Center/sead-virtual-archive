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
import org.seadva.model.SeadRepository;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Converts the xml representation of a {@link org.dataconservancy.model.dcs.DcsFixity} to the Java object model and back.
 * <p/>
 * Example XML:
 * <pre>
 * &lt;fixity algorithm="md5"&gt;fe5b3b4f78b9bf3ae21cd52c2f349174&lt;/fixity&gt;
 * </pre>
 */
public class RepositoryConverter extends AbstractEntityConverter {


    public static final String E_REPOSITORY= "Repository";

    public static final String E_IRID = "irid";
    public static final String E_NAME = "name";
    public static final String E_URL = "url";
    public static final String E_TYPE = "type";


    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        final SeadRepository repository = (SeadRepository) source;



        if (!isEmptyOrNull(repository.getIrId())) {
            writer.startNode(E_IRID);
            writer.setValue(repository.getIrId());
            writer.endNode();
        }

        if (!isEmptyOrNull(repository.getName())) {
            writer.startNode(E_NAME);
            writer.setValue(repository.getName());
            writer.endNode();
        }

        if (!isEmptyOrNull(repository.getUrl())) {
            writer.startNode(E_URL);
            writer.setValue(repository.getUrl());
            writer.endNode();
        }

        if (!isEmptyOrNull(repository.getType())) {
            writer.startNode(E_TYPE);
            writer.setValue(repository.getType());
            writer.endNode();
        }

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        final SeadRepository repository = new SeadRepository();

        while (reader.hasMoreChildren()) {
            reader.moveDown();


            final String value = reader.getValue();
            if (isEmptyOrNull(value)) {
                reader.moveUp();
                continue;
            }

            if (getElementName(reader).equals(E_IRID)) {
                repository.setIrId(value);
            }

            if (getElementName(reader).equals(E_NAME)) {
                repository.setName(value);
            }

            if (getElementName(reader).equals(E_URL)) {
                repository.setUrl(value);
            }

            if (getElementName(reader).equals(E_TYPE)) {
                repository.setType(value);
            }

            reader.moveUp();
        }

        return repository;
    }

    @Override
    public boolean canConvert(Class type) {
        return SeadRepository.class == type;
    }
}

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

package org.dataconservancy.ui.model.builder.xstream;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.ui.model.DataFile;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * An XStream implementation of a serializer and deserializer for File business
 * objects suitable for use in applications and storage in the DCS archive.
 */
public class DataFileConverter
        extends AbstractEntityConverter
        implements ConverterConstants {

    protected static final String E_FILE = "file";

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        final DataFile fileSource = (DataFile) source;
        if (fileSource != null) {

            if (!isEmptyOrNull(fileSource.getId())) {
                writer.addAttribute(E_ID, fileSource.getId());
            }

            if (!isEmptyOrNull(fileSource.getParentId())) {
                writer.startNode(E_PARENT_ID);
                writer.setValue(fileSource.getParentId());
                writer.endNode();
            }

            if (!isEmptyOrNull(fileSource.getSource())) {
                writer.startNode(E_SOURCE);
                writer.setValue(fileSource.getSource());
                writer.endNode();
            }

            if (!isEmptyOrNull(fileSource.getFormat())) {
                writer.startNode(E_FORMAT);
                writer.setValue(fileSource.getFormat());
                writer.endNode();
            }

            if (!isEmptyOrNull(fileSource.getName())) {
                writer.startNode(E_NAME);
                writer.setValue(fileSource.getName());
                writer.endNode();
            }

            if (!isEmptyOrNull(fileSource.getPath())) {
                writer.startNode(E_PATH);
                writer.setValue(fileSource.getPath());
                writer.endNode();
            }

            writer.startNode(E_SIZE);
            context.convertAnother(fileSource.getSize());
            writer.endNode();

        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        DataFile fileObject = new DataFile();
        fileObject.setId(reader.getAttribute(E_ID));

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String ename = getElementName(reader);

             if (ename.equals(E_SOURCE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    fileObject.setSource(value.trim());
                }
            } else if (ename.equals(E_FORMAT)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    fileObject.setFormat(value.trim());
                }
            } else if (ename.equals(E_NAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    fileObject.setName(value.trim());
                }
            } else if (ename.equals(E_PATH)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    fileObject.setPath(value.trim());
                }
            } else if (ename.equals(E_SIZE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    // TODO: This will throw NumberFormatException if the field is garbage.
                    //       But being silent is worse.
                    fileObject.setSize(Long.parseLong(value));
                }
             } else if (ename.equals(E_PARENT_ID)) {
                 final String value = reader.getValue();
                 if (!isEmptyOrNull(value)) {
                     fileObject.setParentId(value.trim());
                 }
          }
            reader.moveUp();
        }

        return fileObject;
    }

    @Override
    public boolean canConvert(Class type) {
        return type == DataFile.class;
    }

}

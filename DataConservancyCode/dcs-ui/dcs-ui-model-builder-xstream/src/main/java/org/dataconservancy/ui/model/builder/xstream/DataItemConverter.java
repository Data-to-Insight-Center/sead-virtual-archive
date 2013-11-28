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
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.joda.time.DateTime;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;
import static org.dataconservancy.model.dcs.support.Util.isNull;

/**
 * An XStream implementation of a serializer and deserializer for DataItem
 * (being refactored into DataItem) business objects suitable for use in
 * applications and storage in the DCS archive.
 */
public class DataItemConverter
        extends AbstractEntityConverter
        implements ConverterConstants {

    private static final String E_FILE = "file";

    public DataItemConverter() {
    }

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        final DataItem dataItemSource = (DataItem) source;
        if (dataItemSource != null) {

            if (!isEmptyOrNull(dataItemSource.getId())) {
                writer.addAttribute(E_ID, dataItemSource.getId());
            }

            if (!isEmptyOrNull(dataItemSource.getName())) {
                writer.startNode(E_NAME);
                writer.setValue(dataItemSource.getName());
                writer.endNode();
            }

            if (!isEmptyOrNull(dataItemSource.getDescription())) {
                writer.startNode(E_DESCRIPTION);
                writer.setValue(dataItemSource.getDescription());
                writer.endNode();
            }

            if (!isNull(dataItemSource.getDepositorId())) {
                writer.startNode(E_DEPOSITOR);
                writer.addAttribute(ATTR_REF, dataItemSource.getDepositorId());
                writer.endNode();
            }

            if (!isNull(dataItemSource.getDepositDate())) {
                writer.startNode(E_DEPOSIT_DATE);
                context.convertAnother(dataItemSource.getDepositDate());
                writer.endNode();
            }

            List<DataFile> fileList = dataItemSource.getFiles();
            if (fileList != null && !fileList.isEmpty()) {
                writer.startNode(E_FILES);
                for (DataFile dataFile : fileList) {
                    writer.startNode(DataItemConverter.E_FILE);
                    context.convertAnother(dataFile);
                    writer.endNode();
                }
                writer.endNode();
            }
            
            if (!isEmptyOrNull(dataItemSource.getParentId())) {
                writer.startNode(E_PARENT_ID);
                writer.setValue(dataItemSource.getParentId());
                writer.endNode();
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        DataItem dataItemObject = new DataItem();
        dataItemObject.setId(reader.getAttribute(E_ID));
        DateTime depositDate = null;
        Person depositor = null;
        DataFile dataFile = null;

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String ename = getElementName(reader);

            if (ename.equals(E_NAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dataItemObject.setName(value.trim());
                }
            } else if (ename.equals(E_DESCRIPTION)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dataItemObject.setDescription(value.trim());
                }
            } else if (ename.equals(E_DEPOSITOR)) {
                final String personId = reader.getAttribute(ATTR_REF);
                if (!isEmptyOrNull(personId)) {
                    dataItemObject.setDepositorId(personId.trim());
                }
            } else if (ename.equals(E_DEPOSIT_DATE)) {
                reader.moveDown();
                depositDate =
                        (DateTime) context.convertAnother(depositDate,
                                                          DateTime.class);
                dataItemObject.setDepositDate(depositDate);
                reader.moveUp();
            } else if (ename.equals(E_FILES)) {
                List<DataFile> fileList = new ArrayList<DataFile>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(DataItemConverter.E_FILE)) {
                        dataFile =
                                (DataFile) context
                                        .convertAnother(dataFile,
                                                        DataFile.class);
                        fileList.add(dataFile);
                    }
                    reader.moveUp();
                }
                dataItemObject.setFiles(fileList);
            } else if (ename.equals(E_PARENT_ID)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dataItemObject.setParentId(value);
                }
            }

            reader.moveUp();
        }

        return dataItemObject;
    }

    @Override
    public boolean canConvert(Class type) {
        return type == DataItem.class;
    }

}

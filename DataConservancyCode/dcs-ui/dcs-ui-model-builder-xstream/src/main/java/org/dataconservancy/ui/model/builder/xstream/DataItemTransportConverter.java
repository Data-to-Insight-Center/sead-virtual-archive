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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.DataItemTransport;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;
import static org.dataconservancy.model.dcs.support.Util.isNull;

/**
 * An XStream implementation of a serializer and deserializer for
 * DataItemTransport business objects suitable for use in
 * applications.  DataItemTransport objects are not to be stored
 * in the archive.
 */
public class DataItemTransportConverter
        extends AbstractEntityConverter
        implements ConverterConstants {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public DataItemTransportConverter() {
    }

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);


        final DataItemTransport dataItemTransportSource = (DataItemTransport) source;
        if (dataItemTransportSource != null) {

            writer.startNode(E_DATA_ITEM_TRANSPORT);

            if (!isNull(dataItemTransportSource.getDataItem())) {
                writer.startNode(E_DATA_ITEM);
                context.convertAnother(dataItemTransportSource.getDataItem());
                writer.endNode();
            }

            if (!isNull(dataItemTransportSource.getInitialDepositDate())) {
                writer.startNode(E_INITIAL_DEPOSIT_DATE);
                context.convertAnother(dataItemTransportSource.getInitialDepositDate());
                writer.endNode();
            }

            if (!isNull(dataItemTransportSource.getDepositStatus())) {
                writer.startNode(E_DEPOSIT_STATUS);
                writer.setValue(dataItemTransportSource.getDepositStatus().toString());
                writer.endNode();
            }

            writer.endNode();

        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        DataItemTransport dataItemTransportObject = new DataItemTransport();
        DateTime initialDepositDate = null;
        DataItem dataItem = null;
        ArchiveDepositInfo.Status depositStatus = null;

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String ename = getElementName(reader);

            if (ename.equals(E_INITIAL_DEPOSIT_DATE)) {
                reader.moveDown();
                initialDepositDate = (DateTime) context.convertAnother(initialDepositDate, DateTime.class);
                dataItemTransportObject.setInitialDepositDate(initialDepositDate);
                reader.moveUp();
            } else if (ename.equals(E_DATA_ITEM)) {
                reader.moveDown();
                dataItem = (DataItem) context.convertAnother(dataItem, DataItem.class);
                dataItemTransportObject.setDataItem(dataItem);
                reader.moveUp();
            } else if (ename.equals(E_DEPOSIT_STATUS)) {

                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dataItemTransportObject.setDepositStatus(ArchiveDepositInfo.Status.valueOf(value.toUpperCase().trim()));
                }

            }

            reader.moveUp();
        }

        return dataItemTransportObject;
    }

    @Override
    public boolean canConvert(Class type) {
        return type == DataItemTransport.class;
    }

}

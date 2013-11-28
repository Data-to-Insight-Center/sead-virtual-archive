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
import com.thoughtworks.xstream.io.json.JsonWriter;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.ui.model.AdiAjaxTransport;
import org.dataconservancy.ui.model.DataItemTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.dataconservancy.model.dcs.support.Util.isNull;

/**
 * An XStream implementation of a serializer and deserializer for
 * AdiAjaxTransport objects used by the AdiAjaxController.
 * AdiAjaxTransport objects are not to be stored
 * in the archive.
 */
public class AdiAjaxTransportConverter
        extends AbstractEntityConverter
        implements ConverterConstants {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public AdiAjaxTransportConverter() {
    }

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);


        final AdiAjaxTransport adiAjaxTransportSource = (AdiAjaxTransport) source;
        if (adiAjaxTransportSource != null) {

            writer.startNode("");

            if (!isNull(adiAjaxTransportSource.getMessage())) {
                writer.startNode("message");
                writer.setValue(adiAjaxTransportSource.getMessage());
                writer.endNode();
            }

            List<DataItemTransport> dataItemTransportList = adiAjaxTransportSource.getDataItemTransportList();
            if (!isNull(dataItemTransportList) && !dataItemTransportList.isEmpty()) {
                ((JsonWriter) writer).startNode("dataItemTransportList", dataItemTransportList.getClass());
                for (DataItemTransport dataItemTransport: dataItemTransportList) {
                    context.convertAnother(dataItemTransport);
                }
                writer.endNode();
            }

            writer.endNode();

        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        AdiAjaxTransport adiAjaxTransportObject = new AdiAjaxTransport();
        String message = null;
        DataItemTransport dataItemTransport = null;

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String ename = getElementName(reader);

            if (ename.equals("message")) {
                reader.moveDown();
                String value = reader.getValue();
                if (!isNull(value) && !value.isEmpty()) {
                    adiAjaxTransportObject.setMessage(value);
                }
                reader.moveUp();
            } else if (ename.equals("dataItemTransportList")) {
                List<DataItemTransport> dataItemTransportList = new ArrayList<DataItemTransport>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_DATA_ITEM_TRANSPORT)) {
                        dataItemTransport = (DataItemTransport) context.convertAnother(dataItemTransport, DataItemTransport.class);
                        dataItemTransportList.add(dataItemTransport);
                    }
                    reader.moveUp();
                }
                adiAjaxTransportObject.setDataItemTransportList(dataItemTransportList);
            }

            reader.moveUp();
        }

        return adiAjaxTransportObject;
    }

    @Override
    public boolean canConvert(Class type) {
        return type == AdiAjaxTransport.class;
    }

}

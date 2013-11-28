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
import org.seadva.model.SeadLogDetail;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Converts the xml representation of a {@link org.dataconservancy.model.dcs.DcsFixity} to the Java object model and back.
 * <p/>
 * Example XML:
 * <pre>
 * &lt;fixity algorithm="md5"&gt;fe5b3b4f78b9bf3ae21cd52c2f349174&lt;/fixity&gt;
 * </pre>
 */
public class SeadLogDetailConverter extends AbstractEntityConverter {


    public static final String E_LOGDETAIL= "logDetail";

    public static final String E_IPADDRESS = "ipAddress";
    public static final String E_USERAGENT = "userAgent";
    public static final String E_SUBJECT = "subject";
    public static final String E_NODEIDENTIFIER = "nodeIdentifier";


    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        final SeadLogDetail logDetail = (SeadLogDetail) source;

        if (!isEmptyOrNull(logDetail.getIpAddress())) {
            writer.startNode(E_IPADDRESS);
            writer.setValue(logDetail.getIpAddress());
            writer.endNode();
        }

        if (!isEmptyOrNull(logDetail.getUserAgent())) {
            writer.startNode(E_USERAGENT);
            writer.setValue(logDetail.getUserAgent());
            writer.endNode();
        }

        if (!isEmptyOrNull(logDetail.getSubject())){
            writer.startNode(E_SUBJECT);
            writer.setValue(logDetail.getSubject());
            writer.endNode();
        }

        if (!isEmptyOrNull(logDetail.getNodeIdentifier())){
            writer.startNode(E_NODEIDENTIFIER);
            writer.setValue(logDetail.getNodeIdentifier());
            writer.endNode();
        }


    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        final SeadLogDetail logDetail = new SeadLogDetail();

        while (reader.hasMoreChildren()) {
            reader.moveDown();


            final String value = reader.getValue();
            if (isEmptyOrNull(value)) {
                reader.moveUp();
                continue;
            }

            if (getElementName(reader).equals(E_IPADDRESS)) {
                logDetail.setIpAddress(value);
            }

            if (getElementName(reader).equals(E_USERAGENT)) {
                logDetail.setUserAgent(value);
            }

            if (getElementName(reader).equals(E_SUBJECT)) {
                logDetail.setSubject(value);
            }

            if (getElementName(reader).equals(E_NODEIDENTIFIER)) {
                logDetail.setNodeIdentifier(value);
            }

            reader.moveUp();
        }

        return logDetail;
    }

    @Override
    public boolean canConvert(Class type) {
        return SeadLogDetail.class == type;
    }
}

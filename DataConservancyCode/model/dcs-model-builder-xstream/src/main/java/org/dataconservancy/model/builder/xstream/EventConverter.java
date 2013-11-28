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

import java.util.Collection;
import java.util.Set;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 *
 */
public class EventConverter extends AbstractEntityConverter {

    public static final String E_EVENT = "Event";
    public static final String E_DATE = "eventDate";
    public static final String E_DETAIL = "eventDetail";
    public static final String E_OUTCOME = "eventOutcome";
    public static final String E_TYPE = "eventType";
    public static final String E_TARGET = "eventTarget";

    public static final String A_ID = "id";
    public static final String A_REF = "ref";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);
        final DcsEvent event = (DcsEvent)source;

        final String id = event.getId();
        if (!isEmptyOrNull(id)) {
            writer.addAttribute(A_ID, id);
        }

        final String date = event.getDate();
        final String outcome = event.getOutcome();
        final String detail = event.getDetail();
        final String type = event.getEventType();
        final Collection<DcsEntityReference> targets = event.getTargets();

        if (!isEmptyOrNull(type)) {
            writer.startNode(E_TYPE);
            writer.setValue(type);
            writer.endNode();
        }

        if (!isEmptyOrNull(date)) {
            writer.startNode(E_DATE);
            writer.setValue(date);
            writer.endNode();
        }

        if (targets != null) {
            for (DcsEntityReference r : targets) {
                writer.startNode(E_TARGET);
                writer.addAttribute(A_REF, r.getRef());
                writer.endNode();
            }
        }

        if (!isEmptyOrNull(detail)) {
            writer.startNode(E_DETAIL);
            writer.setValue(detail);
            writer.endNode();
        }

        if (!isEmptyOrNull(outcome)) {
            writer.startNode(E_OUTCOME);
            writer.setValue(outcome);
            writer.endNode();
        }        
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final DcsEvent event = new DcsEvent();

        final String id = reader.getAttribute(A_ID);
        if (!isEmptyOrNull(id)) {
            event.setId(id);
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            final String name = getElementName(reader);//reader.getNodeName();

            if (name.equals(E_TARGET)) {
                final String ref = reader.getAttribute(A_REF);
                if (!isEmptyOrNull(ref)) {
                    event.addTargets(new DcsEntityReference(ref));
                }
                reader.moveUp();
                continue;
            }

            final String value = reader.getValue();
            
            if (isEmptyOrNull(value)) {
                reader.moveUp();
                continue;
            }

            if (name.equals(E_DATE)) {
                event.setDate(value);
            }

            if (name.equals(E_DETAIL)) {
                event.setDetail(value);
            }

            if (name.equals(E_OUTCOME)) {
                event.setOutcome(value);
            }

            if (name.equals(E_TYPE)) {
                event.setEventType(value);
            }

            reader.moveUp();
        }

        return event;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsEvent.class == type;
    }
}

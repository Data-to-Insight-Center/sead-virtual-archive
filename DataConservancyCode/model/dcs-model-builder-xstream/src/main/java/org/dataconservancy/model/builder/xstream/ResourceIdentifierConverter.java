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

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Converts the xml representation of a {@link DcsResourceIdentifier} to the Java object model and back.
 */
public class ResourceIdentifierConverter extends AbstractEntityConverter {

    /**
     * The Resource Identifier element name
     */
    // Set the constant in your own converter so you can reuses this converter with your own ID name.

    /**
     * The authority ID element name
     */
    public static final String E_AUTHORITYID = "authorityId";
    /**
     * The type ID element name
     */
    public static final String E_TYPEID = "typeId";
    /**
     * The ID value element name
     */
    public static final String E_IDVALUE = "idValue";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        final DcsResourceIdentifier rid = (DcsResourceIdentifier) source;

        if (!isEmptyOrNull(rid.getAuthorityId())) {
            writer.startNode(E_AUTHORITYID);
            writer.setValue(rid.getAuthorityId());
            writer.endNode();
        }

        if (!isEmptyOrNull(rid.getTypeId())) {
            writer.startNode(E_TYPEID);
            writer.setValue(rid.getTypeId());
            writer.endNode();
        }

        if (!isEmptyOrNull(rid.getIdValue())) {
            writer.startNode(E_IDVALUE);
            writer.setValue(rid.getIdValue());
            writer.endNode();
        }

        // This would be better as elements but due to a problem processing them this converter was changed to
        // using elements. DWD
        //if (!isEmptyOrNull(rid.getAuthorityId())) {
        //    writer.addAttribute(A_AUTHORITYID, rid.getAuthorityId());
        //}
        //
        //if (!isEmptyOrNull(rid.getTypeId())) {
        //    writer.addAttribute(A_TYPEID, rid.getTypeId());
        //}
        //
        //if (!isEmptyOrNull(rid.getIdValue())) {
        //    writer.addAttribute(A_IDVALUE, rid.getIdValue());
        //}

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        final DcsResourceIdentifier rid = new DcsResourceIdentifier();

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String name = getElementName(reader);

            if (name.equals(E_AUTHORITYID)) {
                final String authorityId = (String) reader.getValue();
                if (authorityId != null) {
                    rid.setAuthorityId(authorityId);
                }
            }

            if (name.equals(E_TYPEID)) {
                final String typeId = (String) reader.getValue();
                if (typeId != null) {
                    rid.setTypeId(typeId);
                }
            }

            if (name.equals(E_IDVALUE)) {
                final String idValue = (String) reader.getValue();
                if (idValue != null) {
                    rid.setIdValue(idValue);
                }
            }

            reader.moveUp();

        }

        // This would be better as elements but due to a problem processing them this converter was changed to
        // using elements. DWD
        //final String authorityId = reader.getAttribute(A_AUTHORITYID);
        //final String typeId = reader.getAttribute(A_TYPEID);
        //final String idValue = reader.getAttribute(A_IDVALUE);
        //
        //if (!isEmptyOrNull(authorityId)) {
        //    rid.setAuthorityId(authorityId);
        //}
        //
        //if (!isEmptyOrNull(typeId)) {
        //    rid.setTypeId(typeId);
        //}
        //
        //if (!isEmptyOrNull(idValue)) {
        //    rid.setIdValue(idValue);
        //}

        return rid;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsResourceIdentifier.class == type;
    }

}

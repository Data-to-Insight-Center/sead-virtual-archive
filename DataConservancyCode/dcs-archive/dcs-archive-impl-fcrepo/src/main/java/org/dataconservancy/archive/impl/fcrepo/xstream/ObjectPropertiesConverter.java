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
package org.dataconservancy.archive.impl.fcrepo.xstream;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.archive.impl.fcrepo.dto.ObjectProperties;

/**
 * XStream converter for the Fedora object properties.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class ObjectPropertiesConverter
        extends AbstractPackageConverter {

    public static final String STATE =
            "info:fedora/fedora-system:def/model#state";

    public static final String LABEL =
            "info:fedora/fedora-system:def/model#label";

    public static final String OWNERID =
            "info:fedora/fedora-system:def/model#ownerId";

    public static final String CREATEDDATE =
            "info:fedora/fedora-system:def/model#createdDate";

    public static final String LASTMODIFIEDDATE =
            "info:fedora/fedora-system:def/view#lastModifiedDate";

    public static final String ACTIVE = "Active";

    public static final String INACTIVE = "Inactive";

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        ObjectProperties op = (ObjectProperties) source;
        writer.startNode(op.getClass().getName());

        HashMap<String, String> pmap = op.getPropertyMap();
        for (Map.Entry<String, String> e : pmap.entrySet()) {

            // The required attributes for each object property are
            // the NAME and VALUE.  We are not using a separate converter
            // for the properties.

            writer.startNode("foxml:property");
            writer.addAttribute("NAME", e.getKey());
            writer.addAttribute("VALUE", e.getValue());
            writer.endNode();

        }

        writer.endNode();

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        ObjectProperties objectProperties = new ObjectProperties();

        // Process each of the properties without using a separate converter.
        while (reader.hasMoreChildren()) {

            reader.moveDown();
            String name = reader.getAttribute("NAME");
            String value = reader.getAttribute("VALUE");
            objectProperties.getPropertyMap().put(name, value);
            reader.moveUp();

        }

        return objectProperties;
    }

    @Override
    public boolean canConvert(Class type) {
        return ObjectProperties.class == type;
    }

}

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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.archive.impl.fcrepo.dto.ContentLocation;

/**
 * XStream converter for Fedora Content Location elements.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class ContentLocationConverter
        extends AbstractPackageConverter {
    
    public static final String LOCATION_TYPE_INTERNAL = "INTERNAL_ID";
    public static final String LOCATION_TYPE_URL = "URL";
    
    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        ContentLocation cl = (ContentLocation) source;
        writer.startNode(cl.getClass().getName());

        if ((cl.getLocationURL() != null) && (cl.getType() != null)) {

            // The attributes for each Content Location are the REF,
            // and TYPE.
            writer.addAttribute("REF", cl.getLocationURL());
            writer.addAttribute("TYPE", cl.getType());

        } else {
            throw new RuntimeException("Marshalling failed");
        }

        writer.endNode();

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        // Create a  new content location object from the XML.
        ContentLocation cl = new ContentLocation();
        cl.setLocationURL(reader.getAttribute("REF"));
        cl.setType(reader.getAttribute("TYPE"));

        return cl;
    }

    @Override
    public boolean canConvert(Class type) {
        return ContentLocation.class == type;
    }

}

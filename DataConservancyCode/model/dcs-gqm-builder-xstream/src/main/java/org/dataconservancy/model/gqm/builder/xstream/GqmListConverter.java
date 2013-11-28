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
package org.dataconservancy.model.gqm.builder.xstream;

import java.util.List;

import javax.xml.XMLConstants;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.GQMList;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;

public class GqmListConverter extends AbstractEntityConverter {
    final static String E_GQMLIST = "GqmList";
    final static String E_GQMS = "Gqms";
  
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context){
        super.marshal(source, writer, context);
        
        final GQMList gqmList = (GQMList) source;
        final List<GQM> gqms = gqmList.getGQMs();
                
        writer.addAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        writer.addAttribute("xsi:schemaLocation", "http://dataconservancy.org/schemas/gqm/1.0");
        
        if(!gqms.isEmpty()){
            writer.startNode(E_GQMS);
            for(GQM gqm : gqms) {
                writer.startNode(GqmConverter.E_GQM);
                context.convertAnother(gqm);
                writer.endNode();
            }
            writer.endNode();
        }
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        GQMList gqmList = new GQMList();
        
        while(reader.hasMoreChildren()){
            reader.moveDown();
            
            final String elementName = getElementName(reader);          
            
            if( elementName.equals(E_GQMS) ) {
                while( reader.hasMoreChildren() ) {
                    reader.moveDown();
                    
                    if( getElementName(reader).equals(GqmConverter.E_GQM) ){
                        final GQM gqm = (GQM) context.convertAnother(gqmList, GQM.class);
                        gqmList.getGQMs().add(gqm);
                    }
                    reader.moveUp();
                }               
            }
            
            
            reader.moveUp();
        }
        return gqmList;
    }

    @Override
    public boolean canConvert(Class type) {
        return GQMList.class == type;
    }
}

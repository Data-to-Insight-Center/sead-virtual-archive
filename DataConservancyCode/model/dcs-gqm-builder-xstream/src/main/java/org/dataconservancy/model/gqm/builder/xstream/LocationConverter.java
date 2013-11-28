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

import java.net.URI;
import java.net.URISyntaxException;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

public class LocationConverter extends AbstractEntityConverter
{
    /**
     * The Location element name.
     */
    public final static String E_LOCATION = "Location";
    /**
     * The SRID attribute name. 
     */
    public final static String E_SRID = "srid";
  
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context){
        super.marshal(source, writer, context);
        
        final Location location = (Location) source;
        
        if( location.getGeometry() != null){
            writer.startNode(GeometryConverter.E_GEOMETRY);
            context.convertAnother(location.getGeometry());
            writer.endNode();
        }
        
        if( !isEmptyOrNull(location.getSrid().toString())){
            writer.startNode(E_SRID);
            writer.setValue(location.getSrid().toString());
            writer.endNode();
        }
            
    }
    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        Geometry geometry = null;
        URI srid = null; 
        
        while(reader.hasMoreChildren()){
            reader.moveDown();
            
            final String name = getElementName(reader);
            
            if( name.equals(GeometryConverter.E_GEOMETRY)) {
                geometry = (Geometry) context.convertAnother(geometry, Geometry.class);
            }
            
            if( name.equals(E_SRID)) {
                try {
                    srid = new URI(reader.getValue());
                } catch (URISyntaxException e) {
                    
                }
            }
            
            reader.moveUp();
        }
        
        Location location = new Location(geometry, srid);
        return location;
    }

    @Override
    public boolean canConvert(Class type) {
        return Location.class == type;
    }
    
}

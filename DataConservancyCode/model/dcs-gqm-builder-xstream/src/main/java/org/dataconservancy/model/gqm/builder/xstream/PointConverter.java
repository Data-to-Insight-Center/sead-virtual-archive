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



import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

public class PointConverter extends AbstractEntityConverter
{
    /**
     * The Point element name.
     */
    public static final String E_POINT = "point";
    /**
     * The coordinate element name.
     */
    public static final String E_COORDINATE = "coordinate";
    
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context){
        super.marshal(source, writer, context);
        
        final Point point = (Point) source;
        
        writer.startNode(E_COORDINATE);
        writer.setValue(point.toString());
        writer.endNode();
    }
    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        
        Point point = new Point(null);
        
        while(reader.hasMoreChildren()) {
            reader.moveDown();
            
            final String name = getElementName(reader);
            if(name.equals(E_COORDINATE)){
                final String value = reader.getValue();
                if( !isEmptyOrNull(value)){
                    point.setCoordinates(point.fromString(value));
                }
            }
            reader.moveUp();
        }        
        return point;
    }

    @Override
    public boolean canConvert(Class type) {
        return Point.class == type;
    }
    
}

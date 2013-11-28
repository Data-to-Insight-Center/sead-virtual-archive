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

import java.util.ArrayList;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

public class GeometryConverter extends AbstractEntityConverter
{
    /**
     * The Geometry element name.
     */
    public final static String E_GEOMETRY = "Geometry";
    /**
     * The type element name.
     */
    public final static String E_TYPE = "type";
    
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context){
        super.marshal(source, writer, context);
        
        final Geometry geometry = (Geometry) source;
        
        if( !isEmptyOrNull( geometry.getType().name() ) ){
            writer.startNode(E_TYPE);
            writer.setValue(geometry.getType().name());
            writer.endNode();
        }
        
        for( Point p : geometry.getPoints() ){
            writer.startNode(PointConverter.E_POINT);
            context.convertAnother(p);
            writer.endNode();
        }
        
    }
    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        String type = "";
        ArrayList<Point> points = new ArrayList<Point>();
        Geometry geometry = new Geometry(null, new Point(0));
       
        while(reader.hasMoreChildren()){
            reader.moveDown();
            
            final String name = getElementName(reader);
            if(name.equals(E_TYPE)){
                type = reader.getValue();
            }
            
            if(name.equals(PointConverter.E_POINT)){
                final Point point = (Point) context.convertAnother(geometry, Point.class);
                points.add(point);
            }
            
            reader.moveUp();
        }
         
        org.dataconservancy.model.gqm.Geometry.Type typeVal = org.dataconservancy.model.gqm.Geometry.Type.valueOf(type);
        geometry.setType(typeVal);
        
        geometry.setPoints(convertToArray(points));
        return geometry;
    }
    
    private Point[] convertToArray(ArrayList<Point> points){
        Point[] pointsArray = new Point[points.size()];
        
        for(int i = 0; i < points.size(); i++){
            pointsArray[i] = points.get(i);
        }
        
        return pointsArray;
    }

    @Override
    public boolean canConvert(Class type) {
        return (type == Geometry.class);
    }
    
}

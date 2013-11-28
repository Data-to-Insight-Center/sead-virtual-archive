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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;


public class GqmConverter extends AbstractEntityConverter {
    final static String E_GQM = "GQM";
    final static String E_RELATIONS = "Relations";
    final static String E_LOCATIONS = "Locations";
    final static String E_DATETIMEINTERVALS = "DateTimeTntervals";
    final static String E_ENTITYID = "Entityid";
  
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context){
        super.marshal(source, writer, context);
        
        final GQM gqm = (GQM) source;
        final List<Relation> relations = gqm.getRelations();
        final List<Location> locations = gqm.getLocations();
        final List<DateTimeInterval> intervals = gqm.getIntervals();
        
        if( !isEmptyOrNull(gqm.getEntityId())){
            writer.startNode(E_ENTITYID);
            writer.setValue(gqm.getEntityId());
            writer.endNode();
        }
       
        if(!relations.isEmpty()){
            writer.startNode(E_RELATIONS);
            for(Relation r : relations) {
                writer.startNode(RelationConverter.E_RELATION);
                context.convertAnother(r);
                writer.endNode();
            }
            writer.endNode();
        }
        
        if(!locations.isEmpty()){
            writer.startNode(E_LOCATIONS);
            for(Location l : locations){
                writer.startNode(LocationConverter.E_LOCATION);
                context.convertAnother(l);
                writer.endNode();
            }
            writer.endNode();
        }
        
        if(!intervals.isEmpty()){
            writer.startNode(E_DATETIMEINTERVALS);
            for(DateTimeInterval dti : intervals){
                writer.startNode(DateTimeIntervalConverter.E_DATETIMEINTERVAL);
                context.convertAnother(dti);
                writer.endNode();
            }
            writer.endNode();
        }
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        GQM gqm = new GQM("");
        
        while(reader.hasMoreChildren()){
            reader.moveDown();
            
            final String elementName = getElementName(reader);
            
            if( elementName.equals(E_ENTITYID) ){
                gqm.setEntityId(reader.getValue());
            }
            
            if( elementName.equals(E_RELATIONS) ) {
                while( reader.hasMoreChildren() ) {
                    reader.moveDown();
                    
                    if( getElementName(reader).equals(RelationConverter.E_RELATION) ){
                        final Relation r = (Relation) context.convertAnother(gqm, Relation.class);
                        gqm.getRelations().add(r);
                    }
                    reader.moveUp();
                }               
            }
            
            if( elementName.equals(E_LOCATIONS) ) {
               while( reader.hasMoreChildren() ) {
                    reader.moveDown();
                    
                    if( getElementName(reader).equals(LocationConverter.E_LOCATION) ){
                        final Location l = (Location) context.convertAnother(gqm, Location.class);
                        gqm.getLocations().add(l);
                    }
                    reader.moveUp();
                }
            }
            
            if( elementName.equals(E_DATETIMEINTERVALS) ) {
               while( reader.hasMoreChildren() ){
                    reader.moveDown();
                    
                    if( getElementName(reader).equals(DateTimeIntervalConverter.E_DATETIMEINTERVAL) ){
                        final DateTimeInterval dti = (DateTimeInterval) context.convertAnother(gqm, DateTimeInterval.class);
                        gqm.getIntervals().add(dti);
                    }
                    reader.moveUp();
                }
            }
            reader.moveUp();
        }
        return gqm;
    }

    @Override
    public boolean canConvert(Class type) {
        return GQM.class == type;
    }
    
}

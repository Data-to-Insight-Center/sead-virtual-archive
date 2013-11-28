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
package org.dataconservancy.transform.dryvalleys;

import java.net.URI;
import java.util.Iterator;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.index.gqmpsql.SpatialReferenceSystem;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;

public class FieldPhotoMapping implements Mapping<String, Dcp, String, GQM> {
    private final static String DRYVALLEYSROOT = "McMurdoDryValleys";
    private final static String FIELDPHOTOSELEMENT = "FieldPhotos";
    
    //List of elements to extract from the Metadata
    private final static String PHOTOGRAPHER_LONGITUDE = "photographerLongitude";
    private final static String PHOTOGRAPHER_LATITUDE = "photographerLatitude";
    private final static String FIELD_SEASON = "fieldSeason";
    private final static String DESCRIPTION = "description";
        
    @Override
    public void map(String key, Dcp dcp, Output<String, GQM> output) {
        DryValleyXMLParser parser = null;
       
        boolean foundFieldPhoto = false;
        
        if( dcp.getDeliverableUnits().size() >= 1) {
            for( DcsDeliverableUnit du : dcp.getDeliverableUnits()){
                if( du.getMetadata().size() > 0 ){
                    final Iterator<DcsMetadata> itr = du.getMetadata().iterator();
                    while (itr.hasNext()){
                        parser = new DryValleyXMLParser(itr.next());
                        if( parser.getRootNodeName().equalsIgnoreCase(DRYVALLEYSROOT) && 
                                parser.hasElement(FIELDPHOTOSELEMENT) ){
                            foundFieldPhoto = true;
                            break;
                        }                        
                    }                    
                }
                if( !foundFieldPhoto && du.getMetadataRef().size() > 0){
                    while( du.getMetadataRef().iterator().hasNext()){
                        parser = new DryValleyXMLParser(du.getMetadataRef().iterator().next());
                        if( parser.getRootNodeName().equalsIgnoreCase(DRYVALLEYSROOT) && 
                                parser.hasElement(FIELDPHOTOSELEMENT) ){
                            foundFieldPhoto = true;
                            break;
                        }                        
                    }     
                }
                if( foundFieldPhoto ) {
                    break;
                }
            }
        }
        
        if( foundFieldPhoto && parser != null && parser.isValid()) {
        
            GQM gqm = new GQM(key);
            
            if( parser.hasElement(PHOTOGRAPHER_LONGITUDE) && parser.hasElement(PHOTOGRAPHER_LATITUDE) ) {
                double fieldPhotoLat = Double.parseDouble( parser.getValue(PHOTOGRAPHER_LATITUDE).get(0) );
                double fieldPhotoLon = Double.parseDouble( parser.getValue(PHOTOGRAPHER_LONGITUDE).get(0) );
                
                Point point = new Point(fieldPhotoLat, fieldPhotoLon);
                Geometry geom = new Geometry(Geometry.Type.POINT, point);
                
                URI srid = SpatialReferenceSystem.forEPSG(4326); 
                Location location = new Location(geom, srid);
                gqm.getLocations().add(location);
            }
            
            if( parser.hasElement(FIELD_SEASON) ){
                int fieldSeason = Integer.parseInt( parser.getValue(FIELD_SEASON).get(0) );
                DateTimeInterval dti = new DateTimeInterval(fieldSeason, fieldSeason);
                gqm.getIntervals().add(dti);                
            }
            
            if( parser.hasElement(DESCRIPTION) ){
                String description = parser.getValue(DESCRIPTION).get(0);          
                Relation relation = new Relation(URI.create("dc:description"), description);
                gqm.getRelations().add(relation);                
            }            
            output.write(key, gqm);
        }
    }    
}

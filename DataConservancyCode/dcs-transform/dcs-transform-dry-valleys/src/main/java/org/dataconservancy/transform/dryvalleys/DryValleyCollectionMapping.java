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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.index.gqmpsql.SpatialReferenceSystem;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;

public class DryValleyCollectionMapping implements Mapping<String, Dcp, String, GQM> {
    private final static String COLLECTION_TITLE = "McMurdoDryValleys";
    private final static double INVALID_NUM = -99999;
    
    //Elements to extract from the metadata
    private static final String WEST_BOUND_LONGITUDE = "westBoundLongitude";
    private static final String EAST_BOUND_LONGITUDE = "eastBoundLongitude";
    private static final String NORTH_BOUND_LATITUDE = "northBoundLatitude";
    private static final String SOUTH_BOUND_LATITUDE = "southBoundLatitude";
    private static final String BEGIN_POSITION = "beginPosition";
    private static final String END_POSITION = "endPosition";
    private static final String ABSTRACT = "abstract";
    
    @Override
    public void map(String key, Dcp dcp, Output<String, GQM> output) {
        DryValleyXMLParser parser = null;
       
        if( dcp.getCollections().size() >= 1) {
            for( DcsCollection c : dcp.getCollections()){
                if( c.getTitle().equalsIgnoreCase(COLLECTION_TITLE)){
                    if( c.getMetadata().size() > 0 ){
                        if( c.getMetadata().iterator().hasNext() ){
                            parser = new DryValleyXMLParser(c.getMetadata().iterator().next());
                        }
                    } else if( c.getMetadataRef().size() > 0 ){
                        parser = new DryValleyXMLParser(c.getMetadataRef().iterator().next());
                    } 
                    break;                    
                }
            }
        }
        
        if( parser != null && parser.isValid()) {
        
            GQM gqm = new GQM(key);
            
            double westLongitude = INVALID_NUM;
            double eastLongitude = INVALID_NUM;
            double southLatitude = INVALID_NUM;
            double northLatitude = INVALID_NUM;
            
            if( parser.hasElement(WEST_BOUND_LONGITUDE) && parser.hasElement(EAST_BOUND_LONGITUDE)
                    && parser.hasElement(SOUTH_BOUND_LATITUDE) && parser.hasElement(NORTH_BOUND_LATITUDE) ) {
                
                String numberString = parser.getChildValue(WEST_BOUND_LONGITUDE).get(0);
                if( numberString != null && !numberString.isEmpty()) {
                    westLongitude = Double.parseDouble( numberString );
                }
                
                numberString = parser.getChildValue(EAST_BOUND_LONGITUDE).get(0);
                if( numberString != null && !numberString.isEmpty()) {
                    eastLongitude = Double.parseDouble( numberString );
                }
                
                numberString = parser.getChildValue(SOUTH_BOUND_LATITUDE).get(0);
                if( numberString != null && !numberString.isEmpty()) {
                    southLatitude = Double.parseDouble( numberString );
                }
                
                numberString = parser.getChildValue(NORTH_BOUND_LATITUDE).get(0);
                if( numberString != null && !numberString.isEmpty()) {
                    northLatitude = Double.parseDouble( numberString );
                }
               
                if( westLongitude != INVALID_NUM && eastLongitude != INVALID_NUM 
                        && southLatitude != INVALID_NUM && northLatitude != INVALID_NUM ){            
                    Point bottomLeft = new Point(southLatitude, westLongitude);
                    Point bottomRight = new Point(southLatitude, eastLongitude);
                    Point topRight = new Point(northLatitude, eastLongitude);
                    Point topLeft = new Point(northLatitude, westLongitude);
                    
                    Geometry geom = new Geometry(Geometry.Type.POLYGON, bottomLeft, bottomRight, topRight, topLeft);
                    
                    URI srid = SpatialReferenceSystem.forEPSG(4326); 
                    Location location = new Location(geom, srid);
                    gqm.getLocations().add(location);
                }
            }
            
            if( parser.hasElement(BEGIN_POSITION) && parser.hasElement(END_POSITION) ) {
                
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                String startDateString = parser.getValue(BEGIN_POSITION).get(0);
                String endDateString = parser.getValue(END_POSITION).get(0);
                
                if( startDateString != null && !startDateString.isEmpty() 
                        && endDateString != null && !endDateString.isEmpty()){
                    Date startDate = null;
                    Date endDate = null;
                    try {
                        startDate = formatter.parse(startDateString);
                        endDate = formatter.parse(endDateString);
                    } catch (ParseException e) {
                        System.out.println("parse exception: ");
                        e.printStackTrace();
                    }
                    if( startDate != null && endDate != null ) {
                        DateTimeInterval dti = new DateTimeInterval(startDate.getTime(), endDate.getTime());
                        gqm.getIntervals().add(dti);
                    }
                }                
            }
            
            if( parser.hasElement(ABSTRACT) ){
                String studyAbstract = parser.getChildValue(ABSTRACT).get(0);          
                Relation relation = new Relation(URI.create("gmd:abstract"), studyAbstract);
                gqm.getRelations().add(relation);
            }
            
            output.write(key, gqm);
        }
    }    
}

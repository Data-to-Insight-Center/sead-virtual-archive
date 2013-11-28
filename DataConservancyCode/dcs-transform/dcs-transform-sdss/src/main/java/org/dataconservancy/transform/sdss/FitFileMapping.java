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
package org.dataconservancy.transform.sdss;

import java.net.URI;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.index.gqmpsql.SpatialReferenceSystem;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;

public class FitFileMapping implements Mapping<String, Dcp, String, GQM> {
    private final static String FITFILEROOT = "VOTABLE";
     
    //List of elements to extract from the Metadata
    private final static String IMAGE_TITLE = "Image Title";
    private final static String DECLINATION = "Declination";
    private final static String RIGHT_ASCENSION = "Right Ascension";
    private final static String OBSERVATION_DATE = "Mean date of observation";
    private final static String IMAGE_URL = "Image URL";
    
    @Override
    public void map(String key, Dcp dcp, Output<String, GQM> output) {
        SDSSXMLParser parser = null;

        boolean foundFitFile = false;
        
        if( dcp.getDeliverableUnits().size() >= 1) {
            for( DcsDeliverableUnit du : dcp.getDeliverableUnits()){
                while( du.getMetadata().iterator().hasNext()){
                    parser = new SDSSXMLParser(du.getMetadata().iterator().next());
                    if( parser.getRootNodeName().equalsIgnoreCase(FITFILEROOT) ){
                        foundFitFile = true;
                        break;
                    }                        
                }
                if( foundFitFile ) {
                    break;
                }
            }
        }
        
        if( foundFitFile && parser != null && parser.isValid()) {
        
            GQM gqm = new GQM(key);
            
            if( parser.hasElement(DECLINATION) && parser.hasElement(RIGHT_ASCENSION) ) {
                double declination = Double.parseDouble( parser.getValue(DECLINATION) );
                double rightAscension = Double.parseDouble( parser.getValue(RIGHT_ASCENSION) );
                
                Point point = new Point(declination, rightAscension);
                Geometry geom = new Geometry(Geometry.Type.POINT, point);
                
                URI srid = SpatialReferenceSystem.forEquatorialCoordinateSystem("J2000");
                Location location = new Location(geom, srid);
                gqm.getLocations().add(location);
            }
            
            if( parser.hasElement(OBSERVATION_DATE) ){
                double observationDate = Double.parseDouble( parser.getValue(OBSERVATION_DATE) );
                long dateInMillis = convertJulianDateToStandardDate(observationDate);
                DateTimeInterval dti = new DateTimeInterval(dateInMillis, dateInMillis);
                gqm.getIntervals().add(dti);                
            }
            
            if( parser.hasElement(IMAGE_TITLE) ){
                String title = parser.getValue(IMAGE_TITLE);          
                Relation relation = new Relation(URI.create("ImageTitle"), title);
                gqm.getRelations().add(relation);                
            }
            
            if( parser.hasElement(IMAGE_URL) ){
                String url = parser.getValue(IMAGE_URL);          
                Relation relation = new Relation(URI.create("ImageURL"), url);
                gqm.getRelations().add(relation);                
            }
            
            output.write(key, gqm);
        }
    }
      
    private long convertJulianDateToStandardDate(double julianDate){
        int l = (int) (julianDate + 68569);
        int n = ( 4 * l ) / 146097;
        l = l - ( 146097 * n + 3 ) / 4;
        int i = ( 4000 * ( l + 1 ) ) / 1461001;
        l = l - ( 1461 * i ) / 4 + 31;
        int j = ( 80 * l ) / 2447;
        int day = (int) (l - ( 2447 * j ) / 80);
        l = j / 11;
        int month = (int) (j + 2 - ( 12 * l ));
        int year = (int) (100 * ( n - 49 ) + i + l); 

        System.out.println(year + " " + month + " " + day);
        Calendar cal = new GregorianCalendar(year, month, day);
        System.out.println(cal.getTimeInMillis());

        return cal.getTimeInMillis();
    }
}

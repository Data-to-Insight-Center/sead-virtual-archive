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

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.transform.Output;

import junit.framework.Assert;

public class FieldPhotoMappingTest {
    
    private final static String FIELD_PHOTO_METADATA = 
        "<McMurdoDryValleys xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:fgdc=\"http://www.fgdc.gov/metadata/standards/\">\n" 
    +   "    <FieldPhotos>\n" 
    +   "       <FieldPhoto>\n" 
    +   "           <fileName>McDV_FieldPhoto_1993_00694.tif</fileName>\n" 
    +   "           <fieldSeason>1993</fieldSeason>\n" 
    +   "           <photographerLatitude>-77.754058</photographerLatitude>\n"
    +   "           <photographerLongitude>161.9574</photographerLongitude>\n" 
    +   "           <dc:title>McDV_FieldPhoto_1993_00694</dc:title>\n"  
    +   "           <dc:subject>Geology</dc:subject>\n" 
    +   "           <dc:subject>Petrology</dc:subject>\n" 
    +   "           <dc:subject>Igneous Petrology</dc:subject>\n"
    +   "           <dc:subject>Glaciology</dc:subject>\n"
    +   "           <dc:description>Plummet Glacier at northwest corner of Kukri Hills, looking southwest from helicopter above Taylor Glacier.</dc:description>\n"
    +   "           <dc:creator>Marsh, Bruce D.</dc:creator>\n"
    +   "           <dc:publisher>The Data Conservancy, Sheridan Libraries, The Johns Hopkins University</dc:publisher>\n"
    +   "           <dc:date>1993-01</dc:date>\n"
    +   "           <dc:format>image/tif</dc:format>\n"
    +   "           <dc:rights>\n"
    +   "               Copyright Bruce D. Marsh, licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported license. http://creativecommons.org/licenses/by-nc-sa/3.0/\n"
    +   "           </dc:rights>\n"
    +   "           <dc:identifier>McDV_FieldPhoto_1993_00694</dc:identifier>\n"
    +   "           <dc:coverage>Antarctica</dc:coverage>\n"
    +   "           <dc:coverage>McMurdo Dry Valleys</dc:coverage>\n"
    +   "           <USGS_ANTARCTIC_NAMES>\n"
    +   "               <ANTARCTICA_FEATURE_ID ID=\"8274\">\n"
    +   "                   <FEATURE_NAME>Kukri Hills</FEATURE_NAME>\n"
    +   "                   <FEATURE_CLASS>Range</FEATURE_CLASS>\n"
    +   "                   <DESCRIPTION>\n"
    +   "                       Prominent E-W trending range, about 25 mi long and over 2,000 m high, forming the divide between Ferrar Glacier on the S and Taylor Glacier and Taylor Valley on the N, in Victoria Land. Discovered by the British National Antarctic Expedition (BrNAE) (1901-04) and probably so named because its shape resembles that of Kukri, a Gurkha knife.\n"
    +   "                   </DESCRIPTION>\n"
    +   "                   <PRIMARY_LATITUDE_DEC>-77.7333333</PRIMARY_LATITUDE_DEC>\n"
    +   "                   <PRIMARY_LONGITUDE_DEC>162.7</PRIMARY_LONGITUDE_DEC>\n"
    +   "               </ANTARCTICA_FEATURE_ID>\n"
    +   "               <ANTARCTICA_FEATURE_ID ID=\"11891\">\n"
    +   "                   <FEATURE_NAME>Plummet Glacier</FEATURE_NAME>\n"
    +   "                   <FEATURE_CLASS>Glacier</FEATURE_CLASS>\n"
    +   "                   <DESCRIPTION>\n"
    +   "                       The westernmost glacier on the N side of Kukri Hills, flowing N to Taylor Glacier, in Victoria Land. The name is one of a group in the area associated with surveying applied in 1993 by New Zealand Geographic Board (NZGB). The name refers to a plummet, or plumb bob.\n"
    +   "                   </DESCRIPTION>\n"
    +   "                   <PRIMARY_LATITUDE_DEC>-77.7833333</PRIMARY_LATITUDE_DEC>\n"
    +   "                   <PRIMARY_LONGITUDE_DEC>161.9</PRIMARY_LONGITUDE_DEC>\n"
    +   "               </ANTARCTICA_FEATURE_ID>\n"
    +   "               <ANTARCTICA_FEATURE_ID ID=\"15024\">\n"
    +   "                   <FEATURE_NAME>Taylor Glacier</FEATURE_NAME>\n"
    +   "                   <FEATURE_CLASS>Glacier</FEATURE_CLASS>\n"
    +   "                   <DESCRIPTION>\n"
    +   "                       Glacier about 35 mi long, flowing from the plateau of Victoria Land into the W end of Taylor Valley, N of the Kukri Hills. Discovered by the British National Antarctic Expedition (BrNAE) (1901-04) and at that time thought to be a part of Ferrar Glacier. The Western Journey Party of the British Antarctic Expedition (BrAE) (1910-13) determined that the upper and lower portions of what was then known as Ferrar Glacier are apposed, i.e., joined in Siamese-twin fashion N of Knobhead. With this discovery Scott named the upper portion for Griffith Taylor, geologist and leader of the Western Journey Party.\n"
    +   "                   </DESCRIPTION>\n"
    +   "                   <PRIMARY_LATITUDE_DEC>-77.7333333</PRIMARY_LATITUDE_DEC>\n"
    +   "                   <PRIMARY_LONGITUDE_DEC>162.1666667</PRIMARY_LONGITUDE_DEC>\n"
    +   "              </ANTARCTICA_FEATURE_ID>\n"
    +   "           </USGS_ANTARCTIC_NAMES>\n"
    +   "       </FieldPhoto>\n"
    +   "   </FieldPhotos>\n"
    +   "</McMurdoDryValleys>";


    private static final String MORE_ARBITRARY_METADATA = "<foo/>";

    private final String ENTITY_ID = "entityID";
    private GQM gqm;
    private Dcp sip;
    
    private static class GQMTestOutput implements Output<String, GQM> {

        Map<String, GQM> results = new HashMap<String, GQM>();
        
        public void write(String key, GQM value) {
            results.put(key, value);
        }
        
        @Override
        public void close() {
            /* Does nothing */
        }

    }
    
    @Before
    public void setUp() {
        gqm = new GQM(ENTITY_ID);
        double fieldPhotoLat = -77.754058;
        double fieldPhotoLon = 161.9574;
        
        Point point = new Point(fieldPhotoLat, fieldPhotoLon);
        Geometry geom = new Geometry(Geometry.Type.POINT, point);
        
        URI srid = URI.create("http://spatialreference.org/ref/epsg/4326");
        Location location = new Location(geom, srid);
        gqm.getLocations().add(location);
        
        int fieldSeason = 1993;
        DateTimeInterval dti = new DateTimeInterval(fieldSeason, fieldSeason);
        gqm.getIntervals().add(dti);
        
        String description = "Plummet Glacier at northwest corner of Kukri Hills, looking southwest from helicopter above Taylor Glacier.";          
        Relation relation = new Relation(URI.create("dc:description"), description);
        gqm.getRelations().add(relation);
        
        sip = new Dcp();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        DcsMetadata md = new DcsMetadata();
        
        md.setMetadata(FIELD_PHOTO_METADATA);
        du.addMetadata(md);

        DcsMetadata md2 = new DcsMetadata();
        md2.setMetadata(MORE_ARBITRARY_METADATA);

        du.addMetadata(md2);

        sip.addDeliverableUnit(du);    
    }
    
    @Test
    public void testMapping(){
        FieldPhotoMapping mapping = new FieldPhotoMapping();
        
        GQM mappedGQM;
        GQMTestOutput output = new GQMTestOutput();
        
        mapping.map(ENTITY_ID, sip, output);
        
        mappedGQM = output.results.get(ENTITY_ID);
        
        Assert.assertEquals(gqm, mappedGQM);
        
    }   
}

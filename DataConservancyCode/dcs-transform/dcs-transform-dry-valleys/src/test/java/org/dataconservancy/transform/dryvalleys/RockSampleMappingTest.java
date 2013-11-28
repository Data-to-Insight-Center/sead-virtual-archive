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
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.transform.Output;

import junit.framework.Assert;

public class RockSampleMappingTest {
    private static final String XML = 
        "<McMurdoDryValleys>\n"
    +       "<rockSamples>\n"
    +           "<sample>\n"
    +               "<sampleNumber>A-160</sampleNumber>\n"
    +               "<sampleDate>1/15/1997</sampleDate>\n"
    +               "<altitudeFeet/>\n"
    +               "<correctedAltitudeFeet/>\n"
    +               "<aboveBase/>\n"
    +               "<metersAboveBase/>\n"
    +               "<comment>\n"
    +                   "sample of fine grained plagioclase rich - possibly annealed?\n"
    +               "</comment>\n"
    +               "<sampleProfile>Dais</sampleProfile>\n"
    +               "<rockUnit/>\n"
    +               "<rockClassOne>igneous</rockClassOne>\n"
    +               "<rockClassTwo>plutonic</rockClassTwo>\n"
    +               "<rockClassThree>mafic</rockClassThree>\n"
    +               "<rockClassFour>dolerite</rockClassFour>\n"
    +               "<imageFile>\n"
    +                   "<fileName>A-160_1_r.tif</fileName>\n"
    +                   "<fileSize>72345610</fileSize>\n"
    +                   "<dateTimeOriginal>2010:06:05 04:54:30</dateTimeOriginal>\n"
    +                   "<dateLastModified>2010:07:08 18:40:10</dateLastModified>\n"
    +                   "<photoType>Rock Sample</photoType>\n"
    +                   "<surfaceType>Rough</surfaceType>\n"
    +                   "<colorSpace>AdobeRGB (1998)</colorSpace>\n"
    +                   "<width>4256</width>\n"
    +                   "<height>2832</height>\n"
    +                   "<pixelSize>48</pixelSize>\n"
    +               "</imageFile>\n"
    +               "<imageFile>\n"
    +                   "<fileName>A-160_2_r.tif</fileName>\n"
    +                   "<fileSize>72344912</fileSize>\n"
    +                   "<dateTimeOriginal>2010:06:05 04:55:04</dateTimeOriginal>\n"
    +                   "<dateLastModified>2010:07:08 18:40:12</dateLastModified>\n"
    +                   "<photoType>Rock Sample</photoType>\n"
    +                   "<surfaceType>Rough</surfaceType>\n"
    +                   "<colorSpace>AdobeRGB (1998)</colorSpace>\n"
    +                   "<width>4256</width>\n"
    +                   "<height>2832</height>\n"
    +                   "<pixelSize>48</pixelSize>\n"
    +               "</imageFile>\n"
    +               "<imageFile>\n"
    +                   "<fileName>A-160_3_r.tif</fileName>\n"
    +                   "<fileSize>72344672</fileSize>\n"
    +                   "<dateTimeOriginal>2010:06:05 04:56:08</dateTimeOriginal>\n"
    +                   "<dateLastModified>2010:07:08 18:40:12</dateLastModified>\n"
    +                   "<photoType>Rock Sample</photoType>\n"
    +                   "<surfaceType>Rough</surfaceType>\n"
    +                   "<colorSpace>AdobeRGB (1998)</colorSpace>\n"
    +                   "<width>4256</width>\n"
    +                   "<height>2832</height>\n"
    +                   "<pixelSize>48</pixelSize>\n"
    +               "</imageFile>\n"   
    +           "</sample>\n"
    +       "</rockSamples>\n"
    +   "</McMurdoDryValleys>\n";
    
    private final String ENTITY_ID = "entityID";
    private final long DATE_IN_MILLISECONDS = 853304400000l;
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
                
        DateTimeInterval dti = new DateTimeInterval(DATE_IN_MILLISECONDS, DATE_IN_MILLISECONDS);
        gqm.getIntervals().add(dti);
        
        String profile = "Dais";          
        Relation relation = new Relation(URI.create("sampleProfile"), profile);
        gqm.getRelations().add(relation);
        
        String fileNameOne = "A-160_1_r.tif";
        Relation relationTwo = new Relation(URI.create("ImageFileName"), fileNameOne);
        gqm.getRelations().add(relationTwo);
        
        String fileNameTwo = "A-160_2_r.tif";
        Relation relationThree = new Relation(URI.create("ImageFileName"), fileNameTwo);
        gqm.getRelations().add(relationThree);
        
        String fileNameThree = "A-160_3_r.tif";
        Relation relationFour = new Relation(URI.create("ImageFileName"), fileNameThree);
        gqm.getRelations().add(relationFour);
        
        sip = new Dcp();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        DcsMetadata md = new DcsMetadata();
        
        md.setMetadata(XML);
        du.addMetadata(md);
        
        sip.addDeliverableUnit(du);    
    }
    
    @Test
    public void testMapping(){
        RockSampleMapping mapping = new RockSampleMapping();
        
        GQM mappedGQM;
        GQMTestOutput output = new GQMTestOutput();
        
        mapping.map(ENTITY_ID, sip, output);
        
        mappedGQM = output.results.get(ENTITY_ID);
        
        Assert.assertEquals(gqm, mappedGQM);
        
    }   
}

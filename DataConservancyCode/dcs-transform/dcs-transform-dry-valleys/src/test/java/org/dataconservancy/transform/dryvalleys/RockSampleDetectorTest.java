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

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsMetadata;

import junit.framework.Assert;

public class RockSampleDetectorTest {
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
    +               "<imageFile>\n"
    +                   "<fileName>A-160_4_r.tif</fileName>\n"
    +                   "<fileSize>72345478</fileSize>\n"
    +                   "<dateTimeOriginal>2010:06:05 04:56:16</dateTimeOriginal>\n"
    +                   "<dateLastModified>2010:07:08 18:40:26</dateLastModified>\n"
    +                   "<photoType>Rock Sample</photoType>\n"
    +                   "<surfaceType>Rough</surfaceType>\n"
    +                   "<colorSpace>AdobeRGB (1998)</colorSpace>\n"
    +                   "<width>4256</width>\n"
    +                   "<height>2832</height>\n"
    +                   "<pixelSize>48</pixelSize>\n"
    +               "</imageFile>\n"
    +               "<imageFile>\n"
    +                   "<fileName>A-160_5_r.tif</fileName>\n"
    +                   "<fileSize>72346298</fileSize>\n"
    +                   "<dateTimeOriginal>2010:06:05 04:58:09</dateTimeOriginal>\n"
    +                   "<dateLastModified>2010:07:08 18:40:34</dateLastModified>\n"
    +                   "<photoType>Rock Sample</photoType>\n"
    +                   "<surfaceType>Rough</surfaceType>\n"
    +                   "<colorSpace>AdobeRGB (1998)</colorSpace>\n"
    +                   "<width>4256</width>\n" 
    +                   "<height>2832</height>\n"
    +                   "<pixelSize>48</pixelSize>\n"
    +               "</imageFile>\n"
    +               "<imageFile>\n"
    +                   "<fileName>A-160_6_r.tif</fileName>\n"
    +                   "<fileSize>72345660</fileSize>\n"
    +                   "<dateTimeOriginal>2010:06:05 04:58:55</dateTimeOriginal>\n"
    +                   "<dateLastModified>2010:07:08 18:40:38</dateLastModified>\n"
    +                   "<photoType>Rock Sample</photoType>\n"
    +                   "<surfaceType>Rough</surfaceType>\n"
    +                   "<colorSpace>AdobeRGB (1998)</colorSpace>\n"
    +                   "<width>4256</width>\n"
    +                   "<height>2832</height>\n"
    +                   "<pixelSize>48</pixelSize>\n"
    +               "</imageFile>\n"
    +               "<imageFile>\n"
    +                   "<fileName>A-160_8_s.tif</fileName>\n"
    +                   "<fileSize>72342056</fileSize>\n"
    +                   "<dateTimeOriginal>2010:06:27 10:43:26</dateTimeOriginal>\n"
    +                   "<dateLastModified>2010:07:11 12:35:22</dateLastModified>\n"
    +                   "<photoType>Rock Sample</photoType>\n"
    +                   "<surfaceType/>\n"
    +                   "<colorSpace>AdobeRGB (1998)</colorSpace>\n"
    +                   "<width>4256</width>\n"
    +                   "<height>2832</height>\n"
    +                   "<pixelSize>48</pixelSize>\n"
    +               "</imageFile>\n"
    +           "</sample>\n"
    +       "</rockSamples>\n"
    +   "</McMurdoDryValleys>\n";
    
    private Dcp sip;
    private Dcp badSip;
    
    @Before
    public void setUp() {
        sip = new Dcp();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        DcsMetadata md = new DcsMetadata();
        
        md.setMetadata(XML);
        du.addMetadata(md);
        
        sip.addDeliverableUnit(du);
        
        badSip = new Dcp();
        DcsDeliverableUnit du2 = new DcsDeliverableUnit();
        DcsMetadata md2 = new DcsMetadata();
        
        md2.setMetadata("foo");
        du2.addMetadata(md2);
        
        badSip.addDeliverableUnit(du2);
    }
    
    @Test
    public void testProfileDetector(){
        RockSampleProfileDetector detector = new RockSampleProfileDetector();
        
        Assert.assertTrue(detector.hasFormat(sip));
        
        Assert.assertFalse(detector.hasFormat(badSip));
    }
}

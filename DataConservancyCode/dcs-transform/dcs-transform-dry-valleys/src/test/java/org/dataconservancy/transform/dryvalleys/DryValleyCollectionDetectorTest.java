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
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;

import junit.framework.Assert;

public class DryValleyCollectionDetectorTest {
 
    private Dcp sip;
    
    @Before
    public void setUp() {
        sip = new Dcp();
        DcsCollection coll = new DcsCollection();
        coll.setTitle("McMurdoDryValleys");
        
        DcsDeliverableUnit du = new DcsDeliverableUnit();
               
        sip.addCollection(coll);
        sip.addDeliverableUnit(du);      
    }
    
    @Test
    public void testProfileDetector(){
        DryValleyCollectionDetector detector = new DryValleyCollectionDetector();
        
        Assert.assertTrue(detector.hasFormat(sip));
    }
}

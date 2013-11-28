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

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.transform.profile.DcpProfileDetector;

public class RockSampleProfileDetector implements DcpProfileDetector {

    private final static String DRYVALLEYSROOT = "McMurdoDryValleys";
    private final static String ROCKSAMPLESELEMENT = "rockSamples";
    
    @Override
    public boolean hasFormat(Dcp dcp) {
        boolean isRockSample = false;
        if( dcp.getDeliverableUnits().size() >= 1) {
           for( DcsDeliverableUnit du : dcp.getDeliverableUnits()){
               if( du.getMetadata().iterator().hasNext()){
                   DryValleyXMLParser parser = new DryValleyXMLParser(du.getMetadata().iterator().next());
                   if( parser.getRootNodeName().equalsIgnoreCase(DRYVALLEYSROOT) && 
                           parser.hasElement(ROCKSAMPLESELEMENT) ) {
                       isRockSample = true;
                   }
               }
               if( !isRockSample && du.getMetadataRef().iterator().hasNext()){
                   DryValleyXMLParser parser = new DryValleyXMLParser(du.getMetadataRef().iterator().next());
                   if( parser.getRootNodeName().equalsIgnoreCase(DRYVALLEYSROOT) && 
                           parser.hasElement(ROCKSAMPLESELEMENT) ) {
                       isRockSample = true;
                   }
               }
               
               if( isRockSample ){
                   break;
               }
           }
        }
                        
        return isRockSample;
    }        
}

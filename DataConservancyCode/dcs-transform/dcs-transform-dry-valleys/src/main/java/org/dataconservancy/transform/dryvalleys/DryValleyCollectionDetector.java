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
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.transform.profile.DcpProfileDetector;

public class DryValleyCollectionDetector implements DcpProfileDetector {

    private final static String COLLECTION_NAME = "McMurdoDryValleys";
   
    
    @Override
    public boolean hasFormat(Dcp dcp) {
        boolean isDryValleyCollection = false;
        if( dcp.getCollections().size() >= 1) {
           for( DcsCollection collection : dcp.getCollections()){
               if( collection.getTitle().equalsIgnoreCase(COLLECTION_NAME)){
                   isDryValleyCollection = true;
                   break;
               } 
           }
        }
                        
        return isDryValleyCollection;
    }    
    
}

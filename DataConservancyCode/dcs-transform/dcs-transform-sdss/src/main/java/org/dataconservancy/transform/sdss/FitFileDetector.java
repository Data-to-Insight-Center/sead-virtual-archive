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

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.transform.profile.DcpProfileDetector;

public class FitFileDetector implements DcpProfileDetector {

    private static final String CREATOR = "SDSS";
    private static final String ROOT_NAME = "VOTABLE";
    @Override
    public boolean hasFormat(Dcp dcp) {
        boolean isFitFile = false;
        
        if( dcp.getDeliverableUnits().size() > 0 ){
            DcsDeliverableUnit fitDU = null;
            for( DcsDeliverableUnit du : dcp.getDeliverableUnits()){
                if( du.getCreators().contains(CREATOR)){
                    fitDU = du;
                    break;
                }
            }
            
            if( fitDU != null && fitDU.getMetadata().size() > 0){
                SDSSXMLParser parser = new SDSSXMLParser(fitDU.getMetadata().iterator().next());
                if( parser.getRootNodeName().equalsIgnoreCase(ROOT_NAME)){
                    isFitFile = true;
                }
            }
        }
        
        return isFitFile;
    }
    
}

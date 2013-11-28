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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;

public class RockSampleMapping implements Mapping<String, Dcp, String, GQM> {
    private final static String DRYVALLEYSROOT = "McMurdoDryValleys";
    private final static String ROCKSAMPLEELEMENT = "rockSamples";
    
    //Elements to extract from metadata
    private final static String SAMPLE_DATE = "sampleDate";
    private final static String SAMPLE_PROFILE = "sampleProfile";
    private final static String FILE_NAME = "fileName";
    
    @Override
    public void map(String key, Dcp dcp, Output<String, GQM> output) {
        DryValleyXMLParser parser = null;
       
        boolean foundRockSample = false;
        
        if( dcp.getDeliverableUnits().size() >= 1) {
            for( DcsDeliverableUnit du : dcp.getDeliverableUnits()){
                if( du.getMetadata().size() > 0 ){
                    Collection<DcsMetadata> duMetadata = du.getMetadata();
                    for( DcsMetadata md : duMetadata ){
                        parser = new DryValleyXMLParser(md);
                        if( parser.getRootNodeName().equalsIgnoreCase(DRYVALLEYSROOT) && 
                                parser.hasElement(ROCKSAMPLEELEMENT) ){
                            foundRockSample = true;
                            break;
                        }                        
                    }                    
                }
                if( !foundRockSample && du.getMetadataRef().size() > 0 ){
                    Collection<DcsMetadataRef> duMetadataRef = du.getMetadataRef();
                    for( DcsMetadataRef mdRef : duMetadataRef){
                        parser = new DryValleyXMLParser(mdRef);
                        if( parser.getRootNodeName().equalsIgnoreCase(DRYVALLEYSROOT) && 
                                parser.hasElement(ROCKSAMPLEELEMENT) ){
                            foundRockSample = true;
                            break;
                        }       
                    }
                }
                if( foundRockSample ) {
                    break;
                }
            }
        }
        
        if( foundRockSample && parser != null && parser.isValid()) {
        
            GQM gqm = new GQM(key);
            
            //TODO: Find out if there is lat/lon information for the rock samples. 
            if( parser.hasElement(SAMPLE_DATE) ){
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                String sampleDateValue = parser.getValue(SAMPLE_DATE).get(0);
                if( sampleDateValue != null && !sampleDateValue.isEmpty() ){
                    Date sampleDate = null;
                    try {
                        sampleDate = formatter.parse(sampleDateValue);
                    } catch (ParseException e) {
                       System.out.println("parse exception: ");
                       e.printStackTrace();
                    }
                    if( sampleDate != null) {
                        DateTimeInterval dti = new DateTimeInterval(sampleDate.getTime(), sampleDate.getTime());
                        gqm.getIntervals().add(dti);
                    }
                }
            }
            
            if( parser.hasElement(SAMPLE_PROFILE) ){
                String sampleProfile = parser.getValue(SAMPLE_PROFILE).get(0);          
                if( sampleProfile != null && !sampleProfile.isEmpty() ){
                    Relation relation = new Relation(URI.create(SAMPLE_PROFILE), sampleProfile);
                    gqm.getRelations().add(relation);
                }
            }
            
            if( parser.hasElement(FILE_NAME) ){
                List<String> imageFiles = parser.getValue(FILE_NAME);
                for( String fileName : imageFiles ){
                    if( fileName != null && !fileName.isEmpty()){
                        Relation relation = new Relation(URI.create("ImageFileName"), fileName);
                        gqm.getRelations().add(relation);
                    }
                }
            }
            output.write(key, gqm);
        }
    }    
}

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
package org.dataconservancy.dcs.integration.viruschecker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.services.IngestService;
import org.dataconservancy.dcs.ingest.services.IngestServiceException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import junit.framework.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/org/dataconservancy/config/test-applicationContext.xml"})
public class VirusCheckerIT {
  
    private static final String DIRTY_FILE = "/eicar.com.txt";
    private static final String CLEAN_FILE = "/cleanTest.txt";
   
    private Dcp cleanSip;
    private Dcp dirtySip;
    private Dcp multipleFileSip;
    
    @Autowired
    private IngestFramework ingestFramework;
    
    @Autowired
    private IngestService virusChecker;
    
    @Before
    public void setup(){
        dirtySip = new Dcp();
        cleanSip = new Dcp();
        multipleFileSip = new Dcp();
        buildSips();
    }
    
    @Test
    public void cleanFileTest() throws IngestServiceException{
        String id = ingestFramework.getSipStager().addSIP(cleanSip);

        virusChecker.execute(id);

        Dcp newSip = ingestFramework.getSipStager().getSIP(id);

        Assert.assertNotNull(newSip);
        
        int virusCount = numberVirusesFound(newSip);
        
        Assert.assertEquals(0, virusCount);      
    }
    
    @Test
    public void dirtyFileTest() throws IngestServiceException{
        String id = ingestFramework.getSipStager().addSIP(dirtySip);

        virusChecker.execute(id);

        Dcp newSip = ingestFramework.getSipStager().getSIP(id);

        Assert.assertNotNull(newSip);
        
        int virusCount = numberVirusesFound(newSip);
        
        Assert.assertEquals(1, virusCount);      
       
    }
    
    @Test
    public void multipleFileTest() throws IngestServiceException{
        String id = ingestFramework.getSipStager().addSIP(multipleFileSip);

        virusChecker.execute(id);

        Dcp newSip = ingestFramework.getSipStager().getSIP(id);

        Assert.assertNotNull(newSip);
        
        int virusCount = numberVirusesFound(newSip);
        
        Assert.assertEquals(1, virusCount);   
        
        int scanCount = 0;
        for (DcsEvent event : newSip.getEvents()) {
            if (event.getEventType() == Events.VIRUS_SCAN) {
                scanCount++;
            }
        }
        
        Assert.assertEquals(2, scanCount);
    }
    
    public int numberVirusesFound(Dcp sip){
        int virusCount = 0;

        for (DcsEvent event : sip.getEvents()) {
            if (event.getEventType() == Events.VIRUS_SCAN) {
                if (event.getOutcome().contains("A virus")) {
                    virusCount++;
                } 
            }
        }
        
        return virusCount;        
    }
    
    public void buildSips(){
       
        DcsFile file = new DcsFile();
        file.setSource(this.getClass().getResource(DIRTY_FILE).toString());
        file.setExtant(false);
        file.setName("Dirty_File");
        file.setId("dirty");
        dirtySip.addFile(file);
        
        DcsFile cleanFile = new DcsFile();
        cleanFile.setSource(this.getClass().getResource(CLEAN_FILE).toString());
        cleanFile.setExtant(false);
        cleanFile.setName("Clean_File");
        cleanFile.setId("clean");
        cleanSip.addFile(cleanFile);  
        
        multipleFileSip.addFile(file, cleanFile);
    }
}

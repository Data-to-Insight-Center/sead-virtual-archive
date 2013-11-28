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
package org.dataconservancy.ui.services;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.Citation;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

/** 
 * Tests for the CitationService implementation
 */
@DirtiesDatabase
@DirtiesContext
public class CitationServiceImplTest extends BaseUnitTest {
    
    @Autowired
    ArchiveService archiveService;
    
    @Autowired
    RelationshipService relationshipService;
            
    @Autowired
    ProjectService projectService;
    
    @Autowired
    CitationService citationService;

    private Citation generatedCitation;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    
    @Test                                                                                        
    public void createCitationTest(){
      try{
        ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(collectionWithDataDepositID);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection col = null;
        if( resultIter.hasNext()){
            col = resultIter.next();
        }
        assertNotNull(col);
        generatedCitation = citationService.createCitation(collectionWithData);
      } catch (ArchiveServiceException e){
        log.debug("Error retrieving collectionNoData: {}", e);
      }
       assertEquals(collectionWithData.getCreators(), generatedCitation.getCreators());
       assertEquals(collectionWithData.getTitle(), generatedCitation.getTitle());
       assertEquals(projectOne.getPublisher(), generatedCitation.getPublisher());
    }

}

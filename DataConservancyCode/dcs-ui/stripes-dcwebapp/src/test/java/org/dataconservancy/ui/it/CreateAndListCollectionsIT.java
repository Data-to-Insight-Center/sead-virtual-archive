/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.ui.it;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.UserService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * IT verifying that an admin can create a collection, view the collection list, and view the collection details.
 * Originally written to troubleshoot an issue with the deployment of the DCS and the UI on Bamboo.
 */
public class CreateAndListCollectionsIT extends BaseIT {
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person systemAdmin;
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person projectAdmin;
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    @Qualifier("delegatingUserService")
    private UserService userService;
    
    private HttpPost systemAdminLogin;
    private HttpPost projectAdminLogin;
    private HttpGet logout;
    
    @Before
    public void setUp() throws IOException {
        systemAdminLogin = reqFactory.createLoginRequest(systemAdmin).asHttpPost();
        projectAdminLogin = reqFactory.createLoginRequest(projectAdmin).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        HttpAssert.assertStatus(hc, systemAdminLogin, 302);
    }
    
    @Test
    public void testCreateCollectionAndViewCollectionList() throws Exception {
        Project p = new Project();
        
        p.setDescription("A description");
        p.setEndDate(DateTime.now());
        p.setFundingEntity("A funder");
        p.setStartDate(DateTime.now());
        p.setName("Project");
        p.addNumber("1234");
        p.setFundingEntity("A funder");
        p.addPi(projectAdmin.getId());
        
        p = reqFactory.createProjectApiAddRequest(p).execute(hc);
        
        HttpAssert.assertStatus(hc, urlConfig.getProjectUrl(p.getId()).toString(), 200);
        
        Collection c = new Collection();
        c.setId("bar");
        c.setTitle("Collection");
        c.setSummary("A collection");
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
        c.setCreators(creators);
        createCollectionRequest.setCollection(c);
        createCollectionRequest.setProjectId(p.getId());
        HttpAssert.assertStatus(hc, createCollectionRequest.asHttpPost(), 302);
        assertNotNull(archiveSupport.pollAndQueryArchiveForCollectionDu(c.getId()));
        
        HttpAssert.assertStatus(hc, urlConfig.getListCollectionsUrl().toString(), 200);
        HttpAssert.assertStatus(hc, urlConfig.getViewCollectionUrl(c.getId()).toString(), 200);
    }
}

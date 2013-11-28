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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URISyntaxException;

import java.nio.charset.Charset;

import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.resources.MHFTestResources;
import org.dataconservancy.ui.it.support.AddMetadataFormatRequest;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.ValidatingMetadataFileRequest;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.stripes.UiConfigurationActionBean;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ValidatingMetadataFileIT extends BaseIT {
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person adminUser;
    
    private HttpPost adminUserLogin;
    
    private HttpGet logout;
    
    private static final String BOP_URL = "http://dataconservancy.org/schemas/bop/1.0";
    private static final String validBopFilePath = "/SampleXML/project-testbop.xml";
    
    private static final String invalidBopFilePath = "/SampleXML/invalid-bop.xml";
    
    /**
     * Sets up the httpConnection, project, collection, and dataset
     */
    @Before
    public void setUp() throws Exception {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        adminUserLogin = reqFactory.createLoginRequest(adminUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
    }
    
    @Test
    public void testValidateValidFileRegistryFormat() throws ClientProtocolException, IOException, URISyntaxException {
        // Login as the administrator user, validate a sample metadata file, and logout.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        File sampleMetadataFile = new File(this.getClass().getResource(MHFTestResources.SAMPLE_VALID_FGDC_XML_RESOURCE_PATH).getPath());
        ValidatingMetadataFileRequest vmfr = reqFactory.createValidatingMetadataFileRequest(sampleMetadataFile, MetadataFormatId.FGDC_XML_FORMAT_ID);
        HttpResponse authorizedResponse = hc.execute(vmfr.asHttpPost());
        assertEquals("Unable to test file with Admin", 200, authorizedResponse.getStatusLine().getStatusCode());
        freeResponse(authorizedResponse);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
    }
    
    @Test
    public void testValidateInvalidFileRegistryFormat() throws ClientProtocolException, IOException, URISyntaxException {
        // Login as the administrator user, validate a sample metadata file, and logout.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        File sampleMetadataFile = new File(this.getClass().getResource(MHFTestResources.SAMPLE_INVALID_FGDC_XML_RESOURCE_PATH).getPath());
        ValidatingMetadataFileRequest vmfr = reqFactory.createValidatingMetadataFileRequest(sampleMetadataFile, MetadataFormatId.FGDC_XML_FORMAT_ID);
        HttpResponse authorizedResponse = hc.execute(vmfr.asHttpPost());
        assertEquals("Unable to test file with Admin", 400, authorizedResponse.getStatusLine().getStatusCode());
        freeResponse(authorizedResponse);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
    }
    
    @Test
    public void testValidateValidFileMemoryFormat() throws Exception {
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");

        //First add a format to system this format will be held in memory until saved.
        AddMetadataFormatRequest req = new AddMetadataFormatRequest(urlConfig);
        req.setName("bop");
        req.setVersion("1.0");
        req.setSchemaUrl(BOP_URL);
        req.setValidates(true);
        req.setAppliesToCollection(false);
        req.setAppliesToProject(false);
        req.setAppliesToItem(true);
        req.setDisciplineIds(Arrays.asList("dc:discipline:Biology"));

        HttpAssert.assertStatus(hc, req.asHttpPost(), 200);
        
        File sampleMetadataFile = new File(this.getClass().getResource(validBopFilePath).getPath());
        ValidatingMetadataFileRequest vmfr = reqFactory.createValidatingMetadataFileRequest(sampleMetadataFile, "dataconservancy.org:formats:file:metadata:bop:xml");
        HttpResponse authorizedResponse = hc.execute(vmfr.asHttpPost());
        assertEquals("Unable to test file with Admin", 200, authorizedResponse.getStatusLine().getStatusCode());
        freeResponse(authorizedResponse);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
    }
    
    @Test
    public void testValidateInValidFileMemoryFormat() throws Exception {
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");

        //First add a format to system this format will be held in memory until saved.
        AddMetadataFormatRequest req = new AddMetadataFormatRequest(urlConfig);
        req.setName("bop");
        req.setVersion("1.0");
        req.setSchemaUrl(BOP_URL);
        req.setValidates(true);
        req.setAppliesToCollection(false);
        req.setAppliesToProject(false);
        req.setAppliesToItem(true);
        req.setDisciplineIds(Arrays.asList("dc:discipline:Biology"));

        HttpAssert.assertStatus(hc, req.asHttpPost(), 200);
        
        File sampleMetadataFile = new File(this.getClass().getResource(invalidBopFilePath).getPath());
        ValidatingMetadataFileRequest vmfr = reqFactory.createValidatingMetadataFileRequest(sampleMetadataFile, "dataconservancy.org:formats:file:metadata:bop:xml");
        HttpResponse authorizedResponse = hc.execute(vmfr.asHttpPost());
        assertEquals("Unable to test file with Admin", 400, authorizedResponse.getStatusLine().getStatusCode());
        freeResponse(authorizedResponse);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
    }
}

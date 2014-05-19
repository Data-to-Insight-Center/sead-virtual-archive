/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.bagit;


import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import org.dspace.foresite.*;
import org.junit.Test;
import org.sead.acr.common.utilities.json.JSONException;
import org.seadva.bagit.Exception.SEADInvalidOREException;
import org.seadva.bagit.event.impl.OreValidationHandler;
import org.seadva.bagit.model.ActiveWorkspace;
import org.seadva.bagit.model.ActiveWorkspaces;
import org.seadva.bagit.service.Bag;
import org.seadva.bagit.service.BagItUtil;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class BagItTest extends JerseyTest {

    public BagItTest() throws Exception {
        super(new WebAppDescriptor.Builder("org.seadva.bagit").
                contextParam("testPath","/src/main/webapp/").build());
    }

    @Test
    public void testGetACRBag() {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("sparqlEpEnum", "3");

        ClientResponse response = webResource.path("acrToBag")
                .path("bag")
                .path(
                        URLEncoder.encode(
                                "tag:cet.ncsa.uiuc.edu,2008:/bean/Collection/E012014D-7379-4556-8A87-6AD262965C89"
                        )
                )
                .queryParams(params)
                .accept("application/zip")
                .get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }



    @Test
    public void testRESTListACR() throws IOException {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("acrToBag")
                .path("listACR")
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);


        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(),writer);
        String xml = writer.toString();

        XStream xStream = new XStream();
        xStream.alias("ActiveWorkspaces",ActiveWorkspaces.class);
        xStream.alias("ActiveWorkspace",ActiveWorkspace.class);
        ActiveWorkspaces workspaceList = new ActiveWorkspaces();
        xStream.fromXML(xml, workspaceList);

        assertEquals(200, response.getStatus());
        assertEquals(3, workspaceList.getSpaceList().size());
    }

    @Test
    public void testListACR(){
        Bag service= new Bag();
        XStream xStream = new XStream();

        ActiveWorkspaces workspaceList = new ActiveWorkspaces();
        xStream.fromXML(service.viewACR(), workspaceList);
        assertEquals(3, workspaceList.getSpaceList().size());
    }

    @Test
    public void testGetSIP() throws IOException {

        WebResource webResource = resource();

        File file = new File(getClass().getResource("E012014D-7379-4556-8A87-6AD262965C89.zip").getFile());
        FileDataBodyPart fdp = new FileDataBodyPart("file", file,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.bodyPart(fdp);

        ClientResponse response = webResource.path("acrToBag")
                .path("sip")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, formDataMultiPart);
        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(),writer);
        /*String sipText = "<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://dataconservancy.org/schemas/dcp/1.0 http://dataconservancy.org/schemas/dcp/1.0\"><DeliverableUnits><DeliverableUnit id=\"sample_bag\"><title>Sample Bag Collection</title><abstract>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</abstract><pubdate>2007-01-01</pubdate><sizeBytes>0</sizeBytes><fileNo>0</fileNo><contact>None</contact></DeliverableUnit></DeliverableUnits><Manifestations><Manifestation id=\"sample_bagman\"><deliverableUnit ref=\"sample_bag\" /><manifestationFile ref=\"http://pdf_file_id\" /><manifestationFile ref=\"http://xls_file_id\" /></Manifestation></Manifestations><Files><File id=\"http://pdf_file_id\" src=\"http://ashbha.ads.iu.edu/sample/sample.pdf\"><fileName>sample.pdf</fileName><extant>true</extant><format><id scheme=\"http://www.iana.org/assignments/media-types/\">application/pdf</id></format></File><File id=\"http://xls_file_id\" src=\"http://ashbha.ads.iu.edu/sample/sample.xls\"><fileName>sample.xls</fileName><extant>true</extant><format><id scheme=\"http://www.iana.org/assignments/media-types/\">application/vnd.ms-excel</id></format></File></Files></dcp>";
        assertEquals(sipText,writer.toString());*/
        assertNotNull(writer.toString());
        assertEquals(200,response.getStatus());
    }

    @Test
    public void testOreHoleyBagUtil() {
        WebResource webResource = resource();

        File file = new File(getClass().getResource("no_ore_bag.zip").getFile());
        FileDataBodyPart fdp = new FileDataBodyPart("file", file,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.bodyPart(fdp);

        ClientResponse response = webResource.path("bagUtil")
                .path("OreBag")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, formDataMultiPart);
        assertEquals(200,response.getStatus());

    }

    @Context
    ServletContext servletConfig;
    @Test
    public void testOreDataBagUtil() {
        WebResource webResource = resource();

        File file = new File(getClass().getResource("sample_bag.zip").getFile());
        FileDataBodyPart fdp = new FileDataBodyPart("file", file,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.bodyPart(fdp);

        ClientResponse response = webResource.path("bagUtil")
                .path("OreDataBag")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, formDataMultiPart);
        assertEquals(200,response.getStatus());

    }

    @Test
    public void testValidateForMinimalOAIORE() throws IOException, OREParserException, ORESerialiserException, OREException, JSONException, URISyntaxException, SEADInvalidOREException {

        WebResource webResource = resource();

        InputStream input = getClass().getResourceAsStream("Vortex2_Visualization_oaiore.xml");
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap rem = parser.parse(input);
        OreValidationHandler oreValidationHandler = new OreValidationHandler();
        assertEquals(true, oreValidationHandler.validateForMinimalOAIORE(rem));
    }
  }

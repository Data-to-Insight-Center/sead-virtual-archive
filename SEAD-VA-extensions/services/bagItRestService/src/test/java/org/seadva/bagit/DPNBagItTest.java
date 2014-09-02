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

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.junit.Test;
import org.seadva.bagit.service.DPNBagItUtil;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadRepository;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.seadva.registry.mapper.DcsDBMapper;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
//import org.dataconservancy.dcs.util.HttpHeaderUtil;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class DPNBagItTest extends JerseyTest {

    private final String baseUrl = "http://localhost:8080/sead-wf/";

    private String sipPostUrl = baseUrl + "deposit/sip";
    private static DefaultHttpClient client;
    private final static Properties props = new Properties();

    public DPNBagItTest() throws Exception {
        super(new WebAppDescriptor.Builder("org.seadva.bagit").
                contextParam("testPath","/src/main/webapp/").build());
    }

    @Context
    ServletContext servletConfig;
    @Test
    public void testDPNBagUtil() throws IllegalAccessException, ClassNotFoundException, InstantiationException, FileNotFoundException, InvalidXmlException {
        WebResource webResource = resource();
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        //String path = "/home/dpnuser/Documents/dpn/sample_bag/";
        //String path = "/home/dpnuser/Documents/mdpi_40000000000012-1gb/";
        String path = "/Users/Aravindh/Documents/DPN/DPNBags/sample_bag/";
        params.add("dirPath", path);

        client = new DefaultHttpClient();//(DefaultHttpClient) appContext.getBean("httpClient");

        final URL defaultProps = DPNBagItTest.class.getResource("/default.properties");
        assertTrue("default.properties does not exist.", new File(defaultProps.getPath()).exists());
        try {
            props.load(defaultProps.openStream());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

//        ClientResponse response = webResource.path("dpnBagUtil")
//                .path("DPNBag")
//                .queryParams(params)
//                .get(ClientResponse.class);
//        assertEquals(200,response.getStatus());

        //Direct calls

        String sipPath = new DPNBagItUtil().getSip(path)+"IU-sip.xml";
        System.out.println(sipPath);

        //Tar module in wf
        ResearchObject sip =new SeadXstreamStaxModelBuilder().buildSip(new FileInputStream(sipPath));
        for(DcsDeliverableUnit du :sip.getDeliverableUnits())
        {
            SeadDataLocation location = new SeadDataLocation();
            location.setLocation(path); //for tar requirement
            location.setName("filepath");
            location.setType("filepath");
            ((SeadDeliverableUnit) du).setPrimaryLocation(location);
        }

        SeadRepository repo = new SeadRepository();
        repo.setType("cloud");
        repo.setName("IU SDA");
        repo.setIrId("5");
        repo.setUrl("https://www.sdarchive.iu.edu/");
        sip.addRepository(repo);
        new SeadXstreamStaxModelBuilder().buildSip(sip, new FileOutputStream(sipPath));

        client.getCredentialsProvider().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(
                        "seadva@gmail.com", hashPassword("password")
                ));
        try {
            int code = doDeposit(new File(sipPath));
            //buildSipFromRegistry();
            buildSipFromRegistry();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String hashPassword(String password)
    {
        String hashword = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(password.getBytes());
            BigInteger hash = new BigInteger(1, md5.digest());
            hashword = hash.toString(16);
        }
        catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
        {
        }
        return pad(hashword, 32, '0');
    }

    private String pad(String s, int length, char pad) {
        StringBuffer buffer = new StringBuffer(s);
        while (buffer.length() < length) {
            buffer.insert(0, pad);
        }
        return buffer.toString();
    }

    private int doDeposit(File file) throws Exception {

        HttpPost post = new HttpPost(sipPostUrl);
        post.setHeader("CONTENT_TYPE", "application/xml");
        post.setHeader("X-Packaging", "http://dataconservancy.org/schemas/dcp/1.0");

        post.setEntity(new FileEntity(file, "application/xml"));

        HttpResponse response = client.execute(post);

        int code = response.getStatusLine().getStatusCode();
        Header[] location = response.getHeaders("Location");
        if (code >= 200 && code <= 202) {
            response.getAllHeaders();
            Header[] headers = response.getAllHeaders();
            for (Header header : headers)
                System.out.println("FinalKey : " + header.getName()
                        + " ,FinalValue : " + header.getValue());
            HttpEntity responseEntity = response.getEntity();
            InputStream content = responseEntity.getContent();
            try {
                IOUtils.toByteArray(content);
            } finally {
                content.close();
            }
        }
        return code;
    }

    private void buildSipFromRegistry() throws IOException, ClassNotFoundException {
        ResearchObject researchObject = new DcsDBMapper("http://localhost:8080/registry/rest/").getSip("http://localhost:8080/sead-wf/entity/81009");
        new SeadXstreamStaxModelBuilder().buildSip(researchObject, new FileOutputStream("/tmp/output_sip.xml"));
    }
}


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

package org.dataconservancy.dcs.integration.main;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.dataconservancy.dcs.integration.support.Interpolator.interpolate;
import static org.junit.Assert.*;

/**
 * Test case of SIP ingest
 */
public class IngestTest {

    private final String baseUrl = interpolate(new StringBuilder("${dcs.baseurl}/"), 0, props).toString();

    private String sipPostUrl = baseUrl + "deposit/sip";
    private static DefaultHttpClient client;
    private final static Properties props = new Properties();

    @BeforeClass
    public static void init() throws IOException {
       /* ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext(new String[] {
                        "depositClientContext.xml", "classpath*:org/dataconservancy/config/applicationContext.xml"});*/


        client = new DefaultHttpClient();//(DefaultHttpClient) appContext.getBean("httpClient");

        final URL defaultProps = IngestTest.class.getResource("/default.properties");
        assertNotNull("Could not resolve /default.properties from the classpath.", defaultProps);
        assertTrue("default.properties does not exist.", new File(defaultProps.getPath()).exists());
        props.load(defaultProps.openStream());
    }

    @Test
    public void sipIngest_db_authentication() throws Exception {
        client.getCredentialsProvider().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(
                        "seadva@gmail.com","password"
                ));
        int code = doDeposit(new File(IngestTest.class.getResource("/" + "sampleSip.xml").getPath()));
        assertEquals(code,202);
    }

    @Test
    public void sipIngest_oauth_authentication() throws Exception {
        //generate OAuth token from Util below
        sipPostUrl+="?oauth_token="+"ya29.1.AADtN_WGcJREOIxNzHjVID7tG5ccjcITXc2HFzChw2OOBJ5SDXGTz7DdLGcfEmHcA9sr1A";
        int code = doDeposit(new File(IngestTest.class.getResource("/" + "sampleSip.xml").getPath()));
        assertEquals(code,202);
    }
    //https://accounts.google.com/o/oauth2/auth?scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile&state=%2Fprofile&redirect_uri=http://localhost:8080/sead-access/&response_type=token&client_id=343397275658-72p8a7jemrm0rdkgci440dfnnse4g0f7.apps.googleusercontent.com


    private int doDeposit(File file) throws Exception {

        HttpPost post = new HttpPost(sipPostUrl);
        post.setHeader(HttpHeaderUtil.CONTENT_TYPE, "application/xml");
        post.setHeader("X-Packaging", "http://dataconservancy.org/schemas/dcp/1.0");

        post.setEntity(new FileEntity(file, "application/xml"));

        HttpResponse response = client.execute(post);

        int code = response.getStatusLine().getStatusCode();
        if (code >= 200 && code <= 202) {
            response.getAllHeaders();
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
}

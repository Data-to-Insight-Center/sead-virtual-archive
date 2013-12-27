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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import static org.dataconservancy.dcs.integration.support.Interpolator.interpolate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: kavchand
 * Date: 12/27/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class IngestTest {

    private final String baseUrl = interpolate(new StringBuilder("${dcs.baseurl}/"), 0, props).toString();

    private final String sipPostUrl = baseUrl + "deposit/sip";
    private static HttpClient client;
    private final static Properties props = new Properties();

    @BeforeClass
    public static void init() throws IOException {
        ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext(new String[] {
                        "depositClientContext.xml", "classpath*:org/dataconservancy/config/applicationContext.xml"});

        client = (HttpClient) appContext.getBean("httpClient");
        final URL defaultProps = IngestTest.class.getResource("/default.properties");
        assertNotNull("Could not resolve /default.properties from the classpath.", defaultProps);
        assertTrue("default.properties does not exist.", new File(defaultProps.getPath()).exists());
        props.load(defaultProps.openStream());
    }

    @Test
    public void sipleIngest() throws Exception {
       int code = doDeposit(new File(IngestTest.class.getResource("/" + "sampleSip.xml").getPath()));
        assertEquals(code,202);
    }

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

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
package org.dataconservancy.dcs.lineage.http.support;

import org.dataconservancy.model.dcs.DcsEntity;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class RequestUtilTest {

    @Test
    public void testCreateEtag() throws Exception {
        final String entityId = "foo";
        final String expectedEtag = "acbd18db4cc2f85cedef654fccc4a4d8";

        final RequestUtil underTest = new RequestUtil();

        final String actualEtag = underTest.calculateDigest(Arrays.asList(entityId));

        assertEquals(expectedEtag, actualEtag);
    }

    @Test
    public void calculateDigestOverEmptyEntitiesList() {
        final List<DcsEntity> entities = Collections.emptyList();

        final RequestUtil underTest = new RequestUtil();

        assertEquals(null, underTest.calculateDigestForEntities(entities));
    }

    @Test
    public void calculateDigestOverEmptyIdsList() {
        final List<String> ids = Collections.emptyList();

        final RequestUtil underTest = new RequestUtil();

        assertEquals(null, underTest.calculateDigest(ids));

    }

    @Test
    public void testCreateEtags() throws Exception {
        final String entityIdOne = "foo";
        final String entityIdTwo = "bar";
        final String expectedEtag = "3858f62230ac3c915f300c664312c63f";

        final RequestUtil underTest = new RequestUtil();

        final String actualEtag = underTest.calculateDigest(Arrays.asList(entityIdOne, entityIdTwo));

        assertEquals(expectedEtag, actualEtag);
    }

    @Test
    public void testBuildRequestUrlPort80Secure() throws Exception {
        final String expected = "https://instance.org:80/lineage/123";

        final MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("https");
        req.setRemotePort(80);
        req.setSecure(true);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlPort443() throws Exception {
        final String expected = "https://instance.org/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("https");
        req.setRemotePort(443);
        req.setSecure(true);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlPort443Insecure() throws Exception {
        final String expected = "http://instance.org:443/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("http");
        req.setRemotePort(443);
        req.setSecure(false);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlPort8080Insecure() throws Exception {
        final String expected = "http://instance.org:8080/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("http");
        req.setRemotePort(8080);
        req.setSecure(false);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlPort8443Secure() throws Exception {
        final String expected = "https://instance.org:8443/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("https");
        req.setRemotePort(8443);
        req.setSecure(true);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlPort80() throws Exception {
        final String expected = "http://instance.org/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("instance.org");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlLocalhost() throws Exception {
        final String expected = "http://localhost/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("localhost");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

        @Test
    public void testBuildRequestUrlSecureLocalhost() throws Exception {
        final String expected = "https://localhost:80/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("localhost");
        req.setScheme("https");
        req.setSecure(true);
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlLocalhost8080() throws Exception {
        final String expected = "http://localhost:8080/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("localhost");
        req.setScheme("http");
        req.setRemotePort(8080);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }


    @Test
    public void testBuildRequestUrlLocalhostIp() throws Exception {
        final String expected = "http://localhost/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("127.0.0.1");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlLocalhostIp8080() throws Exception {
        final String expected = "http://localhost:8080/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("127.0.0.1");
        req.setScheme("http");
        req.setRemotePort(8080);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlLocalhostIpv6() throws Exception {
        final String expected = "http://localhost/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("0:0:0:0:0:0:0:1%0");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlLocalhostIpv68080() throws Exception {
        final String expected = "http://localhost:8080/lineage/123";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.setRemoteHost("0:0:0:0:0:0:0:1%0");
        req.setScheme("http");
        req.setRemotePort(8080);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlWithHostHeader() throws Exception {
        final String expected = "http://www.foo.com/lineage/123";
        final String hostHeader = "www.foo.com:80";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.addHeader("Host", hostHeader);
        req.setRemoteHost("bar.baz");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }

    @Test
    public void testBuildRequestUrlWithHostHeaderNoPort() throws Exception {
        final String expected = "http://www.foo.com/lineage/123";
        final String hostHeader = "www.foo.com";

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/lineage/123");
        req.addHeader("Host", hostHeader);
        req.setRemoteHost("bar.baz");
        req.setScheme("http");
        req.setRemotePort(80);

        final RequestUtil underTest = new RequestUtil();

        assertEquals(expected, underTest.buildRequestUrl(req));
    }
}

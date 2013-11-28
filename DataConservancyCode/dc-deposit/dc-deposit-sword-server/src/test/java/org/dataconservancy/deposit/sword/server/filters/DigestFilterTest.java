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
package org.dataconservancy.deposit.sword.server.filters;

import java.io.InputStream;

import java.util.Map;

import javax.security.auth.Subject;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.RequestProcessor;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.TargetType;

import org.junit.Test;

import org.dataconservancy.deposit.sword.server.impl.MockRequestContext;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

public class DigestFilterTest {

    private static final String FILE = "/file.txt";

    private static final String FILE_MD5 = "1e75d5298fd12184f34bad372a81b3e6";

    static {

    }

    @Test
    public void calculateMd5Test() throws Exception {
        MockRequestContext request = new MockRequestContext();
        request.setInputStream(this.getClass().getResourceAsStream(FILE));

        DigestFilter filter = new DigestFilter();
        MockProvider provider = new MockProvider();
        filter.filter(request, getFilterChain(request, provider));

        /* At this point, no attributes should be set */
        assertEquals(0,
                     provider.finalRequestContext.getAttributeNames(null).length);

        InputStream stream = provider.finalRequestContext.getInputStream();
        while (stream.read() != -1);

        /* Now that wee've read the stream, we should have the value */
        assertEquals(FILE_MD5, provider.finalRequestContext
                .getAttribute(null, DigestFilter.CALCULATED_DIGEST));
        assertEquals("MD5", provider.finalRequestContext
                .getAttribute(null, DigestFilter.CALCULATED_DIGEST_ALGORITHM));

    }

    @Test
    public void calculateSpecifiedDigestAlgorithmTest() throws Exception {
        final String ALGORITHM = "SHA-1";
        MockRequestContext request = new MockRequestContext();
        request.setInputStream(this.getClass().getResourceAsStream(FILE));

        DigestFilter filter = new DigestFilter();
        filter.setDigestAlgorithm(ALGORITHM);

        MockProvider provider = new MockProvider();
        filter.filter(request, getFilterChain(request, provider));

        InputStream stream = provider.finalRequestContext.getInputStream();
        while (stream.read() != -1);

        /* Now that wee've read the stream, we should have the value */
        assertNotSame(FILE_MD5, provider.finalRequestContext
                .getAttribute(null, DigestFilter.CALCULATED_DIGEST));
        assertEquals(ALGORITHM, provider.finalRequestContext
                .getAttribute(null, DigestFilter.CALCULATED_DIGEST_ALGORITHM));
    }

    private FilterChain getFilterChain(RequestContext request,
                                       MockProvider provider) {
        return new FilterChain(provider, request);
    }

    private class MockProvider
            implements Provider {

        protected RequestContext finalRequestContext;

        public void addRequestProcessors(Map<TargetType, RequestProcessor> requestProcessors) {
        }

        public Abdera getAbdera() {
            return null;
        }

        public Filter[] getFilters(RequestContext request) {
            return new Filter[0];
        }

        public String getProperty(String name) {
            return null;
        }

        public String[] getPropertyNames() {
            return null;
        }

        public Map<TargetType, RequestProcessor> getRequestProcessors() {
            return null;
        }

        public void init(Abdera abdera, Map<String, String> properties) {
        }

        public ResponseContext process(RequestContext request) {
            finalRequestContext = request;
            return null;
        }

        public Subject resolveSubject(RequestContext request) {
            return null;
        }

        public Target resolveTarget(RequestContext request) {
            return null;
        }

        public void setRequestProcessors(Map<TargetType, RequestProcessor> requestProcessors) {
        }

        public String urlFor(RequestContext request, Object key, Object param) {
            return null;
        }
    }
}

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

import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link UrlDepositDocumentResolver}
 */
public class UrlDepositDocumentResolverTest {

    private MockParser parser;

    private MockDocument doc;

    /**
     * Create and wire up mock instances of the {@link DepositDocument} and {@link DepositDocumentParser}.  The
     * mock parser doesn't actually parse anything, it just returns the mock document when the parse(...) method is
     * called.
     */
    @Before
    public void setUp() {
        parser = new MockParser();
        doc = new MockDocument();
        parser.doc = doc;
    }

    /**
     * Attempt to resolve a URL that points to an existing entity on the file system.
     *
     * @throws Exception
     */
    @Test
    public void testResolveOk() throws Exception {
        URL entityUrl = this.getClass().getResource("sampleDeposit/426029.xml");
        assertNotNull(entityUrl);

        doc.id = entityUrl.toString();

        assertTrue(new File(entityUrl.toURI()).exists());

        UrlDepositDocumentResolver underTest = new UrlDepositDocumentResolver(parser);
        assertEquals(doc, underTest.resolve(doc.id));
    }

    /**
     * Attempt to resolve an identifier that is not a URL.
     *
     * @throws Exception
     */
    @Test
    public void testResolveNonUrl() throws Exception {
        doc.id = "not a url";
        parser.doc = doc;

        UrlDepositDocumentResolver underTest = new UrlDepositDocumentResolver(parser);
        assertNull(underTest.resolve(doc.id));
    }

    /**
     * Attempt to resolve a URL that doesn't point to anything.
     * 
     * @throws Exception
     */
    @Test
    public void testResolveNonExistentUrl() throws Exception {
        File f = File.createTempFile("UrlDepositDocumentResolverTest", ".txt");
        f.delete();
        assertFalse(f.exists());
        URL nonExistentUrl = f.toURL();
        doc.id = nonExistentUrl.toString();

        UrlDepositDocumentResolver underTest = new UrlDepositDocumentResolver(parser);
        assertNull(underTest.resolve(doc.id));
    }

    /**
     * This {@code DepositDocumentParser} doesn't actually parse anything, it just returns a document supplied
     * by the caller.
     */
    private class MockParser implements DepositDocumentParser {
        private DepositDocument doc = null;

        @Override
        public DepositDocument parse(InputStream in) throws IOException {
            return doc;
        }
    }

    /**
     * Simple implementation of {@code DepositDocument}.
     */
    private class MockDocument implements DepositDocument {
        private Set<DcsEntity> entities;
        private String id;
        private boolean isComplete;
        private boolean isSuccessful;
        private DcsDeliverableUnit root;

        @Override
        public Set<DcsEntity> getEntities() {
            return entities;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean isComplete() {
            return isComplete;
        }

        @Override
        public boolean isSuccessful() {
            return isSuccessful;
        }

        @Override
        public DcsDeliverableUnit getRoot() {
            return root;
        }
    }
}

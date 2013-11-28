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
package org.dataconservancy.access.connector;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class SimpleConnectorIT extends AbstractConnectorIT {

    private HttpClient client;
    private DcsConnector underTest;

    @Autowired
    @Qualifier("localDcsConnectorConfig")
    private DcsConnectorConfig localDcsConnectorConfig;

    @Before
    public void setUp() throws MalformedURLException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);

        client = new DefaultHttpClient();
        final DcsModelBuilder mb = new DcsXstreamStaxModelBuilder();
        underTest = new HttpDcsConnector(localDcsConnectorConfig, mb);
    }

    @After
    public void tearDown() {
        client.getConnectionManager().shutdown();
    }


    @Test
    public void testSimple() throws MalformedURLException, DcsConnectorFault {
        assertNotNull(underTest.getStream(String.format(ENTITY_ID, "test1")));
    }

    @Test
    public void testGetStreamOk() throws IOException, DcsConnectorFault, SAXException, InvalidXmlException {
        final Resource entitiesDir = rl.getResource("/entities");
        assertTrue(entitiesDir.exists() && entitiesDir.getFile().canRead());

        final InputStream entityIn = underTest.getStream(String.format(ENTITY_ID, "test1"));
        assertNotNull(entityIn);
        final File test1Entity = new File(entitiesDir.getFile(), "test1.xml");
        final String entityString = IOUtils.toString(entityIn);
        XMLAssert.assertXMLEqual(IOUtils.toString(new FileInputStream(test1Entity)), entityString);
        assertEquals(mb.buildSip(new FileInputStream(test1Entity)), mb.buildSip(IOUtils.toInputStream(entityString)));
    }

    @Test
    public void getAllFilesOk() throws DcsConnectorFault {
        // get the file entities
        final Set<DcsFile> fileEntities = new HashSet<DcsFile>();

        for (DcsEntity e : allTestEntities) {
            if (e instanceof DcsFile) {
                fileEntities.add((DcsFile)e);
            }
        }

        assertTrue(fileEntities.size() > 0);
        LOG.debug("Loaded {} file entities from the filesystem.", fileEntities.size());

        int count = 0;
        Iterator<DcsFile> itr = underTest.getFiles(null);
        assertNotNull(itr);
        while (itr.hasNext()) {
            final DcsFile f = itr.next();
            count++;
            assertNotNull(f);
            assertTrue("Expected to find File entity " + f.getId() + " on the filesystem.  " +
                    "(found " + count + " entities so far)", allTestEntitiesById.containsKey(f.getId()));
            assertTrue("Expected " + f.getId() + " to identify a File entity on the filesystem, but instead it " +
                    "identifies a " + allTestEntitiesById.get(f.getId()).getClass().getName(),
                    allTestEntitiesById.get(f.getId()) instanceof DcsFile);
        }

        assertEquals(fileEntities.size(), count);
    }

    @Test
    public void getFileTest54Ok() throws DcsConnectorFault {

        //
        // DU -> Manifestation -> Manifestation File -> File
        // (test50) -> (test51) -> (ref to test54) -> (test54)

        Iterator<DcsFile> itr = underTest.getFiles("http://localhost:8080/entity/test50");
        assertNotNull(itr);
        boolean foundDu = false;
        while (itr.hasNext()) {
            DcsEntity e = itr.next();
            if (e.getId().equals("http://localhost:8080/entity/test55") && e instanceof DcsFile) {
                foundDu = true;
            }
        }

        assertTrue(foundDu);
    }

    @Test
    public void getFileTest10Ok() throws DcsConnectorFault {

        //
        //  File = test10
        //

        Iterator<DcsFile> itr = underTest.getFiles("http://localhost:8080/entity/test10");
        assertNotNull(itr);
        assertTrue(itr.hasNext());
        boolean found = false;
        while (itr.hasNext()) {
            DcsEntity e = itr.next();
            if (e.getId().equals("http://localhost:8080/entity/test10") && e instanceof DcsFile) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void getFilesForFileEntity() throws DcsConnectorFault, InvalidXmlException, IOException, NoSuchAlgorithmException {
        final String entityId = "http://localhost:8080/entity/test10";

        // Retrieve the file entity using getFiles
        final Iterator<DcsFile> files = underTest.getFiles(entityId);
        assertNotNull(files);
        assertTrue(files.hasNext());
        final DcsFile file = files.next();
        assertEquals(entityId, file.getId());

        // Retrieve the source of the file
        final InputStream fileContent = underTest.getStream(file.getSource());
        assertNotNull(fileContent);

        assertFixtyDigestEqual(file.getFixity().iterator().next(), fileContent);
    }


}

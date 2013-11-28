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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.dataconservancy.dcs.util.DigestListener;
import org.dataconservancy.dcs.util.DigestNotificationOutputStream;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFixity;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public abstract class AbstractConnectorIT extends AbstractJUnit4SpringContextTests {

    static Logger LOG = LoggerFactory.getLogger(AbstractConnectorIT.class);
    final static ResourceLoader rl = new DefaultResourceLoader();
    final static DcsModelBuilder mb = new DcsXstreamStaxModelBuilder();

    final static Set<DcsEntity> allTestEntities = new HashSet<DcsEntity>();
    final static Map<String, DcsEntity> allTestEntitiesById = new HashMap<String, DcsEntity>();

    final static String BASE_ENDPOINT = "http://localhost:8080/";
    final static String STREAM_ENDPOINT = BASE_ENDPOINT + "datastream/";
    final static String QUERY_ENDPOINT = BASE_ENDPOINT + "query/";
    final static String ENTITY_ENDPOINT = BASE_ENDPOINT + "entity/";

    final static String DATASTREAM_ID = STREAM_ENDPOINT + "%s";
    final static String ENTITY_ID = ENTITY_ENDPOINT + "%s";

    @BeforeClass
    public static void getAllTestEntities() throws IOException, InvalidXmlException {
        final String entitiesResource = "/entities";
        final Resource entities = rl.getResource(entitiesResource);
        assertNotNull(entities);
        assertTrue(entities.exists() && entities.getFile().canRead() && entities.getFile().isDirectory());
        Collection<File> files = FileUtils.listFiles(entities.getFile(), new String[]{"xml"}, false);
        for (File f : files) {
            Dcp dcp = mb.buildSip(new FileInputStream(f));
            allTestEntities.addAll(dcp.getCollections());
            allTestEntities.addAll(dcp.getDeliverableUnits());
            allTestEntities.addAll(dcp.getEvents());
            allTestEntities.addAll(dcp.getFiles());
            allTestEntities.addAll(dcp.getManifestations());
        }

        for (DcsEntity e : allTestEntities) {
            allTestEntitiesById.put(e.getId(), e);
        }
    }

    /**
     * Asserts that the supplied input stream has the expected fixity.
     *
     * @param expectedFixity the expected fixity value of the stream
     * @param in             the input stream
     * @throws java.security.NoSuchAlgorithmException
     * @throws IOException
     */
    void assertFixtyDigestEqual(final DcsFixity expectedFixity, final InputStream in)
            throws NoSuchAlgorithmException, IOException {
        Assert.assertNotNull("Fixity must not be null", expectedFixity);
        Assert.assertNotNull("Inputstream must not be null", in);

        final StringBuilder actualDigest = new StringBuilder();
        final NullOutputStream nullOs = new NullOutputStream();
        final DigestNotificationOutputStream digestOut = new DigestNotificationOutputStream(nullOs,
                MessageDigest.getInstance(expectedFixity.getAlgorithm()), new DigestListener() {
                    @Override
                    public void notify(byte[] digestValue) throws IOException {
                        for (byte b : digestValue) {
                            actualDigest.append(String.format("%02x", b));
                        }

                    }
                }
        );

        IOUtils.copy(in, digestOut);
        digestOut.close();
        assertEquals(expectedFixity.getValue(), actualDigest.toString());
    }

}

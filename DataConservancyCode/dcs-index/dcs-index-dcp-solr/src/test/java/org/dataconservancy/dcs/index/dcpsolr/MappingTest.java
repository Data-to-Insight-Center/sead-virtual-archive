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
package org.dataconservancy.dcs.index.dcpsolr;

import java.io.IOException;

import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;

/**
 * Test the persistence of entities to the index.
 */
public class MappingTest extends AbstractIndexTest {

    // Do not load into the archive in order to deal with random DcsFile
    // sources.
    public void setUp() throws Exception {
        super.setUp();
        archive = null;
    }

    /**
     * Torture test that generates a bunch of random objects, writes them to the
     * index, reads them back and checks that they survived the transformation.
     */
    public void testMapping() throws Exception {
        Dcp dcp = rb.createDcp(10, 10);

        index(dcp);

        System.out
                .println("bytes per entity: "
                        + (double) FileUtil.size(solrhome)
                        / DcpUtil.asList(dcp).size());

        for (DcsEntity entity : DcpUtil.asList(dcp)) {
            DcsEntity e = service.lookupEntity(entity.getId());

            assertNotNull(e);

            final HierarchicalPrettyPrinter stringBuilder = new HierarchicalPrettyPrinter();
            entity.toString(stringBuilder);
            System.err.println("WANT: " + stringBuilder.toString());
            stringBuilder.reset();
            e.toString(stringBuilder);
            System.err.println("GOT:  " + stringBuilder.toString());

            assertEquals(entity, e);
        }

        assertNull(service.lookupEntity("doesnotexist"));
        assertNull(service.lookupEntity("*"));
        assertNull(service.lookupEntity(""));
        assertNull(service.lookupEntity("!(asdjkfa({"));
        assertNull(service.lookupEntity(">?>{}{{DS[aldsk!#$@#Q$#323q''a"));
        assertNull(service.lookupEntity("\"blahsd"));

        assertEquals(DcpUtil.asList(dcp).size(), service.size());
        service.clear();
        assertEquals(0, service.size());
    }

    /**
     * Torture test that generates a bunch of random objects, writes them to the
     * archive, and indexes the archive.
     * 
     * @throws IndexServiceException
     * @throws AIPFormatException
     */
    public void testMappingCollection() throws IndexServiceException,
            AIPFormatException {
        DcsCollection col = rb.createCollection("parent");
        index(col);

        DcsEntity e = service.lookupEntity(col.getId());

        assertNotNull(e);
        assertEquals(col, e);
    }

    public void testMappingManifestation() throws IndexServiceException,
            AIPFormatException {
        List<DcsFile> files = new ArrayList<DcsFile>();
        DcsManifestation man = rb.createManifestation("du", files);
        Dcp dcp = DcpUtil.add(null, files.toArray(new DcsFile[] {}));
        DcpUtil.add(dcp, man);

        index(dcp);

        DcsEntity e = service.lookupEntity(man.getId());

        // System.out.println("WANT :" + man);
        // System.out.println("GOT  :" + e);

        assertNotNull(e);
        assertEquals(man, e);
    }

    public void testMappingManifestationWithDateCreated()
            throws IndexServiceException, AIPFormatException {

        DcsManifestation man = new DcsManifestation();
        man.setDeliverableUnit("du");
        man.setDateCreated(DateUtility.toIso8601(DateUtility.now()));
        man.setId("man");

        index(man);

        DcsEntity e = service.lookupEntity(man.getId());

        assertNotNull(e);
        assertEquals(man, e);
    }

    public void testMappingEvent() throws IndexServiceException,
            AIPFormatException {
        DcsEvent ev = rb.createEvent("moo");

        index(ev);

        DcsEntity e = service.lookupEntity(ev.getId());

        assertNotNull(e);
        assertEquals(ev, e);
    }

    public void testMappingFile() throws IndexServiceException,
            AIPFormatException {
        DcsFile file = rb.createFile();

        index(file);

        DcsEntity e = service.lookupEntity(file.getId());

        // System.out.println("WANT " + file);
        // System.out.println("GOT  " + e);

        assertNotNull(e);
        assertEquals(file, e);
    }

    public void testMappingDU() throws IndexServiceException,
            AIPFormatException {
        DcsDeliverableUnit du = rb.createDeliverableUnit("blah", null, true);

        index(du);

        DcsEntity e = service.lookupEntity(du.getId());

        // System.out.println("WANT " + du);
        // System.out.println("GOT  " + e);

        assertNotNull(e);
        assertEquals(du, e);
    }

    public void testSolorMappingDUWithRights() throws IOException,
            IndexServiceException, AIPFormatException {
        DcsDeliverableUnit du = rb.createDeliverableUnit("blah", null, true);
       
        du.setRights("this is a rights statement.");

        index(du);

        DcsEntity e = service.lookupEntity(du.getId());

        // System.out.println("WANT " + du);
        // System.out.println("GOT  " + e);

        assertNotNull(e);
        assertEquals(du, e);
    }

    public void testMappingDUWithRightsAndLicenseURI()
            throws IndexServiceException, URISyntaxException,
            AIPFormatException {

        DcsDeliverableUnit du = rb.createDeliverableUnit("blah", null, true);
      
        du.setRights("this is a rights statement.");
   
        index(du);

        DcsEntity e = service.lookupEntity(du.getId());

        // System.out.println("WANT " + du);
        // System.out.println("GOT  " + e);

        assertNotNull(e);
        assertEquals(du, e);
    }
}

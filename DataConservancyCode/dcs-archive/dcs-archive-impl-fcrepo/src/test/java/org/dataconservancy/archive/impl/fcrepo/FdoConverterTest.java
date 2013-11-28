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
package org.dataconservancy.archive.impl.fcrepo;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;

import com.thoughtworks.xstream.XStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.archive.impl.fcrepo.dto.Datastream;
import org.dataconservancy.archive.impl.fcrepo.dto.DatastreamVersion;
import org.dataconservancy.archive.impl.fcrepo.dto.DublinCore;
import org.dataconservancy.archive.impl.fcrepo.dto.DublinCoreElement;
import org.dataconservancy.archive.impl.fcrepo.dto.FedoraDigitalObject;
import org.dataconservancy.archive.impl.fcrepo.dto.XMLContent;
import org.dataconservancy.archive.impl.fcrepo.xstream.FDOXStreamFactory;
import org.dataconservancy.model.dcp.Dcp;

/**
 *
 */
public class FdoConverterTest extends AbstractXstreamConverterTest {

    //private static final String VALID_FDO_XML =
    //        "/dcstest_aee427823745835eeb75ec8f53e5d8a4_file.xml";
    private static final String VALID_FDO_XML =
        "/dcstest_c1963e23b744457a18aa6e69bbe5fd2e_du.xml";

    private Dcp sip;

    @Before
    public void setUp() {
        super.setUp();

      // TODO It would be more efficient to set up the XStream parser once.
        
    }

    @Test
    public void testRoundtrip() {

        // Known FDO as XML.
        InputStream fdoXML = getStream(VALID_FDO_XML);
        
        // Processes the XML for the FDOs that correspond to the DCS entities.
        XStream x = FDOXStreamFactory.newInstance();

        // Convert the test FDO.
        FedoraDigitalObject fdo = (FedoraDigitalObject)x.fromXML(fdoXML);
        //System.out.println("FDO Test: " + fdo.getObjectPid());

        Assert.assertEquals("Found incorrect PID", "dcstest:c1963e23b744457a18aa6e69bbe5fd2e", fdo.getObjectPid());
        //Assert.assertEquals("Found incorrect PID", "dcstest:aee427823745835eeb75ec8f53e5d8a4", fdo.getObjectPid());
        Assert.assertEquals("Unexpected number of datastreams present", 5, fdo.getObjectProperties().getPropertyMap().size());

        HashMap<String, Datastream> dsMap = fdo.getDatastreamMap();
        Assert.assertEquals("Unexpected number of datastreams present", 3, dsMap.size());
        
        // Test the Dublin Core Datastream et. al.
        Datastream dcds = dsMap.get("DC");
        Assert.assertNotNull("Dublin core datastream not present", dcds);
        DatastreamVersion dcdsv = dcds.getVersionList().get(0);
        Assert.assertNotNull("Dublin core datastream version not present", dcdsv);
        XMLContent dcxml = (XMLContent)dcdsv.getContent();
        Assert.assertNotNull("XML content container not present", dcdsv);        
        DublinCore dc = (DublinCore)dcxml.getContent();
        Assert.assertNotNull("Dublin Core container not present", dc);
        ArrayList<DublinCoreElement> list = dc.getElementList();
        Assert.assertEquals("Number of Dublin Core Element wrong", 12, list.size());
        //for (DublinCoreElement e : list) {
        //    System.out.println("dc: key - " + e.getName() + " value - " + e.getValue());
        //}
        
        //System.out.println("Starting Roundtrip Marshall");
        String fdoAsXML = x.toXML(fdo);
        // TODO Check for exception
        // TODO Check it matches
        //System.out.println("Roundtrip: " + fdoAsXML);
        

    }

    private InputStream getStream(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new RuntimeException("No resource found for '" + path + "'.");
        }
        return stream;
    }

}

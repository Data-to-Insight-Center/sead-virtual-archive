/*
 * Copyright 2014 The Trustees of Indiana University
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

package org.seadva.registry.mapper;

import com.sun.jersey.test.framework.JerseyTest;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dspace.foresite.*;
import org.junit.Test;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;

import java.io.*;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;

/**
 * Mapper test cases
 */
public class OreDBMapperTest extends JerseyTest {

    public OreDBMapperTest() throws Exception {
        super("org.seadva.registry.mapper");
    }

    @Test
    public void testMapperForORE() throws IOException, InvalidXmlException, ClassNotFoundException, OREParserException, OREException, URISyntaxException, ORESerialiserException {

        OreDBMapper oreDBMapper =  new OreDBMapper("http://localhost:8080/registry/rest/");
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
//        ResourceMap rem = parser.parse(OreDBMapperTest.class.getResourceAsStream("./Vortex2_Visualization_oaiore.xml"));

//        oreDBMapper.mapfromOre(rem);
        ResourceMap returnedRem = oreDBMapper.toORE(
                "http://localhost:8080/sead-wf/entity/2021"
        );
        FileWriter oreStream = new FileWriter("/tmp/output_oaiore.xml");
        BufferedWriter ore = new BufferedWriter(oreStream);

        String resourceMapXml = "";
        ORESerialiser serial = ORESerialiserFactory.getInstance(RESOURCE_MAP_SERIALIZATION_FORMAT);
        ResourceMapDocument doc = serial.serialise(returnedRem);
        resourceMapXml = doc.toString();

        ore.write(resourceMapXml);
        ore.close();
//        assertEquals(sip.getDeliverableUnits().size(), returnedSip.getDeliverableUnits().size());
    }

    private static final String RESOURCE_MAP_SERIALIZATION_FORMAT = "RDF/XML";
}

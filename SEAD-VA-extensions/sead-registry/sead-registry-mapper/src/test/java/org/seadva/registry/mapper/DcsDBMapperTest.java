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
import org.junit.Test;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;

import static junit.framework.Assert.assertEquals;

/**
 * Mapper test cases
 */
public class DcsDBMapperTest  extends JerseyTest {

    public DcsDBMapperTest() throws Exception {
        super("org.seadva.registry.mapper");
    }

    @Test
    public void testMapperForSip() throws IOException, InvalidXmlException, ClassNotFoundException, ParseException {
        ResearchObject sip = new SeadXstreamStaxModelBuilder().buildSip(
                DcsDBMapperTest.class.getResourceAsStream("./sample.xml")
        );
        new DcsDBMapper("http://localhost:8080/registry/rest/").mapfromSip(sip);
        ResearchObject returnedSip = new DcsDBMapper("http://localhost:8080/registry/rest/").getSip(
                "http://sead-test/0489a707-d428-4db4-8ce0-1ace548bc653"
        );
        new SeadXstreamStaxModelBuilder().buildSip(returnedSip, new FileOutputStream("/tmp/output_sip.xml"));//Now you can go and check the output
        assertEquals(sip.getDeliverableUnits().size(), returnedSip.getDeliverableUnits().size());

    }

}

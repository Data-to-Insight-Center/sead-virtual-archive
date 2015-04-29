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
import org.json.JSONException;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Mapper test cases
 */
public class JsonDBMapperTest extends JerseyTest {

    public JsonDBMapperTest() throws Exception {
        super("org.seadva.registry.mapper");
    }

    @Test
    public void testMapperForJSONLD()
            throws IOException, InvalidXmlException, ClassNotFoundException,
            OREParserException, OREException, URISyntaxException, ORESerialiserException, JSONException {

        JsonDBMapper jsonDBMapper =  new JsonDBMapper("http://localhost:8080/registry/rest/");
        String json = jsonDBMapper.toJSONLD(
                "http://localhost:8080/sead-wf/entity/2020"
        );


        System.out.println(json);
    }

    private static final String RESOURCE_MAP_SERIALIZATION_FORMAT = "RDF/XML";
}

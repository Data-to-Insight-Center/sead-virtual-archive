/*
 * Copyright 2013 The Trustees of Indiana University
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

package org.seadva.registry.service;


import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.seadva.registry.impl.registry.BaseDaoImpl;
import org.seadva.registry.impl.registry.SeadRegistry;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.net.URLEncoder;

import static org.junit.Assert.assertEquals;


public class RegistryTest extends JerseyTest {

    public RegistryTest() throws Exception {
        super("org.seadva.registry.service");

    }

    @Before
    public void init() throws IllegalAccessException, InstantiationException {
        ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext(new String[] {
                        "testContext.xml"});
        ResourceService.seadRegistry = new SeadRegistry((BaseDaoImpl) appContext.getBean("registry"));
        ResourceService.seadRegistry.init();
    }

    @Test
    public void testGetCollection() throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path(
                        URLEncoder.encode(
                                "http://seada-test/test_coll_id"
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }



    @Test
    public void testPutContainer() throws IOException {

        WebResource webResource = resource();

        String jsonString =

                "{\"entity\":{\"entity_id\":\"http://seada-test/test_coll_id\",\"entity_name\":\"Test Collection\"},\"entityType\":{\"http://seada-test/test_coll_id\":[],\"http:/seadva/format_id\":[],\"http://seada-test/test_file_id\":[]},\"properties\":{\"http://seada-test/test_coll_id\":[{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":2,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":3,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":4,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":5,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":6,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":7,\"name\":\"title\",\"valueStr\":\"Test Title\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":8,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":9,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":10,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":11,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":12,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":13,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":14,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":15,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":16,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":17,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":18,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":19,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":23,\"name\":\"Creator\",\"valueStr\":\"Test creator\"}],\"http:/seadva/format_id\":[{\"entity_id\":\"http:/seadva/format_id\",\"property_id\":20,\"name\":\"formatType\",\"valueStr\":\"IANA\"},{\"entity_id\":\"http:/seadva/format_id\",\"property_id\":21,\"name\":\"formatValue\",\"valueStr\":\"MP-4\"},{\"entity_id\":\"http:/seadva/format_id\",\"property_id\":24,\"name\":\"formatType\",\"valueStr\":\"IANA\"},{\"entity_id\":\"http:/seadva/format_id\",\"property_id\":25,\"name\":\"formatValue\",\"valueStr\":\"MP-4\"}],\"http://seada-test/test_file_id\":[{\"entity_id\":\"http://seada-test/test_file_id\",\"property_id\":22,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_file_id\",\"property_id\":26,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"}]},\"aggregations\":{\"http://seada-test/test_coll_id\":[{\"parent_id\":\"http://seada-test/test_coll_id\",\"child_id\":\"http://seada-test/test_file_id\"}],\"http://seada-test/test_file_id\":[]},\"relations\":{\"http://seada-test/test_coll_id\":[],\"http:/seadva/format_id\":[],\"http://seada-test/test_file_id\":[{\"cause_id\":\"http://seada-test/test_file_id\",\"relation\":\"http://purl.org/dc/terms/hasFormat\",\"effect_id\":\"http:/seadva/format_id\"}]},\"childEntities\":{\"http:/seadva/format_id\":[{\"entity_id\":\"http:/seadva/format_id\",\"entity_name\":\"mp4\"}],\"http://seada-test/test_file_id\":[{\"entity_id\":\"http://seada-test/test_file_id\",\"entity_name\":\"Test File\"}]}}";


        File file = new File("./test.txt");
        if(!file.exists())
            file.createNewFile();
        PrintWriter out = new PrintWriter(file.getAbsolutePath());

        out.print(jsonString);
        out.close();


        FileDataBodyPart fdp = new FileDataBodyPart("file", file,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.bodyPart(fdp);

        ClientResponse response = webResource.path("resource")
                .path("putCol")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, formDataMultiPart);

        assertEquals(response.getStatus(),200);
    }

}
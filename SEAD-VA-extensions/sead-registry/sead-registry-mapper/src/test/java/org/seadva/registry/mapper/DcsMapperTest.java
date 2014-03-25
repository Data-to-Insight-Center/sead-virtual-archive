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

import com.google.gson.Gson;
import com.sun.jersey.test.framework.JerseyTest;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.junit.Test;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.seadva.registry.api.ResourceType;
import org.seadva.registry.impl.resource.ContainerResource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Mapper test cases
 */
public class DcsMapperTest  extends JerseyTest {

    DcsMapper dcsMapper;
    public DcsMapperTest() throws Exception {
        super("org.seadva.registry.mapper");
        dcsMapper = new DcsMapper();
        dcsMapper.init();

    }

    @Test
    public void testMapFromJson(){
        String gsonString =

             "{\"entity\":{\"entity_id\":\"http://seada-test/test_coll_id\",\"entity_name\":\"Test Collection\"},\"entityType\":{\"http://seada-test/test_coll_id\":[],\"http:/seadva/format_id\":[],\"http://seada-test/test_file_id\":[]},\"properties\":{\"http://seada-test/test_coll_id\":[{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":2,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":3,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":4,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":5,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":6,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":7,\"name\":\"title\",\"valueStr\":\"Test Title\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":8,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":9,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":10,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":11,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":12,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":13,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":14,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":15,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":16,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":17,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":18,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":19,\"name\":\"Creator\",\"valueStr\":\"Test creator\"},{\"entity_id\":\"http://seada-test/test_coll_id\",\"property_id\":23,\"name\":\"Creator\",\"valueStr\":\"Test creator\"}],\"http:/seadva/format_id\":[{\"entity_id\":\"http:/seadva/format_id\",\"property_id\":20,\"name\":\"formatType\",\"valueStr\":\"IANA\"},{\"entity_id\":\"http:/seadva/format_id\",\"property_id\":21,\"name\":\"formatValue\",\"valueStr\":\"MP-4\"},{\"entity_id\":\"http:/seadva/format_id\",\"property_id\":24,\"name\":\"formatType\",\"valueStr\":\"IANA\"},{\"entity_id\":\"http:/seadva/format_id\",\"property_id\":25,\"name\":\"formatValue\",\"valueStr\":\"MP-4\"}],\"http://seada-test/test_file_id\":[{\"entity_id\":\"http://seada-test/test_file_id\",\"property_id\":22,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"},{\"entity_id\":\"http://seada-test/test_file_id\",\"property_id\":26,\"name\":\"Creator\",\"valueStr\":\"File Test creator\"}]},\"aggregations\":{\"http://seada-test/test_coll_id\":[{\"parent_id\":\"http://seada-test/test_coll_id\",\"child_id\":\"http://seada-test/test_file_id\"}],\"http://seada-test/test_file_id\":[]},\"relations\":{\"http://seada-test/test_coll_id\":[],\"http:/seadva/format_id\":[],\"http://seada-test/test_file_id\":[{\"cause_id\":\"http://seada-test/test_file_id\",\"relation\":\"http://purl.org/dc/terms/hasFormat\",\"effect_id\":\"http:/seadva/format_id\"}]},\"childEntities\":{\"http:/seadva/format_id\":[{\"entity_id\":\"http:/seadva/format_id\",\"entity_name\":\"mp4\"}],\"http://seada-test/test_file_id\":[{\"entity_id\":\"http://seada-test/test_file_id\",\"entity_name\":\"Test File\"}]}}";
        ResearchObject sip = dcsMapper.map(gsonString, ResourceType.CONTAINER);
        assertTrue(sip.getDeliverableUnits().size()>0);
    }

    @Test
    public void testMapToJson() throws InvalidXmlException {
        ResearchObject sip = new SeadXstreamStaxModelBuilder().buildSip(getClass().getResourceAsStream("./sample_sip.xml"));
        assertNotNull(dcsMapper.map(sip, ResourceType.CONTAINER));
    }

}

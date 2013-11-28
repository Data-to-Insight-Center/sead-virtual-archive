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
package org.dataconservancy.ui.model.builder.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.test.support.model.BaseModelTest;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Writer;

import static org.junit.Assert.assertEquals;

/**
 * A unit test for the AdiAjaxDataItem converter.  This test is focused on creating
 * JSON output for the ADI AJAX web service.  Unmarshalling is not tested as the
 * JsonHierarchicalStreamDriver does not support unmarshalling JSON.
 */
public class AdiAjaxDataItemConverterTest extends BaseModelTest {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    private XStream theXstream;

    private String JSON;


    public void setupJSON() {

        JSON =
                "{\n" +
                "  \"@id\": \"" + dataItemOne.getId() + "\",\n" +
                "  \"name\": \"" + dataItemOne.getName() + "\",\n" +
                "  \"description\": \"" + dataItemOne.getDescription() + "\",\n" +
                "  \"depositor\": {\n" +
                "    \"@ref\": \"id:adminUser\"\n" +
                "  },\n" +
                "  \"depositDate\": {\n" +
                "    \"date\": \"4/24/12 12:00:00 AM\"\n" +
                "  },\n" +
                "  \"files\": [\n" +
                "    {\n" +
                "      \"@id\": \"" + dataFileOne.getId() + "\",\n" +
                "      \"parentId\": \"" + dataItemOne.getId() + "\",\n" +
                "      \"source\": \"" + dataFileOne.getSource() + "\",\n" +
                "      \"name\": \"" + dataFileOne.getName() + "\",\n" +
                "      \"path\": \"" + dataFileOne.getPath().replace("\\", "\\\\") + "\",\n" +
                "      \"fileSize\": \"" + dataFileOne.getSize() + "\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"@id\": \"" + dataFileTwo.getId() + "\",\n" +
                "      \"parentId\": \"" + dataItemTwo.getId() + "\",\n" +
                "      \"source\": \"" + dataFileTwo.getSource() + "\",\n" +
                "      \"name\": \"" + dataFileTwo.getName() + "\",\n" +
                "      \"path\": \"" + dataFileTwo.getPath().replace("\\", "\\\\") + "\",\n" +
                "      \"fileSize\": \"" + dataFileTwo.getSize() + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    @Before
    public void setUp() throws Exception {

        theXstream = new XStream( new JsonHierarchicalStreamDriver() {
            public HierarchicalStreamWriter createWriter(Writer writer) {
                return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
            }
        });
        theXstream.setMode(XStream.NO_REFERENCES);
        theXstream.registerConverter(new AdiAjaxDataItemConverter());
        theXstream.registerConverter(new DataFileConverter());
        theXstream.registerConverter(new AdiAjaxDateTimeConverter());
        setupJSON();
        dataItemOne.setDepositorId(admin.getId());
        dataItemOne.getFiles().add(dataFileTwo);
    }

    @Test
    public void testMarshal() throws IOException, SAXException {
        String actualXML = theXstream.toXML(dataItemOne);
       // System.out.println("Expected XML: " + JSON);
       // System.out.println("Actual XML: " + actualXML);
        assertEquals(JSON, actualXML);
    }

}

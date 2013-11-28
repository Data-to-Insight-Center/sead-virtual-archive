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
package org.dataconservancy.dcs.access.ui.client.model;

import com.google.gwt.junit.client.GWTTestCase;

// TODO  Really test out the model

public class GwtTestModel
        extends GWTTestCase {

    public String getModuleName() {
        return "org.dataconservancy.dcs.access.ui.Application";
    }

    public void test() {
        String dcptext =
                "{\n"
                        + "  \"deliverableUnits\": [\n"
                        + "    {\n"
                        + "      \"id\": \"http://127.0.0.1:44697/access/entity/org.dataconservancy.model.dcs.DcsDeliverableUnit,0\",\n"
                        + "      \"collections\": [\n"
                        + "      ],\n"
                        + "      \"metadata\": [\n"
                        + "        {\n"
                        + "          \"schemaUri\": \"sgpypoocks\",\n"
                        + "          \"metadata\": \"<root><eljx>zv</eljx><eljx ik='jdhjk'>vhyevhdrhl</eljx><elai>tt</elai><elai tm='iwkaz'>hautabsmgl</elai><elzy>nz</elzy><elzy mp='xmnpf'>usomsraufr</elzy></root>\",\n"
                        + "          \"metadataUsedByHashcode\": \"<root><eljx>zv</eljx><eljxik=jdhjk>vhyevhdrhl</eljx><elai>tt</elai><elaitm=iwkaz>hautabsmgl</elai><elzy>nz</elzy><elzymp=xmnpf>usomsraufr</elzy></root>\"\n"
                        + "        }\n"
                        + "      ],\n"
                        + "      \"metadataRefs\": [\n"
                        + "      ],\n"
                        + "      \"relations\": [\n"
                        + "      ],\n"
                        + "      \"formerExternalRefs\": [\n"
                        + "        \"tcknphwxes\",\n"
                        + "        \"bzhrajkhcf\"\n"
                        + "      ],\n"
                        + "      \"parents\": [\n"
                        + "      ],\n"
                        + "      \"isDigitalSurrogate\": false,\n"
                        + "      \"coreMd\": {\n"
                        + "        \"creators\": [\n"
                        + "          \"xzmzv\"\n"
                        + "        ],\n"
                        + "        \"subjects\": [\n"
                        + "          \"terw\"\n"
                        + "        ],\n"
                        + "        \"rights\": {\n"
                        + "           \"description\": \"this is a rights statement\"\n"
                        + "        },\n"
                        + "        \"type\": \"fftz\",\n"
                        + "        \"title\": \"xxcrt\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"collections\": [\n"
                        + " {\n"
                        + "      \"id\": \"http://127.0.0.1:44697/access/entity/org.dataconservancy.model.dcs.DcsCollection,3\",\n"
                        + "      \"coreMd\": {\n"
                        + "        \"creators\": [\n"
                        + "          \"biwt\",\n"
                        + "          \"xlrw\",\n"
                        + "          \"thhk\",\n"
                        + "          \"sera\"\n"
                        + "        ],\n"
                        + "        \"subjects\": [\n"
                        + "          \"nkxf\",\n"
                        + "          \"cxbf\",\n"
                        + "          \"yelg\",\n"
                        + "          \"mgpg\"\n"
                        + "        ],\n"
                        + "        \"type\": \"vrsd\",\n"
                        + "        \"title\": \"tjrdgjklfe pkwzrnxani evfzjniqin\"\n"
                        + "      },\n"
                        + "      \"metadata\": [\n"
                        + "      ],\n"
                        + "      \"metadataRef\": [\n"
                        + "        {\n"
                        + "          \"ref\": \"hzpfmnyfgq\"\n"
                        + "        }\n"
                        + "      ]\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"manifestations\": [\n"
                        + "    {\n"
                        + "      \"id\": \"http://127.0.0.1:44697/access/entity/org.dataconservancy.model.dcs.DcsManifestation,1\",\n"
                        + "      \"deliverableUnit\": \"http://test.dataconservancy.org/0\",\n"
                        + "      \"metadata\": [\n"
                        + "        {\n"
                        + "          \"metadata\": \"<root><elkf>pv</elkf><elkf tz='iaacj'>mmagssvjbc</elkf><elpp>ks</elpp><elpp tg='fuqza'>vninfrearp</elpp><elho>vj</elho><elho em='lxvvx'>ibbuaywyel</elho></root>\",\n"
                        + "          \"metadataUsedByHashcode\": \"<root><elkf>pv</elkf><elkftz=iaacj>mmagssvjbc</elkf><elpp>ks</elpp><elpptg=fuqza>vninfrearp</elpp><elho>vj</elho><elhoem=lxvvx>ibbuaywyel</elho></root>\"\n"
                        + "        }\n"
                        + "      ],\n"
                        + "      \"manifestationFiles\": [\n"
                        + "        {\n"
                        + "          \"path\": \"lhsmplgluh\",\n"
                        + "          \"ref\": {\n"
                        + "            \"ref\": \"http://test.dataconservancy.org/2\"\n"
                        + "          }\n"
                        + "        }\n"
                        + "      ],\n"
                        + "      \"technicalEnvironment\": [\n"
                        + "      ],\n"
                        + "      \"metadataRef\": [\n"
                        + "      ]\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"files\": [\n"
                        + "    {\n"
                        + "      \"id\": \"http://127.0.0.1:44697/access/entity/org.dataconservancy.model.dcs.DcsFile,4\",\n"
                        + "      \"source\": \"hgnbqbdwqc\",\n"
                        + "      \"extant\": true,\n"
                        + "      \"valid\": false,\n"
                        + "      \"sizeBytes\": 3568090783224100494,\n"
                        + "      \"fixity\": [\n"
                        + "        {\n"
                        + "          \"algorithm\": \"fleixxzvwy\"\n"
                        + "        }\n"
                        + "      ],\n"
                        + "      \"formats\": [\n"
                        + "        {\n"
                        + "          \"format\": \"xmdkexjbdv\",\n"
                        + "          \"name\": \"qcwsedaqrj\",\n"
                        + "          \"version\": \"sxeocjqeax\"\n"
                        + "        }\n"
                        + "      ],\n"
                        + "      \"metadata\": [\n"
                        + "      ],\n"
                        + "      \"metadataRef\": [\n"
                        + "        {\n"
                        + "          \"ref\": \"hzpfmnyfgq\"\n"
                        + "        }\n"
                        + "      ]\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"events\": [\n"
                        + "  {\n"
                        + "      \"id\": \"http://127.0.0.1:44697/access/entity/org.dataconservancy.model.dcs.DcsEvent,2\",\n"
                        + "      \"date\": \"2010-07-26T15:37:25.421Z\",\n"
                        + "      \"detail\": \"zkropzlsrr chgwpyobpm rkcbzguxgy smfgduwrlj gtdtfflemb\",\n"
                        + "      \"targets\": [\n" + "      ]\n" + "    }\n"
                        + "  ]\n" + "}";

        JsDcp dcp = (JsDcp) JsModel.parseJSON(dcptext);

        assertEquals(1, dcp.getDeliverableUnits().length());
        assertEquals(1, dcp.getManifestations().length());
        assertEquals(1, dcp.getCollections().length());
        assertEquals(1, dcp.getEvents().length());
        assertEquals(1, dcp.getFiles().length());
        assertEquals("this is a rights statement", dcp.getDeliverableUnits().get(0).getCoreMd().getRights());
    }
}

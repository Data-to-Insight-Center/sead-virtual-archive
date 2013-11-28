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
package org.dataconservancy.dcs.ingest.services;

import java.io.ByteArrayOutputStream;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;

public class LabellerTest {

    private static final String EXAMPLE_SIP =
            "/org/dataconservancy/dcs/ingest/services/ManyRelationships.xml";

    private DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    private static IdService idService = new MemoryIdServiceImpl();

    private static BulkIdCreationService bulkIdService = (BulkIdCreationService) idService;

    private static SipStager sipStager = new MemoryStager();

    private static EventManager eventMgr;

    private static IngestFramework fwk = new IngestFramework();

    private static Labeller labeller = new Labeller();

    @BeforeClass
    public static void init() {

        InlineEventManager iem = new InlineEventManager();
        iem.setIdService(bulkIdService);
        iem.setSipStager(sipStager);
        eventMgr = iem;

        fwk.setEventManager(eventMgr);
        fwk.setSipStager(sipStager);

        labeller.setIngestFramework(fwk);
        labeller.setIdentifierService(idService);
        labeller.setBulkIdService(bulkIdService);
        labeller.setIdPrefix("http://dataconservancy.org");
    }

    /* Tests that all local/temporary IDs are replaced */
    @Test
    public void comprehensiveReplacmentTest() throws Exception {

        final String TEMPORARY_ID = "\"example:/";

        Assert.assertTrue(IOUtils.toString(this.getClass()
                .getResourceAsStream(EXAMPLE_SIP)).contains(TEMPORARY_ID));

        String id =
                fwk.getSipStager().addSIP(builder.buildSip(this.getClass()
                        .getResourceAsStream(EXAMPLE_SIP)));

        labeller.execute(id);

        ByteArrayOutputStream content = new ByteArrayOutputStream();
        builder.buildSip(sipStager.getSIP(id), content);
        String resultDcp = new String(content.toByteArray(), "UTF-8");

        Assert.assertFalse("There is at least one temporary id still present!",
                           resultDcp.contains(TEMPORARY_ID));
    }

    /* Make sure relationships to existing objects aren't replaced */
    @Test
    public void existingRelsNotMangledTest() throws Exception {
        final String EXISTING_REL = "\"http://dataconservancy.org/existing/";

        int existingRelCount =
                strcount(EXISTING_REL, IOUtils.toString(this.getClass()
                        .getResourceAsStream(EXAMPLE_SIP)));
        Assert.assertTrue(existingRelCount > 0);
        String id =
                fwk.getSipStager().addSIP(builder.buildSip(this.getClass()
                        .getResourceAsStream(EXAMPLE_SIP)));

        labeller.execute(id);
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        builder.buildSip(sipStager.getSIP(id), content);
        String resultDcp = new String(content.toByteArray(), "UTF-8");

        Assert.assertEquals("Existing rel count has changed!",
                            existingRelCount,
                            strcount(EXISTING_REL, resultDcp));
    }

    /* Make sure inline metadata is not damaged */
    @Test
    public void inlineMetadataNotMangledTest() throws Exception {
        final String INLINE_MD = "INLINE_MD";

        int inlineMdCount =
                strcount(INLINE_MD, IOUtils.toString(this.getClass()
                        .getResourceAsStream(EXAMPLE_SIP)));
        Assert.assertTrue(inlineMdCount > 0);

        String id =
                fwk.getSipStager().addSIP(builder.buildSip(this.getClass()
                        .getResourceAsStream(EXAMPLE_SIP)));

        ByteArrayOutputStream content = new ByteArrayOutputStream();
        builder.buildSip(sipStager.getSIP(id), content);
        String resultDcp = new String(content.toByteArray(), "UTF-8");

        Assert.assertEquals("Existing rel count has changed!",
                            inlineMdCount,
                            strcount(INLINE_MD, resultDcp));
    }

    private int strcount(String needle, String haystack) {
        Pattern p = Pattern.compile(needle);
        Matcher m = p.matcher(haystack);
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }
}

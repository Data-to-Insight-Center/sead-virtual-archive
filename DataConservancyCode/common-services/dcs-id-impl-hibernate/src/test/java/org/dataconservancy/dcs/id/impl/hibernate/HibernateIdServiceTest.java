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
package org.dataconservancy.dcs.id.impl.hibernate;

import java.io.File;
import java.io.PrintWriter;

import java.net.URL;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.dcs.id.api.Types;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.springframework.context.support.ClassPathXmlApplicationContext;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HibernateIdServiceTest {

    private static PrefixableIdService idService;

    @BeforeClass
    public static void setUp() {
        ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext("applicationContext.xml");
        idService = (PrefixableIdService) appContext.getBean("testIdService");

    }

    @Test
    public void uidAssignmentTest() {
        Identifier id1 = idService.create("type");
        Identifier id2 = idService.create("type");

        assertTrue(!id1.getUid().equals(id2.getUid()));
    }

    @Test
    public void uidRetrievalTest() throws Exception {
        Identifier id = idService.create("type");
        Identifier retrieved = idService.fromUid(id.getUid());

        assertEquals(id.getUid(), retrieved.getUid());
        assertEquals(id, retrieved);
    }

    @Test
    public void urlRetrievalTest() throws Exception {
        Identifier id = idService.create("Lineage");
        Identifier retrieved = idService.fromUrl(id.getUrl());
        assertEquals(id.getUrl(), retrieved.getUrl());
        assertEquals(id, retrieved);
    }

    @Test
    public void uidUniquenessTest() throws Exception {
        Identifier lineageId = idService.create(Types.LINEAGE.getTypeName());
        Identifier entityId = idService.create(Types.DELIVERABLE_UNIT.getTypeName());

        assertFalse(lineageId.getUid().equals(entityId.getUid()));
    }

    @Test
    public void typeIsPersistentTest() throws Exception {
        final String TYPE = "myType";

        Identifier id = idService.create(TYPE);
        Identifier retrieved = idService.fromUrl(id.getUrl());

        assertEquals(TYPE, retrieved.getType());
    }

    @Test(expected = IdentifierNotFoundException.class)
    public void nonexistentUidTest() throws IdentifierNotFoundException {
        idService.fromUid("4096");
    }

    @Test(expected = IdentifierNotFoundException.class)
    public void nonexistentNonNumberUidTest()
            throws IdentifierNotFoundException {
        idService.fromUid(UUID.randomUUID().toString());
    }

    @Test(expected = IdentifierNotFoundException.class)
    public void nonexistentUrlTest() throws Exception {
        idService.fromUrl(new URL(idService.getUrlPrefix() + "4906"));
    }

    @Test(expected = IdentifierNotFoundException.class)
    public void nonexistentNonNumericUrlTest() throws Exception {
        idService.fromUrl(new URL(idService.getUrlPrefix() + "XYZ"));
    }

    @Test(expected = IdentifierNotFoundException.class)
    public void nonexistentIncorrectPrefixUrlTest() throws Exception {
        String prefix = idService.getUrlPrefix() + "different";
        idService.fromUrl(new URL(prefix + "10"));
    }

    @Test
    @Ignore("These tests are meant to be run standalone to test performance characteristics of the IdService impls.")
    public void perfTest() throws Exception {
        PrintWriter p = new PrintWriter(new File("/tmp/perf.out"));
        long overallStart = System.currentTimeMillis();
        final int total = 1000000;
        for (int i = 0; i < total; i++) {
//            long start = new Date().getTime();
            Identifier x = idService.create("type");
//            long end = new Date().getTime();
//            p.println(String.format("%s %s", idService.fromUid(x.getUid())
//                    .getUid(), end - start));
        }
        p.println("Generated " + total + " ids in " + (System.currentTimeMillis() - overallStart) + "ms ");
        p.close();
    }

    @Test
    @Ignore("These tests are meant to be run standalone to test performance characteristics of the IdService impls.")
    public void bulkPerfTest() throws Exception {
        PrintWriter p = new PrintWriter(new File("/tmp/bulkperf.out"));
        long overallStart = System.currentTimeMillis();
        int step = 100;
        int batchCount = 0;
        final int total = 1000000;
        for (int i = 0; i < total; i = i + step) {
//            long start = new Date().getTime();
            List<Identifier> ids = ((BulkIdCreationService)idService).create(step, "type");
//            long end = new Date().getTime();
//            for (Identifier x : ids) {
//                p.println(String.format("%s %s %s", batchCount, idService.fromUid(x.getUid())
//                        .getUid(), end - start));
//            }
            batchCount++;
        }
        p.println("Generated " + total + " ids in " + (System.currentTimeMillis() - overallStart) + "ms in " + batchCount + " batches of " + step);
        p.close();
    }

    @Test
    @Ignore("These tests are meant to be run standalone to test performance characteristics of the IdService impls.")
    public void bulkPerfTest2() throws Exception {
        PrintWriter p = new PrintWriter(new File("/tmp/bulkperf2.out"));
        long overallStart = System.currentTimeMillis();
        int step = 1000;
        int batchCount = 0;
        final int total = 1000000;
        for (int i = 0; i < total; i = i + step) {
//            long start = new Date().getTime();
            List<Identifier> ids = ((BulkIdCreationService) idService).create(step, "type");
//            long end = new Date().getTime();
//            for (Identifier x : ids) {
//                p.println(String.format("%s %s %s", batchCount, idService.fromUid(x.getUid())
//                        .getUid(), end - start));
//            }
            batchCount++;
        }
        p.println("Generated " + total + " ids in " + (System.currentTimeMillis() - overallStart) + "ms in " + batchCount + " batches of " + step);
        p.close();
    }

}


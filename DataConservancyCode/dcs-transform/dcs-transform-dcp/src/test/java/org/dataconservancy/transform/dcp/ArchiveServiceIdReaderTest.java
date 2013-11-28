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
package org.dataconservancy.transform.dcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.transform.Reader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;

public class ArchiveServiceIdReaderTest {

    private static String dcsHome;

    private static ConfigurableApplicationContext appContext;

    private static ArchiveStore archive;

    private static DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    @BeforeClass
    public static void setUp() {
        dcsHome = System.getProperty("dcs.home");

        System.setProperty("dcs.home", createTempDir().toString());

        appContext =
                new ClassPathXmlApplicationContext(new String[] {"classpath*:org/dataconservancy/config/applicationContext.xml"});

        archive =
                (ArchiveStore) appContext
                        .getBean("org.dataconservancy.archive.api.ArchiveStore");

    }

    @AfterClass
    public static void tearDown() throws Exception {
        appContext.close();
        System.out.println(System.getProperty("dcs.home"));
        FileUtils.deleteDirectory(new File(System.getProperty("dcs.home")));

        if (dcsHome != null) {
            System.setProperty("dcs.home", dcsHome);
        }
    }

    @Test
    public void testCorrectNumberOfEntities() throws Exception {

        for (int i = 0; i < 3; i++) {
            Dcp dcp = Util.newDcp();
            archive.putPackage(toStream(dcp));
        }

        Iterator<String> entities = archive.listEntities(null);
        int archiveCount = 0;
        while (entities.hasNext()) {
            entities.next();
            archiveCount++;
        }

        Reader<String, String> reader = new ArchiveServiceIdReader(archive);

        int readCount = 0;
        while (reader.nextKeyValue()) {
            readCount++;
            System.out.println(readCount);
        }

        assertEquals(archiveCount, readCount);
    }

    @Test
    public void testIdenticalEntities() throws Exception {
        for (int i = 0; i < 3; i++) {
            Dcp dcp = Util.newDcp();
            archive.putPackage(toStream(dcp));
        }

        Reader<String, String> reader = new ArchiveServiceIdReader(archive);

        while (reader.nextKeyValue()) {
            Dcp fetched =
                    builder.buildSip(archive.getPackage(reader.getCurrentKey()));
            
            for (DcsEntity e : fetched) {
                assertEquals(e.getId(), reader.getCurrentValue());
            }
        }
    }

    private InputStream toStream(Dcp dcp) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        builder.buildSip(dcp, out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private static File createTempDir() {
        File dir =
                new File(System.getProperty("java.io.tmpdir"), UUID
                        .randomUUID().toString());
        if (!dir.mkdirs()) {
            throw new RuntimeException("Could not create file");
        }

        return dir;
    }
}

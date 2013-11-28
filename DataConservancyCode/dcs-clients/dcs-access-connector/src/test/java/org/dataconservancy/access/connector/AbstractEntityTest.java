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
package org.dataconservancy.access.connector;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.junit.BeforeClass;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public abstract class AbstractEntityTest {
    final static Set<DcsEntity> allTestEntities = new HashSet<DcsEntity>();
    final static Map<String, DcsEntity> allTestEntitiesById = new HashMap<String, DcsEntity>();
    final static ResourceLoader rl = new DefaultResourceLoader();
    final static DcsModelBuilder mb = new DcsXstreamStaxModelBuilder();
    static File entitiesDir;

    @BeforeClass
    public static void getAllTestEntities() throws IOException, InvalidXmlException {
        final String entitiesResource = "/entities";
        final Resource entities = rl.getResource(entitiesResource);
        assertNotNull(entities);
        entitiesDir = entities.getFile();
        assertTrue(entitiesDir.exists() && entitiesDir.canRead() && entitiesDir.isDirectory());
        Collection<File> files = FileUtils.listFiles(entities.getFile(), new String[]{"xml"}, false);
        for (File f : files) {
            Dcp dcp = mb.buildSip(new FileInputStream(f));
            allTestEntities.addAll(dcp.getCollections());
            allTestEntities.addAll(dcp.getDeliverableUnits());
            allTestEntities.addAll(dcp.getEvents());
            allTestEntities.addAll(dcp.getFiles());
            allTestEntities.addAll(dcp.getManifestations());
        }

        for (DcsEntity e : allTestEntities) {
            allTestEntitiesById.put(e.getId(), e);
        }
    }
}

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
import org.dataconservancy.dcs.access.impl.solr.RandomEntityBuilder;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 */
public class FsRandomEntityBuilder {

    public static void main(String [] args) {

        if (args[0] == null) {
            System.err.println("Please specify an existing directory where the generated entities should be stored.");
            System.exit(1);
        }

        final File entityDir = new File(args[0]);

        if (!entityDir.exists()) {
            System.err.println("Entity directory " + entityDir + " doesn't exist.  Please create.");
            System.exit(1);
        }

        if (FileUtils.listFiles(entityDir, new String[]{".xml"}, false).size() > 0) {
            System.err.println("Entity directory " + entityDir + " already contains XML files.  Please remove" +
                    "existing files before continuing.");
            System.exit(1);
        }

        buildEntities(entityDir);
    }

    private static void buildEntities(File basedir) {
        // Code borrowed from DcsSolrTest#testSolrMapping
        final DcsModelBuilder mb = new DcsXstreamStaxModelBuilder();
        final RandomEntityBuilder rb = new RandomEntityBuilder(System.currentTimeMillis());
        final List<DcsEntity> entities = new ArrayList<DcsEntity>();
        final Random rand = new Random();

        for (int i = 0; i < 7; i++) {
            String colparent = null;

            if (entities.size() > 0 && rand.nextInt(10) == 0) {
                Collections.shuffle(entities);

                for (DcsEntity entity : entities) {
                    if (entity instanceof DcsCollection) {
                        colparent = entity.getId();
                    }
                }
            }

            final DcsCollection col = rb.createCollection(colparent);
            entities.add(col);

            final int numdu = 5;

            for (int j = 0; j < numdu; j++) {
                final DcsDeliverableUnit du = rb.createDeliverableUnit(col.getId());
                entities.add(du);

                final List<DcsFile> files = new ArrayList<DcsFile>();
                entities.add(rb.createManifestation(du.getId(), files));
                entities.addAll(files);
            }
        }

        final int numevent = 5;

        for (int i = 0; i < numevent; i++) {
            entities.add(rb.createEvent());
        }

        for (DcsEntity entity : entities) {
            final Dcp sip = new Dcp();
            final FileOutputStream out;
            final File file = new File(basedir, entity.getId().substring(entity.getId().lastIndexOf("/")+1) + ".xml");
            try {
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                System.err.println("Unable to write entity XML file " + file);
                continue;
            }

            System.out.println("Generating entity and serializing to " + file);

            try {
                if (entity instanceof DcsCollection) {
                    sip.addCollection((DcsCollection) entity);
                } else if (entity instanceof DcsEvent) {
                    sip.addEvent((DcsEvent) entity);
                } else if (entity instanceof DcsFile) {
                    sip.addFile((DcsFile) entity);
                } else if (entity instanceof DcsManifestation) {
                    sip.addManifestation((DcsManifestation) entity);
                } else if (entity instanceof DcsDeliverableUnit) {
                    sip.addDeliverableUnit((DcsDeliverableUnit) entity);
                } else {
                    // unknown entity type
                }

                mb.buildSip(sip, out);
            } catch (Exception e) {
                System.err.println("Unable to serialize DCS entity: " + e.getMessage());
                continue;
            }
        }
    }
}

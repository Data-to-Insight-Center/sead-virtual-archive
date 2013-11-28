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
package org.dataconservancy.archive.impl.elm;

import java.io.InputStream;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;

import static org.dataconservancy.archive.api.EntityType.COLLECTION;
import static org.dataconservancy.archive.api.EntityType.DELIVERABLE_UNIT;
import static org.dataconservancy.archive.api.EntityType.EVENT;
import static org.dataconservancy.archive.api.EntityType.FILE;
import static org.dataconservancy.archive.api.EntityType.MANIFESTATION;

/**
 * Experimental extended DIP logic
 */
public class ExtendedFullDipTreeLogic
        implements DipLogic {

    private DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    @Override
    public Set<String> extractEntities(String id,
                                       InputStream content,
                                       Metadata md,
                                       boolean initial) {

        if (md.getType().equals(COLLECTION.toString())) {
            return getCollectionEntities(id, content, md, initial);
        } else if (md.getType().equals(DELIVERABLE_UNIT.toString())) {
            return getDeliverableUnitEntities(id, content, md, initial);
        } else if (md.getType().equals(MANIFESTATION.toString())) {
            return getManifestationEntities(id, content, md, initial);
        } else if (md.getType().equals(EVENT.toString())) {
            return getEventEntities(id, content, md, initial);
        } else if (md.getType().equals(FILE.toString())) {
            return getFileEntities(id, content, md, initial);
        }

        return new HashSet<String>();
    }

    private Set<String> getCollectionEntities(String entityId,
                                              InputStream content,
                                              Metadata md,
                                              boolean initial) {
        Set<String> entities = new HashSet<String>();

        /* selected collection is relevant */
        entities.add(entityId);

        try {
            DcsCollection coll = builder.buildCollection(content);

            /* A collection's parent is strongly related */
            if (coll.getParent() != null) {
                entities.add(coll.getParent().getRef());
            }

            /* A collection's metadata is significantly related */
            for (DcsEntityReference ref : coll.getMetadataRef()) {
                entities.add(ref.getRef());
            }
        } catch (InvalidXmlException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                content.close();
            } catch (Exception e) {
            }
        }

        /* A collections events are strongly related, if it is the first */
        addRelated(md, entities, EVENT);

        return entities;
    }

    private Set<String> getDeliverableUnitEntities(String entityId,
                                                   InputStream content,
                                                   Metadata md,
                                                   boolean initial) {
        Set<String> entities = new HashSet<String>();

        entities.add(entityId);

        try {
            DcsDeliverableUnit du = builder.buildDeliverableUnit(content);

            /* A DUs parents are always strongly related */
            for (DcsEntityReference ref : du.getParents()) {
                entities.add(ref.getRef());
            }

            /* A Du's metadata files are strongly related, if it is the first */
            if (initial) {
                for (DcsEntityReference ref : du.getMetadataRef()) {
                    entities.add(ref.getRef());
                }
            }

            /* A DUs collections are strongly related */
            for (DcsEntityReference ref : du.getCollections()) {
                entities.add(ref.getRef());
            }
        } catch (InvalidXmlException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                content.close();
            } catch (Exception e) {
            }
        }

        /* Events and manifestations are strongly related, if it is the first */
        if (initial) {
            addRelated(md, entities, EVENT, MANIFESTATION);
        }

        return entities;
    }

    private Set<String> getManifestationEntities(String entityId,
                                                 InputStream content,
                                                 Metadata md,
                                                 boolean initial) {
        Set<String> entities = new HashSet<String>();
        entities.add(entityId);

        try {
            DcsManifestation man = builder.buildManifestation(content);

            /* A manifestation's DU is significantly related */
            if (man.getDeliverableUnit() != null) {
                entities.add(man.getDeliverableUnit());
            }

            /*
             * A manifestation's metadata files are strongly related
             */
            for (DcsEntityReference ref : man.getMetadataRef()) {
                entities.add(ref.getRef());
            }

            /* A manifestation's files are strongly related */
            for (DcsManifestationFile f : man.getManifestationFiles()) {
                entities.add(f.getRef().getRef());
            }

        } catch (InvalidXmlException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                content.close();
            } catch (Exception e) {
            }
        }

        /* A manifestation's events are significantly related */
        addRelated(md, entities, EVENT);

        return entities;
    }

    private Set<String> getEventEntities(String entityId,
                                         InputStream content,
                                         Metadata md,
                                         boolean initial) {
        Set<String> entities = new HashSet<String>();

        /* An entity is significantly related to itself */
        entities.add(entityId);
        return entities;
    }

    private Set<String> getFileEntities(String entityId,
                                        InputStream content,
                                        Metadata md,
                                        boolean initial) {
        Set<String> entities = new HashSet<String>();

        /* An entity is significantly related to itself */
        entities.add(entityId);

        try {
            DcsFile file = builder.buildFile(content);

            /*
             * A file's metadata files are strongly related
             */
            for (DcsEntityReference ref : file.getMetadataRef()) {
                entities.add(ref.getRef());
            }
        } catch (InvalidXmlException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                content.close();
            } catch (Exception e) {
            }
        }

        /* A file's events are significantly related */
        addRelated(md, entities, EVENT);

        /*
         * if first, a file's manifestations or described objects are
         * significantly related
         */
        if (initial) {
            addRelated(md,
                       entities,
                       MANIFESTATION,
                       COLLECTION,
                       DELIVERABLE_UNIT,
                       FILE);
        }

        return entities;
    }

    private void addRelated(Metadata md,
                            Set<String> entities,
                            EntityType... types) {
        for (Map.Entry<String, String> l : md.getLinks().entrySet()) {
            for (EntityType type : types) {
                if (l.getValue().equals(type.toString())) {
                    entities.add(l.getKey());
                }
            }
        }
    }
}

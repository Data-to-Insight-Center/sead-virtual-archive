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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;

import static org.dataconservancy.archive.api.EntityType.COLLECTION;
import static org.dataconservancy.archive.api.EntityType.DELIVERABLE_UNIT;
import static org.dataconservancy.archive.api.EntityType.EVENT;
import static org.dataconservancy.archive.api.EntityType.FILE;
import static org.dataconservancy.archive.api.EntityType.MANIFESTATION;

/** Finds all outward links of a given entity. */
public class ComprehensiveLinkFinder
        implements LinkFinder {

    public Map<String, Map<String, String>> getOutboundLinks(DcsEntity entity) {

        if (entity instanceof DcsCollection) {
            return toEntityMap(entity.getId(),
                               COLLECTION,
                               getCollectionRels(entity));
        } else if (entity instanceof DcsDeliverableUnit) {
            return toEntityMap(entity.getId(),
                               DELIVERABLE_UNIT,
                               getDeliverableUnitRefs(entity));
        } else if (entity instanceof DcsEvent) {
            return toEntityMap(entity.getId(), EVENT, getEventRefs(entity));
        } else if (entity instanceof DcsFile) {
            return toEntityMap(entity.getId(), FILE, getFileRefs(entity));
        } else if (entity instanceof DcsManifestation) {
            return toEntityMap(entity.getId(),
                               MANIFESTATION,
                               getManifestationRefs(entity));
        } else {
            throw new RuntimeException("Unknown dcs entity type "
                    + entity.getClass());
        }
    }

    private Map<String, Map<String, String>> toEntityMap(String id,
                                                         EntityType type,
                                                         Set<String> rels) {
        Map<String, Map<String, String>> relMap =
                new HashMap<String, Map<String, String>>();
        for (String related : rels) {
            Map<String, String> self = new HashMap<String, String>();
            self.put(id, type.toString());
            relMap.put(related, self);
        }
        return relMap;
    }

    private Set<String> getCollectionRels(DcsEntity collection) {
        Set<String> rels = new HashSet<String>();
        DcsCollection coll = (DcsCollection) collection;

        for (DcsEntityReference ref : coll.getMetadataRef()) {
            rels.add(ref.getRef());
        }

        if (coll.getParent() != null) {
            rels.add(coll.getParent().getRef());
        }
        return rels;
    }

    private Set<String> getDeliverableUnitRefs(DcsEntity deliverableUnit) {
        Set<String> rels = new HashSet<String>();
        DcsDeliverableUnit du = (DcsDeliverableUnit) deliverableUnit;

        for (DcsEntityReference ref : du.getCollections()) {
            rels.add(ref.getRef());
        }

        for (DcsEntityReference ref : du.getMetadataRef()) {
            rels.add(ref.getRef());
        }

        for (DcsEntityReference ref : du.getParents()) {
            rels.add(ref.getRef());
        }

        for (DcsRelation ref : du.getRelations()) {
            rels.add(ref.getRef().getRef());
        }
        return rels;
    }

    private Set<String> getEventRefs(DcsEntity event) {
        Set<String> rels = new HashSet<String>();
        DcsEvent e = (DcsEvent) event;

        for (DcsEntityReference ref : e.getTargets()) {
            rels.add(ref.getRef());
        }

        return rels;
    }

    private Set<String> getFileRefs(DcsEntity file) {
        Set<String> rels = new HashSet<String>();

        DcsFile f = (DcsFile) file;

        for (DcsEntityReference ref : f.getMetadataRef()) {
            rels.add(ref.getRef());
        }
        return rels;
    }

    private Set<String> getManifestationRefs(DcsEntity manifestation) {
        Set<String> rels = new HashSet<String>();
        DcsManifestation m = (DcsManifestation) manifestation;

        for (DcsEntityReference ref : m.getMetadataRef()) {
            rels.add(ref.getRef());
        }

        for (DcsManifestationFile file : m.getManifestationFiles()) {
            rels.add(file.getRef().getRef());
        }

        if (m.getDeliverableUnit() != null) {
            rels.add(m.getDeliverableUnit());
        }
        return rels;
    }
}

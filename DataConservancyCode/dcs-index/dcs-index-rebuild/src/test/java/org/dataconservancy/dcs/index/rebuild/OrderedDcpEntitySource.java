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
package org.dataconservancy.dcs.index.rebuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;

import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;

import static org.dataconservancy.model.dcs.DcsRelationship.IS_METADATA_FOR;

/**
 * Creates a series of interrelated objects, in dependency order.
 * <p>
 * The objects produced by this class are designed to represent different
 * structures of related objects in the DCS, with emphasis on those structures
 * for with the DCS indexes perform some "special" action (e.g. the presence of
 * a relationship in one DCS entity implies changes to the index documents of
 * another). This class returns generated entities in dependency order via
 * {@link #getEntities(Class)}, or as a series of self-consistent DCP packages
 * (again, in dependenceny order) via {@link #getPackages()}.
 * {@link #getPackages()} in particular is designed to emulate the kinds of SIPs
 * that would be observed during the ingest process.
 * </p>
 * Dcs entity patterns created by this source include:
 * <ul>
 * <li>Hierarchies of collections</li>
 * <li>Hierarchies of collection-like DeliverableUnits</li>
 * <li>DeliverableUnits with multiple DeliverableUnit parents</li>
 * <li>DeliverableUnits in multiple collections</li>
 * <li>Events attached to multiple entities</li>
 * <li>Metadata within a DU describing that DU via IS_METADATA_FOR</li>
 * <li>Metadata within one DU describing another DU or collection via
 * IS_METADATA_FOR</li>
 * <li>A File folfilling the role of metadata for another File within the same
 * manifestation</li>
 * <li>A DeliverableUnit with multiple manifestations</li>
 * <li>A Du or Collection directly reference a metadata file via the old Y1P
 * "metadata ref=" element</li>
 * </ul>
 */
public class OrderedDcpEntitySource {

    private MultiMap<String, DcsEntity> parentChildMap =
            new MultiHashMap<String, DcsEntity>();

    private MultiMap<String, DcsFile> metadataFilesMap =
            new MultiHashMap<String, DcsFile>();

    private Map<String, DcsEntity> entities =
            new LinkedHashMap<String, DcsEntity>();

    private List<Dcp> packages = new ArrayList<Dcp>();

    /* Contains all metadata DUs */
    private static final String COLLECTION_OF_METADATA = "coll_of_md";

    /* Contains all metadata DUs */
    private static final String DU_COLLECTION_OF_METADATA = "du_coll_of_md";

    private boolean oldStyleMetadataRefs = false;

    private final MetadataFactory metadata;

    private final File dataDir;

    public void setUseOldStyleMetadataRefs(boolean useOldStyle) {
        oldStyleMetadataRefs = useOldStyle;
    }

    public OrderedDcpEntitySource(int depth, File dataDir) {
        metadata = new MetadataFactory();
        metadata.setDirectory(dataDir);
        this.dataDir = dataDir;
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        init(depth);
    }

    public Collection<DcsEntity> getDescendantsOf(String entity) {
        return getDescendantsOf(entity, 0);
    }

    public Collection<DcsEntity> getDescendantsOf(String entity, int level) {
        Set<DcsEntity> ancestors = new HashSet<DcsEntity>();

        if (parentChildMap.containsKey(entity)) {
            for (DcsEntity child : parentChildMap.get(entity)) {
                ancestors.add(child);
                ancestors.addAll(getDescendantsOf(child.getId(), level + 1));
            }
        }

        return ancestors;
    }

    @SuppressWarnings("unchecked")
    public <T extends DcsEntity> List<T> getEntities(Class<T> type) {
        List<T> results = new ArrayList<T>();

        for (DcsEntity entity : entities.values()) {
            if (type == null || type.isAssignableFrom(entity.getClass())) {
                results.add((T) entity);
            }
        }
        return results;
    }

    public List<Dcp> getPackages() {
        return packages;
    }

    public Collection<DcsFile> getMetadataFilesFor(String entity) {
        if (metadataFilesMap.containsKey(entity)) {
            return metadataFilesMap.get(entity);
        } else {
            return new ArrayList<DcsFile>();
        }
    }

    public MetadataFactory getMetadataFactory() {
        return metadata;
    }

    private void init(int depth) {
        Dcp pkg = new Dcp();
        createCollection(COLLECTION_OF_METADATA, null, pkg);
        createContainerDU(DU_COLLECTION_OF_METADATA,
                          new String[] {},
                          new String[] {},
                          pkg);
        packages.add(pkg);

        for (int level = 0; level < depth; level++) {
            populate(level, level == depth - 1);
        }
    }

    private void populate(int level, boolean leaf) {
        List<String> collectionIds = getIdsForLevel(level, DcsCollection.class);
        List<String> duIds = getIdsForLevel(level, DcsDeliverableUnit.class);

        for (int i = 0; i < collectionIds.size(); i++) {
            DcsCollection collection =
                    createCollection(collectionIds.get(i),
                                     getParentOf(collectionIds.get(i)),
                                     null);

            String duId = duIds.get(i);
            String duParent = getParentOf(duId);
            if (leaf) {
                createDataDU(duId, new String[] {collection.getId(),
                        COLLECTION_OF_METADATA}, new String[] {duParent,
                        DU_COLLECTION_OF_METADATA});
            } else {
                createContainerDU(duId,
                                  new String[] {collection.getId()},
                                  new String[] {duParent},
                                  null);
            }
        }
    }

    private DcsCollection createCollection(String id, String parent, Dcp usepkg) {
        DcsCollection collection = new DcsCollection();

        Dcp pkg = new Dcp();
        if (usepkg != null) {
            pkg = usepkg;
        }

        collection.setId(id);
        collection.setTitle("Collection " + id);
        collection.setType(id + ", "
                + (parent != null ? parent : "parent_coll"));
        collection.addCreator(this.getClass().toString());
        collection.addSubject(id);

        if (parent != null) {
            collection.setParent(new DcsCollectionRef(parent));
            parentChildMap.put(parent, collection);
        }

        if (oldStyleMetadataRefs) {
            DcsEntity[] oldStyleMetadataEntities =
                    createMetadataFor(collection, true);
            collection
                    .addMetadataRef(new DcsMetadataRef(oldStyleMetadataEntities[0]
                            .getId()));
            addTo(pkg, oldStyleMetadataEntities);
        }

        entities.put(id, collection);

        /* Create metadata, and an event */
        DcsEntity[] metadataEntities = createMetadataFor(collection);
        DcsEvent event =
                createEvent("createCollection", metadataEntities, collection);

        addTo(pkg, collection, event);
        addTo(pkg, metadataEntities);

        if (usepkg == null) {
            packages.add(pkg);
        }
        return collection;
    }

    private DcsEntity[] createMetadataFor(DcsEntity entity, boolean... oldStyle) {

        DcsDeliverableUnit metadataDU = new DcsDeliverableUnit();
        metadataDU.setTitle("MetadataDU");
        metadataDU.setId("metadata_du_for_" + entity.getId());
        metadataDU.addCollection(new DcsCollectionRef(COLLECTION_OF_METADATA));
        metadataDU
                .addParent(new DcsDeliverableUnitRef(DU_COLLECTION_OF_METADATA));
        entities.put(metadataDU.getId(), metadataDU);

        DcsFile file = createMetadataFileDescribing(entity);

        DcsManifestation manifestation;
        MultiMap<DcsFile, DcsEntity> metadataFor =
                new MultiHashMap<DcsFile, DcsEntity>();

        if (oldStyle.length == 0) {
            /* use IS_METADATA_FOR relationships */
            metadataFor.put(file, entity);
            manifestation = createManifestation(metadataDU, metadataFor);
        } else {
            /* Suitable for old-style direct reference from containing entity */
            manifestation = createManifestation(metadataDU, metadataFor, file);
        }

        addChldrenTo(COLLECTION_OF_METADATA, metadataDU);
        addChldrenTo(DU_COLLECTION_OF_METADATA, metadataDU);
        addChldrenTo(metadataDU, manifestation);
        addChldrenTo(manifestation, file);

        return new DcsEntity[] {file, metadataDU, manifestation};
    }

    private DcsManifestation createManifestation(DcsDeliverableUnit du,
                                                 MultiMap<DcsFile, DcsEntity> metadataFiles,
                                                 DcsFile... otherFiles) {
        DcsManifestation manifestation = new DcsManifestation();
        manifestation.setId("manifestation_for_" + du.getId() + "."
                + UUID.randomUUID());
        manifestation.setDeliverableUnit(du.getId());
        manifestation.setDateCreated(DateUtility.toIso8601(new Date()));
        manifestation.addTechnicalEnvironment("env1");
        manifestation.addTechnicalEnvironment("env2");

        for (DcsFile file : otherFiles) {
            DcsManifestationFile mf = new DcsManifestationFile();
            mf.setPath("content/");
            mf.setRef(new DcsFileRef(file.getId()));
            manifestation.addManifestationFile(mf);
        }

        for (Map.Entry<DcsFile, Collection<DcsEntity>> metadataMapping : metadataFiles
                .entrySet()) {
            DcsManifestationFile mf = new DcsManifestationFile();
            mf.setPath("metadata/");
            mf.setRef(new DcsFileRef(metadataMapping.getKey().getId()));

            for (DcsEntity entity : metadataMapping.getValue()) {
                mf.addRel(new DcsRelation(IS_METADATA_FOR, entity.getId()));
                metadataFilesMap.put(entity.getId(), metadataMapping.getKey());
            }

            manifestation.addManifestationFile(mf);
        }

        entities.put(manifestation.getId(), manifestation);
        return manifestation;
    }

    private DcsEvent createEvent(String eventType,
                                 DcsEntity[] targets,
                                 DcsEntity... more) {
        DcsEvent event = new DcsEvent();
        event.setId("event_" + eventType + "_" + UUID.randomUUID().toString());

        event.setDate(DateUtility.toIso8601(new Date()));
        event.setEventType(eventType);
        event.setDetail(event.getId());
        event.setOutcome(targets.toString());

        for (DcsEntity entity : targets) {
            event.addTargets(new DcsEntityReference(entity.getId()));
        }

        entities.put(event.getId(), event);

        return event;
    }

    private DcsFile createMetadataFileDescribing(DcsEntity object) {

        DcsFile file = metadata.forEntity(object).toFile();

        entities.put(file.getId(), file);

        return file;
    }

    private DcsDeliverableUnit createContainerDU(String id,
                                                 String[] parentCollections,
                                                 String[] parentDUs,
                                                 Dcp usepkg) {

        DcsDeliverableUnit du = new DcsDeliverableUnit();

        Dcp pkg = new Dcp();
        if (usepkg != null) {
            pkg = usepkg;
        }

        du.setId(id);

        du.addCreator("me");
        du.addCreator("you");
        du.addFormerExternalRef("nothing");
        du.addSubject("on " + id);
        du.setTitle("DU " + id);
        du.setType(DcsDeliverableUnit.class.toString());
        du.setRights("you have no rights");

        for (String parentColl : parentCollections) {
            if (parentColl != null) {
                du.addCollection(new DcsCollectionRef(parentColl));
                parentChildMap.put(parentColl, du);
            }
        }

        for (String parentDU : parentDUs) {
            if (parentDU != null) {
                du.addParent(new DcsDeliverableUnitRef(parentDU));
                parentChildMap.put(parentDU, du);
            }
        }

        entities.put(id, du);

        /* Create metadata, and an event */
        DcsEntity[] metadataEntities = createMetadataFor(du);
        DcsEvent event = createEvent("createContainerDU", metadataEntities, du);

        addTo(pkg, du, event);
        addTo(pkg, metadataEntities);

        if (usepkg == null) {
            packages.add(pkg);
        }
        return du;
    }

    private void createDataDU(String id,
                              String[] parentCollections,
                              String[] parentDUs) {
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        Dcp pkg = new Dcp();

        du.setId(id);
        du.setTitle("data DU " + id);

        for (String parentColl : parentCollections) {
            if (parentColl != null) {
                du.addCollection(new DcsCollectionRef(parentColl));
                parentChildMap.put(parentColl, du);
            }
        }

        for (String parentDU : parentDUs) {
            if (parentDU != null) {
                du.addParent(new DcsDeliverableUnitRef(parentDU));
                parentChildMap.put(parentDU, du);
            }
        }

        entities.put(id, du);

        /* Create a manifesation containing metadata describing this DU */
        DcsFile duMetadataFile = createMetadataFileDescribing(du);
        MultiHashMap<DcsFile, DcsEntity> duMetadataFileMap =
                new MultiHashMap<DcsFile, DcsEntity>();
        duMetadataFileMap.put(duMetadataFile, du);
        DcsManifestation duMetadataManifestation =
                createManifestation(du, duMetadataFileMap);

        /*
         * Create a manifestation containing a data file, and a metadata file
         * describing it
         */
        DcsFile dataFile = createDataFile();
        DcsFile dataMetadataFile = createMetadataFileDescribing(dataFile);
        MultiHashMap<DcsFile, DcsEntity> dataMetadataFileMap =
                new MultiHashMap<DcsFile, DcsEntity>();
        dataMetadataFileMap.put(dataMetadataFile, dataFile);
        DcsManifestation dataManifestation =
                createManifestation(du, dataMetadataFileMap, dataFile);

        /*
         * Create a manifestation containing three metadata files: one
         * describing all parent collections, one describing all parent DU
         * collections, and one describing all parents together
         */
        DcsFile collectionsMdFile =
                createMetadataFileDescribing(entities.get(parentCollections[0]));
        DcsFile duCollectionsMdFile =
                createMetadataFileDescribing(entities.get(parentDUs[0]));
        DcsFile comprehensiveMdFile = createMetadataFileDescribing(du);
        MultiHashMap<DcsFile, DcsEntity> collectionsMetadataFileMap =
                new MultiHashMap<DcsFile, DcsEntity>();
        for (String collectionParent : parentCollections) {
            collectionsMetadataFileMap.put(collectionsMdFile,
                                           entities.get(collectionParent));
            collectionsMetadataFileMap.put(comprehensiveMdFile,
                                           entities.get(collectionParent));

        }
        for (String duParent : parentDUs) {
            collectionsMetadataFileMap.put(duCollectionsMdFile,
                                           entities.get(duParent));
            collectionsMetadataFileMap.put(comprehensiveMdFile,
                                           entities.get(duParent));
        }
        DcsManifestation collectionManifestation =
                createManifestation(du, collectionsMetadataFileMap);

        /* Finally, create an event for the whole darn thing */
        DcsEvent event =
                createEvent("createDataDU", new DcsEntity[] {du,
                        duMetadataManifestation, duMetadataFile, dataFile,
                        dataMetadataFile, dataManifestation, collectionsMdFile,
                        duCollectionsMdFile, comprehensiveMdFile,
                        collectionManifestation});

        addChldrenTo(du,
                     duMetadataManifestation,
                     dataManifestation,
                     collectionManifestation);

        addChldrenTo(duMetadataManifestation, duMetadataFile);
        addChldrenTo(dataManifestation, dataFile, dataMetadataFile);
        addChldrenTo(collectionManifestation,
                     collectionsMdFile,
                     duCollectionsMdFile,
                     comprehensiveMdFile);

        addTo(pkg,
              du,
              duMetadataManifestation,
              duMetadataFile,
              dataFile,
              dataMetadataFile,
              dataManifestation,
              collectionsMdFile,
              duCollectionsMdFile,
              comprehensiveMdFile,
              collectionManifestation,
              event);

        packages.add(pkg);

    }

    private String getParentOf(String id) {
        String[] parts = id.split("_");

        String prefix = parts[0];
        int level = new Integer(parts[1]);
        int number = new Integer(parts[2]);

        if (level > 0) {
            return String.format("%s_%s_%s",
                                 prefix,
                                 level - 1,
                                 (int) Math.floor(number / 2));
        } else {
            return null;
        }
    }

    private List<String> getIdsForLevel(int level,
                                        Class<? extends DcsEntity> type) {
        List<String> ids = new ArrayList<String>();

        for (int i = 0; i < Math.pow(2, level); i++) {
            ids.add(String.format("%s_%s_%s", getPrefix(type), level, i));
        }

        return ids;
    }

    private String getPrefix(Class<? extends DcsEntity> type) {

        if (type.equals(DcsDeliverableUnit.class)) {
            return "du";
        } else if (type.equals(DcsCollection.class)) {
            return "coll";
        } else {
            throw new RuntimeException("Unexpected type");
        }
    }

    private DcsFile createDataFile() {
        OutputStream out = null;
        File f = null;
        try {
            f =
                    new File(dataDir, String.format("data.%s.dat", UUID
                            .randomUUID().toString()));
            out = new FileOutputStream(f);
            out.write("data".getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        DcsFormat format = new DcsFormat();
        format.setSchemeUri("www.iana.org/assignments/media-types/");
        format.setName("text/plain");

        DcsFile file = new DcsFile();
        file.setId("data_file_" + UUID.randomUUID().toString());
        file.setSizeBytes(f.length());
        file.setSource(f.toURI().toString());
        file.setExtant(true);
        file.setName("data.xml");
        file.addFormat(format);

        entities.put(file.getId(), file);

        return file;
    }

    public DcsEntity getEntity(String id) {
        return entities.get(id);
    }

    public static void addTo(Dcp pkg, DcsEntity... entities) {
        for (DcsEntity entity : entities) {
            if (entity instanceof DcsCollection) {
                pkg.addCollection((DcsCollection) entity);
            } else if (entity instanceof DcsDeliverableUnit) {
                pkg.addDeliverableUnit((DcsDeliverableUnit) entity);
            } else if (entity instanceof DcsManifestation) {
                pkg.addManifestation((DcsManifestation) entity);
            } else if (entity instanceof DcsFile) {
                pkg.addFile((DcsFile) entity);
            } else if (entity instanceof DcsEvent) {
                pkg.addEvent((DcsEvent) entity);
            }
        }
    }

    private void addChldrenTo(DcsEntity parent, DcsEntity... children) {
        addChldrenTo(parent.getId(), children);
    }

    private void addChldrenTo(String parent, DcsEntity... children) {
        for (DcsEntity child : children) {
            if (!(child instanceof DcsEvent))
                parentChildMap.put(parent, child);
        }
    }
}

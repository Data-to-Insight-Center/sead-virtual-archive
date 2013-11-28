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
package org.dataconservancy.dcs.index.dcpsolr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;

/**
 * Create entities filled with random values.
 */
public class RandomEntityBuilder {

    private final Random rand;

    private int nextid;

    public RandomEntityBuilder() {
        this.nextid = 0;
        this.rand = new Random();
    }

    public RandomEntityBuilder(long seed) {
        this.nextid = 0;
        this.rand = new Random(seed);
    }

    /**
     * Create Dcp package with some relations between entities.
     * 
     * @param numcol
     *            Number of collections to create
     * @param numdu
     *            Number of du to create in each collection
     * @return package
     */

    public Dcp createDcp(int numcol, int numdu) {
        Dcp dcp = new Dcp();

        for (int i = 0; i < numcol; i++) {
            DcsCollection col = createCollection(null);

            // Randomly add parents to collection

            if (rand.nextBoolean()) {
                for (DcsCollection parent : dcp.getCollections()) {
                    if (rand.nextBoolean()) {
                        col.setParent(new DcsCollectionRef(parent.getId()));
                        break;
                    }
                }
            }

            dcp.addCollection(col);
            addEvents(dcp, col);

            for (int j = 0; j < numdu; j++) {
                DcsDeliverableUnit du = createDeliverableUnit(col.getId(),
                        null, false);

                addEvents(dcp, du);

                // Randomly add parents to du

                if (rand.nextBoolean()) {
                    for (DcsDeliverableUnit parent : dcp.getDeliverableUnits()) {
                        if (rand.nextBoolean()) {
                            du.addParent(new DcsDeliverableUnitRef(parent
                                    .getId()));
                            break;
                        }else {
                            du.addAlternateId(createAlternateId());
                        }
                    }
                }

                dcp.addDeliverableUnit(du);

                List<DcsFile> files = new ArrayList<DcsFile>();

                dcp.addFile(files.toArray(new DcsFile[] {}));

                for (DcsFile file : files) {
                    addEvents(dcp, file);
                    if( rand.nextBoolean()){
                        file.addAlternateId(createAlternateId());
                    }
                }

                DcsManifestation man = createManifestation(du.getId(), files);
                dcp.addManifestation(man);
                addEvents(dcp, man);
            }
        }

        return dcp;
    }

    private void addEvents(Dcp dcp, DcsEntity target) {
        int numevents = rand.nextInt(2);

        for (int i = 0; i < numevents; i++) {
            dcp.addEvent(createEvent(target.getId()));
        }
    }

    public DcsEvent createEvent(String target) {
        DcsEvent event = new DcsEvent();

        event.setId(nextid());
        event.setDate(DateUtility.toIso8601(DateUtility.now()));
        event.setOutcome(randomString(20, false));
        event.setEventType(randomString(2, false));
        event.setDetail(randomText(5));
        event.setTargets(createEnityReferences(rand.nextInt(2)));

        return event;
    }
    
    public DcsResourceIdentifier createAlternateId(){
        DcsResourceIdentifier id = new DcsResourceIdentifier();
        
        id.setAuthorityId(randomString(10, true));
        id.setIdValue(randomString(20, false));
        id.setTypeId(randomString(5, true));
        
        return id;
    }

    private Set<DcsEntityReference> createEnityReferences(int size) {
        Set<DcsEntityReference> set = new HashSet<DcsEntityReference>();

        for (int i = 0; i < size; i++) {
            DcsEntityReference ref = new DcsEntityReference();
            ref.setRef(randomString(3, false));
        }

        return set;
    }

    public DcsManifestation createManifestation(String du, List<DcsFile> files) {
        DcsManifestation man = new DcsManifestation();

        man.setId(nextid());
        man.setDeliverableUnit(du);
        man.setDateCreated(DateUtility.toIso8601(DateUtility.now()));
        man.setMetadata(createMetadataSet(rand.nextInt(3)));
        man.setMetadataRef(createMetadataRefSet(rand.nextInt(2)));

        int numfiles = rand.nextInt(10);

        for (int i = 0; i < numfiles; i++) {
            DcsManifestationFile manfile = new DcsManifestationFile();
            DcsFile file = createFile();

            files.add(file);

            manfile.setRef(new DcsFileRef(file.getId()));
            manfile.setPath(randomString(10, false));

            if (rand.nextBoolean()) {
                manfile.setRelSet(createRelations(rand.nextInt(2)));
            }

            man.addManifestationFile(manfile);
        }

        return man;
    }

    public DcsFile createFile() {
        DcsFile file = new DcsFile();

        file.setExtant(rand.nextBoolean());
        file.setId(nextid());
        file.setFixity(createFixitySet(rand.nextInt(3)));
        file.setFormats(createFormatSet(rand.nextInt(2)));
        file.setName(randomString(8, false));
        file.setSizeBytes(Math.abs(rand.nextLong()));
        file.setSource(randomString(10, false));

        if (rand.nextBoolean()) {
            file.setValid(rand.nextBoolean());
        }

        file.setMetadata(createMetadataSet(rand.nextInt(2)));
        file.setMetadataRef(createMetadataRefSet(rand.nextInt(2)));

        return file;
    }

    private Set<DcsFormat> createFormatSet(int size) {
        Set<DcsFormat> set = new HashSet<DcsFormat>();

        for (int i = 0; i < size; i++) {
            DcsFormat fmt = new DcsFormat();

            fmt.setFormat(randomString(10, false));
            fmt.setName(randomString(10, false));
            fmt.setSchemeUri(randomString(10, false));
            fmt.setVersion(randomString(10, false));

            set.add(fmt);
        }

        return set;
    }

    private Set<DcsFixity> createFixitySet(int size) {
        Set<DcsFixity> set = new HashSet<DcsFixity>();

        for (int i = 0; i < size; i++) {
            DcsFixity fix = new DcsFixity();

            fix.setAlgorithm(randomString(10, false));
            fix.setValue(randomString(10, false));

            set.add(fix);
        }

        return set;
    }

    private String nextid() {
        return "http://test.dataconservancy.org/" + nextid++;
    }

    private String randomString(int length, boolean maybenull) {
        if (maybenull && rand.nextBoolean()) {
            return null;
        }

        char[] buf = new char[length];

        for (int i = 0; i < length; i++) {
            buf[i] = (char) ('a' + rand.nextInt(26));
        }

        return new String(buf);
    }

    private String randomXML(int elements) {
        StringBuilder sb = new StringBuilder();

        sb.append("<root>");

        for (int i = 0; i < elements; i++) {
            String el = "el" + randomString(2, false);

            sb.append("<" + el + ">" + randomString(2, false) + "</" + el + ">");

            sb.append("<" + el + " " + randomString(2, false) + "='"
                    + randomString(5, false) + "'>" + randomString(10, false)
                    + "</" + el + ">");
        }

        sb.append("</root>");

        return sb.toString();
    }

    private String randomText(int words) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < words; i++) {
            sb.append(randomString(10, false));

            if (i != words - 1) {
                sb.append(' ');
            }
        }

        return sb.toString();
    }

    private Set<String> randomStringSet(int size, int strlength) {
        Set<String> result = new HashSet<String>();

        for (int i = 0; i < size; i++) {
            result.add(randomString(strlength, false));
        }

        return result;
    }

    private Set<DcsMetadataRef> createMetadataRefSet(int size) {
        Set<DcsMetadataRef> result = new HashSet<DcsMetadataRef>();

        for (int i = 0; i < size; i++) {
            DcsMetadataRef ref = new DcsMetadataRef();

            ref.setRef(randomString(10, false));

            result.add(ref);
        }

        return result;
    }

    private Set<DcsCollectionRef> createCollectionRefSet(String string) {
        Set<DcsCollectionRef> result = new HashSet<DcsCollectionRef>();

        DcsCollectionRef ref = new DcsCollectionRef();
        ref.setRef(string);
        result.add(ref);

        return result;
    }

    private DcsCollectionRef createCollectionRef(String string) {
        DcsCollectionRef ref = new DcsCollectionRef();
        ref.setRef(string);

        return ref;
    }

    private Set<DcsMetadata> createMetadataSet(int size) {
        Set<DcsMetadata> result = new HashSet<DcsMetadata>();

        for (int i = 0; i < size; i++) {
            result.add(createMetaData());
        }

        return result;
    }

    public DcsDeliverableUnit createDeliverableUnit(String collection,
            String duparent, boolean randomparents) {
        DcsDeliverableUnit du = new DcsDeliverableUnit();

        du.setId(nextid());
        du.setTitle(randomString(rand.nextInt(10) + 1, false));
        du.setCreators(randomStringSet(rand.nextInt(2) + 1,
                rand.nextInt(10) + 1));
        du.setSubjects(randomStringSet(rand.nextInt(2) + 1, rand.nextInt(5) + 1));

        du.setMetadata(createMetadataSet(rand.nextInt(2)));
        du.setMetadataRef(createMetadataRefSet(rand.nextInt(3)));
        du.setFormerExternalRefs(randomStringSet(rand.nextInt(5), 10));
        du.setRights(randomString(2, false));

        if (collection != null) {
            du.setCollections(createCollectionRefSet(collection));
        }

        du.setRelations(createRelations(rand.nextInt(2)));
        du.setType(randomString(4, false));

        if (rand.nextBoolean()) {
            du.setDigitalSurrogate(rand.nextBoolean());
        }
        
        if (rand.nextBoolean()) {
            du.setLineageId(randomString(10, false));
        }

        if (duparent != null) {
            du.addParent(new DcsDeliverableUnitRef(duparent));
        }

        if (randomparents) {
            du.setParents(createDeliverableUnitRefSet(rand.nextInt(2)));
            du.setCollections(createCollectionRefSet(randomString(2, false)));
        }

        return du;
    }

   

    private Set<DcsDeliverableUnitRef> createDeliverableUnitRefSet(int size) {
        Set<DcsDeliverableUnitRef> set = new HashSet<DcsDeliverableUnitRef>();

        for (int i = 0; i < size; i++) {
            set.add(new DcsDeliverableUnitRef(randomString(3, false)));
        }

        return set;
    }

    private Set<DcsRelation> createRelations(int size) {
        Set<DcsRelation> set = new HashSet<DcsRelation>();

        for (int i = 0; i < size; i++) {
            DcsRelation rel = new DcsRelation();

            rel.setRef(new DcsEntityReference(randomString(10, false)));
            rel.setRelUri(randomString(10, false));

            set.add(rel);
        }

        return set;
    }

    private DcsMetadata createMetaData() {
        DcsMetadata md = new DcsMetadata();

        md.setSchemaUri(randomString(10, false));
        md.setMetadata(randomXML(3));

        return md;
    }

    public DcsCollection createCollection(String parent) {
        DcsCollection col = new DcsCollection();

        col.setId(nextid());
        col.setTitle(randomText(rand.nextInt(10) + 1));
        if (parent != null) {
            col.setParent(createCollectionRef(parent));
        }

        col.setMetadata(createMetadataSet(rand.nextInt(2)));
        col.setSubjects(randomStringSet(4, 4));
        col.setType(randomString(4, false));
        col.setCreators(randomStringSet(4, 4));

        return col;
    }
}

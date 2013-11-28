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
package org.dataconservancy.dcs.integration.lineage;

import java.io.File;
import java.io.FileWriter;

import java.net.URL;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.dcs.index.dcpsolr.DcpUtil;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;

public class LineageWebAppBootstrap {
    private static final String ORIGINAL_ENTITY_ID = "original_entity";
    private static final String MIDDLE_ENTITY_ID = "middle_entity";
    private static final String LATEST_ENTITY_ID = "latest_entity";
    private static final String SINGLE_ENTITY_ID = "single_entity";

    private static final String LINEAGE_ID = "lineage";
    private static final String SINGLE_LINEAGE_ID = "single_lineage";

    private final static String ORIGINAL_INGEST_DATE = "2012-01-01T10:59:59Z";

    private final static String MIDDLE_INGEST_DATE = "2012-01-08T10:59:59Z";

    private final static String LATEST_INGEST_DATE = "2012-01-25T10:59:59Z";

    private final static String SINGLE_ENTITY_INGEST_DATE = "2012-01-02T10:59:59Z";
    
    protected static void index(DcpIndexService indexService, ArchiveStore archiveStore, Dcp dcp) throws IndexServiceException, AIPFormatException {
        
        // Package must be in archive so indexing service can lookup files        
        if (archiveStore != null) {
            archiveStore.putPackage(DcpUtil.asInputStream(dcp));
        }
        
        BatchIndexer<Dcp> batch = indexService.index();
        batch.add(dcp);
        batch.close();
        
        indexService.optimize();
    }
        
    public static void seedWebApp(MockIdService mockIdService, DcpIndexService indexService, ArchiveStore archiveStore) throws Exception {
        //Add the ids to the id store
        Identifier originalEntityID = mockIdService.createIdentifier(Types.DELIVERABLE_UNIT.getTypeName(), ORIGINAL_ENTITY_ID);
        Identifier middleEntityID = mockIdService.createIdentifier(Types.DELIVERABLE_UNIT.getTypeName(), MIDDLE_ENTITY_ID);
        Identifier latestEntityID = mockIdService.createIdentifier(Types.DELIVERABLE_UNIT.getTypeName(), LATEST_ENTITY_ID);
        Identifier singleEntityID = mockIdService.createIdentifier(Types.DELIVERABLE_UNIT.getTypeName(), SINGLE_ENTITY_ID);
        
        Identifier lineageID = mockIdService.createIdentifier(Types.LINEAGE.getTypeName(), LINEAGE_ID);
        Identifier singleLineageID = mockIdService.createIdentifier(Types.LINEAGE.getTypeName(), SINGLE_LINEAGE_ID);
        
        //Create the entities for the lineages
        Dcp originalSip = new Dcp();
        
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(originalEntityID.getUid());
        du.setLineageId(lineageID.getUid());
        // Create a DCS File entity and content for the file.
        File dataFile = File.createTempFile("tmpfile", ".xml");
        dataFile.deleteOnExit();
        
        FileWriter w = new FileWriter(dataFile);
        w.write("<text>This file is going to be updated.</text>");
        w.close();
        
        final DcsFile file = new DcsFile();
        
        file.setId("file");
        file.setSource(new URL("file://" + dataFile.getCanonicalPath())
                .toString());
        
        // Create the Manifestation
        final DcsManifestation man = new DcsManifestation();
        man.setDeliverableUnit(du.getId());
        man.setId("man");
        
        // The Manifestation is composed of a ManifestationFile, and the DcsFile
        // created above.
        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setPath("/");
        mf.setRef(new DcsFileRef(file.getId()));
        
        // The ManifestationFile is metadata for the Manifestation's deliverable
        // unit
        final DcsRelation rel = new DcsRelation(
                DcsRelationship.IS_METADATA_FOR, man.getDeliverableUnit());
        mf.addRel(rel);
        
        man.addManifestationFile(mf);
        
        DcsEvent e = new DcsEvent();
        e.setId("DepositID");
        e.setEventType("ingest.complete");
        e.setDate(ORIGINAL_INGEST_DATE);
        e.addTargets(new DcsEntityReference(du.getId()));
        
        DcpUtil.add(originalSip, man, file, du, e);
        index(indexService, archiveStore, originalSip);
        
        //Update the entity id of the du and index the new sip.
        Dcp updateSip = new Dcp();
        du.setId(middleEntityID.getUid());
        e = new DcsEvent();
        e.setId("DepositID");
        e.setEventType("ingest.complete");
        e.setDate(MIDDLE_INGEST_DATE);
        e.addTargets(new DcsEntityReference(du.getId()));
        
        DcpUtil.add(updateSip, man, file, du, e);
        index(indexService, archiveStore, updateSip);
        
        //Update the entity id of the du and index the second update sip.
        Dcp secondUpdateSip = new Dcp();
        du.setId(latestEntityID.getUid());
        
        e = new DcsEvent();
        e.setId("DepositID");
        e.setEventType("ingest.complete");
        e.setDate(LATEST_INGEST_DATE);
        e.addTargets(new DcsEntityReference(du.getId()));
        
        DcpUtil.add(secondUpdateSip, man, file, du, e);
        index(indexService, archiveStore, secondUpdateSip);
        
        //Finally change the entity id and the lineage id and index the sip.
        //This is to ensure only the items in the lineage are returned
        Dcp newLineageSip = new Dcp();
        du.setId(singleEntityID.getUid());
        du.setLineageId(singleLineageID.getUid());
        
        e = new DcsEvent();
        e.setId("DepositID");
        e.setEventType("ingest.complete");
        e.setDate(SINGLE_ENTITY_INGEST_DATE);
        e.addTargets(new DcsEntityReference(du.getId()));
        
        DcpUtil.add(newLineageSip, man, file, du, e);
        index(indexService, archiveStore, newLineageSip);
    }    
}

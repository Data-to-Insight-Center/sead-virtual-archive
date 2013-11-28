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

import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsMetadataRef;

public class SimpleLinkValidatorTest {

    private static IdService ids = new MemoryIdServiceImpl();

    private static IngestFramework ingest = new IngestFramework();

    private static SimpleLinkValidator validator = new SimpleLinkValidator();

    @BeforeClass
    public static void setUp() {
        ingest.setSipStager(new MemoryStager());

        validator.setIngestFramework(ingest);
        validator.setIdentifierService(ids);
    }

    @Test
    public void validSipTest() throws IngestServiceException {
        Dcp sip = new Dcp();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(getValidId(Types.DELIVERABLE_UNIT.getTypeName()));

        DcsManifestation man = new DcsManifestation();
        man.setId(getValidId(Types.MANIFESTATION.getTypeName()));
        man.addMetadataRef(new DcsMetadataRef(getValidId(Types.FILE.getTypeName())));
        man.setDeliverableUnit(du.getId());
        sip.addDeliverableUnit(du);
        sip.addManifestation(man);

        validator.execute(ingest.getSipStager().addSIP(sip));

    }

    @Test(expected = IngestServiceException.class)
    public void unregisteredEntityIdTest() throws IngestServiceException {
        Dcp sip = new Dcp();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("example:/invalid!");
        sip.addDeliverableUnit(du);

        validator.execute(ingest.getSipStager().addSIP(sip));
    }

    @Test(expected = IngestServiceException.class)
    public void unregisteredLinkIdTest() throws IngestServiceException {
        Dcp sip = new Dcp();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(getValidId(Types.DELIVERABLE_UNIT.getTypeName()));
        du.addCollection(new DcsCollectionRef("example:/invalid!"));
        sip.addDeliverableUnit(du);

        validator.execute(ingest.getSipStager().addSIP(sip));
    }

    @Test(expected = IngestServiceException.class)
    public void conflictingEntityIdTest() throws IngestServiceException {
        String id = getValidId(Types.FILE.getTypeName());

        Dcp sip = new Dcp();
        DcsFile file1 = new DcsFile();
        file1.setId(id);
        file1.setName("file1");
        DcsFile file2 = new DcsFile();
        file2.setId(id);
        file2.setName("file2");
        sip.addFile(file1, file2);

        validator.execute(ingest.getSipStager().addSIP(sip));
    }

    private String getValidId(String type) {
        return ids.create(type).getUrl().toString();
    }
}

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

import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;

import static org.junit.Assert.assertTrue;

/**
 * Verifies that all IDs are unique, and all references point to an ID that is
 * already defined.
 * <p>
 * Mostly stolen from ingest services.
 * </p>
 */
public class IdRelationValidator {

    Set<String> ids = new HashSet<String>();

    public void validate(Dcp sip) {

        for (DcsEntity e : sip) {
            assertTrue(!ids.contains(e.getId()));
            ids.add(e.getId());
        }

        for (DcsCollection coll : sip.getCollections()) {

            for (DcsMetadataRef md : coll.getMetadataRef()) {
                assertTrue(ids.contains(md.getRef()));
            }

            if (coll.getParent() != null) {
                assertTrue(ids.contains(coll.getParent().getRef()));
            }
        }

        for (DcsDeliverableUnit du : sip.getDeliverableUnits()) {

            for (DcsMetadataRef md : du.getMetadataRef()) {
                assertTrue(ids.contains(md.getRef()));
            }

            for (DcsCollectionRef coll : du.getCollections()) {
                assertTrue(ids.contains(coll.getRef()));
            }

            for (DcsDeliverableUnitRef dur : du.getParents()) {
                assertTrue(dur.getRef(), ids.contains(dur.getRef()));
            }

            for (DcsRelation rel : du.getRelations()) {
                assertTrue(ids.contains(rel.getRef().getRef()));
            }
        }

        for (DcsEvent e : sip.getEvents()) {

            for (DcsEntityReference er : e.getTargets()) {
                assertTrue(ids.contains(er.getRef()));
            }
        }

        for (DcsFile f : sip.getFiles()) {

            for (DcsMetadataRef md : f.getMetadataRef()) {
                assertTrue(ids.contains(md.getRef()));
            }
        }

        for (DcsManifestation m : sip.getManifestations()) {

            for (DcsMetadataRef md : m.getMetadataRef()) {
                assertTrue(ids.contains(md.getRef()));
            }

            for (DcsManifestationFile mf : m.getManifestationFiles()) {
                assertTrue(ids.contains(mf.getRef().getRef()));

                for (DcsRelation mfr : mf.getRelSet()) {
                    assertTrue(ids.contains(mfr.getRef().getRef()));
                }
            }

            assertTrue(ids.contains(m.getDeliverableUnit()));
        }
    }
}

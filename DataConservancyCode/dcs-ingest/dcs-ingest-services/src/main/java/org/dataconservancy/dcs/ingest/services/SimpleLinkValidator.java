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

import java.net.URL;

import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.ingest.IngestFramework;
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
import org.springframework.beans.factory.annotation.Required;

/**
 * Assures that all identifiers and links contain valid dcs ids, and no two
 * entities in the sip have the same id. <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setIngestFramework(IngestFramework)}</dt>
 * <dd><b>Required</b>.</dd>
 * <dt>{@link #setIdentifierService(IdService)}</dt>
 * <dd><b>Required</b></dd>
 * </dl>
 */
public class SimpleLinkValidator
        extends IngestServiceBase
        implements IngestService {

    private IdService idService;

    @Required
    public void setIdentifierService(IdService idsvc) {
        idService = idsvc;
    }

    public void execute(String sipRef) throws IngestServiceException {
        if (isDisabled()) return;

        Set<String> ids = new HashSet<String>();

        Dcp sip = ingest.getSipStager().getSIP(sipRef);

        for (DcsCollection coll : sip.getCollections()) {
            verifyId(coll, ids);

            for (DcsMetadataRef md : coll.getMetadataRef()) {
                checkIdService(md.getRef());
            }

            if (coll.getParent() != null) {
                checkIdService(coll.getParent().getRef());
            }
        }

        for (DcsDeliverableUnit du : sip.getDeliverableUnits()) {
            verifyId(du, ids);

            for (DcsMetadataRef md : du.getMetadataRef()) {
                checkIdService(md.getRef());
            }

            for (DcsCollectionRef coll : du.getCollections()) {
                checkIdService(coll.getRef());
            }

            for (DcsDeliverableUnitRef dur : du.getParents()) {
                checkIdService(dur.getRef());
            }

            for (DcsRelation rel : du.getRelations()) {
                checkIdService(rel.getRef().getRef());
            }
        }

        for (DcsEvent e : sip.getEvents()) {
            verifyId(e, ids);

            for (DcsEntityReference er : e.getTargets()) {
                checkIdService(er.getRef());
            }
        }

        for (DcsFile f : sip.getFiles()) {
            verifyId(f, ids);

            for (DcsMetadataRef md : f.getMetadataRef()) {
                checkIdService(md.getRef());
            }
        }

        for (DcsManifestation m : sip.getManifestations()) {
            verifyId(m, ids);

            for (DcsMetadataRef md : m.getMetadataRef()) {
                checkIdService(md.getRef());
            }

            for (DcsManifestationFile mf : m.getManifestationFiles()) {
                checkIdService(mf.getRef().getRef());

                for (DcsRelation mfr : mf.getRelSet()) {
                    checkIdService(mfr.getRef().getRef());
                }
            }

            checkIdService(m.getDeliverableUnit());
        }
    }

    private void verifyId(DcsEntity entity, Set<String> ids)
            throws IngestServiceException {
        String id = entity.getId();
        checkIdService(id);

        if (ids.contains(id)) {
            throw new IngestServiceException(String.format("Duplicate entity with identifier '%s' found",
                                                           id));
        } else {
            ids.add(id);
        }
    }

    private void checkIdService(String id) throws IngestServiceException {
        try {
            idService.fromUrl(new URL(id));
        } catch (Exception e) {
            throw new IngestServiceException("Could not find id reference: "
                    + id);
        }
    }
}

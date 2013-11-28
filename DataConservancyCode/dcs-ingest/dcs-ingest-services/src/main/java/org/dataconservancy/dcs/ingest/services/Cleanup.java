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

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.dcs.DcsEvent;

public class Cleanup
        extends IngestServiceBase
        implements IngestService {

    public void execute(String sipRef) throws IngestServiceException {
        if (isDisabled()) return;

        /* Clear any locks and retire lock manager for this sip */
        if (ingest.getLockService().hasLockManager(sipRef)) {
            ingest.getLockService().getLockManager(sipRef).close();
        }
        
        for (DcsEvent event : ingest.getEventManager()
                .getEvents(sipRef, Events.FILE_RESOLUTION_STAGED)) {
            String outcome = event.getOutcome();
            String referenceUri = outcome.substring(0, outcome.indexOf(' '));
            ingest.getFileContentStager().retire(referenceUri);
        }
        ingest.getSipStager().retire(sipRef);
    }
}

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

package org.dataconservancy.dcs.ingest;

import org.springframework.beans.factory.annotation.Required;

/**
 * Bean containing core ingest components.
 */
public class IngestFramework {

    private EventManager eventMgr;

    private SipStager sipStager;

    private FileContentStager fileStager;
    
    private LockService lockService;

    @Required
    public void setEventManager(EventManager mgr) {
        eventMgr = mgr;
    }

    public EventManager getEventManager() {
        return eventMgr;
    }

    @Required
    public void setSipStager(SipStager stager) {
        sipStager = stager;
    }

    public SipStager getSipStager() {
        return sipStager;
    }

    @Required
    public void setFileContentStager(FileContentStager stager) {
        fileStager = stager;
    }

    public FileContentStager getFileContentStager() {
        return fileStager;
    }

    @Required
    public void setLockService(LockService ls) {
        lockService = ls;
    }
    
    public LockService getLockService() {
        return lockService;
    }

}

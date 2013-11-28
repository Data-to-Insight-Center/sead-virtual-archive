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

import org.dataconservancy.dcs.ingest.IngestFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Base configuration for all ingest services.
 */
public class IngestServiceBase {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected IngestFramework ingest;

    private boolean disabled = false;

    @Required
    public void setIngestFramework(IngestFramework fwk) {
        ingest = fwk;
    }

    public void setDisabled(boolean dis) {
        disabled = dis;
    }

    protected boolean isDisabled() {
        return disabled;
    }
}

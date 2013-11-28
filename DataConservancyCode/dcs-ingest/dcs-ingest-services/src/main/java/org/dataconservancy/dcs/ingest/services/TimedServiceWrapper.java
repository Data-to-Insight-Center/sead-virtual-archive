/*
 * Copyright 2013 Johns Hopkins University
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


/**
 * Wraps a delegate IngestService, and stores the time the service started, and when the service completed.
 */
public class TimedServiceWrapper implements IngestService {

    private IngestService delegate;
    private long start;
    private long end;

    public TimedServiceWrapper(IngestService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(String sipRef) throws IngestServiceException {
        start = System.currentTimeMillis();
        try {
            delegate.execute(sipRef);
            end = System.currentTimeMillis();
        } catch (IngestServiceException e) {
            end = System.currentTimeMillis();
            throw e;
        }
    }

    public long getEnd() {
        return end;
    }

    public long getStart() {
        return start;
    }
}

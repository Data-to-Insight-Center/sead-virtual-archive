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

/**
 * Represents a "logical" failure of an ingest service.
 * <p>
 * If an ingest service fails for a determinable, logical cause (say a virus was
 * found, or an http address failed to resolve), then an ingest exception should
 * be thrown by an ingest service. Typically, IngestExceptions will be logged in
 * a "normal" fashion, whereas non IngestExceptions may indicate a fault in
 * underlying code and recieve enhanced logging or scruitiny.
 * </p>
 */
public class IngestServiceException
        extends Exception {

    private static final long serialVersionUID = 1L;

    public IngestServiceException(String message) {
        super(message);
    }

    public IngestServiceException(String message, Throwable t) {
        super(message, t);
    }

    public IngestServiceException(Throwable t) {
        super(t);
    }
}

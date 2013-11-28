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
package org.dataconservancy.dcs.ingest.file.impl;

import java.io.OutputStream;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;

/**
 * Provides a dcp as the uploaded file content document.
 * <p>
 * Creates a dcp document containing the uploaded content's corresponding File
 * entity, as well as any associated events.
 * </p>
 */
public class FileContentDocument
        extends FileDocumentBase {

    private final DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    private final String fileSipId;

    private final EventManager events;

    public FileContentDocument(Dcp sip, String sipRef, EventManager mgr) {
        super(sip);
        fileSipId = sipRef;
        events = mgr;
    }

    public String getMimeType() {
        return "application/xml";
    }

    protected String getSipRef() {
        return fileSipId;
    }

    protected long getDocument(OutputStream out) {
        Dcp pkg = new Dcp();

        pkg.addFile(getFile());
        long latest = 0;

        for (DcsEvent event : events.getEvents(fileSipId)) {
            for (DcsEntityReference ref : event.getTargets()) {
                if (ref.getRef().equals(getFile().getId())) {
                    long timestamp = DateUtility.parseDate((event.getDate()));
                    pkg.addEvent(event);
                    if (timestamp > latest) {
                        latest = timestamp;
                    }
                }
            }
        }

        builder.buildSip(pkg, out);
        return latest;
    }
}

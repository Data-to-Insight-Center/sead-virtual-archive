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
package org.dataconservancy.dcs.ingest.sip;

import java.io.OutputStream;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.deposit.AbstractDepositDocument;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEvent;

/**
 * Creates a dcp document containing the full ingest SIP, plus any additional
 * events.
 */
public class DcpSipContentDocument
        extends AbstractDepositDocument {

    private final DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    private final Dcp sip;

    private final EventManager mgr;

    private final String sipRef;

    public DcpSipContentDocument(Dcp dcp, String sipId, EventManager em) {
        sip = dcp;
        mgr = em;
        sipRef = sipId;
    }

    protected long getDocument(OutputStream out) {

        long lastMod = 0;
        for (DcsEvent e : mgr.getEvents(sipRef)) {
            long date = DateUtility.parseDate(e.getDate());
            if (date > lastMod) lastMod = date;
        }
        builder.buildSip(sip, out);
        return lastMod;
    }

    public String getMimeType() {
        return "application/xml";
    }
}

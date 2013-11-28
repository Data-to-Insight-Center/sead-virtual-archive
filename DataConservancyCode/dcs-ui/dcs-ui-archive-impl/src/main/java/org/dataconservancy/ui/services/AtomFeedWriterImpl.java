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
package org.dataconservancy.ui.services;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.deposit.AtomEventStatusDocument;
import org.dataconservancy.model.dcs.DcsEvent;

import java.io.InputStream;

/**
 * In-memory implementation of {@link AtomFeedWriter}.  This delegates to the {@link AtomEventStatusDocument} class
 * from the DCS ingest core module.  This insures that the mapping of ingest events to atom feed semantics is consistent
 * with a production instance of the DCS.
 */
public class AtomFeedWriterImpl implements AtomFeedWriter {

    private EventManager em;

    /**
     * Construct an AtomFeedWriterImpl with an internal implementation of {@link EventManager}.
     */
    public AtomFeedWriterImpl() {
        this.em = new InMemoryEventManager();
    }

    /**
     * Construct an AtomFeedWriterImpl with the supplied {@link EventManager}
     *
     * @param em the event manager
     * @throws IllegalArgumentException if the supplied event manager is null
     */
    public AtomFeedWriterImpl(EventManager em) {
        if (em == null) {
            throw new IllegalArgumentException("Event manager must not be null.");
        }
        this.em = em;
    }

    @Override
    public InputStream toAtom(String depositId, DcsEvent... events) {
        AtomEventStatusDocument statusDocument = new AtomEventStatusDocument(depositId, em);
        for (DcsEvent e : events) {
            em.addEvent(depositId, e);
        }

        return statusDocument.getInputStream();
    }

}

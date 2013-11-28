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
package org.dataconservancy.dcs.ingest.deposit;

import java.io.OutputStream;

import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;

/**
 * Provides an atom feed document containing entries corresponding to dcs events.
 * <p>
 * Given a collection of events, an atom feed document will be created that
 * contains event entries in descending order of date (newest first. Events with
 * the same date will be in random order). Each atom entry corresponds to a
 * mapping of a {@link DcsEvent} onto atom entry semantics as follows:
 * <ul>
 * <li>id -> atom:id</li>
 * <li>eventType -> atom:title</li>
 * <li>eventDate -> atom:updated</li>
 * <li>eventOutcome -> atom:content</li>
 * <li>eventDetail -> atom:summary</li>
 * <li>eventTarget -> atom:link rel="related"</li>
 * <ul>
 * </p>
 */
public class AtomEventStatusDocument
        extends AbstractDepositDocument {

    private final EventManager mgr;

    private final String depositID;

    private static final Abdera abdera = new Abdera();

    private String idBase = "http://dataconservancy.org/ingest/status/";

    public AtomEventStatusDocument(String sipId, EventManager em) {
        depositID = sipId;
        mgr = em;

    }

    protected long getDocument(OutputStream out) {

        TreeMap<Long, List<DcsEvent>> eventMap =
                new TreeMap<Long, List<DcsEvent>>();
        for (DcsEvent e : mgr.getEvents(depositID)) {
            Long date = DateUtility.parseDate(e.getDate());
            if (!eventMap.containsKey(date)) {
                eventMap.put(date, new ArrayList<DcsEvent>());
            }
            eventMap.get(date).add(e);
        }

        Feed feed = abdera.getFactory().newFeed();
        try {
            feed.setId(idBase + URLEncoder.encode(depositID, "UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        feed.setTitle("Status event feed for ingest " + depositID);
        feed.setUpdated(DateUtility.toIso8601(eventMap.lastKey()));
        feed.addAuthor("DCS ingest service");

        for (Long date : eventMap.descendingKeySet()) {
            for (DcsEvent event : eventMap.get(date)) {
                feed.addEntry(getEntry(event));
            }
        }

        try {
            feed.writeTo(out);
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize "
                    + "atom status document for " + depositID, e);
        }
        return eventMap.lastKey();
    }

    private Entry getEntry(DcsEvent event) {
        Entry entry = abdera.newEntry();
        entry.setId(event.getId());
        entry.setUpdated(event.getDate());
        entry.setTitle(event.getEventType());
        if (event.getOutcome() != null) {
            entry.setContent(event.getOutcome());
        }
        if (event.getDetail() != null) {
            entry.setSummary(event.getDetail());
        }

        for (DcsEntityReference ref : event.getTargets()) {
            entry.addLink(ref.getRef(), Link.REL_RELATED);
        }
        return entry;
    }

    public String getMimeType() {
        return "application/atom+xml;type=feed";
    }
}

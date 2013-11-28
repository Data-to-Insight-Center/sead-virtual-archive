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
import org.dataconservancy.model.dcs.DcsEvent;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An {@link EventManager} which keeps track of events in an internal {@linkplain Map}.  Each instance maintains its
 * own {@code Map} of events; they are not shared between instances.  Note that the behavior of {@link #newEvent(String)}
 * does not completely obey the contract in the {@link EventManager#newEvent(String) interface}.
 * <p/>
 * This implementation is not thread-safe, and should only be used by one thread at a time.
 */
public class InMemoryEventManager implements EventManager {
    private Map<String, Set<DcsEvent>> eventMap = new HashMap<String, Set<DcsEvent>>();

    @Override
    public void addEvent(String id, DcsEvent event) {
        getEventsForId(id).add(event);
    }

    @Override
    public void addEvents(String id, Collection<DcsEvent> events) {
        getEventsForId(id).addAll(events);
    }

    @Override
    public Collection<DcsEvent> getEvents(String id, String... eventTypes) {
        Set<DcsEvent> events = getEventsForId(id);
        if (eventTypes == null || eventTypes.length == 0) {
            return events;
        }

        Set<DcsEvent> matches = new HashSet<DcsEvent>();
        for (DcsEvent e : events) {
            for (String t : eventTypes) {
                if (e.getEventType().equals(t)) {
                    matches.add(e);
                }
            }
        }

        return matches;
    }

    /**
     * {@inheritDoc}
     * <em><strong>Currently not implemented.</strong></em>  Always throws {@code UnsupportedOperationException}
     *
     * @param id
     * @param eventType
     * @throws UnsupportedOperationException always
     */
    @Override
    public DcsEvent getEventByType(String id, String eventType) {
        throw new UnsupportedOperationException("getEventByType(String, String) is not supported by this " +
                "implementation (" + this.getClass().getName() + ")");
    }

    @Override
    public DcsEvent findEventById(String eventId) {
        for (Set<DcsEvent> events : eventMap.values()) {
            if (events.contains(eventId)) {
                for (DcsEvent e : events) {
                    if (eventId.equals(e.getId())) {
                        return e;
                    }
                }
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * <em><strong>N.B.</strong></em>: this implementation does <em>not</em> set an identifier on the returned
     * {@code DcsEvent}.  It only will set the event type, and the current date time.
     *
     * @param eventType the returned event will carry this type
     * @return a new event, with the time set to the current date time, and event type set to {@code eventType}
     */
    @Override
    public DcsEvent newEvent(String eventType) {
        DcsEvent e = new DcsEvent();
        e.setDate(DateTime.now().toDateTimeISO().toString());
        e.setEventType(eventType);
        return e;
    }

    private Set<DcsEvent> getEventsForId(String id) {
        Set<DcsEvent> events;
        if (!eventMap.containsKey(id)) {
            events = new HashSet<DcsEvent>();
            eventMap.put(id, events);
        } else {
            events = eventMap.get(id);
        }

        return events;
    }
}

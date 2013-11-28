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

import java.util.Collection;

import org.dataconservancy.model.dcs.DcsEvent;

/**
 * Manages events associated with a SIP during ingest.
 * <p>
 * Depending on policy, some events may be persisted within the SIP with the
 * intent of being archived. Some may be strictly local to the ingest process.
 * The {@linkplain EventManager} is responsible for interpreting policy and
 * executing the correct action in retrieving and adding events associated with
 * a SIP.
 * </p>
 */
public interface EventManager {

    /**
     * Associate an event with a SIP.
     * <p>
     * EventManager will decide how to perisist the event.
     * </p>
     * 
     * @param id
     *        identifier of a staged SIP.
     * @param event
     *        Event to associate with the SIP.
     */
    public void addEvent(String id, DcsEvent event);

    /**
     * Associate multiple events with a SIP.
     * <p>
     * EventManager will decide how to persist the events. Adding multiple
     * events is likely to be more efficient using this method, rather than
     * multiple single addEvent operations.
     * </p>
     * 
     * @param id
     *        Identifier of a staged sip.
     * @param events
     *        Collection of events to associate with the sip.
     */
    public void addEvents(String id, Collection<DcsEvent> events);

    /**
     * Get events associated with a SIP, possibly restricted by type.
     * 
     * @param id
     *        identifier of a staged SIP which may have associated events.
     * @param eventTypes
     *        if any values are provided, the event list will be restricted by
     *        the specified types.
     * @return Collection of all relevant events.
     */
    public Collection<DcsEvent> getEvents(String id, String... eventTypes);

    /**
     * Get an event where there is expected to be exactly one instance of a
     * given type.
     * 
     * @param id
     *        identifier of a staged SIP which may have associated events.
     * @param eventType
     *        Type type of event requested
     * @return The requested event. I Will be null if not found.
     */
    public DcsEvent getEventByType(String id, String eventType);

    /**
     * Find a particular event given its identifier
     * <p>
     * Used in cases where SIP is not known, this may potentially be expensive,
     * depending on implementation.
     * </p>
     * 
     * @param eventId
     *        event identifier
     * @return DCSEvent, or null if none found.
     */
    public DcsEvent findEventById(String eventId);

    /**
     * Create a new event, not associated with any entity yet.
     * <p>
     * Pre-populates the new event with an identifier and the current datetime.
     * </p>
     * 
     * @param eventType
     *        Event type.
     * @return New event, populated with an id, event type, and date.
     */
    public DcsEvent newEvent(String eventType);
}

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

import org.dataconservancy.model.dcs.DcsEvent;

/**
 * This is an in-memory event manager which always returns {@code ingest.fail} events in response to
 * {@link org.dataconservancy.dcs.ingest.EventManager#newEvent(String)}.  This class is useful in integration tests
 * when testing failed deposits.
 */
public class AlwaysFailingEventManager extends InMemoryEventManager {

    /**
     * {@inheritDoc}
     * <em><strong>N.B.</strong></em> this implementation will always return events with the event type set to
     * "{@code ingest.fail}": the supplied {@code eventType} is ignored.
     *
     * @param eventType ignored by this implementation
     * @return returns an event with a type always set to {@code ingest.fail}
     */
    @Override
    public DcsEvent newEvent(String eventType) {
        DcsEvent e = super.newEvent(eventType);
        e.setEventType("ingest.fail");
        return e;
    }
}

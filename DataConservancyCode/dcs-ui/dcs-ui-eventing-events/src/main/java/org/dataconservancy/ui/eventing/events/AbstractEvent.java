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
package org.dataconservancy.ui.eventing.events;

import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventTopic;

/**
 * Base implementation class of Event.
 */
public abstract class AbstractEvent implements Event {

    private EventClass eventClass;

    private EventTopic eventTopic;

    private EventContext eventContext;

    protected AbstractEvent(EventClass eventClass, EventContext eventContext, EventTopic eventTopic) {
        this.eventClass = eventClass;
        this.eventContext = eventContext;
        this.eventTopic = eventTopic;
    }

    @Override
    public EventClass getEventClass() {
        return eventClass;
    }

    @Override
    public EventTopic getEventTopic() {
        return eventTopic;
    }

    @Override
    public EventContext getEventContext() {
        return eventContext;
    }
}

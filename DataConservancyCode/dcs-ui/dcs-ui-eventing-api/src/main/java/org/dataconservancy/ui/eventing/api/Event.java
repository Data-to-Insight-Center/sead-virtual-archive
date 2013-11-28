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
package org.dataconservancy.ui.eventing.api;

/**
 * Represents an Event in the system.
 *
 * @param <T> the type of object contained in the Event
 */
public interface Event<T> {

    /**
     * The topic of the Event.
     *
     * @return the Event type
     */
    public EventTopic getEventTopic();

    /**
     * The class of the Event.
     *
     * @return the Event class
     */
    public EventClass getEventClass();

    /**
     * The Event Context.
     *
     * @return the Event context
     */
    public EventContext getEventContext();

    /**
     * The object of the Event.
     *
     * @return the object of the Event
     */
    public T getEventObject();
    
}

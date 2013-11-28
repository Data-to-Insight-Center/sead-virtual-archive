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
 * Event classes could be used by the {@link EventManager} to filter which messages are received by
 * {@link EventListener}s.
 * <p/>
 * Not to be confused with Java Classes, EventClass describes classes, or categories, of events.
 */
public enum EventClass {

    /**
     * Event class encompassing logging or audit-like events; e.g. a user logged in, or a user registered,
     * or a data item was retrieved from the repository.
     */
    AUDIT,

    /**
     * Event class encompassing system exceptions; e.g. an ingest failure occurred or a database connection
     * couldn't be retrieved.
     */
    EXCEPTION

}

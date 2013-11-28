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
 * Responsible for invoking the {@code handleEvent} method on its listeners.
 */
public interface EventManager {

    /**
     * Add a listener which will receive future events.
     *
     * @param listener the listener to add
     */
    public void addListener(EventListener listener);

    /**
     * Fire an event at this manager.  It will notify all of the registered listeners
     * of the event.
     *
     * @param eventContext the context of the event
     * @param event the event itself
     */
    public void fire(EventContext eventContext, Event event);

}

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

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A simple test which fires an event and insures that the handleEvent method of the listener is called.
 */
public class DefaultEventManagerTest {


    @Test
    public void testFire() throws Exception {

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final DefaultEventManager underTest = new DefaultEventManager(executor);

        final EventProbe probe = new EventProbe();
        assertFalse(probe.isEventHandled());

        underTest.addListener(new EventListener() {
            @Override
            public String getName() {
                // Default method body
                return null;
            }

            @Override
            public void handleEvent(EventContext eventContext, Event<?> event) {
                ((EventProbe)event.getEventObject()).setEventHandled(true);
            }
        });

        underTest.fire(new EventContext(), new Event<EventProbe>() {
            @Override
            public EventClass getEventClass() {
                // Default method body
                return null;
            }

            @Override
            public EventTopic getEventTopic() {
                // Default method body
                return null;
            }

            @Override
            public EventContext getEventContext() {
                // Default method body
                return null;
            }

            @Override
            public EventProbe getEventObject() {
                return probe;
            }
        });

        // Prevent further submission to the executor
        executor.shutdown();
        // Block for a max of 60 seconds, awaiting the event to be handled.
        executor.awaitTermination(60, TimeUnit.SECONDS);

        assertTrue(probe.isEventHandled());
    }

    private class EventProbe {
        private boolean eventHandled = false;

        boolean isEventHandled() {
            return eventHandled;
        }

        void setEventHandled(boolean eventHandled) {
            this.eventHandled = eventHandled;
        }
    }
}

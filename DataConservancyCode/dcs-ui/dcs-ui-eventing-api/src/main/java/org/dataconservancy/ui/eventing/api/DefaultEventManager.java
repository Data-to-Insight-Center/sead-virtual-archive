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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The default {@link EventManager} implementation, which executes the {@code handleEvent} method of each
 * {@code EventListener} in a separate thread.  This class is configured by default with an unbounded
 * {@link ExecutorService} with a thread pool size of 4.
 */
public class DefaultEventManager implements EventManager {

    private static final int DEFAULT_NUM_THREADS = 4;

    private final List<EventListener> listeners;

    private final ExecutorService executor;

    /**
     * Creates an event manager with an unbounded queue, and 4 threads.
     */
    public DefaultEventManager() {
        executor = Executors.newFixedThreadPool(DEFAULT_NUM_THREADS);
        listeners = Collections.synchronizedList(new ArrayList<EventListener>());
    }

    /**
     * Creates an event manager using the specified {@code ExecutorService}.
     *
     * @param executor the {@code ExecutorService} used to handle events
     */
    public DefaultEventManager(ExecutorService executor) {
        this.executor = executor;
        listeners = Collections.synchronizedList(new ArrayList<EventListener>());
    }

    /**
     * Creates an event manager with an unbounded queue, 4 threads, and attaches the supplied listeners.
     *
     * @param listeners the initial set of listeners
     */
    public DefaultEventManager(List<EventListener> listeners) {
        this.executor = Executors.newFixedThreadPool(DEFAULT_NUM_THREADS);
        this.listeners = Collections.synchronizedList(new ArrayList<EventListener>());
        for (EventListener listener : listeners) {
            this.listeners.add(listener);
        }
    }

    /**
     * Creates an event manager using the specified {@code ExecutorService}, and attaches the supplied listeners.
     *
     * @param executor the {@code ExecutorService} used to handle events
     * @param listeners the initial set of listeners
     */
    public DefaultEventManager(ExecutorService executor, List<EventListener> listeners) {
        this.executor = executor;
        this.listeners = Collections.synchronizedList(new ArrayList<EventListener>());
        for (EventListener listener : listeners) {
            this.listeners.add(listener);
        }
    }

    /**
     * This implementation will invoke the {@code handleEvent} method of each {@code EventListener} in the
     * order it was registered (however, this does not guarantee that the methods will be <em>executed</em> in
     * order of invokation - order of execution is determined by the {@code ExecutorService}).  The {@code handleEvent}
     * method will be invoked in a separate thread.
     *
     * @param eventContext the context in which the event occurred.
     * @param event the event itself.
     */
    @Override
    public void fire(final EventContext eventContext, final Event event) {
        for (final EventListener listener : listeners) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    listener.handleEvent(eventContext, event);
                }
            });
        }
    }

    /**
     * Add a listener to be notified when events are fired.
     *
     * @param listener the listener
     */
    @Override
    public void addListener(EventListener listener) {
        listeners.add(listener);
    }
}

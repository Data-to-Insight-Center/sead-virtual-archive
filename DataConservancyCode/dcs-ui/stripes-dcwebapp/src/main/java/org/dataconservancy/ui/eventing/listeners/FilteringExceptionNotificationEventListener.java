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
package org.dataconservancy.ui.eventing.listeners;

import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.services.NotificationService;
import org.dataconservancy.ui.services.VelocityTemplateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Accepts a list of exceptions that <em>will be ignored</em>.
 */
public class FilteringExceptionNotificationEventListener extends ExceptionNotificationEventListener {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private boolean considerInitialCause = false;

    private boolean considerChain = false;

    private Set<Class<? extends Throwable>> throwablesToIgnore = new HashSet<Class<? extends Throwable>>();

    /**
     * Constructs a listener that will ignore the set of classes specified in {@code throwablesToIgnore}.  This means
     * that if this listener encounters an instance of an ignored class (included subclasses), the event will not
     * be handled (that is, it will be effectivly ignored).
     * <p/>
     * The {@code throwablesToIgnore Set} must contain fully qualified names of classes that extend
     * {@link java.lang.Throwable}.  If the class cannot be found, or if the class does not extend {@code Throwable},
     * it will be ignored.
     *
     * @param notificationService the notification service
     * @param velocityHelper the velocity template helper
     * @param throwablesToIgnore a {@code Set} of {@code Strings} of fully-qualified classes that extend
     *        {@code Throwable}
     */
    public FilteringExceptionNotificationEventListener(NotificationService notificationService,
                                                       VelocityTemplateHelper velocityHelper,
                                                       Set<String> throwablesToIgnore) {
        super(notificationService, velocityHelper);
        for (String throwableClass : throwablesToIgnore) {
            try {
                Class<?> clazz = Class.forName(throwableClass);
                if (isThrowable(clazz)) {
                    this.throwablesToIgnore.add(
                            (Class<? extends Throwable>)clazz);
                } else {
                    log.trace("Ignoring class '" + throwableClass + "', it isn't a java.lang.Throwable.");
                }
            } catch (ClassNotFoundException e) {
                // Try the TCCL
                Class<?> clazz = null;
                final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                if (tccl != null) {
                    try {
                        clazz = Class.forName(throwableClass, false, tccl);
                    } catch (ClassNotFoundException e1) {
                        log.trace("Ignoring class '" + throwableClass + "', it wasn't found.");
                    }
                }

                if (clazz != null && isThrowable(clazz)) {
                    this.throwablesToIgnore.add(
                            (Class<? extends Throwable>)clazz);
                } else {
                    log.trace("Ignoring class '" + throwableClass + "', it isn't a java.lang.Throwable.");
                }
            }
        }
    }

    /**
     * Will return false if any of the following conditions are true:
     * <ul>
     *    <li>The {@link Event#getEventObject() event object} is not an instance of {@code java.lang.Throwable}</li>
     *    <li>The event object is an instance of an ignored {@code Throwable}</li>
     *    <li>The event object is a subclass of an ignored {@code Throwable}</li>
     * </ul>
     *
     * @param eventContext the event context
     * @param event the event
     * @param <T> the type of object contained in the Event
     * @return true if the event should be handled by this listener
     */
    @Override
    protected <T> boolean canHandleEvent(EventContext eventContext, Event<T> event) {
        if (super.canHandleEvent(eventContext, event)) {
            if (!(event.getEventObject() instanceof Throwable)) {
                return false;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Throwable> throwableClass = (Class<? extends Throwable>) event.getEventObject().getClass();

            if (ignoreThrowable((Throwable)event.getEventObject(), throwablesToIgnore, considerInitialCause,
                    considerChain)) {
                // The Event should be ignored, so return false: we can't handle this event
                return false;
            }
        }

        // Handle the Event
        return true;
    }

    /**
     * Obtain a read-only view of the {@code Throwable}s being ignored.
     *
     * @return a {@code Set} of {@code Throwable}s being ignored; may be empty but never {@code null}
     */
    public Set<Class<? extends Throwable>> getThrowablesToIgnore() {
        return Collections.unmodifiableSet(throwablesToIgnore);
    }

    /**
     * When {@code true}, this implementation will ignore events when an
     * {@link #getThrowablesToIgnore() ignored throwable} is the initial cause of the {@code Throwable}
     *
     * @return true if the cause of a {@code Throwable} chain should be considered when deciding whether or not to
     *         ignore the event
     */
    public boolean isConsiderInitialCause() {
        return considerInitialCause;
    }

    /**
     * When {@code true}, this implementation will ignore events when an
     * {@link #getThrowablesToIgnore() ignored throwable} is the initial cause of the {@code Throwable}
     *
     * @param considerInitialCause if the cause of a {@code Throwable} chain should be considered when deciding whether
     *        or not to ignore the event
     */
    public void setConsiderInitialCause(boolean considerInitialCause) {
        this.considerInitialCause = considerInitialCause;
    }

    /**
     * When {@code true}, this implementation will ignore events when an
     * {@link #getThrowablesToIgnore() ignored throwable} appears at any point in the {@code Throwable} chain.
     *
     * @return true if the entire {@code Throwable} chain should be considered when deciding whether or not to
     *         ignore the event
     */
    public boolean isConsiderChain() {
        return considerChain;
    }

    /**
     * When {@code true}, this implementation will ignore events when an
     * {@link #getThrowablesToIgnore() ignored throwable} appears at any point in the {@code Throwable} chain.
     *
     * @param considerChain true if the entire {@code Throwable} chain should be considered when deciding whether or not
     *        to ignore the event
     */
    public void setConsiderChain(boolean considerChain) {
        this.considerChain = considerChain;
    }

    private boolean isThrowable(Class c) {
        return java.lang.Throwable.class.isAssignableFrom(c);
    }

    /**
     * Returns {@code true} if the {@code eventThrowable} should be ignored.  First this method checks to see
     * if the {@code eventThrowable} is a class or subclass of any of the {@code Throwable}s in
     * {@code throwablesToIgnore}.  Next, if {@code considerChain} is {@code true} the method checks each
     * {@code Throwable} in the exception chain to see if it is a class or subclass of any {@code throwablesToIgnore}.
     * Finally, if {@code considerInitialCause} is {@code true}, the method checks to see if the initial cause of
     * {@code eventThrowable} is a class or subcleass of any {@code throwablesToIgnore}.
     *
     * @param eventThrowable the Throwable propagated by the {@code Event}
     * @param throwablesToIgnore the Throwables that should be ignored by this {@code EventListener}
     * @param considerInitialCause consider the initial cause of the {@code eventThrowable} exception chain
     * @param considerChain consider the entire exception chain of {@code eventThrowable}
     * @return {@code true} if this listener should ignore the {@code eventThrowable}
     */
    boolean ignoreThrowable(Throwable eventThrowable, Set<Class<? extends Throwable>> throwablesToIgnore,
                            boolean considerInitialCause, boolean considerChain) {
        // Check to see if the Throwable obtained from the Event is contained in the Set of Throwables to ignore,
        // or if the Throwable obtained from the Event is a subclass of any of Throwables to ignore.  If so,
        // ignore the Event.
        if (isAssignableFromIgnoredThrowable(eventThrowable.getClass(), throwablesToIgnore)) {
            return true;
        }

        // If we are supposed to consider the entire exception chain of the Throwable obtained from the Event,
        // do so
        Throwable initialCause = eventThrowable.getCause();
        while (initialCause != null && initialCause.getCause() != null) {
            if (considerChain && isAssignableFromIgnoredThrowable(initialCause.getClass(), throwablesToIgnore)) {
                return true;
            }
            initialCause = initialCause.getCause();
        }

        if (initialCause == null) {
            initialCause = eventThrowable;
        }

        if (considerInitialCause && isAssignableFromIgnoredThrowable(initialCause.getClass(), throwablesToIgnore)) {
            return true;
        }

        return false;
    }

    private boolean isAssignableFromIgnoredThrowable(Class<? extends Throwable> eventThrowable,
                                                     Set<Class<? extends Throwable>> throwablesToIgnore) {
        // Check for exact match
        if (throwablesToIgnore.contains(eventThrowable)) {
            return true;
        }

        // Check to see if the throwableClass is a subclass of an ignored throwable
        for (Class<? extends Throwable> toIgnore : throwablesToIgnore) {
            if (toIgnore.isAssignableFrom(eventThrowable)) {
                return true;
            }
        }

        return false;
    }
}

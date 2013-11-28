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
import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventTopic;
import org.dataconservancy.ui.services.NotificationService;
import org.dataconservancy.ui.services.VelocityTemplateHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests insuring that events are properly filtered based on Exception type
 */
public class FilteringExceptionNotificationEventListenerTest {

    private NotificationService notificationService = mock(NotificationService.class);

    private VelocityTemplateHelper velocityHelper = mock(VelocityTemplateHelper.class);

    private EventContext eventContext = mock(EventContext.class);

    private Set<String> toIgnore = new HashSet<String>();

    private Event event = mock(Event.class);

    @Before
    public void setUp() {
        when(event.getEventClass()).thenReturn(EventClass.EXCEPTION);
        when(event.getEventTopic()).thenReturn(EventTopic.EXCEPTION);
    }

    @After
    public void tearDown() {
        verify(event).getEventClass();
        verify(event).getEventTopic();
        verify(event, atLeastOnce()).getEventObject();
    }

    /**
     * If an Exception is ignored, and the FilteringExceptionNotificationEventListener receives an event with that
     * exact class, then the event should not be handled (that is, it should be ignored).
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleIgnoredThrowableExactMatch() throws Exception {
        when(event.getEventObject()).thenReturn(new IOException("An Exception to ignore."));
        toIgnore.add(IOException.class.getName());

        FilteringExceptionNotificationEventListener underTest =
                new FilteringExceptionNotificationEventListener(notificationService, velocityHelper, toIgnore);

        assertFalse(underTest.canHandleEvent(eventContext, event));
    }

    /**
     * If an Exception is ignored, and the FilteringExceptionNotificationEventListener receives an event with that
     * that is a subclass of the ignored Exception class, then the event should not be handled
     * (that is, it should be ignored).
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleIgnoredThrowableSubclass() throws Exception {
        when(event.getEventObject()).thenReturn(new MyException());
        toIgnore.add(IOException.class.getName());

        FilteringExceptionNotificationEventListener underTest =
                new FilteringExceptionNotificationEventListener(notificationService, velocityHelper, toIgnore);

        assertFalse(underTest.canHandleEvent(mock(EventContext.class), event));
    }

    /**
     * Insures that Exceptions which are not ignored are handled.
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleNotIgnoredThrowable() throws Exception {
        when(event.getEventObject()).thenReturn(new IllegalArgumentException());
        toIgnore.add(IOException.class.getName());

        FilteringExceptionNotificationEventListener underTest =
                new FilteringExceptionNotificationEventListener(notificationService, velocityHelper, toIgnore);

        assertTrue(underTest.canHandleEvent(mock(EventContext.class), event));
    }

    /**
     * Insures that the FilteringExceptionNotificationEventListener functions properly when the supplied set of
     * Ignored throwables is empty.
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleEmptyIgnoresSet() throws Exception {
        when(event.getEventObject()).thenReturn(new IllegalArgumentException());
        assertTrue(toIgnore.isEmpty());

        FilteringExceptionNotificationEventListener underTest =
                new FilteringExceptionNotificationEventListener(notificationService, velocityHelper, toIgnore);

        assertTrue(underTest.canHandleEvent(mock(EventContext.class), event));
    }

    /**
     * Insures that if the initial cause of the ignored throwable is to be ignored, and the consider initial cause
     * flag is true, that the throwable is indeed ignored (the listener will signal it cannot handle the event).
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleWhenThrowableIsInitialCauseAndConsiderInitialCauseFlagIsTrue() throws Exception {
        // Construct a chain, with the initial cause being an IllegalArgumentException
        IllegalArgumentException iae = new IllegalArgumentException("Initial cause.");
        RuntimeException rte = new RuntimeException("Terminal throwable", iae);

        // Ignore the IllegalArgumentException, which is the initial cause.
        toIgnore.add(IllegalArgumentException.class.getName());

        // But the event listener will see the RuntimeException, at first.
        when(event.getEventObject()).thenReturn(rte);

        // Set the flag so that the FENEL will consider the initial cause.
        FilteringExceptionNotificationEventListener underTest =
                new FilteringExceptionNotificationEventListener(notificationService, velocityHelper, toIgnore);
        underTest.setConsiderInitialCause(true);

        assertFalse(underTest.canHandleEvent(eventContext, event));
    }

    /**
     * Insures that if the initial cause of the ignored throwable is to be ignored, and the consider initial cause
     * flag is false, that the throwable is NOT ignored (the listener will signal it CAN handle the event).
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleWhenThrowableIsInitialCauseAndConsiderInitialCauseFlagIsFalse() throws Exception {
        // Construct a chain, with the initial cause being an IllegalArgumentException
        IllegalArgumentException iae = new IllegalArgumentException("Initial cause.");
        RuntimeException rte = new RuntimeException("Terminal throwable", iae);

        // Ignore the IllegalArgumentException, which is the initial cause.
        toIgnore.add(IllegalArgumentException.class.getName());

        // But the event listener will see the RuntimeException, at first.
        when(event.getEventObject()).thenReturn(rte);

        // Set the flag so that the FENEL will _not_ consider the initial cause.
        FilteringExceptionNotificationEventListener underTest =
                new FilteringExceptionNotificationEventListener(notificationService, velocityHelper, toIgnore);
        assertFalse(underTest.isConsiderInitialCause());

        assertTrue(underTest.canHandleEvent(eventContext, event));
    }

    /**
     * Insures that if any class of the throwable chain is to be ignored, and the consider chain
     * flag is true, that the throwable is indeed ignored (the listener will signal it cannot handle the event).
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleWhenThrowableIsInChainAndConsiderChainFlagIsTrue() throws Exception {
        // Construct a chain, with the initial cause being an NPE, and the middle of the chain being an IAE
        NullPointerException npe = new NullPointerException("Initial cause.");
        IllegalArgumentException iae = new IllegalArgumentException("Middle cause.", npe);
        RuntimeException rte = new RuntimeException("Terminal throwable", iae);

        // Ignore the IllegalArgumentException, which is in the middle of the chain.
        toIgnore.add(IllegalArgumentException.class.getName());

        // But the event listener will see the RuntimeException, at first.
        when(event.getEventObject()).thenReturn(rte);

        // Set the flag so that the FENEL will consider the entire chain
        FilteringExceptionNotificationEventListener underTest =
                new FilteringExceptionNotificationEventListener(notificationService, velocityHelper, toIgnore);
        underTest.setConsiderChain(true);

        assertFalse(underTest.canHandleEvent(eventContext, event));
    }

    /**
     * Insures that if any class of the throwable chain is to be ignored, and the consider chain
     * flag is false, that the throwable is NOT ignored (the listener will signal it CAN handle the event).
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleWhenThrowableIsInChainAndConsiderChainFlagIsFalse() throws Exception {
        // Construct a chain, with the initial cause being an NPE, and the middle of the chain being an IAE
        NullPointerException npe = new NullPointerException("Initial cause.");
        IllegalArgumentException iae = new IllegalArgumentException("Middle cause.", npe);
        RuntimeException rte = new RuntimeException("Terminal throwable", iae);

        // Ignore the IllegalArgumentException, which is in the middle of the chain.
        toIgnore.add(IllegalArgumentException.class.getName());

        // But the event listener will see the RuntimeException, at first.
        when(event.getEventObject()).thenReturn(rte);

        // Set the flag so that the FENEL will NOT consider the chain
        FilteringExceptionNotificationEventListener underTest =
                new FilteringExceptionNotificationEventListener(notificationService, velocityHelper, toIgnore);
        assertFalse(underTest.isConsiderChain());

        assertTrue(underTest.canHandleEvent(eventContext, event));
    }

    /**
     * Insures that if middle class of the throwable chain is to be ignored, and both the consider chain and initial cause
     * flags are true, that the throwable is indeed ignored (the listener will signal it cannot handle the event).
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleWhenThrowableIsInChainAndAllFlagsTrue() throws Exception {
        // Construct a chain, with the initial cause being an NPE, and the middle of the chain being an IAE
        NullPointerException npe = new NullPointerException("Initial cause.");
        IllegalArgumentException iae = new IllegalArgumentException("Middle cause.", npe);
        RuntimeException rte = new RuntimeException("Terminal throwable", iae);

        // Ignore the IllegalArgumentException, which is in the middle of the chain.
        toIgnore.add(IllegalArgumentException.class.getName());

        // But the event listener will see the RuntimeException, at first.
        when(event.getEventObject()).thenReturn(rte);

        // Set the flag so that the FENEL will consider the chain and the initial cause
        FilteringExceptionNotificationEventListener underTest =
                new FilteringExceptionNotificationEventListener(notificationService, velocityHelper, toIgnore);
        underTest.setConsiderChain(true);
        underTest.setConsiderInitialCause(true);

        assertFalse(underTest.canHandleEvent(eventContext, event));
    }

    /**
     * Insures that if initial class of the throwable chain is to be ignored, and both the consider chain and initial cause
     * flags are true, that the throwable is indeed ignored (the listener will signal it cannot handle the event).
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleWhenThrowableIsInitialCauseAndAllFlagsTrue() throws Exception {
        // Construct a chain, with the initial cause being an NPE, and the middle of the chain being an IAE
        NullPointerException npe = new NullPointerException("Initial cause.");
        IllegalArgumentException iae = new IllegalArgumentException("Middle cause.", npe);
        RuntimeException rte = new RuntimeException("Terminal throwable", iae);

        // Ignore the NullPointerException, which is the initial cause of the chain.
        toIgnore.add(NullPointerException.class.getName());

        // But the event listener will see the RuntimeException, at first.
        when(event.getEventObject()).thenReturn(rte);

        // Set the flag so that the FENEL will consider the chain and the initial cause
        FilteringExceptionNotificationEventListener underTest =
                new FilteringExceptionNotificationEventListener(notificationService, velocityHelper, toIgnore);
        underTest.setConsiderChain(true);
        underTest.setConsiderInitialCause(true);

        assertFalse(underTest.canHandleEvent(eventContext, event));
    }

    /**
     * Insures that if the terminal class of the throwable chain is to be ignored, and both the consider chain and
     * initial cause flags are true, that the throwable is indeed ignored (the listener will signal it cannot handle
     * the event).
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleWhenThrowableIsTerminalCauseAndAllFlagsTrue() throws Exception {
        // Construct a chain, with the initial cause being an NPE, and the middle of the chain being an IAE
        NullPointerException npe = new NullPointerException("Initial cause.");
        IllegalArgumentException iae = new IllegalArgumentException("Middle cause.", npe);
        RuntimeException rte = new RuntimeException("Terminal throwable", iae);

        // Ignore the RuntimeException, which is the last throwable in the chain.
        toIgnore.add(RuntimeException.class.getName());

        // The event listener will see the RuntimeException, at first.
        when(event.getEventObject()).thenReturn(rte);

        // Set the flag so that the FENEL will consider the chain and the initial cause
        FilteringExceptionNotificationEventListener underTest =
                new FilteringExceptionNotificationEventListener(notificationService, velocityHelper, toIgnore);
        underTest.setConsiderChain(true);
        underTest.setConsiderInitialCause(true);

        assertFalse(underTest.canHandleEvent(eventContext, event));
    }

    private class MyException extends IOException {
        private MyException() {
        }

        private MyException(Throwable cause) {
            super(cause);
        }
    }
}

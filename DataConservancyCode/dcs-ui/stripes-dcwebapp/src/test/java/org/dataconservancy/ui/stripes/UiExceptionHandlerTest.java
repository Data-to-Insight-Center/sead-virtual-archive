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
package org.dataconservancy.ui.stripes;


import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.Message;

import net.sourceforge.stripes.action.Resolution;
import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventListener;
import org.dataconservancy.ui.eventing.api.EventManager;
import org.dataconservancy.ui.exceptions.BaseUiException;
import org.dataconservancy.ui.exceptions.CollectionException;

import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.exception.SourcePageNotFoundException;
import org.dataconservancy.ui.exceptions.DepositException;
import org.dataconservancy.ui.exceptions.EZIDMetadataException;
import org.dataconservancy.ui.exceptions.EZIDServiceException;
import org.dataconservancy.ui.exceptions.ProfileUpdateException;
import org.dataconservancy.ui.exceptions.RegistrationUpdateException;
import org.dataconservancy.ui.services.RelationshipException;
import org.dataconservancy.ui.exceptions.UiConfigurationUpdateException;
import org.dataconservancy.ui.exceptions.ViewDepositStatusException;
import org.dataconservancy.ui.exceptions.ViewProjectActivityException;
import org.dataconservancy.ui.exceptions.ViewProjectCollectionsException;
import org.dataconservancy.ui.model.StatusPropertiesContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UiExceptionHandlerTest extends BaseActionBeanTest {

    private EventManager eventManager;

    private StatusPropertiesContext statusPropertiesContext;

    private UiExceptionHandler underTest = new UiExceptionHandler();

    private final EventProbe probe = new EventProbe();

    @Before
    public void setUpCollaborators() {
        eventManager = mock(EventManager.class);
        statusPropertiesContext = mock(StatusPropertiesContext.class);
        underTest.injectStatusPropertiesContext(statusPropertiesContext);
        underTest.injectEventManager(eventManager);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                probe.context = (EventContext) invocation.getArguments()[0];
                probe.event = (Event) invocation.getArguments()[1];
                return null;
            }
        }).when(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
    }

    private class EventProbe {
        private Event<?> event;
        private EventContext context;
    }

    private class ListenerProbe implements EventListener {
        private EventProbe probe;

        private ListenerProbe(EventProbe probe) {
            this.probe = probe;
        }

        @Override
        public String getName() {
            return "Listener Probe";
        }

        @Override
        public void handleEvent(EventContext eventContext, Event<?> event) {
            probe.event = event;
            probe.context = eventContext;
        }
    }



    /**
     * Checks that the UiExceptionHandler handleThrowabale method calls the event manager fire
     * method exactly once.
     * @throws Exception
     */
    @Test
    public void testHandleThrowable() throws Exception {
        HttpServletRequest mockReq = mock(HttpServletRequest.class);
        BaseActionBean mockBAB = mock(BaseActionBean.class);
        ActionBeanContext mockABC = mock(ActionBeanContext.class);

        when(mockReq.getAttribute(anyString())).thenReturn(mockBAB);
        when(mockBAB.getContext()).thenReturn(mockABC);
        when(mockABC.getMessages(anyString())).thenReturn(new ArrayList<Message>());
        when(mockABC.getSourcePageResolution()).thenReturn(new ErrorResolution(500));
        
        Throwable myThrowable = new Throwable("Test string for testHandleThrowable");
        
        underTest.handleThrowable(myThrowable, mockReq, mock(HttpServletResponse.class));

        verify(mockReq, times(2)).getAttribute(anyString());
        verify(mockBAB, times(2)).getContext();
        verify(mockABC).getMessages(anyString());
        verify(mockABC).getSourcePageResolution();
        verify(eventManager).fire(Matchers.<EventContext>anyObject(), Matchers.<Event>any());

        assertEquals(myThrowable, probe.event.getEventObject());
    }

    /**
     * Under certain conditions, the Source Page Resolution won't be obtainable by the UiExceptionHandler.  This is the
     * case, for example, when an ActionBean cannot be rendered for permissions reasons.  This test insures that when
     * a Source Page Resolution isn't obtainable, the UiExceptionHandler still does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleExceptionWhenBeanHasNoSourcePageResolution() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);
        final RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);
        // When the source page resolution is retrieved, throw an exception.
        when(beanCtx.getSourcePageResolution()).thenThrow(new SourcePageNotFoundException(beanCtx));
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);
        when(req.getRequestDispatcher(UiExceptionHandler.ERROR_JSP)).thenReturn(dispatcher);

        // This is our throwable
        BaseUiException buie = new BaseUiException("Error message");
        final int expectedHttpStatusCode = 500;
        buie.setHttpStatusCode(expectedHttpStatusCode);

        underTest.handleThrowable(buie, req, res);

        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the buie
        verify(eventManager).fire(any(EventContext.class), Matchers.<Event>any());
        assertEquals(buie, probe.event.getEventObject());

        // Verify that the Source Page Resolution was attempted to be found.
        verify(beanCtx).getSourcePageResolution();

        // Verify that the Request Dispatcher for the ERROR_JSP was obtained
        verify(req).getRequestDispatcher(UiExceptionHandler.ERROR_JSP);
    }

    /**
     * Under normal conditions, the Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleExceptionWhenBeanHasSourcePageResolution() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        // This is our throwable
        BaseUiException buie = new BaseUiException("Error message");
        final int expectedHttpStatusCode = 500;
        buie.setHttpStatusCode(expectedHttpStatusCode);

        // Execute the method under test.
        underTest.handleThrowable(buie, req, res);

        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the buie.
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(buie, probe.event.getEventObject());

        // Verify that the Source Page Resolution was attempted to be found.
        verify(beanCtx).getSourcePageResolution();

        // Verify that the Source Page Resolution was executed.
        verify(resolution).execute(req, res);

    }

    /**
     * Mostly exceptions will be thrown by ActionBeans, but the Stripes exception handling framework can handle
     * exceptions thrown from other objects as well, not just ActionBeans.  This test insures that all of the proper
     * things happen when the exception is thrown outside of an action bean:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Re-throws the handled exception as a ServletException (properly chained)</li>
     *     <li>Attaches the error message to the HttpServletRequest</li>
     * </ul>
     * Note that the UiExceptionHandler determines whether or not the exception was thrown by ActionBean or not by
     * looking at the HttpServletRequest and checking for the presence of the ActionBean.  If the bean is present, then
     * it is assumed the Exception was thrown from the bean.  If the bean is not present, then it is assumed the bean
     * was not thrown by the bean.
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleExceptionWhenBeanNotAvailable() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When Stripes asks for the bean, return null
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(null);

        // This is our throwable
        BaseUiException buie = new BaseUiException("Error message");
        final int expectedHttpStatusCode = 500;
        buie.setHttpStatusCode(expectedHttpStatusCode);

        // Execute the method under test.
        try {
            underTest.handleThrowable(buie, req, res);
            fail("Expected a ServletException to be thrown.");
        } catch (ServletException e) {
            assertEquals(e.getCause(), buie);
        }

        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the buie
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(buie, probe.event.getEventObject());

        // Verify that the Source Page Resolution was never attempted to be found.
        verify(beanCtx, times(0)).getSourcePageResolution();

        // Verify that the exception was placed on request (should it be placed on the response??)
        verify(req).setAttribute(eq(UiExceptionHandler.ERROR_MESSAGE_KEY), anyString());
        
    }

    /**
     * This error should only occur in an ActionBean.
     * The Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     *     <li>Includes the cause of the CollectionException in the error message, instead of the "An administrator
     *         was notified" text.</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleAddCollectionException() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // Return an array that we can check later for messages that are added to the action bean context
        final List<Message> messages = new ArrayList<Message>();
        when(beanCtx.getMessages(UiExceptionHandler.ERROR_MESSAGE_KEY)).thenReturn(messages);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        //this is our collection exception
        final String causeMsg = "Cause message";
        CollectionException ce = new CollectionException(causeMsg);
        final int expectedHttpStatusCode = 500;

        // Execute the method under test.
        try {
            underTest.handleAddCollectionException(ce, req, res);
        } catch (ServletException e){
            assertEquals(e.getCause(), ce);
        }
        
        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the ce
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(ce, probe.event.getEventObject());

        // Verify that the Source Page Resolution was found.
        verify(beanCtx, times(1)).getSourcePageResolution();

        // Verify that a SimpleError message was added, and that it includes the cause of the error.
        verify(beanCtx).getMessages(UiExceptionHandler.ERROR_MESSAGE_KEY);
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).getMessage(Locale.US).endsWith(causeMsg));
   }

    /**
     * This error should only occur in an ActionBean.
     * The Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleDepositException() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        //this is our deposit exception
        DepositException de = new DepositException();
        final int expectedHttpStatusCode = 500;

        // Execute the method under test.
        try {
            underTest.handleDepositException(de, req, res);
        } catch (ServletException e){
            assertEquals(e.getCause(), de);
        }

        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the ce
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(de, probe.event.getEventObject());

        // Verify that the Source Page Resolution was found.
        verify(beanCtx, times(1)).getSourcePageResolution();
    }

     /**
     * This error should only occur in an ActionBean.
     * The Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleProfileUpdateException() throws Exception{
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        //this is our profile update exception
        ProfileUpdateException pue = new ProfileUpdateException();
        final int expectedHttpStatusCode = 500;

        // Execute the method under test.
        try {
            underTest.handleProfileUpdateException(pue, req, res);
        } catch (ServletException e){
            assertEquals(e.getCause(), pue);
        }


        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the ce
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(pue, probe.event.getEventObject());

        // Verify that the Source Page Resolution was found.
        verify(beanCtx, times(1)).getSourcePageResolution();
    }

    /**
     * This error should only occur in an ActionBean.
     * The Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleViewProjectActivityException() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        //this is our view project activity update exception
        ViewProjectActivityException vpae = new ViewProjectActivityException();
        final int expectedHttpStatusCode = 500;

        // Execute the method under test.
        try {
            underTest.handleViewProjectActivityException(vpae, req, res);
        } catch (ServletException e){
            assertEquals(e.getCause(), vpae);
        }


        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the ce
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(vpae, probe.event.getEventObject());

        // Verify that the Source Page Resolution was found.
        verify(beanCtx, times(1)).getSourcePageResolution();
    }
    
    /**
     * This error should only occur in an ActionBean.
     * The Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleUiConfigurationUpdateException() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        //this is our Ui configuration update exception
        UiConfigurationUpdateException ucue = new UiConfigurationUpdateException();
        final int expectedHttpStatusCode = 500;

        // Execute the method under test.
        try {
            underTest.handleUiConfigurationUpdateException(ucue, req, res);
        } catch (ServletException e){
            assertEquals(e.getCause(), ucue);
        }


        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the ce
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(ucue, probe.event.getEventObject());

        // Verify that the Source Page Resolution was found.
        verify(beanCtx, times(1)).getSourcePageResolution();
    }

    /**
     * This error should only occur in an ActionBean.
     * The Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleRegistrationUpdateException() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        //this is our registration update exception
        RegistrationUpdateException rue = new RegistrationUpdateException();
        final int expectedHttpStatusCode = 500;

        // Execute the method under test.
        try {
            underTest.handleRegistrationUpdateException(rue, req, res);
        } catch (ServletException e){
            assertEquals(e.getCause(), rue);
        }


        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the ce
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(rue, probe.event.getEventObject());

        // Verify that the Source Page Resolution was found.
        verify(beanCtx, times(1)).getSourcePageResolution();
    }

    /**
     * This error should only occur in an ActionBean.
     * The Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleRelationshipException() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        //this is our relationship exception
        RelationshipException re = new RelationshipException();
        final int expectedHttpStatusCode = 500;

        // Execute the method under test.
        try {
            underTest.handleRelationshipException(re, req, res);
        } catch (ServletException e){
            assertEquals(e.getCause(), re);
        }

        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the ce
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(re, probe.event.getEventObject());

        // Verify that the Source Page Resolution was found.
        verify(beanCtx, times(1)).getSourcePageResolution();
    }

     /**
     * This error should only occur in an ActionBean.
     * The Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleViewProjectCollectionsException() throws Exception{
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        //this is our view project collections exception
        ViewProjectCollectionsException vpce = new ViewProjectCollectionsException();
        final int expectedHttpStatusCode = 500;

        // Execute the method under test.
        try {
            underTest.handleViewProjectCollectionsException(vpce, req, res);
        } catch (ServletException e){
            assertEquals(e.getCause(), vpce);
        }

        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the ce
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(vpce, probe.event.getEventObject());

        // Verify that the Source Page Resolution was found.
        verify(beanCtx, times(1)).getSourcePageResolution();
    }
    /**
     * This error should only occur in an ActionBean.
     * The Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleEZIDMetadataException() throws Exception{
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        //this is our view project collections exception
        EZIDMetadataException eme = new EZIDMetadataException();
        final int expectedHttpStatusCode = 500;

        // Execute the method under test.
        try {
            underTest.handleEZIDMetadataException(eme, req, res);
        } catch (ServletException e){
            assertEquals(e.getCause(), eme);
        }

        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the eme
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(eme, probe.event.getEventObject());

        // Verify that the Source Page Resolution was found.
        verify(beanCtx, times(1)).getSourcePageResolution();
    }

    /**
     * This error should only occur in an ActionBean.
     * The Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleEZIDServiceException() throws Exception{
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        //this is our view project collections exception
        EZIDServiceException ese = new EZIDServiceException();
        final int expectedHttpStatusCode = 500;

        // Execute the method under test.
        try {
            underTest.handleEZIDServiceException(ese, req, res);
        } catch (ServletException e){
            assertEquals(e.getCause(), ese);
        }

        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the ese
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(ese, probe.event.getEventObject());

        // Verify that the Source Page Resolution was found.
        verify(beanCtx, times(1)).getSourcePageResolution();
    }

 /**
     * This error should only occur in an ActionBean.
     * The Source Page Resolution will be obtained by the UiExceptionHandler. This test insures
     * that when a Source Page Resolution is obtained the UiExceptionHandler does the right things:
     * <ul>
     *     <li>Sets the HTTP status code on the response</li>
     *     <li>Fires an Event</li>
     *     <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
     *         ERROR_JSP</li>
     * </ul>
     *
     * @throws Exception should never happen
     */
    @Test
    public void testHandleViewDepositStatusException() throws Exception{
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        //this is our view deposit status exception
        ViewDepositStatusException vdse = new ViewDepositStatusException();
        final int expectedHttpStatusCode = 500;

        // Execute the method under test.
        try {
            underTest.handleViewDepositStatusException(vdse, req, res);
        } catch (ServletException e){
            assertEquals(e.getCause(), vdse);
        }

        // Verify the HttpServletResponse has the proper status code set
        verify(res).setStatus(expectedHttpStatusCode);

        // Verify that the the event was fired, and that the event object is the ce
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(vdse, probe.event.getEventObject());

        // Verify that the Source Page Resolution was found.
        verify(beanCtx, times(1)).getSourcePageResolution();

    }

    /**
     * When the UiExceptionHandler doesn't have an instance of EventManager, a RuntimeException should be thrown.  It's
     * a RuntimeException because something has been mis-configured by an administrator, and they should know about it
     * and fix it before proceeding.
     * 
     * @throws Exception
     */
    @Test
    @DirtiesContext
    public void testHandleExceptionWithNullEventManager() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        // When Stripes asks Spring for the EventManager, return null.
        ApplicationContext appContext = (ApplicationContext) spy(servletCtx.getAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE));
        when(appContext.getBean("eventManager", EventManager.class)).thenReturn(null);

        // Re-publish the 'spied' version of the application context
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appContext);

        // This is our throwable
        BaseUiException buie = new BaseUiException("Error message");
        final int expectedHttpStatusCode = 401;
        buie.setHttpStatusCode(expectedHttpStatusCode);

        // Insure that the instance under test has a null event manager, that way when the UiExceptionHandler looks to
        // Spring, it will retrieve a null instance of EventManager.
        underTest.injectEventManager(null);

        // Execute the method under test.
        try {
            underTest.handleThrowable(buie, req, res);
            fail("Expected a RuntimeException to be thrown.");
        } catch (RuntimeException e) {
            // expected
        }

        verify(appContext).getBean("eventManager", EventManager.class);
    }

    /**
     * Insures that even if the status properties object is null, the event is still fired.
     * 
     * @throws Exception
     */
    @Test
    @DirtiesContext
    public void testHandleExceptionWithNullStatusProperties() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        // When Stripes asks Spring for the StatusPropertiesContext, return null.
        ApplicationContext appContext = (ApplicationContext) spy(servletCtx.getAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE));
        when(appContext.getBean("statusPropertiesContext", StatusPropertiesContext.class)).thenReturn(null);

        // Re-publish the 'spied' version of the application context
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appContext);

        // This is our throwable
        BaseUiException buie = new BaseUiException("Error message");
        final int expectedHttpStatusCode = 401;
        buie.setHttpStatusCode(expectedHttpStatusCode);

        // Insure that the instance under test has a null status properties context, that way when the
        // UiExceptionHandler looks to Spring, it will retrieve a null instance of the status properties context.
        underTest.injectStatusPropertiesContext(null);

        // Execute the method under test.
        underTest.handleThrowable(buie, req, res);

        // Ensure the mocks were properly called, and that an event wsa fired.
        verify(appContext).getBean("statusPropertiesContext", StatusPropertiesContext.class);
        verify(eventManager).fire(Matchers.<EventContext>any(), Matchers.<Event>any());
        assertEquals(buie, probe.event.getEventObject());
    }

    @Test
    public void testDefaultHttpStatusCode() throws Exception {
        // Mock Servlet API objects
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // Mock Stripes objects
        final ActionBean bean = mock(ActionBean.class);
        final ActionBeanContext beanCtx = mock(ActionBeanContext.class);
        final Resolution resolution = mock(Resolution.class);

        // Mock behaviors
        when(bean.getContext()).thenReturn(beanCtx);

        // When the source page resolution is retrieved, return our resolution
        when(beanCtx.getSourcePageResolution()).thenReturn(resolution);

        // When Stripes asks for the bean, return it.
        when(req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)).thenReturn(bean);

        // When Stripes asks Spring for the StatusPropertiesContext, return null.
        ApplicationContext appContext = (ApplicationContext) spy(servletCtx.getAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE));
        when(appContext.getBean("statusPropertiesContext", StatusPropertiesContext.class)).thenReturn(null);

        // Re-publish the 'spied' version of the application context
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appContext);

        // This is our throwable
        BaseUiException buie = new BaseUiException("Error message");
        final int expectedDefaultHttpStatusCode = 500;
        //do not set an HTTP status code on the exception

        // Execute the method under test.
        underTest.handleThrowable(buie, req, res);

        //verify that the default http status code is set on the response
        verify(res).setStatus(expectedDefaultHttpStatusCode);
    }

}

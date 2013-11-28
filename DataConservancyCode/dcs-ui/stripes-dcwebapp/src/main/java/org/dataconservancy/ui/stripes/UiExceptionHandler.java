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
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.exception.DefaultExceptionHandler;
import net.sourceforge.stripes.exception.SourcePageNotFoundException;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.integration.spring.SpringHelper;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventManager;
import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.events.ExceptionEvent;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.CollectionException;
import org.dataconservancy.ui.exceptions.BaseUiException;
import org.dataconservancy.ui.exceptions.DepositException;
import org.dataconservancy.ui.exceptions.EZIDMetadataException;
import org.dataconservancy.ui.exceptions.EZIDServiceException;
import org.dataconservancy.ui.exceptions.ProfileUpdateException;
import org.dataconservancy.ui.exceptions.RegistrationUpdateException;
import org.dataconservancy.ui.exceptions.UiConfigurationUpdateException;
import org.dataconservancy.ui.exceptions.ViewDepositStatusException;
import org.dataconservancy.ui.exceptions.ViewProjectActivityException;
import org.dataconservancy.ui.exceptions.ViewProjectCollectionsException;
import org.dataconservancy.ui.model.StatusPropertiesContext;
import org.dataconservancy.ui.services.RelationshipException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.dataconservancy.ui.stripes.MessageKey.*;

/**
 * Centralized exception handling for the DCS UI.
 * <p/>
 * The responsibilities of this class are:
 * <ul>
 * <li>Sets the HTTP status code on the response</li>
 * <li>Fires an Event</li>
 * <li>If the handled exception originates from an Action Bean:</li>
 * <ul>
 * <li>Renders an error page (executes the source page resolution of the action bean, or forwards to the
 * ERROR_JSP if the source page resolution is not available)</li>
 * </ul>
 * <li>If the handled exception <em>does not</em> originate from an Action Bean:</li>
 * <ul>
 * <li>Re-throws the handled exception as a ServletException (properly chained to the handled exception)</li>
 * <li>Attaches the error message to the HttpServletRequest</li>
 * </ul>
 * </ul>
 * <p/>
 * By default, all {@code Throwable}s are {@link #handleThrowable(Throwable, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse) caught}.
 * <p/>
 * Note: this class requires an instance of {@code EventManager} to be {@link #injectEventManager(org.dataconservancy.ui.eventing.api.EventManager) injected},
 * otherwise events will not be fired.
 *
 * @see <a href="http://stripesframework.org/display/stripes/Exception+Handling">Stripes Exception Handling</a>
 */
public class UiExceptionHandler extends DefaultExceptionHandler {

    /**
     * This is the key that stores the List of error messages in the Stripes ActionBean, or the key that
     * can be used to retrieve the error message from the HttpServletResponse.
     */
    public static final String ERROR_MESSAGE_KEY = "UserInputMessages";

    /**
     * The error JSP used to render error messages when a Stripes Source Page Resolution is not found.
     * Package-private for unit testing.
     */
    static final String ERROR_JSP = "/pages/error.jsp";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * The default HTTP status code returned when the handled exception does not provide an HTTP status code to
     * use.
     */
    private int defaultHttpErrorStatus = 500;

    /**
     * Responsible for firing events.  If this is {@code null}, no events will be fired.
     */
    private EventManager eventManager;

    /**
     * Contains the build context: SVN revision, build number, and build timestamp.  If it isn't {@code null}, they
     * will be included in the Event Context of fired events.
     */
    private StatusPropertiesContext buildContext;

    /**
     * Provides the message text for properties
     */
    final Properties messageKeys;

    /**
     * The HTTP header which may be set by Apache HTTPD mod_proxy.  The value of the header contains the host name
     * that the proxy server forwarded the request for.  If there is only one proxy between the client and the server,
     * the value represents the host name of the client.  Otherwise it will contain a comma delimited list containing
     * the client and all intervening proxy servers.
     */
    private final String X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * The HTTP header which may be set by Apache HTTPD mod_proxy.  The value of the header contains the original
     * {@code Host} header sent by the client.
     */
    private final String X_FORWARDED_HOST = "X-Forwarded-Host";

    public UiExceptionHandler() {
        // Load static text
        try {
            messageKeys = new Properties();
            InputStream in = getClass().getResourceAsStream("/pageText/errorUiText.properties");
            if (null != in) {
                messageKeys.load(in);
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load static text: " + e.getMessage(), e);
        }
    }

    /**
     * Catch-all handler for exceptions that are not handled by a more specific method.  An error message is placed
     * in flash scope (only useful if the view renders the message), and an event is fired.  If the error doesn't
     * originate from an Action Bean, it is wrapped in a ServletException and re-thrown.
     *
     * @param error the throwable
     * @param req   the HttpServletRequest
     * @param res   the HttpServletResponse
     */
    public void handleThrowable(Throwable error, HttpServletRequest req, HttpServletResponse res) throws Exception {
        // Make sure the Throwable's error message isn't null, so we avoid a MissingFormatArgumentException when
        // we parameterize the message for the user.
        final String throwableMessage = error.getMessage() == null ? "" : error.getMessage();
        final String message = String.format(messageKeys.getProperty(MSG_KEY_GENERIC_ERROR), throwableMessage);
        handleInternal(message, error, req, res);
    }

    /**
     * Places a nice error message in flash scope for the user, and fires an event with the error.
     *
     * @param e   the EZIDMetadataException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleEZIDMetadataException(EZIDMetadataException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String errorMessage = String.format(messageKeys.getProperty(MSG_KEY_ERROR_CREATING_EZID_METADATA), "creator", "publication date");
        handleInternal(errorMessage, e, req, res);
    }

    /**
     * Places a nice error message in flash scope for the user, and fires an event with the error.
     *
     * @param e   the EZIDServiceException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleEZIDServiceException(EZIDServiceException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String errorMessage = String.format(messageKeys.getProperty(MSG_KEY_ERROR_CREATING_EZID));
        handleInternal(errorMessage, e, req, res);
    }

    /**
     * Places a nice error message in flash scope for the user, and fires an event with the error.
     *
     * @param e   the CollectionException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleAddCollectionException(CollectionException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String errorMessage =
                (e.getMessage() != null && e.getMessage().length() > 0) ?
                        String.format(messageKeys.getProperty(MSG_KEY_ERROR_DEPOSITING_COLLECTION), e.getMessage()) :
                        String.format(messageKeys.getProperty(MSG_KEY_ERROR_DEPOSITING_COLLECTION),
                                messageKeys.getProperty(MSG_KEY_ADMIN_NOTIFIED));
        handleInternal(errorMessage, e, req, res);
    }

    /**
     * Places a nice error message in flash scope for the user, and fires an event with the error.
     *
     * @param e   the DepositException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleDepositException(DepositException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String depositedFileName;
        if (e.getDepositedFile() != null) {
            depositedFileName = e.getDepositedFile().getFileName();
        } else {
            depositedFileName = "";
        }

        String errorMessage = String.format(messageKeys.getProperty(MSG_KEY_DEPOSIT_ERROR), depositedFileName);

        handleInternal(errorMessage, e, req, res);
    }

    /**
     * Places a nice error message in flash scope for the user, and fires an event with the error.
     *
     * @param e   the ProfileUpdateException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleProfileUpdateException(ProfileUpdateException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String errorMessage = messageKeys.getProperty(MSG_KEY_PROFILE_UPDATE_ERROR);
        handleInternal(errorMessage, e, req, res);
    }

    /**
     * Places a nice error message in flash scope for the user, and fires an event with the error.
     *
     * @param e   the ViewProjectActivityException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleViewProjectActivityException(ViewProjectActivityException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String errorMessage = String.format(messageKeys.getProperty(MSG_KEY_VIEW_PROJECT_ACTIVTY_ERROR), e.getProjectName(), e.getProjectId());
        handleInternal(errorMessage, e, req, res);
    }

    /**
     * Places a nice error message in flash scope for the user, and fires an event with the error.
     *
     * @param e   the UiConfigurationUpdateException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleUiConfigurationUpdateException(UiConfigurationUpdateException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String errorMessage = e.getMessage();
        handleInternal(errorMessage, e, req, res);
    }

    /**
     * Places a nice error message in flash scope for the user, and fires an event with the error.
     *
     * @param e   the RegistrationUpdateException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleRegistrationUpdateException(RegistrationUpdateException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String errorMessage = String.format(messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_REGISTRATION), e.getUserId());
        handleInternal(errorMessage, e, req, res);
    }

    /**
     * Places a nice error message in flash scope for the user, and fires an event with the error.
     *
     * @param e   the RelationshipException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleRelationshipException(RelationshipException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String errorMessage = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_RELATIONSHIP);
        handleInternal(errorMessage, e, req, res);
    }

    /**
     * Places a nice error message in flash scope for the user, and fires an event with the error.
     *
     * @param e   the ViewProjectCollectionsException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleViewProjectCollectionsException(ViewProjectCollectionsException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        handleInternal(e.getMessage(), e, req, res);
    }

    /**
     * Places a nice error message in flash scope for the user, and fires an event with the error.
     *
     * @param e   the ViewDepositStatusException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleViewDepositStatusException(ViewDepositStatusException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        handleInternal(e.getMessage(), e, req, res);
    }

    /**
     *
     * @param e the BizPolicyException
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there is an error re-executing the action bean
     */
    public void handleBizPolicyException(BizPolicyException e, HttpServletRequest req, HttpServletResponse res) throws Exception {
        handleInternal(e.getMessage(), e, req, res);
    }

    /**
     * The HTTP status code to be used in the response when the handled exception doesn't indicate a proper status code.
     *
     * @return the default HTTP status code to be used in the response
     */
    public int getDefaultHttpErrorStatus() {
        return defaultHttpErrorStatus;
    }

    /**
     * The HTTP status code to be used in the response when the handled exception doesn't indicate a proper status code.
     *
     * @param defaultHttpErrorStatus the default HTTP status code to be used in the response
     */
    public void setDefaultHttpErrorStatus(int defaultHttpErrorStatus) {
        this.defaultHttpErrorStatus = defaultHttpErrorStatus;
    }

    /**
     * Common logic for exception handling.  If the exception originates from an Action Bean, the supplied message is
     * placed in flash scope (only useful if the view renders the message), an event is fired, and the Action Bean is
     * re-rendered.  If the error doesn't originate from an Action Bean, the message is placed on the request, and
     * the Throwable is wrapped in a ServletException and re-thrown.
     *
     * @param message the error message
     * @param t the error being handled
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there's an error re-executing the ActionBean {@code Resolution}.
     */
    private void handleInternal(String message, Throwable t, HttpServletRequest req, HttpServletResponse res) throws Exception {
        final ActionBean bean = getActionBean(req);

        createAndFireEvent(t, bean, req);

        if (errorOccurredInAnActionBean(req, t)) {
            placeErrorInFlashAndRender(bean, t, message, req, res);
        } else {
            // The request didn't occur in an ActionBean.
            placeErrorInHttpServletRequest(t, message, req, res);
            throw new ServletException(message + " " +
                    "(Exception did not originate from an action bean!)", t);
        }
    }

    /**
     * Creates an event and event context representing the exception, and fires it.
     *
     * @param t    the exception in question
     * @param bean the action bean which the exception came from, may be null
     * @param req  the HttpServletRequest
     */
    private void createAndFireEvent(Throwable t, ActionBean bean, HttpServletRequest req) {
        injectSpringBeans();
        
        if (eventManager != null) {
            EventContext eventContext = new EventContext();
            eventContext.setRequestUri(req.getRequestURI());
            eventContext.setEventClass(EventClass.EXCEPTION);

            // Handle requests that may have been proxied by Apache http mod_proxy; don't report the proxy server
            // information, report the client's information
            if (req.getHeader(X_FORWARDED_FOR) != null) {
                eventContext.setOriginIp(req.getHeader(X_FORWARDED_FOR));
            } else {
                eventContext.setOriginIp(req.getRemoteAddr());
            }

            if (req.getHeader(X_FORWARDED_HOST) != null) {
                eventContext.setHostName(req.getHeader(X_FORWARDED_HOST));
            } else {
                eventContext.setHostName(req.getServerName());
            }

            if (buildContext != null) {
                eventContext.setBuildDate(buildContext.getBuildTimeStamp());
                eventContext.setBuildNumber(buildContext.getBuildNumber());
                eventContext.setRevisionNumber(buildContext.getBuildRevision());
            } else {
                eventContext.setBuildDate("Unknown");
                eventContext.setBuildNumber("Unknown");
                eventContext.setRevisionNumber("Unknown");
            }

            eventContext.setEventDate(DateTime.now().toString(DateTimeFormat.fullDateTime()));

            if (bean != null) {
                eventContext.setActionBean(bean.getClass().getName());
                if (bean instanceof BaseActionBean) {
                    try {
                        eventContext.setUser(((BaseActionBean) bean).getAuthenticatedUser().getId());
                    } catch (NullPointerException e) {
                        eventContext.setUser("no authenticated user");
                    }
                }
            }

            eventManager.fire(eventContext, new ExceptionEvent(eventContext, t));
        } else {
            final String msg = "Event manager was null!  Events will not be fired!";
            throw new RuntimeException(msg);
        }
    }

    /**
     * Places the message in the {@code bean}'s Context (normally displayed in the flash), and re-executes the Action
     * Bean so the message will render.  If the supplied {@code Throwable} doesn't contain an HTTP status code to use in
     * the response, then {@link #defaultHttpErrorStatus} will be used.
     *
     * @param bean    the ActionBean
     * @param e       the exception
     * @param message the error message, placed in the flash.
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     * @throws Exception if there's an error re-executing the action bean resolution
     */
    private void placeErrorInFlashAndRender(ActionBean bean, Throwable e, String message, HttpServletRequest req,
                                            HttpServletResponse res) throws Exception {

        // Make sure the correct error status code is set on the http response
        int status = setResponseStatus(e, res);

        //If the status code is 401 or 403, we always want the error resolution.
        if (401 == status || 403 == status) {
            ErrorResolution errorRes = new ErrorResolution(status, e.getMessage());
            errorRes.execute(req, res);
        } else {

            // Put the error message in the flash.
            bean.getContext().getMessages(ERROR_MESSAGE_KEY).add(new SimpleMessage(message));

            // If there is a SourcePageResolution, execute it, otherwise execute a forward resolution
            // to the ERROR_JSP page.
            try {
                bean.getContext().getSourcePageResolution().execute(req, res);
            } catch (SourcePageNotFoundException spnfe) {
                ForwardResolution errorRes = new ForwardResolution(ERROR_JSP);
                errorRes.execute(req, res);
            }
        }
    }

    /**
     * Set the proper HTTP response status on the HttpServletResponse.
     *
     * @return the status code that is set
     * @param e the throwable being handled
     * @param res the HttpServletResponse
     */
    private int setResponseStatus(Throwable e, HttpServletResponse res) {
        int httpStatusCode = defaultHttpErrorStatus;

        if (e instanceof BaseUiException) {
            httpStatusCode = ((BaseUiException) e).getHttpStatusCode();
            if (httpStatusCode < 200) {
                httpStatusCode = defaultHttpErrorStatus;
            }
        }
        else if (e instanceof BizPolicyException) {
            BizPolicyException.Type exceptionType = ((BizPolicyException) e).getType();
            
            //Reason over the exception type to provide a http status code
            if (exceptionType == BizPolicyException.Type.AUTHENTICATION_ERROR) {
                //This should be thrown when the user is not logged in and tried to do
                // something they were not authorized to do.
                httpStatusCode = 401;
            }
            else if (exceptionType == BizPolicyException.Type.AUTHORIZATION_ERROR) {
                //This should be thrown when the user was logged in, but
                // didn't have permission to do something.
                httpStatusCode = 403;
            }
            else if (exceptionType == BizPolicyException.Type.VALIDATION_ERROR) {
                //This should be thrown when the information that the user
                // entered was invalid based on the request.
                httpStatusCode = 400;
            }
        }

        res.setStatus(httpStatusCode);
        return httpStatusCode;
    }

    /**
     * Attaches the message to the HttpServletRequest using the {@link #ERROR_MESSAGE_KEY} attribute, and sets the
     * appropriate response status on the HttpServletResponse.
     *
     * @param t the throwable caught by this handler
     * @param message the error message to be placed in the request
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     */
    private void placeErrorInHttpServletRequest(Throwable t, String message, HttpServletRequest req, HttpServletResponse res) {
        req.setAttribute(ERROR_MESSAGE_KEY, message);
        setResponseStatus(t, res);
    }

    /**
     * Obtain the ActionBean, if any, from the request.
     *
     * @param req the HttpServletRequest
     * @return the ActionBean, or {@code null} if the request doesn't contain one
     */
    private ActionBean getActionBean(HttpServletRequest req) {
        return (ActionBean) req.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
    }

    /**
     * Determine whether or not the error being handled occurred in an ActionBean.  If so, we can potentially re-use
     * the ActionBean to render an error message, otherwise we'll just re-throw the handled exception wrapped in
     * a ServletException.
     *
     * @param req the HttpServletRequest
     * @param t the Throwable being handled
     * @return true if the error occurred in an action bean.
     */
    boolean errorOccurredInAnActionBean(HttpServletRequest req, Throwable t) {
        return getActionBean(req) != null;
    }

    /**
     * The Spring-configured {@code EventManager}.  If an {@code EventManager} is not injected
     * into this instance, no events will be fired.
     *
     * @param em the EventManager instance
     */
    @SpringBean("eventManager")
    public void injectEventManager(EventManager em) {
        this.eventManager = em;
    }

    /**
     * The Spring-configured {@code StatusPropertiesContext}.  If this object is injected, error messages will
     * be supplied with this context.
     *
     * @param buildContext the StatusPropertiesContext
     */
    @SpringBean("statusPropertiesContext")
    public void injectStatusPropertiesContext(StatusPropertiesContext buildContext) {
        this.buildContext = buildContext;
    }

    /**
     * Uses the Stripes {@link SpringHelper} to inject Spring beans into this instance.  Can be called multiple times
     * without worry.
     */
    private void injectSpringBeans() {
        // Inject the spring beans into this instance.
        try {
            if (null == eventManager || null == buildContext)
                SpringHelper.injectBeans(this, StripesFilter.getConfiguration().getServletContext());
        } catch (Exception e) {
            // protect against a NoSuchBeanDefinitionException (if the annotation on injectEventManager(...) doesn't
            // specify a valid bean).
            log.warn(e.getMessage(), e);
        }
    }


}

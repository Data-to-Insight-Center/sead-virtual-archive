/*
 * Copyright 2013 Johns Hopkins University
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

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_FINDING_PASSWORD_RESET_REQUEST;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_PASSWORD_RESET_REQUEST_EMAIL_ADDRESS;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_FINDING_USER_BY_EMAIL;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_PASSWORD_RESET_REQUEST_FAIL;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_PASSWORD_RESET_REQUEST_SUCCESS;
import static org.dataconservancy.ui.stripes.MessageKey. MSG_KEY_PASSWORD_RESET_REQUEST_EXISTS;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import org.apache.velocity.VelocityContext;
import org.dataconservancy.ui.exceptions.PasswordResetServiceException;
import org.dataconservancy.ui.model.Email;
import org.dataconservancy.ui.model.PasswordResetRequest;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.NotificationService;
import org.dataconservancy.ui.services.PasswordResetService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.services.VelocityTemplateHelper;
import org.dataconservancy.ui.util.UiBaseUrlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Action bean to manage forgotten password reset
 */

@UrlBinding("/reset/reset.action")
public class PasswordResetActionBean extends BaseActionBean {

    private Email emailNotification;
    
    private String requestId;

    //the velocity template path is set from the
    //injected email notification's body
    private String velocityTemplatePath;
    
    @Validate(required=true, maxlength=50, converter=EmailTypeConverter.class)
    private String emailAddress;
    
    @Validate(required=true, on="submitNewPassword", minlength=5, maxlength=20)
    private String password;

    @Validate(required=true, on="submitNewPassword", minlength=5, maxlength=20, expression="this == password")
    private String confirmedPassword;
    


    /**
     * The forward destination when requesting the password reset
     */
    static final String PASSWORD_RESET_REQUEST_PATH = "/pages/password-reset-request.jsp";
    
    /**
     *  The forward destination when submitting the reset password 
     */
    static final String PASSWORD_RESET_SUBMIT_PATH = "/pages/password-reset-submit.jsp";

    /**
     *  The forward resolution after a successful password request
     */
    
    static final String REQUEST_SUCCESS_PATH = "/pages/error.jsp";
 
    /**
     *  The forward destination after successful password reset
     */
    static final String LOGIN_FORM_PATH = "/pages/login.jsp";
    
    static final String REQUEST_ID_SESSION_KEY = "requestId";
    
    static final String PROPERTIES_FILE_PATH = "/StripesResources.properties";
    
    private VelocityTemplateHelper velocityTemplateHelper;

    private UiBaseUrlConfig uiBaseUrlConfig;

    ///////////////
    //
    // Services
    //
    ///////////////

    private PasswordResetService passwordResetService;

    private UserService userService;

    private NotificationService notificationService;
    
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public PasswordResetActionBean(){
        super();

        // Ensure desired properties are available.
        try {
            assert (messageKeys.containsKey(MSG_KEY_ERROR_FINDING_PASSWORD_RESET_REQUEST));
            assert (messageKeys.containsKey(MSG_KEY_ERROR_PASSWORD_RESET_REQUEST_EMAIL_ADDRESS));
            assert (messageKeys.containsKey(MSG_KEY_ERROR_FINDING_USER_BY_EMAIL));
            assert (messageKeys.containsKey(MSG_KEY_PASSWORD_RESET_REQUEST_FAIL)) ;
            assert (messageKeys.containsKey(MSG_KEY_PASSWORD_RESET_REQUEST_SUCCESS));
            assert (messageKeys.containsKey(MSG_KEY_PASSWORD_RESET_REQUEST_EXISTS));
        }
        catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of "
                + MSG_KEY_ERROR_FINDING_PASSWORD_RESET_REQUEST + ", "
                + MSG_KEY_ERROR_PASSWORD_RESET_REQUEST_EMAIL_ADDRESS + ", "
                + MSG_KEY_ERROR_FINDING_USER_BY_EMAIL + ", "
                + MSG_KEY_PASSWORD_RESET_REQUEST_FAIL + ", "
                + MSG_KEY_PASSWORD_RESET_REQUEST_SUCCESS + ", "
                + MSG_KEY_PASSWORD_RESET_REQUEST_EXISTS +  " is missing.");
        }
    }

    /////////////////////////
    //
    // Stripes Resolutions
    //
    /////////////////////////    
    @DontValidate
    @DefaultHandler
    public Resolution render(){
        
        if(requestId == null || requestId.isEmpty()){ //user is beginning the process, does not have a notification link yet
            return new ForwardResolution(PASSWORD_RESET_REQUEST_PATH);
        } else {//user is coming in from her email link, has the request id on the query string
            return new ForwardResolution(PASSWORD_RESET_SUBMIT_PATH);
        }
    }

    public Resolution submitPasswordResetRequest() {
        List<Message> requestMessages = getContext().getMessages("request");
        //try to get the user
        Person user = userService.get(emailAddress);
        PasswordResetRequest passwordResetRequest;
        if(null == user){
            //user is not in system, put a message on the flash
            final String msg = messageKeys.getProperty(MSG_KEY_ERROR_FINDING_USER_BY_EMAIL);
            requestMessages.add(new SimpleMessage(msg));
        } else {
            try{
                passwordResetRequest = passwordResetService.create(user);
            } catch(PasswordResetServiceException prse) {
                final String msg = messageKeys.getProperty(MSG_KEY_PASSWORD_RESET_REQUEST_FAIL);
                log.info(msg);
                return new ErrorResolution(500, msg);
            }
            if(passwordResetRequest != null){
                //successful creation of request for this user
                sendEmailNotification(passwordResetRequest, user);
                // kick user out to the "error" screen with a helpful message on it
                List<Message> userInputMessages = getContext().getMessages("UserInputMessages");
                final String msg = messageKeys.getProperty(MSG_KEY_PASSWORD_RESET_REQUEST_SUCCESS);
                userInputMessages.add(new SimpleMessage(String.format(msg, emailAddress)));
                return new ForwardResolution(REQUEST_SUCCESS_PATH);
            } else {
               //null return at this point means that the user already had an active request in the system
               final String msg = messageKeys.getProperty(MSG_KEY_PASSWORD_RESET_REQUEST_EXISTS);
               requestMessages.add(new SimpleMessage(String.format(msg, emailAddress)));
            }
        }
        //the following resolution is for failure above
        return new ForwardResolution(PASSWORD_RESET_REQUEST_PATH);
    }

    public Resolution submitNewPassword(){
        getRequestId(); //pull the requestId from the session - this sets the field value also
        List<Message> passwordMessages = getContext().getMessages("password");

        //try to get the user
        Person user = userService.get(emailAddress);
        //try to get the request
        PasswordResetRequest request = passwordResetService.getActiveRequest(requestId);

        if(null == request){
            //request not found, probably expired, put a message on the flash
            final String msg = messageKeys.getProperty(MSG_KEY_ERROR_FINDING_PASSWORD_RESET_REQUEST);
            passwordMessages.add(new SimpleMessage(msg));
        }

        if(null == user){
            //user is not in system, put a message on the flash
            final String msg = messageKeys.getProperty(MSG_KEY_ERROR_FINDING_USER_BY_EMAIL);
            passwordMessages.add(new SimpleMessage(msg));
        }

        if(request!= null && user!= null){
            if(emailAddress.equals(request.getUserEmailAddress())){
                //success - the supplied email address corresponds to a user
                //and to a user request for a new password
                userService.updatePassword(user.getId(), password);
                passwordResetService.remove(request.getId());
                //user now has new password, redirect to login page
                return new ForwardResolution(LOGIN_FORM_PATH);
            } else {
                //email address the user supplied does not match the address on the request
                //put a message on the flash
                final String msg = messageKeys.getProperty(MSG_KEY_ERROR_PASSWORD_RESET_REQUEST_EMAIL_ADDRESS);
                passwordMessages.add(new SimpleMessage(msg));
            }
        }
        //this is the resolution for failure, has the appropriate message(s) set on the flash
        return new ForwardResolution(PASSWORD_RESET_SUBMIT_PATH);
    }

    /////////////////////////
    //
    // Setters and Getters
    //
    /////////////////////////
    
    public void setEmailAddress(String emailAddress){
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress(){
        return emailAddress;
    }
    
    public void setRequestId(String id){
        requestId = id;
        HttpSession ses = getContext().getRequest().getSession();
        ses.setAttribute(REQUEST_ID_SESSION_KEY, requestId);
    }
    
    public String getRequestId(){
        HttpSession ses = getContext().getRequest().getSession();
        requestId = (String) ses.getAttribute(REQUEST_ID_SESSION_KEY);
        return requestId;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return password;
    }

    public void setConfirmedPassword(String confirmedPassword){
        this.confirmedPassword = confirmedPassword;
    }

    public String getConfirmedPassword(){
        return confirmedPassword;
    }

    //////////////////////////
    //
    // Private Methods
    //
    //////////////////////////

    private void sendEmailNotification(PasswordResetRequest request, Person user){
        List<String> recipients = new ArrayList<String>();
        recipients.add(request.getUserEmailAddress());
        
        emailNotification.setRecipient(recipients.toArray(new String[]{}));
     
        final VelocityContext velocityContext = new VelocityContext();
        Properties instanceProps = new Properties();
        String instanceNameKey = "dataConservancyInstance.name";
        String instanceNameValue;
        try {
            instanceProps = loadPropertiesFile(PROPERTIES_FILE_PATH);
            instanceNameValue =  instanceProps.getProperty(instanceNameKey);
        }
        catch (Exception e) {
            instanceNameValue= instanceNameKey +  "????";
        }

        String base = uiBaseUrlConfig.getBaseUrl().toString();
        String path = PasswordResetActionBean.class.getAnnotation(UrlBinding.class).value();
        String query = "?requestId=" + request.getId();

        velocityContext.put("passwordResetLink", base + path + query);
        velocityContext.put("instanceName", instanceNameValue);
        velocityContext.put("person", user);
        velocityContext.put("windowLength", passwordResetService.getPasswordResetRequestWindow());

        final String message = velocityTemplateHelper.execute(velocityTemplatePath, velocityContext);

        //setNotificationMessage places the new interpolated message on the body field
        emailNotification.setNotificationMessage(message);
        notificationService.sendNotification(emailNotification);
    }
    
    /////////////////////////
    //
    // Injected Beans
    //
    /////////////////////////

    /**
     * Stripes-injected PasswordResetService
     * @param passwordResetService
     */
    @SpringBean("passwordResetService")
    public void injectPasswordResetService(PasswordResetService passwordResetService){
        this.passwordResetService = passwordResetService;
    }

    /**
     * Stripes-injected UserService
     * @param userService
     */
    @SpringBean("userService")
    public void injectUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Stripes-injected NotificationService
     * @param notificationService
     */
    @SpringBean("notificationService")
    public void injectNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Stripes-injected Email
     * @param notification
     */
    @SpringBean("passwordResetNotification")
    public void injectPasswordResetNotification(Email notification) {
        //do not use the injected notification in the action bean
        //copy its properties onto a fresh notification so that
        //the original notification body field persists
        emailNotification = new Email();
        emailNotification.setSender(notification.getFromAddress());
        emailNotification.setSubject(notification.getSubject());
        //the velocity template path is stored on the
        //injected notification's body
        velocityTemplatePath=notification.getBody();
    }

    /**
     *  Stripes-injected VelocityTemplateHelper
     * @param velocityTemplateHelper
     */
    @SpringBean("velocityTemplateHelper")
    public void injectVelocityTemplateHelper(VelocityTemplateHelper velocityTemplateHelper){
        this.velocityTemplateHelper = velocityTemplateHelper;
    }

    /**
     * Stripes-injected UiBaseUrlConfig
     * @param uiBaseUrlConfig
     */
    @SpringBean("dcsUiBaseUrlConfig")
    private void injectUiBaseUrlConfig(UiBaseUrlConfig uiBaseUrlConfig) {
        this.uiBaseUrlConfig = uiBaseUrlConfig;
    }

}

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

package org.dataconservancy.ui.services;

import org.dataconservancy.ui.dao.PasswordResetRequestDAO;
import org.dataconservancy.ui.exceptions.PasswordResetServiceException;
import org.dataconservancy.ui.model.PasswordResetRequest;
import org.dataconservancy.ui.model.Person;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 *  Implementation of {@link PasswordResetService}
 */
public class PasswordResetServiceImpl implements PasswordResetService {
    private Integer passwordResetRequestWindow; //number of hours a password reset request is valid
    private PasswordResetRequestDAO passwordResetRequestDAO;
    private UserService userService;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public PasswordResetServiceImpl(PasswordResetRequestDAO passwordResetRequestDAO, UserService userService){
        if (passwordResetRequestDAO == null) {
            throw new IllegalArgumentException("PasswordResetRequestDAO must not be null.");
        }
        if (userService == null) {
             throw new IllegalArgumentException("UserService must not be null.");
        }
        this.passwordResetRequestDAO = passwordResetRequestDAO;
        this.userService = userService;
        if(passwordResetRequestWindow == null){
            passwordResetRequestWindow = Integer.parseInt(getWindowPropertyValue());
        }
        log.trace("Instantiated {} with {}", this, passwordResetRequestDAO);
    }

    @Override
    public PasswordResetRequest create(Person person) throws PasswordResetServiceException {

        PasswordResetRequest passwordResetRequest = new PasswordResetRequest();        
        if(userService.get(person.getId()) != null){
            passwordResetRequest.setUserEmailAddress(person.getEmailAddress());
            passwordResetRequest.setRequestDate(DateTime.now());
            passwordResetRequest.setId(UUID.randomUUID().toString());
        } else {
            return null;
        }

        if(findActiveRequestsForUser(person).isEmpty()){
        try{
            passwordResetRequestDAO.add(passwordResetRequest);
            } catch (DuplicateKeyException e){
                throw(new PasswordResetServiceException (e));
            } catch (DataIntegrityViolationException e){
                throw(new PasswordResetServiceException(e));
            }
            log.debug("Creating PasswordResetRequest {}", passwordResetRequest);
            return passwordResetRequestDAO.get(passwordResetRequest.getId());
        } else {
            return null;
        }
    }

    @Override
    public PasswordResetRequest getActiveRequest(String id){
        log.debug("Obtaining PasswordResetRequest for id {}", id);
        PasswordResetRequest prr = passwordResetRequestDAO.get(id);
        if(prr == null || prr.getRequestDate().plusHours(passwordResetRequestWindow).isBeforeNow()){
            return null;
        }
        return prr;
    }

    @Override
    public void remove(String id) {
        passwordResetRequestDAO.delete(id);
        log.debug("Removing PasswordResetRequest with id {}", id);
    }

    @Override
    public void removeExpiredRequests() {
        for(PasswordResetRequest request : passwordResetRequestDAO.list()){
            if(request.getRequestDate().plusHours(passwordResetRequestWindow).isBeforeNow()){
                passwordResetRequestDAO.delete(request.getId());
                log.debug("Removing expired PasswordResetRequest {}", request);
             }
         }
    }
    
    @Override
    public List<PasswordResetRequest> findAllRequests(){
        return passwordResetRequestDAO.list();
    }

    @Override
    public int getPasswordResetRequestWindow(){
        return passwordResetRequestWindow;
    }

    @Override
    public void setPasswordResetRequestWindow(int hours){
        passwordResetRequestWindow = hours;
    }

    //////////////////////////
    //
    // private methods
    //
    //////////////////////////

    private List<PasswordResetRequest> findActiveRequestsForUser(Person user){
        List<PasswordResetRequest> userRequests = new ArrayList<PasswordResetRequest>();
        for(PasswordResetRequest request : passwordResetRequestDAO.list()){
            if(request.getUserEmailAddress().equals(user.getEmailAddress()) &&
                    request.getRequestDate().plusHours(passwordResetRequestWindow).isAfterNow() ){
                userRequests.add(request);
             }
        }
        return userRequests;
    }

    private String getWindowPropertyValue() {
        final String resourcePath = "/int.properties";
        final String propertyKey = "dcs.ui.passwordResetRequestWindow";

        InputStream in = PasswordResetService.class.getResourceAsStream(resourcePath);

        if (in == null) {
            log.debug("Classpath resource " + resourcePath + " was not found.");
            return null;
        }

        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            log.error("Error loading classpath resource " + resourcePath + ": " + e.getMessage(), e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return props.getProperty(propertyKey);
    }
}
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
package org.dataconservancy.dcs.notify.api;


/**
 * Dcs Notification Service.
 *
 * @author Bill Steel
 * @version $Id: DcsNotifyService.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public interface DcsNotifyService {
    
    /**
     * Clients should call this method when they need to send off an event notification to
     * registered listeners.
     * The DcsEvent object should have the correct DcsEventType set.
     * @param event
     * @throws InvalidDcsEventTypeException
     * @throws NotificationSvcUnavailableException 
     */
    public void fire(DcsEvent event) throws InvalidDcsEventTypeException, NotificationSvcUnavailableException;
    

}

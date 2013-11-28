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
 * A DCS Notification.  The message listeners are setup to receive
 * objects of this type.  The GeneralEventConverter and NewDcsEntityConverter
 * convert from the jmsMessage received from the jms topic to a
 * DcsNotification object.
 * Although the message converter could convert to a different object if 
 * it so chose to do so and the listener was set up to receive it.
 *
 * @author Bill Steel
 * @version $Id: DcsNotification.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public class DcsNotification {
    
    private String source;
    private String message;
    private DcsEventType eventType;
    
    
    public DcsEventType getEventType() {
        return eventType;
    }


    
    private void setEventType(DcsEventType eventType) {
        this.eventType = eventType;
    }


    public DcsNotification(String source, String message, DcsEventType eventType){
        setSource(source);
        setMessage(message);
        setEventType(eventType);
    }

    
    public String getSource() {
        return source;
    }

    
    private void setSource(String source) {
        this.source = source;
    }

    
    public String getMessage() {
        return message;
    }

    
    private void setMessage(String message) {
        this.message = message;
    }
    

}

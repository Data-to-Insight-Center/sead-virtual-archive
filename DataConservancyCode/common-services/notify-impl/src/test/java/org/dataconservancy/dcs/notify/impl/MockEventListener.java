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
package org.dataconservancy.dcs.notify.impl;

import org.dataconservancy.dcs.notify.api.DcsNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mock event listener used in the jUnit tests.
 *
 * @author Bill Steel
 * @version $Id: MockEventListener.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public class MockEventListener {
    
    static Logger log = LoggerFactory.getLogger(MockEventListener.class);

    public MockEventListener(String listenerId)
    {
        setListenerId(listenerId);
        log.info("Creating new MockEventListener");
    }
    
   private String listenerId;
    
   private void setListenerId(String listenerId) {
       this.listenerId = listenerId;
   }

    public String getListenerId() {
        return listenerId;
    }

    private int messagesReceived = 0;
    
    
    /**
     * This method is wired into the Spring listener-container to be called
     * when a message is received.
     * 
     * It simply logs to the console and println's to the console.
     * It also increments the number of messages it has received.  Call
     * getMessagesReceived() to get this number.
     * 
     * @param notification
     */
    public void onNotification(DcsNotification notification) {
        
        log.info("Message Received by: "+getListenerId());
        log.info("Message follows: "+notification.getMessage());

        System.out.println("Notification received");
        System.out.println("Event Message: "+notification.getMessage());
        System.out.println("Event Source: "+notification.getSource());
        
        incrementMessagesReceived();
        
    }
    
    private void incrementMessagesReceived() {

        messagesReceived++;
        log.info("Total No. of messages received by: "+getListenerId()+" = "+getMessagesReceived());
        
    }

    public int getMessagesReceived()
    {
        return messagesReceived;
    }

}

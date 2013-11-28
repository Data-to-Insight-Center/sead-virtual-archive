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


import java.io.IOException;

import java.util.Calendar;

import org.dataconservancy.dcs.notify.api.DcsEvent;
import org.dataconservancy.dcs.notify.api.DcsEventType;
import org.dataconservancy.dcs.notify.api.NotificationSvcUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    static Logger log = LoggerFactory.getLogger(AppTest.class);

    private AbstractApplicationContext appContext;

    //event listeners
    private MockEventListener listenerGeneralEvent;
    private MockEventListener listenerNewDcsEntity;
    private MockEventListener listenerAll;
    
    public void setUp() throws IOException {

        //Get the Spring Application Context.
        /**
         * Application contexts are necessary to publish events to beans that are
         * registered as listeners.
         */
        appContext = new ClassPathXmlApplicationContext(new String[] {
                "notifyService.xml"});
        appContext.registerShutdownHook();
        
        //Get references to the listeners
        listenerGeneralEvent = (MockEventListener) appContext.getBean("testEventListenerGeneralEvent");
        listenerNewDcsEntity = (MockEventListener) appContext.getBean("testEventListenerNewDcsEntity");
        listenerAll = (MockEventListener) appContext.getBean("testEventListenerAll");


        log.info("Got Spring Application Context:" + appContext);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        
        log.info("Closing Application Context");
        //appContext.close();
        //Thread.sleep(1000);
    }
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testEventFiring() throws Exception
    {
        String message = "Testing event firing: "+Calendar.getInstance().getTime().toString();
        
        DcsEvent dcsEvent = new DcsEvent("test", message, DcsEventType.GENERAL_DCS_EVENT);
        
        DcsNotifyServiceImpl notifySvc = new DcsNotifyServiceImpl();
        
        try {
            fireEvent(dcsEvent, notifySvc);
            
            //wait for the async listeners to receive the message.
            Thread.sleep(1000);
            
            //check that the test listeners printed received messages to the console
            assertEquals(1, listenerGeneralEvent.getMessagesReceived());
            assertEquals(0, listenerNewDcsEntity.getMessagesReceived());
            assertEquals(1, listenerAll.getMessagesReceived());
        } catch (NotificationSvcUnavailableException e) {
            log.error("Notification service is unavailable.");
        }
        
        log.info("End of testEventFiring");
        
        appContext.close();

    }
    
    
    public void testMultipleEvents() throws Exception
    {
        String message1 = "Testing multiple events, event 1: "+Calendar.getInstance().getTime().toString();
        String message2 = "Testing multiple events, event 2: "+Calendar.getInstance().getTime().toString();
        String message3 = "Testing multiple events, event 3: "+Calendar.getInstance().getTime().toString();
        String message4 = "Testing multiple events, event 4: "+Calendar.getInstance().getTime().toString();
        String message5 = "Testing multiple events, event 5: "+Calendar.getInstance().getTime().toString();
        
        //Create some test events
        DcsEvent dcsEvent1 = new DcsEvent("test", message1, DcsEventType.GENERAL_DCS_EVENT);
        DcsEvent dcsEvent2 = new DcsEvent("test", message2, DcsEventType.GENERAL_DCS_EVENT);
        DcsEvent dcsEvent3 = new DcsEvent("test", message3, DcsEventType.NEW_DCS_ENTITY);
        DcsEvent dcsEvent4 = new DcsEvent("test", message4, DcsEventType.NEW_DCS_ENTITY);
        DcsEvent dcsEvent5 = new DcsEvent("test", message5, DcsEventType.NEW_DCS_ENTITY);
               
        //Fire off the event
        DcsNotifyServiceImpl notifySvc = new DcsNotifyServiceImpl();
        
        try {
            fireEvent(dcsEvent1, notifySvc);
            fireEvent(dcsEvent2, notifySvc);
            fireEvent(dcsEvent3, notifySvc);
            fireEvent(dcsEvent4, notifySvc);
            fireEvent(dcsEvent5, notifySvc);
   
            //wait for the async listeners to receive the message.
            Thread.sleep(1000);
            
            //check that the test listeners printed received messages to the console
            assertEquals(2, listenerGeneralEvent.getMessagesReceived());
            assertEquals(3, listenerNewDcsEntity.getMessagesReceived());
            assertEquals(5, listenerAll.getMessagesReceived());
        } catch (NotificationSvcUnavailableException e) {
            log.error("Notification service is unavailable.");
        }
        
        log.info("End of testMultipleEvents");
        
        appContext.close();

    }
    
    
    private void fireEvent(DcsEvent dcsEvent, DcsNotifyServiceImpl notifySvc) throws Exception
    {
        notifySvc.fire(dcsEvent);
        return;
    }
    
}

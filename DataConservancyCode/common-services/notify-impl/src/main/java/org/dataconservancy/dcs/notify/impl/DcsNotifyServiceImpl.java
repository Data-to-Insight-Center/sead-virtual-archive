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

import java.net.ConnectException;

import javax.jms.JMSException;

import org.dataconservancy.dcs.notify.api.DcsEvent;
import org.dataconservancy.dcs.notify.api.DcsEventType;
import org.dataconservancy.dcs.notify.api.DcsNotifyService;
import org.dataconservancy.dcs.notify.api.InvalidDcsEventTypeException;
import org.dataconservancy.dcs.notify.api.NotificationSvcUnavailableException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.UncategorizedJmsException;

/**
 * Implementation of DcsNotifyService.  Used to send out event notifications to registered
 * listeners.
 * Listeners are registered using Spring JMS.  See the readme.txt file at:
 * notify-impl/readme.txt
 *
 * @author Bill Steel
 * @version $Id: DcsNotifyServiceImpl.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public class DcsNotifyServiceImpl implements DcsNotifyService {
   
    public DcsNotifyServiceImpl() {}
    
    /**
     * Clients should call this method when they need to send off an event notification to
     * registered listeners.
     * The DcsEvent object should have the correct DcsEventType set.
     * @param event
     * @throws InvalidDcsEventTypeException
     */
    public void fire(final DcsEvent dcsEvent) throws InvalidDcsEventTypeException,
        NotificationSvcUnavailableException 
    {
                
        DcsEventType eventType = getDcsEventType(dcsEvent);
        
        switch (eventType) {
            case NEW_DCS_ENTITY: newDcsEntity(dcsEvent);  
                break;
            case GENERAL_DCS_EVENT: generalEvent(dcsEvent); 
                break;
            default: throw new InvalidDcsEventTypeException();       
        }
    }
    
    /**
     * Publish an event using the generalEventPublisher bean.  This is wired in the notifyService.xml
     * config file to a specific topic.
     * @param dcsEvent
     */
    private void generalEvent(DcsEvent dcsEvent) throws NotificationSvcUnavailableException {
        //Use Spring to instantiate the publishers.
        BeanFactory factory = new XmlBeanFactory(new ClassPathResource("notifyService.xml"));
        PublishGeneralEvent pubGenEvent = (PublishGeneralEvent) factory.getBean("generalEventPublisher");
        try {
            pubGenEvent.send(dcsEvent);
        } catch (UncategorizedJmsException jmsException) {
            if(JMSException.class.isInstance(jmsException.getCause()))
            {
                if(ConnectException.class.isInstance(jmsException.getCause().getCause()))
                {
                    throw new NotificationSvcUnavailableException(jmsException);
                }
            }
        }
        
    }
    

    /**
     * Publish an event using the newDcsEntityPublisher bean.  This is wired in the notifyService.xml
     * config file to a specific topic.
     * @param dcsEvent
     */
    private void newDcsEntity(DcsEvent dcsEvent) throws NotificationSvcUnavailableException {
        //Use Spring to instantiate the publishers.
        BeanFactory factory = new XmlBeanFactory(new ClassPathResource("notifyService.xml"));
        PublishNewDcsEntity pubNewDcsEntity = (PublishNewDcsEntity) factory.getBean("newDcsEntityPublisher");
        try {
            pubNewDcsEntity.send(dcsEvent);
        } catch (UncategorizedJmsException jmsException) {
            if(JMSException.class.isInstance(jmsException.getCause()))
            {
                if(ConnectException.class.isInstance(jmsException.getCause().getCause()))
                {
                    throw new NotificationSvcUnavailableException(jmsException);
                }
            }
        }
    }

    private DcsEventType getDcsEventType(DcsEvent dcsEvent) {
        
        return dcsEvent.getEventType();
        
    }
    
    



}

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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.dataconservancy.dcs.notify.api.DcsEvent;
import org.dataconservancy.dcs.notify.api.DcsNotification;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * Takes a DcsEvent object and converts to a JMS message object. and back.
 *
 * @author Bill Steel
 * @version $Id: GeneralEventConverter.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public class GeneralEventConverter implements MessageConverter {

    public GeneralEventConverter() {}

    /**
     * Convert from a jmsMessage to a DcsNotification.  Used to convert messages from
     * jmsMessage when receiving messages from a topic.
     * {@inheritDoc}
     */
    public Object fromMessage(Message message) throws JMSException,
            MessageConversionException {

        if(!(message instanceof ObjectMessage)) {
            throw new MessageConversionException(
            "Message isn't a ObjectMessage");
            }
        
            ObjectMessage objMessage = (ObjectMessage) message;
            
            DcsEvent dcsEvent = (DcsEvent) objMessage.getObject();

            DcsNotification dcsNotification = new DcsNotification(
                     dcsEvent.getSource(),
                     dcsEvent.getMessage(),
                     dcsEvent.getEventType());
            
            return dcsNotification;
    }



    /**
     * Convert from a DcsEvent to a jmsMessage.  Used to convert to a jmsMessage for 
     * messages posted to the topic.
     * {@inheritDoc}
     */
    public Message toMessage(Object object, Session session) throws JMSException,
            MessageConversionException {

            if (!(object instanceof DcsEvent)) {
                throw new MessageConversionException("Object isn't a DcsEvent");
            }
            
            DcsEvent dcsEvent = (DcsEvent) object;
            ObjectMessage message = session.createObjectMessage();
            message.setObject(dcsEvent);
            //message.setObject("eventType", (Object) dcsEvent.getEventType());
            //message.setString("eventInfo", dcsEvent.getMessage());
            //message.setString("eventSource", dcsEvent.getSource());
        
        return message;
    }
    
    

}

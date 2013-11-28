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


import javax.jms.Destination;

import org.dataconservancy.dcs.notify.api.DcsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;


/**
 * This class is used to publish messages to a topic.  The
 * specific topic being published to is wired in the Spring 
 * config file.
 * 
 * Currently it is wired to publish to the NewDcsEntity topic.
 * 
 * Note this is duplicate code to PublishGeneralEvent.  In
 * reality that class could be used instead of this one but
 * this is here to show how the notification service design
 * can be extended for additional publishers, message converters
 * and topics.
 *
 * @author Bill Steel
 * @version $Id: PublishNewDcsEntity.java 2826 2012-02-02 20:52:15Z emetsger $
 */
public class PublishNewDcsEntity {

    String TOPIC = "PUBLISH NEW DCS ENTITY TOPIC";
    
    static Logger log = LoggerFactory.getLogger(PublishNewDcsEntity.class);

    private Destination topic;

    
    public void setTopic(Destination destination) {
        this.topic = destination;
    }

    public void send(DcsEvent dcsEvent) {
        jmsTemplate.convertAndSend(this.topic, dcsEvent);  
        log.info("Publishing event to topic: "+TOPIC);
        log.info("--> Event Source:  "+dcsEvent.getSource());
        log.info("--> Event Message: "+dcsEvent.getMessage());
    }
     
    private JmsTemplate jmsTemplate;
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

}

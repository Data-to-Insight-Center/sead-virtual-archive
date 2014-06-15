/*
#
# Copyright 2013 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
*/
package org.dataconservancy.dcs.ingest.services.rules.impl;

import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.services.runners.model.ServiceQueueModifier;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.drools.runtime.StatelessKnowledgeSession;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.pack.ResearchObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Loader and executor of rules
 */
public class Executor {

    public static Map<String, Integer> mapPriorities = new HashMap<String, Integer>();
    public static BlockingQueue<String> outputMessages = new ArrayBlockingQueue<String>(50);

    public Queue<String> executeRules(SipStager sipStager, String sipId, ServiceQueueModifier queueModifier) throws FileNotFoundException, InvalidXmlException {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        ResearchObject researchObject = (ResearchObject)sipStager.getSIP(sipId);

        DcsDeliverableUnit du = researchObject.getDeliverableUnits().iterator().next();

        String sessionKey = "ServiceSession";
        StatelessKnowledgeSession statelessKnowledgeSessionForIdeals =
                (StatelessKnowledgeSession)applicationContext.getBean(sessionKey);
        statelessKnowledgeSessionForIdeals.execute(newArrayList(((SeadDeliverableUnit)du)));
        statelessKnowledgeSessionForIdeals.execute(newArrayList(queueModifier, ((SeadDeliverableUnit)du), researchObject));
        return outputMessages;
    }
}

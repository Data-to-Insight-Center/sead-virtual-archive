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
import org.dataconservancy.dcs.ingest.services.runners.model.RepositoryMatcher;
import org.dataconservancy.dcs.ingest.services.runners.model.ServiceQueueModifier;
import org.dataconservancy.dcs.ingest.services.util.Output;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.drools.definition.rule.Rule;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.BeforeActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.runtime.StatelessKnowledgeSession;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.pack.ResearchObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Loader and executor of rules
 */
public class Executor {

    public Executor(BlockingQueue<String> outputMessages){
        this.outputMessages = outputMessages;
    }
    public static Map<String, Integer> mapPriorities = new HashMap<String, Integer>();
    BlockingQueue<String> outputMessages;// = new ArrayBlockingQueue<String>(50);

    public void executeRules(SipStager sipStager, String sipId, ServiceQueueModifier queueModifier, Map<String, Integer> matchedRepositories) throws FileNotFoundException, InvalidXmlException {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        ResearchObject researchObject = (ResearchObject)sipStager.getSIP(sipId);

        DcsDeliverableUnit du = researchObject.getDeliverableUnits().iterator().next();

        String sessionKey = "ServiceSession";
        StatelessKnowledgeSession ksession =
                (StatelessKnowledgeSession)applicationContext.getBean(sessionKey);
        //     statelessKnowledgeSessionForIdeals.execute(newArrayList(((SeadDeliverableUnit)du)));
        ksession.addEventListener(new DefaultAgendaEventListener() {
            @Override
            public void beforeActivationFired(final BeforeActivationFiredEvent event) {
                final Rule rule = event.getActivation().getRule();
                final Logger log = Logger.getLogger(rule.getPackageName() + "." + rule.getName());
                log.info(event.getClass().getSimpleName());
                System.out.println(("Matchmaker matched and executed the rule "+ event.getActivation().getRule().getName()));
                outputMessages.add("Matchmaker matched and executed the rule "+ event.getActivation().getRule().getName());
            }

            @Override
            public void afterActivationFired(final AfterActivationFiredEvent event) {
                if( event.getActivation().getRule().getName().contains("Decision"))
                    outputMessages.add("Final matched repository based on priorities: "+ Output.repoName);
            }
        });

        ksession.execute(newArrayList(queueModifier, ((SeadDeliverableUnit)du), researchObject, new RepositoryMatcher(matchedRepositories)));
        // return outputMessages;
    }
}

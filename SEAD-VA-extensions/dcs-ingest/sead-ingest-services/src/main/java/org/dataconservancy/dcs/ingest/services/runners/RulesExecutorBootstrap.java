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

//Developed based on ExecutorBootstrap code from JHU

package org.dataconservancy.dcs.ingest.services.runners;

import org.dataconservancy.dcs.ingest.Bootstrap;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.services.IngestService;
import org.dataconservancy.dcs.ingest.services.IngestServiceException;
import org.dataconservancy.dcs.ingest.services.TimedServiceWrapper;
import org.dataconservancy.dcs.ingest.services.runners.model.ServiceQueueModifier;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.drools.runtime.StatelessKnowledgeSession;
import org.seadva.ingest.Events;
import org.seadva.model.pack.ResearchObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;


public class RulesExecutorBootstrap
        implements Bootstrap {

    private static final Logger log =
            LoggerFactory.getLogger(RulesExecutorBootstrap.class);

    private IngestFramework ingest;

    private static BlockingQueue<IngestService> ingestServices;

    private Executor executor;

    public static Map<String,IngestService> serviceMap;

    StatelessKnowledgeSession ksession;

    static {
        serviceMap = new HashMap<String, IngestService>();
    }

    @Required
    public void setServiceMap(Map<String,IngestService> serviceMap){
        this.serviceMap = serviceMap;
    }

    @Required
    public void setIngestFramework(IngestFramework fwk) {
        ingest = fwk;
    }

    @Required
    public void setExecutor(Executor exe) {
        executor = exe;
    }

    @Required
    public void setKSession(StatelessKnowledgeSession session) {
        ksession = session;
    }


    public void startIngest(String stagedSipID) {
        ServiceQueueModifier queueModifier = new ServiceQueueModifier(new ArrayBlockingQueue<IngestService>(10));
        Map<String, Integer> matchedRepositories  = new HashMap<String, Integer>();
        executor.execute(new RulesRunner(queueModifier, ingest.getSipStager(), stagedSipID, matchedRepositories));
        executor.execute(new IngestRunner(queueModifier, stagedSipID));
    }

    private class IngestRunner
            implements Runnable {

        String sipRef;
        ServiceQueueModifier queueModifier;
        public IngestRunner(ServiceQueueModifier queueModifier, String sipId) {
            this.queueModifier = queueModifier;
            this.sipRef = sipId;
        }

        public void run() {
            System.out.print("Running Ingest Runner");

            Class<? extends IngestService> currentService = null;
            long start = System.currentTimeMillis();
            addIngestEvent(sipRef);

            while(true){

                if(queueModifier.isEmpty()){
                    try {
                        Thread.sleep(3*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    continue;
                }

                //For each SipRef/Workflow, the services are executed in the sequential order in which they are added
                //Multiple SipRefs/workflows can be executed in parallel

                try {
                    IngestService svc = queueModifier.getIngestService();
                    currentService = svc.getClass();
                    if (log.isDebugEnabled()) {
                        log.debug("Ingest {}: Executing service {}",
                                sipRef,
                                currentService.getSimpleName());
                    }
                    TimedServiceWrapper wrapper = new TimedServiceWrapper(svc);
                    wrapper.execute(sipRef);
                    log.debug("Ingest {}: Service {} execution: {} ms",
                            new Object[] { sipRef, svc.getClass().getName(), (wrapper.getEnd() - wrapper.getStart()) });

                    Thread.sleep(1*1000); //To prevent reading SIP before it is entirely updated by an activity to include events (especially to be able to read failure events)
                    Dcp sip = ingest.getSipStager().getSIP(sipRef);

                    int doneFlag = 0;
                    for(DcsEvent event:sip.getEvents()){
                        System.out.println("Debugging events: "+event.getDetail());

                        if(event.getEventType().equalsIgnoreCase(Events.INGEST_SUCCESS)||event.getEventType().equalsIgnoreCase(Events.INGEST_FAIL))
                        {

                            doneFlag = 1;
                            log.info("Total ingest ({}) execution: {} ms", sipRef, System.currentTimeMillis() - start);

                        }
                    }

                    if (doneFlag==1)
                        break;

                } catch (IngestServiceException e) {

                    /*
                     * If we receive a "logical" ingest exception, log a basic
                     * failure event
                     */
                    ingest.getEventManager()
                            .addEvent(sipRef,
                                    getFailureEvent(e, sipRef, currentService));
                    log.info("failed ingest: " + sipRef, e);

                } catch (Throwable e) {
                    /*
                     * If we receive an "unexpected" ingest exception, log the stack
                     * trace
                     */
                    DcsEvent fail = getFailureEvent(e, sipRef, currentService);

                    ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
                    PrintStream capture = new PrintStream(stackTrace);
                    e.printStackTrace(capture);
                    fail.setDetail(new String(stackTrace.toByteArray()));
                    ingest.getEventManager().addEvent(sipRef, fail);
                    log.info("failed ingest: " + sipRef, e);
                }



            }

        }

        private DcsEvent getFailureEvent(Throwable e,
                                         String sipRef,
                                         Class<? extends IngestService> svc) {
            DcsEvent fail =
                    ingest.getEventManager().newEvent(Events.INGEST_FAIL);
            fail.setOutcome(svc.getName());
            fail.setDetail(e.getMessage());

            DcsEvent ingestStart =
                    ingest.getEventManager()
                            .getEventByType(sipRef, Events.INGEST_START);

            for (DcsEntityReference ref : ingestStart.getTargets()) {
                fail.addTargets(ref);
            }

            return fail;
        }

        private Set<DcsEntityReference> getEntities(Dcp dcp) {
            Set<DcsEntityReference> entities =
                    new HashSet<DcsEntityReference>();
            addRefs(entities, dcp.getCollections());
            addRefs(entities, dcp.getDeliverableUnits());
            addRefs(entities, dcp.getEvents());
            addRefs(entities, dcp.getFiles());
            addRefs(entities, dcp.getManifestations());

            return entities;
        }

        private void addRefs(Collection<DcsEntityReference> refs,
                             Collection<? extends DcsEntity> entities) {
            for (DcsEntity e : entities) {
                refs.add(new DcsEntityReference(e.getId()));
            }
        }

        private void addIngestEvent(String sipRef) {
            DcsEvent ingestStart =
                    ingest.getEventManager().newEvent(Events.INGEST_START);
            Dcp sip = ingest.getSipStager().getSIP(sipRef);
            ingestStart.setOutcome(sipRef);
            ingestStart.setDetail("Ingest " + sipRef + " started");
            ingestStart.setTargets(getEntities(sip));

            ingest.getEventManager().addEvent(sipRef, ingestStart);
        }
    }

    private class RulesRunner
            implements Runnable {

        ServiceQueueModifier queueModifier;
        SipStager sipStager;
        String sipId;
        BlockingQueue<String> outputMessages;
        Map<String, Integer> matchedRepositories;

        RulesRunner(ServiceQueueModifier queueModifier, SipStager sipStager, String sipId, Map<String, Integer> matchedRepositories){
            this.queueModifier = queueModifier;
            this.sipStager = sipStager;
            this.sipId = sipId;
            this.outputMessages = new ArrayBlockingQueue<String>(50);
            this.matchedRepositories = matchedRepositories;
        }

        public void run() {
            System.out.print("Running Rules Runner");
            try {
                new org.dataconservancy.dcs.ingest.services.rules.impl.Executor(ksession, this.outputMessages).executeRules(this.sipStager, this.sipId, this.queueModifier, this.matchedRepositories);

                while (true){
                    if(this.outputMessages.isEmpty()){
                        Thread.sleep(2*1000);
                        continue;
                    }
                    String message = this.outputMessages.peek();
                    DcsEvent event = createEvent(this.sipId, message);
                    ResearchObject sip = (ResearchObject) this.sipStager.getSIP(this.sipId);
                    sip.addEvent(event);

                    synchronized (this){
                        this.sipStager.updateSIP(sip, this.sipId);//This is could potentially cause consistency issues due to multiple thread updating the sip
                    }
                    this.outputMessages.remove();
                    if(message.contains("workflow"))
                        break;
                }
                //You need to get the message back out as an event
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvalidXmlException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        private DcsEvent createEvent(String sipRef, String message) {
            DcsEvent matchEvent =
                    ingest.getEventManager().newEvent(Events.MATCH_MAKING);
            matchEvent.setOutcome(sipRef);
            matchEvent.setDetail(message);
            return  matchEvent;
        }

    }
}

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
import org.dataconservancy.dcs.ingest.services.IngestService;
import org.dataconservancy.dcs.ingest.services.IngestServiceException;
import org.dataconservancy.dcs.ingest.services.TimedServiceWrapper;
import org.seadva.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
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

    private static Map<String,IngestService> serviceMap;
    static{
        serviceMap = new HashMap<String, IngestService>();
    }

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

    public void addIngestServices(IngestService... services) {
        if(ingestServices==null)
            ingestServices = new ArrayBlockingQueue<IngestService>(10);
        for(IngestService ingestService: services)
            ingestServices.add(ingestService);
    }

    public static void addIngestServicesName(String... services) {
        if(ingestServices==null)
            ingestServices = new ArrayBlockingQueue<IngestService>(10);
        for(String ingestService: services)
            ingestServices.add(serviceMap.get(ingestService));
    }
    public void startIngest(String stagedSipID) {
        executor.execute(new RulesRunner(ingest.getSipStager(), stagedSipID));
        executor.execute(new IngestRunner(stagedSipID));
    }

    private class IngestRunner
            implements Runnable {

        private final String sipRef;

        public IngestRunner(String sipId) {
            sipRef = sipId;
        }

        public void run() {
            long start = System.currentTimeMillis();
            Class<? extends IngestService> currentService = null;

            addIngestEvent(sipRef);
            try {
                /* Try to execute all ingest services in order */
                while (true){
                    if(ingestServices==null||ingestServices.isEmpty()){
                        Thread.sleep(2*1000);
                        continue;
                    }
                    IngestService svc = ingestServices.peek();
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

                    Dcp sip = ingest.getSipStager().getSIP(sipRef);
                    int doneFlag = 0;

                    for(DcsEvent event:sip.getEvents()){
                        if(event.getEventType().equalsIgnoreCase(Events.INGEST_SUCCESS))
                        {
                            doneFlag = 1;
                            break;
                        }
                    }
                    ingestServices.remove();
                    if (doneFlag==1)
                        break;
                }
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

            log.info("Total ingest ({}) execution: {} ms", sipRef, System.currentTimeMillis() - start);
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


        SipStager sipStager;
        String sipId;

        RulesRunner(SipStager sipStager, String sipId){
            this.sipStager = sipStager;
            this.sipId = sipId;
        }

        public void run() {
            try {
                new org.dataconservancy.dcs.ingest.services.rules.impl.Executor().executeRules(this.sipStager, this.sipId);
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvalidXmlException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}

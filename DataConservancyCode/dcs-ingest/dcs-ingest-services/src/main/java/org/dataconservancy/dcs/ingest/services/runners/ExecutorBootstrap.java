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
package org.dataconservancy.dcs.ingest.services.runners;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import org.dataconservancy.dcs.ingest.Bootstrap;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.services.IngestService;
import org.dataconservancy.dcs.ingest.services.IngestServiceException;
import org.dataconservancy.dcs.ingest.services.TimedServiceWrapper;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Directly executes a static ingest service pipeline using a thread pool.
 * <p>
 * Executes IngestServices in order within a thread. The number of actively
 * executing threads may be limited to a specified number. If there are no
 * available threads, ingest processes will be queued in an unbounded queue and
 * executed when there are available resources. The queue is memory based, so
 * more than a few thousand or hundred thousand ingest jobs pending may be a
 * problem.
 * </p>
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setIngestFramework(IngestFramework)}</dt>
 * <dd><b>Required</b>.</dd>
 * <dt>{@link #setIngestServices(IngestService...)}</dt>
 * <dd><b>Required</b>. Determines the list of services to be executed in an
 * ingest process. Will be executed in order.</dd>
 * <dt>{@link #setExecutionThreadCount(int)}</dt>
 * <dd>Optional. Sets the size of the actuvely executing thread pool. Default is
 * 4 threads.</dd>
 * </dl>
 */
public class ExecutorBootstrap
        implements Bootstrap {

    private static final Logger log =
            LoggerFactory.getLogger(ExecutorBootstrap.class);

    private IngestFramework ingest;

    private IngestService[] ingestServices;

    private Executor executor;

    @Required
    public void setIngestFramework(IngestFramework fwk) {
        ingest = fwk;
    }

    @Required
    public void setExecutor(Executor exe) {
        executor = exe;
    }

    @Required
    public void setIngestServices(IngestService... services) {
        ingestServices = services;
    }

    public void startIngest(String stagedSipID) {
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
                for (IngestService svc : ingestServices) {
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
}

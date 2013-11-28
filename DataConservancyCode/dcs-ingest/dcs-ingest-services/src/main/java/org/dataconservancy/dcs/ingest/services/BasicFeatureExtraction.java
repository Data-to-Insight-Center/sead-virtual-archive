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
package org.dataconservancy.dcs.ingest.services;

import java.util.List;

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.dcp.SipDcpReader;
import org.dataconservancy.transform.execution.BasicExecutionEnvironment;
import org.dataconservancy.transform.execution.ExceptionListener;
import org.dataconservancy.transform.execution.OutputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static org.dataconservancy.dcs.ingest.Events.TRANSFORM;
import static org.dataconservancy.dcs.ingest.Events.TRANSFORM_FAIL;

/**
 * Executes a series of feature extraction jobs on ingested SIPs.
 * <p>
 * All configured extraction/transformation jobs will be applied equally to
 * every SIP ingested. That is to say, this basic feature extraction service
 * impl does not decide jobs based upon policy.
 * </p>
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setExecutionEnvironment(BasicExecutionEnvironment)}</dt>
 * <dd><b>Required</b>. This is the fully-configured execution environment that
 * will be used for running every feature extraction job</dd>
 * <dt>{@link #setJobs(List)}</dt>
 * <dd><b>Required</b>. This shall contain every {@link Mapping} instance and
 * its corresponding {@link Output}. All {@linkplain Job}s will be applied to
 * every ingested sip.</dd>
 * <dt>{@link #setIngestFramework(IngestFramework)}</dt>
 * <dd><b>Required</b>. Contains the sipStager from which the sip will be
 * pulled, and an event manager into which to create the archive event.</dd>
 * </dl>
 */
public class BasicFeatureExtraction
        extends IngestServiceBase
        implements IngestService {

    private static final Logger LOG = LoggerFactory
            .getLogger(BasicFeatureExtraction.class);

    private List<Job<?, ?>> jobs;

    private BasicExecutionEnvironment runner;

    @Required
    public void setJobs(List<Job<?, ?>> jobs) {
        this.jobs = jobs;
    }

    @Required
    public void setExecutionEnvironment(BasicExecutionEnvironment env) {
        runner = env;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(String sipRef) throws IngestServiceException {
        for (Job<?, ?> job : jobs) {
            SipDcpReader reader =
                    new SipDcpReader(ingest.getSipStager().getSIP(sipRef));
            Dcp dcp = null;
            OutputFactory<?, ?> instanceFactory = job.outputFactory.newInstance();
            try {
                dcp = ingest.getSipStager().getSIP(sipRef);

                ExceptionListener[] listeners = new ExceptionListener[0];

                if (job.exceptionListener != null) {
                    listeners = new ExceptionListener[] {job.exceptionListener};
                }
                
                runner.execute(reader,
                               (Mapping<String, Dcp, Object, Object>) job.mapping,
                               (Output<Object, Object>) instanceFactory.newOutput(),
                               listeners);

                instanceFactory.close(true);
                if (job.outcomeListener != null) {
                    job.outcomeListener.onSuccess(sipRef, ingest);
                }

                if (job.logOutcome) {
                    log(sipRef, dcp, job.label, TRANSFORM);
                }
            } catch (Exception e) {

                instanceFactory.close(false);
                if (job.outcomeListener != null) {
                    job.outcomeListener.onFailure(sipRef, ingest, e);
                }

                String msg =
                        String.format("Failed execution of job '%s'", job.label);

                if (job.allowFailure) {
                    if (job.logOutcome) {
                        log(sipRef, dcp, job.label, TRANSFORM_FAIL);
                    }
                    LOG.debug(msg, e);
                } else {
                    LOG.warn(msg, e);
                    throw new IngestServiceException(msg, e);
                }
            }
        }
    }

    private void log(String sipRef, Dcp dcp, String label, String eventType) {

        DcsEvent outcome = ingest.getEventManager().newEvent(eventType);

        outcome.setOutcome("ingest " + sipRef);
        outcome.setDetail(label);
        for (DcsEntity du : dcp.getDeliverableUnits()) {
            outcome.addTargets(new DcsEntityReference(du.getId()));
        }
        for (DcsEntity du : dcp.getCollections()) {
            outcome.addTargets(new DcsEntityReference(du.getId()));
        }

        ingest.getEventManager().addEvent(sipRef, outcome);
    }

    /**
     * Transform/extraction job to be run by {@link BasicFeatureExtraction}.
     * <p>
     * Specifies a single transformation/extraction job, plus allows defining
     * extra context for logging or allowing failures without stopping ingest.
     * </p>
     * <h2>Configuration</h2>
     * <dl>
     * <dt>{@link #setMapping(Mapping)}</dt>
     * <dd><b>Required</b>. The {@link Mapping} used in this job</dd>
     * <dt>{@link #setOutput(Output)}</dt>
     * <dd><b>Required</b>. The {@link Output} used in this job.</dd>
     * <dt>{@link #setFailureAllowed(boolean)}</dt>
     * <dd>Allows failure of this job without stopping ingest or other
     * extractions. Default is false (i.e. an exception will stop ingest)</dd>
     * <dt>{@link #setLabel(String)}</dt>
     * <dd>Assign a label/name to this job. Used to assist in logging and
     * reporting.</dd>
     * <dt>{@link #setCreateEvent(boolean)}</dt>
     * <dd>Determine of the success/failure of transformation job should be
     * logged as a {@link DcsEvent} of type {@link Events#TRANSFORM} or
     * {@link Events#TRANSFORM_FAIL}. Default is false (i.e. no event will be
     * created)</dd>
     * <dt>{@link #setOutcomeListener(OutcomeListener)}</dt>
     * <dd>Notify the given {@link OutcomeListener} when a job completes
     * normally or abnormally.</dd>
     * </dl>
     */
    public static class Job<Ko, Vo> {

        Mapping<String, Dcp, Ko, Vo> mapping;

        OutputFactory<Ko, Vo> outputFactory;

        String label = toString();

        OutcomeListener outcomeListener = null;

        ExceptionListener exceptionListener = null;

        boolean allowFailure = false;

        boolean logOutcome = false;

        @Required
        public void setMapping(Mapping<String, Dcp, Ko, Vo> mapping) {
            this.mapping = mapping;
        }

        @Required
        public void setOutputFactory(OutputFactory<Ko, Vo> output) {
            this.outputFactory = output;
        }

        public void setFailureAllowed(boolean fail) {
            allowFailure = fail;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setCreateEvent(boolean log) {
            logOutcome = log;
        }

        public void setOutcomeListener(OutcomeListener listener) {
            outcomeListener = listener;
        }

        public void setExceptionListener(ExceptionListener listener) {
            exceptionListener = listener;
        }
    }

    /**
     * Allows user-defined behaviour upon success or failure of a transform job.
     * <p>
     * Called when a feature extraction/transformation job completes.
     * </p>
     */
    public interface OutcomeListener {

        /**
         * Called when a job successfully finishes
         * 
         * @param sipRef
         *        Sip currently being ingested and acted upon by the feature
         *        extraction framework.
         * @param ingest
         *        Current ingest framework.
         */
        public void onSuccess(String sipRef, IngestFramework ingest);

        /**
         * Called when a job fails with an exception
         * 
         * @param sipRef
         *        Sip currently being ingested and acted upon by the feature
         *        extraction framework.
         * @param ingest
         *        Current ingest framework.
         * @param e
         *        Exception thrown by feature extraction/transformation job.
         */
        public void onFailure(String sipRef, IngestFramework ingest, Exception e);

    }
}

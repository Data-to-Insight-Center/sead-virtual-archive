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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.dcs.ingest.services.BasicFeatureExtraction.Job;
import org.dataconservancy.dcs.ingest.services.BasicFeatureExtraction.OutcomeListener;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.execution.BasicExecutionEnvironment;
import org.dataconservancy.transform.execution.OutputFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.dataconservancy.dcs.ingest.Events.TRANSFORM;
import static org.dataconservancy.dcs.ingest.Events.TRANSFORM_FAIL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BasicFeatureExtractionTest {

    private static IdService ids = new MemoryIdServiceImpl();

    private static BulkIdCreationService bulkIdService = (BulkIdCreationService) ids;

    private static SipStager stager = new MemoryStager();

    private static IngestFramework ingest = new IngestFramework();

    private static SimpleLinkValidator validator = new SimpleLinkValidator();

    private static final String EXAMPLE_SIP =
            "/org/dataconservancy/dcs/ingest/services/exampleDIP.xml";

    private final TestOutputFactory outputFactory = new TestOutputFactory();

    private DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    @BeforeClass
    public static void setUp() {
        InlineEventManager mgr = new InlineEventManager();
        mgr.setIdService(bulkIdService);
        mgr.setSipStager(stager);

        ingest.setSipStager(stager);
        ingest.setEventManager(mgr);

        validator.setIngestFramework(ingest);
        validator.setIdentifierService(ids);
    }

    @Test
    public void sipSubmissionTest() throws Exception {

        BasicFeatureExtraction featureExtractionService =
                new BasicFeatureExtraction();
        featureExtractionService.setIngestFramework(ingest);
        featureExtractionService
                .setExecutionEnvironment(new BasicExecutionEnvironment());

        List<Job<?, ?>> jobs = new ArrayList<Job<?, ?>>();

        TestMapping mapping = new TestMapping();

        Job<Object, Object> job = new Job<Object, Object>();
        job.setMapping(mapping);
        job.setOutputFactory(outputFactory);
        jobs.add(job);

        featureExtractionService.setJobs(jobs);

        Dcp sip = builder.buildSip(getClass().getResourceAsStream(EXAMPLE_SIP));
        Collection<DcsDeliverableUnit> duSet = sip.getDeliverableUnits();
        String id = ingest.getSipStager().addSIP(sip);

        featureExtractionService.execute(id);

        assertTrue("Mapping not called", mapping.mappingCalled);

        assertEquals(1, outputFactory.latest.values.size());

        assertTrue(duSet.containsAll(((Dcp) outputFactory.latest.values.get(0))
                .getDeliverableUnits()));

    }

    @Test
    public void executesAllJobsTest() throws Exception {
        simpleSubmit(50, false, null);
    }

    @Test
    public void successEventWhenRequestedTest() throws Exception {
        String sip = simpleSubmit(10, true, null);

        assertEquals(10, ingest.getEventManager().getEvents(sip, TRANSFORM)
                .size());

        assertEquals(0, ingest.getEventManager().getEvents(sip, TRANSFORM_FAIL)
                .size());
    }

    @Test
    public void noSuccessEventWhenNotRequestedTest() throws Exception {
        String sip = simpleSubmit(10, false, null);

        assertEquals(0, ingest.getEventManager().getEvents(sip, TRANSFORM)
                .size());
    }

    @Test
    public void successEventLabelTest() throws Exception {
        String label = "TEST_LABEL";
        String sip = simpleSubmit(1, true, null, label);

        assertEquals(label, ingest.getEventManager().getEvents(sip, TRANSFORM)
                .iterator().next().getDetail());
    }

    @Test
    public void outcomeCalledSuccessTest() throws Exception {

        final List<String> success = new ArrayList<String>();

        simpleSubmit(10, true, new OutcomeListener() {

            public void onSuccess(String sipRef, IngestFramework ingest) {
                assertNotNull(ingest.getSipStager().getSIP(sipRef));
                success.add(sipRef);
            }

            public void onFailure(String sipRef,
                                  IngestFramework ingest,
                                  Exception e) {
                fail("job should not have terminated successfully");
            }
        });

        assertEquals(10, success.size());
    }

    private String simpleSubmit(int numberOfMappings,
                                boolean doLog,
                                OutcomeListener listener,
                                String... label) throws Exception {
        BasicFeatureExtraction featureExtractionService =
                new BasicFeatureExtraction();
        featureExtractionService.setIngestFramework(ingest);
        featureExtractionService
                .setExecutionEnvironment(new BasicExecutionEnvironment());

        List<Job<?, ?>> jobs = new ArrayList<Job<?, ?>>();

        for (int i = 0; i < numberOfMappings; i++) {
            Job<Object, Object> job = new Job<Object, Object>();
            job.setMapping(new TestMapping());
            job.setOutputFactory(outputFactory);
            job.setCreateEvent(doLog);

            if (listener != null) {
                job.setOutcomeListener(listener);
            }

            if (label.length > 0) {
                job.setLabel(label[0]);
            }
            jobs.add(job);
        }

        featureExtractionService.setJobs(jobs);

        Dcp sip = builder.buildSip(getClass().getResourceAsStream(EXAMPLE_SIP));
        String id = ingest.getSipStager().addSIP(sip);

        featureExtractionService.execute(id);

        for (Job<?, ?> job : jobs) {
            assertTrue(((TestMapping) job.mapping).mappingCalled);
        }

        return id;
    }

    @Test(expected = IngestServiceException.class)
    public void exceptionHandlingTest() throws Exception {
        doExceptionTest(1, false, false, null);
    }

    @Test
    public void allowFailureTest() throws Exception {
        doExceptionTest(1, true, false, null);
    }

    @Test
    public void allowAllFailedJobsTest() throws Exception {
        doExceptionTest(10, true, false, null);
    }

    @Test
    public void failureEventWhenRequestedTest() throws Exception {
        String sip = doExceptionTest(10, true, true, null);

        assertEquals(10, ingest.getEventManager()
                .getEvents(sip, TRANSFORM_FAIL).size());
        assertEquals(0, ingest.getEventManager().getEvents(sip, TRANSFORM)
                .size());
    }

    @Test
    public void failureEventWhenNotRequestedTest() throws Exception {
        String sip = doExceptionTest(10, true, false, null);

        assertEquals(0, ingest.getEventManager().getEvents(sip, TRANSFORM_FAIL)
                .size());
    }

    @Test
    public void failureEventLabelTest() throws Exception {
        String label = "TEST_LABEL";
        String sip = doExceptionTest(1, true, true, null, label);

        assertEquals(label,
                     ingest.getEventManager().getEvents(sip, TRANSFORM_FAIL)
                             .iterator().next().getDetail());
    }

    @Test
    public void outcomeCalledfailureAllowedTest() throws Exception {

        final List<Exception> exceptions = new ArrayList<Exception>();

        doExceptionTest(10, true, true, new OutcomeListener() {

            public void onSuccess(String sipRef, IngestFramework ingest) {
                fail("job should not have terminated successfully");
            }

            public void onFailure(String sipRef,
                                  IngestFramework ingest,
                                  Exception e) {
                exceptions.add(e);
                assertNotNull(ingest.getSipStager().getSIP(sipRef));
            }
        });

        assertEquals(10, exceptions.size());
    }

    @Test
    public void outcomeCalledfailureNotAllowedTest() throws Exception {

        final List<Exception> exceptions = new ArrayList<Exception>();

        try {
            doExceptionTest(1, false, true, new OutcomeListener() {

                public void onSuccess(String sipRef, IngestFramework ingest) {
                    fail("job should not have terminated successfully");
                }

                public void onFailure(String sipRef,
                                      IngestFramework ingest,
                                      Exception e) {
                    exceptions.add(e);
                    assertNotNull(ingest.getSipStager().getSIP(sipRef));
                }
            });
        } catch (Exception e) {
        } finally {
            /* Should have had an opportunity to record an exception */
            assertEquals(1, exceptions.size());
        }
    }

    private String doExceptionTest(int numberOfMappings,
                                   boolean allowException,
                                   boolean doLog,
                                   OutcomeListener listener,
                                   String... label) throws Exception {
        BasicFeatureExtraction featureExtractionService =
                new BasicFeatureExtraction();
        featureExtractionService.setIngestFramework(ingest);
        featureExtractionService
                .setExecutionEnvironment(new BasicExecutionEnvironment());

        List<Job<?, ?>> jobs = new ArrayList<Job<?, ?>>();

        for (int i = 0; i < numberOfMappings; i++) {
            TestMapping mapping = new TestMapping();
            mapping.doException = true;
            Job<Object, Object> job = new Job<Object, Object>();
            job.setMapping(mapping);
            job.setOutputFactory(outputFactory);
            job.setCreateEvent(doLog);
            job.setFailureAllowed(allowException);

            if (listener != null) {
                job.setOutcomeListener(listener);
            }

            if (label.length > 0) {
                job.setLabel(label[0]);
            }
            jobs.add(job);
        }

        featureExtractionService.setJobs(jobs);

        Dcp sip = builder.buildSip(getClass().getResourceAsStream(EXAMPLE_SIP));
        String id = ingest.getSipStager().addSIP(sip);

        featureExtractionService.execute(id);

        for (Job<?, ?> job : jobs) {
            assertTrue(((TestMapping) job.mapping).mappingCalled);
        }

        return id;
    }

    @Test
    public void springDefinedMappingsTest() throws Exception {
        ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext(new String[] {
                        "ingestFramework.xml", "featureExtraction.xml"});
        IngestService featureExtractionService =
                (IngestService) appContext.getBean("featureExtraction");

        IngestFramework testIngest =
                (IngestFramework) appContext
                        .getBean("org.dataconservancy.dcs.ingest.IngestFramework");

        Dcp sip = builder.buildSip(getClass().getResourceAsStream(EXAMPLE_SIP));

        String id = testIngest.getSipStager().addSIP(sip);

        featureExtractionService.execute(id);

        TestOutput output =
                ((SingularOutputFactory) appContext.getBean("output")).out;

        assertEquals(5, output.keys.size());
        assertEquals(5, output.values.size());

        for (int i = 0; i < 5; i++) {
            assertEquals(Integer.toString(i), output.values.get(i));
        }
    }

    public static class TestMapping
            implements Mapping<String, Dcp, Object, Object> {

        boolean mappingCalled = false;

        boolean doException = false;

        Object value = null;

        public TestMapping() {

        }

        public TestMapping(Object constantValue) {
            value = constantValue;
        }

        @Override
        public void map(String key, Dcp val, Output<Object, Object> writer) {
            mappingCalled = true;
            if (doException) {
                throw new RuntimeException();
            } else {
                writer.write(key, value != null ? value : val);
            }

        }
    }

    public static class TestOutput
            implements Output<Object, Object> {

        List<Object> keys = new ArrayList<Object>();

        List<Object> values = new ArrayList<Object>();

        public void write(Object key, Object value) {
            keys.add(key);
            values.add(value);
        }

        public void close() {
            /* Do nothing */
        }
    }

    public static class TestOutputFactory
            implements OutputFactory<Object, Object> {

        TestOutput latest;

        @Override
        public TestOutput newOutput() {
            latest = new TestOutput();
            return latest;
        }

        @Override
        public void close(boolean... success) {
            /* Do nothing */
        }

        @Override
        public OutputFactory<Object, Object> newInstance() {
            return this;
        }
    }

    public static class SingularOutputFactory
            implements OutputFactory<Object, Object> {

        TestOutput out = new TestOutput();

        @Override
        public TestOutput newOutput() {
            return out;
        }

        @Override
        public void close(boolean... success) {
            /* Do nothing */
        }

        @Override
        public OutputFactory<Object, Object> newInstance() {
            return this;
        }
    }
}

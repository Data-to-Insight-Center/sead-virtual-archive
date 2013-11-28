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
package org.dataconservancy.model.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
class TestCaseResolver {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String TESTCASE_DESCRIPTOR = "/dcs-model-testcases.properties";
    private final List<TestCase> testCases;

    public TestCaseResolver() {
        this.testCases = loadTestCases();
    }

    public List<TestCase> getTestCases() {
        return Collections.unmodifiableList(this.testCases);
    }

    /**
     * Abstraction for loading resources from classpaths, filesystems, or URLs.
     * See: http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/resources.html
     */
    private final ResourceLoader resourceLoader = new DefaultResourceLoader(DcsModelBuilderTest.class.getClassLoader());

    private List<TestCase> loadTestCases() {
        // Load the testcase descriptors
        final InputStream in = DcsModelBuilderTest.class.getResourceAsStream(TESTCASE_DESCRIPTOR);
        assertNotNull("Could not find test case descriptors resource '" + TESTCASE_DESCRIPTOR + "' on the classpath.");

        final Properties testProps = new Properties();
        try {
            testProps.load(in);
        } catch (IOException e) {
            final String errMsg = "Unable to load test case descriptors from classpath resource '" + TESTCASE_DESCRIPTOR + "': " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        final SortedSet<String> testIds = new TreeSet<String>();
        for (String propName : testProps.stringPropertyNames()) {
            final String testId = propName.substring(0, propName.indexOf("."));
            testIds.add(testId);
        }

        final List<TestCase> testCases = new ArrayList<TestCase>();

        for (String testId : testIds) {
            // description
            // entity
            // expectedException
            // resource
            final String descriptionValue = testProps.getProperty(testId + ".description");
            final String entityValue = testProps.getProperty(testId + ".entity");
            final String expectedExceptionValue = testProps.getProperty(testId + ".expectedException");
            final String resourceValue = testProps.getProperty(testId + ".resource");

            final Class entityClass;
            try {
                entityClass = Class.forName(entityValue);
            } catch (ClassNotFoundException e) {
                final String errMsg = "Could not load entity class '" + entityValue + "'";
                log.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
            final TestResult.Result result;
            final Class exceptionClass;

            if (!isEmptyOrNull(expectedExceptionValue)) {
                try {
                    exceptionClass = Class.forName(expectedExceptionValue);
                    result = TestResult.Result.FAIL;
                } catch (ClassNotFoundException e) {
                    final String errMsg = "Could not load expected exception class '" + expectedExceptionValue + "'";
                    log.error(errMsg, e);
                    throw new RuntimeException(errMsg, e);
                }
            } else {
                exceptionClass = null;
                result = TestResult.Result.PASS;
            }

            final Resource resource = resourceLoader.getResource(resourceValue);

            testCases.add(new TestCase(resource, entityClass, testId, descriptionValue, result, exceptionClass));
        }

        return testCases;
    }
}

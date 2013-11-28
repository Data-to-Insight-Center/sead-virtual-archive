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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class ValidateTestCasesTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final TestCaseResolver resolver = new TestCaseResolver();

    private ModelValidator underTest;

    @Before
    public void setUp() {
        this.underTest = new ModelValidator();
    }

    @Test
    public void testValidateValidXml() throws Exception {
        log.debug("Executing testValidateValidXml");
        final List<TestCase> testcases = resolver.getTestCases();
        boolean foundPassingTestcases = false;
        for (TestCase tc : testcases) {
            if (tc.getExpectedResult() == TestResult.Result.PASS) {
                foundPassingTestcases = true;
                assertTrue("Test case resource '" + tc.getResource().getDescription() + "' does not exist.", tc.getResource().exists());
                log.debug("Validating " + tc.getResource().getFilename());
                try {
                    underTest.validate(tc.getResource().getInputStream());
                } catch (InvalidXmlException e) {
                    final StringBuilder errors = new StringBuilder("Validating " + tc.getResource().getFilename() + " failed:\n");
                    for (String msg : e.getErrorMessages()) {
                        errors.append(msg).append("\n");
                    }
                    fail(errors.toString());
                }
            }
        }

        assertTrue("Did not find any test cases to execute!", foundPassingTestcases);
    }

    @Test
    public void testValidateInvalidXml() throws Exception {
        log.debug("Executing testValidateInvalidXml");
        final List<TestCase> testcases = resolver.getTestCases();
        boolean foundFailingTestcases = false;
        for (TestCase tc : testcases) {
            if (tc.getExpectedResult() == TestResult.Result.FAIL) {
                foundFailingTestcases = true;
                assertTrue("Test case resource '" + tc.getResource().getDescription() + "' does not exist.", tc.getResource().exists());
                try {
                    log.debug("Validating " + tc.getResource().getFilename());
                    underTest.validate(tc.getResource().getInputStream());
                    fail("Expected xml resource to be invalid: '" + tc.getResource().getDescription() + "'");
                } catch (InvalidXmlException e) {
                    final StringBuilder msg = new StringBuilder("Validation errors: \n");
                    for (String error : e.getErrorMessages()) {
                        msg.append(msg).append("\n");
                    }
                    log.error(e.toString());
                }
            }
        }

        assertTrue("Did not find any test cases to execute!", foundFailingTestcases);
    }
}

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

import org.dataconservancy.model.dcs.support.Assertion;
import org.springframework.core.io.Resource;

/**
 * Encapsulates a test case XML file and the expected result of the test case.
 */
class TestCase implements Comparable<TestCase> {

    /**
     * A <code>Resource</code> pointing to the test case XML file.
     */
    private final Resource resource;

    /**
     * The expected result of executing the test case: <code>true</code> for a test case expected to pass
     * and <code>false</code> for a test case expected to fail.
     */
    private final TestResult.Result expectedResult;

    /**
     * If a test case is expected to fail, this field may be set to the exception class expected to be thrown.
     * This class may be a super type of the actual exception thrown.  This field may be null, in which case
     * no assertions will be made on the exception class thrown by the test case.
     */
    private final Class expectedException;

    private final Class entity;

    private final String id;

    private final String description;

    /**
     * Constructor creating a test case which is expected to pass.
     *
     * @param resource a <code>Resource</code> pointing to the test case XML file (required)
     * @param entity the entity under test (required)
     * @param id the name of the test case (required)
     * @param description a short description of the test case
     * @throws IllegalArgumentException if any of the required arguments are <code>null</code> or the empty <code>String</code>
     */
    TestCase(Resource resource, Class entity, String id, String description) {
        Assertion.notNull(resource);
        Assertion.notNull(entity);
        Assertion.notEmptyOrNull(id);

        this.resource = resource;
        this.entity = entity;
        this.id = id;
        this.description = description;
        this.expectedResult = TestResult.Result.PASS;
        this.expectedException = null;
    }

    /**
     * Constructs a test case.  If a test is expected to fail, the <code>expectedException</code> parameter can
     * be set.
     *
     * @param resource a <code>Resource</code> pointing to the test case XML file (required)
     * @param entity the entity under test (required)
     * @param id the name of the test case (required)
     * @param description a short description of the test case
     * @param expectedResult indicating whether or not the test should pass (required)
     * @param expectedException the exception this test should throw when failing
     * @throws IllegalArgumentException if any of the required arguments are <code>null</code> or the empty <code>String</code>
     */
    TestCase(Resource resource, Class entity, String id, String description, TestResult.Result expectedResult, Class expectedException) {
        Assertion.notNull(resource);
        Assertion.notNull(entity);
        Assertion.notEmptyOrNull(id);
        Assertion.notNull(expectedResult);

        this.resource = resource;
        this.expectedResult = expectedResult;
        this.expectedException = expectedException;
        this.entity = entity;
        this.id = id;
        this.description = description;
    }

    Resource getResource() {
        return resource;
    }

    TestResult.Result getExpectedResult() {
        return expectedResult;
    }

    Class getExpectedException() {
        return expectedException;
    }

    Class getEntity() {
        return entity;
    }

    String getId() {
        return id;
    }

    String getDescription() {
        return description;
    }

    /**
     * Provides for ordering of test cases by their test names.
     * <p/>
     *
     * @inheritDoc
     */
    @Override
    public int compareTo(TestCase testCase) {
        return this.id.compareTo(testCase.id);
    }

}

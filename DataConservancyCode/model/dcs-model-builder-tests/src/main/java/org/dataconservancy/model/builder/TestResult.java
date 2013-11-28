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

/**
 * Encapsulates the result of a test case: the success or failure of the test, the name of the test, and an optional
 * message which can contain, for example, a stacktrace.
 */
class TestResult {

    /**
     * Possible results of a test execution.
     */
    enum Result {
        /** The test passed */
        PASS,
        /** The test failed */
        FAIL
    }

    /**
     * The test case that this applies to
     */
    private final TestCase testCase;

    /**
     * The result of executing the test case.
     */
    private final Result actualResult;

    /**
     * A <code>Throwable</code> thrown by the execution of a test.
     */
    private final Throwable actualException;

    /**
     * A message associated with the execution of a test.  For example, in the case of a test failure this may contain
     * a stacktrace.
     */
    private String msg;

    /**
     * Constructs a test result.
     *
     * @param tc the testcase that this result is for (may not be <code>null</code>)
     * @param actualResult the result of executing the test case (may not be <code>null</code>)
     * @param actualException the exception thrown by executing the test case (may be <code>null</code>)
     * @throws IllegalArgumentException if required parameters are null
     */
    TestResult(TestCase tc, TestResult.Result actualResult, Throwable actualException) {
        Assertion.notNull(tc);
        this.testCase = tc;
        Assertion.notNull(actualResult);
        this.actualResult = actualResult;
        this.actualException = actualException;
    }

    /**
     * Constructs a test result.
     *
     * @param tc the testcase that this result is for (may not be <code>null</code>)
     * @param actualResult the result of executing the test case (may not be <code>null</code>)
     * @param actualException the exception thrown by executing the test case (may be <code>null</code>)
     * @param msg a message
     * @throws IllegalArgumentException if required parameters are null
     */
    TestResult(TestCase tc, Result actualResult, Throwable actualException, String msg) {
        this(tc, actualResult, actualException);
        this.msg = msg;
    }

    /**
     * Evaluates the result of the test, determining whether or not the test case passed or failed.
     *
     * @return the result
     */
    Result evaluate() {
        switch (testCase.getExpectedResult()) {

            // If the test case was expected to pass:
            case PASS:
                // 1) make sure that no exceptions were thrown by the test case execution
                // 2) make sure that the actual result of the test equals the expected result.

                if (actualException == null && actualResult == testCase.getExpectedResult()) {
                    return Result.PASS;
                }
                return Result.FAIL;

            // If the test case was expected to fail:
            case FAIL:
                // 1) make sure that an exception was thrown by the test case execution
                // 2) ensure that the thrown exception was an instance of or a super type of the expected exception
                // 3) make sure that the actual result of the test equals the expected result.

                if (actualException == null) {
                    this.msg = "Test case was expected to fail with a " + testCase.getExpectedException().getName() +
                            ", but no exception was thrown.";
                    return Result.FAIL;
                }

                if (testCase.getExpectedException().isAssignableFrom(actualException.getClass())
                        && actualResult == testCase.getExpectedResult()) {
                        return Result.PASS;
                }                

                this.msg = "Test case was expected to fail with a " + testCase.getExpectedException().getName() + ", " +
                        "but a " + actualException.getClass().getName() + " was thrown instead.";
                return Result.FAIL;

            // Sanity check
            default:
                throw new RuntimeException("Unhandled test case result type: " + testCase.getExpectedResult());
        }
    }

    /**
     * The test case that this result applies to.
     *
     * @return the test case
     */
    TestCase getTestCase() {
        return testCase;
    }

    /**
     * The actual exception thrown by the execution of the test case.
     *
     * @return the exception, may be <code>null</code>
     */
    Throwable getActualException() {
        return actualException;
    }

    /**
     * Any messages associated with the the test.  For example, a stacktrace.
     *
     * @return the message
     */
    String getMsg() {
        return msg;
    }    
}

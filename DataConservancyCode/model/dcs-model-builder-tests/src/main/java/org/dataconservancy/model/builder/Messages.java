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

/**
 * Contains strings used for formatting messages.
 */
class Messages {


    /**
     * A test case has been added for execution.
     * Parameters: test case description, test case id, test case path
     */
    final static String TEST_ADD = "Adding test case %s (id: %s) from resource (classpath or file) %s";
    
    /**
     * A test case is executing.
     * Parameters: Test case file name
     */
    final static String TEST_EXE = "Executing test case %s";

    /**
     * The results of a test case are being evaluated
     * Parameters: Test case description, test case result
     */
    final static String TEST_EVAL = "Evaluating results of test case %s: %s";

    /**
     * A test case passed.
     */
    final static String TEST_EVAL_PASS = "PASSED";

    /**
     * A test case failed.
     */
    final static String TEST_EVAL_FAIL = "FAILED";


    /**
     * A test case could not be deserialized from XML to a Java object.
     * Parameters: reason
     */
    final static String TEST_FAIL_DESERIALIZATION = "Deserialization FAILED: %s";


    /**
     * A test case could not be serialized from a Java object to XML.
     * Parameters: reason
     */
    final static String TEST_FAIL_SERIALIZATION = "Serialization FAILED: %s";

    /**
     * A test case could not be serialized.  Includes the actual XML resulting from the serialization, and the expected
     * xml.
     * Parameters: reason, actual xml, expected xml
     */
    final static String TEST_FAIL_SERIALIZATION_WITH_DIFF = "Serialization FAILED: %s  Actual: \n[%s]\n Expected: \n[%s]\n";

    /**
     * A test case failed validation tests (the serialized XML was not valid).  Includes the actual XML resulting from
     * the serialization.
     * Parameters: Test case file name, reason, actual xml
     */
    final static String TEST_FAIL_VALIDATION = "%s: Validation FAILED: %s  Actual: \n[%s]\n";

    /**
     * A test case failed validation tests.  Includes the actual XML resulting from the serialization, and the expected
     * xml.
     * Parameters: reason, actual xml, expected xml
     */
    final static String TEST_FAIL_VALIDATION_WITH_DIFF = "Validation FAILED: %s  Actual: \n[%s]\n Expected: \n[%s]\n";
}

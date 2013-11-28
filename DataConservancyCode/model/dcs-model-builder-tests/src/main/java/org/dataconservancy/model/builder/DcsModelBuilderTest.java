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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.dataconservancy.model.builder.Messages.TEST_ADD;
import static org.dataconservancy.model.builder.Messages.TEST_EVAL;
import static org.dataconservancy.model.builder.Messages.TEST_EVAL_FAIL;
import static org.dataconservancy.model.builder.Messages.TEST_EVAL_PASS;
import static org.dataconservancy.model.builder.Messages.TEST_EXE;
import static org.dataconservancy.model.builder.Messages.TEST_FAIL_DESERIALIZATION;
import static org.dataconservancy.model.builder.Messages.TEST_FAIL_SERIALIZATION;
import static org.dataconservancy.model.builder.Messages.TEST_FAIL_SERIALIZATION_WITH_DIFF;
import static org.dataconservancy.model.builder.TestResult.Result;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Functional tests for the {@link org.dataconservancy.model.builder.DcsModelBuilder builder interface}.  Subclasses
 * extend this class and implement {@link #getUnderTest()}, which provides this test an implementation of a
 * <code>DcsModelBuilder</code>.
 * <p/>
 * There are two kinds of tests in this test fixture.  The first kind of tests should be familiar to anyone who
 * has used JUnit before: they are simple test methods with little or no logic in them.  For example:
 * <pre>
    &#64;Test(expected = IllegalArgumentException.class)
    public void testSerializeDeliverableUnitWithNullSink() {
        underTest.buildDeliverableUnit(new DcsDeliverableUnit(), null);
    }
 * </pre>
 * The second kind of tests in this fixture are more complex, utilizing processing logic to perform the same test
 * over multiple inputs.
 * <p/>
 * The processing logic is as follows:
 * <ol>
 *     <li>A list of test cases are compiled.  This is done only once (see {@link #loadTestCaseDescriptors()}).</li>
 *     <li>Obtain an instance of the <code>DcsModelBuilder</code> under test.  This is done once for each test method.</li>
 *     <li>The JUnit test method is executed:</li>
 *     <ul>
 *         <li>Test cases pertinent to the test method are selected</li>
 *         <li>Each test case is executed</li>
 *         <li>The test case results are evaluated for success or failure</li>
 *         <li>If all test cases pass, then the JUnit test method passes</li>
 *     </ul>
 * </ol>
 * The fixture is designed to execute all of the test cases.  Individual test cases may pass or fail, but in order for
 * an implementation to be compliant with the <code>DcsModelBuilder</code> interface, all test cases must pass.  Test
 * cases are XML documents, which are crafted to test some functionality of the interface.  Some test cases are
 * valid DCS entities or DCPs, other test cases contain malformed XML, invalid XML, or even non-XML content.  Test
 * cases can be added over time, as bugs or other features are added to the <code>DcsModelBuilder</code> interface.
 * <p/>
 * {@link TestCase Test cases} are defined in the properties file <code>dcs-model-testcases.properties</code> found at
 * the root of the class path (i.e. <code>Class.getResourceAsStream(/dcs-model-testcases.properties)</code>).  The
 * {@link TestCaseResolver} is responsible for interrogating this file and providing this test with a list of test cases.
 *
 * @see TestCase
 * @see TestCaseResolver
 */
public abstract class DcsModelBuilderTest {

    private static final Logger LOG = LoggerFactory.getLogger(DcsModelBuilderTest.class);

    /**
     * Static list of test cases, assembled from the TestCaseResolver
     */
    private static List<TestCase> TEST_DESCRIPTORS = new ArrayList<TestCase>();

    /**
     * The DCS model builder implementation under test.  Supplied by subclasses.
     */
    private DcsModelBuilder underTest;

    /**
     * Abstraction for loading resources from classpaths, filesystems, or URLs.
     * See: http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/resources.html
     */
    private final ResourceLoader resourceLoader = new DefaultResourceLoader(DcsModelBuilderTest.class.getClassLoader());

    /**
     * A holder for the test results, keyed by the name of the test case.
     */
    private Map<String, TestResult> testResults;

    /**
     * The schema used to validate the xml.
     */
    private Resource dcpSchema;

    /**
     * Reads the test case descriptors properties file, and creates a List of TestCase objects.
     */
    @BeforeClass
    public static void loadTestCaseDescriptors() {
        final TestCaseResolver resolver = new TestCaseResolver();
        TEST_DESCRIPTORS = resolver.getTestCases();
    }

    /**
     * Sets options on XMLUnit, locates the schema used to validate the DCP xml, validates the schema document itself,
     * and ensures we have an implementation to test.
     */
    @Before
    public void setUp() throws IOException {

        // Set XMLUnit options
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);

        // Locate the schema
        final String schemaResourcePath = "classpath:/schema/dcp.xsd";
        dcpSchema = resourceLoader.getResource(schemaResourcePath);
        assertNotNull("Could not find the schema " + schemaResourcePath, dcpSchema);
        assertTrue("Schema does not exist at " + schemaResourcePath, dcpSchema.exists());

        // Validate the schema itself
        try {
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(dcpSchema.getInputStream()));
        } catch (SAXException e) {
            fail("Schema document '" + dcpSchema.getURI().toString() + "' is invalid: " + e.getMessage());
        }

        // Ensure we have an implementation to test!
        assertNotNull("Builder instance under test must not be null!", this.underTest = getUnderTest());
    }

    /**
     * Iterates over the test results, failing this JUnit test if any of the results indicate failure.
     */
    private void evaluateResults() {

        assertNotNull("No test results were found (results were null)", testResults);

        boolean failures = false;
        final StringBuilder failureMsg = new StringBuilder("There were test failures.\n");
        for (Map.Entry<String, TestResult> r : testResults.entrySet()) {
            switch (r.getValue().evaluate()) {
                case PASS:
                    LOG.debug(String.format(TEST_EVAL, r.getValue().getTestCase().getDescription(), TEST_EVAL_PASS));
                    break;
                
                case FAIL:
                    failures = true;
                    failureMsg.append(r.getKey()).append(": ").append(r.getValue().getMsg()).append("\n");
                    final Throwable t = r.getValue().getActualException();
                    if (t != null) {
                        final ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
                        t.printStackTrace(new PrintStream(stackTrace));
                        failureMsg.append(new String(stackTrace.toByteArray())).append("\n");
                    }
                    LOG.debug(String.format(TEST_EVAL, r.getValue().getTestCase().getDescription(), TEST_EVAL_FAIL));
                    break;
            }
        }

        testResults = null;
        if (failures) {
            fail(failureMsg.toString());
        }
    }
    
    /**
     * Return an instance of the builder to be tested.  This method will be invoked once for each test
     * method.
     *
     * @return the builder instance
     */
    public abstract DcsModelBuilder getUnderTest();

    @Test
    public void testDeliverableUnit() throws IOException, SAXException {
        final List<TestCase> testcases = getTestcaseListing(DcsDeliverableUnit.class);
        testResults = runTestcases(testcases, DcsDeliverableUnit.class);
        evaluateResults();
    }

    @Test
    public void testManifestation() throws IOException, SAXException {
        final List<TestCase> testcases = getTestcaseListing(DcsManifestation.class);
        testResults = runTestcases(testcases, DcsManifestation.class);
        evaluateResults();
    }

    @Test
    public void testFile() throws IOException, SAXException {
        final List<TestCase> testcases = getTestcaseListing(DcsFile.class);
        testResults = runTestcases(testcases, DcsFile.class);
        evaluateResults();
    }

    @Test
    public void testSip() throws IOException, SAXException {
        final List<TestCase> testcases = getTestcaseListing(Dcp.class);
        testResults = runTestcases(testcases, Dcp.class);
        evaluateResults();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSerializeCollectionWithNullCollection() {
        underTest.buildCollection(null, new NullOutputStream());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSerializeCollectionWithNullSink() {
        underTest.buildCollection(new DcsCollection(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeCollectionWithNullInputStream() throws InvalidXmlException {
        underTest.buildCollection(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSerializeDeliverableUnitWithNullDeliverableUnit() {
        underTest.buildDeliverableUnit(null, new NullOutputStream());        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSerializeDeliverableUnitWithNullSink() {
        underTest.buildDeliverableUnit(new DcsDeliverableUnit(), null);        
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeDeliverableWithNullInputStream() throws InvalidXmlException {
        underTest.buildDeliverableUnit(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSerializeEventWithNullEvent() {
        underTest.buildEvent(null, new NullOutputStream());        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSerializeEventWithNullSink() {
        underTest.buildEvent(new DcsEvent(), null);        
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeEventWithNullInputStream() throws InvalidXmlException {
        underTest.buildEvent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSerializeFileWithNullEvent() {
        underTest.buildFile(null, new NullOutputStream());        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSerializeFileWithNullSink() {
        underTest.buildFile(new DcsFile(), null);        
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeFileWithNullInputStream() throws InvalidXmlException {
        underTest.buildFile(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSerializeManifestationWithNullManifestation() {
        underTest.buildManifestation(null, new NullOutputStream());        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSerializeManifestationWithNullSink() {
        underTest.buildManifestation(new DcsManifestation(), null);        
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeManifestationWithNullInputStream() throws InvalidXmlException {
        underTest.buildManifestation(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSerializeSipWithNullSip() {
        underTest.buildSip(null, new NullOutputStream());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSerializeSipWithNullSink() {
        underTest.buildSip(new Dcp(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeSipWithNullInputStream() throws InvalidXmlException {
        underTest.buildSip(null);        
    }
    
    /**
     * Executes the provided test cases.  This method does not evaluate the results.
     * The success or failure of each test case is indicated in the returned Map.
     * <p/>
     * Executing a test case currently means:
     * <ol>
     *   <li>Deserializing the test case XML to an object</li>
     *   <li>Serializing the object to XML</li>
     *   <li>Comparing the test case XML to the serialized XML for equivalence</li>
     * </ol>
     *
     * @param testcases <code>Resource</codes>es of test case XML documents.
     * @param forEntityClass the DCS model object being tested.
     * @return a Map of test results, keyed by test name.
     */
    private Map<String, TestResult> runTestcases(List<TestCase> testcases, Class forEntityClass) throws IOException, SAXException {
        final Map<String, TestResult> results = new HashMap<String, TestResult>();

        for (TestCase testcase : testcases) {
            final Resource testResource = testcase.getResource();
            final String name = testcase.getResource().getFilename();
            LOG.debug(String.format(TEST_EXE, name));

            Object deserializedObj = null;

            try {
                if (forEntityClass == DcsFile.class) {
                    deserializedObj = underTest.buildFile(testResource.getInputStream());
                } else if (forEntityClass == DcsManifestation.class) {
                    deserializedObj = underTest.buildManifestation(testResource.getInputStream());
                } else if (forEntityClass == DcsDeliverableUnit.class) {
                    deserializedObj = underTest.buildDeliverableUnit(testResource.getInputStream());
                } else if (forEntityClass == Dcp.class) {
                    deserializedObj = underTest.buildSip(testResource.getInputStream());
                } else {
                    fail("Unhandled test case type " + forEntityClass.getName());
                }
            } catch (Exception e) {
                final String errMsg = String.format(TEST_FAIL_DESERIALIZATION, e.getMessage());
                results.put(name, new TestResult(testcase, Result.FAIL, e, errMsg));
                continue;
            }

            try {
                // Assert an object was created
                assertNotNull(deserializedObj);
            } catch (Throwable t) {
                final String errMsg = String.format(TEST_FAIL_DESERIALIZATION, "Deserialized object was null.");
                results.put(name, new TestResult(testcase, Result.FAIL, t, errMsg));
                LOG.debug(name + " stackrace: \n", t);
                continue;
            }

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                // Serialize the created file to XML
                if (forEntityClass == DcsFile.class) {
                    underTest.buildFile((DcsFile) deserializedObj, out);
                } else if (forEntityClass == DcsManifestation.class) {
                    underTest.buildManifestation((DcsManifestation) deserializedObj, out);
                } else if (forEntityClass == DcsDeliverableUnit.class) {
                    underTest.buildDeliverableUnit((DcsDeliverableUnit) deserializedObj, out);
                } else if (forEntityClass == Dcp.class) {
                    underTest.buildSip((Dcp) deserializedObj, out);
                } else {
                    fail("Unhandled test case type " + forEntityClass.getName());
                }
            } catch (Exception e) {
                final String errMsg = String.format(TEST_FAIL_SERIALIZATION, e.getMessage());
                results.put(name, new TestResult(testcase, Result.FAIL, e, errMsg));
                LOG.debug(name + " stacktrace: \n", e);
                continue;
            }

            // Assert that the actual XML and the expected XML are equivalent.
            // This also ensures that the actual XML is well-formed.

            String actualXml = null;
            String expectedXml = null;
            try {
                actualXml = new String(out.toByteArray());
                // Assert the object can be serialized back to XML, and that the serialized XML is equivalent to the test case XML
                expectedXml = IOUtils.toString(testResource.getInputStream(), "UTF-8");
                final Diff diff = new Diff(expectedXml, actualXml);
                // Because the java object model deals with Sets, there is no order.  So we compare XML nodes that
                // have identical name and attributes.
                diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
                XMLAssert.assertXMLEqual(diff, true);
            } catch (Throwable t) {
                final String errMsg = String.format(TEST_FAIL_SERIALIZATION_WITH_DIFF, t.getMessage(), actualXml, expectedXml);
                results.put(name, new TestResult(testcase, Result.FAIL, t, errMsg));
                LOG.debug(name + " stacktrace: \n", t);
                continue;
            }

            // Validation is the responsibility of the client, for reasons stated on the DcsModelBuilder interface.
//
//            // Ensure that XML is valid.  We have to wrap entities in DCP xml.  The DCP SIP should not be wrapped.
//            if (forEntityClass == DcsFile.class) {
//                actualXml = String.format(FILES_WRAPPER, actualXml);
//            }
//
//            if (forEntityClass == DcsManifestation.class) {
//                actualXml = String.format(MANIFESTATIONS_WRAPPER, actualXml);
//            }
//
//            if (forEntityClass == DcsDeliverableUnit.class) {
//                actualXml = String.format(DU_WRAPPER, actualXml);
//            }
//
//            if (forEntityClass != Dcp.class) {
//                actualXml = String.format(DCP_WRAPPER, actualXml);
//            }
//
//
//            final ErrorHandlerCollector validationResult = isValid(testcase, actualXml);
//            if (validationResult.iterator().hasNext()) {
//                final StringBuilder validationErrors = new StringBuilder();
//                for (SAXParseException e : validationResult) {
//                    validationErrors.append(e.getMessage()).append("\n");
//                }
//                final String errMsg = String.format(TEST_FAIL_VALIDATION_WITH_DIFF, validationErrors.toString(), actualXml, expectedXml);
//                results.put(name, new TestResult(testcase, Result.FAIL, validationResult.iterator().next(), errMsg));
//                continue;
//            }

            results.put(name, new TestResult(testcase, Result.PASS, null));
        }

        return results;
    }

    private ErrorHandlerCollector isValid(TestCase tc, String xml) throws IOException, SAXException {
        assertNotNull(tc);
        assertNotNull(xml);
        assertFalse(xml.trim().length() == 0);

        final TestResult result;

        final Schema s = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(new StreamSource(dcpSchema.getInputStream()));

        final ErrorHandlerCollector errors = new ErrorHandlerCollector();

        final Validator v = s.newValidator();
        v.setErrorHandler(errors);
        try {
            v.validate(new StreamSource(IOUtils.toInputStream(xml)));
        } catch (SAXException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }

        return errors;
    }

    /**
     * Obtain a ordered list of test cases for the supplied DCS entity.
     *
     * @param forEntityClass the DCS entity being tested
     * @return a List of Resources, each Resource is an XML document representing a test case
     */
    private List<TestCase> getTestcaseListing(Class forEntityClass) {
        final List<TestCase> testcases = new ArrayList<TestCase>();
        for (TestCase tc : TEST_DESCRIPTORS) {
            if (tc.getEntity() == forEntityClass) {
                testcases.add(tc);
                LOG.debug(String.format(TEST_ADD, tc.getDescription(), tc.getId(), tc.getResource().getFilename()));
            }
        }
        
        assertTrue("Did not discover any test cases for " + forEntityClass.getName(), testcases.size() > 0);
        return testcases;
    }

    /**
     * SAX ErrorHandler which simply keeps references to errors in a member List.
     */
    private class ErrorHandlerCollector implements ErrorHandler, Iterable<SAXParseException> {
        private List<SAXParseException> allErrors = new ArrayList<SAXParseException>();

        @Override
        public void warning(SAXParseException e) throws SAXException {
            allErrors.add(e);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            allErrors.add(e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            allErrors.add(e);
        }

        @Override
        public Iterator<SAXParseException> iterator() {
            return allErrors.iterator();
        }
    }

}

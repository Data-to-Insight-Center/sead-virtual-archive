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
package org.dataconservancy.ui.test.support;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Used to annotate test methods or classes that modify the database.  When this annotation is found, all databases
 * will have their tables dropped and recreated.  <strong>Test classes must inherit from {@code BaseSpringAwareTest}
 * or otherwise update the {@code @TestExecutionListeners} to include the the {@code DirtiesDatabaseTestExecutionListener}</strong>,
 * otherwise this annotation will not have any affect.
 * <p/>
 * In the spirit of Spring's {@link org.springframework.test.annotation.DirtiesContext}, either test classes
 * or test methods may be annotated.  When a test method is annotated, the databases will be cleared after
 * the test method executes, and after any {@code @After} methods.  When a class is annotated, by default the databases
 * will be cleared after all {@code @Test}, {@code @After}, and {@code @AfterClass} methods execute.  The behavior
 * of the class-level annotation can be changed by setting the value of the annotation to {@link #AFTER_EACH_TEST_METHOD},
 * which will clear and rebuild the database tables as if each test method was annotated with {@code @DirtiesDatabase}.
 * Note that the value of the annotation is ignored when a test method is annotated.
 * <p/>
 * This annotation is typically inspected and acted upon by a Spring
 * {@link org.springframework.test.context.TestExecutionListener}.
 */
@Target({METHOD, TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DirtiesDatabase {

    /**
     * The possible modes of this annotation when used at the class level (ignored when test methods are annotated).
     * Each mode has an analog string constant.
     */
    public enum CLASS_MODE {

        /**
         * The database tables are cleared after the test class has executed.
         */
        AFTER_CLASS,

        /**
         * The database tables are cleared after each test method executes.
         */
        AFTER_EACH_TEST_METHOD
    }

    /**
     * The "AFTER_CLASS" mode static string constant
     */
    public static final String AFTER_CLASS = "AFTER_CLASS";

    /**
     * The "AFTER_EACH_TEST_METHOD" mode static string constant
     */
    public static final String AFTER_EACH_TEST_METHOD = "AFTER_EACH_TEST_METHOD";

    /**
     * The mode of the annotation, by default {@link #AFTER_CLASS}.
     *
     * @return the value
     */
    public String value() default AFTER_CLASS;

}

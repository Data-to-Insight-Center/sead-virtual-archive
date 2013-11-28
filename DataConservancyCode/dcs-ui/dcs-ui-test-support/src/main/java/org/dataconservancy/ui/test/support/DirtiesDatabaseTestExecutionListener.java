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

import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.lang.reflect.Method;
import java.util.Map;

import static org.dataconservancy.ui.test.support.DirtiesDatabase.CLASS_MODE.AFTER_CLASS;
import static org.dataconservancy.ui.test.support.DirtiesDatabase.CLASS_MODE.AFTER_EACH_TEST_METHOD;

/**
 * Inspects and acts on the presence of a {@link DirtiesDatabase} annotation, clearing out database tables if found.
 *
 * @see DirtiesDatabase
 */
public class DirtiesDatabaseTestExecutionListener extends AbstractTestExecutionListener {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);

        handleDirtiesDatabase(testContext, false);
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        super.afterTestClass(testContext);

        handleDirtiesDatabase(testContext, true);
    }

    private void handleDirtiesDatabase(TestContext testContext, boolean afterClass) {
        DirtiesDatabase a = null;
        boolean foundOnClass = false;

        // Search for the annotation.  If we are called from afterTestClass, then we just get the
        // annotation from the class.  If we are called from afterTestMethod, we look for the annotation
        // on the method.
        if (afterClass) {
            a = getAnnotationFromClass(testContext.getTestClass());
            foundOnClass = true;
        } else {
            a = getAnnotationFromMethod(testContext.getTestMethod(), false);
            if (a == null) {
                a = getAnnotationFromClass(testContext.getTestClass());
                foundOnClass = (a != null);
            }
        }

        // If the annotation is not found, log and return
        if (a == null) {
            log.trace("No @DirtiesDatabase annotation found on the test method or test class.");
            return;
        }

        // Get the annotation mode
        DirtiesDatabase.CLASS_MODE currentMode = getModeValue(a);

        // Log if it wasn't valid, and return.
        if (currentMode == null) {
            log.warn("No valid CLASS_MODE found for the @DirtiesDatabase annotation.  Must be one of {} or {}",
                    DirtiesDatabase.CLASS_MODE.AFTER_CLASS, AFTER_EACH_TEST_METHOD);
            return;
        }

        log.trace("Found @DirtiesDatabase with mode {} from {}", currentMode, (afterClass ? "class" : "method"));

        // If we are being invoked by afterTestMethod, and the annotation was on the method,
        // then ignore the mode, and just clear the tables.
        if (!afterClass && !foundOnClass) {
            log.trace("Clearing tables: @DirtiesDatabase was found on a method, so the mode doesn't matter.");
            clearTables(testContext);
            return;
        }

        // If we are being invoked by afterTestMethod, and the annotation was on the class,
        // inspect the mode.  If the mode is 'AFTER_EACH_TEST_METHOD', we clear the tables.
        if (!afterClass && foundOnClass && currentMode == AFTER_EACH_TEST_METHOD) {
            log.trace("Clearing tables: @DirtiesDatabase was found on the class with mode {}", currentMode);
            clearTables(testContext);
            return;
        }

        // If we are being invoked by afterTestMethod, and the annotation was on the class,
        // inspect the mode.  If the mode is 'AFTER_CLASS', we do not clear tables.
        if (!afterClass && foundOnClass && currentMode == AFTER_CLASS) {
            log.trace("Not clearing tables: @DirtiesDatabase was found on the class with mode {}", currentMode);
            return;
        }

        // If we are being invoked by afterTestClass, the annotation came from the class.
        // Inspect the mode.  If the mode is 'AFTER_CLASS', clear the tables.
        if (afterClass && currentMode == AFTER_CLASS) {
            log.trace("Clearing tables: @DirtiesDatabase was found on the class with mode {}", currentMode);
            clearTables(testContext);
            return;
        }
    }

    private DirtiesDatabase.CLASS_MODE getModeValue(DirtiesDatabase a) {
        DirtiesDatabase.CLASS_MODE mode = null;
        try {
            mode = DirtiesDatabase.CLASS_MODE.valueOf(a.value().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.trace(e.getMessage(), e);
        }

        return mode;
    }

    private DirtiesDatabase getAnnotationFromClass(Class testClass) {
        return AnnotationUtils.findAnnotation(testClass, DirtiesDatabase.class);
    }

    private DirtiesDatabase getAnnotationFromMethod(Method testMethod, boolean includeClass) {
        DirtiesDatabase a = AnnotationUtils.findAnnotation(testMethod, DirtiesDatabase.class);
        if (a == null && includeClass) {
            a = getAnnotationFromClass(testMethod.getDeclaringClass());
        }

        return a;
    }

    private void clearTables(TestContext testContext) {
        log.trace("*** Clearing Tables ***");

        JdbcTemplate t = testContext.getApplicationContext().getBean(JdbcTemplate.class);
        if (t == null) {
            throw new RuntimeException("No JdbcTemplate bean type found in the application context.");
        }

        Map<String, DataSourceInitializer> initializerMap =
                testContext.getApplicationContext().getBeansOfType(DataSourceInitializer.class);

        log.trace("Found {} initializers", initializerMap.size());

        for (Map.Entry<String, DataSourceInitializer> e : initializerMap.entrySet()) {
            String beanName = e.getKey();
            DataSourceInitializer initializer = e.getValue();

            log.trace("Invoking DataSourceInitializer with bean name {} and type {}", beanName,
                    initializer.getClass().getName());
            try {
                initializer.afterPropertiesSet();
            } catch (Exception e1) {
                log.warn("Error invoking DataSourceInitializer " + beanName + ": " + e1.getMessage(), e);
            }
        }

    }
}

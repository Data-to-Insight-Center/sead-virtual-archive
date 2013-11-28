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
package org.dataconservancy.ui.stripes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.stripes.integration.spring.SpringHelper;
import net.sourceforge.stripes.validation.ValidationError;

import org.dataconservancy.ui.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class PersonTypeConverterTest extends BaseActionBeanTest {
    private PersonTypeConverter ptc;

    @Before
    public void setup() throws Exception {
        ptc = new PersonTypeConverter();
        SpringHelper
                .injectBeans(
                        ptc,
                        (ApplicationContext) servletCtx
                                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE));

    }

    @Test
    public void testSuccessfulConversion() {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();

        Person result = ptc.convert(user.getEmailAddress(), Person.class,
                errors);

        assertEquals(0, errors.size());
        assertEquals(user, result);
    }

    @Test
    public void testFailedConversion() {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();

        Person result = ptc.convert("fail", Person.class, errors);

        assertEquals(1, errors.size());
        assertNull(result);
    }
}

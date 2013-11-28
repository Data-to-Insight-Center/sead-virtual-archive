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

import java.util.Locale;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.integration.spring.SpringHelper;
import net.sourceforge.stripes.validation.DefaultTypeConverterFactory;
import net.sourceforge.stripes.validation.TypeConverter;

import org.dataconservancy.ui.model.Person;

/**
 * Enable spring injection in TypeConverters. A TypeConverter which needs spring
 * injection should be added to the init method.
 */
public class CustomTypeConverterFactory extends DefaultTypeConverterFactory {

    public TypeConverter getInstance(Class aClass, Locale locale)
            throws Exception {
        return inject(super.getInstance(aClass, locale));
    }

    public TypeConverter getTypeConverter(Class aClass, Locale locale)
            throws Exception {
        return inject(super.getTypeConverter(aClass, locale));
    }

    protected TypeConverter inject(TypeConverter converter) {
        if (converter != null) {
            SpringHelper.injectBeans(converter, getConfiguration()
                    .getServletContext());
        }
        return converter;
    }

    public void init(Configuration configuration) {
        super.init(configuration);

        add(Person.class, PersonTypeConverter.class);
    }
}

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

import java.util.Collection;
import java.util.Locale;

import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.ValidationError;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.UserService;

/**
 * Convert a user id into a registered Person object.
 */
public class PersonTypeConverter implements TypeConverter<Person> {
    private UserService user_service;

    @SpringBean("userService")
    public void setUserService(UserService user_service) {
        this.user_service = user_service;
    }

    public Person convert(String s, Class<? extends Person> arg1,
            Collection<ValidationError> errors) {
        s = s.trim();
        Person p = user_service.get(s);

        if (p == null) {
            errors.add(new SimpleError("Invalid user id " + s));
        }

        return p;
    }

    public void setLocale(Locale arg0) {

    }
}

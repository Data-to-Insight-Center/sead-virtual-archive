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
package org.dataconservancy.ui.stripes.ext;

import java.util.Locale;

import org.dataconservancy.ui.model.Person;

import net.sourceforge.stripes.format.Formatter;

/**
 * Format a person object. By default the user id is returned.
 */
public class PersonFormatter implements Formatter<Person> {
    public String format(Person person) {
        return person.getEmailAddress();
    }

    public void init() {
    }

    public void setFormatPattern(String arg0) {
    }

    public void setFormatType(String arg0) {
    }

    public void setLocale(Locale arg0) {
    }
}

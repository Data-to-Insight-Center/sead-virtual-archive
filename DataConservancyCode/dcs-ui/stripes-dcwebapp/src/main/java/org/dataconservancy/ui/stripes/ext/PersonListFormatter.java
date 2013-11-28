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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sourceforge.stripes.config.DontAutoLoad;
import org.dataconservancy.ui.model.Person;

import net.sourceforge.stripes.format.Formatter;

// TODO This doesn't seem to get called by stripes:format for Person.getPis()
// Stripes complaint: Type parameter for non-abstract Formatter [class org.dataconservancy.ui.stripes.ext.PersonListFormatter] is not a class.
// It isn't being loaded because List is an abstract type (an Interface).  Annotating this class with @DontAutoLoad so we
// stop getting all the errors in the logs.

/**
 * Format a list of Person into comma separated email addresses.
 */
@DontAutoLoad
public class PersonListFormatter implements Formatter<List<Person>> {
    public String format(List<Person> list) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(list.get(i).getEmailAddress());
        }

        return sb.toString();
    }

    public void init() {
    }

    public void setFormatPattern(String pat) {
    }

    public void setFormatType(String type) {
    }

    public void setLocale(Locale locale) {

    }
}
